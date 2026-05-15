#!/usr/bin/env bash
# =============================================================================
#  DataStream — Pre-flight environment check (macOS / Linux / WSL)
#
#  Usage:
#      cd /path/to/DataStream
#      bash scripts/verify-environment.sh
#
#  Exit code: 0 when all required checks pass, non-zero otherwise.
#
#  Read-only. Idempotent. Safe to re-run any time.
#  Mirror of scripts/verify-environment.ps1 — keep them in sync if you edit.
# =============================================================================

set -uo pipefail

# Color helpers (no-op when stdout is not a TTY).
if [[ -t 1 ]]; then
    G='\033[0;32m'; Y='\033[0;33m'; R='\033[0;31m'; C='\033[0;36m'; D='\033[0;90m'; N='\033[0m'
else
    G=''; Y=''; R=''; C=''; D=''; N=''
fi

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

PASS=0; WARN=0; FAIL=0; REQ_FAIL=0
declare -a SUMMARY_LINES=()

record() {
    # $1=name $2=status(PASS|WARN|FAIL) $3=detail $4=remediation $5=required(true|false)
    local name="$1" status="$2" detail="$3" remediation="$4" required="$5"
    local glyph="" color=""
    case "$status" in
        PASS) glyph='[OK]  '; color="$G"; PASS=$((PASS+1));;
        WARN) glyph='[WARN]'; color="$Y"; WARN=$((WARN+1));;
        FAIL) glyph='[FAIL]'; color="$R"; FAIL=$((FAIL+1));
              if [[ "$required" == "true" ]]; then REQ_FAIL=$((REQ_FAIL+1)); fi;;
    esac
    printf "${color}%s %-42s${N} %s\n" "$glyph" "$name" "$detail"
    if [[ "$status" != "PASS" && -n "$remediation" ]]; then
        printf "${D}        Fix:  %s${N}\n" "$remediation"
    fi
}

echo
echo -e "${C}======================================================================${N}"
echo -e "${C}  DataStream  Pre-flight Environment Check${N}"
echo -e "${C}  Repo:  $REPO_ROOT${N}"
echo -e "${C}======================================================================${N}"
echo

# ---- 1. JDK 17+ ------------------------------------------------------------
if command -v java >/dev/null 2>&1; then
    raw="$(java -version 2>&1 | head -n1)"
    ver="$(echo "$raw" | sed -nE 's/.*"([0-9]+)(\.([0-9]+))?.*/\1 \3/p')"
    major="$(echo "$ver" | awk '{print $1}')"
    minor="$(echo "$ver" | awk '{print $2}')"
    [[ "$major" == "1" && -n "$minor" ]] && major="$minor"
    if [[ -z "$major" ]]; then
        record "JDK detected on PATH" "WARN" "$raw" \
                "Install JDK 17 or 21 from https://adoptium.net/" "true"
    elif (( major < 17 )); then
        record "JDK 17+" "FAIL" "found JDK $major" \
                "AGP 8.2 requires JDK 17+. Use Android Studio's bundled JDK or install JDK 17 from https://adoptium.net/." "true"
    else
        record "JDK 17+" "PASS" "JDK $major OK" "" "true"
    fi
else
    record "JDK 17+" "WARN" "no java on PATH" \
            "Optional if you only use Android Studio (it bundles its own JDK 17)." "false"
fi

# ---- 2. git ----------------------------------------------------------------
if command -v git >/dev/null 2>&1; then
    record "git installed" "PASS" "$(git --version)" "" "true"
else
    record "git installed" "FAIL" "not found" \
            "Install: https://git-scm.com/downloads" "true"
fi

# ---- 3. Disk space (>=10 GB free) ------------------------------------------
if command -v df >/dev/null 2>&1; then
    avail_mb=$(df -m -P "$REPO_ROOT" | awk 'NR==2{print $4}')
    if [[ -n "$avail_mb" ]]; then
        avail_gb=$(( avail_mb / 1024 ))
        if   (( avail_gb >= 10 )); then
            record "Disk space (>=10 GB free)" "PASS" "${avail_gb} GB free" "" "true"
        elif (( avail_gb >= 5 )); then
            record "Disk space" "WARN" "${avail_gb} GB free (10 GB recommended)" \
                    "Free up disk if you plan to develop heavily." "true"
        else
            record "Disk space" "FAIL" "${avail_gb} GB free (need >=10 GB)" \
                    "Free up disk before opening Android Studio." "true"
        fi
    else
        record "Disk space" "WARN" "unable to query" "" "false"
    fi
else
    record "Disk space" "WARN" "df not available" "" "false"
fi

