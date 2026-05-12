#!/usr/bin/env bash
# Start json-server mock backend for VES Monitor Mobile
# Usage:
#   bash start.sh          → bind 0.0.0.0:8090 (LAN accessible)
#   bash start.sh local    → bind 127.0.0.1:8090 only

set -e
cd "$(dirname "$0")"

PORT=8090
MODE="${1:-lan}"

if ! command -v npx >/dev/null 2>&1; then
    echo "[!] Node.js / npx not found. Install: https://nodejs.org/"
    exit 1
fi

echo "================================================"
echo "VES Monitor Mock Backend"
echo "================================================"
echo "Port      : $PORT"
echo "Mode      : $MODE"
echo "Data      : $(pwd)/db.json"
echo "Routes    : $(pwd)/routes.json"
echo ""
if [ "$MODE" = "local" ]; then
    echo "URL       : http://localhost:$PORT"
    echo "Emulator  : http://10.0.2.2:$PORT (loopback to host)"
    npx json-server --watch db.json --routes routes.json --middlewares middleware.js --port $PORT
else
    HOST_IP=$(hostname -I 2>/dev/null | awk '{print $1}' || ipconfig getifaddr en0 2>/dev/null || echo "<your-ip>")
    echo "URL local : http://localhost:$PORT"
    echo "URL LAN   : http://$HOST_IP:$PORT  ← real device dùng URL này"
    echo "Emulator  : http://10.0.2.2:$PORT (loopback to host)"
    echo ""
    echo "Endpoints:"
    echo "  POST /api/auth/login"
    echo "  GET  /api/security/score"
    echo "  GET  /api/security/cascade-risks"
    echo "  GET  /api/pillars/{1-4}/..."
    echo "  GET  /api/alerts/active"
    echo "  GET  /api/recommendations"
    echo "  POST /api/recommendations/:id/acknowledge"
    echo "  GET  /api/raw/{fuel-prices,grid-load}/latest"
    echo "  GET  /api/health"
    echo ""
    echo "Stop with Ctrl+C"
    echo "================================================"
    echo ""
    npx json-server --watch db.json --routes routes.json --middlewares middleware.js --port $PORT --host 0.0.0.0
fi
