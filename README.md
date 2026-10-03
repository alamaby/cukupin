# Cukupin

Aplikasi Android untuk mengatur daya tahan uang: tetapkan dana dan tanggal target,
catat pengeluaran, lalu pantau apakah sisa uang cukup sampai tanggal yang ditentukan.

- **Package:** `com.alamaby.cukupin`
- **Bahasa UI:** Indonesia
- **Teknologi:** Kotlin, Jetpack Compose (Material3), Room, Navigation Compose,
  Coroutines/Flow, DataStore, MVVM
- **Min SDK 26 · Target SDK 34 · Kotlin 1.9 · AGP 8.x**
- **Offline-first**, tanpa akun, satu target aktif dalam satu waktu.

## Membuka & membangun di Android Studio

1. Buka Android Studio → **Open** → pilih folder `cukupin/`.
2. Biarkan Gradle sync selesai (Android Studio memakai `gradle-wrapper.properties`
   untuk mengunduh Gradle 8.7; `gradle-wrapper.jar` sudah disertakan).
3. Pilih konfigurasi **app**, lalu **Run ▶** pada emulator/perangkat (API 26+).

Alternatif via terminal (butuh Android SDK; `ANDROID_HOME` **atau** `local.properties` berisi `sdk.dir`):

> **Butuh JDK 17+.** AGP 8.5 tidak bisa jalan di Java 8, jadi arahkan `JAVA_HOME` ke JDK 17+
> (paling mudah: JBR bawaan Android Studio) sebelum memanggil `./gradlew`:
>
> ```bash
> # Git Bash
> export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
> ```
>
> ```bat
> :: cmd.exe / PowerShell
> set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
> ```
>

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest   # unit test BudgetCalculator
```

APK debug dihasilkan di `app/build/outputs/apk/debug/`:

| Berkas | Keterangan |
| --- | --- |
| `app-arm64-v8a-debug.apk` | khusus perangkat arm64 (mayoritas HP Android) |
| `app-universal-debug.apk` | semua arsitektur (armeabi-v7a, x86/x86_64 untuk emulator) |

Untuk membangun **hanya** arm64-v8a (lebih cepat, cocok untuk HP arm64):

```bash
./gradlew assembleDebug -Pandroid.injected.build.abi=arm64-v8a
```

Properti `android.injected.build.abi` adalah mode build ala Android Studio: hasilnya ditulis ke
`app/build/intermediates/apk/debug/app-arm64-v8a-debug.apk` (AGP menunjuk lokasinya lewat
`app/build/intermediates/apk_ide_redirect_file/debug/createDebugApkListingFileRedirect/redirect.txt`),
**bukan** ke `app/build/outputs/apk/debug/`.

`./gradlew` sudah siap pakai: `gradle/wrapper/gradle-wrapper.jar` disertakan di repo, jadi tidak
perlu memasang Gradle manual. `.gitignore` menjaga `build/`, `.gradle/`, `local.properties`,
`*.apk`/`*.aab`, dan keystore agar tidak ikut ter-commit.

Memasang ke perangkat/emulator yang terhubung:

```bash
./gradlew installDebug
```

Alternatifnya, pakai skrip pembantu yang memasang APK debug ke satu-satunya
perangkat yang terhubung, memaksa flag `-t`, dan menjalankan aplikasinya:

```bash
scripts/install-debug.sh              # pasang saja
scripts/install-debug.sh --launch     # pasang lalu jalankan MainActivity
scripts/install-debug.sh -s emulator-5554 --apk path/ke/apk.apk
```

> **Kenapa flag `-t` wajib?** APK variant `debug` di project ini selalu membawa
> `android:testOnly="true"` pada manifest hasil merge — flag itu di-inject Android
> Gradle Plugin karena `testInstrumentationRunner` aktif di `app/build.gradle.kts`.
> Karena itu `adb install -r` biasa gagal dengan
> `INSTALL_FAILED_TEST_ONLY: Failed to install test-only apk`. Skrip mengirim
> `-t` (izin pasang APK bertanda test) secara otomatis. `./gradlew installDebug`
> tidak pernah bermasalah soal ini karena Gradle sudah menambahkan flag tersebut.

> Satu-satunya library native di aplikasi ini berasal dari DataStore
> (`libdatastore_shared_counter.so`), jadi APK `arm64-v8a` sekitar 47 KB lebih kecil
> daripada versi universal karena tidak menyertakan salinan armeabi-v7a/x86/x86_64.
> Keduanya bisa dipasang di HP arm64; versi universal tetap diperlukan untuk emulator
> (x86_64) dan perangkat 32-bit. `assembleRelease` masih menghasilkan APK **tanpa
> tanda tangan** karena `signingConfigs` belum dikonfigurasi.

## Struktur proyek

```
app/src/main/java/com/alamaby/cukupin/
├── MainActivity.kt                 # Entry point + StartupViewModel
├── CukupinApplication.kt           # Inisialisasi DI manual & Room
├── di/
│   ├── AppContainer.kt             # Manual dependency injection
│   └── ViewModelFactory.kt
├── domain/
│   ├── model/                      # BudgetTarget, ExpenseTransaction,
│   │                               # ExpenseCategory, FundAdjustment, enums,
│   │                               # BudgetCalculationInput, BudgetSummary
│   ├── calculator/
│   │   └── BudgetCalculator.kt     # Murni & deterministik (PRD §49)
│   ├── repository/                 # Kontrak repository
│   ├── usecase/                    # 9 use case (PRD §52)
│   └── time/                       # DateProvider (abstraksi waktu, PRD §53)
├── data/
│   ├── local/                      # Room: entities, DAO, AppDatabase
│   ├── repository/                 # Implementasi repository + mapper
│   └── prefs/                      # OnboardingPreferences (DataStore)
└── ui/
    ├── theme/                      # Color, Type, Theme (Material3)
    ├── navigation/                 # Routes, NavHost, StartupViewModel
    ├── components/                 # HealthStatusCard, StatRow, dll.
    ├── util/                       # formatRupiah, HealthStatusUi, dll.
    └── screens/
        ├── onboarding/             # Ditampilkan sekali (FR-01)
        ├── createtarget/           # Form target + pratinjau (FR-02)
        ├── dashboard/              # Saldo, hari, batas aman, status (FR-03)
        ├── addexpense/             # Tambah & ubah pengeluaran (FR-04/06)
        ├── history/                # Riwayat + hapus + Urungkan (FR-05/07)
        ├── statistics/             # Grafik & insight (FR-08)
        └── summary/                # Ringkasan akhir + ulangi target (FR-10/11)
```

## Aturan bisnis penting

- **Uang = `Long`**, tidak pernah `Float`/`Double` untuk penyimpanan (NFR-03).
- **Tanggal keuangan = `LocalDate`** (inklusif), audit = `Instant` (NFR-04).
- `batasAwal = totalDana / totalHari` (floor); `batasAman = sisaSaldo / sisaHari`.
- Perkalian nominal × hari memakai `BigDecimal` agar aman dari overflow.
- Status kesehatan selalu punya **ikon + teks**, bukan warna saja (FR-09).
- Transaksi di luar periode **ditolak dengan pesan jelas** (PRD §50),
  tidak disimpan diam-diam.
- Hapus transaksi: konfirmasi + snackbar **Urungkan** (FR-07).

## Pengujian

Unit test kalkulator: `app/src/test/.../domain/calculator/BudgetCalculatorTest.kt`
mencakup skenario PRD §56 (normal, terlalu cepat, saldo negatif, belum mulai,
selesai) plus kasus tepi (overflow, pembulatan ke bawah, periode 1 hari).
