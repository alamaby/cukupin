#!/usr/bin/env bash
#
# Memasang APK debug Cukupin ke emulator/perangkat yang terhubung.
#
# Alasan script ini ada: APK variant `debug` di-project ini selalu carrying flag
# `android:testOnly="true"` di manifest hasil merge. Flag itu di-inject Android Gradle
# Plugin karena `testInstrumentationRunner` aktif, sehingga `adb install` biasa gagal
# dengan INSTALL_FAILED_TEST_ONLY. Script ini memaksa flag `-t` (--test-only) supaya
# pemasangan tidak pernah gagal karena alasan itu, dan otomatis memilih perangkat yang
# benar ketika `adb devices` punya lebih dari satu entri.
#
# Cara pakai:
#   scripts/install-debug.sh                 # pasang ke perangkat yang terdeteksi
#   scripts/install-debug.sh --launch        # pasang lalu jalankan MainActivity
#   scripts/install-debug.sh -s emulator-5554 # targetkan serial tertentu
#   scripts/install-debug.sh --apk path.apk  # pakai APK tertentu
#
# Exit code:
#   0  sukses
#   1  ada yang tidak lengkap (adb/SDK tidak ada, APK tidak ditemukan)
#   2  tidak ada perangkat yang siap
#   3  adb install mengembalikan gagal

set -uo pipefail

readonly APP_ID="com.alamaby.cukupin"
readonly MAIN_ACTIVITY="${APP_ID}/.MainActivity"

SERIAL=""
APK_PATH=""
DO_LAUNCH=0

die() {
  printf 'ERROR: %s\n' "$1" >&2
  exit "${2:-1}"
}

usage() {
  cat <<'EOF'
Penggunaan: scripts/install-debug.sh [opsi]

  -s, --serial <serial>   Targetkan perangkat tertentu (default: auto-pilih)
  -a, --apk <path>        Pakai file APK tertentu (default: cari yang terbaru)
  -l, --launch            Jalankan MainActivity setelah pemasangan sukses
  -h, --help              Tampilkan bantuan ini
EOF
}

# ---------------------------------------------------------------------------
# Parsing argumen
# ---------------------------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    -s|--serial) SERIAL="${2:-}"; shift 2 ;;
    -a|--apk)    APK_PATH="${2:-}"; shift 2 ;;
    -l|--launch) DO_LAUNCH=1; shift ;;
    -h|--help)   usage; exit 0 ;;
    *) usage >&2; die "Opsi tidak dikenal: $1" ;;
  esac
done

# Script ini mencari repository root sendiri, sehingga bisa dipanggil dari mana saja.
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT" || die "Gagal masuk ke repo root: $REPO_ROOT"

# ---------------------------------------------------------------------------
# Cari adb
#
# Urutan pencarian SDK: ANDROID_HOME, ANDROID_SDK_ROOT, sdk.dir di local.properties,
# lalu andalkan PATH. File local.properties hanya dibaca lewat grep dan tidak pernah
# dicetak ke output, karena isinya bisa berupa path lokal milik mesin ini.
# ---------------------------------------------------------------------------
if [[ -n "${ANDROID_HOME:-}" && -x "${ANDROID_HOME}/platform-tools/adb.exe" ]]; then
  ADB="${ANDROID_HOME}/platform-tools/adb.exe"
elif [[ -n "${ANDROID_HOME:-}" && -x "${ANDROID_HOME}/platform-tools/adb" ]]; then
  ADB="${ANDROID_HOME}/platform-tools/adb"
elif [[ -n "${ANDROID_SDK_ROOT:-}" && -x "${ANDROID_SDK_ROOT}/platform-tools/adb.exe" ]]; then
  ADB="${ANDROID_SDK_ROOT}/platform-tools/adb.exe"
elif [[ -f local.properties ]] && grep -q '^sdk\.dir=' local.properties 2>/dev/null; then
  _sdk="$(grep '^sdk\.dir=' local.properties | head -1 | cut -d= -f2- | tr '\\' '/' | sed 's|://|://|')"
  if   [[ -x "${_sdk}/platform-tools/adb.exe" ]]; then ADB="${_sdk}/platform-tools/adb.exe"
  elif [[ -x "${_sdk}/platform-tools/adb" ]];      then ADB="${_sdk}/platform-tools/adb"
  fi
fi

if [[ -z "${ADB:-}" ]]; then
  if command -v adb >/dev/null 2>&1; then
    ADB="$(command -v adb)"
  else
    die "adb tidak ditemukan. Set ANDROID_HOME atau tambahkan platform-tools ke PATH."
  fi
fi
readonly ADB
printf 'adb    : %s\n' "$ADB"

"$ADB" start-server >/dev/null 2>&1 || true

