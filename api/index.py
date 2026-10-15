import os
import sys
from pathlib import Path

# Add project root and backend to sys.path so modules can be imported in serverless
PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))

backend_dir = PROJECT_ROOT / "backend"
if str(backend_dir) not in sys.path:
    sys.path.insert(0, str(backend_dir))

from backend.main import app
