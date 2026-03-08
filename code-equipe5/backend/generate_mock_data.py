import json, random
from datetime import date, datetime, timedelta, timezone

# ------------------------------------------------------------
# Mock-data generator for the Équipe 6 Dashboard
# Generates 1 year of runs + cases with:
#   - Selenium (UI)
#   - JMeter (API / perf-ish)
#   - RestAssured (API)  <-- new
# Also adds "requirements" so we can build Feature Health Cards.
# ------------------------------------------------------------

random.seed(42)

# 1 full year (inclusive): 2025-03-08 -> 2026-03-08 (366 days)
START = date(2025, 3, 8)
END   = date(2026, 3, 8)

PROJECT = "TAF"
TESTS_PER_DAY = 5

# Each template = one test case per day (we keep 5/day)
# name, type, tool, typical duration(ms), suite, requirements[]
test_templates = [
    ("Login UI",          "UI",  "Selenium",    5200, "Authentication", ["REQ-AUTH-LOGIN"]),
    ("Checkout UI",       "UI",  "Selenium",    6800, "Checkout",       ["REQ-ORDER-CHECKOUT"]),
    ("Login API",         "API", "RestAssured",  800, "Authentication", ["REQ-AUTH-LOGIN"]),
    ("Create Project API","API", "RestAssured",  950, "Projects",       ["REQ-PROJECT-CREATE"]),
    ("Search API",        "API", "JMeter",      1050, "Search",         ["REQ-SEARCH-BASIC"]),
    ("Export API",         "API", "RestAssured", 900, "Export",         ["REQ-EXPORT-PDF"]),
]

runs, cases = [], []

def make_error(tool: str, name: str) -> dict:
    """Very simple fake error messages that feel realistic."""
    if tool == "Selenium":
        return {"type": "TimeoutException", "message": f"Element not found in time: {name}"}
    if tool == "RestAssured":
        return {"type": "AssertionError", "message": f"Expected HTTP 200 but got 500: {name}"}
    # JMeter
    return {"type": "HttpError", "message": f"Non-2xx responseCode detected: {name}"}

def maybe_defects(requirements: list, failed: bool) -> list:
    """Attach a defect sometimes (only for failed tests)."""
    if not failed:
        return []
    # ~50% of failed tests have a defect "created"
    if random.random() < 0.5:
        # simple stable-ish bug id based on requirement
        tag = requirements[0].replace("REQ-", "")
        num = random.randint(100, 999)
        return [f"BUG-{tag}-{num}"]
    return []

d = START
while d <= END:
    run_id = f"RUN-{d.strftime('%Y%m%d')}"
    created_at = f"{d.isoformat()}T12:00:00Z"

    # Failures pattern:
    # ~1 day out of 5 has failures (same style as your current script)
    fail_count = 0 if (d.toordinal() % 5) else random.choice([1, 2])
    passed_count = TESTS_PER_DAY - fail_count
    status = "passed" if fail_count == 0 else "failed"

    # test_runs document (summary)
    runs.append({
        "project": {"key": PROJECT},
        "pipeline": {"runId": run_id},
        "run": {
            "status": status,
            "stats": {
                "total": TESTS_PER_DAY,
                "passed": passed_count,
                "failed": fail_count,
                # arbitrary run duration so it changes over time
                "durationMs": 120000 + (d - START).days * 1000
            }
        },
        "createdAt": created_at
    })

    # Choose which tests fail for that day
    idxs = list(range(TESTS_PER_DAY))
    failing = set(random.sample(idxs, fail_count)) if fail_count else set()
    
    daily_tests = random.sample(test_templates, TESTS_PER_DAY)
    for i, (name, typ, tool, dur, suite, reqs) in enumerate(daily_tests):
        case_failed = (i in failing)
        case_status = "failed" if case_failed else "passed"
        executed_at = f"{d.isoformat()}T12:00:{10*(i+1):02d}Z"

        # Build the test_cases document
        doc = {
            "project": PROJECT,
            "runId": run_id,
            "name": name,
            "suite": suite,                 # helps grouping by functional area
            "type": typ,                    # UI / API
            "tool": tool,                   # Selenium / JMeter / RestAssured
            "status": case_status,          # passed / failed
            "durationMs": dur + random.randint(-120, 220),
            "executedAt": executed_at,      # keep as ISO string (works with your backend conversions)
            "executedBy": "seed-generator", # simple traceability

            # --- Key new field for Feature Health Cards ---
            "requirements": reqs,
        }

        # Optional additions for "so what?" dashboards
        # Add error + defects only if failed
        if case_failed:
            doc["error"] = make_error(tool, name)
            defects = maybe_defects(reqs, True)
            if defects:
                doc["defects"] = defects

        cases.append(doc)

    d += timedelta(days=1)

out = {
    "_comment": "Dashboard mock data for MongoDB (collections: test_runs and test_cases).",
    "meta": {
        "project": PROJECT,
        "testsPerDay": TESTS_PER_DAY,
        "dateRange": {"start": START.isoformat(), "end": END.isoformat()},
        "generatedAt": datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
        "notes": "Seed data includes Selenium + JMeter + RestAssured and requirements for Feature Health Cards."
    },
    "collections": {
        "test_runs": runs,
        "test_cases": cases
    }
}

with open("mockData.json", "w", encoding="utf-8") as f:
    json.dump(out, f, indent=2)

print(f"Created mockData.json with runs={len(runs)} cases={len(cases)}")
