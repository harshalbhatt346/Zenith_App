import sys
from datetime import datetime
from pathlib import Path
from typing import List, Optional

from fastapi import FastAPI, HTTPException, Query, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

# Support UTF-8 output on Windows consoles
if sys.stdout and hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if sys.stderr and hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

# Ensure project root is in sys.path
PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

from backend.database import get_db_connection, init_db
from backend.models import (
    EventDetail,
    EventIn,
    EventOut,
    RegistrationIn,
    RegistrationListItem,
    RegistrationOut,
    StatsOut,
)
from backend.seed_data import seed_events

app = FastAPI(
    title="Zenith '26 Fest Management System API",
    description="API backend for Zenith '26 College Fest Management System",
    version="1.0.0",
)

# Enable CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup_event():
    """Initializes the database and seeds events on startup."""
    init_db()
    try:
        count = seed_events()
        print(f"[STARTUP] Database ready with {count} events.")
    except Exception as e:
        print(f"[STARTUP ERROR] Seeding failed: {e}", file=sys.stderr)


# ==========================================
# API ENDPOINTS
# ==========================================


@app.get("/api/events", response_model=List[EventOut], tags=["Events"])
def get_events(
    category: Optional[str] = Query(None, description="Filter by event category"),
    search: Optional[str] = Query(None, description="Search keyword matching title"),
):
    """
    List events with computed available_seats.
    Optional filters: category, search (case-insensitive substring in title).
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT 
            e.id, e.title, e.category, e.description, e.event_date, e.event_time, 
            e.venue, e.capacity, e.fee, e.icon,
            (e.capacity - (SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.id)) AS available_seats
        FROM events e
        WHERE 1=1
    """
    params = []

    if category and category.strip().lower() != "all":
        query += " AND LOWER(e.category) = LOWER(?)"
        params.append(category.strip())

    if search and search.strip():
        query += " AND LOWER(e.title) LIKE ?"
        params.append(f"%{search.strip().lower()}%")

    query += " ORDER BY e.event_date ASC, e.event_time ASC, e.id ASC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    events = [
        EventOut(
            id=row["id"],
            title=row["title"],
            category=row["category"],
            description=row["description"],
            event_date=row["event_date"],
            event_time=row["event_time"],
            venue=row["venue"],
            capacity=row["capacity"],
            fee=row["fee"],
            icon=row["icon"],
            available_seats=max(0, row["available_seats"]),
        )
        for row in rows
    ]
    return events


