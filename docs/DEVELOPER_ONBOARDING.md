# 👨‍💻 DEVELOPER_ONBOARDING — DataStream (VES-Monitor Android)

> **Audience**: Android developer (junior → mid) who already knows Android Studio + Kotlin + Gradle but is **new to this project**. You won't be taught what a Fragment or a ViewModel is — you'll be taught **how this codebase wires them together**.
>
> **Goal**: After reading this doc end-to-end (~30 min), you should be able to:
> 1. Run the app locally against a mock backend.
> 2. Trace a feature from UI → ViewModel → Repository → API → DTO.
> 3. Add a new screen or a new API endpoint **without breaking conventions**.
>
> **Complement docs**:
> - [`QUICKSTART_FOR_USER.md`](QUICKSTART_FOR_USER.md) — non-developer setup guide (Vietnamese, longer, beginner)
> - [`ANDROID_ONBOARDING.md`](ANDROID_ONBOARDING.md) — original full briefing (~30 KB)
> - [`ARCHITECTURE.md`](ARCHITECTURE.md) — deeper architectural rationale
> - [`API_CONTRACT.md`](API_CONTRACT.md) — 14-endpoint REST spec + JSON samples
> - [`TROUBLESHOOTING.md`](../TROUBLESHOOTING.md) — 20 known failure modes

---

## Section 1 — Project context (5 min read)

### 1.1. Domain

The app is the **mobile face of a Vietnam Energy Security (VES) monitoring system**. The system ingests fuel / grid / weather signals from across the country, runs a stream pipeline (Kafka → Flink), persists state to Postgres, and serves a REST API for clients (this Android app + a JavaFX desktop console).

Backend repo: https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres

### 1.2. The four IEA / APERC pillars

Energy security is modelled as **4 pillars**, each scored 0..100:

| # | Pillar | Canonical path | Legacy alias |
|---|--------|----------------|--------------|
| 1 | **Supply Security** | `/api/pillars/1/supply-security` | `/api/pillars/1/outlook` |
| 2 | **Market Resilience** | `/api/pillars/2/market-resilience` | `/api/pillars/2/volatility` |
| 3 | **Grid Reliability** | `/api/pillars/3/grid-reliability` | `/api/pillars/3/shedding[-plan]` |
| 4 | **Energy Transition** | `/api/pillars/4/energy-transition` | `/api/pillars/4/netzero`, `/net-zero` |

The Android client uses **canonical paths only** — backend keeps legacy aliases for the desktop console.

### 1.3. The composite ESI score

Backend folds the four pillar scores into a **single Energy Security Index (ESI)** at `GET /api/security/score`:

```json
{
  "pillar1Score": 72.3,
  "pillar2Score": 64.1,
  "pillar3Score": 81.5,
  "pillar4Score": 55.8,
  "overallScore": 68.4,
  "status": "ELEVATED",
  "computedAt": "2026-05-13T10:30:00Z"
}
```

Status thresholds (`util/EnergySecurityHelper.kt`):

| Score | Status     | Color (light) |
|-------|------------|---------------|
| ≥ 80  | `SECURE`   | green          |
| 60–79 | `ELEVATED` | amber         |
| 40–59 | `STRESSED` | orange        |
| < 40  | `CRITICAL` | red           |

### 1.4. App role

The app is **a read-mostly dashboard + alert + recommendation acknowledgment client**.

- **Read**: pillars, alerts, recommendations, fuel/grid raw streams (12 of 13 GET endpoints).
- **Write**: only `POST /api/recommendations/{id}/acknowledge` — user-driven action.

No business logic in the app; we **display what the backend computed**. UI just maps `status` strings → colours and `Double` scores → labels.

### 1.5. Out of scope

These are explicitly **not** in v1.0 (kept out to control complexity):

| Feature | Why deferred |
|---------|--------------|
| AI / LLM recommendations | Backend produces rule-based recos only; we display them as-is. |
| WebSocket realtime updates | Backend doesn't expose `/ws`. We poll every 30 s instead. |
| Firebase Cloud Messaging push | No FCM project + no backend `/fcm/register` endpoint yet. |
| Offline cache (Room) | App always assumes online. `TokenManager` is the only persistent state. |
| Biometric login | Out of scope for student project; pure username+password. |

These are tracked in [`docs/MASS_QA_REPORT.md`](MASS_QA_REPORT.md) as candidate v1.1 work.

---

## Section 2 — Tech stack at a glance

### 2.1. Libraries

