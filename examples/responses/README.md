# 📦 Example Responses — VES Monitor API

14 file JSON sample response cho 14 endpoint. Dùng để:
- **Hard-code initial state** trong UI khi chưa có network call
- **Test Gson deserialize** offline: `Gson().fromJson(rawJson, DtoClass::class.java)`
- **Compare** với response thật từ backend/mock để verify shape

> Toàn bộ data này cũng có trong `mock/db.json` — file ở đây là phiên bản **split-per-endpoint** giúp Android Dev dễ copy-paste vào unit test fixture.

---

## 📋 Mapping

| # | File | Endpoint | Code |
|---|------|----------|------|
| 1 | `01_login_200.json` | POST `/api/auth/login` | 200 |
| 2 | `02_security_score_200.json` | GET `/api/security/score` | 200 |
| 3 | `03_security_cascade_200.json` | GET `/api/security/cascade-risks` | 200 |
| 4 | `04_pillar1_outlook_200.json` | GET `/api/pillars/1/outlook` | 200 |
| 5 | `05_pillar2_volatility_200.json` | GET `/api/pillars/2/volatility` | 200 |
| 6 | `06_pillar3_shedding_200.json` | GET `/api/pillars/3/shedding-plan` | 200 |
| 7 | `07_pillar4_netzero_200.json` | GET `/api/pillars/4/net-zero-progress` | 200 |
| 8 | `08_alerts_active_200.json` | GET `/api/alerts/active` | 200 |
| 9 | `09_recommendations_200.json` | GET `/api/recommendations` | 200 |
| 10 | `10_acknowledge_200.json` | POST `/api/recommendations/{id}/acknowledge` | 200 |
| 11 | `11_fuel_prices_latest_200.json` | GET `/api/raw/fuel-prices/latest` | 200 |
| 12 | `12_grid_load_latest_200.json` | GET `/api/raw/grid-load/latest` | 200 |
| 13 | `13_health_200.json` | GET `/api/health` | 200 |
| 14 | `14_error_401.json` | Bất kỳ endpoint nào với token sai/hết hạn | 401 |

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

        assertEquals(76.4, dto.overallScore, 0.01)
        assertEquals("STABLE", dto.status)
        assertEquals(4, dto.pillar1Score.toInt() / 20)  // 82 → score group 4
        assertEquals("DECREASING", dto.trend)
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
        "overallScore": 76.4,
        "status": "STABLE",
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

Android Dev **không cần đoán** — luôn lấy từ folder này làm reference.
