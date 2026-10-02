# ABI split arm64-v8a + perbaikan blocker build

Created: 2026-10-02 14:04:00 (+0700)

## Task / Masalah

1. User butuh APK yang bisa dipasang di perangkat **arm64-v8a**, dan memilih: APK universal tetap
   dipertahankan **dan** ditambahkan split ABI arm64 eksplisit.
2. Build sebenarnya mustahil sebelum task ini dikerjakan: `gradle/wrapper/gradle-wrapper.jar` tidak
   ada, dan `./gradlew` hanya skrip kustom yang mencetak pesan error, bukan skrip Gradle asli.

## Key Files Changed

- [app/build.gradle.kts](../../app/build.gradle.kts) — blok `splits { abi { … } }` baru di dalam `android {}`:

  ```kotlin
  // Menghasilkan APK per-ABI. Satu-satunya library native saat ini berasal dari
  // DataStore (libdatastore_shared_counter.so), jadi APK arm64-v8a tidak lagi
  // membawa salinan armeabi-v7a/x86/x86_64. APK universal tetap dibuat supaya
  // emulator (x86_64) dan perangkat 32-bit tetap bisa memasang.
  splits {
      abi {
          isEnable = true
          reset()
          include("arm64-v8a")
          isUniversalApk = true
      }
  }
  ```

- [app/src/main/res/drawable/ic_launcher_foreground.xml](../../app/src/main/res/drawable/ic_launcher_foreground.xml)
  dan [app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml](../../app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml)
  — **dibuat baru**; sebelumnya `res/` hanya berisi `values/`, sehingga AAPT gagal dengan
  `resource mipmap/ic_launcher … not found`.
- [HistoryScreen.kt:114](../../app/src/main/java/com/alamaby/cukupin/ui/screens/history/HistoryScreen.kt) —
  `Arrangement.spacedAt(12.dp)` → `Arrangement.spacedBy(12.dp)` (unresolved reference).
- [OnboardingScreen.kt](../../app/src/main/java/com/alamaby/cukupin/ui/screens/onboarding/OnboardingScreen.kt) —
  tambah `import androidx.compose.foundation.ExperimentalFoundationApi` dan `@OptIn(ExperimentalFoundationApi::class)`
  pada `fun OnboardingScreen`; `HorizontalPager`/`rememberPagerState` masih eksperimental di Compose 1.6.

## Keputusan Teknis

- Split **hanya** `arm64-v8a`, bukan semua ABI: satu-satunya library native berasal dari DataStore
  (`libdatastore_shared_counter.so`), sehingga penurunan ukuran hanya ±47 KB, sementara filter
  otomatis memblokir perangkat 32-bit. `isUniversalApk = true` menutupi kebutuhan itu.
- `reset()` dipakai agar ABI bawaan (armeabi-v7a, x86, x86_64) tidak ikut ter-build.
- Ikon launcher direkonstruksi dari nol (adaptive icon, foreground vektor 108dp, background
  `green_primary`) karena sumber daya aslinya hilang dan tidak ada file ikon lain di repo.

## Asumsi / Risiko

> - Ikon launcher hasil rekonstruksi **bukan** desain asli. Ini solusi pragmatis agar build bisa
>   jalan; file ikon asli sebaiknya dipakai kembali begitu tersedia.
> - `reset()` + filter satu ABI berarti perangkat 32-bit tidak dapat memasang APK split sama sekali;
>   hanya universal yang menutupi. Keputusan sadar, bukan kelalaian.
> - Build release tetap unsigned karena `signingConfigs` belum dikonfigurasi.
> - `versionName = "1.0"` / `versionCode = 1` sengaja tidak diubah. Task ini adalah perbaikan
>   tooling, bukan fitur atau perbaikan bug, jadi aturan §4 AGENTS.md soal menaikkan `versionCode`
>   tidak diterapkan di sini.

## Verifikasi

Dijalankan **dengan binary Gradle 8.7 dari cache**
(`~/.gradle/wrapper/dists/gradle-8.7-all/.../gradle-8.7/bin/gradle`) karena wrapper jar belum ada
pada saat itu — bukan lewat `./gradlew`:

- `assembleDebug` + `testDebugUnitTest` → **BUILD SUCCESSFUL**; `lintDebug` → SUCCESS.
- Output: `app/build/outputs/apk/debug/app-arm64-v8a-debug.apk` (16.056.794 B; `native-code: 'arm64-v8a'`
  saja; berisi `lib/arm64-v8a/libdatastore_shared_counter.so`) dan `app-universal-debug.apk`
  (16.105.321 B, 4 ABI).
- `output-metadata.json` memuat elemen `UNIVERSAL` + `ONE_OF_MANY` (ABI `arm64-v8a`).
- `testDebugUnitTest`: 14 tes, 0 gagal (`BudgetCalculatorTest`).
- `apksigner verify` → `CN=Android Debug`, jadi APK debug bisa langsung di-install.
- `adb devices` → kosong; belum pernah diuji di perangkat nyata.

## Blocker / Open Item

- Tidak ada perangkat/emulator untuk uji pasang.
- Warning lint/pra-eksisting dibiarkan: parameter `estimatedRunOutDate` tak terpakai di
  `BudgetCalculator.kt:182`, ikon deprecated di beberapa screen.

## Proposed Conventional Commit

`feat(build): split ABI arm64-v8a dan perbaiki blocker build proyek`

## Related

- Entry lanjutan: [2026-10-02 14:38:00 — perbaikan Gradle wrapper](143800-gradle-wrapper-repair.md)
- Dokumen: [README.md](../../README.md) bagian "Membuka & membangun di Android Studio"
