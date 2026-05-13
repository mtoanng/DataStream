# 🧭 START HERE — Lộ trình từ clone repo đến demo cuối kỳ

> 📅 Tổng thời gian: ~30-35 giờ, trải đều **4 tuần**
> 🎯 Đầu ra: APK Android demo + báo cáo môn + slide
> ⚡ Đọc file này **TRƯỚC TẤT CẢ** — nó là tour guide cho repo này.
>
> 🆕 **Synced với Java backend `v1.0.0` + Phase 7.6/7.7** (commit `e64d447`, 13/05/2026). Pillar taxonomy đã refactor IEA/APERC — xem callout dưới + `docs/API_CONTRACT.md`.

---

## 🆕 Post-v1.0.0 update (13/05/2026)

Backend Java đã release `v1.0.0` rồi tiếp tục có 2 post-release fix (Phase 7.6 + 7.7) trên `origin/main`:

- **Pillar taxonomy refactor**: P1 `outlook` → `supply-security`, P2 `volatility` → `market-resilience`, P3 `shedding[-plan]` → `grid-reliability`, P4 `netzero / net-zero` → `energy-transition`. Backend giữ legacy alias path nên code Android cũ KHÔNG break, **nhưng shape response đã đổi** sang IEA-shaped DTO mới — DTO Android phải refactor theo. Recommend code mới target canonical path.
- **Login response**: dùng `expiresInMs` (không phải `expiresIn`), không có `tokenType`, `user` thêm field `enabled`.
- **Security score**: status enum đổi sang `SECURE / ELEVATED / STRESSED / CRITICAL` (bỏ `STABLE / AT_RISK`), field `calculatedAt` → `computedAt`, không còn `trend`.
- **Cascade-risks**: deprecated, luôn trả `[]`. UI nên hide/render placeholder.
- **Acknowledge**: response `{id, newStatus, acknowledgedBy}` (không còn `acknowledgedAt`/`note`); body cho phép `{status: 'ACKNOWLEDGED' | 'DISMISSED', note?}`.
- **Path đổi**: `/api/raw/fuel-prices/latest` → `/api/fuel-prices/latest`, `/api/raw/grid-load/latest` → `/api/grid-load/latest`. Mock server vẫn route cả 2 path.
- **Endpoint mới**: `GET /api/auth/me` trả `UserDto` của session hiện tại.
- **Examples + mock đã sync**: `mock/db.json`, `mock/routes.json`, `mock/middleware.js`, và toàn bộ `examples/responses/*.json` đã được regenerate theo shape mới.

→ Đọc chi tiết: **[`docs/API_CONTRACT.md`](API_CONTRACT.md)**.

---

## 📜 Bạn đang ở đâu trong dự án?

Đồ án **Phát triển ứng dụng di động Android** của môn Mobile (CTU/UIT/...). Bạn sẽ build 1 app Android giám sát **An ninh năng lượng VN** thời gian thực, consume 14 endpoint REST API từ **backend Java có sẵn**.

```
┌──────────────────────────────────┐         ┌──────────────────────────────────┐
│      📱 ANDROID APP (bạn làm)     │  HTTPS  │   ☕ JAVA BACKEND (đã có)         │
│      ───────────────────────     │ ──────► │   ───────────────────────         │
│  • Kotlin + MVVM                 │  REST   │  • Spring Boot 2.7              │
│  • 7 màn hình                    │  + JWT  │  • Postgres + Flink + Kafka     │
│  • 7 PR / 4 tuần                 │         │  • 14 endpoint REST              │
└──────────────────────────────────┘         └──────────────────────────────────┘
                  │
                  │ (khi backend chưa chạy / khác mạng)
                  ▼
         🎭 Mock backend (json-server, trong repo này)
         port 8090, 14/14 endpoint verified PASS
```

**Quan hệ giữa 2 đồ án:**
- Đồ án Java (của Leader, độc lập): https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres
- Đồ án Android (bạn): **chính là repo này**, có repo Git riêng

Bạn KHÔNG cần đọc / chạy code Java. Chỉ cần biết backend trả gì (qua `docs/API_CONTRACT.md`) và mock backend chạy thế nào (qua `mock/README.md`).

---

## 🗺️ Bản đồ tài liệu — đọc theo thứ tự nào?

