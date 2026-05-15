# 🚀 QUICKSTART_FOR_USER — Chạy app VES-Monitor từ con số 0

> **Đối tượng**: Người không phải Android developer, muốn clone repo trên máy Windows mới và thấy app chạy trong emulator.
>
> **Thời gian setup lần đầu**: ~30–60 phút (chủ yếu là cài Android Studio + tải SDK + boot emulator).
>
> **Đã có Android Studio và SDK?** Nhảy thẳng tới [§3 Clone & open](#section-3--clone-và-mở-project-5-phút).

Hướng dẫn này song hành với 2 file ngắn hơn ở repo root:
- [`README.md`](../README.md) — tổng quan repo
- [`SETUP_CHECKLIST.md`](../SETUP_CHECKLIST.md) — checklist 1 trang
- [`TROUBLESHOOTING.md`](../TROUBLESHOOTING.md) — 20 lỗi thường gặp + fix

---

## Section 1 — Tổng quan (2 phút đọc)

### App là gì?

**VES-Monitor Mobile** là ứng dụng Android giám sát **An ninh năng lượng Việt Nam (Vietnam Energy Security)** theo thời gian thực. App lấy dữ liệu từ backend Spring Boot (Kafka + Flink + Postgres) và hiển thị:

| Màn hình | Nội dung chính |
|----------|----------------|
| **Home** | Đồng hồ ESI tròn (0–100) + 4 thẻ nhỏ cho 4 trụ cột |
| **Pillars** | 4 tab chi tiết: Supply Security / Market Resilience / Grid Reliability / Energy Transition |
| **Alerts** | Danh sách cảnh báo realtime, filter theo severity (CRITICAL / WARNING / INFO) |
| **Recommendations** | Khuyến nghị từ backend + nút **Acknowledge** (xác nhận đã đọc) |
| **Settings** | Đổi server URL, test connectivity, logout |

### Cần gì để chạy?

| Thành phần | Bắt buộc? | Ghi chú |
|------------|-----------|---------|
| **Android Studio Hedgehog 2023.1** (hoặc mới hơn) | ✅ Có | Đã bundle sẵn JDK 17 + Gradle |
| **Android SDK Platform 34** | ✅ Có | Cài qua **SDK Manager** trong AS |
| **Emulator** (Pixel 5 / API 34) | ✅ Có | Hoặc dùng điện thoại Android thật (Android 8.0+) |
| **Git for Windows** | ✅ Có | Để `git clone` |
| **Backend HOẶC mock server** | ✅ Có (1 trong 2) | Mock server là lựa chọn dễ nhất |
| **Node.js 18 LTS** | ⚪ Chỉ nếu chạy mock | Cài từ https://nodejs.org/ |
| **Docker Desktop** | ⚪ Chỉ nếu chạy backend Java thật | Cần ~4 GB RAM trống |

### Thời gian setup lần đầu

| Bước | Thời gian |
|------|-----------|
| Tải installer Android Studio (~1.2 GB) | 5–15 phút (tuỳ mạng) |
| Cài đặt Android Studio | 5–10 phút |
| Tải SDK Platform 34 + tools (~500 MB) | 5–10 phút |
| Tạo & boot emulator Pixel 5 API 34 lần đầu | 3–5 phút |
| Clone repo + Gradle sync lần đầu (~150 MB) | 5–10 phút |
| Build APK + install lần đầu | 1–2 phút |
| **Tổng (mạng tốt, máy nhanh)** | **~30 phút** |
| **Tổng (mạng chậm/proxy công ty)** | **~60–90 phút** |

> 💡 **Tip**: Tải Android Studio **trong nền** trong khi đọc tiếp các section bên dưới.

---

## Section 2 — Cài đặt phần mềm (15–30 phút)

### 2.1. Tải Android Studio Hedgehog 2023.1.x (hoặc mới hơn)

**Link download chính thức**: https://developer.android.com/studio

- File installer Windows: `android-studio-2023.1.x.xx-windows.exe` (~1.2 GB)
- Hoặc cùng phiên bản trở lên: **Iguana 2023.2**, **Jellyfish 2024.1+**

> ⚠️ **Warning**: Đừng dùng AS **cũ hơn Hedgehog** (Flamingo / Giraffe). Project này dùng **AGP 8.2.0** mà các phiên bản trước không hỗ trợ.

### 2.2. Cài đặt Android Studio

1. Chạy installer → **Next** → **Next** → **Install** → **Finish**.
   - Để mặc định mọi thứ. Cài vào `C:\Program Files\Android\Android Studio\`.
   - Khi hỏi "Start Android Studio" cuối installer → tick chọn → **Finish**.
2. **Pop-up "Import Settings"**: chọn **Do not import settings** → OK.
3. **Pop-up "Android Studio Setup Wizard"**:
   - Welcome → **Next**
   - **Install Type** → chọn **Standard** → **Next**
   - **UI Theme** → tuỳ thích (Darcula / Light)
   - **Verify Settings** → **Next** → **Finish**
4. AS sẽ tự tải Android SDK + Platform 34 + AVD mẫu (~2 GB, mất 5–15 phút). Để nó chạy nền.

> 💡 **Tip**: Nếu AS bị treo ở bước tải SDK, kiểm tra Internet + xem [§ Khắc phục case 1](#case-1--gradle-sync-fail-could-not-find-comgithubphiljaympandroidchart) ở cuối file này.

### 2.3. Cài SDK Platform 34 (nếu setup wizard chưa cài)

Sau khi setup wizard xong, mở **Welcome to Android Studio** → click **More Actions** → **SDK Manager**:

| Tab | Action |
|-----|--------|
| **SDK Platforms** | Tick ✅ **Android 14.0 (API 34)** → **Apply** → đợi tải |
| **SDK Tools** | Đảm bảo các mục sau đã tick:<br>• Android SDK Build-Tools 34<br>• Android SDK Platform-Tools<br>• Android Emulator<br>• Android SDK Command-line Tools (latest) |

Cuối cùng click **OK** để đóng SDK Manager.

### 2.4. Tạo Virtual Device (Emulator)

1. **Welcome to Android Studio** → **More Actions** → **Virtual Device Manager** (hoặc trong AS đã mở project: Tools → Device Manager).
2. Click **Create Device** (góc trên trái).
3. **Category → Phone** → chọn **Pixel 5** → **Next**.
4. **System Image** → tab **Recommended** → tìm dòng:
   - Release Name: **UpsideDownCake**
   - API Level: **34**
   - Target: **Android 14.0 (Google APIs)**
   - ABI: **x86_64**
   - Click **Download** bên cạnh dòng đó (~1 GB, mất 3–10 phút).
5. Sau khi tải xong → click **Next**.
6. **AVD Name**: để mặc định `Pixel 5 API 34` → **Finish**.
7. Trong danh sách Virtual Device, bạn sẽ thấy entry **Pixel 5 API 34**. Nút **▶** màu xanh ở bên phải dùng để boot emulator (chưa cần bấm lúc này).

> ⚠️ **Warning**: Nếu CPU bạn không hỗ trợ **HAXM** hoặc **Hyper-V**, emulator sẽ chạy rất chậm. Bật **Windows Hypervisor Platform** trong **Turn Windows features on or off** (Control Panel) rồi reboot.

### 2.5. (Optional) Tải JDK 17 độc lập

**Android Studio đã bundle sẵn JDK 17** trong `C:\Program Files\Android\Android Studio\jbr\`. Chỉ cần JDK độc lập **nếu**:

- Bạn muốn chạy `.\gradlew` từ PowerShell ngoài Android Studio.
- Hoặc bạn có project Java khác cần JDK.

Tải từ: https://adoptium.net/ (chọn **Temurin 17 LTS**, Windows x64 .msi).

Sau khi cài, mở **PowerShell mới**:

```powershell
java -version
# Mong đợi: openjdk version "17.x.x" 2024-xx-xx
```

---

## Section 3 — Clone và mở project (5 phút)

### 3.1. Cài Git for Windows (nếu chưa có)

Tải từ: https://git-scm.com/download/win → cài bằng installer mặc định (Next, Next, …, Finish).

Test trong PowerShell:

```powershell
git --version
# Mong đợi: git version 2.x.x.windows.x
```

### 3.2. Clone repo

Mở **PowerShell** (Start → gõ "PowerShell"):

```powershell
cd C:\Users\$env:USERNAME
git clone https://github.com/mtoanng/DataStream.git
cd DataStream
```

Sau khi clone, repo nằm ở `C:\Users\<your-username>\DataStream\`.

> 💡 **Tip**: Đừng clone vào `C:\Program Files\` hoặc `OneDrive\` (sync ngầm có thể làm Gradle build fail). Dùng đường dẫn ngắn không có dấu cách như `C:\Users\<your-username>\DataStream` là an toàn nhất.

### 3.3. Mở trong Android Studio

1. Mở **Android Studio** → **Welcome** screen → click **Open**.
2. Trỏ tới `C:\Users\<your-username>\DataStream` → **OK**.
3. **Pop-up "Trust and Open Project?"** → click **Trust Project**.
4. AS có thể hiện thêm: **"Use Gradle from: 'gradle-wrapper.properties' file"** → để mặc định → **OK**.

AS sẽ tự nhận diện đây là Android project (vì có `build.gradle.kts` ở root + thư mục `app/`).

---

## Section 4 — Gradle sync lần đầu (5–15 phút)

Sau khi AS mở project, **status bar ở dưới cùng** sẽ hiển thị:

```
Gradle: Build Model ... → Gradle: Configure project :app ... → Gradle: Resolve dependencies ...
```

**AS đang làm những gì?**

1. Đọc `settings.gradle.kts` + `gradle/wrapper/gradle-wrapper.properties` → biết dùng **Gradle 8.5**.
2. Tải Gradle 8.5 distribution (~150 MB, lần đầu only) vào `C:\Users\<you>\.gradle\wrapper\dists\`.
3. Đọc `gradle/libs.versions.toml` + `app/build.gradle.kts` → resolve toàn bộ dependency tree (Retrofit, Moshi, Material, MPAndroidChart, …) từ **Maven Central**, **Google Maven**, **JitPack**.
4. Tải ~150 MB dependencies về `C:\Users\<you>\.gradle\caches\`.
5. Tự generate `gradle/wrapper/gradle-wrapper.jar` nếu thiếu (repo này đã commit sẵn nên thường không generate lại).
6. Tự tạo file `local.properties` với:
   ```
   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
   ```

### Kết quả mong đợi

✅ Status bar phía dưới chuyển sang **xanh** với thông báo:

```
Gradle sync finished in 4 min 23 sec
```

✅ Cây project (Project tool window bên trái) hiện **biểu tượng Android-head xanh** bên cạnh module `app/`.

✅ Cấu trúc thư mục thấy được:

```
DataStream/
├── app/              ← module chính
│   ├── src/main/java/com/mtoanng/datastream/
│   ├── src/main/res/
│   └── src/test/java/com/mtoanng/datastream/
├── gradle/
└── mock/
```

### Nếu Gradle sync **fail**

❌ Status bar hiện đỏ + thông báo lỗi → đọc tab **Build → Build Output** (View → Tool Windows → Build).

**Dòng đỏ ĐẦU TIÊN** trong Build Output thường là root cause. Tra cứu nó trong [`TROUBLESHOOTING.md`](../TROUBLESHOOTING.md). Các case thường gặp:

| Triệu chứng | Section trong TROUBLESHOOTING |
|-------------|-------------------------------|
| `Could not resolve com.github.PhilJay:MPAndroidChart` | §1 — JitPack repo |
| `Plugin 'com.android.application' not found` | §2 — wrapper version mismatch |
| `Unsupported class file major version 65` | §3 — JDK 21 vs Gradle |
| `Could not find or load main class … GradleWrapperMain` | §4 — wrapper jar |
| `Could not GET 'https://…'` timeout | §16 — corporate proxy |
| `Java heap space` | §15 — bump `org.gradle.jvmargs` |

> 💡 **Tip**: 30% các "ghost error" của AS sẽ tự biến mất sau khi làm **File → Invalidate Caches → Restart**. Đây luôn là phương án thử đầu tiên trước khi đào sâu.

---

## Section 5 — Khởi động backend HOẶC mock server (5 phút)

App **không tự chứa dữ liệu** — nó gọi REST API từ backend. Bạn có **2 lựa chọn**:

### 5.1. Lựa chọn A — Mock server (KHUYẾN NGHỊ cho lần đầu)

Mock server dùng `json-server` (Node.js) — đọc file `mock/db.json` rồi serve REST API tại `http://localhost:8090`. Phù hợp cho test UI mà không cần dựng full stack.

**Bước 1**: Cài Node.js 18 LTS từ https://nodejs.org/ (chọn "LTS" → installer Windows). Test:

```powershell
node --version
# Mong đợi: v18.x.x hoặc v20.x.x
npm --version
# Mong đợi: 9.x.x hoặc 10.x.x
```

**Bước 2**: Trong PowerShell, mở folder `mock/` và start:

```powershell
cd C:\Users\$env:USERNAME\DataStream\mock
npm install            # lần đầu only, cài json-server (~20 MB)
.\start.ps1            # bind 0.0.0.0:8090 (LAN accessible)
```

Hoặc chế độ localhost only (không cho thiết bị LAN truy cập):

```powershell
.\start.ps1 local
```

Hoặc dùng cmd / npm trực tiếp:

```cmd
cd C:\Users\<user>\DataStream\mock
npm install
npm start
```

**Kết quả mong đợi**:

```
================================================
VES Monitor Mock Backend
================================================
Node      : v20.x.x
Port      : 8090
URL local : http://localhost:8090
URL LAN   : http://192.168.1.x:8090
Emulator  : http://10.0.2.2:8090 (loopback to host)

Endpoints (Phase 7.6 IEA/APERC + legacy aliases):
  POST /api/auth/login    GET /api/auth/me
  ...
```

**Cửa sổ PowerShell này phải mở cả khi chạy app** — đóng = mock server tắt.

**Smoke test** từ PowerShell **khác** (cửa sổ thứ 2):

```powershell
Invoke-RestMethod http://localhost:8090/api/health
# Mong đợi: status=UP, db=UP
```

> ⚠️ **Warning**: Lần đầu chạy `.\start.ps1`, Windows Firewall sẽ hỏi **"Allow Node.js to communicate on private/public networks"** → tick cả 2 → **Allow access**.

### 5.2. Lựa chọn B — Backend Java thật (nâng cao)

Cần Docker Desktop + ~4 GB RAM trống. Repo backend nằm ở:

👉 https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres

Tóm tắt (chi tiết xem README repo đó):

```powershell
cd C:\Users\$env:USERNAME
git clone https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres.git
cd Real-time-processing-with-Kafka-Flink-Postgres
docker compose -f docker-compose.lite.yml up -d
# Đợi 60–90s cho Postgres + Kafka + backend khởi động
curl http://localhost:8090/api/health
```

> 💡 **Tip**: Đối với demo / handover lần đầu, **Lựa chọn A (mock server) đủ rồi**. Backend Java là khi bạn muốn xem realtime data thật từ pipeline Kafka → Flink → Postgres.

---

## Section 6 — Chạy app trên emulator (2 phút)

1. Trong Android Studio, **toolbar trên cùng** có dropdown **"Running Devices"** (cạnh nút ▶ Run). Click → chọn **Pixel 5 API 34**.
   - Nếu emulator chưa boot, AS sẽ tự boot. Mất 30–60s lần đầu, ~10s sau (Quick boot snapshot).
2. Khi emulator boot xong (thấy màn hình home Android), click **▶ Run 'app'** (hoặc phím tắt **Shift+F10**).
3. AS sẽ:
   1. **Build APK** — ~30–60s lần đầu, ~5–10s các lần sau (incremental).
   2. **Install APK** vào emulator — ~5s.
   3. **Launch `LoginActivity`** — màn hình login hiện ra.

**Kết quả mong đợi**: Trong cửa sổ emulator, bạn thấy **màn hình login** với:
- Icon ổ khoá
- Tiêu đề "VES Monitor"
- 2 ô input: Username, Password (pre-filled `admin` / `admin`)
- Nút **Sign In** màu xanh
- Link **Configure Server** ở dưới (default URL = `http://10.0.2.2:8090/`)

> ⚠️ **Warning**: Nếu màn hình login **crash ngay khi vừa launch** với `NullPointerException: networkSecurityConfig`, xem [TROUBLESHOOTING §20](../TROUBLESHOOTING.md#20-app-crashes-on-launch-with-nullpointerexception-networksecurityconfig).

---

## Section 7 — Đăng nhập và test (3 phút)

### 7.1. Đăng nhập

1. **Username**: `admin` (đã pre-filled)
2. **Password**: `admin` (đã pre-filled)
3. Click **Sign In**

App sẽ gọi `POST /api/auth/login` tới `http://10.0.2.2:8090/api/auth/login` (= `http://localhost:8090/api/auth/login` trên máy host nhờ emulator loopback `10.0.2.2`).

### 7.2. Kết quả mong đợi

**Đăng nhập thành công** → màn `MainActivity` mở với:
- **Bottom navigation** có 5 tab: Home / Pillars / Alerts / Recommendations / Settings.
- Tab **Home** mặc định active, hiển thị:
  - **Đồng hồ ESI tròn** ở giữa (số 0–100, màu theo status).
  - Trạng thái text: SECURE / ELEVATED / STRESSED / CRITICAL.
  - Timestamp "Updated: 2026-05-13 17:30".
  - **4 thẻ pillar** 2x2 phía dưới: P1 Supply, P2 Market, P3 Grid, P4 Transition.

### 7.3. Test smoke

- **Tap tab Pillars** → thấy 4 sub-tab (Supply Security / Market Resilience / Grid Reliability / Energy Transition). Swipe ngang để chuyển tab.
- **Tap tab Alerts** → danh sách cảnh báo, có filter chips CRITICAL / WARNING / INFO / All.
- **Tap tab Recommendations** → danh sách khuyến nghị, tap 1 item → dialog ACK với note input.
- **Tap tab Settings** → thấy username / role / email + ô đổi server URL + nút Test connection + Logout.
- **Pull-to-refresh** (kéo xuống ở list view) → list refresh, indicator quay.

### 7.4. Auto-refresh

Khi ở foreground, **Home** auto-refresh mỗi **30 giây** (config trong `AppConfig.DEFAULT_REFRESH_SECONDS`). Bạn thấy:
- Timestamp "Updated" thay đổi mỗi 30s.
- Số trên gauge có thể nhảy nhẹ nếu mock data dynamic.

---

## Section 8 — Khắc phục sự cố thường gặp

10 case Vietnamese-flavored, dày hơn `TROUBLESHOOTING.md` ở chỗ giải thích kỹ "tại sao", và bổ sung context cho người không phải Android dev.

### Case 1 — Gradle sync fail: "Could not find com.github.PhilJay:MPAndroidChart"

**Triệu chứng** (trong Build Output tab):

```
Could not resolve com.github.PhilJay:MPAndroidChart:v3.1.0.
Required by: project :app
```

**Nguyên nhân**: Thư viện `MPAndroidChart` (vẽ biểu đồ) host trên **JitPack** (`https://jitpack.io`), không phải Maven Central. Nếu mạng công ty / proxy chặn `jitpack.io`, hoặc `settings.gradle.kts` thiếu dòng JitPack, sẽ fail.

**Cách fix**:

1. Mở https://jitpack.io trong trình duyệt — phải load được. Nếu **bị chặn** → cấu hình proxy ở step 3.
2. Mở `settings.gradle.kts` ở repo root. Đảm bảo có khối:
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           google()
           mavenCentral()
           maven { url = uri("https://jitpack.io") }  // ← bắt buộc
       }
   }
   ```
3. (Mạng có proxy) Edit `gradle.properties` ở repo root, uncomment phần proxy:
   ```properties
   systemProp.https.proxyHost=proxy.bosch.com
   systemProp.https.proxyPort=8080
   systemProp.https.nonProxyHosts=*.localhost|127.0.0.1|10.*|192.168.*
   ```
4. **File → Sync Project with Gradle Files**.

> 📚 Chi tiết: [TROUBLESHOOTING §1](../TROUBLESHOOTING.md#1-gradle-sync--jitpack-dependency-not-found) + [§16](../TROUBLESHOOTING.md#16-behind-a-corporate-proxy).

### Case 2 — Sync fail "Unsupported class file major version 65"

**Triệu chứng**: Status bar đỏ + dòng:

```
> Failed to apply plugin 'com.android.internal.application'.
> Unsupported class file major version 65
```

**Nguyên nhân**: Bạn đang chạy với **JDK 21** (`major version 65`), nhưng Gradle classes được compile cho **JDK 17**.

**Cách fix**: Bắt AS dùng **embedded JDK 17** của nó.

1. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**.
2. Mục **Gradle JDK** → đổi sang **Embedded JDK** (= `C:\Program Files\Android\Android Studio\jbr`, JDK 17.x).
3. **Apply** → **OK**.
4. **File → Sync Project with Gradle Files**.

### Case 3 — Emulator không boot / treo ở logo Android

**Triệu chứng**: Cửa sổ emulator mở, hiện màn hình logo Android xoay, đứng mãi không vào home screen.

**Nguyên nhân**:
- (a) Không có **hardware acceleration** (HAXM / Hyper-V / WHPX) bật.
- (b) **Snapshot bị hỏng** từ lần đóng app trước.
- (c) Không đủ RAM trống (cần ~2.5 GB cho emulator).

**Cách fix**:

1. **(a)** Bật Windows Hypervisor Platform:
   - Control Panel → Programs → Turn Windows features on or off.
   - Tick: **Windows Hypervisor Platform**, **Virtual Machine Platform**, **Hyper-V** (nếu có).
   - **Reboot máy**.
2. **(b)** Wipe data của emulator:
   - **Tools → Device Manager** → click 3 chấm bên cạnh AVD → **Wipe Data** → confirm.
3. **(c)** Đóng Chrome, Slack, Docker Desktop → mở lại emulator.

### Case 4 — App login fail: "Network error: Unable to resolve host"

**Triệu chứng**: Tap Sign In → snackbar đỏ dưới đáy:

```
Network error: Unable to resolve host "10.0.2.2": No address associated with hostname
```

Hoặc:

```
Network error: failed to connect to /10.0.2.2 (port 8090): connect failed: ECONNREFUSED
```

**Nguyên nhân**:
- **Backend / mock server chưa chạy** — kiểm tra cửa sổ PowerShell chạy `.\start.ps1` có còn mở không.
- Hoặc app đang chạy trên **điện thoại thật** (`10.0.2.2` chỉ work trên emulator).

**Cách fix**:

1. **Trên máy host**: mở PowerShell mới, test:
   ```powershell
   Invoke-RestMethod http://localhost:8090/api/health
   ```
   Nếu fail → mock chưa chạy → quay lại [§5.1](#51-lựa-chọn-a--mock-server-khuyến-nghị-cho-lần-đầu).
2. **Trên emulator**: URL `http://10.0.2.2:8090/` là đúng (10.0.2.2 = host loopback). Đừng đổi.
3. **Trên điện thoại thật**: vào tab **Settings** trong app → đổi Base URL thành `http://<laptop-LAN-IP>:8090/`. Tìm laptop LAN IP:
   ```powershell
   ipconfig
   # Tìm dòng "IPv4 Address" trong "Wireless LAN adapter Wi-Fi"
   # Ví dụ: 192.168.1.42
   ```
   Sau khi đổi URL → app **tự rebuild Retrofit client**, login lại.

