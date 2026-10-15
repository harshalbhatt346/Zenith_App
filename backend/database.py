import os
import re
import sqlite3
from pathlib import Path
from typing import Any, Optional, Tuple, Union

# Database path for local SQLite fallback
DB_PATH = Path(__file__).resolve().parent.parent / "fest.db"


def get_database_url() -> Optional[str]:
    """Returns the PostgreSQL connection URL from environment variables if set."""
    return (
        os.getenv("DATABASE_URL")
        or os.getenv("POSTGRES_URL")
        or os.getenv("POSTGRES_PRISMA_URL")
        or os.getenv("POSTGRES_URL_NON_POOLING")
    )


def is_postgres() -> bool:
    """Checks whether PostgreSQL is configured via environment variables."""
    url = get_database_url()
    return bool(url and url.strip())


class PostgresCursorWrapper:
    """
    Wraps a psycopg2 DictCursor to provide seamless compatibility with
    the existing codebase (parameter placeholder translation '?' -> '%s',
    and lastrowid support for INSERT statements).
    """

    def __init__(self, cursor: Any):
        self.cursor = cursor
        self.lastrowid: Optional[int] = None

    def execute(self, query: str, params: Optional[Union[Tuple, list]] = None):
        # Translate '?' placeholders to '%s' for psycopg2
        pg_query = query.replace("?", "%s")

        # Handle lastrowid for INSERT statements by appending RETURNING id if not present
        is_insert = pg_query.strip().upper().startswith("INSERT INTO")
        has_returning = "RETURNING" in pg_query.upper()

        if is_insert and not has_returning:
            # Strip trailing semicolon if present, then add RETURNING id
            clean_query = pg_query.strip().rstrip(";")
            pg_query = f"{clean_query} RETURNING id;"
            if params is not None:
                self.cursor.execute(pg_query, params)
            else:
                self.cursor.execute(pg_query)
            try:
                row = self.cursor.fetchone()
                if row:
                    self.lastrowid = row[0] if isinstance(row, (tuple, list)) else row.get("id")
            except Exception:
                self.lastrowid = None
            return self

        if params is not None:
            self.cursor.execute(pg_query, params)
        else:
            self.cursor.execute(pg_query)
        return self

    def fetchone(self):
        return self.cursor.fetchone()

    def fetchall(self):
        return self.cursor.fetchall()

    def fetchmany(self, size=None):
        return self.cursor.fetchmany(size) if size else self.cursor.fetchmany()

    def close(self):
        self.cursor.close()

    @property
    def rowcount(self):
        return self.cursor.rowcount

    @property
    def description(self):
        return self.cursor.description

    def __iter__(self):
        return iter(self.cursor)


class PostgresConnectionWrapper:
    """Wraps a psycopg2 connection to return PostgresCursorWrapper cursors."""

    def __init__(self, conn: Any):
        self.conn = conn

    def cursor(self):
        return PostgresCursorWrapper(self.conn.cursor())

    def commit(self):
        self.conn.commit()

    def rollback(self):
        self.conn.rollback()

    def close(self):
        self.conn.close()

    def execute(self, query: str, params: Optional[Union[Tuple, list]] = None):
        cur = self.cursor()
        cur.execute(query, params)
        return cur

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        if exc_type is not None:
            self.rollback()
        else:
            self.commit()
        self.close()


def get_db_connection():
    """
    Provides a database connection with dict/row-level attribute access:
    - If DATABASE_URL or POSTGRES_URL is configured (e.g. on Vercel), connects to PostgreSQL.
    - Otherwise, falls back to local SQLite (fest.db).
    """
    db_url = get_database_url()

    if db_url and db_url.strip():
        import psycopg2
        import psycopg2.extras

        # Handle postgres:// to postgresql:// conversion if needed
        clean_url = db_url.strip()
        if clean_url.startswith("postgres://"):
            clean_url = clean_url.replace("postgres://", "postgresql://", 1)

        raw_conn = psycopg2.connect(
            clean_url,
            cursor_factory=psycopg2.extras.DictCursor
        )
        return PostgresConnectionWrapper(raw_conn)

    # SQLite local fallback
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn


def init_db() -> None:
    """Initializes the database schema if tables do not exist."""
    conn = get_db_connection()
    cursor = conn.cursor()

    if is_postgres():
        # PostgreSQL Schema
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS events (
                id SERIAL PRIMARY KEY,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                event_date TEXT NOT NULL,
                event_time TEXT NOT NULL,
                venue TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                fee INTEGER NOT NULL DEFAULT 0,
                icon TEXT
            );
            """
        )

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS registrations (
                id SERIAL PRIMARY KEY,
                event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
                student_name TEXT NOT NULL,
                roll_number TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT,
                registered_at TEXT NOT NULL,
                UNIQUE(event_id, email)
            );
            """
        )
    else:
        # SQLite Schema
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                event_date TEXT NOT NULL,
                event_time TEXT NOT NULL,
                venue TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                fee INTEGER NOT NULL DEFAULT 0,
                icon TEXT
            );
            """
        )

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER NOT NULL REFERENCES events(id),
                student_name TEXT NOT NULL,
                roll_number TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT,
                registered_at TEXT NOT NULL,
                UNIQUE(event_id, email)
            );
            """
        )

    conn.commit()
    conn.close()


if __name__ == "__main__":
    init_db()
    db_target = "PostgreSQL" if is_postgres() else f"SQLite at {DB_PATH}"
    print(f"Database tables initialized successfully for {db_target}")
