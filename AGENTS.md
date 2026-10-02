# Agent Instructions — Cukupin

Instruksi tetap untuk **semua agent** (Codebuff/Freebuff, Claude Code, Codex, OpenCode, Cursor,
dan lainnya) yang bekerja di repo ini. Isi berkas ini disalin dari instruksi global pengguna di
`~/.claude/CLAUDE.md`, `~/.codex/AGENTS.md`, dan `~/.config/opencode/AGENTS.md`, lalu disesuaikan
dengan stack proyek ini (Android/Kotlin/Compose/Room). Bagian hasil penyesuaian ditandai
**[adaptasi]**.

## Rule Precedence

Saat aturan berbenturan, urutan prioritasnya (tertinggi lebih dulu):

1. Instruksi eksplisit user di percakapan saat ini.
2. Panduan spesifik proyek di `.memory/README.md`, atau `PROJECT_MEMORY.md` legacy bila format baru belum ada.
3. Aturan tetap di dokumen ini.

Kalau konfliknya tidak jelas atau berisiko tinggi, angkat ke user — jangan diam-diam memilih satu sisi.

## 1. Aturan Dasar

1. **Commit: Conventional Commits, satu baris saja, tanpa trailer `Co-authored-by:` apa pun**
   (termasuk bot/agent). Berlaku juga saat hanya *mengusulkan* pesan commit. Aturan ini **menimpa
   default perkakas apa pun** yang menempelkan footer otomatis (mis. `Generated with … 🤖`).
2. **Hemat token: jangan menjalankan command verifikasi hanya untuk "memastikan".** Cukup
   informasikan command yang perlu dijalankan user. Perapian yang aman (mis. format kode) boleh dijalankan.
   - **[adaptasi]** Pengecualian yang disepakati (2 Okt 2026, saat memperbaiki Gradle wrapper):
     jalankan verifikasi bila **(a)** user meminta bukti nyata, **(b)** perubahan menyentuh
     build/toolchain/Gradle/dependency, atau **(c)** risiko regresinya tinggi — lalu laporkan apa
     yang dijalankan beserta hasilnya.
3. Terapkan prinsip berikut bila memungkinkan: SOLID; *Database as Code* (migration management);
   *Non-Destructive Migrations*; *End-to-End Type Safety*; *Strict Row Level Security (RLS)*;
   strategi rendering Next.js yang optimal (RSC); validasi environment variable; *Documentation is
   Key*; *Clean Code*; *State Feedback & Submission Prevention*; *Data Fetching Optimization*;
   internationalization (i18n); *First-Party Anti-Bot*; *Responsive Web Design* & mobile-first.
4. Kalau user bertanya/menginstruksikan dalam Bahasa Indonesia, **balas dalam Bahasa Indonesia** —
   kecuali istilah teknis (nama variabel/fungsi/kolom, keyword SQL, error code, jargon Inggris tanpa
   padanan alami) yang tetap dalam bahasa aslinya, atau user meminta balasan berbahasa Inggris.
   Berlaku di semua project/percakapan.

## 2. Working Style

### 2.1 Pakai Contoh yang Relevan

Saat menjelaskan konsep, mengusulkan pendekatan, atau menulis dokumentasi, sertakan contoh konkret
bila membantu pemahaman (potongan kode, contoh query, contoh struktur file/folder). Lewati contoh
hanya kalau redundan atau permintaannya murni mekanis (mis. pesan commit satu baris).

### 2.2 Berpikir Kritis Sebelum Mengusulkan

Untuk setiap ide, rekomendasi, atau proposal:

- Cari celah, kasus tepi, atau mode kegagalan sebelum menyajikannya sebagai final.
- Sertakan minimal satu kontra-argumen, risiko, atau keterbatasan — jangan hanya sisi baiknya.
- Kalau proposalnya belum matang atau belum terverifikasi, katakan terus terang, jangan memakai
  nada percaya diri yang palsu.
- Berlaku untuk plan, usulan arsitektur, desain kode, dan strategi migrasi.
- Proporsional: output sepele/mekanis (pesan commit, perapian format) tidak perlu kontra-argumen.

### 2.3 Gaya Diagram

Saat membuat diagram (flowchart, ERD, arsitektur, sequence, dsb.), pakai tema/gaya default tool atau
library yang dipakai. Jangan pakai skin, palet warna, atau tema kustom kecuali user memintanya.

## 3. Arsitektur & Standar Domain