# ---- 4-6. Network reachability ---------------------------------------------
test_endpoint() {
    local url="$1" label="$2" required="$3"
    if command -v curl >/dev/null 2>&1; then
        local code
        code=$(curl -s -o /dev/null -m 8 -w "%{http_code}" -I "$url" 2>/dev/null || echo "000")
        if [[ "$code" =~ ^[23] ]]; then
            record "$label" "PASS" "HTTP $code" "" "$required"
        elif [[ "$code" == "000" ]]; then
            record "$label" "WARN" "unreachable" \
                    "Check network / VPN / corporate firewall. URL: $url" "$required"
        else
            record "$label" "WARN" "HTTP $code" \
                    "Endpoint reached but returned non-2xx; usually still works for Gradle." "$required"
        fi
    else
        record "$label" "WARN" "curl missing" "Install curl to verify network." "false"
    fi
}

test_endpoint "https://repo.maven.apache.org/maven2/"       "Maven Central reachable"           "true"
test_endpoint "https://dl.google.com/dl/android/maven2/"    "Google Maven reachable"            "true"
test_endpoint "https://jitpack.io/"                          "JitPack reachable (MPAndroidChart)" "true"

# ---- 7. gradle-wrapper.jar -------------------------------------------------
JAR="$REPO_ROOT/gradle/wrapper/gradle-wrapper.jar"
if [[ -f "$JAR" ]]; then
    size=$(wc -c < "$JAR" | tr -d ' ')
    if [[ "$size" -eq 43462 ]]; then
        record "gradle-wrapper.jar OK" "PASS" "43,462 bytes (Gradle 8.5 canonical)" "" "true"
    else
        record "gradle-wrapper.jar size" "WARN" "$size bytes (expected 43,462)" \
                "Restore from fresh clone or run 'gradle wrapper --gradle-version 8.5'." "true"
    fi
else
    record "gradle-wrapper.jar" "FAIL" "missing!" \
            "Restore from fresh clone or run 'gradle wrapper --gradle-version 8.5'." "true"
fi

# ---- 8. local.properties or template ---------------------------------------
if [[ -f "$REPO_ROOT/local.properties" ]]; then
    record "local.properties present" "PASS" "exists" "" "true"
elif [[ -f "$REPO_ROOT/local.properties.template" ]]; then
    record "local.properties template" "PASS" "template exists; AS creates local.properties on first sync" "" "true"
else
    record "local.properties" "WARN" "neither present" \
            "AS auto-generates it, but the template makes troubleshooting clearer." "false"
fi

# ---- 9. Android Studio detection -------------------------------------------
AS_PATHS=(
    "/Applications/Android Studio.app"
    "/Applications/Android Studio Preview.app"
    "/snap/android-studio/current/android-studio"
    "/opt/android-studio/bin/studio.sh"
    "$HOME/android-studio/bin/studio.sh"
)
AS_FOUND=""
for p in "${AS_PATHS[@]}"; do
    if [[ -e "$p" ]]; then AS_FOUND="$p"; break; fi
done
if [[ -n "$AS_FOUND" ]]; then
    record "Android Studio installed" "PASS" "$AS_FOUND" "" "true"
else
    record "Android Studio installed" "WARN" "not detected at standard paths" \
            "Install from https://developer.android.com/studio" "true"
fi

# ---- 10. ANDROID_HOME / SDK detection --------------------------------------
SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK_ROOT" ]]; then
    if [[ -d "$HOME/Library/Android/sdk" ]]; then SDK_ROOT="$HOME/Library/Android/sdk"; fi
    if [[ -d "$HOME/Android/Sdk" ]];          then SDK_ROOT="$HOME/Android/Sdk"; fi
fi
if [[ -n "$SDK_ROOT" && -d "$SDK_ROOT" ]]; then
    record "Android SDK detected" "PASS" "$SDK_ROOT" "" "false"
else
    record "Android SDK detected" "WARN" "not found at standard path" \
            "AS will install it on first run. Or set ANDROID_HOME manually." "false"
fi

# -----------------------------------------------------------------------------
# Summary
# -----------------------------------------------------------------------------
echo
echo -e "${C}----------------------------------------------------------------------${N}"
printf "${C}  Summary:  %d PASS  /  %d WARN  /  %d FAIL    (required failures: %d)${N}\n" "$PASS" "$WARN" "$FAIL" "$REQ_FAIL"
echo -e "${C}----------------------------------------------------------------------${N}"
echo

if (( REQ_FAIL == 0 )); then
    echo -e "${G}READY:${N} Open the repo folder in Android Studio (File -> Open),"
    echo -e "       trust the project, wait for Gradle sync to go green,"
    echo -e "       then hit Run on a Pixel 5 / API 34 emulator."
    echo
    echo -e "${C}Next:${N} read TRY_THIS_FIRST.md (3-min happy path)."
    exit 0
else
    echo -e "${R}NOT READY:${N} fix the FAIL items above, then re-run this script."
    echo -e "${R}          See TROUBLESHOOTING.md for detailed remediation.${N}"
    exit 1
fi
