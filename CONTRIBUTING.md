# 🤝 Contributing — DataStream

Light contributing guide. The project is an academic / educational repo so the bar is "good enough to be re-used or graded", not "Apache TLP". Read [`TRY_THIS_FIRST.md`](TRY_THIS_FIRST.md) before anything else.

---

## Branch + PR convention

- **Default branch**: `main` (protected — green CI required when CI exists).
- **Branch names**: `feature/<short-slug>`, `fix/<short-slug>`, `chore/<short-slug>`, `docs/<short-slug>`.
- **One PR = one logical change.** Don't bundle unrelated edits.
- **Title**: `<type>(<scope>): <imperative summary>` — same as commits below.
- **Body**: include a 2-3 line summary + screenshots if UI changed.

---

## Commit message style (Conventional Commits, light)

```
<type>(<scope>): <imperative present-tense summary, 50-72 chars>

<optional body — wrap at ~72 chars>
<optional footer — references issues, BREAKING CHANGE notes, etc.>
```

| Type     | When to use |
|----------|-------------|
| `feat`   | New user-visible feature |
| `fix`    | Bug fix |
| `chore`  | Build / tooling / non-source housekeeping |
| `docs`   | Docs only |
| `refactor` | No behaviour change |
| `test`   | Add or fix tests |
| `perf`   | Performance |
| `style`  | Formatting only (no code change) |

**Scope** is the module / area: `app`, `network`, `ui`, `manifest`, `proguard`, `gradle`, `docs`, `mock`, etc.

Examples (all from this repo's git log):

```
fix(android): ProGuard rules + manifest + NSC polish
docs(android): add TRY_THIS_FIRST.md entrance doc
chore(android): pre-flight verify-environment script (PowerShell + Bash)
```

---

## Code style

- **Kotlin**: official Kotlin style (`kotlin.code.style=official` in `gradle.properties`). Run `Code → Reformat Code` (Ctrl+Alt+L) before pushing — `.editorconfig` enforces 4-space indent + LF + UTF-8.
- **Imports**: AS's default — sorted alphabetically, no wildcards.
- **Comments**: explain WHY, not WHAT. Don't narrate obvious code.
- **DTOs**: keep field names matching the backend exactly (Moshi's `@Json` only used when the JSON name diverges from the Kotlin property name — see `data/dto/UserDto.kt` for the convention).
- **Strings**: every user-visible string goes in `res/values/strings.xml` (English) and `res/values-vi/strings.xml` (Vietnamese). No hardcoded strings in Kotlin.

---

## Tests

- New repository / ViewModel? Add a unit test under `app/src/test/...` using JUnit 4 + Mockito-Kotlin (see `LoginViewModelTest.kt` for the canonical shape).
- Network-layer changes? Use `MockWebServer` (already on test classpath via `libs.mockwebserver`).
- Run `:app:test` locally before opening a PR — green tests are the entry bar.

---

## Don't ship

- ❌ `*.keystore` / `*.jks` / `keystore.properties` (covered by `.gitignore`).
- ❌ `local.properties` (each developer has their own SDK path).
- ❌ Secrets in code or in `gradle.properties` (use environment variables or `keystore.properties` injected via Gradle).
- ❌ `mock/node_modules/`, `app/build/`, `.gradle/` — all covered by `.gitignore`.
- ❌ Auto-generated screenshots / videos — store these on the issue tracker, not in git.

---

## Reviewing a PR

If you are reviewing:
1. Pull the PR locally → `git fetch origin pr/<n>/head:pr-<n>; git checkout pr-<n>`.
2. Run `.\scripts\verify-environment.ps1` then sync Gradle.
3. Run `:app:test` and `:app:lint`. Both must be green.
4. Smoke-test on a Pixel 5 / API 34 emulator: login + Home + each tab in `bottom_nav.xml`.
5. Approve when (a) tests + lint green, (b) screenshots match the change, (c) no new top-level dependency was added without discussion.

---

> 📌 Anything not covered here, default to "what would the existing code do?" or open an issue and ask.
