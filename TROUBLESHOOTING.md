# 🛠 TROUBLESHOOTING — DataStream (Android)

Top failure modes you may hit on **first sync**, **first build**, or
**first run**. Each section: **Symptom → Cause → Fix (copy-paste)**.

If your problem isn't here, check the Gradle "Build Output" tab in
Android Studio (View → Tool Windows → Build) — the *first* red line
usually names the root cause.

---

## 1. Gradle sync — JitPack dependency not found

**Symptom**

```
Could not resolve com.github.PhilJay:MPAndroidChart:v3.1.0.
Required by: project :app
> Could not resolve com.github.PhilJay:MPAndroidChart:v3.1.0.
```

**Cause** — The MPAndroidChart 3.1.0 library is hosted on JitPack
(`https://jitpack.io`), not Maven Central. If a previous edit
accidentally removed the JitPack repo, resolution fails.

**Fix** — Make sure `settings.gradle.kts` has the JitPack repo in
`dependencyResolutionManagement.repositories`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // ← required for MPAndroidChart
    }
}
```

Then **File → Sync Project with Gradle Files**.

---

## 2. "Plugin [id: 'com.android.application'] was not found"

**Symptom**

```
Plugin [id: 'com.android.application', version: '8.2.0', apply: false] was not found in any of the following sources:
- Gradle Core Plugins
- Plugin Repositories
```

**Cause** — The Gradle wrapper version doesn't match what AGP 8.2 needs,
**or** the corporate proxy is blocking `plugins.gradle.org`.

**Fix** — Verify these two pin down compatible versions:

- `gradle/wrapper/gradle-wrapper.properties` → `gradle-8.5-bin.zip`
- `gradle/libs.versions.toml` → `agp = "8.2.0"`

If versions look right, you're behind a proxy. See §16.

---

## 3. "Unsupported class file major version 65"

**Symptom**

```
> Failed to apply plugin 'com.android.internal.application'.
> Unsupported class file major version 65
```

**Cause** — JDK 21 (`major version 65`) tries to load Gradle classes
compiled for JDK 17. Older Gradle versions don't recognise JDK 21
bytecode.

**Fix** — Gradle 8.5 (this project) **does** support JDK 21 but only
when run from Android Studio Hedgehog+ (which uses its bundled JDK 17
for compilation). If you launched from terminal with JDK 21:

```powershell
# Use AS's bundled JDK 17 for command-line builds:
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew clean
```

Or in **File → Settings → Build, Execution, Deployment → Build Tools →
Gradle → Gradle JDK**, pick "Embedded JDK 17".

---

## 4. `gradle-wrapper.jar` not found

**Symptom**

```
Error: Could not find or load main class org.gradle.wrapper.GradleWrapperMain
Caused by: java.lang.ClassNotFoundException: org.gradle.wrapper.GradleWrapperMain
```

**Cause** — The wrapper jar at `gradle/wrapper/gradle-wrapper.jar` is
missing (e.g. because `.gitignore` excluded it on a fork).

**Fix** — The repo now commits the jar (43,462 bytes, Gradle 8.5).
Verify with:

```powershell
Test-Path gradle/wrapper/gradle-wrapper.jar
# → True
```

If still missing, regenerate via Android Studio: **File → Sync
Project with Gradle Files** (AS will run `gradle wrapper` using its
embedded Gradle), or from terminal if you have system Gradle:

```powershell
gradle wrapper --gradle-version 8.5
```

---

## 5. "Manifest merger failed"

**Symptom**

```
Manifest merger failed : Attribute application@allowBackup value=(true)
from AndroidManifest.xml:8:9-35
  is also present at [androidx.work:work-runtime:2.9.0] AndroidManifest.xml:26:18-44
    value=(false).