# ---------------------------------------------------------------------------
# Pilih perangkat
#
# Hanya perangkat berstatus `device` yang bisa dipakai; `offline` (emulator masih
# boot atau kehabisan memori host) dan `unauthorized` (dialog USB debugging belum
# disetujui di HP) sengaja tidak dipilih supaya pesan errornya jelas, bukan
# membingungkan.
# ---------------------------------------------------------------------------
mapfile -t READY_SERIALS < <(
  "$ADB" devices | awk 'NR>1 && $2=="device" {print $1}'
)

if [[ -z "$SERIAL" ]]; then
  # Dengan lebih dari satu kandidat, berhenti lebih baik daripada memilih
  # yang salah lalu memasang ke perangkat yang tidak dimaksud.
  COUNT="${#READY_SERIALS[@]}"
  if [[ "$COUNT" -eq 0 ]]; then
    printf '\nTidak ada perangkat berstatus `device`.\n' >&2
    printf 'Status saat ini:\n' >&2
    "$ADB" devices -l >&2
    exit 2
  elif [[ "$COUNT" -eq 1 ]]; then
    SERIAL="${READY_SERIALS[0]}"
  else
    printf 'Ditemukan %s perangkat; pilih salah satu dengan --serial:\n' "$COUNT" >&2
    printf '  %s\n' "${READY_SERIALS[@]}" >&2
    exit 2
  fi
else
  FOUND=0
  for s in "${READY_SERIALS[@]}"; do [[ "$s" == "$SERIAL" ]] && FOUND=1; done
  [[ "$FOUND" -eq 1 ]] || die "Serial '$SERIAL' tidak ada di daftar perangkat siap."
fi
readonly SERIAL
printf 'target : %s\n\n' "$SERIAL"

# ---------------------------------------------------------------------------
# Tentukan APK
#
# Dua lokasi yang mungkin dipakai Gradle:
#   1. app/build/outputs/apk/debug/     -> hasil `./gradlew assembleDebug` biasa
#   2. app/build/intermediates/apk/debug/ -> hasil build dengan
#      -Pandroid.injected.build.abi (mode ala Android Studio)
# Kalau keduanya ada, ambil yang paling baru modification time-nya.
# ---------------------------------------------------------------------------
find_newest_apk() {
  find "$1" -maxdepth 1 -name '*.apk' -type f -printf '%T@ %p\n' 2>/dev/null \
    | sort -rn | head -1 | cut -d' ' -f2-
}

if [[ -n "$APK_PATH" ]]; then
  [[ -f "$APK_PATH" ]] || die "APK tidak ditemukan: $APK_PATH"
else
  APK_PATH="$(find_newest_apk app/build/outputs/apk/debug)"
  [[ -n "$APK_PATH" ]] || APK_PATH="$(find_newest_apk app/build/intermediates/apk/debug)"
  [[ -n "$APK_PATH" ]] || die \
    "APK debug belum ada. Bangun dulu: ./gradlew assembleDebug"
fi
readonly APK_PATH
printf 'APK    : %s\n\n' "$APK_PATH"

# ---------------------------------------------------------------------------
# Pasang
#
# `-r`  : ganti versi lama yang sudah terpasang
# `-t`  : izinkan APK bertanda testOnly. WAJIB untuk variant debug di-project ini;
#         tanpa flag ini adb menolak dengan INSTALL_FAILED_TEST_ONLY. Flag ini
#         aman untuk APK yang tidak bertanda testOnly, jadi selalu dikirimkan.
# ---------------------------------------------------------------------------
printf 'Memasang...\n'
if ! "$ADB" -s "$SERIAL" install -r -t "$APK_PATH"; then
  printf '\nPemasangan gagal.\n' >&2
  exit 3
fi

# Verifikasi lewat package manager, bukan hanya Exit code adb, supaya "Sukses"
# benar-benar berarti APK tercatat di /data/app.
if ! "$ADB" -s "$SERIAL" shell pm path "$APP_ID" >/dev/null 2>&1; then
  die "Pemasangan melaporkan sukses tapi $APP_ID tidak terdaftar di perangkat." 3
fi
printf '\nTerpasang: %s\n' "$("$ADB" -s "$SERIAL" shell pm path "$APP_ID" | tr -d '\r')"

# ---------------------------------------------------------------------------
# Opsional: jalankan aplikasi
# ---------------------------------------------------------------------------
if [[ "$DO_LAUNCH" -eq 1 ]]; then
  printf '\nMenjalankan %s\n' "$MAIN_ACTIVITY"
  "$ADB" -s "$SERIAL" shell am start -n "$MAIN_ACTIVITY" | tr -d '\r'
fi

exit 0