# 🏛️ Architecture — VES-Monitor Mobile

> Hướng dẫn kiến trúc + file structure + Gradle deps **khuyến nghị** cho intermediate Android dev. Bạn có thể tự chọn alternative — đây là baseline để báo cáo Chương 5 môn Android.

---

## 1. Tổng quan kiến trúc

**Pattern: MVVM single-module** + Repository + Coroutines.

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer                            │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐         │
│  │ Activity / │  │  Fragment  │  │   Adapter  │         │
│  │  Compose?  │  │            │  │            │         │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘         │
│        └─────── observes ──┴────────────┘                │
│                            │                             │
│                     LiveData / StateFlow                 │
│                            │                             │
│  ┌─────────────────────────▼─────────────────────────┐  │
│  │             ViewModel (per screen)                │  │
│  │  • Hold UI state (loading/success/error)          │  │
│  │  • Expose methods triggered by UI                 │  │
│  │  • Calls Repository                               │  │
│  └─────────────────────────┬─────────────────────────┘  │
└────────────────────────────┼────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                    Data Layer                            │
│  ┌─────────────────────────────────────────────────┐    │
│  │              Repository (per domain)            │    │
│  │  • Chọn datasource (API / cache / mock)         │    │
│  │  • Map DTO ↔ domain model                       │    │
│  │  • Error handling (network / parse)             │    │
│  └────────────────┬───────────────────┬────────────┘    │
│                   │                   │                 │
│       ┌───────────▼─────────┐ ┌───────▼──────────┐      │
│       │  Retrofit ApiService │ │ SharedPreferences│      │
│       │  (14 endpoint)       │ │ (JWT + settings) │      │
│       └───────────┬─────────┘ └───────┬──────────┘      │
└───────────────────┼─────────────────────────────────────┘
                    │
                    │ HTTP + JWT Bearer
                    ▼
              REST API Backend (port 8090)