Suggestion: add 'tools:replace="android:allowBackup"' …
```

**Cause** — Some library you added later declared a conflicting
`<application>` attribute.

**Fix** — Add the suggested attribute to your `<application>` element.
This repo already declares the common ones:

```xml
tools:replace="android:allowBackup,android:fullBackupContent"
```

If the conflict is on a different attribute (e.g. `appComponentFactory`),
append it to that list separated by commas.

---

## 6. "ViewBinding generation failed for layout fragment_xyz.xml"

**Symptom** — Compile error pointing at a Kotlin file using
`FragmentXyzBinding`:

```
error: cannot find symbol class FragmentXyzBinding
```

**Cause** — The layout XML has a parse error, **or** two `@+id` declarations
collide inside the same layout, **or** the layout filename uses `-` instead
of `_` (e.g. `fragment-xyz.xml` won't generate a binding class).

**Fix**

1. Open the offending layout, click "Design" → if AS shows
   "Unable to parse layout", the cause is at top of the Errors tab.
2. Search for duplicate `android:id="@+id/foo"` inside that file.
3. Make sure the filename is **lowercase + underscores** only.

To force a clean ViewBinding regeneration:

```powershell
.\gradlew clean
.\gradlew :app:assembleDebug
```

Or in AS: **Build → Clean Project**, then **Build → Rebuild Project**.

---

## 7. "R.id.xyz not found" / "Unresolved reference: xyz"

**Symptom**

```
error: cannot find symbol variable xyz
  binding.xyz.text = "..."
```

**Cause** — The Kotlin code references an `@+id` that doesn't exist in
the layout, **or** has a different casing/spelling.

**Fix** — Open the layout file and confirm the `@+id` matches the
Kotlin name. ViewBinding converts `snake_case` IDs to `camelCase`
(e.g. `@+id/btn_login` → `binding.btnLogin`), but underscores
inside the ID are dropped, not kept.

| Layout `@+id`      | Generated field         |
|---------------------|--------------------------|
| `btn_login`         | `binding.btnLogin`       |
| `tv_overall_status` | `binding.tvOverallStatus`|
| `tilUsername`       | `binding.tilUsername`    |

A frequent gotcha: **hyphens** in IDs (`btn-login`) **break the binding**.
Use underscores.

---

## 8. "Theme.Material3.DayNight.NoActionBar not found"

**Symptom**

```
AAPT: error: resource style/Theme.Material3.DayNight.NoActionBar not found.
```

**Cause** — Missing or outdated Material Components library.

**Fix** — Check `gradle/libs.versions.toml` has at least:

```toml
material = "1.11.0"
```

Then in `app/build.gradle.kts`:

```kotlin
implementation(libs.material) // → com.google.android.material:material:1.11.0
```

Sync Gradle.

---

## 9. Login fails with "Unable to resolve host"

**Symptom** — Login button shows snackbar:

```
Network error: Unable to resolve host "10.0.2.2": No address associated with hostname
```

**Cause** — The emulator can't reach the Java backend, or you're using
a real device but pointing at `10.0.2.2` (which means "emulator host
loopback", not your phone's loopback).

**Fix**

- **On emulator**: leave URL at `http://10.0.2.2:8090` (default). Make
  sure the backend is running on the host machine at port 8090. Test
  from PowerShell: `curl http://localhost:8090/api/health`.
- **On real device**: open the in-app **Settings** screen, change
  base URL to `http://<your-laptop-LAN-IP>:8090` (e.g.
  `http://192.168.1.42:8090`). Phone and laptop must be on the same
  Wi-Fi. Also add that IP to `network_security_config.xml` — see §11.

> 💡 **Note**: changing the base URL takes effect **instantly** — no app
> restart required. Since the 16 May 2026 sweep-fix pass, `NetworkModule`
> hands out a delegating proxy that re-resolves the underlying Retrofit
> instance on every call, so existing repository references keep working
> against the new URL the moment you tap **Save**.

---

## 10. Login fails with 401 even though credentials look right

**Symptom**

```
Snackbar: "Invalid credentials" (or similar 401 message from backend)
```

**Cause** — Either the wrong default seed (`admin`/`admin`), a typo,
or the backend has rotated credentials.

**Fix**

- Default seed in this repo: **`admin` / `admin`** (lowercase, no
  whitespace).
- If you reset the backend DB recently, re-run the seed script:
  `mvn -pl ves-backend-api flyway:migrate` on the backend side.
- Read backend logs (`backend.log`) for the exact failure reason.

---

## 11. `CleartextHttpException` at runtime

