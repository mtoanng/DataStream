# 📋 API Contract — VES-Monitor REST API

> Bookmark file này khi code. Đây là **single source of truth** cho 14 endpoint mà app Android sẽ consume.
>
> Khi backend live, OpenAPI spec authoritative tại: `http://<backend-host>:8090/v3/api-docs` (import vào Postman để có collection update mới nhất).
>
> 🆕 **Synced với Java backend `v1.0.0` + Phase 7.6/7.7** (commit `e64d447` trên `origin/main`, 13/05/2026). Pillar taxonomy đã được refactor theo IEA/APERC — xem [§ Post-v1.0.0 update](#-post-v100-update--pillar-taxonomy--shape-changes) bên dưới.

---

## ⚠️ Post-v1.0.0 update — pillar taxonomy + shape changes

Backend Phase 7.1 đã re-design 4 pillars theo chuẩn quốc tế **IEA / APERC** (Availability / Affordability / Accessibility / Acceptability). Phase 7.6 đã migrate `PillarController` + `PillarDao` + 4 DTO sang shape mới và **giữ legacy aliases** để client cũ không break.

### Pillar path map

| Pillar | Canonical (IEA/APERC, recommend cho code mới) | Legacy alias (vẫn work) |
|---|---|---|
| 1 — Supply Security | `GET /api/pillars/1/supply-security` | `GET /api/pillars/1/outlook` |
| 2 — Market Resilience | `GET /api/pillars/2/market-resilience` | `GET /api/pillars/2/volatility` |
| 3 — Grid Reliability | `GET /api/pillars/3/grid-reliability` | `GET /api/pillars/3/shedding`, `/3/shedding-plan` |
| 4 — Energy Transition | `GET /api/pillars/4/energy-transition` | `GET /api/pillars/4/netzero`, `/4/net-zero` |

> **Cả 2 path đều trả CÙNG response shape mới** (IEA-shaped DTO). Legacy alias chỉ là route forward — không có shape cũ nào còn được serve.

### Status enum thresholds (ALL pillars + composite ESI)

`SECURE ≥ 80` · `ELEVATED 60-79` · `STRESSED 40-59` · `CRITICAL < 40`

(Lưu ý: enum cũ `STABLE / AT_RISK` đã bỏ.)

### Composite ESI (Energy Security Index)

`ESI = 0.30 × P1 + 0.20 × P2 + 0.30 × P3 + 0.20 × P4` — IEA-standard weighting.

### Endpoint-level changes (so với v0.x)

| Endpoint | Thay đổi quan trọng |
|---|---|
| `POST /api/auth/login` | Field `expiresIn` (giây) → **`expiresInMs`** (mili-giây). KHÔNG còn `tokenType`. `user` thêm field `enabled: boolean`. |
| `GET /api/auth/me` | **Mới** — trả `UserDto` của user hiện tại (yêu cầu Bearer). |
| `GET /api/security/score` | Field `calculatedAt` → **`computedAt`**. Status enum đổi (xem trên). KHÔNG còn `trend`. |
| `GET /api/security/cascade-risks` | **Deprecated** — `v_cascade_risks` view bị drop ở Phase 7.1. Endpoint vẫn trả `200 OK` nhưng body luôn là `[]`. App nên hide hoặc render gracefully. |
| `GET /api/pillars/...` | Toàn bộ 4 pillar — shape mới (xem mục 4-7 dưới). Legacy alias path vẫn work. |
| `GET /api/alerts/active` | Field `triggeredValue` → **`triggeredPrice`**. Thêm `ruleName`, `location`, `operator`, `ageSeconds`. Query param `limit` (default 20, max 200). |
| `GET /api/recommendations` | Query param đổi: KHÔNG còn `?status=...`, dùng `?limit=N` (default 50, max 200). Response item KHÔNG còn `status / acknowledgedAt / acknowledgedBy / note` (chỉ trả PENDING & chưa expired); thêm `ageSeconds`, `expired`. |
| `POST /api/recommendations/{id}/acknowledge` | Body cho phép `{status: 'ACKNOWLEDGED' (default) \| 'DISMISSED', note?}`. Response shape: `{id, newStatus, acknowledgedBy}` (KHÔNG còn `acknowledgedAt`, KHÔNG echo `note`). |
| `GET /api/fuel-prices/latest` | **Path đổi**: bỏ prefix `/raw/` (cũ: `/api/raw/fuel-prices/latest`). Query: `?fuel_type=X&limit=N` (default 20, max 500). Mock vẫn route cả 2 path để backward-compat. |
| `GET /api/grid-load/latest` | **Path đổi**: bỏ prefix `/raw/` (cũ: `/api/raw/grid-load/latest`). KHÔNG còn query `?region=`. Shape thêm `regionName`, `status`; field `isPeakHour` → **`peakHour`**; bỏ `id`. |
| `GET /api/health` | Thêm field `service: "ves-backend-api"` ở đầu object. |

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
  │  body: {accessToken, expiresInMs, user}  │
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

- **Token TTL**: `expiresInMs = 28800000` (8 giờ).
- **Header**: `Authorization: Bearer <accessToken>` cho mọi request authenticated.
- **Khi 401**: clear SharedPreferences → quay về Login.

### Seed users (cả mock + backend live)

| Username | Password | Role | Quyền |
|----------|----------|------|-------|
| `admin` | `admin` | ADMIN | Full access (CRUD users, regions, alert_rules) |
| `manager` | `manager` | MANAGER | Đọc + acknowledge recommendation |
| `viewer` | `viewer` | VIEWER | Read-only |

---

## 📊 Endpoints

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
  "expiresInMs": 28800000,
  "user": {
    "id": 1,
    "username": "admin",
    "fullName": "Administrator",
    "email": "admin@ves.local",
    "role": "ADMIN",
    "enabled": true
  }
}
```

**Response 401** (sai password): xem mục [Error format](#-error-format).

---

### 1b. GET /api/auth/me — Thông tin user hiện tại (Bearer)

**Response 200 OK**: `UserDto` (cùng shape với field `user` trong login response).

```json
{
  "id": 1,
  "username": "admin",
  "fullName": "Administrator",
  "email": "admin@ves.local",
  "role": "ADMIN",
  "enabled": true
}
```

---

### 2. GET /api/security/score — Energy Security Index (ESI)

**Auth**: Bearer token

**Response 200 OK**:
```json
{
  "pillar1Score": 64.83,
  "pillar2Score": 59.59,
  "pillar3Score": 90.11,
  "pillar4Score": 72.09,
  "overallScore": 72.82,
  "status": "ELEVATED",
  "computedAt": "2026-05-13T10:30:00Z"
}
```

**Composite formula**: `ESI = 0.30·P1 + 0.20·P2 + 0.30·P3 + 0.20·P4` (IEA weights).

**Status enum** (ALL pillars + composite):
- `SECURE` ≥ 80 (green `#2E7D32`)
- `ELEVATED` 60-79 (yellow `#F9A825`)
- `STRESSED` 40-59 (orange `#EF6C00`)
- `CRITICAL` < 40 (red `#C62828`)