| Library | Version | Role | Docs |
|---------|---------|------|------|
| Kotlin | `1.9.21` | Language | https://kotlinlang.org/docs/home.html |
| AGP (Android Gradle Plugin) | `8.2.0` | Build system | https://developer.android.com/build/releases/gradle-plugin |
| Gradle | `8.5` | Build runner | https://docs.gradle.org/8.5/release-notes.html |
| AndroidX Core KTX | `1.12.0` | Kotlin extensions | https://developer.android.com/jetpack/androidx/releases/core |
| AppCompat | `1.6.1` | Backward-compatibility | https://developer.android.com/jetpack/androidx/releases/appcompat |
| Material Components | `1.11.0` | Material 3 widgets | https://github.com/material-components/material-components-android |
| ConstraintLayout | `2.1.4` | Layouts | https://developer.android.com/jetpack/androidx/releases/constraintlayout |
| SwipeRefreshLayout | `1.1.0` | Pull-to-refresh | https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout |
| Fragment KTX | `1.6.2` | Fragment helpers | https://developer.android.com/jetpack/androidx/releases/fragment |
| Activity KTX | `1.8.2` | `viewModels()` for Activity | https://developer.android.com/jetpack/androidx/releases/activity |
| RecyclerView | `1.3.2` | Lists | https://developer.android.com/jetpack/androidx/releases/recyclerview |
| Lifecycle (vm, livedata, runtime) | `2.7.0` | MVVM | https://developer.android.com/jetpack/androidx/releases/lifecycle |
| Navigation Component | `2.7.6` | Single-Activity nav graph + Safe Args | https://developer.android.com/guide/navigation |
| Kotlinx Coroutines | `1.7.3` | Async / structured concurrency | https://kotlinlang.org/docs/coroutines-guide.html |
| Retrofit | `2.9.0` | REST client | https://square.github.io/retrofit/ |
| OkHttp | `4.12.0` | HTTP transport | https://square.github.io/okhttp/ |
| Moshi | `1.15.0` | JSON serialization | https://github.com/square/moshi |
| Timber | `5.0.1` | Logging | https://github.com/JakeWharton/timber |
| MPAndroidChart | `v3.1.0` (JitPack) | Charts | https://github.com/PhilJay/MPAndroidChart |
| JUnit 4 | `4.13.2` | Unit tests | https://junit.org/junit4/ |
| Mockito Core | `5.8.0` | Mocking | https://site.mockito.org/ |
| Mockito-Kotlin | `5.2.1` | Kotlin-friendly Mockito | https://github.com/mockito/mockito-kotlin |
| MockWebServer | `4.12.0` | Fake HTTP server | https://github.com/square/okhttp/tree/master/mockwebserver |
| AndroidX arch core-testing | `2.2.0` | `InstantTaskExecutorRule` | https://developer.android.com/jetpack/androidx/releases/arch-core |
| Coroutines test | `1.7.3` | `runTest`, `TestDispatcher` | https://kotlinlang.org/docs/coroutines-guide.html |

### 2.2. Architectural decisions (locked)

> ⚠️ **Don't change these without team review.** They are baked into the codebase and reflected in tests + onboarding.

| Decision | Choice | Why |
|----------|--------|-----|
| Language | **Kotlin only** | Kotlin everywhere; no Java source. |
| UI toolkit | **XML Views + ViewBinding** | Compose deferred to v1.1 — keeps the student-project complexity manageable. |
| Architecture pattern | **MVVM** | Fragment ↔ ViewModel ↔ Repository ↔ Retrofit. |
| State holders | **LiveData** | Lifecycle-aware out of the box; no need for StateFlow. |
| DI | **Manual via `DataStreamApp`** | No Hilt, no Koin. The container is ~25 lines (`DataStreamApp.kt`). Easier to learn + zero kapt annotation-processing cost. |
| Navigation | **Single-Activity + Navigation Component** | `LoginActivity` → `MainActivity` (host) + 5 fragments via nav graph. |
| Async | **Kotlin Coroutines + LiveData bridge** | `viewModelScope.launch { … }` everywhere. |
| Persistence | **SharedPreferences only** | `TokenManager` + `AppConfig`. No Room. |
| Theming | **Material 3 DayNight** | Brand colours in `colors.xml`; auto-switches with system theme. |
| i18n | **EN (default) + VI (`values-vi/`)** | UI-facing strings translated to Vietnamese. |
| Min / target / compile SDK | **26 / 34 / 34** | Android 8.0 supports 99% of active devices; targetSdk 34 = Play Store mandate 2025. |
| Java compile target | **17** | Source + target. Matches AS-bundled JBR 17. |

---

## Section 3 — Repo tour

### 3.1. Top-level layout

```
DataStream/
├── README.md, SETUP_CHECKLIST.md, TROUBLESHOOTING.md   ← root user docs
├── build.gradle.kts                                    ← root Gradle script (plugins only)
├── settings.gradle.kts                                 ← module list + repos (incl. JitPack)
├── gradle.properties                                   ← Gradle daemon args + proxy template
├── gradle/
│   ├── libs.versions.toml                              ← version catalog (single source of truth)
│   └── wrapper/
│       ├── gradle-wrapper.jar                          ← 43,462 bytes — committed
│       └── gradle-wrapper.properties                   ← pins gradle-8.5-bin.zip
├── gradlew, gradlew.bat                                ← wrapper scripts
├── local.properties.template                           ← template only; real local.properties is gitignored
├── app/                                                ← the only Android module
│   ├── build.gradle.kts                                ← min/target/compileSdk + dependencies
│   ├── proguard-rules.pro                              ← R8 keep rules for release builds
│   └── src/
│       ├── main/                                       ← production code
│       └── test/                                       ← JVM unit tests (15 methods, 3 files)
├── docs/                                               ← all narrative documentation
│   ├── START_HERE.md
│   ├── ANDROID_ONBOARDING.md
│   ├── API_CONTRACT.md
│   ├── ARCHITECTURE.md
│   ├── DEVELOPER_ONBOARDING.md                         ← you're reading this
│   ├── QUICKSTART_FOR_USER.md
│   ├── KICKOFF_AGENDA.md
│   ├── AUDIT_REPORT_ANDROID.md
│   └── MASS_QA_REPORT.md
├── mock/                                               ← json-server mock backend
│   ├── db.json, routes.json, middleware.js
│   ├── package.json (json-server@0.17.4)
│   └── start.ps1, start.sh, README.md
└── examples/responses/                                 ← 14 JSON sample payloads (golden contracts)
```