@app.get("/api/events/{event_id}", response_model=EventDetail, tags=["Events"])
def get_event_detail(event_id: int):
    """
    Get detailed information for a single event, including computed available_seats.
    Returns 404 if the event does not exist.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        """
        SELECT 
            e.id, e.title, e.category, e.description, e.event_date, e.event_time, 
            e.venue, e.capacity, e.fee, e.icon,
            (e.capacity - (SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.id)) AS available_seats
        FROM events e
        WHERE e.id = ?
        """,
        (event_id,),
    )
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} not found",
        )

    return EventDetail(
        id=row["id"],
        title=row["title"],
        category=row["category"],
        description=row["description"],
        event_date=row["event_date"],
        event_time=row["event_time"],
        venue=row["venue"],
        capacity=row["capacity"],
        fee=row["fee"],
        icon=row["icon"],
        available_seats=max(0, row["available_seats"]),
    )


@app.post(
    "/api/events/{event_id}/register",
    response_model=RegistrationOut,
    status_code=status.HTTP_201_CREATED,
    tags=["Registrations"],
)
def register_for_event(event_id: int, reg_data: RegistrationIn):
    """
    Register a student for an event with strict server-side validation:
    - event_id must exist (404)
    - student_name, roll_number, email must not be empty (400)
    - email must contain '@' (400)
    - event must not be at capacity (409 "Event is full")
    - email must not already be registered for this event (409 "Already registered")
    """
    student_name = reg_data.student_name.strip() if reg_data.student_name else ""
    roll_number = reg_data.roll_number.strip() if reg_data.roll_number else ""
    email = reg_data.email.strip().lower() if reg_data.email else ""
    phone = reg_data.phone.strip() if reg_data.phone else None

    # Server-side validation 1: Required fields non-empty
    if not student_name:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Student name cannot be empty",
        )
    if not roll_number:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Roll number cannot be empty",
        )
    if not email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email cannot be empty",
        )

    # Server-side validation 2: Email format
    if "@" not in email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid email: Email must contain '@'",
        )

    conn = get_db_connection()
    cursor = conn.cursor()

    # Server-side validation 3: event_id exists
    cursor.execute("SELECT id, title, capacity FROM events WHERE id = ?", (event_id,))
    event_row = cursor.fetchone()
    if not event_row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} does not exist",
        )

    capacity = event_row["capacity"]

    # Server-side validation 4: Check if already registered
    cursor.execute(
        "SELECT id FROM registrations WHERE event_id = ? AND LOWER(email) = ?",
        (event_id, email),
    )
    if cursor.fetchone():
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Already registered",
        )

    # Server-side validation 5: Check capacity
    cursor.execute(
        "SELECT COUNT(*) FROM registrations WHERE event_id = ?",
        (event_id,),
    )
    current_reg_count = cursor.fetchone()[0]
    if current_reg_count >= capacity:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Event is full",
        )

    # Insert registration
    registered_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    cursor.execute(
        """
        INSERT INTO registrations (event_id, student_name, roll_number, email, phone, registered_at)
        VALUES (?, ?, ?, ?, ?, ?)
        """,
        (event_id, student_name, roll_number, email, phone, registered_at),
    )
    conn.commit()
    new_reg_id = cursor.lastrowid
    conn.close()

    return RegistrationOut(
        id=new_reg_id,
        event_id=event_id,
        student_name=student_name,
        registered_at=registered_at,
    )


@app.get(
    "/api/registrations",
    response_model=List[RegistrationListItem],
    tags=["Registrations"],
)
def get_registrations(
    event_id: Optional[int] = Query(None, description="Filter registrations by event_id"),
):
    """
    List registrations, each enriched with event_title.
    Optional query param: event_id.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT 
            r.id, r.event_id, r.student_name, r.roll_number, r.email, r.phone, r.registered_at,
            e.title AS event_title
        FROM registrations r
        JOIN events e ON r.event_id = e.id
    """
    params = []
    if event_id is not None:
        query += " WHERE r.event_id = ?"
        params.append(event_id)

    query += " ORDER BY r.id DESC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    return [
        RegistrationListItem(
            id=row["id"],
            event_id=row["event_id"],
            student_name=row["student_name"],
            roll_number=row["roll_number"],
            email=row["email"],
            phone=row["phone"],
            registered_at=row["registered_at"],
            event_title=row["event_title"],
        )
        for row in rows
    ]


@app.get("/api/stats", response_model=StatsOut, tags=["Admin"])
def get_stats():
    """
    Returns live stats computed from the database on every call:
    - total_events
    - total_registrations
    - total_capacity
    - overall_utilization_pct (total_registrations / total_capacity * 100, rounded to 1 decimal)
    - upcoming_events_count (events with event_date >= today)
    - events_by_category (dict mapping category -> count)
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    # Total events and total capacity
    cursor.execute("SELECT COUNT(*), COALESCE(SUM(capacity), 0) FROM events;")
    total_events, total_capacity = cursor.fetchone()

    # Total registrations
    cursor.execute("SELECT COUNT(*) FROM registrations;")
    total_registrations = cursor.fetchone()[0]

    # Overall utilization percentage
    if total_capacity > 0:
        overall_utilization_pct = round((total_registrations / total_capacity) * 100, 1)
    else:
        overall_utilization_pct = 0.0

    # Upcoming events count (events today or in future)
    today_iso = datetime.now().strftime("%Y-%m-%d")
    cursor.execute("SELECT COUNT(*) FROM events WHERE event_date >= ?;", (today_iso,))
    upcoming_events_count = cursor.fetchone()[0]

    # Events by category
    cursor.execute(
        """
        SELECT category, COUNT(*) as count 
        FROM events 
        GROUP BY category 
        ORDER BY count DESC;
        """
    )
    category_rows = cursor.fetchall()
    events_by_category = {row["category"]: row["count"] for row in category_rows}

    conn.close()

    return StatsOut(
        total_events=total_events,
        total_registrations=total_registrations,
        total_capacity=total_capacity,
        overall_utilization_pct=overall_utilization_pct,
        upcoming_events_count=upcoming_events_count,
        events_by_category=events_by_category,
    )