**Symptom** — App crashes when first network call goes out:

```
java.io.IOException: Cleartext HTTP traffic to 192.168.5.42 not permitted
```

**Cause** — `network_security_config.xml` doesn't whitelist your dev
backend's IP. Android NSC takes **literal** IP addresses — CIDR ranges
are *not* supported.

**Fix** — Open
`app/src/main/res/xml/network_security_config.xml` and add your laptop's
LAN IP inside `<domain-config cleartextTrafficPermitted="true">`:

```xml
<domain includeSubdomains="false">192.168.5.42</domain>
```

Then rebuild — NSC is baked into the APK so changes need a new install.

---

## 12. "Failed to load Fragment com.mtoanng.datastream.ui.home.HomeFragment"

**Symptom**

```
java.lang.RuntimeException: Cannot create instance of class
com.mtoanng.datastream.ui.home.HomeFragment
Caused by: java.lang.ClassNotFoundException: ... HomeFragment
```

**Cause** — A typo in `nav_graph.xml` `android:name`, **or** you
renamed/moved the Fragment without updating the FQN in nav_graph.

**Fix** — Open `app/src/main/res/navigation/nav_graph.xml` and verify
each `<fragment android:name="...">` is the exact fully-qualified
class path of a real `.kt` file. Case matters.

---

## 13. Bottom nav highlight doesn't follow the selected tab

**Symptom** — Tapping a bottom nav icon switches the fragment, but the
icon highlight stays on the previous one.

**Cause** — The menu item ID in `res/menu/bottom_nav.xml` doesn't match
the fragment ID in `res/navigation/nav_graph.xml`. `NavController
.setupWithNavController(bottomNav)` wires them up **by identical id**.

**Fix** — Verify ALL of these match 1:1 (current repo state):

| Menu (`bottom_nav.xml`) | Nav graph (`nav_graph.xml`)        |
|--------------------------|-------------------------------------|
| `@+id/homeFragment`      | `<fragment android:id="@+id/homeFragment">` |
| `@+id/pillarsFragment`   | `<fragment android:id="@+id/pillarsFragment">` |
| `@+id/alertsFragment`    | `<fragment android:id="@+id/alertsFragment">` |
| `@+id/recommendationsFragment` | `<fragment android:id="@+id/recommendationsFragment">` |
| `@+id/settingsFragment`  | `<fragment android:id="@+id/settingsFragment">` |

---

## 14. Layout Preview: "Error inflating class FragmentContainerView"

**Symptom** — In the **Design** tab of `activity_main.xml`, you see:

```
Error inflating class androidx.fragment.app.FragmentContainerView
```

**Cause** — Android Studio's Layout Inspector bug (cosmetic). The app
itself still builds + runs fine.

**Fix** — Click the **Refresh** icon in the preview toolbar, or
**Build → Rebuild Project**, or restart AS. Ignore if the bug persists
— it does *not* affect the runtime.

---

## 15. Gradle sync runs out of memory ("Java heap space")

**Symptom** — Mid-sync:

```
Could not execute Gradle daemon. Java heap space.
```

**Cause** — Default `-Xmx2048m` is too small for laptops indexing very
large dep graphs.

**Fix** — Edit `gradle.properties` and bump the heap:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8 -XX:+HeapDumpOnOutOfMemoryError
```

Then **File → Invalidate Caches → Restart**.

---

## 16. Behind a corporate proxy

**Symptom** — Sync hangs for 60 s then fails with:

```
Could not GET 'https://jcenter.bintray.com/...': Connection timed out
```

**Cause** — Corporate firewall blocks direct outbound HTTPS.

**Fix** — Uncomment + fill the proxy block at the bottom of
`gradle.properties`:

```properties
systemProp.https.proxyHost=proxy.example.com
systemProp.https.proxyPort=8080
systemProp.https.nonProxyHosts=*.localhost|127.0.0.1|10.*|192.168.*|172.16.*
```

For authenticated proxies, also add `proxyUser` + `proxyPassword`.

Then **File → Sync Project with Gradle Files**.

---

## 17. Emulator slow on cold boot

**Symptom** — Pixel 5 / API 34 AVD takes 2-3 minutes to start every time.

**Cause** — Cold Boot recreates the device state from scratch.

**Fix** — Use **Quick Boot** (default in AS Hedgehog+):

1. **Device Manager** → click the **Edit** ✏ icon next to your AVD.
2. **Advanced Settings** → **Boot option** → check **Quick boot**.
3. Run the AVD once with Cold Boot to create the snapshot. Subsequent
   boots take ~10 s.

---

## 18. "Process 'command sdkmanager.bat' finished with non-zero exit value 1"

**Symptom**

```
> Failed to install the following Android SDK packages as some licences have not been accepted.
   build-tools;34.0.0 Android SDK Build-Tools 34
