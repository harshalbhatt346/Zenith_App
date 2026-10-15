"""
Migration Script: Migrate Zenith '26 Database from SQLite (fest.db) to PostgreSQL.
Usage:
    python backend/migrate_to_postgres.py [POSTGRES_URL]
Or:
    $env:DATABASE_URL="postgresql://user:pass@host:port/dbname?sslmode=require"
    python backend/migrate_to_postgres.py
"""

import os
import sys
import sqlite3
from pathlib import Path

# Add project root to sys.path
PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from backend.database import get_database_url, init_db

SQLITE_PATH = PROJECT_ROOT / "fest.db"


def migrate():
    # 1. Determine PostgreSQL connection string
    pg_url = None
    if len(sys.argv) > 1 and sys.argv[1].strip():
        pg_url = sys.argv[1].strip()
    else:
        pg_url = get_database_url()

    if not pg_url:
        print("=" * 70)
        print("ERROR: No PostgreSQL connection URL provided!")
        print("=" * 70)
        print("Please provide your PostgreSQL connection string in one of two ways:")
        print("1. As an argument:")
        print("   python backend/migrate_to_postgres.py \"postgresql://user:pass@host:5432/dbname\"")
        print("\n2. As an environment variable:")
        print("   # Windows PowerShell")
        print("   $env:DATABASE_URL=\"postgresql://user:pass@host:5432/dbname\"")
        print("   python backend/migrate_to_postgres.py")
        print("\n   # macOS/Linux")
        print("   export DATABASE_URL=\"postgresql://user:pass@host:5432/dbname\"")
        print("   python backend/migrate_to_postgres.py")
        print("=" * 70)
        sys.exit(1)

    # Clean URL if starts with postgres://
    if pg_url.startswith("postgres://"):
        pg_url = pg_url.replace("postgres://", "postgresql://", 1)

    os.environ["DATABASE_URL"] = pg_url

    # Check if SQLite DB exists
    if not SQLITE_PATH.exists():
        print(f"ERROR: SQLite database file not found at '{SQLITE_PATH}'")
        sys.exit(1)

    print("=" * 70)
    print("ZENITH '26: SQLITE -> POSTGRESQL MIGRATION TOOL")
    print("=" * 70)
    print(f" Source SQLite: {SQLITE_PATH}")
    # Mask password in URL for display
    display_url = pg_url
    if "@" in display_url and "://" in display_url:
        proto, rest = display_url.split("://", 1)
        creds, host_part = rest.split("@", 1)
        user = creds.split(":", 1)[0]
        display_url = f"{proto}://{user}:*****@{host_part}"
    print(f" Target Postgres: {display_url}")
    print("-" * 70)

    import psycopg2
    import psycopg2.extras

    # 2. Connect to SQLite
    sqlite_conn = sqlite3.connect(SQLITE_PATH)
    sqlite_conn.row_factory = sqlite3.Row
    sqlite_cur = sqlite_conn.cursor()

    # 3. Connect to PostgreSQL and initialize schema
    try:
        pg_conn = psycopg2.connect(pg_url)
        pg_cur = pg_conn.cursor()
    except Exception as e:
        print(f"Failed to connect to PostgreSQL: {e}")
        sys.exit(1)

    print("[1/4] Ensuring PostgreSQL schema exists...")
    init_db()

    # 4. Migrate Events
    print("[2/4] Migrating events table...")
    sqlite_cur.execute("SELECT * FROM events ORDER BY id ASC")
    events = sqlite_cur.fetchall()
    migrated_events = 0

    for ev in events:
        pg_cur.execute(
            """
            INSERT INTO events (id, title, category, description, event_date, event_time, venue, capacity, fee, icon)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                title = EXCLUDED.title,
                category = EXCLUDED.category,
                description = EXCLUDED.description,
                event_date = EXCLUDED.event_date,
                event_time = EXCLUDED.event_time,
                venue = EXCLUDED.venue,
                capacity = EXCLUDED.capacity,
                fee = EXCLUDED.fee,
                icon = EXCLUDED.icon;
            """,
            (
                ev["id"],
                ev["title"],
                ev["category"],
                ev["description"],
                ev["event_date"],
                ev["event_time"],
                ev["venue"],
                ev["capacity"],
                ev["fee"],
                ev["icon"],
            ),
        )
        migrated_events += 1

    # Reset events sequence
    pg_cur.execute("SELECT setval(pg_get_serial_sequence('events', 'id'), COALESCE(MAX(id), 1)) FROM events;")
    pg_conn.commit()
    print(f"       Migrated/updated {migrated_events} events.")

    # 5. Migrate Registrations
    print("[3/4] Migrating registrations table...")
    sqlite_cur.execute("SELECT * FROM registrations ORDER BY id ASC")
    registrations = sqlite_cur.fetchall()
    migrated_regs = 0

    for reg in registrations:
        pg_cur.execute(
            """
            INSERT INTO registrations (id, event_id, student_name, roll_number, email, phone, registered_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (event_id, email) DO UPDATE SET
                student_name = EXCLUDED.student_name,
                roll_number = EXCLUDED.roll_number,
                phone = EXCLUDED.phone,
                registered_at = EXCLUDED.registered_at;
            """,
            (
                reg["id"],
                reg["event_id"],
                reg["student_name"],
                reg["roll_number"],
                reg["email"],
                reg["phone"],
                reg["registered_at"],
            ),
        )
        migrated_regs += 1

    # Reset registrations sequence
    pg_cur.execute("SELECT setval(pg_get_serial_sequence('registrations', 'id'), COALESCE(MAX(id), 1)) FROM registrations;")
    pg_conn.commit()
    print(f"       Migrated/updated {migrated_regs} registrations.")

    # 6. Verification
    print("[4/4] Verifying data in PostgreSQL...")
    pg_cur.execute("SELECT COUNT(*) FROM events;")
    pg_event_count = pg_cur.fetchone()[0]
    pg_cur.execute("SELECT COUNT(*) FROM registrations;")
    pg_reg_count = pg_cur.fetchone()[0]

    print("-" * 70)
    print(f"SUCCESS: Migration completed successfully!")
    print(f"  • Total events in PostgreSQL: {pg_event_count}")
    print(f"  • Total registrations in PostgreSQL: {pg_reg_count}")
    print("=" * 70)

    sqlite_conn.close()
    pg_cur.close()
    pg_conn.close()


if __name__ == "__main__":
    migrate()
