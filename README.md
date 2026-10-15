# Zenith '26 College Fest Management System

A full-stack, client-server application built for the **Zenith '26 Annual College Fest**. This project demonstrates the core architectural concepts of modern software engineering: client-server separation, the three-layer architecture (Presentation, Application/Business Logic, Database), robust server-side validation ("never trust the client"), live dynamic capacity calculations, RESTful API contracts, and a **native Android mobile application developed in Kotlin**.

---

## 🚀 Tech Stack

| Layer | Technology | Details |
|---|---|---|
| **Mobile App (Frontend)** | Native Android, Kotlin, Jetpack Compose | Material 3, declarative reactive UI, debounced search, category filter pills, dynamic cards, modals, live capacity alerts, Retrofit 2 + OkHttp |
| **Web App (Frontend)** | HTML5, Tailwind CSS (Play CDN), Vanilla JavaScript | Original web interface preserved alongside Android app |
| **Backend** | Python 3.10+, FastAPI, Pydantic | RESTful API, asynchronous server, strict server-side validation, CORS enabled, Vercel Serverless ready |
| **Database** | SQLite3 (Local) / PostgreSQL (Vercel) | Dual-driver support (`fest.db` locally or Neon / Supabase / Vercel Postgres via `DATABASE_URL` / `POSTGRES_URL`) |
| **Testing** | Postman & Automated Python Suite | Automated collection covering 18 test cases across status codes 200, 201, 400, 404, 409 |

---

## 📁 Project Structure

```text
Zenith26FestSystem/
├── api/
│   └── index.py          # Vercel Serverless ASGI entry point exposing FastAPI
├── backend/
│   ├── database.py       # Dual-driver database layer (SQLite fallback + PostgreSQL psycopg2 wrapper)
│   ├── migrate_to_postgres.py # Standalone data migration tool (SQLite -> PostgreSQL)
│   ├── models.py         # Pydantic models with type hints (EventOut, RegistrationIn, StatsOut, etc.)
│   ├── seed_data.py      # openpyxl Excel parser reading data/events_seed.xlsx into DB + sequence sync
│   └── main.py           # FastAPI app, API route controllers, server validation, static file mounting
├── android/              # Native Android Application (Kotlin)
│   ├── app/
│   │   ├── build.gradle.kts  # App dependencies (Compose, Retrofit, Gson, Coroutines)
│   │   ├── proguard-rules.pro
│   │   └── src/main/
│   │       ├── AndroidManifest.xml # Internet permissions & network security config
│   │       ├── res/
│   │       │   ├── xml/network_security_config.xml # Cleartext HTTP traffic support for local dev
│   │       │   └── values/ (colors, strings, themes)
│   │       └── java/com/zenith26/fest/
│   │           ├── MainActivity.kt        # Entry point with Compose Navigation & URL config
│   │           ├── ZenithApp.kt           # Application class
│   │           ├── data/
│   │           │   ├── api/
│   │           │   │   ├── ZenithApiService.kt  # Retrofit API interface (all 5 endpoints + stretch)
│   │           │   │   └── RetrofitClient.kt    # Base URL configuration & OkHttp client
│   │           │   └── model/
│   │           │       ├── Event.kt             # Event domain models
│   │           │       ├── Registration.kt      # Registration models
│   │           │       ├── Stats.kt             # Dashboard stats models
│   │           │       └── CreateEventRequest.kt
│   │           ├── repository/
│   │           │   └── ZenithRepository.kt      # Repository for IO operations
│   │           ├── viewmodel/
│   │           │   ├── StudentViewModel.kt      # Student portal state & debounced search
│   │           │   └── AdminViewModel.kt        # Admin dashboard state & metrics
│   │           └── ui/
│   │               ├── theme/ (Color, Type, Theme) # Exact Tailwind color palette
│   │               ├── navigation/ZenithNavigation.kt
│   │               ├── components/              # Cards, Modals, Tables, State Views
│   │               ├── student/StudentPortalScreen.kt
│   │               └── admin/AdminDashboardScreen.kt
│   ├── build.gradle.kts  # Project-level Gradle build configuration
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   └── gradlew.bat
├── frontend/             # Original Web Frontend (HTML / Tailwind CSS / JavaScript)
│   ├── index.html        # Student event discovery portal & registration experience
│   ├── admin.html        # Fest coordinator operational analytics & registrations manager
│   └── js/
│       ├── app.js        # Student client logic
│       └── admin.js      # Admin client logic
├── data/
│   └── events_seed.xlsx  # 14 initial fest events spreadsheet (Technical, Cultural, Sports, Gaming, Workshops)
├── fest.db               # SQLite database file (created automatically on startup for local dev)
├── vercel.json           # Vercel deployment & rewrite configuration
├── .vercelignore         # Deployment exclusions (omits Android code, APK, venv from Vercel)
├── requirements.txt      # Python dependencies: fastapi, uvicorn, pydantic, openpyxl, psycopg2-binary
├── zenith26-api.postman_collection.json # Ready-to-import Postman test collection (v2.1.0)
├── Postman-Testing-Commands-Zenith26.md # 18-step testing guide with request/response payloads
├── test_suite_api.py     # Automated Python test runner for all 18 API contracts
└── README.md             # Project documentation and quickstart instructions
```