```
START_HERE.md            ◄──── bạn đang đọc, biết phải đọc gì tiếp
   │
   ├─► ANDROID_ONBOARDING.md   (15') — bối cảnh + scope + prerequisites + first-day
   │
   ├─► API_CONTRACT.md         (10') — 14 endpoint spec, bookmark khi code
   │
   ├─► ARCHITECTURE.md         (10') — MVVM structure + Gradle deps + skeleton code
   │
   ├─► KICKOFF_AGENDA.md       (5')  — agenda meeting đầu với Leader
   │
   └─► mock/README.md          (5')  — cách chạy mock backend
```

Tổng đọc lần đầu: **~45 phút**. Sau đó dùng làm reference khi cần.

---

## ⏱️ Day -1 — Trước khi bắt đầu (~30 phút)

Trước khi clone repo, đảm bảo có:

### Hardware
- [ ] Laptop ≥ 8GB RAM (Android Studio + emulator tốn ~4GB)
- [ ] Điện thoại Android cá nhân **API 24+** (Android 7.0+) — để test trên máy thật
- [ ] Cáp USB connect điện thoại với laptop

### Software (cài lần lượt)
- [ ] **Java JDK 17** — Tải Temurin: https://adoptium.net/temurin/releases/?version=17
- [ ] **Android Studio** Hedgehog 2023.1.1+ — https://developer.android.com/studio
- [ ] **Git** — https://git-scm.com/downloads (Windows: kèm Git Bash)
- [ ] **Node.js 18+** — https://nodejs.org/ (cần cho mock backend)
- [ ] **Postman** (free) — https://www.postman.com/downloads/

### Tài khoản
- [ ] **GitHub** — để clone repo + push code của bạn
- [ ] **Gmail** — login Android Studio, đồng bộ SDK

### Validation (verify mọi thứ work)

Mở terminal (PowerShell / Bash) chạy:

```bash
java --version       # phải hiện ≥ 17
git --version        # ≥ 2.30
node --version       # ≥ 18
npm --version        # ≥ 9
```

Mở Android Studio:
- File → Settings → Appearance → đảm bảo theme dễ nhìn
- Tools → SDK Manager → Install Android SDK Platform 34 + Build-Tools 34.0.0
- Tools → AVD Manager → Create Virtual Device → Pixel 5 → API 34 → Download → Finish

**Đảm bảo emulator boot được** trước khi sang bước tiếp theo. Nếu emulator chậm, enable **Hyper-V** (Windows) hoặc **Intel HAXM** (Mac/Linux).

---

## 🎯 Day 1 — Setup + smoke test (~3-4 giờ)

### Step 1.1: Clone repo (5')

```bash
cd ~/projects                        # hoặc folder bạn muốn
git clone https://github.com/mtoanng/DataStream.git
cd DataStream
```

### Step 1.2: Đọc onboarding docs (45')

Đọc theo thứ tự, không cần thuộc, biết chỗ tra cứu là OK:

