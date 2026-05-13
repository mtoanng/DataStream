# ✅ SETUP_CHECKLIST — DataStream (Android)

Follow this list **before** you open Android Studio. Total time: 10-15 min if everything is fresh, ~2 min if you've already done Android work on this laptop.

For first-failure recovery, see [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md).

---

## A. Prerequisites (one-time per machine)

| # | Requirement | Test it works | Why |
|---|---|---|---|
| 1 | **JDK 17** (or 21) installed | `java -version` should print `17.x` or `21.x` | AGP 8.2 needs JDK 17+. AS bundles its own JDK 17, but a system JDK helps for command-line builds. |
| 2 | **Android Studio Hedgehog 2023.1+** (or Iguana 2023.2 / Jellyfish 2024.1) | Help → About → shows version | Earlier AS versions don't support AGP 8.2. |
| 3 | **Android SDK Platform 34** | AS → SDK Manager → SDK Platforms → "Android 14 (API 34)" is ✓ | `compileSdk = 34`, `targetSdk = 34`. |
| 4 | **Android SDK Build-Tools 34.0.0** | AS → SDK Manager → SDK Tools → "Android SDK Build-Tools 34" | Compiles resources + DEX. |
| 5 | **AVD (Pixel 5 / API 34)** | AS → Device Manager → green ▶ icon next to it | Default target for Run. |
| 6 | **At least 8 GB RAM free** | Task Manager | Gradle daemon needs ~2 GB, AS needs ~3 GB, emulator needs ~2.5 GB. |
| 7 | **Internet access** for first sync | Open `https://jitpack.io` in browser | First sync downloads ~150 MB from Maven Central + Google + JitPack. |

### Optional (corporate machines)

| # | Setting | When |
|---|---|---|
| 8 | `JAVA_HOME` env var | Pointed at AS's bundled JDK: `C:\Program Files\Android\Android Studio\jbr`. Useful for command-line `gradlew` work. |
| 9 | HTTPS proxy in `gradle.properties` | If `jitpack.io` is blocked. See [TROUBLESHOOTING.md §16](TROUBLESHOOTING.md#16-behind-a-corporate-proxy). |

---

## B. Recommended AVD spec

```
Device:       Pixel 5
System image: Android 14 (API 34) — x86_64 with Google APIs
RAM:          2048 MB
Internal:     2048 MB
SD card:      512 MB
Graphics:     Automatic (Hardware GLES 2.0)
Boot option:  Quick boot (after first cold boot)
```

Why Pixel 5 / API 34: matches the project's `targetSdk`, has a normal
1080×2340 screen, and the API 34 system image is the one Android Studio
suggests by default in Hedgehog+.

---

## C. First-open sequence

Do these **in this order**:

### Step 1 — Clone (or pull) the repo

```powershell
git clone https://github.com/mtoanng/DataStream.git
cd DataStream
git pull origin main         # if you already cloned earlier
```

### Step 2 — Open in Android Studio

1. **File → Open** → select the **`DataStream/`** folder (not a subfolder!).
2. When AS prompts **"Trust Project?"** → click **Trust Project**.
3. AS may show a "Use Gradle from: 'gradle-wrapper.properties' file" dialog
   → click **OK** (default).

### Step 3 — Wait for Gradle sync

- Bottom progress bar: "Gradle: Build Model" → "Gradle: Configure project :app".
- First sync downloads ~150 MB and takes **3-5 min** on a fast laptop +
  fast internet. Up to 10 min behind a corporate proxy.
- ✅ Success looks like: green "Gradle sync finished" toast + the
  `app/` module shows a blue Android-head icon in the project tree.
- ❌ If you see red errors, jump to [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md).

### Step 4 — First build (Make Project)

1. **Build → Make Project** (or `Ctrl+F9`).
2. Should compile in **< 60 s** on a warm cache, < 3 min cold.
3. ✅ Green "Build successful" in the Build Output tab.

### Step 5 — Pick the emulator

1. Top toolbar → device dropdown → pick your **Pixel 5 API 34**.
2. The emulator window opens (takes 30-60 s on first cold boot).

### Step 6 — Start the backend

Either run the **mock server** (no Java/Postgres needed) …

```powershell
cd mock
.\start.ps1
# → server listening on http://localhost:8090
```

… **or** the full **Java backend** (Spring Boot + Postgres + Flink) —
follow the sibling repo:
[Real-time-processing-with-Kafka-Flink-Postgres](https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres).

The emulator reaches `localhost` on the host machine as `10.0.2.2`,
so the app's default `http://10.0.2.2:8090` URL already works.

### Step 7 — ▶ Run

1. Click the green **▶ Run** button (or `Shift+F10`).
2. AS installs the APK on the emulator (10-20 s).
3. **LoginActivity** opens with `admin` / `admin` pre-filled.
4. Tap **Sign In** → bottom-nav shows (Home / Pillars / Alerts / Recommendations / Settings).

✅ **Done.** The Home tab shows a circular gauge + 4 mini-cards updated every 30 s.

---

## D. Quick sanity checks before pulling your hair out

Run these from PowerShell at the repo root.

```powershell
# 1. Wrapper jar present?
Test-Path gradle/wrapper/gradle-wrapper.jar       # → True

# 2. Gradle wrapper version pinned to 8.5?
Select-String 'distributionUrl' gradle/wrapper/gradle-wrapper.properties
# → distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip

# 3. local.properties NOT committed?
Test-Path local.properties                        # → False or True (either fine; gitignored)

# 4. SDK path resolvable? (AS sets this automatically)
Get-Content local.properties -ErrorAction SilentlyContinue
# Should mention sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# 5. JDK >= 17?
java -version                                     # → 17.x or 21.x
```

If any of those fail, see [`TROUBLESHOOTING.md`](TROUBLESHOOTING.md).

---

## E. Useful Gradle commands (after sync is green)

| Command | What it does |
|---|---|
| `.\gradlew clean` | Wipe `build/` (force full recompile). |
| `.\gradlew :app:assembleDebug` | Build debug APK. Output at `app/build/outputs/apk/debug/app-debug.apk`. |
| `.\gradlew :app:assembleRelease` | Build minified release APK (ProGuard / R8). Needs signing config for installable APK. |
| `.\gradlew :app:test` | Run all 15 JVM unit tests (Formatters + AuthRepository + LoginViewModel). |
| `.\gradlew :app:lint` | Static analysis. Reports go to `app/build/reports/lint-results-debug.html`. |
| `.\gradlew --refresh-dependencies` | Force-redownload everything (use if JitPack lookup got cached as 404). |

---

## F. What's NOT installed by following this checklist

| Thing | Where to get it |
|---|---|
| The **Java backend** (Spring Boot + Postgres + Kafka + Flink) | [Real-time-processing-with-Kafka-Flink-Postgres](https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres) |
| **Node.js 18+** (only needed for `mock/start.ps1`) | https://nodejs.org/ |
| **Docker Desktop** (only for the full backend stack) | https://www.docker.com/products/docker-desktop |

You can complete the entire mobile UI without ever installing Docker or
running the real backend — just use the mock server.