### 3.2. Single module `app/`

```
app/src/main/
├── AndroidManifest.xml                                 ← 2 activities + INTERNET + ACCESS_NETWORK_STATE
├── java/com/mtoanng/datastream/                        ← 56 .kt files
│   ├── DataStreamApp.kt                                ← Application class, manual DI container
│   ├── data/
│   │   ├── network/                                    ← Retrofit + OkHttp + Moshi setup
│   │   │   ├── ApiService.kt                           ← Retrofit interface (13 endpoints)
│   │   │   ├── NetworkModule.kt                        ← Object building OkHttpClient + Retrofit
│   │   │   ├── AuthInterceptor.kt                      ← Adds Authorization: Bearer header
│   │   │   └── NetworkResult.kt                        ← sealed class<T> { Success | Error | Loading }
│   │   ├── dto/                                        ← 16 Moshi-annotated data classes
│   │   │   ├── LoginRequest, LoginResponse, UserDto
│   │   │   ├── SecurityScoreDto                        ← /api/security/score body
│   │   │   ├── Pillar1..4SupplySecurityDto             ← /api/pillars/N body
│   │   │   ├── AlertDto, RecommendationDto             ← list payloads
│   │   │   ├── AcknowledgeRequest/Response, FuelPriceDto, GridLoadDto
│   │   │   ├── HealthResponse, ErrorResponse
│   │   ├── repository/                                 ← 6 files (5 repos + safeApiCall helper)
│   │   │   ├── Repos.kt                                ← suspend fun safeApiCall<T>
│   │   │   ├── AuthRepository, SecurityRepository
│   │   │   ├── PillarRepository, AlertRepository, RecommendationRepository
│   │   └── prefs/
│   │       ├── TokenManager.kt                         ← JWT + expiry in SharedPrefs
│   │       └── AppConfig.kt                            ← baseUrl + refreshInterval in SharedPrefs
│   ├── ui/
│   │   ├── login/                                      ← LoginActivity + LoginViewModel
│   │   ├── main/                                       ← MainActivity (host for nav graph + bottom nav)
│   │   ├── home/                                       ← HomeFragment + HomeViewModel (ESI gauge + 4 cards)
│   │   ├── pillars/                                    ← PillarsFragment + 4 Pillar*Fragment + PillarsPagerAdapter + PillarsViewModel
│   │   ├── alerts/                                     ← AlertsFragment + AlertsViewModel + AlertsAdapter
│   │   ├── recommendations/                            ← RecommendationsFragment + ViewModel + Adapter
│   │   ├── settings/                                   ← SettingsFragment + SettingsViewModel
│   │   └── common/                                     ← BaseFragment, ViewModelFactory, PillarScoreView, StatusBadge
│   └── util/                                           ← Formatters, Extensions, EnergySecurityHelper
└── res/
    ├── layout/                                         ← 14 XML layouts
    ├── menu/bottom_nav.xml                             ← 5 bottom-nav items
    ├── navigation/nav_graph.xml                        ← 5 fragments + tab-index arg for pillars
    ├── values/strings.xml, colors.xml, themes.xml, dimens.xml, attrs.xml
    ├── values-vi/strings.xml                           ← Vietnamese translations
    ├── values-night/                                   ← dark theme overrides
    ├── drawable/                                       ← 7 vector icons + ic_launcher_foreground
    ├── mipmap-anydpi-v26/                              ← adaptive launcher icons
    └── xml/
        ├── network_security_config.xml                 ← cleartext allow-list for dev backends
        ├── backup_rules.xml, data_extraction_rules.xml
```

### 3.3. Test sources

```
app/src/test/java/com/mtoanng/datastream/
├── util/FormattersTest.kt                              ← 8 cases — pure JVM, no Android
├── data/repository/AuthRepositoryTest.kt               ← 3 cases — MockWebServer
└── ui/login/LoginViewModelTest.kt                      ← 4 cases — Mockito-Kotlin + InstantTaskExecutorRule
```

15 cases total. Run from AS Terminal:

```powershell
.\gradlew :app:testDebugUnitTest
```

### 3.4. Why this layout?

- **Data / UI split**: separates network + persistence from rendering. New devs can change `data/` without touching layouts.
- **Feature folders** (`ui/home/`, `ui/alerts/`, …): each contains Fragment + ViewModel + (optional) Adapter. Each feature is **self-contained**.
- **Common code** in `ui/common/` and `util/`: extracted only when used by ≥ 2 features.

---

## Section 4 — First-day setup

You're an Android dev — assume you have **AS Hedgehog+**, **SDK 34**, and **a Pixel 5 API 34 AVD** already.

