# Navigation Test Coverage & RTL Icon Plan

Created: 2026-10-04 09:20:00

## Objective
Menutup tiga batasan yang tersisa dari audit navigasi + lenderan ikon back:

1. `hasPreviousDestination()` adalah fungsi identitas dan 3 unit test-nya secara
   teknis mustahil gagal — rasa aman semu, bukan jaminan.
2. Tombol Batal di Create Target saat dibuka dari Dashboard belum pernah diklik
   di emulator.
3. `Icons.Filled.ArrowBack` deprecated di 6 file (ikon tidak otomatis mirror di
   locale RTL).

## Scope
- Menghapus fungsi identitas + unit test yang menggantung padanya.
- Menambah androidTest nyata berbasis `TestNavHostController` + Compose test rule
  yang benar-benar merender NavHost dan memeriksa ikon navigasi per jalur masuk.
- Migrasi 6 pemanggilan `Icons.Filled.ArrowBack` ke `Icons.AutoMirrored.Filled.ArrowBack`.
- Menutup kasus Batal-dari-Dashboard lewat UI di emulator.
- Menambah langkah CI yang meng-*compile* androidTest supaya instrumentation test tidak
  bisa rusak diam-diam tanpa pernah dikompilasi.

Di luar scope:
- Menjalankan emulator di CI (butuh image ~1 GB + job jauh lebih lambat).
- Menambah job `reactivecircus/android-emulator-runner`.
- Menambah CI database Room untuk test yang menyentuh data.

## Milestones
1. Plan & konteks build.
2. Migrasi ikon ke AutoMirrored.
3. Ganti test tautologis dengan androidTest nyata.
4. Verifikasi lokal (unit, lint, assemble, androidTest di emulator, UI check).
5. Dokumentasi memory + commit.

## Tasks
- [x] M1: Plan file dibuat, build config & CI yaml dibaca.
- [x] M2: Migrasi `Icons.Filled.ArrowBack` → `Icons.AutoMirrored.Filled.ArrowBack`
      di AboutScreen, AddExpenseScreen, CreateTargetScreen, HistoryScreen,
      StatisticsScreen, TargetSummaryScreen.
- [x] M3: Hapus `hasPreviousDestination` dari CukupinNavGraph.kt dan
      hapus `CreateTargetNavigationTest.kt` di `src/test`.
- [x] M4: Tambah dependensi `androidx.navigation:navigation-testing` +
      `compose ui-test-junit4` + `ui-test-manifest` (debugImplementation).
- [x] M5: Tulis androidTest yang merender NavHost sungguhan dan memeriksa ikon
      Batal pada tiga jalur masuk.
- [x] M6: Jalankan `testDebugUnitTest`, `lintDebug`, `assembleDebug`,
      `assembleDebugAndroidTest`.
- [x] M7: Jalankan `connectedDebugAndroidTest` di emulator — 5/5 lulus.
- [x] M8 (revisi): verifikasi klik Batal dari Dashboard. Bukan lewat uiautomator
      manual, tapi lewat instrumentation test yang benar-benar mengklik node
      `contentDescription = "Batal"` lalu memastikan Dashboard muncul lagi.
- [x] M9: Tambah langkah compile androidTest di `.github/workflows/ci.yml`.
- [x] M10: Entry `.memory/` + indeks README.

## Perubahan Scope di Tengah Pengerjaan

- **M5 berubah rancangan.** Rencana semula memakai `TestNavHostController`. Kenyataannya
  di navigation 2.7.7 controller itu tidak bisa dipakai bersama `composable()`:
  `TestNavigatorProvider` mengembalikan `TestNavigator` untuk semua nama navigator,
  sedangkan `composable()` mengharapkan `ComposeNavigator` → `ClassCastException`
  pada composite destination. Kelasnya `final`, jadi tidak bisa ditimpa. Test memakai
  NavController Compose biasa, sama seperti produksi.
- **Scope bertambah: dua bug nyata ditemukan saat menulis test.**
  1. Tombol batal tidak pernah muncul karena `popUpTo(DASHBOARD, inclusive)` di
     `onCreateTarget`. Ketiga jalur masuk menghasilkan `previousBackStackEntry == null`.
  2. `CukupinApplication.onCreate()` membangun Room secara eager dan memicu ANR cold
     start — proses dibunuh sebelum tes pertama jalan.
