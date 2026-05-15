# 🎨 Excalidraw Architecture Diagrams — Android (DataStream)

Hand-editable architecture diagrams for the **DataStream** Android client (MVVM, Retrofit, JWT). Each `.excalidraw` file is plain JSON — open it at [excalidraw.com](https://excalidraw.com) (File → Open) or with the VS Code / Cursor extension `pomdtr.excalidraw-editor`.

These diagrams complement the markdown docs in `../../docs/` (`ARCHITECTURE.md`, `API_CONTRACT.md`, etc.) — markdown for diff-friendly source, Excalidraw for whiteboard-style edits.

## Files

| # | File | Description |
|---|---|---|
| 1 | [`01_app_architecture.excalidraw`](./01_app_architecture.excalidraw) | 3-tier MVVM stack: Activities + Fragments → ViewModels (LiveData) → Repositories (5) → ApiService / OkHttp / Moshi → backend. Plus the manual DI container (`DataStreamApp`). |
| 2 | [`02_screen_flow.excalidraw`](./02_screen_flow.excalidraw) | User navigation: LoginActivity → MainActivity (single-activity host) → 5 BottomNav tabs (Home, Pillars, Alerts, Recommendations, Settings) → detail screens (BottomSheet, AlertDialog, ViewPager2 pages) → logout back to Login. |
| 3 | [`03_data_flow_android.excalidraw`](./03_data_flow_android.excalidraw) | End-to-end sequence for one network call (HomeFragment loading the security score): Fragment.onResume → ViewModel coroutine → Repository → Retrofit → OkHttp + AuthInterceptor → backend → Moshi parse → LiveData → render. Includes the 30 s polling loop. |
| 4 | [`04_module_structure.excalidraw`](./04_module_structure.excalidraw) | Folder tree of the project: root, `app/` module sources (`data/`, `ui/`, `util/`), resources (layouts, navigation, network security), `mock/` (json-server), `examples/`, `docs/`, gradle wrapper. |
| 5 | [`05_network_security.excalidraw`](./05_network_security.excalidraw) | Authentication flow (login → JWT → SharedPreferences → AuthInterceptor on every request → JwtAuthFilter on backend → 200 vs 401 branches), `network_security_config.xml` cleartext domains, baseUrl options (emulator / real device / mock). |

## How to open / edit

### Web (no install)

1. Go to <https://excalidraw.com>
2. Menu → **File → Open** → pick any `.excalidraw` file from this folder.
3. Edit, then **File → Save As** to overwrite the same file.

### VS Code / Cursor extension

1. Install [`pomdtr.excalidraw-editor`](https://marketplace.visualstudio.com/items?itemName=pomdtr.excalidraw-editor).
2. Click any `.excalidraw` file — it renders inline.
3. Edits save back to the file automatically.

### Export to PNG / SVG

Inside Excalidraw: menu → **Export image…** → PNG / SVG. Recommended `2x` scale for slides.

## Style key

- **Blue** — UI / Activities / Fragments / inbound data
- **Purple** — ViewModels / coroutines / Application class
- **Teal** — Repositories / data layer / OK responses / SharedPreferences
- **Orange** — Network layer / Retrofit / OkHttp / backend interactions
- **Pink** — Errors / 401 / interrupt flows
- **Gray (dashed)** — DI links / external boundaries / parent-folder edges in tree

## Caveats

These diagrams are **best-effort accurate** as of commit `d2f31a6` on `origin/main`. The source of truth is always the code:

- App / DI → `app/src/main/java/com/mtoanng/datastream/DataStreamApp.kt`
- Repos & DTOs → `app/src/main/java/com/mtoanng/datastream/data/`
- ViewModels & screens → `app/src/main/java/com/mtoanng/datastream/ui/`
- Navigation graph → `app/src/main/res/navigation/nav_graph.xml`
- Network security → `app/src/main/res/xml/network_security_config.xml`

If the code drifts, please re-edit these `.excalidraw` files directly — they are the artefact, not generated.