```

### Tại sao MVVM thay vì MVI / Clean Arch?

| | MVVM | MVI | Clean Arch |
|---|------|-----|------------|
| Học cost | Thấp | Trung bình | Cao |
| Code lines cho app 7 màn | ~2000 | ~3000 | ~5000 |
| Phù hợp đồ án sinh viên | ✅ | 🟡 | ❌ overkill |
| Đủ điểm "có kiến trúc rõ ràng" | ✅ | ✅ | ✅ |

→ MVVM **rõ ràng + đơn giản + đủ điểm**.

---

## 2. File structure đầy đủ

```
DataStream/
├── app/                                  # Module Android (sẽ tạo bằng AS)
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   │
│       │   ├── kotlin/vn/edu/ves/mobile/
│       │   │   ├── VesMonitorApp.kt          # Application class
│       │   │   │                              # • Init Timber, ApiClient, prefs
│       │   │   │
│       │   │   ├── data/
│       │   │   │   ├── api/
│       │   │   │   │   ├── ApiClient.kt      # Retrofit singleton + dynamic BASE_URL
│       │   │   │   │   ├── ApiService.kt     # @GET/@POST interface (14 endpoint)
│       │   │   │   │   ├── AuthInterceptor.kt  # Inject Bearer header
│       │   │   │   │   └── ErrorInterceptor.kt # Map 401 → trigger logout
│       │   │   │   │
│       │   │   │   ├── dto/                  # Match backend DTO (camelCase)
│       │   │   │   │   ├── auth/
│       │   │   │   │   │   ├── LoginRequest.kt, LoginResponse.kt
│       │   │   │   │   │   └── UserDto.kt
│       │   │   │   │   ├── security/
│       │   │   │   │   │   ├── SecurityScoreDto.kt, CascadeRiskDto.kt
│       │   │   │   │   ├── pillar/
│       │   │   │   │   │   ├── Pillar1OutlookDto.kt ... Pillar4NetZeroDto.kt
│       │   │   │   │   ├── alert/
│       │   │   │   │   │   └── AlertDto.kt
│       │   │   │   │   ├── recommendation/
│       │   │   │   │   │   ├── RecommendationDto.kt, AckRequest.kt
│       │   │   │   │   └── raw/
│       │   │   │   │       ├── FuelPriceDto.kt, GridLoadLatestDto.kt
│       │   │   │   │
│       │   │   │   ├── prefs/
│       │   │   │   │   └── SessionManager.kt  # SharedPreferences wrapper
│       │   │   │   │                          # • saveToken(), getToken()
│       │   │   │   │                          # • saveBaseUrl(), getBaseUrl()
│       │   │   │   │                          # • clear() on logout
│       │   │   │   │
│       │   │   │   └── repo/
│       │   │   │       ├── AuthRepository.kt
│       │   │   │       ├── SecurityRepository.kt
│       │   │   │       ├── PillarRepository.kt
│       │   │   │       ├── AlertRepository.kt
│       │   │   │       └── RecommendationRepository.kt
│       │   │   │
│       │   │   ├── ui/
│       │   │   │   ├── splash/
│       │   │   │   │   └── SplashActivity.kt
│       │   │   │   ├── login/
│       │   │   │   │   ├── LoginActivity.kt
│       │   │   │   │   └── LoginViewModel.kt
│       │   │   │   ├── main/
│       │   │   │   │   ├── MainActivity.kt   # Host bottom-nav + toolbar
│       │   │   │   │   └── MainViewModel.kt
│       │   │   │   ├── home/
│       │   │   │   │   ├── HomeFragment.kt   # 4 KPI card + Security Score gauge
│       │   │   │   │   └── HomeViewModel.kt
│       │   │   │   ├── pillar/
│       │   │   │   │   ├── Pillar1Fragment.kt + Pillar1ViewModel.kt
│       │   │   │   │   ├── Pillar2Fragment.kt + Pillar2ViewModel.kt
│       │   │   │   │   ├── Pillar3Fragment.kt + Pillar3ViewModel.kt
│       │   │   │   │   └── Pillar4Fragment.kt + Pillar4ViewModel.kt
│       │   │   │   ├── alerts/
│       │   │   │   │   ├── AlertListFragment.kt
│       │   │   │   │   ├── AlertAdapter.kt
│       │   │   │   │   └── AlertViewModel.kt
│       │   │   │   ├── recommendations/
│       │   │   │   │   ├── RecListFragment.kt
│       │   │   │   │   ├── RecAdapter.kt
│       │   │   │   │   └── RecViewModel.kt
│       │   │   │   ├── settings/
│       │   │   │   │   └── SettingsActivity.kt
│       │   │   │   └── about/
│       │   │   │       └── AboutActivity.kt
│       │   │   │
│       │   │   └── util/
│       │   │       ├── Result.kt              # sealed class Loading/Success/Error
│       │   │       ├── DateFormat.kt          # ISO 8601 ↔ user-friendly
│       │   │       ├── Extensions.kt          # View.gone(), View.visible(), etc.
│       │   │       └── Constants.kt           # PREF_KEYS, INTENT_KEYS
│       │   │
│       │   └── res/
│       │       ├── layout/
│       │       │   ├── activity_splash.xml
│       │       │   ├── activity_login.xml
│       │       │   ├── activity_main.xml      # Bottom-nav host
│       │       │   ├── activity_settings.xml
│       │       │   ├── activity_about.xml
│       │       │   ├── fragment_home.xml
│       │       │   ├── fragment_pillar1.xml ... fragment_pillar4.xml
│       │       │   ├── fragment_alerts.xml
│       │       │   ├── fragment_recommendations.xml
│       │       │   ├── item_alert.xml          # RecyclerView item
│       │       │   ├── item_recommendation.xml
│       │       │   └── item_kpi_card.xml
│       │       ├── values/
│       │       │   ├── strings.xml             # Vietnamese (default)
│       │       │   ├── colors.xml              # 4 pillar colors + status colors
│       │       │   ├── themes.xml              # Material 3 light
│       │       │   ├── dimens.xml
│       │       │   └── styles.xml
│       │       ├── values-en/strings.xml       # English fallback
│       │       ├── values-night/themes.xml     # Material 3 dark
│       │       ├── drawable/                   # icons + splash + backgrounds
│       │       ├── mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher.png
│       │       └── menu/
│       │           ├── bottom_nav.xml          # 5 tab
│       │           └── toolbar_main.xml        # About, Settings, Logout
│       │
│       ├── test/                              # Unit tests
│       │   └── kotlin/.../
│       │       ├── ViewModelTests/
│       │       └── RepositoryTests/
│       └── androidTest/                       # Instrumented tests
│           └── kotlin/.../
│
├── build.gradle.kts                            # Root
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/
├── docs/                                       # Repo này đã có
├── mock/                                       # Repo này đã có
└── examples/                                   # Repo này đã có
```

---

## 3. Gradle dependencies (`app/build.gradle.kts`)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "vn.edu.ves.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.edu.ves.mobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // bật cho production thật
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // ===== Core Android =====
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // ===== Lifecycle =====
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // ===== Navigation =====
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // ===== Network =====
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ===== Coroutines =====
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ===== Chart =====
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // ===== Logging =====
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ===== Test =====
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

**Root `settings.gradle.kts`**:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")  // BẮT BUỘC cho MPAndroidChart
    }
}

rootProject.name = "VES Monitor Mobile"
include(":app")
```

---

## 4. Code skeleton tham khảo