```

**Cause** — Android SDK licences not accepted yet.

**Fix** — Accept all licences (run as the same user):

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
# Press 'y' + Enter for each prompt
```

If `cmdline-tools` is missing, install it via **Android Studio →
SDK Manager → SDK Tools → Android SDK Command-line Tools (latest)**.

---

## 19. MPAndroidChart class not found at runtime

**Symptom**

```
java.lang.NoClassDefFoundError: com.github.mikephil.charting.charts.BarChart
```

**Cause** — Gradle sync skipped the JitPack repo (e.g. offline mode),
so the jar was never downloaded; only the stub class is on the classpath.

**Fix**

1. Make sure Gradle is **not in Offline Mode**: AS toolbar → ⚙ icon
   on the Gradle panel → uncheck "Offline mode".
2. **File → Sync Project with Gradle Files**.
3. If still missing, **File → Invalidate Caches → Restart** and
   sync again. JitPack's first build of a coordinate can take 30-60 s
   server-side.

---

## 20. App crashes on launch with `NullPointerException: networkSecurityConfig`

**Symptom**

```
java.lang.RuntimeException: Unable to instantiate application
  com.mtoanng.datastream.DataStreamApp: java.lang.NullPointerException:
  Attempt to read from field 'int android.content.pm.ApplicationInfo
  .networkSecurityConfigRes' on a null object reference
```

**Cause** — The `<application android:networkSecurityConfig="@xml/network_security_config">`
attribute was removed (or the XML file deleted).

**Fix** — Restore both:

- `app/src/main/AndroidManifest.xml` line 16: `android:networkSecurityConfig="@xml/network_security_config"`
- `app/src/main/res/xml/network_security_config.xml` (present in this repo)

Then **Build → Rebuild Project**.

   ```

---

## 21. Google Sign-In failed with status code 10

**Symptom** — Snack bar shows:
`Lỗi 10: Sai mã SHA-1 hoặc sai Client ID (Phải dùng Web ID). Kiểm tra Logcat!`

**Cause** — This is a `DEVELOPER_ERROR`. It happens when:
1. The SHA-1 of your debug/release signing certificate is not registered in the Google Cloud Console or Firebase.
2. You are using the "Android Client ID" instead of the "Web Client ID" for the `requestIdToken` parameter.

**Fix**
1. Check Logcat for the tag `GOOGLE_AUTH_DEBUG`. The app automatically prints your current SHA-1 there on launch (see `LoginActivity.printSignatureInfo`).
2. Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials) or Firebase Console.
3. Add a new **Android Client ID** with your package name (`com.mtoanng.datastream`) and the SHA-1 from Logcat.
4. Find the **Web Client ID** (type: Web application) in the same project.
5. Copy that Web Client ID into `app/src/main/res/values/strings.xml` under `google_web_client_id`.

---

## 🆘 Still stuck?

1. Read the **first** line of the red error in **Build → Build Output**
   — that's almost always the root cause.
2. Search the exact error string on
   [stackoverflow.com/questions/tagged/android-gradle-plugin](https://stackoverflow.com/questions/tagged/android-gradle-plugin).
3. Try **File → Invalidate Caches → Restart** — solves ~30% of AS-internal
   "ghost" errors.
4. As a last resort, blow away local caches:

   ```powershell
   .\gradlew clean
   Remove-Item -Recurse -Force .gradle
   Remove-Item -Recurse -Force build, app/build -ErrorAction SilentlyContinue
   ```

   Then re-open in AS and sync.
