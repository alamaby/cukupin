# Instruksi agent level repo (AGENTS.md) + uji injeksi

Created: 2026-10-03 06:18:46 (+0700)

## Task / Masalah

User menanyakan berkas instruksi agent apa saja yang berlaku untuk repo ini, lalu meminta
seluruh instruksi global miliknya disalin ke repo ini.

Hasil pemeriksaan disk:

- Tidak ada `AGENTS.md`, `CLAUDE.md`, `GEMINI.md`, `.cursorrules`, `.cursor/rules`,
  `.github/copilot-instructions.md`, atau `.windsurfrules` di repo. Satu-satunya berkas `.md`
  adalah `README.md`.
- `.freebuff/project-id` hanya berisi UUID proyek, bukan instruksi.
- Tiga berkas instruksi global ada di HOME milik tool lain: `~/.claude/CLAUDE.md` (8.976 B),
  `~/.codex/AGENTS.md` (1.172 B, versi ringkas), `~/.config/opencode/AGENTS.md` (12.957 B, lengkap).
- `~/.agents/skills/` berisi 28 skill global; `~/.config/freebuff-desktop/` hanya berisi state.

## Key Files Changed

- [AGENTS.md](../../AGENTS.md) — baru, 365 baris. Gabungan ketiga berkas global, ditulis dalam
  Bahasa Indonesia dan disesuaikan ke stack proyek; bagian adaptasi ditandai `[adaptasi]`.
- [CLAUDE.md](../../CLAUDE.md) — baru, penunjuk 4 baris ke `AGENTS.md`.
- [.memory/README.md](../README.md) plus entry bertanggal 2 dan 3 Okt 2026 — inisialisasi memory.

## Yang Disalin Apa Adanya

Rule Precedence; conventional commit satu baris tanpa trailer `Co-authored-by:`; aturan hemat token;
prinsip SOLID, Database as Code, Non-Destructive Migrations, End-to-End Type Safety, RLS, validasi
environment variable, Clean Code; Working Style; Project Memory; Database read-only; Menyimpan plan
ke Markdown; fakta-versus-commentary; commentary proses bisnis; format output code review.

## Yang Diadaptasi

- Aturan Flutter dipetakan ke Android/Kotlin: kenaikan `versionName` dan `versionCode`, perintah
  `./gradlew`, syarat JDK 17+, migrasi Room non-destruktif.
- C2M dan TM Forum ODA ditahan sebagai klausa kondisional; Cukupin memakai TOGAF proporsional
  plus invariant uang `Long`, tanggal `LocalDate`, dan `BigDecimal`.
- Env Guard Supabase diganti sasaran `keystore.properties`, `*.jks`, `local.properties`, dan API key.
- Aturan provider OpenCode dan skill graphify dipindah ke lampiran agar tidak tercampur aturan kode.

## Keputusan Teknis

- `AGENTS.md` menjadi satu-satunya sumber instruksi repo karena dibaca Codex, OpenCode, dan
  Freebuff dari root; `CLAUDE.md` hanya penunjuk untuk Claude Code.
- Aturan "jangan jalankan verifikasi" diberi pengecualian tertulis bertanggal 2 Okt 2026, agar
  tugas perbaikan build tetap bisa dibuktikan.

## Bukti Injeksi

Setelan `uiPrefs.injectAgentsMd` di `~/.config/freebuff-desktop/state.json` bernilai `true`.
Pada giliran berikutnya, blok "Project instructions" berlabel `AGENTS.md` memuat kalimat yang baru
ditulis pada 3 Okt 2026, termasuk penanda `[adaptasi dari §4 global yang berbasis Flutter]` dan
catatan bertanggal 2 Okt 2026. Kalimat itu tidak ada di berkas global mana pun.

## Asumsi / Risiko

> - Injeksi `AGENTS.md` diperlakukan sebagai system prompt, jadi isinya bukan data: kesalahan
>   di sana mengubah perilaku agent tanpa terlihat.
> - Frekuensi pembacaan, per giliran atau hanya saat thread dibuat, belum dibuktikan dan sedang
>   diuji memakai blok kanari di bagian 12 AGENTS.md.
> - Salinan ini bisa basi kalau berkas global di HOME berubah; butuh sinkronisasi berkala.
> - Konflik dengan default platform: Freebuff menutup commit dengan trailer
>   `Co-Authored-By: Codebuff`, sedangkan aturan global melarang trailer apa pun. Aturan user
>   dipilih dan dicatat di sini agar tidak berubah diam-diam.

## Verifikasi

- Pencarian `find` dan `ls` di repo serta HOME untuk memastikan daftar berkas instruksi.
- Nilai `injectAgentsMd` dibaca tanpa mencetak nilai sensitif lain seperti token auth.
- Perbandingan isi suntikan terhadap `AGENTS.md` dan terhadap ketiga berkas global.

## Blocker / Open Item

- Blok kanari bagian 12 di `AGENTS.md` harus dihapus setelah uji injeksi selesai.
- Belum ada entri memory untuk pekerjaan inisialisasi memory itu sendiri.

## Proposed Conventional Commit

`docs(agents): add repo agent instructions copied from global config`

## Related

- [AGENTS.md](../../AGENTS.md), [CLAUDE.md](../../CLAUDE.md)
- Sumber: `~/.claude/CLAUDE.md`, `~/.codex/AGENTS.md`, `~/.config/opencode/AGENTS.md`
