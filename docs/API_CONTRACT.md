# 📋 API Contract — VES-Monitor REST API

> Bookmark file này khi code. Đây là **single source of truth** cho 14 endpoint mà app Android sẽ consume.
>
> Khi backend live, OpenAPI spec autoritative tại: `http://<backend-host>:8090/v3/api-docs` (import vào Postman để có collection update mới nhất).

---

## 🌐 Base URL

| Môi trường | URL | Khi dùng |
|------------|-----|----------|
| **Mock server** (repo này) | `http://localhost:8090` (Android Studio emulator: `http://10.0.2.2:8090`) | Dev UI/UX trước, khi backend chưa build |
| **Backend live local** | `http://10.0.2.2:8090` (emulator) hoặc `http://<host-IP>:8090` (real device LAN) | Test integration thật |
| **Backend qua tunnel** | `https://<random>.trycloudflare.com` | Demo từ xa |

App **PHẢI** support đổi `BASE_URL` qua màn Settings để switch giữa 3 môi trường mà không cần rebuild APK.

### Manifest yêu cầu

```xml
<application android:usesCleartextTraffic="true" ... >
```

Hoặc dùng `network_security_config.xml` cho strict mode.

---

## 🔐 Authentication

### Login flow

```
[App]                                    [Backend]
  │  POST /api/auth/login                    │
  │  body: {username, password}              │
  │ ───────────────────────────────────────► │
  │  200 OK                                  │
  │  body: {accessToken, expiresIn, user}    │
  │ ◄─────────────────────────────────────── │
  │                                          │
  │  Save token → SharedPreferences          │
  │                                          │
  │  GET /api/* (any other endpoint)         │
  │  header: Authorization: Bearer <token>   │
  │ ───────────────────────────────────────► │
  │  200 OK / 401 Unauthorized               │
  │ ◄─────────────────────────────────────── │
```

- **Token TTL**: 28800s = 8 giờ
- **Header**: `Authorization: Bearer <accessToken>` cho mọi request authenticated
- **Khi 401**: clear SharedPreferences → quay về Login

### Seed users (cả mock + backend live)

| Username | Password | Role | Quyền |
|----------|----------|------|-------|
| `admin` | `admin` | ADMIN | Full access (CRUD users, regions, alert_rules) |
| `manager` | `manager` | MANAGER | Đọc + acknowledge recommendation |
| `viewer` | `viewer` | VIEWER | Read-only |

---

## 📊 14 Endpoints

### 1. POST /api/auth/login — Đăng nhập (Public)