### 4.1 ApiClient.kt — Retrofit singleton với dynamic BASE_URL

```kotlin
object ApiClient {
    @Volatile private var instance: ApiService? = null
    @Volatile private var currentBaseUrl: String = "http://10.0.2.2:8090"

    fun get(sessionManager: SessionManager): ApiService {
        val baseUrl = sessionManager.getBaseUrl() ?: currentBaseUrl
        if (instance == null || baseUrl != currentBaseUrl) {
            synchronized(this) {
                currentBaseUrl = baseUrl
                instance = buildRetrofit(baseUrl, sessionManager).create(ApiService::class.java)
            }
        }
        return instance!!
    }

    private fun buildRetrofit(baseUrl: String, sm: SessionManager): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sm))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                        else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

### 4.2 ApiService.kt — Retrofit interface

```kotlin
interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @GET("api/security/score")
    suspend fun getSecurityScore(): Response<SecurityScoreDto>

    @GET("api/security/cascade-risks")
    suspend fun getCascadeRisks(): Response<List<CascadeRiskDto>>

    @GET("api/pillars/1/outlook")
    suspend fun getPillar1Outlook(): Response<List<Pillar1OutlookDto>>

    @GET("api/pillars/2/volatility")
    suspend fun getPillar2Volatility(): Response<List<Pillar2VolatilityDto>>

    @GET("api/pillars/3/shedding-plan")
    suspend fun getPillar3Shedding(): Response<Pillar3SheddingDto>

    @GET("api/pillars/4/net-zero-progress")
    suspend fun getPillar4NetZero(): Response<Pillar4NetZeroDto>

    @GET("api/alerts/active")
    suspend fun getActiveAlerts(): Response<List<AlertDto>>

    @GET("api/recommendations")
    suspend fun getRecommendations(
        @Query("status") status: String = "PENDING"
    ): Response<List<RecommendationDto>>

    @POST("api/recommendations/{id}/acknowledge")
    suspend fun acknowledgeRecommendation(
        @Path("id") id: Long,
        @Body req: AckRequest
    ): Response<RecommendationDto>

    @GET("api/raw/fuel-prices/latest")
    suspend fun getLatestFuelPrices(@Query("limit") limit: Int = 50): Response<List<FuelPriceDto>>

    @GET("api/raw/grid-load/latest")
    suspend fun getLatestGridLoad(@Query("region") region: String? = null): Response<List<GridLoadLatestDto>>

    @GET("api/health")
    suspend fun health(): Response<HealthDto>
}
```

### 4.3 AuthInterceptor.kt — inject Bearer header

```kotlin
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = sessionManager.getToken()

        // Skip auth header cho login + health (public endpoints)
        val path = original.url.encodedPath
        if (token.isNullOrBlank() || path.endsWith("/auth/login") || path.endsWith("/health")) {
            return chain.proceed(original)
        }

        val withAuth = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(withAuth)
    }
}
```

### 4.4 Result.kt — sealed class cho UI state

```kotlin
sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
}
```

### 4.5 PillarRepository.kt — example

```kotlin
class PillarRepository(private val api: ApiService) {

    suspend fun getPillar1Outlook(): Result<List<Pillar1OutlookDto>> = try {
        val resp = api.getPillar1Outlook()
        if (resp.isSuccessful && resp.body() != null) {
            Result.Success(resp.body()!!)
        } else {
            Result.Error("HTTP ${resp.code()}: ${resp.message()}", resp.code())
        }
    } catch (e: Exception) {
        Timber.e(e, "getPillar1Outlook failed")
        Result.Error("Network error: ${e.message}")
    }

    // Tương tự cho pillar 2/3/4
}
```

### 4.6 Pillar1ViewModel.kt — example

```kotlin
class Pillar1ViewModel(private val repo: PillarRepository) : ViewModel() {

    private val _state = MutableLiveData<Result<List<Pillar1OutlookDto>>>(Result.Loading)
    val state: LiveData<Result<List<Pillar1OutlookDto>>> = _state

    fun load() {
        _state.value = Result.Loading
        viewModelScope.launch {
            _state.value = repo.getPillar1Outlook()
        }
    }
}
```

### 4.7 Pillar1Fragment.kt — observe pattern

```kotlin
class Pillar1Fragment : Fragment(R.layout.fragment_pillar1) {

    private val vm: Pillar1ViewModel by viewModels {
        ViewModelFactory(PillarRepository(ApiClient.get(SessionManager(requireContext()))))
    }
    private var _binding: FragmentPillar1Binding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPillar1Binding.bind(view)