---

## ⚡ How to Run the Application

### 1. Prerequisites
- **Python 3.10+** installed.
- **Android Studio** (Hedgehog or newer recommended) or Android SDK with JDK 17+.

---

### 2. Start the FastAPI Backend

Open a terminal in the project directory:

```bash
# Windows PowerShell
.\venv\Scripts\Activate.ps1
# Or create fresh venv if needed: python -m venv venv && .\venv\Scripts\Activate.ps1 && pip install -r requirements.txt

# Start FastAPI server accessible to both PC and mobile devices on Wi-Fi:
uvicorn backend.main:app --host 0.0.0.0 --port 8001 --reload
```

*(On macOS/Linux: `source venv/bin/activate` then `uvicorn backend.main:app --host 0.0.0.0 --port 8001 --reload`)*

The server will automatically initialize SQLite `fest.db` and seed the 14 events from `data/events_seed.xlsx`.

---

### 3. Run the Native Android App (or Install APK Directly)

#### 📦 Direct APK Installation (No Android Studio required!):
The debug APK has already been compiled and generated for you:
- **Root APK file**: [`zenith26-fest.apk`](zenith26-fest.apk) (16.6 MB)
- **Gradle output location**: `android/app/build/outputs/apk/debug/app-debug.apk`

To install directly onto an emulator or connected phone:
```bash
adb install -r zenith26-fest.apk
```
Or simply transfer `zenith26-fest.apk` to your Android phone via USB/WhatsApp/Drive and tap to install!

#### 🛠️ Or Open & Run in Android Studio:
1. Open **Android Studio**.
2. Select **Open** and choose the `android` folder (`c:\Users\harsh\OneDrive\Desktop\Mobile App\android`).
3. Allow Gradle to sync dependencies.
4. Select an Android Emulator or connected physical device running Android 7.0 (API 24) or higher.
5. Click **Run (Shift + F10)**.

#### 🌐 Backend URL Configuration on Android:
- **Android Emulator**: The app defaults to `http://10.0.2.2:8001/`, which is Android's special loopback alias pointing to the development computer's `localhost:8001`.
- **Physical Device**: Connect the phone to the same Wi-Fi network as your computer. Tap the **⚙️ Settings icon** in the top navigation bar of the app and set the URL to `http://<YOUR_COMPUTER_LAN_IP>:8001/` (e.g., `http://192.168.1.100:8001/`).
- **ADB Port Forwarding (USB Cable)**: Run `adb reverse tcp:8001 tcp:8001` in your terminal, then tap ⚙️ in the app and select `http://127.0.0.1:8001/`.
- **Live Vercel Production**: Tap ⚙️ and enter your production Vercel URL: `https://your-project.vercel.app/`.

