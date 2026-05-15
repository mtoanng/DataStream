# 🚦 TRY THIS FIRST — DataStream (VES-Monitor Android)

> **You are looking at the right file.** This is the **single entrance** for any developer who just cloned `mtoanng/DataStream` and wants to see the app run.
>
> Mục tiêu: từ `git clone` đến **Login → Home dashboard** trong **3 phút** trên một máy đã có Android Studio, ~30 phút trên một máy mới tinh.

[![Status](https://img.shields.io/badge/status-Handover%20Ready-brightgreen)]() [![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-green)]() [![Language](https://img.shields.io/badge/language-Kotlin%201.9.21-purple)]() [![Build](https://img.shields.io/badge/Gradle-8.5-blueviolet)]() [![AGP](https://img.shields.io/badge/AGP-8.2.0-orange)]() [![Tests](https://img.shields.io/badge/tests-15%2F15-success)]() [![License](https://img.shields.io/badge/license-Educational-blue)]()

---

## ⚡ 3-minute happy path (bạn đã có Android Studio + JDK 17)

```powershell
# 1) Clone
git clone https://github.com/mtoanng/DataStream.git
cd DataStream

# 2) Pre-flight check — bỏ qua tốn 5-15 phút "tại sao sync fail?" sau này
.\scripts\verify-environment.ps1
# (hoặc trên macOS / Linux / WSL: bash scripts/verify-environment.sh)

# 3) Mở Android Studio:  File -> Open -> chọn folder DataStream/
#    Khi AS hỏi "Trust Project?" -> bấm Trust Project.
#    Đợi Gradle sync (3-5 phút lần đầu, ~30 GB tải).

# 4) Tạo & boot emulator Pixel 5 / API 34 (xem SETUP_CHECKLIST.md mục B).

# 5) Trong app/ -> Run 'app' (Shift+F10) -> chọn emulator.
#    Login form mở sẵn với admin / admin -> bấm Sign In.

# 6) ✅ Bạn đang nhìn thấy Home dashboard với ESI gauge + 4 pillar mini-cards.
```

> **Backend?** Không cần — app default trỏ về `http://10.0.2.2:8090`. Có 2 cách:
> - **Mock server (5 phút setup, đầy đủ 14 endpoint)**: `cd mock; .\start.ps1` (cần Node.js 18+).
> - **Backend Java thật (Spring Boot + Postgres + Flink)**: clone & chạy [`mtoanng/Real-time-processing-with-Kafka-Flink-Postgres`](https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres).

---

## ❌ Nếu `verify-environment.ps1` fail

| # | Lỗi script báo | Nguyên nhân thường gặp | Fix nhanh | Chi tiết |
|---|---|---|---|---|
| 1 | `JDK 17+` FAIL | Chưa cài JDK hoặc đang dùng JDK 8/11 | Tải [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17), hoặc dùng JDK 17 bundled trong Android Studio | [TROUBLESHOOTING §3](TROUBLESHOOTING.md) |
| 2 | `git installed` FAIL | Chưa cài Git for Windows | Tải [Git for Windows](https://git-scm.com/download/win) | — |
| 3 | `Disk space` FAIL | < 10 GB trống | Dọn ổ; Gradle cache + emulator có thể chiếm ~8 GB | — |
| 4 | `Maven Central reachable` WARN/FAIL | Mạng/VPN/proxy công ty chặn outbound HTTPS | Mở `gradle.properties` -> uncomment block `systemProp.https.proxyHost=…` | [TROUBLESHOOTING §16](TROUBLESHOOTING.md) |
| 5 | `JitPack reachable` WARN/FAIL | Tương tự (cần cho MPAndroidChart) | Như trên + thêm `jitpack.io` vào allowlist proxy | [TROUBLESHOOTING §1](TROUBLESHOOTING.md) |
| 6 | `gradle-wrapper.jar` FAIL | File 43 462 byte bị xoá hoặc clone hỏng | `git checkout -- gradle/wrapper/gradle-wrapper.jar` hoặc clone lại | [TROUBLESHOOTING §4](TROUBLESHOOTING.md) |
| 7 | `local.properties` WARN | Lần đầu mở repo, AS chưa generate file | An toàn — bỏ qua. AS sẽ tạo `local.properties` với `sdk.dir=…` ngay khi sync | — |
| 8 | `Android Studio installed` WARN | Chưa cài hoặc cài ở thư mục lạ | Tải [Android Studio Hedgehog 2023.1+](https://developer.android.com/studio) | — |

**Sau khi fix**: chạy lại `.\scripts\verify-environment.ps1`. Khi nó in `READY` → mở project.

---

## 🧭 3 đường vào tuỳ vai trò

| Bạn là... | Đọc file này | Thời gian |
|---|---|---|
| **Lần đầu mở project (lập trình viên Android)** | `TRY_THIS_FIRST.md` (bạn đang đọc) → [`SETUP_CHECKLIST.md`](SETUP_CHECKLIST.md) | 30-45 phút |
| **Người không phải dev, muốn demo nhanh** | [`docs/QUICKSTART_FOR_USER.md`](docs/QUICKSTART_FOR_USER.md) | 60 phút |
| **Android dev mới join team** (cần hiểu kiến trúc) | [`docs/DEVELOPER_ONBOARDING.md`](docs/DEVELOPER_ONBOARDING.md) | 2 giờ |

---

## 📊 At-a-glance

| Metric | Value |
|---|---:|
| Kotlin source files | 56 |
| Kotlin source lines (main) | ~ 2 000 |
| Layout XML files | 14 |
| Total XML resources | 36 |
| DTO classes (`data/dto/`) | 16 |
| Test files / `@Test` methods | 3 / 15 |
| Backend endpoints consumed | 13 canonical (+ 6 legacy aliases) |
| Min / target / compile SDK | 26 / 34 / 34 |
| Languages supported | English (default) + Vietnamese (`values-vi/`) |
| Architecture | MVVM single-Activity + Fragments + Navigation Component |
| Dependency Injection | Manual (no Hilt / Dagger / Koin) |
| Network stack | Retrofit 2.9 + OkHttp 4.12 + Moshi 1.15 |
| Charts | MPAndroidChart 3.1.0 (JitPack) |
| Logging | Timber 5.0.1 |

---

## 🔗 Direct links

| Topic | Link |
|---|---|
| **Backend repo** (Java + Spring Boot + Flink + Postgres) | [`mtoanng/Real-time-processing-with-Kafka-Flink-Postgres`](https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres) |
| Mock server how-to (offline UI dev) | [`mock/README.md`](mock/README.md) |
| API contract (14 endpoints + samples) | [`docs/API_CONTRACT.md`](docs/API_CONTRACT.md) |
| Architecture deep-dive | [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) |
| Excalidraw architecture diagrams (5 scenes) | [`docs/diagrams/excalidraw/`](docs/diagrams/excalidraw/) |
| Tour guide 4 tuần (kickoff → PR7) | [`docs/START_HERE.md`](docs/START_HERE.md) |
| Setup checklist | [`SETUP_CHECKLIST.md`](SETUP_CHECKLIST.md) |
| Troubleshooting (20 known failures) | [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md) |
| Handover checklist (what was verified) | [`HANDOVER_CHECKLIST.md`](HANDOVER_CHECKLIST.md) |
| Issue tracker | https://github.com/mtoanng/DataStream/issues |

---

## 🧯 Top 5 first-import failures (copy-paste fixes)

If your first-import experience hits one of these, you are in good company.

### 1. Gradle sync stuck on "Build Model" for >5 minutes

```powershell
# 1) File -> Invalidate Caches -> Invalidate and Restart
# 2) Quit Android Studio. Then:
Remove-Item -Recurse -Force .gradle, build, app/build -ErrorAction SilentlyContinue
# 3) Re-open in AS.
```

### 2. "SDK location not found. Define location with sdk.dir in the local.properties file."

```powershell
Copy-Item local.properties.template local.properties
# Then open local.properties and set:
#   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
# Adjust path; common alternatives:
#   $env:LOCALAPPDATA\Android\Sdk
#   C:\Android\Sdk
# Save -> File -> Sync Project with Gradle Files.
```

### 3. "Unsupported JVM target" or "compileSdkVersion is incompatible"

```text
Android Studio -> File -> Settings ->
    Build, Execution, Deployment -> Build Tools -> Gradle ->
        Gradle JDK -> pick "Embedded JDK" (jbr-17)
File -> Sync Project with Gradle Files.
```

### 4. JitPack unreachable / `Could not resolve com.github.PhilJay:MPAndroidChart:v3.1.0`

You're behind a corporate proxy or JitPack is throttled. In `gradle.properties`, uncomment the proxy block at the bottom and fill in your proxy host/port:

```properties
systemProp.https.proxyHost=proxy.example.com
systemProp.https.proxyPort=8080
systemProp.https.nonProxyHosts=*.localhost|127.0.0.1|10.*|192.168.*|172.16.*
```

Then `File → Sync Project with Gradle Files`. Full guide in [`TROUBLESHOOTING.md §16`](TROUBLESHOOTING.md).

### 5. App runs but Login fails with "Unable to resolve host" / "Connection refused"

The app started but can't reach a backend on `http://10.0.2.2:8090`.

- **Quickest path**: start the bundled mock server in another terminal:

```powershell
cd mock
.\start.ps1
# server now listening on http://localhost:8090 — emulator reaches it as 10.0.2.2:8090
```

- **Or** start the Java backend stack ([instructions](https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres)).

- **Or** use the in-app **Settings** screen to point Base URL at any reachable backend (real device on Wi-Fi → `http://<your-laptop-LAN-IP>:8090`; remember to also add that IP to `network_security_config.xml`, see [`TROUBLESHOOTING.md §11`](TROUBLESHOOTING.md)).

---

## ✅ Sanity check after first build

After Gradle sync goes green, run these from PowerShell to confirm the app is healthy:

```powershell
# 1) Wrapper jar still 43 462 bytes (canonical)
(Get-Item gradle/wrapper/gradle-wrapper.jar).Length    # → 43462

# 2) All 36 XML files parse cleanly
$xml = Get-ChildItem app/src/main -Recurse -Filter *.xml; foreach ($f in $xml) { [xml](Get-Content -Raw $f.FullName) | Out-Null }; "OK: $($xml.Count) files"

# 3) All JSON sample / mock files parse cleanly
$json = Get-ChildItem mock, examples -Recurse -Filter *.json; foreach ($f in $json) { Get-Content -Raw $f.FullName | ConvertFrom-Json | Out-Null }; "OK: $($json.Count) files"
```

If all three commands print `OK`, you're truly green.

---

## 🆘 Still stuck?

1. Read the **first** red line in `Build → Build Output` — it almost always names the actual root cause.
2. Search the exact error message at [stackoverflow.com/questions/tagged/android-gradle-plugin](https://stackoverflow.com/questions/tagged/android-gradle-plugin).
3. `File → Invalidate Caches → Restart` — solves ~30% of "ghost" Android Studio errors.
4. Compare your environment with [`SETUP_CHECKLIST.md`](SETUP_CHECKLIST.md) section A (prerequisites).
5. Open an issue at https://github.com/mtoanng/DataStream/issues with the verify-environment output + Build tab text.

---

> 📦 This repo is **handover-baseline** at tag `v1.0.0-handover`. The Java backend has its own `v1.0.0-handover` tag at the matching point. See [`HANDOVER_CHECKLIST.md`](HANDOVER_CHECKLIST.md) for everything that was verified before handing the project over.
