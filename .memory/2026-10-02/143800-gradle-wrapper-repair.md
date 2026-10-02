# Perbaikan Gradle wrapper, .gitignore, .gitattributes, dan CI

Created: 2026-10-02 14:38:00 (+0700)

## Task / Masalah

User menjalankan `./gradlew assembleDebug -Pandroid.injected.build.abi=arm64-v8a` dan gagal dengan
pesan dari skrip wrapper kustom:

```
gradle-wrapper.jar not found. Open this project in Android Studio,
or download Gradle 8.7 from https://gradle.org/install/ and run:
  gradle wrapper --gradle-version 8.7
```

Penyebabnya tiga: `gradle/wrapper/gradle-wrapper.jar` tidak ada di repo, `gradlew`/`gradlew.bat`
memang bukan skrip resmi Gradle melainkan versi kustom, dan repo tidak punya `.gitignore` sehingga
`app/build/` (berisi APK ±16 MB) muncul sebagai untracked dan berisiko ikut ter-commit.

## Key Files Changed

- [gradle/wrapper/gradle-wrapper.jar](../../gradle/wrapper/gradle-wrapper.jar) — **dibuat ulang**,
  43.453 B, sha256 `cb0da6751c2b753a16ac168bb354870ebb1e162e9083f116729cec9c781156b8`.
- [gradlew](../../gradlew) dan [gradlew.bat](../../gradlew.bat) — diganti skrip resmi Gradle 8.7.
- [gradle/wrapper/gradle-wrapper.properties](../../gradle/wrapper/gradle-wrapper.properties) —
  ditulis ulang oleh Gradle; `distributionUrl` tetap `gradle-8.7-bin.zip`.
- [.gitignore](../../.gitignore) — **baru**: `build/`, `app/build/`, `.gradle/`, `local.properties`,
  `.kotlin/`, `captures/`, `.externalNativeBuild/`, `.cxx/`, `*.apk`, `*.aab`, `*.ap_`, `*.dex`,
  `.idea/`, `*.iml`, `*.hprof`, `.DS_Store`, `Thumbs.db`, `.freebuff/`, `*.jks`, `*.keystore`,
  `keystore.properties`.
- [.gitattributes](../../.gitattributes) — **baru**: `* text=auto`, `gradlew` LF, `gradlew.bat` CRLF,
  `*.sh` LF, `*.bat` CRLF, dan `*.jar`/`*.apk`/`*.aab`/`*.png`/`*.jks`/`*.keystore` sebagai binary.
- [README.md](../../README.md) — tabel output APK, perintah satu-ABI, catatan JDK 17+ + `JAVA_HOME`,
  penjelasan bahwa `-Pandroid.injected.build.abi` menulis ke `app/build/intermediates/apk/debug/`.
- [ci.yml](../../.github/workflows/ci.yml) — langkah "Bootstrap Gradle 8.7 and regenerate wrapper"
  dihapus; sisanya (JDK 17, cache, test, lint, assemble, upload artefak `*.apk`) tidak berubah.

## Keputusan Teknis

- Wrapper diregenerasi memakai binary Gradle 8.7 yang sudah ter-cache
  (`~/.gradle/wrapper/dists/gradle-8.7-all/aan3ydargesu18aqyqjwhr3pc/gradle-8.7/bin/gradle`)
  dengan `JAVA_HOME` = JBR Android Studio, perintah
  `gradle wrapper --gradle-version 8.7 --distribution-type bin`.
- Provenance wrapper jar diverifikasi: byte-identik dengan `gradle-wrapper.jar` yang dibundel di dalam
  `lib/plugins/gradle-wrapper-8.7.jar` pada distribusi Gradle 8.7 resmi, jadi aman untuk di-commit.
- `gradlew` diubah ke mode `100755` di git index lewat `git update-index --chmod=+x` (mode `100644`
  sebelumnya membuat `./gradlew` gagal di CI Linux/macOS). Perintah ini **men-stage** `gradlew`;
  belum ada commit.
- Tidak memakai `org.gradle.java.home` di `gradle.properties` karena nilainya machine-specific dan
  akan merusak CI Linux. Kebutuhan JDK 17+ lebih baik didokumentasikan di README.
- `*.bat` dipaksa CRLF lewat `.gitattributes` karena batch script Windows rapuh dengan LF, sedangkan
  `gradlew` wajib LF agar tidak rusak di Git Bash/CI.

## Asumsi / Risiko

> -.run pertama `./gradlew` mengunduh `gradle-8.7-bin.zip` (±130 MB) ke
>   `~/.gradle/wrapper/dists/gradle-8.7-bin/`. Tidak ada `distributionSha256Sum` di properties,
>   jadi integritas distribusi hanya dijamin HTTPS, bukan checksum.
> - `gradlew.bat` yang dihasilkan Gradle di mesin ini memakai akhir baris LF. `*.bat text eol=crlf`
>   di `.gitattributes` belum diuji langsung lewat `cmd.exe` dari lingkungan ini
>   (percobaan sebelumnya timeout), jadi keputusannya berdasarkan perilaku standar Git, bukan bukti.
> - Menambahkan `.gitattributes` tidak memicu diff massal: seluruh 86 berkas di index sudah LF.

## Verifikasi

Semua lewat **`./gradlew` sungguhan** (bukan binary cache), dengan `JAVA_HOME` = JBR 21.0.9:

- `./gradlew --version` → Gradle 8.7, JVM 21.0.9; wrapper mengunduh distribusi bin sendiri.
- `./gradlew assembleDebug -Pandroid.injected.build.abi=arm64-v8a` → **BUILD SUCCESSFUL**;
  APK `app/build/intermediates/apk/debug/app-arm64-v8a-debug.apk` (16.056.810 B), hanya berisi
  `lib/arm64-v8a/libdatastore_shared_counter.so`, signature `CN=Android Debug`.
- `./gradlew assembleDebug` → `app-arm64-v8a-debug.apk` (16.056.794 B) +
  `app-universal-debug.apk` (16.105.321 B) di `app/build/outputs/apk/debug/`.
- `./gradlew testDebugUnitTest lintDebug assembleDebug` → **BUILD SUCCESSFUL** (14 tes, 0 gagal).
- `git status` setelah `.gitignore`: `.gradle/`, `app/build/`, `.freebuff/` hilang dari untracked.
- Cek mode: `git ls-files -s gradlew` → `100755`.

## Blocker / Open Item

- `cmd.exe gradlew.bat --version` belum terverifikasi (timeout di lingkungan ini).
- Semua perubahan masih belum di-commit.

## Proposed Conventional Commit

`build: restore Gradle wrapper and add git ignore rules`

## Related

- Entry sebelumnya: [2026-10-02 14:04:00 — ABI split arm64-v8a](140400-abi-split-arm64.md)
- [README.md](../../README.md), [AGENTS.md](../../AGENTS.md) §4
