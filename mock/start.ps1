# Start json-server mock backend for VES Monitor Mobile (Windows PowerShell)
# Usage:
#   .\start.ps1          → bind 0.0.0.0:8090 (LAN accessible — recommend khi test trên điện thoại thật)
#   .\start.ps1 local    → bind 127.0.0.1:8090 only

param(
    [string]$Mode = "lan"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$Port = 8090

# Check Node.js
try {
    $nodeVer = node --version
} catch {
    Write-Host "[!] Node.js not found. Install from https://nodejs.org/" -ForegroundColor Red
    exit 1
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "VES Monitor Mock Backend" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Node      : $nodeVer"
Write-Host "Port      : $Port"
Write-Host "Mode      : $Mode"
Write-Host "Data      : $((Get-Location).Path)\db.json"
Write-Host "Routes    : $((Get-Location).Path)\routes.json"
Write-Host ""

if ($Mode -eq "local") {
    Write-Host "URL       : http://localhost:$Port"
    Write-Host "Emulator  : http://10.0.2.2:$Port (loopback to host)"
    Write-Host ""
    Write-Host "Stop with Ctrl+C"
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host ""
    npx json-server --watch db.json --routes routes.json --middlewares middleware.js --port $Port
} else {
    # Lấy LAN IP của máy
    $lanIp = (Get-NetIPAddress -AddressFamily IPv4 -PrefixOrigin Dhcp `
              | Where-Object { $_.IPAddress -notmatch '^169\.254' } `
              | Select-Object -First 1 -ExpandProperty IPAddress) 2>$null
    if (-not $lanIp) { $lanIp = "<your-ip>" }

    Write-Host "URL local : http://localhost:$Port"
    Write-Host "URL LAN   : http://${lanIp}:${Port}  ← real device dùng URL này (cùng WiFi với laptop)"
    Write-Host "Emulator  : http://10.0.2.2:$Port (loopback to host)"
    Write-Host ""
    Write-Host "Endpoints (Phase 7.6 IEA/APERC + legacy aliases):"
    Write-Host "  POST /api/auth/login    GET /api/auth/me"
    Write-Host "  GET  /api/security/score    /api/security/cascade-risks (always [])"
    Write-Host "  GET  /api/pillars/1/{supply-security|outlook}"
    Write-Host "  GET  /api/pillars/2/{market-resilience|volatility}"
    Write-Host "  GET  /api/pillars/3/{grid-reliability|shedding|shedding-plan}"
    Write-Host "  GET  /api/pillars/4/{energy-transition|netzero|net-zero}"
    Write-Host "  GET  /api/alerts/active?limit=N"
    Write-Host "  GET  /api/recommendations?limit=N"
    Write-Host "  POST /api/recommendations/:id/acknowledge   body {status?,note?}"
    Write-Host "  GET  /api/{fuel-prices,grid-load}/latest    (also /api/raw/... for compat)"
    Write-Host "  GET  /api/health"
    Write-Host ""
    Write-Host "💡 Nếu Windows Firewall hỏi → cho phép Node.js access mạng LAN"
    Write-Host "Stop with Ctrl+C"
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host ""
    npx json-server --watch db.json --routes routes.json --middlewares middleware.js --port $Port --host 0.0.0.0
}
