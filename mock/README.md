# 🎭 Mock Backend — VES Monitor

Mock backend cho phép Android Dev code **toàn bộ UI + flow** mà không cần đợi backend Java live. Khi backend Phase 4.5 build xong, Android app chỉ cần đổi 1 biến `BASE_URL` → switch sang backend thật, mọi shape DTO/response sẽ tương thích.

> Stack: [json-server](https://github.com/typicode/json-server) `v0.17.4` (Node.js).

---

## ⚡ Quick start

### Linux / macOS / WSL

```bash
cd mock
bash start.sh           # bind 0.0.0.0:8090 (LAN accessible)
# hoặc
bash start.sh local     # bind 127.0.0.1:8090 only
```

### Windows PowerShell

```powershell
cd mock
.\start.ps1             # bind 0.0.0.0:8090 (LAN accessible)
# hoặc
.\start.ps1 local       # localhost only
```

### Yêu cầu

- **Node.js ≥ 16** (16.x / 18.x / 20.x đều OK). Cài tại https://nodejs.org/
- **npm / npx**: đi kèm Node.js
- **Port 8090**: free (nếu bị chiếm, đổi `PORT` trong script)

Lần đầu chạy, `npx` sẽ tự download `json-server` (~20MB, ~30s).

---

## 🧪 Smoke test (sau khi chạy mock)

### Test public endpoint

```bash
curl http://localhost:8090/api/health
# Expected: {"status":"UP","db":"UP","timestamp":"2026-05-12T10:30:00Z"}
```

### Test login → lấy token

```bash
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
# Expected: {"accessToken":"...","tokenType":"Bearer","expiresIn":28800,"user":{...}}
```

### Test endpoint cần auth

> ⚠️ **Lưu ý**: json-server **không kiểm tra JWT** — mọi request đều trả 200 dù có / không header `Authorization`. Đây là MOCK, không phải security gateway. Trong Android app vẫn phải gửi header để khi switch sang backend thật không phải sửa code.

```bash
curl -X GET http://localhost:8090/api/security/score \
  -H "Authorization: Bearer FAKE_TOKEN_OK_FOR_MOCK"
# Expected: full JSON score object
```

---

## 📋 14 Endpoint coverage

| # | Method | Path | Mock JSON key |
|---|--------|------|---------------|
| 1 | POST | `/api/auth/login` | `auth_login` |
| 2 | GET | `/api/security/score` | `security_score` |
| 3 | GET | `/api/security/cascade-risks` | `security_cascade` |
| 4 | GET | `/api/pillars/1/outlook` | `pillar1_outlook` |
| 5 | GET | `/api/pillars/2/volatility` | `pillar2_volatility` |
| 6 | GET | `/api/pillars/3/shedding-plan` | `pillar3_shedding` |
| 7 | GET | `/api/pillars/4/net-zero-progress` | `pillar4_netzero` |
| 8 | GET | `/api/alerts/active` | `alerts_active` |
| 9 | GET | `/api/recommendations` | `recommendations` |
| 10 | POST | `/api/recommendations/:id/acknowledge` | `ack_response` |
| 11 | GET | `/api/raw/fuel-prices/latest` | `fuel_prices_latest` |
| 12 | GET | `/api/raw/grid-load/latest` | `grid_load_latest` |
| 13 | GET | `/api/health` | `health` |
| 14 | GET | `/v3/api-docs` | ❌ Không support (chỉ live backend) |

---

## 🌐 Truy cập từ điện thoại Android thật (LAN)

1. Chạy `start.sh` hoặc `start.ps1` không có argument `local` → mock bind `0.0.0.0:8090`.
2. Lấy IP LAN của máy host:
   - **Linux/WSL**: `hostname -I`
   - **macOS**: `ipconfig getifaddr en0`
   - **Windows PowerShell**: `(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike '169.*' -and $_.IPAddress -ne '127.0.0.1' } | Select-Object -First 1).IPAddress`
   - Ví dụ: `192.168.1.105`
3. **Cùng WiFi** với điện thoại → đổi `BASE_URL` trong Android app → `http://192.168.1.105:8090`
4. **Mở Windows Firewall** cho Node.js (Windows hỏi lần đầu, chọn Allow).
5. Test từ điện thoại: mở Chrome → `http://192.168.1.105:8090/api/health` → trả JSON là OK.

### Trouble: kết nối refuse / timeout

- ✔ Cùng WiFi? (kiểm tra cả laptop + phone)
- ✔ Firewall đã allow port 8090?
- ✔ Bind `0.0.0.0` không phải `127.0.0.1`? (rerun không có `local`)
- ✔ IP đúng? (đôi khi máy có 2-3 NIC, chọn IP của WiFi, không phải Ethernet/VPN)

---

## 🛠️ Tùy chỉnh data mock

### Thêm/sửa response

Mở `db.json` → sửa giá trị → save → json-server **auto-reload** (do flag `--watch`).

Ví dụ thay đổi Security Score:

```diff
   "security_score": {
-    "overallScore": 76.4,
+    "overallScore": 45.2,
-    "status": "STABLE",
+    "status": "CRITICAL",
```

→ Refresh app → score mới hiện ra ngay.

### Thêm endpoint mới

1. Thêm key mới trong `db.json`:
   ```json
   "my_new_data": { "foo": "bar" }
   ```
2. Map route trong `routes.json`:
   ```json
   "/api/my-new-endpoint": "/my_new_data"
   ```
3. Restart mock server (Ctrl+C → chạy lại).

### Simulate slow network

json-server không có built-in delay, nhưng OkHttp side có thể cấu hình:

```kotlin
// Trong ApiClient.kt — testing only
.connectTimeout(15, TimeUnit.SECONDS)
.readTimeout(15, TimeUnit.SECONDS)
```

Hoặc dùng [Android Studio Network Profiler] → throttle download speed.

---

## ⚠️ Hạn chế của mock

| Tính năng | Mock support | Backend live |
|-----------|--------------|--------------|
| GET endpoint | ✅ Đầy đủ | ✅ |
| POST login → token | ✅ (static token) | ✅ (real JWT) |
| Validate JWT | ❌ (mọi request 200) | ✅ (401 nếu sai) |
| POST acknowledge → mutate state | ❌ (luôn trả `ack_response` static) | ✅ (update DB) |
| Pagination | ❌ | ✅ (sẽ thêm nếu cần) |
| Real-time push | ❌ | ❌ (mọi UI dùng pull) |
| OpenAPI `/v3/api-docs` | ❌ | ✅ |

→ **Mock đủ cho UI/UX dev**, nhưng test "happy path" cuối phải làm với backend live.

---

## 🔧 Postman alternative

Nếu json-server không stable trên máy bạn:

1. Import OpenAPI spec từ Leader (file `openapi-3.0.json`) → New → Import → File.
2. Postman → Mock servers → Create mock from collection → expose URL `https://<mock-id>.mock.pstmn.io`.
3. Đổi `BASE_URL` trong Android app → URL Postman mock.

Postman Free Plan cho phép **1000 mock calls/month**, đủ cho dev.

---

## 🧹 Cleanup

Dừng server: `Ctrl+C` trong terminal.

Xóa cache node:
```bash
rm -rf node_modules .cache  # nếu lỡ cài local thay vì npx
```

---

> 💡 **Mock này nằm trong Git** để Leader và Android Dev sync data shape dễ. Khi backend update DTO, Leader update `db.json` → commit → Android pull về → tự nhiên mock + spec đồng bộ.
