# 📱 DataStream — VES-Monitor Mobile (Android)

> Đồ án môn **Phát triển ứng dụng di động (Android)** — ứng dụng giám sát **An ninh năng lượng Việt Nam** thời gian thực.
> Repo này **độc lập** với đồ án Java (backend Spring Boot + Flink + Postgres). Mobile app chỉ consume **14 endpoint REST API** mà backend đó expose.

[![Status](https://img.shields.io/badge/status-Bootstrapping-blue)]() [![Platform](https://img.shields.io/badge/platform-Android%207.0%2B-green)]() [![Language](https://img.shields.io/badge/language-Kotlin-purple)]()

---

## 👋 Bắt đầu từ đâu?

> 🆕 **Lần đầu vào repo này?** → Mở **[`docs/START_HERE.md`](docs/START_HERE.md)** trước hết. File đó là tour guide từ Day -1 đến PR cuối, có checklist + troubleshooting + skill self-assessment.
>
> Sau khi đọc START_HERE, đọc tiếp 3 file theo thứ tự (tổng ~30 phút):

| # | File | Mô tả |
|---|------|-------|
| 🧭 | **[`docs/START_HERE.md`](docs/START_HERE.md)** | **Tour guide 4 tuần — đọc TRƯỚC TẤT CẢ** |
| 1 | **[`docs/ANDROID_ONBOARDING.md`](docs/ANDROID_ONBOARDING.md)** | Briefing đầy đủ: bối cảnh, scope, stack khuyến nghị, prerequisites, first-day setup, kickoff agenda |
| 2 | **[`docs/API_CONTRACT.md`](docs/API_CONTRACT.md)** | Đặc tả 14 endpoint + sample request/response — bookmark khi code |
| 3 | **[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)** | Khuyến nghị MVVM single-module + file structure + Gradle dependencies |

---

## 🏗️ Cấu trúc repo

```
DataStream/
├── README.md                    # 👈 Bạn đang đọc
├── .gitignore                   # Android Studio gitignore
│
├── docs/
│   ├── START_HERE.md           # 🧭 Tour guide 4 tuần (đọc đầu tiên)
│   ├── ANDROID_ONBOARDING.md   # 📖 Briefing đầy đủ
│   ├── API_CONTRACT.md         # 📋 Đặc tả 14 endpoint
│   ├── ARCHITECTURE.md         # 🏛️ MVVM + file structure + Gradle deps
│   └── KICKOFF_AGENDA.md       # 🤝 Agenda meeting đầu với Leader
│
├── mock/                        # 🎭 Mock backend (json-server)
│   ├── db.json                 # Dữ liệu mẫu 14 endpoint
│   ├── routes.json             # Route mapping
│   ├── start.sh / start.ps1    # Script chạy mock (port 8090)
│   └── README.md               # Hướng dẫn chạy mock
│
└── examples/responses/          # 📦 14 JSON file sample response
    ├── 01_login_200.json
    ├── 02_security_score_200.json
    ├── ...
    └── README.md
```

Khi Android Dev start code, sẽ tạo thêm folder `app/` ở root chứa project Android Studio.

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
# → {"status":"UP","db":"UP",...}

# 2. Login với seed user
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
# → {"accessToken":"...","user":{"role":"ADMIN",...}}

# 3. Login sai password → 401
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrong"}'
# → 401 {"status":401,"error":"Unauthorized",...}

# 4. Acknowledge khuyến nghị
curl -X POST http://localhost:8090/api/recommendations/42/acknowledge \
  -H "Content-Type: application/json" \
  -d '{"note":"OK done"}'
# → {"id":42,"status":"ACKNOWLEDGED","note":"OK done",...}
```

### 3. Mở Android Studio

Khi bắt đầu code (theo `docs/ANDROID_ONBOARDING.md §7`):

```
Android Studio → New Project → Empty Activity
  Name        : VES Monitor Mobile
  Package     : vn.edu.ves.mobile
  Language    : Kotlin
  Min SDK     : API 24 (Android 7.0)
  Build       : Kotlin DSL
  Location    : <repo-root>/app/
```

Sync Gradle → run trên emulator.

### 4. Trỏ app vào mock URL

Trong `app/src/main/java/.../data/api/ApiClient.kt`:

```kotlin
const val BASE_URL = "http://10.0.2.2:8090"  // emulator → host loopback
```

---

## 🔗 Liên kết với đồ án Java (backend)

| Thông tin | Giá trị |
|-----------|---------|
| **Repo backend** | https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres |
| **Backend stack** | Spring Boot 2.7 + JWT + JdbcTemplate + Postgres 15 |
| **Backend port** | 8090 (mặc định) |
| **API spec** | 14 endpoint REST, JWT Bearer auth, OpenAPI 3.0 |
| **Khi backend live** | Đổi `BASE_URL` trong `ApiClient.kt` (hoặc qua màn Settings của app) |

> 📌 Backend hiện đang ở Phase 4.5 — **code-complete nhưng build pending** (Bosch NTLM proxy block Maven repo). Sẽ build trên hotspot 4G hoặc CI. Trong khi chờ, **dùng mock server trong repo này** để dev song song.

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
| **[docs/START_HERE.md](docs/START_HERE.md)** | **🧭 Tour guide 4 tuần — Day -1 → PR7 → demo** |
| [docs/ANDROID_ONBOARDING.md](docs/ANDROID_ONBOARDING.md) | Onboarding chi tiết bối cảnh + scope |
| [docs/API_CONTRACT.md](docs/API_CONTRACT.md) | Đặc tả 14 endpoint + JSON samples |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | MVVM structure + Gradle deps + 7 màn breakdown |
| [docs/KICKOFF_AGENDA.md](docs/KICKOFF_AGENDA.md) | Agenda meeting đầu với Leader (45') |
| [mock/README.md](mock/README.md) | Cách chạy mock backend bằng json-server |
| [examples/responses/README.md](examples/responses/README.md) | Index 14 file JSON sample |

---

## 🛠️ Tech stack (khuyến nghị)

```
Language      : Kotlin 1.9+
Min SDK       : 24 (Android 7.0)
Target SDK    : 34 (Android 14)
Architecture  : MVVM single-module
UI            : XML + Material 3 + ViewBinding
Network       : Retrofit 2 + OkHttp + Gson
Async         : Kotlin Coroutines
Chart         : MPAndroidChart
Storage       : SharedPreferences (JWT + settings)
Logging       : Timber
Test          : JUnit 4 + Mockito
Build         : Gradle KTS
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