```powershell
# 1. Clone
git clone https://github.com/mtoanng/DataStream.git
cd DataStream

# 2. Open in Android Studio — File → Open → DataStream/, Trust Project.

# 3. First Gradle sync (~3–5 min): wait for the green "Gradle sync finished" toast.
#    AS will auto-generate local.properties pointing at your SDK.

# 4. Start mock backend in a separate PowerShell window
cd mock
npm install            # one-time, installs json-server
.\start.ps1            # bind 0.0.0.0:8090

# 5. Pick the Pixel 5 / API 34 AVD in the toolbar → ▶ Run.
```

App lands on `LoginActivity` with `admin` / `admin` pre-filled in the form. Hit **Sign In** → `MainActivity` with bottom nav.

> 💡 **Tip**: The login form's `admin/admin` defaults come from `activity_login.xml` (`android:text="admin"` on the two `TextInputEditText` views). Strip those if you're recording a demo for a real-customer-facing screenshot.

### Login credentials

Default seed (`mock/db.json` users table):

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin` |
| Role | `ADMIN` |

The mock server returns a fake JWT (`"accessToken": "mock-jwt-abc123"`); `TokenManager` stores it in `auth_session.xml` SharedPreferences and `AuthInterceptor` attaches it to every subsequent request.

Server URL can be edited at runtime via the **Settings** tab (saved to `app_config.xml`); no rebuild needed.

---

## Section 5 — Codebase walkthrough by feature: **HomeFragment + ESI gauge**

We'll trace **one full vertical slice** so you internalize the patterns. Pick something else after — every feature follows the same shape.

### 5.1. The flow

```
HomeFragment (user pulls to refresh)
      │
      ▼
HomeViewModel.refresh()                  ← ViewModel exposes LiveData<SecurityScoreDto?>
      │
      ▼
SecurityRepository.getScore()            ← repo wraps API call in safeApiCall { … }
      │
      ▼
ApiService.securityScore()               ← @GET("api/security/score") suspend fun
      │
      ▼
Retrofit + OkHttp                        ← AuthInterceptor injects Bearer token
      │
      ▼
http://10.0.2.2:8090/api/security/score  ← mock or real backend
      │
      ▼ JSON body
SecurityScoreDto                         ← Moshi @JsonClass(generateAdapter = true)
      │
      ▼
NetworkResult.Success<SecurityScoreDto>
      │
      ▼
HomeViewModel._score.value = data        ← LiveData fires
      │
      ▼
HomeFragment observer renders            ← scoreGauge.setScore(…), pillar cards, badges
```

### 5.2. The Fragment — observe LiveData, render Views

```16:62:app/src/main/java/com/mtoanng/datastream/ui/home/HomeFragment.kt
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        binding.cardPillar1.setOnClickListener { goToPillar(0) }
        // … cardPillar2..4 …

        viewModel.score.observe(viewLifecycleOwner) { score ->
            score ?: return@observe
            binding.scoreGauge.setScore(score.overallScore, score.status)
            binding.tvOverallStatus.text = getString(EnergySecurityHelper.statusStringRes(score.status))
            binding.tvOverallStatus.setTextColor(EnergySecurityHelper.statusColor(requireContext(), score.status))
            binding.tvUpdated.text = getString(R.string.label_updated_at, Formatters.isoToLocal(score.computedAt))

            binding.tvP1Score.text = Formatters.score(score.pillar1Score)
            // … P2..P4 …
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) binding.root.snack(msg, isError = true)
        }
    }

    override fun onResume() { super.onResume(); viewModel.startAutoRefresh() }
    override fun onPause()  { super.onPause();  viewModel.stopAutoRefresh()  }
```

**What to notice:**
- Inherits from `BaseFragment` (provides `viewModelFactory` lazily from `DataStreamApp`).
- `_binding` nullable pair + `binding get()` non-null getter — standard ViewBinding leak-protection idiom.
- 3 LiveData observers: `score`, `isLoading`, `error` — strict separation of "data / loading / error".
- Auto-refresh tied to `onResume` / `onPause` so polling stops when fragment isn't visible.

### 5.3. The ViewModel — orchestrate repository + expose LiveData

```15:62:app/src/main/java/com/mtoanng/datastream/ui/home/HomeViewModel.kt
class HomeViewModel(
    private val repo: SecurityRepository,
    private val appConfig: AppConfig,
) : ViewModel() {

    private val _score = MutableLiveData<SecurityScoreDto?>()
    val score: LiveData<SecurityScoreDto?> = _score

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var pollingJob: Job? = null

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.getScore()) {
                is NetworkResult.Success -> { _score.value = r.data; _error.value = null }
                is NetworkResult.Error   -> _error.value = r.message
                NetworkResult.Loading    -> Unit
            }
            _isLoading.value = false
        }
    }

    fun startAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                refresh()
                delay(appConfig.refreshIntervalSeconds * 1000L)
            }
        }
    }
