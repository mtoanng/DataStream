# 📦 Example Responses — VES Monitor API

14 file JSON sample response cho 14 nhóm endpoint. Dùng để:
- **Hard-code initial state** trong UI khi chưa có network call
- **Test Gson deserialize** offline: `Gson().fromJson(rawJson, DtoClass::class.java)`
- **Compare** với response thật từ backend/mock để verify shape

> Toàn bộ data này cũng có trong `mock/db.json` — file ở đây là phiên bản **split-per-endpoint** giúp Android Dev dễ copy-paste vào unit test fixture.

> 🆕 **Synced với Java backend `v1.0.0` + Phase 7.6/7.7** (commit `e64d447` trên `origin/main`). Pillar taxonomy đã được refactor theo IEA/APERC — xem callout dưới.

---

## ⚠️ Post-v1.0.0 update — pillar taxonomy refactor

Backend Phase 7.1 đã re-design 4 pillars theo IEA/APERC chuẩn quốc tế. Phase 7.6 đã migrate `PillarController` + `PillarDao` + 4 DTO sang shape mới.

| Pillar | Canonical path (recommend) | Legacy alias (vẫn work) | Sample file |
|---|---|---|---|
| 1 — Supply Security | `GET /api/pillars/1/supply-security` | `/1/outlook` | `04_pillar1_outlook_200.json` |
| 2 — Market Resilience | `GET /api/pillars/2/market-resilience` | `/2/volatility` | `05_pillar2_volatility_200.json` |
| 3 — Grid Reliability | `GET /api/pillars/3/grid-reliability` | `/3/shedding`, `/3/shedding-plan` | `06_pillar3_shedding_200.json` |
| 4 — Energy Transition | `GET /api/pillars/4/energy-transition` | `/4/netzero`, `/4/net-zero` | `07_pillar4_netzero_200.json` |

**Cả 2 path đều trả CÙNG response shape mới** (IEA-shaped DTO, không phải shape cũ). File `04..07` ở đây giữ tên file cũ cho backward-compat link, nhưng **content đã update sang shape mới**.

Field name mới (so với `v0.x` shape cũ):
- **P1**: `idr / sfri / hhiSupply / n1Resilience / pillar1Score / status / computedAt` (KHÔNG còn `stockDays / targetDays / supplyStatus / recommendationText`)
- **P2**: `sigma30d / priceGapPct / betaCrude / affordabilityIdx / pillar2Score / status / computedAt` (KHÔNG còn `latestPrice / volatilityIndex / signal`)
- **P3**: flat array `regionCode / reserveMarginPct / peakLoadFactor / sheddingProb / freqStabilityIdx / pillar3Score / status / computedAt` (KHÔNG còn wrapper object `{totalRegions, criticalRegions, regions[]}`)
- **P4**: flat array `regionCode / renewablePct / co2Intensity / curtailmentRate / netzeroProgress / pillar4Score / status / computedAt` (KHÔNG còn wrapper object `{targetYear, currentYear, overallProgressPct, regions[]}`)

Score thresholds: `SECURE ≥80 / ELEVATED 60-79 / STRESSED 40-59 / CRITICAL <40`.

---

## 📋 Mapping

| # | File | Endpoint (canonical) | Code |
|---|------|----------|------|
| 1 | `01_login_200.json` | POST `/api/auth/login` | 200 |
| 2 | `02_security_score_200.json` | GET `/api/security/score` | 200 |
| 3 | `03_security_cascade_200.json` | GET `/api/security/cascade-risks` (deprecated → `[]`) | 200 |
| 4 | `04_pillar1_outlook_200.json` | GET `/api/pillars/1/supply-security` (alias `/1/outlook`) | 200 |
| 5 | `05_pillar2_volatility_200.json` | GET `/api/pillars/2/market-resilience` (alias `/2/volatility`) | 200 |
| 6 | `06_pillar3_shedding_200.json` | GET `/api/pillars/3/grid-reliability` (alias `/3/shedding[-plan]`) | 200 |
| 7 | `07_pillar4_netzero_200.json` | GET `/api/pillars/4/energy-transition` (alias `/4/netzero`, `/4/net-zero`) | 200 |
| 8 | `08_alerts_active_200.json` | GET `/api/alerts/active?limit=20` | 200 |
| 9 | `09_recommendations_200.json` | GET `/api/recommendations?limit=50` | 200 |
| 10 | `10_acknowledge_200.json` | POST `/api/recommendations/{id}/acknowledge` | 200 |
| 11 | `11_fuel_prices_latest_200.json` | GET `/api/fuel-prices/latest` (note: NO `/raw/` prefix) | 200 |
| 12 | `12_grid_load_latest_200.json` | GET `/api/grid-load/latest` (note: NO `/raw/` prefix) | 200 |
| 13 | `13_health_200.json` | GET `/api/health` | 200 |
| 14 | `14_error_401.json` | Bất kỳ endpoint nào với token sai/hết hạn | 401 |

> Backend còn 1 endpoint phụ: `GET /api/auth/me` (trả `UserDto` của session hiện tại) — shape giống field `user` trong `01_login_200.json`.

---

## 💡 Use case: Android unit test

```kotlin
// app/src/test/kotlin/.../SecurityRepositoryTest.kt
class SecurityRepositoryTest {

    @Test
    fun `parse security score response`() {
        val rawJson = javaClass.classLoader
            ?.getResourceAsStream("02_security_score_200.json")
            ?.bufferedReader()?.readText()
            ?: error("Fixture not found")

        val dto = Gson().fromJson(rawJson, SecurityScoreDto::class.java)

        assertEquals(72.82, dto.overallScore, 0.01)
        assertEquals("ELEVATED", dto.status)
        assertEquals(64.83, dto.pillar1Score, 0.01)
        assertNotNull(dto.computedAt)
    }
}
```

**Setup**: copy các JSON file này vào `app/src/test/resources/` để load làm fixture.

---

## 💡 Use case: hard-code preview data

Khi build UI lần đầu, chưa có network:

```kotlin
// HomeFragment.kt
private fun loadPreviewData() {
    val rawJson = """{
        "overallScore": 72.82,
        "status": "ELEVATED",
        ...
    }"""  // copy from 02_security_score_200.json

    val score = Gson().fromJson(rawJson, SecurityScoreDto::class.java)
    renderScore(score)
}
```

Lúc connect Retrofit → xóa preview, dùng `vm.load()`.

---

## ⚠️ Đồng bộ với backend

Khi backend Java team update DTO (vd thêm field mới), Leader sẽ:
1. Cập nhật file tương ứng trong folder này
2. Cập nhật `mock/db.json`
3. Commit + thông báo Android Dev pull về

Android Dev **không cần đoán** — luôn lấy từ folder này làm reference. Authoritative spec là `docs/openapi.json` của repo Java backend.