        binding.swipeRefresh.setOnRefreshListener { vm.load() }

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                Result.Loading -> binding.progressBar.visible()
                is Result.Success -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
                    renderData(state.data)
                }
                is Result.Error -> {
                    binding.progressBar.gone()
                    binding.swipeRefresh.isRefreshing = false
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
        vm.load()
    }

    private fun renderData(data: List<Pillar1OutlookDto>) {
        // Render table + chart MPAndroidChart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

## 5. Testing strategy (đủ điểm Chương 6)

| Loại test | Số lượng mục tiêu | Ví dụ |
|-----------|--------------------|-------|
| **Unit ViewModel** | 4-6 file | Mock Repository → verify state transitions Loading → Success / Error |
| **Unit Repository** | 2-3 file | Mock ApiService → verify map response, handle 4xx/5xx |
| **Unit Util** | 1-2 file | DateFormat parsing, SessionManager save/load |
| **Instrumented UI** | 1-2 file (optional) | Espresso click login button, assert MainActivity launched |

Tổng: 8-12 test file, đủ "có Unit Test" theo yêu cầu môn.

---

## 6. Performance tips

- **Lazy load fragments** trong bottom-nav → chỉ load data khi user vào tab (không preload hết 4 pillar)
- **Cache JWT** trong memory, không read SharedPreferences mỗi request (OkHttp interceptor đã đảm bảo)
- **Image** không tải về (không có ảnh từ backend), nếu có icon thì dùng vector drawable
- **MPAndroidChart**: dùng `LineDataSet.setDrawCircles(false)` cho chart có > 100 point để fluid scroll
- **RecyclerView**: dùng `DiffUtil` thay vì `notifyDataSetChanged()` khi list refresh

---

## 7. UI/UX guidelines

### Color palette (Material 3)

| Token | Light | Dark | Dùng cho |
|-------|-------|------|----------|
| Primary | `#1F77B4` | `#90CAF9` | App bar, primary button, brand |
| Secondary | `#FF6F00` | `#FFB74D` | FAB, accent |
| Surface | `#FFFFFF` | `#121212` | Card, background |
| Pillar 1 (vàng) | `#FFE599` | `#7F6000` | Tab supply |
| Pillar 2 (xanh) | `#CFE2F3` | `#0B5394` | Tab prices |
| Pillar 3 (cam) | `#FCE5CD` | `#B45F06` | Tab grid |
| Pillar 4 (xanh lá) | `#D9EAD3` | `#38761D` | Tab renewable |
| Success | `#38761D` | `#81C784` | SECURE status |
| Warning | `#B45F06` | `#FFB74D` | WARNING status |
| Critical | `#CC0000` | `#EF5350` | CRITICAL status |

### Typography (Material 3)

- **Display Large**: 57sp (Security Score gauge)
- **Headline Medium**: 28sp (Screen title)
- **Title Large**: 22sp (Card title)
- **Body Large**: 16sp (default text)
- **Label Medium**: 12sp (chip, badge)

### Spacing

- Margins: 8/16/24dp
- Padding: 8/16dp
- Card elevation: 2dp

---

## 8. Logging & debugging

```kotlin
// Trong VesMonitorApp.kt
override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
        Timber.plant(Timber.DebugTree())
    }
}

// Trong code
Timber.d("Loading pillar 1 data...")
Timber.e(e, "API call failed for endpoint /pillars/1")
```

**Logcat filter** trong Android Studio:
- Tag: `Timber` → xem all log app
- Level: Verbose → để debug deep

**OkHttp logging** đã được enable trong `ApiClient.kt` (chỉ DEBUG build) → mỗi request/response in raw ra logcat.

---

## 9. Build release APK

```bash
# Debug build (mặc định)
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release build (chưa minify, không signed)
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release-unsigned.apk

# Signed release (cần keystore):
# 1. Tạo keystore: keytool -genkey -v -keystore ves.jks ...
# 2. Set keystore.properties (KHÔNG commit)
# 3. ./gradlew assembleRelease
```

Cho đồ án, **app-debug.apk** là đủ — upload vào GitHub Release để giảng viên cài.

---

## 10. Khi cần thay đổi kiến trúc

Nếu sau khi bắt đầu code thấy MVVM cản trở (vd muốn dùng Compose), bạn có thể migrate, nhưng:
- Báo Leader để cập nhật báo cáo
- Migration làm trên branch riêng, không break main
- Doc lại trong Chương 5 báo cáo "tại sao chọn migrate"

---

> 📌 Khi follow file này, bạn đảm bảo có:
> - ✅ MVVM rõ ràng (Chương 5 báo cáo)
> - ✅ Separation of concerns (UI / Repository / API)
> - ✅ Unit testability (mock Repository / ApiService dễ)
> - ✅ Maintainability (mỗi screen folder riêng)
> - ✅ Đủ điểm phần "Kiến trúc + công nghệ" của môn