### Case 5 — Login fail 401 dù credentials đúng

**Triệu chứng**: Snackbar `Invalid credentials` hoặc `HTTP 401`.

**Nguyên nhân**: Mock seed user khác `admin`/`admin`, hoặc bạn đang point vào backend Java thật mà DB đã rotate password.

**Cách fix**:

1. Test login bằng curl từ PowerShell:
   ```powershell
   $body = '{"username":"admin","password":"admin"}'
   Invoke-RestMethod -Uri http://localhost:8090/api/auth/login `
                     -Method POST `
                     -ContentType 'application/json' `
                     -Body $body
   # Mong đợi: trả về object có accessToken, expiresInMs, user
   ```
   - Nếu fail → mock seed sai. Mở `mock/db.json`, search `"users"`, xem field `username` + `password`.
2. Nếu point backend Java thật → leader-team cung cấp credentials đã rotate.

### Case 6 — Gradle sync chậm (>15 phút) hoặc timeout

**Nguyên nhân**: Mạng chậm, hoặc proxy công ty đang inspect HTTPS từng request.

**Cách fix**:

1. Bumb timeout + heap trong `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
   org.gradle.parallel=true
   org.gradle.daemon=true
   ```
2. Mở **Gradle panel** (bên phải AS) → click ⚙ → **Offline Mode** OFF (nếu đang ON).
3. Bật **Settings → Build → Gradle → Download external annotations and Javadocs** → OFF (giảm tải).
4. Sau khi sync xong lần đầu, các lần sau chỉ mất 10–30s nhờ cache local.

### Case 7 — Build fail: "Unresolved reference: FragmentXyzBinding"

**Triệu chứng**: Compile fail với:

```
error: cannot find symbol class FragmentHomeBinding
```

**Nguyên nhân**: **ViewBinding** chưa generate lại sau khi sửa layout XML. Thường xảy ra khi layout XML có parse error (typo, duplicate ID).

**Cách fix**:

1. Trong AS: **Build → Clean Project** → đợi xong.
2. **Build → Rebuild Project**.
3. Nếu vẫn fail → mở file layout bị nghi, click tab **Design**. Nếu AS báo "Unable to parse layout" → fix XML.
4. Cuối cùng: **File → Invalidate Caches → Restart**.

> 📚 Chi tiết: [TROUBLESHOOTING §6](../TROUBLESHOOTING.md#6-viewbinding-generation-failed-for-layout-fragment_xyzxml) + [§7](../TROUBLESHOOTING.md#7-ridxyz-not-found--unresolved-reference-xyz).

### Case 8 — App crash khi rotate màn hình hoặc chuyển tab nhanh

**Nguyên nhân**: Một ViewModel đang giữ reference tới fragment đã destroy.

**Cách fix**: Đây là edge case của Android framework, không phải bug của project. Workaround: tránh xoay màn hình khi đang trong animation. Nếu reproduce stable → mở **Logcat** (View → Tool Windows → Logcat), filter level **Error**, copy stack trace, post vào team channel.

### Case 9 — Emulator không thấy internet (404 mọi request, không gọi được https://google.com)

**Nguyên nhân**: DNS của emulator bị broken — thường khi VPN trên host vào ra liên tục.

**Cách fix**:

1. Đóng emulator.
2. **Tools → Device Manager** → 3 chấm → **Cold Boot Now**.
3. Hoặc thêm DNS fix: emulator → **Settings → Wi-Fi → AndroidWifi → Edit network → Show advanced → DNS** → set `8.8.8.8`.

### Case 10 — `npm install` trong `mock/` fail với error `EACCES` / `ENOENT`

**Nguyên nhân**: Node.js cài vào path có ký tự đặc biệt (vd. dấu cách trong tên user), hoặc PowerShell không có quyền ghi.

**Cách fix**:

1. Xác nhận Node + npm đã cài:
   ```powershell
   node --version; npm --version
   ```
2. Xoá cache npm + thử lại:
   ```powershell
   cd mock
   Remove-Item -Recurse -Force node_modules, package-lock.json -ErrorAction SilentlyContinue
   npm cache clean --force
   npm install
   ```
3. Nếu vẫn fail → mở PowerShell **as Administrator** → `cd mock` → `npm install`.

> 📚 Đầy đủ 20 case + Android-specific deeper: [`TROUBLESHOOTING.md`](../TROUBLESHOOTING.md).

---

## Section 9 — Liên hệ & Resources

### Internal docs trong repo này

| File | Mục đích |
|------|----------|
| [`README.md`](../README.md) | Tổng quan repo + tech stack |
| [`SETUP_CHECKLIST.md`](../SETUP_CHECKLIST.md) | Checklist 1 trang (10–15 phút) |
| [`TROUBLESHOOTING.md`](../TROUBLESHOOTING.md) | 20 lỗi thường gặp + fix copy-paste |
| [`docs/START_HERE.md`](START_HERE.md) | Tour guide 4 tuần cho developer onboard |
| [`docs/ANDROID_ONBOARDING.md`](ANDROID_ONBOARDING.md) | Onboarding chi tiết (~30k chữ) |
| [`docs/API_CONTRACT.md`](API_CONTRACT.md) | 14 REST endpoint + sample request/response |
| [`docs/ARCHITECTURE.md`](ARCHITECTURE.md) | MVVM structure + Gradle deps + 7 màn breakdown |
| [`docs/DEVELOPER_ONBOARDING.md`](DEVELOPER_ONBOARDING.md) | Onboarding cho Android dev (kèm theo file này) |
| [`docs/AUDIT_REPORT_ANDROID.md`](AUDIT_REPORT_ANDROID.md) | Audit log Phase 1 → final |
| [`docs/MASS_QA_REPORT.md`](MASS_QA_REPORT.md) | Báo cáo mass QA pass cuối cùng |
| [`mock/README.md`](../mock/README.md) | Hướng dẫn mock backend chi tiết |
| [`examples/responses/README.md`](../examples/responses/README.md) | Index 14 JSON sample payload |

### External resources

- **Android Studio download**: https://developer.android.com/studio
- **Android Studio docs**: https://developer.android.com/studio/intro
- **Gradle 8.5 user guide**: https://docs.gradle.org/8.5/userguide/userguide.html
- **JitPack (MPAndroidChart host)**: https://jitpack.io
- **Material 3 design**: https://m3.material.io
- **Node.js LTS** (cho mock): https://nodejs.org
- **Git for Windows**: https://git-scm.com/download/win

### Repo liên quan

- **Android app** (đây): https://github.com/mtoanng/DataStream
- **Backend Java + Kafka + Flink + Postgres**: https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres

### Issue tracker

- Báo lỗi / hỏi đáp Android app: https://github.com/mtoanng/DataStream/issues
- Báo lỗi backend / API: https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres/issues

---

## Checklist tổng kết

Sau khi hoàn thành tất cả section ở trên, bạn nên có thể tick được tất cả các mục dưới đây:

- [ ] Android Studio Hedgehog 2023.1+ đã cài
- [ ] SDK Platform 34 + Build-Tools 34 đã tải
- [ ] AVD `Pixel 5 API 34` (x86_64) đã tạo và boot được
- [ ] Repo `DataStream` đã clone về `C:\Users\<you>\DataStream`
- [ ] Gradle sync **xanh** (không có dòng đỏ nào trong Build Output)
- [ ] Mock server hoặc backend Java đang chạy tại `http://localhost:8090`
- [ ] `Invoke-RestMethod http://localhost:8090/api/health` trả về `{"status":"UP", ...}`
- [ ] Click ▶ Run trong AS → APK build OK + install vào emulator OK
- [ ] LoginActivity hiện ra, đăng nhập `admin`/`admin` thành công
- [ ] Tab Home hiện đồng hồ ESI + 4 thẻ pillar (số ≠ "--")
- [ ] Bottom nav chuyển được 5 tab, pull-to-refresh hoạt động

✅ **Tất cả tick xong → app đã chạy thành công.** Khi cần dev tiếp, xem [`docs/DEVELOPER_ONBOARDING.md`](DEVELOPER_ONBOARDING.md).

---

**Repo**: https://github.com/mtoanng/DataStream
**Tài liệu này**: `docs/QUICKSTART_FOR_USER.md`
**Cập nhật lần cuối**: 15/05/2026 — HEAD `origin/main`
