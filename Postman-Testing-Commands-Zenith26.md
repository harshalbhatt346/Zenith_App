# Postman Testing Guide: Zenith '26 College Fest Management System

This testing guide provides **18 concrete API requests** to run in Postman desktop app against your local FastAPI backend (`http://127.0.0.1:8001`), testing every endpoint, status code (`200`, `201`, `400`, `404`, `409`), and server-side validation rule.

---

## 🛠️ Setup Instructions for Postman

1. **Start the FastAPI Backend**:
   Inside project folder:
   ```bash
   .\venv\Scripts\uvicorn backend.main:app --reload --port 8001
   ```
2. **Open Postman Desktop App**:
   - You do NOT need to create an account — use Postman's lightweight Scratch Pad / Offline API client.
3. **Import Collection**:
   - Click **Import** (top left).
   - Drag and drop or browse to `zenith26-api.postman_collection.json`.
4. **Environment / Variable**:
   - Collection variable `baseUrl` is pre-set to `http://127.0.0.1:8001`.

---

## 📋 Comprehensive 18-Test Run Matrix

### Folder 1: Events Endpoints (`/api/events`)

#### Test 1: GET All Events
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events`
* **Expected Status**: `200 OK`
* **Expected Response**: Array of 14 events. Each object has `id`, `title`, `category`, `capacity`, and `available_seats`.

#### Test 2: GET Events Filtered by Category (Technical)
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events?category=Technical`
* **Expected Status**: `200 OK`
* **Expected Response**: Array containing only events where `category == "Technical"` (Code Sprint, Robo Race, AI Pitch Fest, Web Design Rumble).

#### Test 3: GET Events Filtered by Category (Cultural)
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events?category=Cultural`
* **Expected Status**: `200 OK`
* **Expected Response**: Array of 5 Cultural events (Battle of Bands, Dance Fiesta, Nukkad Natak, Fashion Fusion, Open Mic Night).

#### Test 4: GET Events Search Keyword (Title Match)
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events?search=Design`
* **Expected Status**: `200 OK`
* **Expected Response**: Events matching "Design" (Web Design Rumble, UI/UX Design Workshop).

#### Test 5: GET Events Search Case-Insensitive
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events?search=valorant`
* **Expected Status**: `200 OK`
* **Expected Response**: Event "Valorant Showdown".

#### Test 6: GET Events Search No Matches (Empty Array)
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events?search=NonExistentKeywordXYZ`
* **Expected Status**: `200 OK`
* **Expected Response**: `[]` (empty JSON array, HTTP 200).

#### Test 7: GET Single Event Detail (ID 1)
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events/1`
* **Expected Status**: `200 OK`
* **Expected Response**:
  ```json
  {
    "id": 1,
    "title": "Code Sprint 2.0",
    "category": "Technical",
    "description": "A high-intensity 3-hour competitive coding showdown. Tackle algorithmic problems and win exciting cash prizes.",
    "event_date": "2026-11-14",
    "event_time": "10:00 AM",
    "venue": "Main Auditorium",
    "capacity": 100,
    "fee": 0,
    "icon": "💻",
    "available_seats": 100
  }
  ```

#### Test 8: GET Single Event Detail - Non-Existent ID (404 Not Found)
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events/999`
* **Expected Status**: `404 Not Found`
* **Expected Response**:
  ```json
  {
    "detail": "Event with id 999 not found"
  }
  ```

---

### Folder 2: Registration Endpoints & Server-Side Validation

#### Test 9: POST Register Student - Successful (201 Created)
* **Method**: `POST`
* **URL**: `{{baseUrl}}/api/events/1/register`
* **Headers**: `Content-Type: application/json`
* **Body (Raw JSON)**:
  ```json
  {
    "student_name": "Aarav Mehta",
    "roll_number": "2026CS042",
    "email": "aarav.mehta@college.edu",
    "phone": "+91 9876543210"
  }
  ```
* **Expected Status**: `201 Created`
* **Expected Response**:
  ```json
  {
    "id": 1,
    "event_id": 1,
    "student_name": "Aarav Mehta",
    "registered_at": "2026-10-12 12:00:00"
  }
  ```

