# 📱 DataStream — VES-Monitor Mobile (Android)

> Đồ án môn **Phát triển ứng dụng di động (Android)** — ứng dụng giám sát **An ninh năng lượng Việt Nam** thời gian thực.
> Repo này **độc lập** với đồ án Java (backend Spring Boot + Flink + Postgres). Mobile app chỉ consume **14 endpoint REST API** mà backend đó expose.
>
> 🆕 **Synced với Java backend `v1.0.0` + Phase 7.6/7.7** (commit `e64d447`, 13/05/2026). Pillar taxonomy đã refactor IEA/APERC. Backward-compat aliases giữ nguyên — code Android cũ KHÔNG break, nhưng shape DTO đã đổi. Đọc [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md) để biết chi tiết.

[![Status](https://img.shields.io/badge/status-Handover%20Ready-brightgreen)]() [![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-green)]() [![Language](https://img.shields.io/badge/language-Kotlin%201.9.21-purple)]() [![Build](https://img.shields.io/badge/Gradle-8.5-blueviolet)]() [![AGP](https://img.shields.io/badge/AGP-8.2.0-orange)]() [![Tests](https://img.shields.io/badge/tests-15%2F15-success)]() [![License](https://img.shields.io/badge/license-Educational-blue)]()

---

> 🚦 **Lần đầu vào repo này?** → Mở [`TRY_THIS_FIRST.md`](TRY_THIS_FIRST.md) trước. Đó là **3-minute happy path** + bảng remediation cho 5 lỗi import phổ biến nhất + link đến mọi tài liệu khác. Nếu bạn chỉ có 5 phút, đọc file đó là đủ.

---

## 🚀 Quickstart (Android Studio)

> **App source code is ready.** Clone, open in Android Studio, hit Run.

1. **Clone & pre-flight check**
   ```powershell
   git clone https://github.com/mtoanng/DataStream.git
   cd DataStream
   .\scripts\verify-environment.ps1   # 10 checks; exits 0 when ready
   ```
2. Open `DataStream/` in **Android Studio Hedgehog (2023.1)**, **Iguana (2023.2)**, or **Jellyfish (2024.1+)**.
3. Wait for **Gradle sync** (~3–5 min first time, pulls ~150 MB of deps from Maven Central / Google Maven / JitPack).
   - Android Studio will auto-generate `gradle/wrapper/gradle-wrapper.jar` on first sync — that's expected, the wrapper script + properties are already in the repo.
   - JDK 17 is required (AGP 8.2). JDK 21 also works because AGP targets bytecode 17. AS bundles its own JDK 17 — usually no extra setup.
4. Start the **Java backend** on the host machine (port `8090`) — see [Real-time-processing-with-Kafka-Flink-Postgres](https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres). Or use the bundled `mock/` server (see below) for UI-only dev.
5. Pick a **Pixel 5 / API 34** emulator and hit ▶ Run.
6. Login with `admin` / `admin` (seed user pre-filled in the form).
7. The default server URL is `http://10.0.2.2:8090` (the emulator's host loopback). For a **real device** on Wi-Fi, open the **Settings** tab → set Base URL to `http://<your-laptop-LAN-IP>:8090`.

App auto-refreshes the dashboard every 30 s when in foreground. Pull-to-refresh works on every list. Swipe between Pillar tabs.

### 🧪 Testing without backend (mock server)

If you don't want to run the full Java stack:

```powershell
cd mock
.\start.ps1   # PowerShell
# or: bash start.sh   # WSL / git-bash
```

Mock server listens on `http://localhost:8090` with the same JSON shapes as the real backend. The emulator reaches it at `http://10.0.2.2:8090`.

### 🧰 What's in the `app/` module

```
app/
├── build.gradle.kts                           # Module Gradle config (min/target/compile SDK 26/34/34)
├── proguard-rules.pro
└── src/
    ├── main/
    │   ├── AndroidManifest.xml                # 2 activities + INTERNET + cleartext config
    │   ├── java/com/mtoanng/datastream/
    │   │   ├── DataStreamApp.kt               # Manual DI container + Timber init
    │   │   ├── data/
    │   │   │   ├── network/                   # Retrofit + OkHttp + Moshi + AuthInterceptor
    │   │   │   ├── dto/                       # 16 DTOs matching backend v1.0.0 + Phase 7.6/7.7
    │   │   │   ├── repository/                # Auth/Security/Pillar/Alert/Recommendation
    │   │   │   └── prefs/                     # TokenManager + AppConfig (SharedPreferences)
    │   │   ├── ui/
    │   │   │   ├── login/                     # LoginActivity + ViewModel
    │   │   │   ├── main/                      # Single-Activity host (BottomNav + NavGraph)
    │   │   │   ├── home/                      # ESI gauge + 2x2 pillar mini-cards
    │   │   │   ├── pillars/                   # TabLayout + ViewPager2 + 4 pillar tabs
    │   │   │   ├── alerts/                    # RecyclerView + severity chips + bottom sheet
    │   │   │   ├── recommendations/           # RecyclerView + ACK dialog
    │   │   │   ├── settings/                  # Server URL + /api/health test + logout
    │   │   │   └── common/                    # PillarScoreView gauge + StatusBadge chip
    │   │   └── util/                          # Formatters, Extensions, EnergySecurityHelper
    │   └── res/                               # 22 layouts + Material 3 theme (light/dark)
    │                                          # bilingual strings (values/, values-vi/)
    │                                          # network_security_config.xml (allow 10.0.2.2 + LAN)
    └── test/                                  # JUnit + Mockito + MockWebServer (15 tests across 3 files)
```

### 📐 Architecture decisions (locked)

| | |
|---|---|
| Language | Kotlin 1.9.21 |
| Build | Gradle 8.5 + AGP 8.2.0 (Kotlin DSL + version catalog) |
| Min / target / compile SDK | 26 / 34 / 34 |
| UI | XML Views + ViewBinding (no Jetpack Compose) |
| Architecture | MVVM, single-Activity + Fragments + Navigation Component |
| DI | Manual (no Hilt/Dagger/Koin) |
| Network | Retrofit 2.9 + OkHttp 4.12 + Moshi 1.15 |
| Async | Coroutines + LiveData |
| Charts | MPAndroidChart 3.1.0 (via JitPack) |
| Persistence | SharedPreferences only (no Room) |
| Logging | Timber 5.0.1 |
| Theming | Material 3 DayNight + brand palette matching the JavaFX desktop |
| i18n | EN (default) + VI (`values-vi/`) |

---

## 👋 Bắt đầu từ đâu?

> 🚦 **Bước 0**: chạy [`./scripts/verify-environment.ps1`](scripts/verify-environment.ps1) (Windows) hoặc [`bash scripts/verify-environment.sh`](scripts/verify-environment.sh) (mac/Linux/WSL) → script kiểm tra JDK / disk / network / Android Studio / wrapper jar trong < 10 s.
>
> 🆕 **Bước 1**: đọc [`TRY_THIS_FIRST.md`](TRY_THIS_FIRST.md) (3-min happy path + 5 lỗi import phổ biến nhất + bảng nav 3 đường vào).
>
> 🧭 **Bước 2**: nếu bạn là dev tham gia team Android, đọc [`docs/START_HERE.md`](docs/START_HERE.md) — tour guide 4 tuần từ Day -1 đến PR cuối.

| # | File | Mô tả | Audience |
|---|------|-------|---------|
| 🚦 | **[`TRY_THIS_FIRST.md`](TRY_THIS_FIRST.md)** | **3-min happy path + remediation table** | Mọi người (đọc đầu tiên) |
| ✅ | [`SETUP_CHECKLIST.md`](SETUP_CHECKLIST.md) | Checklist 1 trang trước khi mở AS | Dev lần đầu |
| 🛠 | [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md) | 20 lỗi phổ biến + fix copy-paste | Dev gặp lỗi |
| 🧭 | [`docs/START_HERE.md`](docs/START_HERE.md) | Tour guide 4 tuần kickoff → PR7 | Dev mới join team |
| 👨‍💻 | [`docs/DEVELOPER_ONBOARDING.md`](docs/DEVELOPER_ONBOARDING.md) | Hands-on architecture walkthrough | Dev hiểu codebase |
| 1 | [`docs/ANDROID_ONBOARDING.md`](docs/ANDROID_ONBOARDING.md) | Briefing đầy đủ: bối cảnh, scope, stack khuyến nghị | Dev mới |
| 2 | [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md) | 14 endpoint + sample request/response | Dev khi code feature |
| 3 | [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | MVVM single-module + Gradle deps | Dev hiểu kiến trúc |
| 📐 | [`docs/diagrams/excalidraw/`](docs/diagrams/excalidraw/) | 5 architecture diagrams | Dev / mentor / grader |
| 🚀 | [`docs/QUICKSTART_FOR_USER.md`](docs/QUICKSTART_FOR_USER.md) | Setup chi tiết cho non-dev | Người không biết Android |
| 🤝 | [`HANDOVER_CHECKLIST.md`](HANDOVER_CHECKLIST.md) | Checklist mọi thứ đã verify trước handover | Reviewer / mentor |

---

## 🏗️ Cấu trúc repo

```
DataStream/
├── README.md                    # 👈 Bạn đang đọc
├── TRY_THIS_FIRST.md            # 🚦 3-min happy path + 5 lỗi import phổ biến
├── SETUP_CHECKLIST.md
├── TROUBLESHOOTING.md
├── HANDOVER_CHECKLIST.md        # ✅ Checklist mọi thứ đã verify trước handover
├── .gitignore
├── .gitattributes               # Line-ending policy (LF/CRLF) — đảm bảo gradlew không bị mangle trên Windows
├── build.gradle.kts             # Root Gradle config (AGP 8.2 + Kotlin 1.9.21)
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat        # Wrapper scripts
├── gradle/
│   ├── libs.versions.toml       # Dependency version catalog
│   └── wrapper/
│       ├── gradle-wrapper.jar          # 43,462 bytes (Gradle 8.5 canonical, committed)
│       └── gradle-wrapper.properties
├── local.properties.template
│
├── scripts/                     # ✅ Pre-flight environment check (PowerShell + Bash)
│   ├── verify-environment.ps1
│   └── verify-environment.sh
│
├── app/                         # ★ Android Studio module (package com.mtoanng.datastream)
│   ├── build.gradle.kts
│   ├── proguard-rules.pro       # R8 release rules: Moshi + Retrofit + MPAndroidChart kept
│   └── src/
│       ├── main/                # AndroidManifest, 56 .kt sources, 14 layouts, 36 XML resources
│       └── test/                # 3 test files / 15 @Test methods (JVM unit tests)
│
├── docs/
│   ├── START_HERE.md            # 🧭 Tour guide 4 tuần
│   ├── DEVELOPER_ONBOARDING.md  # Hands-on architecture walkthrough
│   ├── ANDROID_ONBOARDING.md
│   ├── API_CONTRACT.md
│   ├── ARCHITECTURE.md
│   ├── KICKOFF_AGENDA.md
│   ├── QUICKSTART_FOR_USER.md
│   ├── AUDIT_REPORT_ANDROID.md  # Pre-first-sync static audit
│   ├── MASS_QA_REPORT.md        # Final cross-repo QA gate
│   ├── FINAL_GATE_REPORT.md     # Handover gate sign-off (this audit)
│   └── diagrams/excalidraw/     # 5 .excalidraw architecture scenes
│
├── mock/                        # Mock backend (json-server) — full 14-endpoint stub
│   ├── db.json
│   ├── routes.json
│   ├── middleware.js
│   ├── start.sh / start.ps1
│   └── README.md
│
└── examples/responses/          # 14 JSON sample payloads (one per endpoint)
```

> ✅ The `app/` module + Gradle wrapper are now in place — open in Android Studio and sync. See the [Quickstart](#-quickstart-android-studio) section above.

---

## 🚀 Quick start

### 1. Clone repo

```bash
git clone https://github.com/mtoanng/DataStream.git
cd DataStream
```

### 2. Chạy mock backend (5 phút setup)

```bash
# Cài Node.js 18+ trước: https://nodejs.org/

# Linux/macOS/WSL
cd mock && bash start.sh

# Windows PowerShell
cd mock; .\start.ps1
```

Mock server chạy tại `http://localhost:8090` — y hệt URL backend thật. Đã được verify **14/14 endpoint** trả đúng response (xem [`mock/README.md`](mock/README.md) cho smoke test). Quick check:

```bash
# 1. Health check
curl http://localhost:8090/api/health
# → {"service":"ves-backend-api","timestamp":"...","db":"UP","status":"UP"}

# 2. Login với seed user
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
# → {"accessToken":"...","expiresInMs":28800000,"user":{"role":"ADMIN","enabled":true,...}}

# 3. Login sai password → 401
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrong"}'
# → 401 {"status":401,"error":"Unauthorized",...}

# 4. Pillar 1 (canonical hoặc legacy alias đều work, cùng shape)
curl http://localhost:8090/api/pillars/1/supply-security
curl http://localhost:8090/api/pillars/1/outlook
# → [{"regionCode":"VN_HANOI","fuelType":"GASOLINE","idr":0.842,"sfri":56.6,...}]

# 5. Acknowledge khuyến nghị (Phase 7.6 shape)
curl -X POST http://localhost:8090/api/recommendations/42/acknowledge \
  -H "Content-Type: application/json" \
  -d '{"status":"ACKNOWLEDGED","note":"OK done"}'
# → {"id":42,"newStatus":"ACKNOWLEDGED","acknowledgedBy":1}
```

### 3. Mở Android Studio

Repo đã sẵn module `app/` (package `com.mtoanng.datastream`, min SDK 26, Gradle 8.5 + AGP 8.2.0). Mở thư mục root → Android Studio sync Gradle → ▶ Run.

Xem [§ Quickstart](#-quickstart-android-studio) ở đầu file này để biết chi tiết.

### 4. Đổi server URL

Default là `http://10.0.2.2:8090` (emulator's host loopback). Đổi qua màn **Settings** trong app — không cần rebuild APK.

---

## 🔗 Liên kết với đồ án Java (backend)

| Thông tin | Giá trị |
|-----------|---------|
| **Repo backend** | https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres |
| **Backend stack** | Spring Boot 2.7 + JWT + JdbcTemplate + Postgres 15 |
| **Backend port** | 8090 (mặc định) |
| **API spec** | 14 endpoint REST + 6 legacy alias (20 paths trong OpenAPI), JWT Bearer auth, OpenAPI 3.0 |
| **Pillar taxonomy** | IEA/APERC: P1 supply-security · P2 market-resilience · P3 grid-reliability · P4 energy-transition (legacy paths `/outlook`, `/volatility`, `/shedding[-plan]`, `/netzero`/`/net-zero` vẫn work) |
| **Khi backend live** | Đổi `BASE_URL` trong `ApiClient.kt` (hoặc qua màn Settings của app) |
| **Backend tag hiện tại** | `v1.0.0` (commit `3d30b39`, 13/05/2026) + post-release fixes Phase 7.6/7.7 trên `origin/main` (commit `e64d447`) — REST 13/13 endpoint `200 OK` |

> 📌 Backend đã được build + verify E2E với Docker Lite stack (5 container, ~1.6 GB RAM). 13/13 REST endpoint trả `200 OK`. Mock server trong repo này đã sync với shape mới.

---

## 👥 Team

| Vai trò | Người | Nhiệm vụ |
|---------|-------|----------|
| **Project Owner / Backend** | Leader | Cung cấp API, JWT token mẫu, review PR Android |
| **Android Developer** | Bạn | Build toàn bộ Android app (5-7 màn) |

---

## 📋 Roadmap Android (~30h, 4 tuần)

| PR | Mô tả | Time |
|----|-------|------|
| 1 | Bootstrap: Gradle + Retrofit + ApiClient + SessionManager | 3h |
| 2 | Splash + Login + persist JWT | 4h |
| 3 | Main shell + Home dashboard (4 KPI + Security Score gauge) | 5h |
| 4 | 4 Pillar tabs + MPAndroidChart | 7h |
| 5 | Alerts + Recommendations + ACK button | 5h |
| 6 | Settings + About + i18n VN/EN | 3h |
| 7 | Polish + build APK + screenshot + demo prep | 3h |

Chi tiết: `docs/ANDROID_ONBOARDING.md §10`.

---

## 📚 Tài liệu liên quan

| File | Nội dung |
|------|----------|
| **[TRY_THIS_FIRST.md](TRY_THIS_FIRST.md)** | **🚦 3-min happy path + remediation table** |
| [SETUP_CHECKLIST.md](SETUP_CHECKLIST.md) | ✅ Prereqs + AVD + first-open sequence |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | 🛠 20 lỗi phổ biến + fix copy-paste |
| [HANDOVER_CHECKLIST.md](HANDOVER_CHECKLIST.md) | 🤝 Mọi thứ đã verify trước khi bàn giao |
| [scripts/verify-environment.ps1](scripts/verify-environment.ps1) | Pre-flight check 10 mục (PowerShell) |
| [scripts/verify-environment.sh](scripts/verify-environment.sh) | Bản bash equivalent (mac/Linux/WSL) |
| **[docs/START_HERE.md](docs/START_HERE.md)** | **🧭 Tour guide 4 tuần — Day -1 → PR7 → demo** |
| [docs/DEVELOPER_ONBOARDING.md](docs/DEVELOPER_ONBOARDING.md) | Hands-on architecture walkthrough cho Android dev |
| [docs/ANDROID_ONBOARDING.md](docs/ANDROID_ONBOARDING.md) | Onboarding chi tiết bối cảnh + scope |
| [docs/API_CONTRACT.md](docs/API_CONTRACT.md) | Đặc tả 14 endpoint + JSON samples |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | MVVM structure + Gradle deps + 7 màn breakdown |
| [docs/KICKOFF_AGENDA.md](docs/KICKOFF_AGENDA.md) | Agenda meeting đầu với Leader (45') |
| [docs/QUICKSTART_FOR_USER.md](docs/QUICKSTART_FOR_USER.md) | Setup chi tiết cho người không phải dev |
| [docs/AUDIT_REPORT_ANDROID.md](docs/AUDIT_REPORT_ANDROID.md) | Pre-first-sync static audit (14 findings, 8 fixed) |
| [docs/MASS_QA_REPORT.md](docs/MASS_QA_REPORT.md) | Cross-repo QA gate (28 DTO contract reads) |
| [docs/FINAL_GATE_REPORT.md](docs/FINAL_GATE_REPORT.md) | Final handover sign-off + confidence rating |
| [docs/diagrams/excalidraw/](docs/diagrams/excalidraw/) | 5 architecture diagrams (Excalidraw + PNG export instructions) |
| [mock/README.md](mock/README.md) | Cách chạy mock backend bằng json-server |
| [examples/responses/README.md](examples/responses/README.md) | Index 14 file JSON sample |

---

## 🛠️ Tech stack (đã chốt)

```
Language      : Kotlin 1.9.21
Min SDK       : 26 (Android 8.0)
Target SDK    : 34 (Android 14)
Architecture  : MVVM single-module + Navigation Component
UI            : XML Views + Material 3 + ViewBinding
Network       : Retrofit 2.9 + OkHttp 4.12 + Moshi 1.15
Async         : Kotlin Coroutines + LiveData
Chart         : MPAndroidChart 3.1.0 (JitPack)
Storage       : SharedPreferences (TokenManager + AppConfig)
Logging       : Timber 5.0.1
Test          : JUnit 4 + Mockito-Kotlin + MockWebServer
Build         : Gradle 8.5 + AGP 8.2.0 + Kotlin DSL + version catalog
```

Chi tiết + alternative: `docs/ARCHITECTURE.md`.

---

## 📞 Liên hệ Leader

- Backend issue (API contract, JWT) → group chat
- Mock server không work → tự debug + Stack Overflow + AI tool (Cursor / ChatGPT)
- Cần token mẫu / OpenAPI spec / data mới → Leader cung cấp qua chat

---

**Môn học:** Phát triển ứng dụng di động (Android)
**Trường:** ĐH Công nghệ Thông tin — Khoa Hệ thống Thông tin
**Năm học:** 2026