> ⚠️ Status enum cũ `STABLE / AT_RISK` đã bị bỏ. Cũng KHÔNG còn field `trend`.

---

### 3. GET /api/security/cascade-risks — [DEPRECATED]

**Auth**: Bearer token

**Status**: View `v_cascade_risks` đã bị drop ở Phase 7.1. Endpoint vẫn trả `200 OK` nhưng body luôn là **mảng rỗng** để client cũ không break. Cascade analysis sẽ được re-implement ở phase sau bằng cách correlate alerts đa-pillar.

**Response 200 OK**:
```json
[]
```

> 💡 Android UI nên **hide section này** (kiểm tra `if (list.isEmpty()) hide()`) hoặc render placeholder "Đang chuẩn bị tính năng này".

---

### 4. GET /api/pillars/1/supply-security — Pillar 1 (Availability)

**Aliases**: `GET /api/pillars/1/outlook` (legacy, cùng shape).

**Auth**: Bearer token

**Sub-indicators** (IEA + APERC):
- `idr` — Import Dependency Ratio (0-1, lower = better)
- `sfri` — Strategic Fuel Reserve Index = stock days (target ≥ 90)
- `hhiSupply` — Herfindahl-Hirschman concentration index (0-10000, lower = more diversified)
- `n1Resilience` — Days of cover if largest single fuel source disrupted