- **Telecom/utility rating (usage-based charging), billing, dan payment system:** pakai **Oracle
  Customer to Meter (C2M)** dan **TM Forum ODA** sebagai rujukan utama untuk domain model, process
  flow, dan desain API. Setiap penyimpangan wajib dijustifikasi eksplisit di bagian `## Notes` pada
  file plan (lihat §7).
- **Proyek lain — termasuk Cukupin:** pakai **TOGAF**, atau standar lain yang sesuai, sebagai
  benchmark, dan terapkan secara proporsional. Rujuk fase ADM/view arsitektur yang relevan untuk
  pekerjaan berskala enterprise, tapi jangan memaksakan seremoni governance enterprise pada fitur
  kecil, satu service, atau perbaikan bug rutin.
- Standar ini memandu **desain dan dokumentasi**. Kalau penerapannya berimplikasi pada perubahan
  skema/struktur database live, perubahan itu harus dituangkan ke file plan (§7), bukan dieksekusi
  langsung — lihat batasan read-only di §6.
- **[adaptasi]** Invariant domain Cukupin (jangan dilanggar): uang selalu `Long` (jangan
  `Float`/`Double` untuk penyimpanan), tanggal keuangan `LocalDate` (audit `Instant`), perkalian
  nominal × hari memakai `BigDecimal`, dan status kesehatan selalu punya ikon + teks (bukan warna saja).

## 4. Android / Kotlin Development **[adaptasi dari §4 global yang berbasis Flutter]**

- Saat diminta **membangun/menambah fitur**: naikkan **minor** `versionName` di
  `app/build.gradle.kts` sebesar 1, reset patch ke 0, dan naikkan `versionCode` sebesar 1.
- Saat diminta **memperbaiki bug**: naikkan **patch** `versionName` sebesar 1 dan `versionCode` sebesar 1.
- Sebelum menganggap pekerjaan "aman/selesai", verifikasi memakai (jalankan hanya bila §1.2 mengizinkan):

  ```bash
  ./gradlew testDebugUnitTest
  ./gradlew lintDebug
  ./gradlew assembleDebug                                        # APK arm64-v8a + universal
  ./gradlew assembleDebug -Pandroid.injected.build.abi=arm64-v8a # hanya arm64 (hasil di
                                                                 # app/build/intermediates/apk/debug/)
  ```

- Wajib **JDK 17+** (AGP 8.5 tidak bisa jalan di Java 8). Set `JAVA_HOME` ke JDK 17+ sebelum
  memanggil `./gradlew`, mis. `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"`
  (Git Bash) atau `set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"` (cmd).
- Build butuh Android SDK lewat `ANDROID_HOME` atau `local.properties` (`sdk.dir`).
- **Migrasi Room wajib non-destruktif** dan disimpan sebagai kode di repo; jangan pernah
  menjalankan perubahan skema ad-hoc ke database perangkat/user.
- Jangan commit `build/`, `.gradle/`, `local.properties`, `*.apk`/`*.aab`, atau file keystore —
  semuanya sudah di-`.gitignore`. `gradle/wrapper/gradle-wrapper.jar` justru **harus** ikut
  di-commit supaya `./gradlew` langsung bisa dipakai.

## 5. Project Memory

### Discovery dan Precedence

- Sebelum mengerjakan task, cari `.memory/README.md` di root project atau workspace.
- Jika ada, baca file tersebut lebih dulu dan gunakan `.memory/` sebagai sumber memory aktif. Baca
  hanya entry yang relevan lewat indeks, nama file, atau pencarian keyword; jangan muat seluruh
  history tanpa kebutuhan.
- Jika `.memory/README.md` belum ada, baca `PROJECT_MEMORY.md` legacy bila tersedia.
- Jika keduanya ada, `.memory/` menjadi sumber aktif dan `PROJECT_MEMORY.md` menjadi arsip
  historis read-only, kecuali instruksi project menyatakan lain.

### Format Aktif

- Simpan setiap task signifikan sebagai `.memory/YYYY-MM-DD/HHmmss-kebab-case-topic.md` memakai
  timezone lokal project.
- Satu file per task, bukan satu file per hari. Tambahkan suffix unik singkat bila beberapa agent
  berpotensi membuat nama sama.
- Setiap entry memuat task/masalah, file penting yang diubah, keputusan teknis/bisnis,
  asumsi/risiko, blocker/open item, verifikasi yang dilakukan atau disarankan, proposed Conventional
  Commit satu baris, dan link plan/spec/issue terkait bila relevan.