#### Test 10: POST Register Student - Re-verify Available Seats Dropped
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/events/1`
* **Expected Status**: `200 OK`
* **Verification**: `available_seats` should now be `99` (decreased by 1 from 100).

#### Test 11: POST Register Student - Missing Student Name (400 Bad Request)
* **Method**: `POST`
* **URL**: `{{baseUrl}}/api/events/1/register`
* **Body (Raw JSON)**:
  ```json
  {
    "student_name": "",
    "roll_number": "2026CS042",
    "email": "test@college.edu"
  }
  ```
* **Expected Status**: `400 Bad Request`
* **Expected Response**:
  ```json
  {
    "detail": "Student name cannot be empty"
  }
  ```

#### Test 12: POST Register Student - Invalid Email without '@' (400 Bad Request)
* **Method**: `POST`
* **URL**: `{{baseUrl}}/api/events/1/register`
* **Body (Raw JSON)**:
  ```json
  {
    "student_name": "Kavya Patel",
    "roll_number": "2026IT012",
    "email": "kavya_at_college.edu"
  }
  ```
* **Expected Status**: `400 Bad Request`
* **Expected Response**:
  ```json
  {
    "detail": "Invalid email: Email must contain '@'"
  }
  ```

#### Test 13: POST Register Student - Duplicate Registration (409 Conflict)
* **Method**: `POST`
* **URL**: `{{baseUrl}}/api/events/1/register`
* **Body (Raw JSON)**:
  ```json
  {
    "student_name": "Aarav Mehta",
    "roll_number": "2026CS042",
    "email": "aarav.mehta@college.edu"
  }
  ```
* **Expected Status**: `409 Conflict`
* **Expected Response**:
  ```json
  {
    "detail": "Already registered"
  }
  ```

#### Test 14: POST Register Student - Non-Existent Event ID (404 Not Found)
* **Method**: `POST`
* **URL**: `{{baseUrl}}/api/events/9999/register`
* **Body (Raw JSON)**:
  ```json
  {
    "student_name": "Test User",
    "roll_number": "2026ME001",
    "email": "test@college.edu"
  }
  ```
* **Expected Status**: `404 Not Found`
* **Expected Response**:
  ```json
  {
    "detail": "Event with id 9999 does not exist"
  }
  ```

---

### Folder 3: Admin & Reporting Endpoints

#### Test 15: GET All Registrations (Enriched with Event Title)
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/registrations`
* **Expected Status**: `200 OK`
* **Expected Response**:
  ```json
  [
    {
      "id": 1,
      "event_id": 1,
      "student_name": "Aarav Mehta",
      "roll_number": "2026CS042",
      "email": "aarav.mehta@college.edu",
      "phone": "+91 9876543210",
      "registered_at": "2026-10-12 12:00:00",
      "event_title": "Code Sprint 2.0"
    }
  ]
  ```

#### Test 16: GET Registrations Filtered by Event ID
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/registrations?event_id=1`
* **Expected Status**: `200 OK`
* **Expected Response**: Registrations specific to Event 1.

#### Test 17: GET Live Dashboard Stats
* **Method**: `GET`
* **URL**: `{{baseUrl}}/api/stats`
* **Expected Status**: `200 OK`
* **Expected Response**:
  ```json
  {
    "total_events": 14,
    "total_registrations": 1,
    "total_capacity": 1457,
    "overall_utilization_pct": 0.1,
    "upcoming_events_count": 14,
    "events_by_category": {
      "Technical": 4,
      "Cultural": 5,
      "Sports": 2,
      "Gaming": 1,
      "Workshop": 2
    }
  }
  ```

---

### Folder 4: Stretch Goal Endpoints

#### Test 18: DELETE Cancel Registration (Admin)
* **Method**: `DELETE`
* **URL**: `{{baseUrl}}/api/registrations/1`
* **Expected Status**: `200 OK`
* **Expected Response**:
  ```json
  {
    "message": "Registration cancelled successfully",
    "id": 1,
    "event_id": 1
  }
  ```
* **Post-Check**: Querying `GET /api/events/1` shows `available_seats` restored back to `100`.