```

**What to notice:**
- Constructor takes **dependencies as parameters** (manual DI). `ViewModelFactory` injects them.
- `Mutable…LiveData` is `private`; only the immutable `LiveData<T>` is exposed. UI **cannot** mutate state.
- `viewModelScope` scoped coroutine cancels automatically when ViewModel is cleared.
- `pollingJob` cancellation in `stopAutoRefresh` + `onCleared` prevents leaks.
- `when` on `NetworkResult` sealed class — exhaustive, no `else` branch.

### 5.4. The Repository — bridge Retrofit to ViewModel

```8:12:app/src/main/java/com/mtoanng/datastream/data/repository/SecurityRepository.kt
class SecurityRepository(private val api: ApiService) {
    suspend fun getScore(): NetworkResult<SecurityScoreDto> = safeApiCall { api.securityScore() }
    suspend fun getCascadeRisks(): NetworkResult<List<Map<String, Any?>>> = safeApiCall { api.cascadeRisks() }
    suspend fun getHealth(): NetworkResult<HealthResponse> = safeApiCall { api.health() }
}
```

Thin pass-through to Retrofit + `safeApiCall`. Future-proofing: when we add a cache layer (Room) we wrap it here.

The `safeApiCall` helper (in `data/repository/Repos.kt`) translates Retrofit `Response<T>` + IOException into `NetworkResult<T>`:

```16:42:app/src/main/java/com/mtoanng/datastream/data/repository/Repos.kt
internal suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
                ?: return NetworkResult.Error(response.code(), "Empty response body")
            NetworkResult.Success(body)
        } else {
            val raw = response.errorBody()?.string()
            val parsed = raw?.let { runCatching {
                NetworkModule.moshi.adapter(ErrorResponse::class.java).fromJson(it)
            }.getOrNull() }
            val msg = parsed?.message ?: response.message().ifBlank { "HTTP ${response.code()}" }
            Timber.w("API error %d: %s", response.code(), msg)
            NetworkResult.Error(response.code(), msg)
        }
    } catch (e: IOException) {
        Timber.e(e, "Network IO failure")
        NetworkResult.Error(message = "Network error: ${e.localizedMessage ?: "no connection"}")
    } catch (e: JsonDataException) { … }
    catch (e: Exception) { … }
}
```

**Why this matters**: every API call goes through one place — error mapping is consistent, snackbars carry the backend's actual `message` field, and ViewModels stay free of `try/catch`.

### 5.5. The API interface — Retrofit declarations

```30:87:app/src/main/java/com/mtoanng/datastream/data/network/ApiService.kt
interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/security/score")
    suspend fun securityScore(): Response<SecurityScoreDto>

    @GET("api/pillars/1/supply-security")
    suspend fun pillar1(): Response<List<Pillar1SupplySecurityDto>>

    @GET("api/alerts/active")
    suspend fun alerts(@Query("limit") limit: Int = 20): Response<List<AlertDto>>

    @POST("api/recommendations/{id}/acknowledge")
    suspend fun acknowledge(
        @Path("id") id: Long,
        @Body body: AcknowledgeRequest,
    ): Response<AcknowledgeResponse>

    // … 8 more endpoints …
}
```

**Why `Response<T>` instead of bare `T`?**
- Lets repositories read **non-2xx body** (401, 5xx) via `response.errorBody()` → user sees the backend's error message.
- Plain `T` would throw `HttpException` for 4xx/5xx, harder to translate into UI strings.

### 5.6. The DTO — Moshi data class

```5:18:app/src/main/java/com/mtoanng/datastream/data/dto/SecurityScoreDto.kt
@JsonClass(generateAdapter = true)
data class SecurityScoreDto(
    val pillar1Score: Double,
    val pillar2Score: Double,
    val pillar3Score: Double,
    val pillar4Score: Double,
    val overallScore: Double,
    val status: String,
    val computedAt: String,
)
```

`@JsonClass(generateAdapter = true)` is a marker only — we don't run Moshi's codegen kapt. Instead, `NetworkModule.moshi` uses `KotlinJsonAdapterFactory` (reflection) — works at runtime, zero build cost. The `@JsonClass` annotation is kept so a future migration to codegen is one-line.

### 5.7. The network module — Retrofit + OkHttp wiring

```20:73:app/src/main/java/com/mtoanng/datastream/data/network/NetworkModule.kt
object NetworkModule {

    private const val TIMEOUT_SECONDS = 20L

    @Volatile private var retrofit: Retrofit? = null
    @Volatile private var currentBaseUrl: String = ""

    val moshi: Moshi by lazy {
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    }

    @Synchronized
    fun apiService(tokenManager: TokenManager, appConfig: AppConfig): ApiService {
        val baseUrl = appConfig.baseUrl
        if (retrofit == null || currentBaseUrl != baseUrl) {
            retrofit = build(baseUrl, tokenManager)
            currentBaseUrl = baseUrl
        }
        return retrofit!!.create(ApiService::class.java)
    }

    /** Force a rebuild — call after the user changes baseUrl in Settings. */
    @Synchronized
    fun recreate(tokenManager: TokenManager, appConfig: AppConfig): ApiService { … }

    private fun build(baseUrl: String, tokenManager: TokenManager): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(HttpLoggingInterceptor().apply { level = if (BuildConfig.DEBUG) BODY else BASIC })
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
    }
}
```

**Key idea — runtime base-URL change**: the `SettingsFragment` lets the user edit base URL. After saving, `app::rebuildApi` is called → `NetworkModule.recreate()` discards the singleton Retrofit and builds a new one. All repositories grab `apiService()` lazily, so they pick up the new client automatically — no app restart.

### 5.8. The DI container — `DataStreamApp`

```20:42:app/src/main/java/com/mtoanng/datastream/DataStreamApp.kt
class DataStreamApp : Application() {

    val tokenManager: TokenManager by lazy { TokenManager.getInstance(this) }
    val appConfig: AppConfig by lazy { AppConfig.getInstance(this) }