# ==========================================
# STRETCH GOAL ENDPOINTS
# ==========================================


@app.post(
    "/api/events",
    response_model=EventOut,
    status_code=status.HTTP_201_CREATED,
    tags=["Stretch Goals"],
)
def create_event(event_in: EventIn):
    """
    Stretch goal 1: Lets the admin add a new event with server-side validation.
    """
    title = event_in.title.strip()
    category = event_in.category.strip()
    venue = event_in.venue.strip()
    event_date = event_in.event_date.strip()
    event_time = event_in.event_time.strip()

    if not title or not category or not venue or not event_date or not event_time:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Title, category, venue, date, and time are required fields.",
        )

    if event_in.capacity <= 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Capacity must be greater than zero.",
        )

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        """
        INSERT INTO events (title, category, description, event_date, event_time, venue, capacity, fee, icon)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            title,
            category,
            event_in.description.strip() if event_in.description else "",
            event_date,
            event_time,
            venue,
            event_in.capacity,
            max(0, event_in.fee),
            event_in.icon.strip() if event_in.icon else "🎉",
        ),
    )
    conn.commit()
    new_event_id = cursor.lastrowid
    conn.close()

    return EventOut(
        id=new_event_id,
        title=title,
        category=category,
        description=event_in.description.strip() if event_in.description else "",
        event_date=event_date,
        event_time=event_time,
        venue=venue,
        capacity=event_in.capacity,
        fee=max(0, event_in.fee),
        icon=event_in.icon.strip() if event_in.icon else "🎉",
        available_seats=event_in.capacity,
    )


@app.delete("/api/registrations/{registration_id}", tags=["Stretch Goals"])
def cancel_registration(registration_id: int):
    """
    Stretch goal 2: Lets the admin cancel a registration.
    Returns 200 on success, 404 if not found.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id, event_id, student_name FROM registrations WHERE id = ?", (registration_id,))
    reg = cursor.fetchone()
    if not reg:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Registration with id {registration_id} not found",
        )

    cursor.execute("DELETE FROM registrations WHERE id = ?", (registration_id,))
    conn.commit()
    conn.close()

    return {
        "message": "Registration cancelled successfully",
        "id": registration_id,
        "event_id": reg["event_id"],
    }


# ==========================================
# STATIC FILES & FRONTEND ROUTES (Step 4)
# ==========================================

FRONTEND_DIR = PROJECT_ROOT / "frontend"

# Mount /js directory for script files
app.mount("/js", StaticFiles(directory=FRONTEND_DIR / "js"), name="js")


@app.get("/", include_in_schema=False)
def serve_index():
    """Serves the student portal."""
    return FileResponse(FRONTEND_DIR / "index.html")


@app.get("/index.html", include_in_schema=False)
def serve_index_html():
    return FileResponse(FRONTEND_DIR / "index.html")


@app.get("/admin.html", include_in_schema=False)
def serve_admin():
    """Serves the admin dashboard."""
    return FileResponse(FRONTEND_DIR / "admin.html")