1. **[`README.md`](../README.md)** (~3') — overview repo
2. **[`docs/ANDROID_ONBOARDING.md`](ANDROID_ONBOARDING.md)** (~15') — đọc kỹ phần "Stack" + "Prerequisites"
3. **[`docs/API_CONTRACT.md`](API_CONTRACT.md)** (skim ~10') — biết có 14 endpoint, auth qua JWT
4. **[`docs/ARCHITECTURE.md`](ARCHITECTURE.md)** (skim ~10') — biết folder structure khuyến nghị

### Step 1.3: Chạy mock backend (10')

```bash
cd mock

# Windows PowerShell
.\start.ps1

# Linux/macOS/WSL
bash start.sh
```

**Lần đầu chạy** sẽ download json-server (~30s). Khi thấy:

```
\{^_^}/ hi!
  Loading db.json
  Loading routes.json
  Loading middleware.js
  Done
  Resources
  ...
```

→ Mock đã sẵn sàng tại `http://localhost:8090`.

> 💡 Để cửa sổ này CHẠY LIÊN TỤC khi bạn code. Mở terminal khác để chạy các lệnh khác.

### Step 1.4: Verify mock với Postman (15')

Mở Postman, tạo collection "VES Mock" với 4 request:

| # | Method | URL | Body | Expected |
|---|--------|-----|------|----------|
| 1 | GET | `http://localhost:8090/api/health` | — | 200, `{"status":"UP"}` |
| 2 | POST | `http://localhost:8090/api/auth/login` | `{"username":"admin","password":"admin"}` | 200, có `accessToken` |
| 3 | POST | `http://localhost:8090/api/auth/login` | `{"username":"admin","password":"wrong"}` | **401**, `{"error":"Unauthorized"}` |
| 4 | GET | `http://localhost:8090/api/security/score` | — | 200, `{"overallScore":76.4,...}` |

**Nếu 4 case này pass**: mock OK, tiếp Step 1.5.
**Nếu fail**: xem mục [🆘 Troubleshooting](#-troubleshooting) bên dưới.

### Step 1.5: Probe task (~2 giờ)

> 🎯 **Mục đích**: chứng minh bạn có thể build 1 app Android tối giản gọi được API. Đây KHÔNG phải là PR đồ án — chỉ là "Hello World có Retrofit". Push lên GitHub cá nhân của bạn (KHÔNG push vào repo này), share link Leader để Leader confirm bạn đủ skill bước tiếp.

#### Yêu cầu:

1. Tạo project Android Studio mới:
   - Name: `ProbeApp`
   - Package: `com.example.probeapp`
   - Language: **Kotlin**
   - Build: **Kotlin DSL**
   - Min SDK: **API 24 (Android 7.0)**

2. Sửa `app/build.gradle.kts` thêm:
   ```kotlin
   dependencies {
       // ... existing ...
       implementation("com.squareup.retrofit2:retrofit:2.9.0")
       implementation("com.squareup.retrofit2:converter-gson:2.9.0")
       implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
   }
   ```

3. Trong `AndroidManifest.xml`, thêm:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />

   <application android:usesCleartextTraffic="true" ...>
   ```

4. Tạo 1 màn duy nhất:
   - 1 Button: "Test API"
   - 1 TextView (scrollable): hiện response

5. Khi click button → gọi `GET http://10.0.2.2:8090/api/health` qua Retrofit + Coroutines → hiển thị raw JSON response lên TextView.

   - `10.0.2.2` là alias mà Android emulator dùng để gọi `localhost` của host machine.
   - Mock backend phải đang chạy trên laptop (Step 1.3).

6. Push project lên GitHub cá nhân (repo mới `ProbeApp`), share link cho Leader.

#### Hint: skeleton code

```kotlin
// HealthService.kt
interface HealthService {
    @GET("api/health")
    suspend fun getHealth(): okhttp3.ResponseBody
}

// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8090/")
            .build()
            .create(HealthService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            lifecycleScope.launch {
                try {
                    val raw = api.getHealth().string()
                    findViewById<TextView>(R.id.tvResult).text = raw
                } catch (e: Exception) {
                    findViewById<TextView>(R.id.tvResult).text = "Error: ${e.message}"
                }
            }
        }
    }
}
```

#### Deadline & deliverable

- **Hoàn thành trong 2 ngày** (cuối Day 2, sáng Day 3)
- Push lên GitHub cá nhân repo `ProbeApp` (PUBLIC, KHÔNG push vào repo DataStream)
- Share link với Leader qua chat
- Nếu không xong cũng share progress + chỗ stuck → Leader sẽ giúp

### ✅ Day 1 done checklist

- [ ] Setup môi trường (Java, Android Studio, Node, Git) ✓ Day -1
- [ ] Clone repo DataStream ✓
- [ ] Đọc README + 3 doc chính (~45')
- [ ] Mock backend chạy được, 4 case Postman pass
- [ ] Probe task tạo + bắt đầu code

---

## 🎯 Day 2-3 — Hoàn tất probe task + chuẩn bị kickoff (~3-4 giờ)

### Step 2.1: Hoàn tất probe task

- Click button → hiện `{"status":"UP","db":"UP","timestamp":"2026-..."}`?
- Test trên cả emulator + điện thoại thật?
- Push lên GitHub, share link Leader?

### Step 2.2: Chuẩn bị kickoff meeting

Đọc kỹ [`docs/KICKOFF_AGENDA.md`](KICKOFF_AGENDA.md). Chuẩn bị trước:

- [ ] Mockup 2-3 màn chính (Figma free / draw.io / Paint cũng OK)
  - Login screen
  - Home dashboard (Security Score + 4 KPI)
  - 1 Pillar tab (vd Pillar 3 với load chart)
- [ ] Note 5-10 câu hỏi muốn hỏi Leader
- [ ] Confirm với Leader giờ meet (45')

### Step 2.3: Meeting kickoff với Leader (~45')

Theo agenda trong `KICKOFF_AGENDA.md`. Đầu ra:

- ✅ Chốt stack (Kotlin / XML / Material 3 / MVVM — recommend trong `ARCHITECTURE.md`)
- ✅ Roadmap 7 PR + deadline từng PR
- ✅ Add bạn là collaborator của repo `DataStream`
- ✅ Daily standup time
- ✅ JWT token mẫu nếu cần

---

## 🎯 Tuần 1 — PR1 + PR2 (~7 giờ)

### PR1: Bootstrap (~3 giờ)

> Khởi tạo Android project chính thức trong folder `app/` của repo `DataStream`.

#### Tasks
- [ ] Tạo project Android Studio:
  - **Location**: trong `<DataStream-root>/app/` (project ở root level)
  - **Name**: `VES Monitor Mobile`
  - **Package**: `vn.edu.ves.mobile`
  - **Min SDK**: 24
  - **Language**: Kotlin
  - **Build**: Kotlin DSL
- [ ] Copy `app/build.gradle.kts` deps từ [`docs/ARCHITECTURE.md §3`](ARCHITECTURE.md)
- [ ] Copy `settings.gradle.kts` (có JitPack repo cho MPAndroidChart)
- [ ] Tạo folder structure:
  ```
  app/src/main/kotlin/vn/edu/ves/mobile/
  ├── data/api/        — Retrofit setup
  ├── data/dto/        — DTOs (rỗng tạm thời)
  ├── data/prefs/      — SessionManager
  ├── data/repo/       — Repositories (rỗng tạm thời)
  ├── ui/              — Activities/Fragments (rỗng tạm thời)
  └── util/            — Result.kt, Extensions.kt
  ```
- [ ] Tạo `ApiClient.kt` + `ApiService.kt` (skeleton từ `ARCHITECTURE.md §4.1-4.2`)
- [ ] Tạo `Result.kt` (sealed class Loading/Success/Error)
- [ ] Tạo `SessionManager.kt` (SharedPreferences wrapper)
- [ ] App build + run được (empty screen OK)
- [ ] Commit + PR lên branch `feature/01-bootstrap`

#### Acceptance criteria
- App launches without crash
- `./gradlew assembleDebug` thành công
- README có thêm dòng "Build: `./gradlew assembleDebug`"

### PR2: Splash + Login (~4 giờ)

> Màn đầu tiên người dùng thấy. Sau khi login thành công → lưu JWT vào SharedPreferences → navigate Main.

#### Tasks
- [ ] Tạo `SplashActivity` (logo + 1.5s delay → navigate Login)
- [ ] Tạo `LoginActivity` + `LoginViewModel`:
  - 2 input field (username, password)
  - Button Login
  - Progress bar khi loading
  - Snackbar khi error
- [ ] DTO: `LoginRequest`, `LoginResponse`, `UserDto` (xem [`examples/responses/01_login_200.json`](../examples/responses/01_login_200.json))
- [ ] `AuthRepository.login()` gọi `api.login(req)`, save token nếu thành công
- [ ] Sau login thành công → finish LoginActivity + start MainActivity (stub Activity rỗng cũng được)
- [ ] Test 3 case:
  - admin/admin → vào Main
  - admin/wrong → snackbar "Sai mật khẩu"
  - empty fields → snackbar "Vui lòng nhập đầy đủ"
- [ ] Commit + PR `feature/02-login`

#### Acceptance criteria
- Click Login với admin/admin → main screen + JWT lưu SharedPreferences
- Kill app + reopen → vẫn nhận token (chưa cần navigate ngay, sẽ làm PR3)

---

## 🎯 Tuần 2 — PR3 + PR4 (~12 giờ)

### PR3: Main shell + Home (~5 giờ)

- [ ] `MainActivity` host BottomNavigationView 5 tab (Home, P1, P2, P3, P4) — chưa cần tab khác, tạm hide
- [ ] `HomeFragment` + `HomeViewModel` gọi `GET /api/security/score`
- [ ] UI: 4 KPI card (1 cho mỗi pillar score) + 1 Security Score gauge ở giữa
- [ ] Pull to refresh
- [ ] Auto-load mỗi khi vào tab Home
- [ ] Khi token hết hạn (401) → clear SharedPreferences + navigate Login

### PR4: 4 Pillar tabs (~7 giờ)

> Tab nặng nhất, có chart. Làm 1 pillar trước, sau đó duplicate cho 3 cái còn lại.

- [ ] DTOs cho 4 pillar (xem `examples/responses/04-07`)
- [ ] `PillarRepository` với 4 method
- [ ] `Pillar1Fragment` + `Pillar1ViewModel`:
  - RecyclerView danh sách region → fuel inventory
  - 1 chart MPAndroidChart (bar chart show stockDays vs target)
- [ ] Duplicate cho Pillar 2 (line chart price trend), Pillar 3 (gauge load%), Pillar 4 (pie chart renewable share)

> 💡 **Tip**: dùng `examples/responses/` để hard-code data preview trong layout XML preview, KHÔNG đợi networkmỗi lần.

---

## 🎯 Tuần 3 — PR5 + PR6 (~8 giờ)

### PR5: Alerts + Recommendations + Ack (~5 giờ)

- [ ] `AlertListFragment` — RecyclerView 4 alert mẫu với badge severity color
- [ ] `RecListFragment` — RecyclerView 4 recommendation với button "Acknowledge"
- [ ] Click button → AlertDialog hỏi note → POST `/api/recommendations/{id}/acknowledge`
- [ ] Sau ack thành công → refresh list

### PR6: Settings + About + i18n (~3 giờ)

- [ ] `SettingsActivity`:
  - Input đổi BASE_URL (default `http://10.0.2.2:8090`)
  - Toggle Dark Mode
  - Button Logout (clear SharedPreferences + back to Login)
- [ ] `AboutActivity`: tên app, version, tên team, link repo
- [ ] `values-en/strings.xml` — bản tiếng Anh fallback
- [ ] Test đổi BASE_URL sang URL khác → app gọi đúng URL mới

---

## 🎯 Tuần 4 — PR7 + demo prep (~5 giờ)

### PR7: Polish + APK + demo (~3 giờ)

- [ ] Fix lint warnings (chỉ những cái critical)
- [ ] Test full flow trên emulator + điện thoại thật
- [ ] Build release APK: `./gradlew assembleRelease`
- [ ] Upload APK lên GitHub Release của repo `DataStream`
- [ ] Chụp 8-10 screenshot UI → folder `docs/screenshots/`
- [ ] Quay video demo 3-5 phút (Loom / OBS / Screen recorder điện thoại)

### Demo prep (~2 giờ)

- [ ] Dress rehearsal 1 lần với Leader (test backend live nếu có, hoặc mock)
- [ ] Slide / poster nếu môn yêu cầu
- [ ] Báo cáo môn Android theo template trường (Leader có template hoặc tự tìm)
- [ ] Backup plan: nếu backend down lúc demo → fallback mock

---

## 🆘 Troubleshooting

### Mock backend

**"npm error code UNABLE_TO_VERIFY_LEAF_SIGNATURE"** hoặc **"E401"**
→ Bạn trên máy có corporate proxy. `mock/.npmrc` đã ép public registry — nhưng nếu vẫn fail, edit `mock/.npmrc` thêm:
```
strict-ssl=false
registry=https://registry.npmjs.org/
```
Hoặc tắt VPN/proxy 2 phút để install xong, sau đó bật lại.

**"Port 8090 already in use"**
→ Process khác đang chiếm port. Đổi port:
```bash
PORT=8091 bash start.sh         # Linux
# hoặc edit start.ps1 đổi $Port = 8091
```
Nhớ đổi `BASE_URL` trong app theo.

**"Cannot find db.json"**
→ Chạy `start.sh` từ thư mục `mock/`, KHÔNG phải từ root repo.

### Android Studio

**Gradle sync fail "Could not resolve com.github.PhilJay:MPAndroidChart"**
→ Chưa add JitPack repo. Sửa `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")   // ← add this
    }
}
```

**Emulator chạy chậm**
→ Enable Hyper-V (Windows) hoặc HAXM (Mac). Hoặc dùng điện thoại thật qua USB Debugging.

**App build OK nhưng "Connection refused" khi click button**
→ Mock backend đã chạy chưa? Test bằng:
- PC: `curl http://localhost:8090/api/health`
- Emulator: dùng `http://10.0.2.2:8090` (KHÔNG phải `localhost`)
- Điện thoại thật: dùng IP LAN của laptop (`http://192.168.x.x:8090`) + cùng WiFi

**"CLEARTEXT communication not permitted"**
→ Thiếu trong `AndroidManifest.xml`:
```xml
<application android:usesCleartextTraffic="true" ...>
```

### Git / GitHub

**"Permission denied push"**
→ Leader chưa add bạn collaborator. Ping Leader hoặc fork repo + tạo PR từ fork.

**Merge conflict**
→ `git pull origin main` trước khi push. Nếu conflict, mở file, sửa, `git add`, `git commit`, `git push`.

---

## 📞 Khi cần help

| Vấn đề | Hỏi ai | Cách hỏi |
|--------|--------|----------|
| Backend / API trả khác doc | **Leader** | Screenshot Postman response + endpoint URL |
| Mock không chạy | **Leader** (lần đầu), sau đó tự debug | Log đầy đủ + step đã làm |
| Android Studio crash, build fail | **Stack Overflow / ChatGPT** trước | Copy full error log |
| Logic UI / animation | **ChatGPT / Cursor** | Mô tả flow + mockup |
| Git, branch, conflict | **Leader** lần đầu | Nói rõ branch + lệnh đã chạy |

**Daily standup template** (gửi vào chat mỗi tối):

```
[Ngày YYYY-MM-DD]
✅ Done:
  - <task hoàn thành hôm nay>
🚧 Doing:
  - <task đang làm>
🚨 Blocked:
  - <nếu có, mô tả ngắn + đã thử gì>
```

---

## ✅ Tổng checklist từ đầu đến cuối

### Pre-work
- [ ] Cài đầy đủ tool (Java/AS/Node/Git/Postman) ✓ Day -1
- [ ] Đọc 4 doc (START_HERE + ANDROID_ONBOARDING + API_CONTRACT + ARCHITECTURE)
- [ ] Mock backend chạy được + 4 Postman case pass
- [ ] Probe task xong → push GitHub cá nhân → Leader confirm

### Coding (7 PR)
- [ ] PR1: Bootstrap (project skeleton, Retrofit setup)
- [ ] PR2: Splash + Login
- [ ] PR3: Main shell + Home dashboard
- [ ] PR4: 4 Pillar tabs với chart
- [ ] PR5: Alerts + Recommendations + Acknowledge
- [ ] PR6: Settings + About + i18n
- [ ] PR7: Polish + APK + screenshot + video

### Demo
- [ ] APK release upload GitHub
- [ ] 8-10 screenshot UI
- [ ] Video demo 3-5'
- [ ] Báo cáo môn theo template
- [ ] Dress rehearsal với Leader
- [ ] Demo cuối kỳ ✨

---

## 🎓 Kỹ năng bạn cần (self-assessment trước khi start)

Đánh dấu mức của bạn hôm nay:

| Kỹ năng | 🟢 Pro | 🟡 OK | 🔴 Cần học |
|---------|--------|-------|------------|
| Kotlin basic (`val`, `data class`, lambda, `when`) | | | |
| Android Activity/Fragment lifecycle | | | |
| XML layout (ConstraintLayout, RecyclerView) | | | |
| ViewModel + LiveData | | | |
| Kotlin Coroutines (`suspend`, `viewModelScope`) | | | |
| Retrofit + Gson | | | |
| Material 3 (Card, BottomNav, Snackbar) | | | |
| Git (clone/branch/commit/push/PR) | | | |
| Android Studio (run emulator, đọc Logcat) | | | |
| MPAndroidChart | | | |

**Quy tắc**:
- ≥ 7 🟢 mục → đủ làm, full speed
- 4-6 🟢 mục → đủ, cần học thêm trong tuần 1 (link sẵn trong `ANDROID_ONBOARDING.md §15`)
- < 4 🟢 mục → báo Leader, có thể scope cut hoặc Leader pair-program nhiều hơn

---

## 🚀 Bắt đầu ngay

```bash
git clone https://github.com/mtoanng/DataStream.git
cd DataStream
code .                              # mở VS Code đọc docs trước
cd mock && bash start.sh           # khởi động mock backend
```

Hello, future Android Developer 👋. **Welcome to the team!**

Khi gặp khó khăn ở bất kỳ step nào — **đừng silent**. Báo Leader ngay, đỡ tốn thời gian.

→ Tiếp theo: mở [`ANDROID_ONBOARDING.md`](ANDROID_ONBOARDING.md) để hiểu sâu hơn về bối cảnh.
