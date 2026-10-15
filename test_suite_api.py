import json
import urllib.request
import urllib.error

BASE_URL = "http://127.0.0.1:8001"

def request(method, path, data=None):
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"} if data else {}
    body = json.dumps(data).encode("utf-8") if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            status_code = resp.status
            content = resp.read().decode("utf-8")
            return status_code, json.loads(content) if content else None
    except urllib.error.HTTPError as e:
        status_code = e.code
        content = e.read().decode("utf-8")
        try:
            return status_code, json.loads(content)
        except Exception:
            return status_code, content

def run_tests():
    print("=" * 60)
    print("RUNNING ZENITH '26 18-STEP POSTMAN / API VERIFICATION SUITE")
    print("=" * 60)
    passed = 0
    total = 0

    def assert_test(name, condition, details=""):
        nonlocal passed, total
        total += 1
        if condition:
            passed += 1
            print(f" [PASS] Test {total}: {name}")
        else:
            print(f" [FAIL] Test {total}: {name} -> {details}")

    # Test 1: GET All Events
    s, d = request("GET", "/api/events")
    assert_test("GET All Events (14 events, status 200)", s == 200 and len(d) == 14, f"Status: {s}, count: {len(d) if isinstance(d, list) else d}")

    # Test 2: Category Filter Technical
    s, d = request("GET", "/api/events?category=Technical")
    assert_test("GET Events Category=Technical (4 events, all Technical)", s == 200 and len(d) == 4 and all(x['category'] == 'Technical' for x in d))

    # Test 3: Category Filter Cultural
    s, d = request("GET", "/api/events?category=Cultural")
    assert_test("GET Events Category=Cultural (5 events, all Cultural)", s == 200 and len(d) == 5 and all(x['category'] == 'Cultural' for x in d))

    # Test 4: Search Keyword Design
    s, d = request("GET", "/api/events?search=Design")
    assert_test("GET Events search=Design (matches 'Web Design Rumble' & 'UI/UX Design Workshop')", s == 200 and len(d) >= 1 and all('design' in x['title'].lower() for x in d))

    # Test 5: Search Case-Insensitive valorant
    s, d = request("GET", "/api/events?search=valorant")
    assert_test("GET Events search=valorant case-insensitive", s == 200 and len(d) == 1 and "Valorant" in d[0]['title'])

    # Test 6: Search No Matches
    s, d = request("GET", "/api/events?search=NonExistentKeywordXYZ")
    assert_test("GET Events search no matches returns empty array []", s == 200 and len(d) == 0)

    # Test 7: GET Single Event Detail 1
    s, d = request("GET", "/api/events/1")
    assert_test("GET Single Event Detail ID 1", s == 200 and d['id'] == 1 and d['title'] == 'Code Sprint 2.0')

    # Test 8: GET Non-Existent ID 999 (404)
    s, d = request("GET", "/api/events/999")
    assert_test("GET Non-Existent ID 999 returns 404", s == 404 and "not found" in str(d).lower())

    # Test 9: POST Register Student - Successful (201)
    test_email = "postman.tester@fest.zenith.edu"
    reg_payload = {
        "student_name": "Aarav Mehta",
        "roll_number": "2026CS042",
        "email": test_email,
        "phone": "+91 9876543210"
    }
    s, d = request("POST", "/api/events/1/register", reg_payload)
    if s == 409 and "already registered" in str(d).lower():
        # clean up or use alternate email
        test_email = "aarav.unique@college.edu"
        reg_payload["email"] = test_email
        s, d = request("POST", "/api/events/1/register", reg_payload)
    created_reg_id = d.get('id') if isinstance(d, dict) else None
    assert_test("POST Register Student (201 Created)", s == 201 and d.get('event_id') == 1, f"Status: {s}, body: {d}")

    # Test 10: Re-verify Available Seats Dropped
    s, d = request("GET", "/api/events/1")
    assert_test("Re-verify available seats updated after registration", s == 200 and d['available_seats'] < d['capacity'])

    # Test 11: Missing Student Name (400)
    s, d = request("POST", "/api/events/1/register", {
        "student_name": "",
        "roll_number": "2026CS042",
        "email": "test@college.edu"
    })
    assert_test("Missing Student Name returns 400 Bad Request", s == 400 and "Student name cannot be empty" in str(d))

    # Test 12: Invalid Email without '@' (400)
    s, d = request("POST", "/api/events/1/register", {
        "student_name": "Kavya Patel",
        "roll_number": "2026IT012",
        "email": "kavya_at_college.edu"
    })
    assert_test("Invalid Email without '@' returns 400 Bad Request", s == 400 and "@" in str(d))

    # Test 13: Duplicate Registration (409 Conflict)
    s, d = request("POST", "/api/events/1/register", reg_payload)
    assert_test("Duplicate Registration returns 409 Conflict ('Already registered')", s == 409 and "Already registered" in str(d))

    # Test 14: Non-Existent Event ID (404)
    s, d = request("POST", "/api/events/9999/register", {
        "student_name": "Test User",
        "roll_number": "2026ME001",
        "email": "test9999@college.edu"
    })
    assert_test("Register Non-Existent Event returns 404", s == 404 and "does not exist" in str(d))

    # Test 15: GET All Registrations
    s, d = request("GET", "/api/registrations")
    assert_test("GET Registrations returns list with event_title", s == 200 and len(d) >= 1 and 'event_title' in d[0])

    # Test 16: GET Registrations Filtered by Event ID
    s, d = request("GET", "/api/events")
    s, d = request("GET", "/api/registrations?event_id=1")
    assert_test("GET Registrations filtered by event_id=1", s == 200 and all(x['event_id'] == 1 for x in d))

    # Test 17: GET Live Dashboard Stats
    s, d = request("GET", "/api/stats")
    assert_test("GET Live Dashboard Stats has all fields and calculation", s == 200 and d['total_events'] == 14 and 'overall_utilization_pct' in d and 'events_by_category' in d)

    # Test 18: DELETE Cancel Registration (Stretch Goal)
    if created_reg_id:
        s, d = request("DELETE", f"/api/registrations/{created_reg_id}")
        assert_test("DELETE Cancel Registration returns 200 OK", s == 200 and "cancelled" in str(d).lower())
    else:
        assert_test("DELETE Cancel Registration", False, "No registration ID created to cancel")

    print("=" * 60)
    print(f"RESULTS: {passed} / {total} TESTS PASSED")
    print("=" * 60)

if __name__ == "__main__":
    run_tests()