    fun apiService(): ApiService = NetworkModule.apiService(tokenManager, appConfig)
    fun rebuildApi(): ApiService = NetworkModule.recreate(tokenManager, appConfig)

    fun authRepository(): AuthRepository = AuthRepository(apiService(), tokenManager)
    fun securityRepository(): SecurityRepository = SecurityRepository(apiService())
    // … pillar / alert / recommendation repositories …

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
```

The whole DI container is 25 lines. `ViewModelFactory` (`ui/common/`) reads from it and constructs ViewModels with their repository deps.

---

## Section 6 — Common dev tasks

### 6.1. Add a new screen

Suppose product asks for a **"Server Health"** screen reachable from Settings.

1. **Create layout** at `app/src/main/res/layout/fragment_server_health.xml`. Use ViewBinding-friendly snake_case IDs.
2. **Create `ServerHealthFragment` + `ServerHealthViewModel`** under `ui/serverhealth/`. Inherit `BaseFragment`; observe LiveData; render.
3. **Register the fragment in the nav graph**: edit `app/src/main/res/navigation/nav_graph.xml`:
   ```xml
   <fragment
       android:id="@+id/serverHealthFragment"
       android:name="com.mtoanng.datastream.ui.serverhealth.ServerHealthFragment"
       android:label="@string/nav_server_health"
       tools:layout="@layout/fragment_server_health"/>
   ```
4. **(If reachable from bottom nav)** add to `res/menu/bottom_nav.xml` **with the same `@+id/serverHealthFragment`** — `NavigationUI.setupWithNavController` wires them by identical id.
5. **Wire into `ViewModelFactory`** (`ui/common/ViewModelFactory.kt`):
   ```kotlin
   modelClass.isAssignableFrom(ServerHealthViewModel::class.java) ->
       ServerHealthViewModel(app.securityRepository()) as T
   ```
6. **Navigate** from another fragment:
   ```kotlin
   findNavController().navigate(R.id.serverHealthFragment)
   ```
   Or with args:
   ```kotlin
   val args = Bundle().apply { putString("tabKey", "uptime") }
   findNavController().navigate(R.id.serverHealthFragment, args)
   ```

> 💡 **Tip**: If you forget step 5, you'll get `IllegalArgumentException: Unknown ViewModel`. Easy to spot in Logcat.

### 6.2. Add a new API endpoint

Suppose backend ships `GET /api/security/forecast`.

1. **Define the DTO** at `data/dto/SecurityForecastDto.kt`:
   ```kotlin
   @JsonClass(generateAdapter = true)
   data class SecurityForecastDto(
       val horizonHours: Int,
       val predictedScore: Double,
       val confidence: Double,
   )
   ```
2. **Add the Retrofit method** in `data/network/ApiService.kt`:
   ```kotlin
   @GET("api/security/forecast")
   suspend fun forecast(@Query("hours") hours: Int = 24): Response<SecurityForecastDto>
   ```
3. **Add repo method** to `SecurityRepository.kt`:
   ```kotlin
   suspend fun getForecast(hours: Int = 24): NetworkResult<SecurityForecastDto> =
       safeApiCall { api.forecast(hours) }
   ```
4. **Use from ViewModel** (e.g. `HomeViewModel`):
   ```kotlin
   viewModelScope.launch {
       when (val r = repo.getForecast()) {
           is NetworkResult.Success -> _forecast.value = r.data
           is NetworkResult.Error   -> _error.value = r.message
           NetworkResult.Loading    -> Unit
       }
   }
   ```
5. **Update mock**: add to `mock/db.json` and `mock/routes.json` so non-network dev still works. Add a golden sample at `examples/responses/15_security_forecast_200.json`.
6. **Write a repository unit test** with MockWebServer (see §7).

### 6.3. Updating the backend contract

The **authoritative source** for endpoints + JSON shapes is:

1. The backend OpenAPI doc at `docs/openapi.json` in the Java repo (https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres).
2. The Java DTO classes (`ves-backend-api/src/main/java/.../dto/`).

**Propagation flow** when backend changes a field:

```
backend Java DTO change
        │
        ▼
update `examples/responses/NN_xxx_200.json` (golden sample)
        │
        ▼
update `mock/db.json` to match new shape
        │
        ▼
update Android DTO in `app/src/main/java/.../data/dto/`
        │
        ▼
update repository tests if shape changed (MockWebServer)
        │
        ▼
sync project + run tests
```

> ⚠️ **Warning**: Don't change Android DTOs from a customer-bug-report alone. Confirm with the backend dev first — typos in mock data have led to "fixes" that broke prod parity.

---

## Section 7 — Testing

### 7.1. Layout

```
app/src/test/java/com/mtoanng/datastream/
├── util/FormattersTest.kt                     ← 8 cases — pure JVM
├── data/repository/AuthRepositoryTest.kt      ← 3 cases — MockWebServer
└── ui/login/LoginViewModelTest.kt             ← 4 cases — Mockito-Kotlin + InstantTaskExecutorRule
```

15 cases. Run with:

```powershell
.\gradlew :app:testDebugUnitTest
# Or with HTML report:
.\gradlew :app:testDebugUnitTest --info
```

Report HTML lands at `app/build/reports/tests/testDebugUnitTest/index.html`.

### 7.2. How to add a new unit test

**For pure logic** (e.g. a new helper in `util/`):

```kotlin
class MyHelperTest {
    @Test
    fun `clamps value to [0,100]`() {
        assertEquals(100.0, MyHelper.clamp(150.0), 0.001)
        assertEquals(0.0, MyHelper.clamp(-5.0), 0.001)
        assertEquals(42.0, MyHelper.clamp(42.0), 0.001)
    }
}
```

**For a repository** (Retrofit-based) — use MockWebServer:

```kotlin
class MyRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var api: ApiService

