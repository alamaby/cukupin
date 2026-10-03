# Perbaikan CI: pasang Android SDK langsung, hilangkan paket `tools` yang dihapus

Created: 2026-10-03 07:47:33 (+0700)

## Task / Masalah

Workflow CI merah sejak 2 Okt 2026, sebelum perubahan ABI split pernah ada. Run `chore: initial ci`
gagal dalam 23 detik di langkah `Set up Android SDK`, sehingga seluruh langkah Gradle ter-skip.

Akar masalah dari log runner: `android-actions/setup-android@v3` memanggil `sdkmanager "tools"`,
dan paket `tools` sudah dihapus dari repositori SDK Google. Hasilnya
`Warning: Failed to find package 'tools'` lalu `sdkmanager` keluar dengan kode 1.

Konsekuensi: wrapper Gradle yang baru dipulihkan dan split ABI arm64 belum pernah divalidasi
oleh CI; semua bukti sampai saat ini hanya berasal dari mesin lokal.

## Key Files Changed

- [.github/workflows/ci.yml](../../.github/workflows/ci.yml) — langkah `android-actions/setup-android@v3`
  diganti blok bash yang memasang paket sendiri, ditambah langkah diagnostik baru
  `List installed SDK packages` (total langkah 10 menjadi 11).

## Keputusan Teknis

- Action `setup-android` dibuang, bukan ditambal: runner `ubuntu-latest` sudah menyediakan Android
  SDK di `/usr/local/lib/android/sdk`, sesuai path yang tertera di log kegagalan.
- Resolusi `sdkmanager` dua tingkat: coba `cmdline-tools/latest/bin/sdkmanager`, lalu fallback ke
  glob versi (`sort -V | tail -1`) karena runner mungkin hanya menyediakan `cmdline-tools/16.0`.
- Paket yang dipasang: `platform-tools`, `platforms;android-34`, `build-tools;34.0.0`, sesuai
  `compileSdk = 34` dan build tools default AGP 8.5.
- `yes | sdkmanager --licenses > /dev/null || true` dipakai agar langkah tidak gagal hanya karena
  lisensi, dan `::error::` dipakai kalau `sdkmanager` benar-benar tidak ditemukan.
- Langkah `List installed SDK packages` ditambahkan agar versi paket yang terpasang terlihat di log
  saat CI gagal, tanpa perlu menebak.

## Asumsi / Risiko

> - Mengergentakkan pada `ubuntu-latest` sudah punya SDK. Belum diuji untuk runner self-hosted,
>   macOS, atau Windows, yang bisa punya tata letak berbeda.
> - `actions/checkout@v4`, `actions/setup-java@v4`, dan `actions/upload-artifact@v4` masih
>   menargetkan Node.js 20 dan GitHub memaksa eksekusi di Node 24. Itu hanya warning, bukan kegagalan,
>   tapi versi action eventually perlu dinaikkan.
> - `|| true` pada penerimaan lisensi menyembunyikan kemungkinan kegagalan lisensi. SENGaja:
>   runner sudah pre-accept, dan kegagalan di sini tidak menambah nilai informasi.

## Verifikasi

- Run `37083086501` pada commit `1ed2881`: **success**, seluruh 16 langkah hijau, termasuk
  `Set up Android SDK`, `Run unit tests`, `Run Android Lint`, dan `Assemble debug APK`.
- Artefak `app-debug-apks` diunduh dan diperiksa isinya:
  - `app-arm64-v8a-debug.apk` 16.056.794 B, hanya berisi `lib/arm64-v8a/libdatastore_shared_counter.so`
  - `app-universal-debug.apk` 16.105.321 B, berisi 4 ABI (arm64-v8a, armeabi-v7a, x86, x86_64)
  - Kedua ukuran **identik** dengan hasil build lokal di Windows, jadi build reproducible lintas OS.
- File `ci.yml` divalidasi sebagai YAML dengan 11 langkah terurut.

## Blocker / Open Item

- `distributionSha256Sum` untuk `gradle-8.7-bin.zip` belum ditambahkan; distribusi Gradle
  hanya dijamin HTTPS.
- Belum ada job CI yang memverifikasi split ABI secara eksplisit, misalnya mengecek
  `native-code` APK memakai `apkanalyzer`.
- `gradle/wrapper/gradle-wrapper.jar` tercatat dengan mode `100755` (cosmetic, tidak berpengaruh).

## Proposed Conventional Commit

`fix(ci): install Android SDK packages directly instead of removed tools package`

## Related

- Run CI: `https://github.com/alamaby/cukupin/actions/runs/37083086501`
- Entry sebelumnya: [2026-10-03 06:18:46 — instruksi agent](061846-agent-instructions.md)