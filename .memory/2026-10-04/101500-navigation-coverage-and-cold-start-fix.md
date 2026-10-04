# Navigation Coverage & Cold Start Fix

Date: 2026-10-04 10:15:00 (+0700)

## Task

Menutup tiga batasan sisa dari audit navigasi:

1. `hasPreviousDestination()` adalah fungsi identitas dan 3 unit test-nya mustahil gagal.
2. Tombol Batal di Create Target saat dibuka dari Dashboard belum pernah diklik.
3. `Icons.Filled.ArrowBack` deprecated di 6 file.

## Root Cause — two real bugs, bukan cuma test yang lemah

**Bug 1: tombol batal tidak pernah muncul.** `onCreateTarget` di `CukupinNavGraph`
memakai `popUpTo(Routes.DASHBOARD) { inclusive = true }`, jadi Dashboard hilang dari
back stack setiap kali form dibuka. Akibatnya `previousBackStackEntry` bernilai null
di ketiga jalur masuk (start destination, setelah onboarding, dari Dashboard), sehingga
`showBackButton` selalu false dan `onCancel` tidak pernah terpakai. Efeknya lebih luas:
menekan tombol back sistem dari form yang dibuka via Dashboard akan menutup aplikasi,
karena form itu satu-satunya entri back stack.

Perbaikan: `onCreateTarget` tidak lagi mem-pop Dashboard; `onTargetCreated` memakai
`popUpTo(Routes.DASHBOARD) { inclusive = true }` supaya entri Dashboard lama diganti,
bukan ditumpuk.

**Bug 2: ANR cold start.** `CukupinApplication.onCreate()` membangun Room secara
eager. `db.xDao()` memverifikasi kelas hasil generate Room, dan tiap verifikasi
memakan 100-250 ms di emulator. Totalnya melebihi batas startup, sistem menandai ANR
dan proses dibunuh sebelum tes pertama sempat jalan ("Process crashed", 0 test).
Perbaikan: container dibangun dengan `by lazy`.

## File Penting

- `app/src/main/java/com/alamaby/cukupin/ui/navigation/CukupinNavGraph.kt` — popUpTo
  diperbaiki, `hasPreviousDestination` dihapus, `CukupinApp` gains parameter
  `navController` opsional.
- `app/src/main/java/com/alamaby/cukupin/CukupinApplication.kt` — container jadi `by lazy`.
- `app/src/androidTest/java/com/alamaby/cukupin/ui/navigation/CukupinNavigationTest.kt`
  — 5 tes navigasi, baru.
- `gradle/libs.versions.toml`, `app/build.gradle.kts` — dependensi test + versionCode 3,
  versionName "1.2".
- `.github/workflows/ci.yml` — langkah `assembleDebugAndroidTest`.
- `plans/2026-10-04-navigation-coverage-and-rtl-icons-plan.md`.

## Keputusan Teknis

- **`TestNavHostController` tidak dipakai.** Di navigation 2.7.7 controller itu memasang
  `TestNavigatorProvider` yang mengembalikan `TestNavigator` untuk semua nama, sedangkan
  `composable()` mengharapkan `ComposeNavigator` → ClassCastException. Kelasnya juga
  `final`, jadi tidak bisa ditimpa. Test memakai NavController milik Compose, sama seperti
  produksi.
- Konsekuensinya: test hanya bisa memeriksa apa yang terlihat di layar. Invariant
  "back stack tidak memuat dua entri Dashboard setelah menyimpan" dijaga lewat kode dan
  tidak dikunci test.
- **Test dibuktikan bisa gagal.** `popUpTo(DASHBOARD, inclusive)` sengaja dikembalikan
  sebentar: 2 dari 5 tes gagal tepat di `dibukaDariDashboardMenampilkanTombolBatal` dan
  `tombolBatalKembaliKeDashboard`. Setelah dipulihkan, 5/5 hijau.

## Verifikasi

- `testDebugUnitTest` — 14 tes (3 tes tautologis dihapus), 0 failure.
- `lintDebug` — SUCCESSFUL, tidak ada lagi sebutan ArrowBack di laporan.
- `assembleDebug` — SUCCESSFUL.
- `assembleDebugAndroidTest` — SUCCESSFUL.
- `connectedDebugAndroidTest` di emulator `Medium_Phone_API_36.0_Android_16.0` —
  5/5 lulus, 0 failed.
- Mutation check di atas.

## Asumsi / Risiko

> Konfirmasi bahwa ikon `AutoMirrored` benar-benar mirror saat RTL belum diuji: AVD
> pengujian berbahasa Indonesia (LTR). Pembuktiannya hanya dari API, bukan dari tampilan.
>
> `connectedDebugAndroidTest` tidak berjalan di CI karena butuh emulator; CI hanya
> mengompilasi androidTest. Test bisa rusak diam-diam di level runtime sampai ada yang
> menjalankan emulator lokal.

## Open Items

- Warning deprecated `TrendingUp`/`TrendingDown` di `HealthStatusUi.kt` belum dibersihkan.
- Belum ada entry memory untuk troubleshooting `adb offline` dan `INSTALL_FAILED_TEST_ONLY`.

## Proposed Commit

`fix(nav): keep dashboard on the back stack when opening create target`