    @Before
    fun setup() {
        server = MockWebServer().apply { start() }
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun teardown() { server.shutdown() }

    @Test
    fun `returns Success on 200`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"UP"}"""))
        val repo = MyRepository(api)
        val result = repo.getStatus()
        assertTrue(result is NetworkResult.Success)
    }
}
```

**For a ViewModel** — use Mockito-Kotlin + `InstantTaskExecutorRule` + a test dispatcher:

```kotlin
class MyViewModelTest {
    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before fun setup() { Dispatchers.setMain(testDispatcher) }
    @After  fun teardown() { Dispatchers.resetMain() }

    @Test
    fun `loading-success-idle sequence on refresh`() = runTest {
        val repo: MyRepository = mock {
            onBlocking { fetch() } doReturn NetworkResult.Success(MyDto(value = 42))
        }
        val vm = MyViewModel(repo)
        val states = mutableListOf<Boolean>()
        vm.isLoading.observeForever { states.add(it) }

        vm.refresh()
        advanceUntilIdle()

        assertEquals(listOf(false, true, false), states)
    }
}
```

> 💡 **Tip**: `app/build.gradle.kts` sets `testOptions.unitTests.isReturnDefaultValues = true` so calls to Android stubs (e.g. `Log.d`) return defaults instead of throwing. Don't rely on that for production logic — keep platform-free code in `util/`.

### 7.3. What's NOT tested

- **Compose UI tests / Espresso**: deferred, no instrumented tests in the repo yet.
- **End-to-end smoke**: planned via `androidTest/` but blocked on emulator-in-CI.
- **MPAndroidChart rendering**: visual, manually verified during demos.

---

## Section 8 — Build variants + release

### 8.1. Debug build

```powershell
.\gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

`debug` flavor uses `applicationIdSuffix` defaults (none) — same package as release. `isMinifyEnabled = false`. Auto-signs with the AS-generated debug keystore (`~/.android/debug.keystore`).

### 8.2. Release build (shrunk + obfuscated)

```powershell
.\gradlew :app:assembleRelease
# Will FAIL unless a signing config is added.
```

`release` build flavor in `app/build.gradle.kts`:
- `isMinifyEnabled = true` — R8 shrinks + obfuscates.
- `isShrinkResources = true` — drops unused drawables/strings.
- Uses `proguard-rules.pro` on top of AGP's `proguard-android-optimize.txt`.

**To produce an installable release APK**, you need a signing config. Easiest path is via **Build → Generate Signed Bundle / APK**:

1. Build → Generate Signed Bundle / APK → **APK** → **Next**.
2. Click **Create new…** to make a release keystore (`vesmonitor-release.jks`). Save to `~/keystores/` (NOT in repo).
3. Set key alias + passwords. **Document them in a password manager** — losing them = cannot ship updates.
4. Variant: `release` → **Finish**.

APK lands at `app/build/outputs/apk/release/app-release.apk` (~6 MB after R8).

> ⚠️ **Warning**: Never commit keystores or passwords. `.gitignore` already excludes `*.jks`, `*.keystore`, `keystore.properties`.

### 8.3. ProGuard rules to be aware of

Already in `app/proguard-rules.pro`:

- `-keep class com.mtoanng.datastream.data.dto.** { *; }` — keep DTOs (Moshi reflection).
- `-keep class com.github.mikephil.charting.** { *; }` — keep MPAndroidChart.
- `-keep public class com.mtoanng.datastream.ui.common.PillarScoreView` — custom Views inflated from XML.

If R8 strips a class you reference reflectively (e.g. a new custom View), add a `-keep` rule here.

---

## Section 9 — Known limitations & TODOs

| Area | Limitation | Planned |
|------|------------|---------|
| AI-driven recommendations | Backend produces **rule-based recos only**; no LLM. App displays them as-is. | v1.1 — pending backend AI module. |
| Realtime updates | App polls **every 30 s** (`AppConfig.DEFAULT_REFRESH_SECONDS`); no WebSocket. | v1.1 — needs backend `/ws` endpoint. |
| Push notifications (FCM) | Not integrated. Alerts are only visible when app foreground polls. | v1.2 — needs FCM project + backend `/fcm/register`. |
| Offline cache | Only JWT + base URL persisted. List screens show blank on no-network. | v1.1 — Room cache for last successful response. |
| Biometric login | Not implemented. Username + password only. | v1.2 (low priority). |
| Compose migration | XML Views + ViewBinding throughout. Compose is a v2 path. | v2.0. |
| Instrumented (Espresso) tests | None yet. Unit tests cover 3 files. | Mid-v1.1. |
| Multi-language beyond EN/VI | Only `values/` (EN default) + `values-vi/` shipped. | On demand. |
| Tablet / foldable layouts | Pure phone portrait. No `sw600dp/` resources. | v1.2 if needed. |
| App icon variants | Adaptive icon only (`mipmap-anydpi-v26/`). No legacy PNG mipmaps. | Acceptable; backward compatibility via the adaptive icon legacy bitmap. |

The next-step backlog lives in [`docs/MASS_QA_REPORT.md`](MASS_QA_REPORT.md) §11 ("Future improvements").

---

## Section 10 — Resources & contacts

### Internal docs (in this repo)

| Doc | When to read |
|-----|--------------|
| [`README.md`](../README.md) | Always — top-level repo intro. |
| [`SETUP_CHECKLIST.md`](../SETUP_CHECKLIST.md) | Before the first sync. |
| [`TROUBLESHOOTING.md`](../TROUBLESHOOTING.md) | When sync / build / run fails. |
| [`docs/START_HERE.md`](START_HERE.md) | Day-1 — 4-week tour guide. |
| [`docs/ARCHITECTURE.md`](ARCHITECTURE.md) | Before designing a new screen. |
| [`docs/API_CONTRACT.md`](API_CONTRACT.md) | When adding / changing an endpoint. |
| [`docs/ANDROID_ONBOARDING.md`](ANDROID_ONBOARDING.md) | For the original full-fat onboarding (~30 KB). |
| [`docs/QUICKSTART_FOR_USER.md`](QUICKSTART_FOR_USER.md) | When non-dev wants to clone + run. |
| [`docs/AUDIT_REPORT_ANDROID.md`](AUDIT_REPORT_ANDROID.md) | To see what changed across phases. |
| [`docs/MASS_QA_REPORT.md`](MASS_QA_REPORT.md) | The final QA pass + outstanding items. |
| [`mock/README.md`](../mock/README.md) | When working without backend. |
| [`examples/responses/README.md`](../examples/responses/README.md) | Golden JSON contracts. |

### External

| Topic | Link |
|-------|------|
| Android developer guides | https://developer.android.com/docs |
| Kotlin reference | https://kotlinlang.org/docs/reference/ |
| Material 3 components | https://m3.material.io/components |
| ConstraintLayout cheatsheet | https://constraintlayout.com/basics/create_alignments.html |
| Retrofit | https://square.github.io/retrofit/ |
| OkHttp | https://square.github.io/okhttp/ |
| Moshi | https://github.com/square/moshi |
| MPAndroidChart wiki | https://github.com/PhilJay/MPAndroidChart/wiki |
| AGP 8.2 release notes | https://developer.android.com/build/releases/past-releases/agp-8-2-0-release-notes |
| Gradle 8.5 docs | https://docs.gradle.org/8.5/userguide/userguide.html |
| Coroutines guide | https://kotlinlang.org/docs/coroutines-guide.html |

### Backend

- **Repo**: https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres
- **API spec**: `docs/openapi.json` in that repo
- **Postman collection**: `docs/postman/VES-API.postman_collection.json` (in backend repo)

### Contacts

- **Project Owner / Backend Lead**: Leader (group chat for API + JWT issues).
- **Android Maintainer**: you, after reading this doc. 🙂
- **Issue tracker**: https://github.com/mtoanng/DataStream/issues

---

## Appendix A — File-count cheat sheet

Use this to sanity-check after a rebase / merge:

| Item | Expected count | Where |
|------|---------------|-------|
| Kotlin source files | **56** | `app/src/main/java/com/mtoanng/datastream/**/*.kt` |
| DTO files | **16** | `data/dto/*.kt` |
| Test files | **3** | `app/src/test/java/.../**/*.kt` |
| Test methods | **15** | 8 (Formatters) + 3 (AuthRepository) + 4 (LoginViewModel) |
| Layouts | **14** | `res/layout/*.xml` |
| Fragments registered in nav graph | **5** | `res/navigation/nav_graph.xml` |
| Bottom-nav items | **5** | `res/menu/bottom_nav.xml` |
| Vector drawables | **7** | `res/drawable/*.xml` |
| EN string keys | **92** | `res/values/strings.xml` |
| VI string keys | **56** | `res/values-vi/strings.xml` (UI-facing strings only; system / debug strings stay EN) |
| TROUBLESHOOTING scenarios | **20** | `TROUBLESHOOTING.md` |

## Appendix B — Quick PowerShell sanity check

Run these from the repo root after cloning / pulling:

```powershell
# Gradle wrapper jar present + correct size?
(Get-Item gradle/wrapper/gradle-wrapper.jar).Length          # → 43462

# Gradle pinned to 8.5?
Select-String 'distributionUrl' gradle/wrapper/gradle-wrapper.properties
# → distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip

# Kotlin source count?
(Get-ChildItem app\src\main\java -Recurse -Filter *.kt | Measure-Object).Count   # → 56

# Test methods?
Select-String -Path app\src\test\java -Recurse -Pattern "@Test" | Measure-Object   # → 15

# Mock db.json valid?
try { $null = (Get-Content mock\db.json -Raw | ConvertFrom-Json); 'VALID' } catch { "INVALID: $_" }
```

---

**Repo**: https://github.com/mtoanng/DataStream
**This doc**: `docs/DEVELOPER_ONBOARDING.md`
**Last updated**: 2026-05-15 — HEAD `origin/main`
