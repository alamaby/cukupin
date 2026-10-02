# Project Memory — Cukupin

_Indeks + current state. Bukan duplikasi activity log; detail tiap task ada di entry-nya._
Format version: 1 (`.memory/YYYY-MM-DD/HHmmss-kebab-case-topic.md`)
Last updated: 2026-10-03 06:22 (+0700)

## Current State

- Aplikasi Android Kotlin/Compose (`com.alamaby.cukupin`), minSdk 26, target/compileSdk 34, JDK 17,
  AGP 8.5.2, Kotlin 1.9.24, Room via KSP, MVVM + DI manual, offline-first, UI berbahasa Indonesia.
- Build `assembleDebug`, `testDebugUnitTest`, dan `lintDebug` **lolos** memakai `./gradlew`
  (Gradle 8.7 dari wrapper, JVM 21.0.9 JBR).
- ABI split aktif: `arm64-v8a` + universal. Debug arm64 = 16.056.794 B, universal = 16.105.321 B.
- `./gradlew` berfungsi penuh; `gradle/wrapper/gradle-wrapper.jar` kini ikut di repo.
- `.gitignore` + `.gitattributes` ada; `build/`, `.gradle/`, `local.properties`, `*.apk`,
  `.freebuff/` tidak lagi muncul sebagai untracked.
- Instruksi agent level repo aktif: `AGENTS.md` (+ penunjuk `CLAUDE.md`), disalin dari
  `~/.claude/CLAUDE.md`, `~/.codex/AGENTS.md`, `~/.config/opencode/AGENTS.md` dan diadaptasi ke
  stack Android/Kotlin.
- `plans/` belum pernah dipakai; `.memory/` baru diinisialisasi 3 Okt 2026.

## Active Decisions

- **ABI split arm64-v8a + universal.** App punya satu library native (`libdatastore_shared_counter.so`
  dari DataStore), jadi split mengurangi ukuran nyata (±47 KB), bukan kosmetik. Universal tetap
  dibuat agar emulator x86_64 dan perangkat 32-bit bisa memasang.
- **JDK 17+ wajib, dibuktikan.** `java` default mesin ini 1.8.0_503 dan membuat build gagal dengan
  pesan menyesatkan `No matching variant ... required '8.7'`. Solusi yang dipakai: `JAVA_HOME` ke JBR
  Android Studio; didokumentasikan di README, belum di-`gradle.properties` (nilai `org.gradle.java.home`
  di sana akan merusak CI Linux).
- **Tanpa trailer `Co-authored-by:`** di commit mana pun, termasuk bot — aturan ini menang atas default
  perkakas.
- **Konvensi berkas instructions = `AGENTS.md` di root**, karena Freebuff (`uiPrefs.injectAgentsMd`),
  Codex, dan OpenCode membacanya dari sana; `CLAUDE.md` hanya penunjuk untuk Claude Code.
- **Tidak memakai DB MCP untuk Room.** Perubahan skema hanya lewat migration file non-destruktif
  (§4 AGENTS.md), tidak pernah ad-hoc ke perangkat.

## Open Items / Blockers

- **Blok kanari masih ada di `AGENTS.md` §12** (`CANARY-UJI-INJEKSI-2026-10-03-A7F2`) — harus dihapus
  setelah uji frekuensi injeksi selesai. Lihat entry 2026-10-03.
- **Tidak ada perangkat/emulator terhubung** (`adb devices` kosong) → `installDebug` dan
  `adb install -r` belum pernah diuji di perangkat nyata. APK sudah diverifikasi signature-nya
  (`CN=Android Debug`).
- **`assembleRelease` masih unsigned** — `signingConfigs` belum dikonfigurasi, `isMinifyEnabled = false`.
- **Perubahan belum di-commit.** `gradlew` sudah ter-stage (mode `100755` + konten resmi); sisanya unstaged.
- **Warning lint/pra-eksisting belum dibersihkan:** `BudgetCalculator.kt:182` parameter
  `estimatedRunOutDate` tidak terpakai; ikon deprecated `ArrowBack` di `AddExpenseScreen.kt:62`,
  `HistoryScreen.kt:67`, `StatisticsScreen.kt:61`; `TrendingUp`/`TrendingDown` di `HealthStatusUi.kt:59,83,99`
  (saran: `Icons.AutoMirrored.Filled.*`).
- **Tidak ada test instrumentation** — `androidTest` baru punya `compose-bom`; belum ada UI test.

## Recent Entries

| Timestamp | Topik |
| --- | --- |
| [2026-10-03 06:18:46](2026-10-03/061846-agent-instructions.md) | `AGENTS.md`/`CLAUDE.md` + uji injeksi aturan |
| [2026-10-02 14:38:00](2026-10-02/143800-gradle-wrapper-repair.md) | Perbaikan Gradle wrapper, `.gitignore`, `.gitattributes`, CI |
| [2026-10-02 14:04:00](2026-10-02/140400-abi-split-arm64.md) | ABI split arm64-v8a + perbaikan blocker build |

## Legacy Archive

Tidak ada `PROJECT_MEMORY.md` — repo ini belum pernah punya memory aktif sebelum 3 Okt 2026.