**Request**:
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Response 200 OK**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwicm9sZSI6IkFETUlOIn0.SIGNATURE",
  "tokenType": "Bearer",
  "expiresIn": 28800,
  "user": {
    "id": 1,
    "username": "admin",
    "fullName": "Administrator",
    "email": "admin@ves.local",
    "role": "ADMIN"
  }
}
```

**Response 401** (sai password): xem mục [Error format](#error-format).

---

### 2. GET /api/security/score — Energy Security Score

**Auth**: Bearer token

**Response 200 OK**:
```json
{
  "overallScore": 76.4,
  "status": "STABLE",
  "pillar1Score": 82.0,
  "pillar2Score": 68.5,
  "pillar3Score": 75.0,
  "pillar4Score": 80.0,
  "calculatedAt": "2026-05-12T10:30:00Z",
  "trend": "DECREASING"
}
```

**Enums**:
- `status` ∈ {`SECURE`, `STABLE`, `AT_RISK`, `CRITICAL`}
- `trend` ∈ {`INCREASING`, `STABLE`, `DECREASING`}

**Score thresholds**:
- 90-100: SECURE (xanh lá)
- 70-89: STABLE (xanh dương)
- 50-69: AT_RISK (cam)
- 0-49: CRITICAL (đỏ)

---

### 3. GET /api/security/cascade-risks — Rủi ro cascade đa pillar

**Auth**: Bearer token

**Response 200 OK**:
```json
[
  {
    "id": 1,
    "riskType": "CASCADE_SUPPLY_GRID",
    "severity": "HIGH",
    "affectedPillars": [1, 3],
    "affectedRegions": ["VN_HANOI", "VN_HAIPHONG"],
    "description": "Tồn kho diesel HÀ NỘI < 30 ngày + lưới điện peak 88% → nguy cơ thiếu cả nhiên liệu lẫn điện trong 2 tuần tới",
    "detectedAt": "2026-05-12T09:15:00Z"
  }
]
```

---

### 4-7. GET /api/pillars/{1-4}/* — 4 Pillar dashboards

#### `/api/pillars/1/outlook` — Pillar 1: Nguồn cung

```json
[
  {
    "regionCode": "VN_HANOI",
    "fuelType": "GASOLINE",
    "stockDays": 56.6,
    "targetDays": 90,
    "deficitDays": 33.4,
    "supplyStatus": "WARNING",
    "recommendationText": "Chuyển 5000 KL từ Ninh Thuận về Hà Nội trước 2026-05-20"
  }
]
```
`supplyStatus` ∈ {`SECURE`, `WARNING`, `CRITICAL`}

#### `/api/pillars/2/volatility` — Pillar 2: Biến động giá

```json
[
  {
    "fuelType": "BRENT_CRUDE",
    "latestPrice": 87.5,
    "priceUnit": "USD/barrel",
    "priceChangePct": 12.3,
    "volatilityIndex": 18.7,
    "trend": "INCREASING",
    "signal": "ELEVATED",
    "windowHours": 24
  }
]
```
`signal` ∈ {`STABLE`, `ELEVATED`, `VOLATILE`}

#### `/api/pillars/3/shedding-plan` — Pillar 3: Phụ tải

```json
{
  "totalRegions": 6,
  "criticalRegions": 2,
  "regions": [
    {
      "regionCode": "VN_HANOI",
      "loadPct": 88.5,
      "loadMw": 10620,
      "capacityMw": 12000,
      "isPeakHour": true,
      "sheddingPriority": "P1",
      "recommendedShedMw": 800,
      "rationale": "Load 88.5% trong khung 18-22h. Cut KCN Thăng Long (~800MW) để giảm tải về 82%."
    }
  ]
}
```
`sheddingPriority` ∈ {`P1` (urgent), `P2`, `P3`, `NONE`}

#### `/api/pillars/4/net-zero-progress` — Pillar 4: Chuyển đổi & MT

```json
{
  "targetYear": 2050,
  "currentYear": 2026,
  "overallProgressPct": 23.5,
  "renewableCapacityMw": 5400,
  "emissionIntensityKgPerMwh": 580,
  "emissionTarget": 100,
  "regions": [
    {
      "regionCode": "VN_NINHTHUAN",
      "renewableShare": 65.0,
      "emissionIntensity": 320,
      "status": "ON_TRACK"
    }
  ]
}
```
`status` ∈ {`ON_TRACK`, `LAGGING`, `OFF_TRACK`}

---

### 8. GET /api/alerts/active — Danh sách cảnh báo

**Auth**: Bearer token

**Response 200 OK**:
```json
[
  {
    "id": 123,
    "ruleId": 5,
    "metricType": "GRID_LOAD_PCT",
    "fuelType": null,
    "region": "VN_HANOI",
    "severity": "CRITICAL",
    "message": "Grid load Hà Nội = 92.5% > threshold 92%",
    "triggeredValue": 92.5,
    "threshold": 92.0,
    "eventTimestamp": "2026-05-12T10:25:00Z",
    "alertTimestamp": "2026-05-12T10:25:03Z"
  }
]
```

**Enums**:
- `metricType` ∈ {`FUEL_PRICE`, `GRID_LOAD_PCT`, `EMISSION_INTENSITY`, `INVENTORY_DAYS`}
- `severity` ∈ {`INFO`, `WARNING`, `CRITICAL`}

---

### 9. GET /api/recommendations — Danh sách khuyến nghị

**Auth**: Bearer token

**Query params**:
- `status` (optional, default `PENDING`): `PENDING` | `ACKNOWLEDGED` | `DISMISSED` | `EXPIRED`

**Response 200 OK**:
```json
[
  {
    "id": 42,
    "pillar": 1,
    "actionType": "TRANSFER_STOCK",
    "severity": "WARNING",
    "title": "Chuyển 5000 KL Gasoline NINHTHUAN → HANOI",
    "message": "Hà Nội còn 56.6 ngày tồn kho, dưới target 90. Đề xuất chuyển 5000 KL từ kho Cát Lái...",
    "suggestedData": {
      "from": "VN_NINHTHUAN",
      "to": "VN_HANOI",
      "volumeKl": 5000,
      "fuel": "GASOLINE"
    },
    "status": "PENDING",
    "suggestedAt": "2026-05-12T08:15:00Z",
    "acknowledgedAt": null,
    "acknowledgedBy": null,
    "note": null,
    "expiresAt": "2026-05-19T08:15:00Z"
  }
]
```

**`actionType` các giá trị**:
- `TRANSFER_STOCK` (Pillar 1)
- `PRICE_HEDGE` (Pillar 2)
- `PEAK_SHAVING_PREP`, `LOAD_SHED` (Pillar 3)
- `RENEWABLE_RAMPUP`, `EMISSION_OFFSET` (Pillar 4)

**`suggestedData` schema thay đổi theo `actionType`** — app render JSON thô hoặc parse theo type.

---

### 10. POST /api/recommendations/{id}/acknowledge — Xác nhận khuyến nghị

**Auth**: Bearer (manager hoặc admin)

**Request**:
```json
{
  "note": "Đã chuyển stock theo plan, hoàn tất 2026-05-13"
}
```

**Response 200 OK**:
```json
{
  "id": 42,
  "status": "ACKNOWLEDGED",
  "acknowledgedAt": "2026-05-12T11:00:00Z",
  "acknowledgedBy": 1,
  "note": "Đã chuyển stock theo plan, hoàn tất 2026-05-13"
}
```

**Response 403** nếu role = VIEWER.

---

### 11. GET /api/raw/fuel-prices/latest — Giá nhiên liệu gần nhất

**Auth**: Bearer token

**Query**: `limit` (default 50, max 200)

**Response**:
```json
[
  {
    "id": 9999,
    "eventTimestamp": "2026-05-12T10:00:00Z",
    "fuelType": "BRENT_CRUDE",
    "price": 87.50,
    "priceUnit": "USD/barrel",
    "location": "Global Market",
    "region": null,
    "source": "WORLD_BANK"
  }
]
```

---

### 12. GET /api/raw/grid-load/latest — Tải lưới gần nhất

**Auth**: Bearer token

**Query**: `region` (optional, lọc theo region_code)

**Response**:
```json
[
  {
    "id": 1234,
    "regionCode": "VN_HANOI",
    "loadMw": 10620,
    "capacityMw": 12000,
    "loadPct": 88.5,
    "isPeakHour": true,
    "eventTime": "2026-05-12T10:30:00Z"
  }
]
```

---

### 13. GET /api/health — Health check (Public)

**Response 200 OK**:
```json
{
  "status": "UP",
  "db": "UP",
  "timestamp": "2026-05-12T10:30:00Z"
}
```

---

### 14. GET /v3/api-docs — OpenAPI spec (Public)

Trả về OpenAPI 3.0 JSON đầy đủ. Import vào:
- **Postman**: New → Import → Link → paste URL
- **OpenAPI Generator**: `openapi-generator-cli generate -i http://localhost:8090/v3/api-docs -g kotlin -o ./generated`