- **M8 replaced** oleh test otomatis; dump uiautomator manual tidak lagi dibutuhkan.

## Risks
- `compose ui-test-junit4` belum ada di Gradle cache, jadi butuh unduhan. Bila
  jaringan repo Google diblokir, langkah M4-M5 gagal dan harus dikembalikan ke
  kondisi sebelum.
- Emulator host pernah mati sendiri karena RAM habis. Kalau `connectedDebugAndroidTest`
  gagal karena device `offline`, itu bukan bukti test salah — harus dicatat sebagai
  keterbatasan, bukan dibungkus jadi "skip".
- `TestNavHostController` ikut me-reset state ViewModel antar destination, jadi
  test tidak boleh bergantung pada data Room nyata.
- Memindahkan ke `AutoMirrored` mengubah orientasi ikon di RTL. AVD pengujian
  berbahasa Indonesia (LTR) tidak bisa memverifikasi itu; pembuktiannya hanya
  dari API (`AutoMirrored` + `startAutomaticallyMirrored`).

## Progress Log
- 2026-10-04 09:20:00 — Plan dibuat. Emulator dinyalakan di background (PID 36196,
  AVD `Medium_Phone_API_36.0_Android_16.0`) karena boot lama dan RAM host kini
  6.7 GB bebas (sebelumnya 0,1-0,64 GB yang jadi akar cause `adb offline`).
- 2026-10-04 09:35:00 — M2 selesai. Laporan lint tidak lagi menyebut ArrowBack.
- 2026-10-04 09:50:00 — M3 selesai. `hasPreviousDestination` dihapus; test tautologis
  di `src/test` dihapus (total unit test turun dari 17 ke 14).
- 2026-10-04 10:00:00 — M4-M5 selesai, tapi `TestNavHostController` tidak kompatibel
  dengan `composable()` di 2.7.7 (ClassCastException). Rancangan test diubah ke
  NavController Compose biasa.
- 2026-10-04 10:05:00 — `connectedDebugAndroidTest` gagal "Process crashed" dengan 0
  test. Akar cause dari logcat: `ANR in com.alamaby.cukupin — failed to complete
  startup`, Building Room di `onCreate()` membengkak jadi `by lazy`.
- 2026-10-04 10:10:00 — 5/5 tes instrumentation lulus di emulator.
- 2026-10-04 10:12:00 — Mutation check: `popUpTo(DASHBOARD, inclusive)` dikembalikan
  sementara; 2 dari 5 tes gagal tepat di `dibukaDariDashboardMenampilkanTombolBatal`
  dan `tombolBatalKembaliKeDashboard`. Bug dipulihkan, 5/5 hijau lagi. Ini bukti test
  punya gigi, bukan selalu hijau.
- 2026-10-04 10:15:00 — M9, M10 selesai. Versi dinaikkan ke 1.2 / versionCode 3
  (bug fix → patch +1). Verifikasi akhir: `testDebugUnitTest` 14/0/0, `lintDebug`
  SUCCESSFUL, `assembleDebug` SUCCESSFUL, `assembleDebugAndroidTest` SUCCESSFUL,
  `connectedDebugAndroidTest` 5/5, `aapt2 dump badging` → versionCode 3 / 1.2.
- 2026-10-04 10:20:00 — Semua task selesai. Perubahan belum di-commit (menunggu
  persetujuan user).

## Notes
- Alasan menghapus, bukan mempertahankan, `hasPreviousDestination`: logika produksi
  yang dipakai NavGraph adalah `navController.previousBackStackEntry != null`.
  Fungsi pembungkus itu hanya mengembalikan argumennya apa adanya, jadi unit test
  atasnya hanya menguji identitas fungsi, bukan perilaku navigasi. Test yang
  benar harus menyentuh `NavHost` sungguhan — itu memang butuh instrumented
  test, bukan unit test.
- Tiga jalur masuk Create Target yang ditutup test baru: (1) sebagai
  `startDestination`, (2) setelah onboarding `popUpTo(ONBOARDING, inclusive)`,
  (3) dibuka dari Dashboard lewat `onCreateTarget`.