**Response 200 OK**:
```json
[
  {
    "regionCode": "VN_HANOI",
    "fuelType": "GASOLINE",
    "idr": 0.842,
    "sfri": 56.6,
    "hhiSupply": 4250.32,
    "n1Resilience": 22.4,
    "pillar1Score": 58.71,
    "status": "STRESSED",
    "computedAt": "2026-05-13T10:30:00Z"
  }
]
```

**P1 score formula**: `0.25·IDR_inv + 0.35·SFRI_norm + 0.20·HHI_inv + 0.20·N1_norm` (rescaled 0-100).

---

### 5. GET /api/pillars/2/market-resilience — Pillar 2 (Affordability)

**Aliases**: `GET /api/pillars/2/volatility` (legacy, cùng shape).

**Auth**: Bearer token

**Sub-indicators** (IEA + IMF):
- `sigma30d` — Rolling price std-dev (1h window proxy for 30-day dispersion)
- `priceGapPct` — Local-vs-benchmark gap, benchmark = avg(WTI, Brent)
- `betaCrude` — OLS slope of fuel returns vs crude returns
- `affordabilityIdx` — Composite affordability score, clamp [0, 100]

**Response 200 OK**:
```json
[
  {
    "fuelType": "BRENT_CRUDE",
    "sigma30d": 4.2156,
    "priceGapPct": 0.00,
    "betaCrude": 1.000,
    "affordabilityIdx": 12.50,
    "pillar2Score": 48.27,
    "status": "STRESSED",
    "computedAt": "2026-05-13T10:30:00Z"
  }
]
```

**P2 score formula**: `0.30·sigma_inv + 0.25·gap_inv + 0.20·beta_proximity_to_1 + 0.25·affordability`.

---

### 6. GET /api/pillars/3/grid-reliability — Pillar 3 (Accessibility)

**Aliases**: `GET /api/pillars/3/shedding`, `GET /api/pillars/3/shedding-plan` (legacy, cùng shape).

**Auth**: Bearer token

**Sub-indicators** (NERC + IEEE 1366):
- `reserveMarginPct` — `(peak_capacity - peak_load) / peak_capacity × 100`
- `peakLoadFactor` — `peak_load / avg_load` (1h window)
- `sheddingProb` — `P(load_pct > 95)` over last 1h, range [0, 1]
- `freqStabilityIdx` — `100 - stddev(load_pct) × 10`, clamp [0, 100]

**Response 200 OK** (flat array, 1 row per region):
```json
[
  {
    "regionCode": "VN_HCM",
    "reserveMarginPct": 7.90,
    "peakLoadFactor": 1.318,
    "sheddingProb": 0.0834,
    "freqStabilityIdx": 84.65,
    "pillar3Score": 64.12,
    "status": "ELEVATED",
    "computedAt": "2026-05-13T10:30:00Z"
  }
]
```

> ⚠️ Shape cũ là wrapper object `{totalRegions, criticalRegions, regions[]}` — bị bỏ. Giờ là flat array.

**P3 score formula**: `0.30·reserve + 0.20·peak_inv + 0.30·shed_inv + 0.20·freq_stability`.

---

### 7. GET /api/pillars/4/energy-transition — Pillar 4 (Acceptability)

**Aliases**: `GET /api/pillars/4/netzero`, `GET /api/pillars/4/net-zero` (legacy, cùng shape).

**Auth**: Bearer token

**Sub-indicators** (IPCC AR6 + Net-Zero 2050):
- `renewablePct` — Renewable share of demand `Σ ren_mw / avg_load_mw × 100`
- `co2Intensity` — Carbon intensity (kg/MWh)
- `curtailmentRate` — Wasted renewable capacity `(cap - output) / cap × 100`
- `netzeroProgress` — `current_renewable_pct / 70.0 × 100`, clamp 100 (linear path to 70% by 2050)