---

## ❌ Error format

Tất cả 4xx/5xx response đều có format chung:

```json
{
  "timestamp": "2026-05-12T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token expired",
  "path": "/api/security/score"
}
```

### Cách xử lý chung trong app

```kotlin
when (response.code()) {
    401 -> {
        // Token hết hạn / sai
        sessionManager.clearToken()
        navController.navigate(R.id.action_global_login)
    }
    403 -> snackbar.show("Không đủ quyền (role: ${session.role})")
    in 500..599 -> snackbar.show("Lỗi server, vui lòng thử lại")
    else -> snackbar.show("Lỗi: ${errorBody.message}")
}
```

---

## 🧪 Sample data files

Folder [`examples/responses/`](../examples/responses/) chứa **14 file JSON** sample cho mỗi endpoint — dùng để:
- Hard-code initial state trong UI khi chưa có network
- Hiểu shape data trước khi viết DTO
- Test deserialize bằng `Gson().fromJson(rawJson, Pillar1OutlookDto::class.java)`

---

## 🔄 Versioning

API version hiện tại: **v1.0.0** (Phase 4.5 — May 2026).

Khi backend update breaking change, sẽ:
1. Update file này (commit vào repo backend)
2. Thông báo trong group chat
3. Tag git release `api-v1.x.x`

Android Dev **không cần đoán** — luôn check file này hoặc OpenAPI live spec để biết contract chính thức.

---

## 📞 Khi contract không khớp với mock / backend

Bug có thể đến từ:
- **Mock data thiếu/sai field** → sửa `mock/db.json` trong repo này
- **Backend trả khác doc** → ping Leader, có thể là bug backend hoặc doc out-of-date
- **DTO Android không match** → check field name camelCase vs snake_case (Spring Boot dùng camelCase mặc định)

> 💡 Khi nghi ngờ: chạy `curl -v` trực tiếp + so sánh raw JSON với DTO. Đừng tin Retrofit silent deserialize — nó skip field thiếu mà không báo.