---

## ☁️ Deploying to Vercel with PostgreSQL

Because Vercel serverless functions have ephemeral, read-only file systems, the system seamlessly supports **PostgreSQL** (Neon, Supabase, Vercel Postgres, or RDS) when deployed to the cloud, while retaining local **SQLite** fallback when running offline.

### Step 1: Create a PostgreSQL Database
Create a free database using any managed PostgreSQL provider:
- **[Neon Serverless Postgres](https://neon.tech)** (Recommended — instant setup, free tier)
- **[Supabase](https://supabase.com)**
- **[Vercel Postgres](https://vercel.com/docs/storage/vercel-postgres)**

Copy your database connection string, which looks like:
```text
postgresql://neondb_owner:YOUR_PASSWORD@ep-xyz-pooler.us-east-2.aws.neon.tech/neondb?sslmode=require
```

### Step 2: Migrate Existing Data from SQLite to PostgreSQL
Run the built-in migration script to replicate all 14 fest events, registrations, schemas, and primary key sequences into your PostgreSQL database:

```bash
# Windows PowerShell
python backend/migrate_to_postgres.py "postgresql://user:pass@host/dbname?sslmode=require"

# Or set as environment variable:
$env:DATABASE_URL="postgresql://user:pass@host/dbname?sslmode=require"
python backend/migrate_to_postgres.py
```

### Step 3: Deploy to Vercel
You can deploy using GitHub or the Vercel CLI:

#### Option A: Deploy with Vercel CLI
```bash
npm install -g vercel
vercel
```

#### Option B: Deploy with GitHub
1. Push this repository to GitHub.
2. In the [Vercel Dashboard](https://vercel.com/dashboard), click **Add New > Project** and import the repository.
3. In **Environment Variables**, add:
   - **Key**: `DATABASE_URL` (or `POSTGRES_URL`)
   - **Value**: `postgresql://user:pass@host/dbname?sslmode=require`
4. Click **Deploy**.

Once deployed:
- Web Student Portal: `https://your-project.vercel.app/`
- Web Admin Portal: `https://your-project.vercel.app/admin.html`
- Interactive API Docs: `https://your-project.vercel.app/docs`
- Android App: Tap ⚙️ in the app top bar and set Base URL to `https://your-project.vercel.app/`

---

## 📡 REST API Contract

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/api/events` | List all events. Supports optional `?category=` and `?search=` query parameters. Available seats computed live. | `200 OK` |
| `GET` | `/api/events/{event_id}` | Retrieve details for a single event by ID. | `200 OK`, `404 Not Found` |
| `POST` | `/api/events/{event_id}/register` | Register a student for an event. Performs independent server-side validation. | `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `GET` | `/api/registrations` | List student registrations with event titles. Supports optional `?event_id=` filter. | `200 OK` |
| `GET` | `/api/stats` | Return live computed dashboard statistics (registrations, capacity, utilization %, upcoming count). | `200 OK` |
| `POST` | `/api/events` | *(Stretch Goal)* Admin adds a new fest event with server validation. | `201 Created`, `400 Bad Request` |
| `DELETE` | `/api/registrations/{id}` | *(Stretch Goal)* Admin cancels a registration and releases the seat. | `200 OK`, `404 Not Found` |

---

## 📱 Native Android Features & UI Fidelity

The native Kotlin Android application preserves the exact design system, typography, color palette, and interaction models of the original interface:

1. **Fest Branding & Top Bar**:
   - ⚡ Emoji logo and gradient **ZENITH '26** title (`#7C3AED` to `#EC4899`).
   - "Annual College Fest" pill badge.
   - Quick navigation pill to the Admin Dashboard.
   - ⚙️ Settings dialog for one-tap backend URL configuration.

2. **Hero Banner**:
   - Dark festive gradient (`#312E81` via `#4C1D95` to `#0F172A`).
   - Event dates badge (`📅 November 14 – 15, 2026 • Main Campus`).
   - Tagline: *"Ignite Your Passion at Zenith '26"*.

3. **Live Search & Category Filtering**:
   - Debounced search bar with instant clear button (`✕`).
   - Category filter pills: `All Events`, `💻 Technical`, `🎭 Cultural`, `⚽ Sports`, `🎮 Gaming`, `🛠️ Workshop`.
   - Results counter (`Showing 14 events`).

4. **Event Cards & Capacity Visuals**:
   - Card top accent bar: Purple-indigo gradient when seats remain; solid rose-500 when full.
   - Category badges matching exact color schemes (Technical: Blue, Cultural: Purple, Sports: Emerald, Gaming: Rose, Workshop: Amber).
   - Prominent **FULL** badge when `available_seats == 0`.
   - Seats count formatted dynamically (`X / Total left`).
   - "Free" label in emerald-600 or "₹Fee".
   - "View Details" action button.

5. **Event Details & Student Registration Modal**:
   - Full event metadata with date, time, venue, and description.
   - Dynamic seats availability banner (purple-50 when available, rose-50 when full).
   - Form inputs with real-time client validation: Student Full Name, College Roll / ID, Phone (Optional), College Email.
   - Server-side error display showing the exact backend error message (e.g. `Event is full`, `Already registered`, `Student name cannot be empty`).
   - Automatic seat count refresh and auto-dismiss on successful HTTP 201 registration.

6. **Admin Coordinator Dashboard**:
   - Four Operations Metric cards: Total Events (🎪), Total Registrations (👥), Capacity Utilization % with visual fill bar (📈), Upcoming Events (⏳).
   - Category Breakdown grid displaying counts per category.
   - Events Table with utilization percentages sorted descending; high demand events (>90% capacity) highlighted in warning rose styling with ⚠️ badges.
   - Raw Registrations table with event filter dropdown and registration cancellation actions.
   - Add New Event modal dialog with server validation.

7. **All Application States Supported**:
   - **Loading State**: Circular progress indicator with descriptive text.
   - **Error State**: Backend connection error panel with server instructions and "Retry Fetching Events" button.
   - **Empty State**: Search/filter empty state with 🔍 icon and "Show All Events" reset button.

---

## 🛡️ Key Architectural & Product Concepts

1. **The Three-Layer Architecture**:
   - **Presentation Layer**: Native Kotlin Android App (Jetpack Compose) consuming REST APIs.
   - **Application / Server Layer**: FastAPI in `backend/main.py`. Re-validates every input, enforces business constraints (capacities, duplicates), and coordinates responses.
   - **Data Layer**: SQLite database `fest.db` with relational constraints and transactional integrity.

2. **"Never Trust the Client"**:
   - The frontend validates required inputs and email patterns for an immediate user feedback experience.
   - The backend independently re-runs all checks. Even if an attacker bypasses the frontend using cURL or Postman, empty fields, malformed emails, over-capacity bookings, and duplicate signups are rejected with HTTP 400 or 409.

3. **Single Source of Truth**:
   - `available_seats` is **never** stored as a separate column in the database table. It is dynamically computed as `capacity - COUNT(registrations)` on every query. This eliminates data drift and race conditions.

---

## 🧪 Testing with Postman & Automated Test Suite

### Running Automated Test Suite
With FastAPI running on `http://127.0.0.1:8001`:
```bash
python test_suite_api.py
```
Outputs validation results across all 18 test cases.

### Postman Manual & Automated Testing
1. Open Postman desktop app.
2. Click **Import** and select `zenith26-api.postman_collection.json`.
3. Ensure the server is running on `http://127.0.0.1:8001`.
4. Run the requests in order to test happy paths (`200`, `201`) and error paths (`400`, `404`, `409`). Refer to `Postman-Testing-Commands-Zenith26.md` for the complete 18-test reference guide.