- Jangan buat entry untuk pembacaan trivial, penjelasan saja, formatting-only, atau pekerjaan tanpa
  knowledge project yang tahan lama.

### Indeks Memory

- `.memory/README.md` adalah ringkasan current state dan indeks navigasi, bukan duplikasi activity log.
- Pelihara timestamp update, versi format, current state, active decisions, open items/blockers,
  link arsip legacy, dan maksimal 20 link entry terbaru.
- Entry lama tetap dipertahankan walau keluar dari `Recent Entries`; jangan hapus history hanya
  untuk membatasi ukuran indeks.

### Update Setelah Task

- Setelah perubahan signifikan, buat satu entry timestamped dan update `.memory/README.md` hanya
  jika current state, active decisions, open items, atau recent links berubah.
- Baca ulang file target tepat sebelum edit jika ada kemungkinan agent paralel. Pertahankan memory
  milik user atau agent lain.
- Setelah `.memory/` diinisialisasi, jangan append entry baru ke `PROJECT_MEMORY.md`.

### Migrasi Legacy

- Jika hanya `PROJECT_MEMORY.md` tersedia, buat `.memory/README.md`, pertahankan file legacy di
  lokasi semula, link sebagai arsip historis, lalu ekstrak hanya keputusan aktif, blocker, dan
  current state.
- Jangan pecah atau rewrite seluruh history legacy kecuali user meminta migrasi penuh.
- Semua task signifikan berikutnya memakai format aktif.

### Safety

- Jangan simpan secret, password, access token, private key, data personal sensitif, atau data
  production yang tidak diperlukan dalam project memory.
- **Env Guard:**
  - **Jangan pernah** mencetak, log, echo, atau `cat` isi `.env*`, file kredensial, `keystore.properties`,
    `*.jks`/`*.keystore`, `local.properties`, `CRON_SECRET`, atau API key ke chat, output tool,
    komentar kode, maupun markdown.
  - Jangan menaruh nilai asli pada file contoh — hanya placeholder yang jelas palsu.
  - Jangan commit file kredensial; semuanya sudah di-`.gitignore`.
  - Saat agent butuh secret, baca dari environment saat runtime — jangan tampung di variabel yang
    dicetak, jangan `echo`.
  - Kalau sebuah tool call berpotensi membocorkan secret (mis. `cat .env.local`,
    `grep -r "password"`), tolak dan usulkan alternatif yang aman (mis. `grep --exclude=".env*"`).
  - Kalau secret terekspos di chat, segera sarankan rotasi kunci tersebut.

## 6. Database (Read-Only)

Agent terhubung ke database eksternal via MCP dalam mode **READ ONLY**. Tujuannya terbatas untuk:
membaca data, menganalisis struktur database, membantu menyusun query `SELECT`, serta menjelaskan
data dan relasi antar tabel.

**Statement yang diizinkan:** `SELECT`, `WITH` (CTE), `DESCRIBE`/`DESC`, `EXPLAIN PLAN`, `DBMS_METADATA`

**Statement yang dilarang:** `INSERT`, `UPDATE`, `DELETE`, `MERGE`, `TRUNCATE`, `DROP`, `ALTER`,
`CREATE`, `RENAME`, `GRANT`, `REVOKE`, `COMMIT`, `ROLLBACK`, `BEGIN…END`, `DECLARE`, `EXECUTE`,
pemanggilan PROCEDURE/FUNCTION yang bisa mengubah data, atau statement SQL/PL-SQL lain yang
berpotensi mengubah database.

**Aturan eksekusi:**

1. Sebelum menjalankan SQL, pastikan statement diawali `SELECT` atau `WITH`.
2. Kalau query mengandung keyword terlarang, jangan dijalankan.
3. Kalau user meminta perubahan data, jangan membuat atau menjalankan query modifikasi.
4. Jelaskan bahwa environment database ini READ ONLY dan hanya mengizinkan operasi baca.
5. Kalau ragu apakah sebuah statement bisa mengubah data, anggap UNSAFE dan tolak.
6. Jangan pernah mencari akal untuk melewati batasan READ ONLY.

**Respons penolakan standar:**

> "Operation denied. This database connection only allows read access (READ ONLY). Commands that could change the database structure or data are not permitted."

**[adaptasi]** Database aplikasi Cukupin adalah Room lokal di perangkat. Aturan di atas berlaku
penuh untuk database eksternal yang terhubung via MCP; perubahan skema Room tetap harus lewat
migration file di repo (non-destruktif), bukan dijalankan langsung.

