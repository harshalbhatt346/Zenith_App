import os
import sys
import traceback
from pathlib import Path

# Add project root and backend to sys.path so modules can be imported in serverless
PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

backend_dir = PROJECT_ROOT / "backend"
if str(backend_dir) not in sys.path:
    sys.path.insert(0, str(backend_dir))

cwd = Path(os.getcwd())
if str(cwd) not in sys.path:
    sys.path.insert(0, str(cwd))

try:
    from backend.main import app
except Exception as e:
    from fastapi import FastAPI
    from fastapi.responses import HTMLResponse

    app = FastAPI(title="Zenith '26 Error Diagnostics")
    err_tb = traceback.format_exc()
    print(f"[FATAL VERCEL ERROR] {err_tb}", file=sys.stderr)

    @app.api_route("/{path_name:path}", methods=["GET", "POST", "PUT", "DELETE"])
    def catch_all(path_name: str = ""):
        return HTMLResponse(
            f"<html><head><title>Zenith '26 - Initialization Error</title></head>"
            f"<body style='font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif; padding: 2rem; background: #0f172a; color: #f8fafc;'>"
            f"<div style='max-width: 800px; margin: 0 auto;'>"
            f"<h2 style='color: #ef4444;'>FastAPI Startup Error on Vercel</h2>"
            f"<p style='color: #94a3b8;'>The backend encountered an error while importing application modules:</p>"
            f"<pre style='background: #1e293b; padding: 1.5rem; border-radius: 8px; color: #fca5a5; overflow-x: auto; line-height: 1.5;'>{err_tb}</pre>"
            f"</div>"
            f"</body></html>",
            status_code=500,
        )