**Response 200 OK** (flat array, 1 row per region):
```json
[
  {
    "regionCode": "VN_NINHTHUAN",
    "renewablePct": 65.00,
    "co2Intensity": 320.00,
    "curtailmentRate": 8.40,
    "netzeroProgress": 92.86,
    "pillar4Score": 89.32,
    "status": "SECURE",
    "computedAt": "2026-05-13T10:30:00Z"
  }
]
```

> ⚠️ Shape cũ là wrapper object `{targetYear, currentYear, overallProgressPct, regions[]}` — bị bỏ. Giờ là flat array.

**P4 score formula**: `0.30·renewable·2 + 0.25·intensity_inv + 0.20·curtailment_inv + 0.25·netzero`.

---

### 8. GET /api/alerts/active — Danh sách cảnh báo

**Auth**: Bearer token

**Query params**:
- `limit` (optional, default 20, max 200)

**Response 200 OK**:
```json
[
  {
    "id": 1001,
    "ruleId": 5,
    "ruleName": "Grid load CRITICAL >90%",
    "metricType": "GRID_LOAD_PCT",
    "fuelType": null,
    "location": null,
    "region": "VN_HCM",
    "triggeredPrice": 92.10,
    "threshold": 90.00,
    "operator": "GT",
    "severity": "CRITICAL",
    "message": "Grid load HCM = 92.1% > threshold 90%",
    "eventTimestamp": "2026-05-13T10:25:00Z",
    "alertTimestamp": "2026-05-13T10:25:03Z",
    "ageSeconds": 297
  }
]
```

**Enums**:
- `metricType` ∈ {`FUEL_PRICE`, `GRID_LOAD_PCT`, `EMISSION_INTENSITY`, `INVENTORY_DAYS`}
- `severity` ∈ {`INFO`, `WARNING`, `CRITICAL`}
- `operator` ∈ {`GT`, `LT`, `GTE`, `LTE`, `EQ`}

> ⚠️ Field cũ `triggeredValue` đã đổi thành **`triggeredPrice`** (tên giữ "price" nhưng có thể chứa loadPct / intensity / days tuỳ `metricType`). Thêm `ruleName`, `location`, `operator`, `ageSeconds`.

---

### 9. GET /api/recommendations — Danh sách khuyến nghị

**Auth**: Bearer token

**Query params**:
- `limit` (optional, default 50, max 200)

> ⚠️ KHÔNG còn `?status=` — endpoint chỉ trả những recommendation **PENDING & chưa expired**. Acknowledged/Dismissed/Expired không xuất hiện ở list này (sẽ cần endpoint riêng nếu phase sau cần audit trail).

**Response 200 OK**:
```json
[
  {
    "id": 42,
    "pillar": 1,
    "actionType": "TRANSFER_STOCK",
    "severity": "WARNING",
    "title": "Chuyển 5000 KL Gasoline NINHTHUAN → HANOI",
    "message": "Hà Nội còn 56.6 ngày tồn kho, dưới target 90...",
    "suggestedData": {
      "from": "VN_NINHTHUAN",
      "to": "VN_HANOI",
      "volumeKl": 5000,
      "fuel": "GASOLINE"
    },
    "suggestedAt": "2026-05-13T08:15:00Z",
    "ageSeconds": 8100,
    "expiresAt": "2026-05-20T08:15:00Z",
    "expired": false
  }
]
```

**`actionType` các giá trị thường gặp**:
- `TRANSFER_STOCK`, `HEDGE_IMPORT` (Pillar 1)
- `PRICE_HEDGE` (Pillar 2)
- `PEAK_SHAVING_PREP`, `LOAD_SHED` (Pillar 3)
- `RENEWABLE_RAMPUP`, `EMISSION_OFFSET` (Pillar 4)

**`suggestedData` schema thay đổi theo `actionType`** — backend trả nguyên JSONB (Spring `@JsonRawValue`), app render JSON thô hoặc parse theo type.

> ⚠️ Field cũ `status / acknowledgedAt / acknowledgedBy / note` đã bị bỏ khỏi list response. Thêm `ageSeconds`, `expired`.

---

### 10. POST /api/recommendations/{id}/acknowledge — Ack hoặc Dismiss

**Auth**: Bearer (manager hoặc admin)