## 7. Menyimpan Plan ke File Markdown

Setiap kali user meminta hal berikut (atau respons apa pun yang memuat urutan task/milestone/langkah
terstruktur): membuat/generate plan; project plan, implementation plan, migration plan; roadmap,
action plan, task breakdown, execution plan — maka agent **WAJIB**:

1. Menulis setiap plan ke file Markdown (`.md`).
2. Satu file untuk satu plan — jangan mencampur beberapa plan tak berkaitan dalam satu file.
3. Membuat file tersebut segera setelah plan dibuat.
4. Menyertakan keduanya dalam respons: isi plan dan path file Markdown yang dibuat.
5. Memelihara file plan itu selama pengerjaan — jangan memperlakukannya sebagai dokumen sekali
   tulis. Setiap ada progres, update file plan yang sama untuk mencerminkan: task yang **selesai**
   (centang `- [x]` di `## Tasks`), task yang **masih pending**, task yang ternyata **tidak mungkin /
   blocked** beserta alasannya, dan perkembangan lain yang belum tercakup template (perubahan
   scope, risiko baru, keputusan di tengah eksekusi). Catat setiap update sebagai entri bertanggal
   di `## Progress Log` dan centang/beri anotasi pada `## Tasks`, supaya file plan tetap menjadi
   satu-satunya sumber kebenaran status plan tersebut — tanpa file terpisah.

### Konvensi Nama File

```
plans/YYYY-MM-DD-<kebab-case-plan-name>.md
```

Contoh:

```
plans/2026-10-02-abi-split-arm64-plan.md
plans/2026-10-02-release-signing-plan.md
plans/2026-10-02-mobile-app-roadmap.md
```

Kalau ada beberapa plan dengan nama sama di hari yang sama, tambahkan suffix angka:
`plans/2026-10-02-api-migration-plan-2.md`, `-3.md`, dst.

### Template Markdown

```md
# <Plan Title>

Created: YYYY-MM-DD HH:mm:ss

## Objective
<plan objective>

## Scope
- item 1
- item 2

## Milestones
1. Phase 1
2. Phase 2
3. Phase 3

## Tasks
- [ ] Task 1
- [ ] Task 2
- [ ] Task 3

## Risks
- Risk 1
- Risk 2

## Progress Log
- YYYY-MM-DD HH:mm:ss — <what was done / what's pending / what's blocked / other notable update>

## Notes
Additional information.
```

### Constraints

- Satu file = satu plan.
- Jangan menimpa file plan yang sudah ada kecuali diminta eksplisit.
- Kalau user mengubah plan yang ada, update file yang sama.
- Kalau user meminta plan baru, buat file Markdown baru.
- Plan selalu dalam format Markdown (`.md`).

## 8. Dokumentasi: Pisahkan Fakta Terverifikasi dari Commentary

- Saat menulis atau mengupdate dokumentasi teknis/analisis (spec, knowledge doc, technical doc,
  dsb.) di project manapun, tulis **fakta yang sudah diverifikasi** (hasil baca kode, database,
  Figma, MoM, atau kutipan sumber lain) sebagai teks/tabel biasa.
- Pindahkan **commentary** — interpretasi, klasifikasi/judgment beserta alasannya, rekomendasi,
  needs-clarification/needs-confirmation, spekulasi ("kemungkinan", "sepertinya"), atau asesmen
  dampak/risiko — ke Markdown blockquote (`> `).
- Saat merancang desain atau analisis (keputusan desain, rancangan solusi, kolom/field baru, dsb.),
  sertakan referensi ke sumber acuan (MoM/Figma). Kalau keputusannya murni hasil pemikiran sendiri,
  tetap wajib ada commentary yang menjelaskan alasan/justifikasinya secara detail.
- Tujuannya: isi dokumen inti hanya berisi fakta tervalidasi, sedangkan bagian yang masih butuh
  keputusan/klarifikasi/asumsi mudah dipisahkan secara visual.
- Untuk tabel: blockquote tidak bisa berada di dalam cell, jadi kosongkan cell menjadi fakta murni
  lalu taruh satu blockquote konsolidasi tepat setelah tabel, dirujuk per baris.
- Jangan terapkan aturan ini di dalam code block (kode, PlantUML/Mermaid, JSON, SQL) — biarkan
  syntax aslinya.
- Kalau project punya aturan lebih spesifik, aturan project berlaku sebagai perluasan/detail, bukan
  pengganti prinsip global ini.

