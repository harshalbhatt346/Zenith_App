import os
import sys
from pathlib import Path
import openpyxl

# Support UTF-8 output on Windows consoles
if sys.stdout and hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if sys.stderr and hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

# Ensure project root is in sys.path
PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from backend.database import get_db_connection, init_db, is_postgres

DATA_FILE = PROJECT_ROOT / "data" / "events_seed.xlsx"


def seed_events() -> int:
    """
    Seeds the events table from data/events_seed.xlsx if the table is empty.
    Raises FileNotFoundError if data/events_seed.xlsx is missing.
    Returns the number of seeded events.
    """
    init_db()

    conn = get_db_connection()
    cursor = conn.cursor()

    # Skip seeding if events table already has rows
    cursor.execute("SELECT COUNT(*) FROM events;")
    count = cursor.fetchone()[0]
    if count > 0:
        conn.close()
        return count

    # Check if data/events_seed.xlsx exists
    if not DATA_FILE.exists():
        conn.close()
        print(f"[SEED] Seed data file not found at '{DATA_FILE}'. Skipping initial Excel seed.", file=sys.stderr)
        return 0

    wb = openpyxl.load_workbook(DATA_FILE)
    if "Events" not in wb.sheetnames:
        conn.close()
        raise ValueError("Sheet 'Events' not found in data/events_seed.xlsx")

    sheet = wb["Events"]
    rows = list(sheet.iter_rows(values_only=True))

    if not rows:
        conn.close()
        raise ValueError("The 'Events' sheet is empty.")

    # Row 1 is header row
    header = rows[0]
    expected_headers = [
        "id",
        "title",
        "category",
        "description",
        "event_date",
        "event_time",
        "venue",
        "capacity",
        "fee",
        "icon",
    ]

    header_clean = [str(h).strip().lower() if h is not None else "" for h in header]
    if header_clean[:len(expected_headers)] != expected_headers:
        conn.close()
        raise ValueError(
            f"Invalid headers in Excel sheet: {header_clean}. Expected: {expected_headers}"
        )

    inserted_count = 0
    for row in rows[1:]:
        if not row or row[0] is None:
            continue

        (
            event_id,
            title,
            category,
            description,
            event_date,
            event_time,
            venue,
            capacity,
            fee,
            icon,
        ) = row[:10]

        cursor.execute(
            """
            INSERT INTO events (id, title, category, description, event_date, event_time, venue, capacity, fee, icon)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """,
            (
                int(event_id),
                str(title),
                str(category),
                str(description) if description is not None else "",
                str(event_date),
                str(event_time),
                str(venue),
                int(capacity),
                int(fee) if fee is not None else 0,
                str(icon) if icon is not None else "",
            ),
        )
        inserted_count += 1

    if is_postgres():
        try:
            cursor.execute("SELECT setval(pg_get_serial_sequence('events', 'id'), COALESCE(MAX(id), 1)) FROM events;")
        except Exception as seq_err:
            print(f"[SEED] Note: Could not sync postgres sequence: {seq_err}", file=sys.stderr)

    conn.commit()
    conn.close()
    return inserted_count


if __name__ == "__main__":
    try:
        count = seed_events()
        print(f"Seed completed. Current event count in database: {count}")

        # Show contents of events table
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT id, title, category, event_date, event_time, venue, capacity, fee, icon FROM events;")
        rows = cursor.fetchall()
        print(f"\n--- Events in Database ({len(rows)} events) ---")
        for r in rows:
            print(f"[{r['id']}] {r['icon']} {r['title']} | {r['category']} | {r['event_date']} {r['event_time']} | {r['venue']} | Cap: {r['capacity']} | Fee: ₹{r['fee']}")
        conn.close()
    except Exception as e:
        print(f"Error seeding database: {e}", file=sys.stderr)
        sys.exit(1)