**Request** (body optional):
```json
{
  "status": "ACKNOWLEDGED",
  "note": "Đã chuyển stock theo plan, hoàn tất 2026-05-13"
}
```

- `status` ∈ {`ACKNOWLEDGED` (default), `DISMISSED`}
- `note` optional, free text

**Response 200 OK**:
```json
{
  "id": 42,
  "newStatus": "ACKNOWLEDGED",
  "acknowledgedBy": 1
}
```

**Response 400** nếu `status` không hợp lệ.
**Response 404** nếu recommendation không tồn tại / đã processed trước đó.
**Response 401/403** nếu thiếu/sai token / role không đủ quyền.

> ⚠️ Response shape mới chỉ có 3 field. KHÔNG còn `acknowledgedAt`, KHÔNG echo `note` (chỉ persist DB-side).

---

### 11. GET /api/fuel-prices/latest — Giá nhiên liệu gần nhất

> ⚠️ **Path đã đổi**: bỏ prefix `/raw/`. Cũ: `/api/raw/fuel-prices/latest` → mới: `/api/fuel-prices/latest`. Mock server vẫn route cả 2 path để khỏi break code Android cũ.

**Auth**: Bearer token

**Query**:
- `fuel_type` (optional, snake_case): `BRENT_CRUDE` | `WTI_CRUDE` | `GASOLINE` | `DIESEL`
- `limit` (optional, default 20, max 500)

**Response**:
```json
[
  {
    "id": 9999,
    "eventTimestamp": "2026-05-13T10:00:00Z",
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

### 12. GET /api/grid-load/latest — Tải lưới gần nhất

> ⚠️ **Path đã đổi**: bỏ prefix `/raw/`. Cũ: `/api/raw/grid-load/latest` → mới: `/api/grid-load/latest`. Mock server vẫn route cả 2 path.
>
> Query `?region=` đã bỏ — backend giờ trả tất cả region trong 1 lần.

**Auth**: Bearer token

**Response**:
```json
[
  {
    "regionCode": "VN_HANOI",
    "regionName": "Hà Nội",
    "loadMw": 10620.00,
    "capacityMw": 12000.00,
    "loadPct": 88.50,
    "peakHour": true,
    "status": "ELEVATED",
    "eventTime": "2026-05-13T10:30:00Z"
  }
]
```

> ⚠️ Field cũ `isPeakHour` (boolean) đã đổi thành **`peakHour`** (Lombok unwrapping). Thêm `regionName`, `status`. Bỏ `id`.

---

### 13. GET /api/health — Health check (Public)

**Response 200 OK**:
```json
{
  "service": "ves-backend-api",
  "timestamp": "2026-05-13T10:30:00Z",
  "db": "UP",
  "status": "UP"
}
```

> Thêm field `service`. Trả 503 nếu DB down (`status: "DEGRADED"`, `db: "DOWN"`, kèm `error`).

---

### 14. GET /v3/api-docs — OpenAPI spec (Public)

Trả về OpenAPI 3.0 JSON đầy đủ (~20 paths bao gồm legacy aliases). Import vào:
- **Postman**: New → Import → Link → paste URL
- **OpenAPI Generator**: `openapi-generator-cli generate -i http://localhost:8090/v3/api-docs -g kotlin -o ./generated`

Spec snapshot lưu trong repo Java: `docs/openapi.json`.

---

## ❌ Error format

Tất cả 4xx/5xx response đều có format chung:

```json
{
  "timestamp": "2026-05-13T10:30:00Z",
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

Folder [`examples/responses/`](../examples/responses/) chứa **14 file JSON** sample cho mỗi endpoint — đã được sync với shape mới sau Phase 7.6/7.7. Dùng để:
- Hard-code initial state trong UI khi chưa có network
- Hiểu shape data trước khi viết DTO
- Test deserialize bằng `Gson().fromJson(rawJson, Pillar1SupplySecurityDto::class.java)`

---

## 🔄 Versioning

API version hiện tại: **`v1.0.0`** (tag `3d30b39`, 13/05/2026) + post-release fixes Phase 7.6/7.7 (`e64d447` trên `origin/main`).

Khi backend update breaking change, sẽ:
1. Update file này (commit vào repo Android)
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