## 9. Commentary Wajib untuk Blok Kode yang Terkait Proses Bisnis

- Setiap menulis atau mengedit kode — bahasa apa pun, termasuk Kotlin, SQL/migration script, Gradle
  Kotlin DSL, dsb. — sertakan commentary di tiap blok kode yang merepresentasikan atau
  mengimplementasikan proses/aturan bisnis, supaya pembaca berikutnya paham maksud bisnisnya tanpa menebak.
- Ini pengecualian eksplisit dari default "minim komentar, hanya kalau WHY tidak jelas": untuk blok
  berisi logic bisnis (validasi aturan, kalkulasi, transisi status/workflow, kondisi eligibility,
  threshold), commentary tetap wajib walau kodenya sudah jelas dari nama variabel/fungsi — yang
  dijelaskan adalah alasan bisnisnya, bukan sintaksnya.
- Isi commentary fokus ke KENAPA secara bisnis: aturan/requirement yang diimplementasikan, kondisi
  bisnis yang dicek, alasan suatu branch/exception ada, atau referensi requirement/ticket/MoM.
  Jangan menerangkan ulang hal yang sudah jelas dari sintaks (mis. "increment counter by 1").
- Berlaku untuk kode baru dan blok yang diubah dalam scope task; tidak perlu menambahkan commentary
  retroaktif ke file yang tidak disentuh kecuali diminta.
- Blok yang murni teknis/generik (boilerplate, getter/setter, utility tanpa business rule) tetap
  ikuti default minim-komentar.

## 10. Format Output Code Review

Saat melaporkan hasil code review (dari `/code-review`, review manual, atau permintaan ad-hoc),
gunakan format berikut per temuan:

```
### N. {emoji} {SEVERITY} — {judul singkat masalah}
**File:** [nama_file.ext](path/relatif/nama_file.ext) (baris X-Y, detail lokasi singkat)

{Paragraf penjelasan: apa masalahnya, kenapa terjadi, skenario konkret yang bisa gagal/rusak.
Sebut file/komponen lain yang terdampak atau yang sudah punya workaround berbeda kalau relevan.}

**Cara resolve:** {Paragraf actionable: langkah konkret memperbaiki, termasuk pendekatan/pattern
yang disarankan. Kalau ada, sebutkan kenapa ini sebaiknya dikerjakan sebelum merge atau bisa ditunda.}
```

- Mapping emoji severity: 🔴 KRITIS, 🟠 TINGGI, 🟡 SEDANG, 🟢 RENDAH.
- Nomor urut (`N.`) berlanjut dari yang paling kritis ke paling ringan, tidak reset per kategori.
- Judul singkat harus menyebut area/mekanisme yang bermasalah (mis. "Split ABI arm64 dibatalkan
  diam-diam saat `installDebug` dipakai"), bukan generic ("Bug di build").
- Body berupa paragraf (bukan bullet bertele-tele) untuk penjelasan masalah dan cara resolve — boleh
  menyebut nama variabel/fungsi/komponen spesifik secara inline.
- Temuan berupa risiko bisnis/keputusan belum final (bukan bug teknis) dipisahkan jadi section
  tersendiri di akhir, jangan dinomori campur dengan temuan teknis.

## 11. Lampiran: Aturan Perkakas Lokal (dari instruksi global)

Bagian ini bukan aturan kode proyek, tapi tetap tersalin supaya perilakunya konsisten.

- **OpenCode provider config (`opencode.json`)**: untuk provider OpenAI-compatible kustom, selalu
  set `limit.context` **dan** `limit.output` bersamaan — menyetel `context` saja bisa membuat
  provider hilang dari picker TUI walau `opencode models <provider>` masih menampilkannya.
  Contoh: `"limit": { "context": 1048576, "output": 131072 }`. Alur kerja: backup `opencode.json`
  dulu (`opencode.json.bak-YYYY-MM-DD-<reason>`), edit hanya model target, validasi JSON, lalu
  verifikasi dengan `opencode models <provider>` dan restart TUI + `/models`. Batasan: `output`
  terlalu besar bisa memicu error `max_tokens` di sisi provider.
- **graphify** (`~/.claude/skills/graphify/SKILL.md`, skill milik Claude Code): dipicu oleh `/graphify`.
  Saat user menulis `/graphify`, pakai skill graphify sebelum melakukan hal lain.
- Berkas ini boleh disunting user. Kalau instruksi global di HOME berubah, sinkronkan perubahan ke sini.

