<#
    .SYNOPSIS
        Pre-flight verification before opening DataStream in Android Studio.

    .DESCRIPTION
        Walks 10 checks (JDK, git, disk, network, repo files, Android Studio
        installation) and prints a green/red checklist. Exits with code 0 when
        every required check passes, non-zero otherwise.

        Run BEFORE opening Android Studio for the first time on a new machine
        — saves you 5-15 min of "why is sync failing?" forensics.

    .EXAMPLE
        cd C:\path\to\DataStream
        .\scripts\verify-environment.ps1

    .NOTES
        Read-only. Idempotent. Safe to re-run any time.
        Repo:  https://github.com/mtoanng/DataStream
        Maps to: TRY_THIS_FIRST.md, SETUP_CHECKLIST.md, TROUBLESHOOTING.md
#>

[CmdletBinding()]
param(
    [switch]$Quiet,        # suppress remediation hints
    [switch]$JsonOutput    # emit machine-readable JSON instead of pretty text
)

$ErrorActionPreference = 'Continue'   # we WANT each check to keep going on a fail

# -----------------------------------------------------------------
# Helpers
# -----------------------------------------------------------------
$script:Results = @()
$script:RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path

function Add-Result {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [ValidateSet('PASS', 'WARN', 'FAIL')] [string]$Status,
        [string]$Detail = '',
        [string]$Remediation = '',
        [bool]$Required = $true
    )
    $script:Results += [pscustomobject]@{
        Name        = $Name
        Status      = $Status
        Detail      = $Detail
        Remediation = $Remediation
        Required    = $Required
    }
}

function Write-Check {
    param([pscustomobject]$Result)
    if ($script:JsonOutput) { return }
    $glyph = switch ($Result.Status) {
        'PASS' { '[OK]   ' }
        'WARN' { '[WARN] ' }
        'FAIL' { '[FAIL] ' }
    }
    $color = switch ($Result.Status) {
        'PASS' { 'Green' }
        'WARN' { 'Yellow' }
        'FAIL' { 'Red' }
    }
    Write-Host ("{0}{1,-42} {2}" -f $glyph, $Result.Name, $Result.Detail) -ForegroundColor $color
    if ($Result.Status -ne 'PASS' -and -not $Quiet -and $Result.Remediation) {
        Write-Host ("        Fix:  {0}" -f $Result.Remediation) -ForegroundColor DarkGray
    }
}

# -----------------------------------------------------------------
# Banner
# -----------------------------------------------------------------
if (-not $JsonOutput) {
    Write-Host ''
    Write-Host '======================================================================' -ForegroundColor Cyan
    Write-Host '  DataStream  Pre-flight Environment Check'                           -ForegroundColor Cyan
    Write-Host "  Repo:  $script:RepoRoot"                                            -ForegroundColor DarkGray
    Write-Host '======================================================================' -ForegroundColor Cyan
    Write-Host ''
}

# -----------------------------------------------------------------
# 1. JDK 17+ on PATH
# -----------------------------------------------------------------
try {
    $javaOut = & java -version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0 -or -not $javaOut) { throw 'java missing' }

    $verLine = ($javaOut -split "`n" | Select-Object -First 1).Trim()
    $major = $null
    if ($verLine -match '"(\d+)(?:\.(\d+))?') {
        $major = [int]$matches[1]
        # Java 1.8 reports as 1.8.x — second group is the real major.
        if ($major -eq 1 -and $matches[2]) { $major = [int]$matches[2] }
    }
    if ($null -eq $major) {
        Add-Result 'JDK detected on PATH' 'WARN' "version unparseable: $verLine" `
            'Install JDK 17 or 21 from https://adoptium.net/' $true
    }
    elseif ($major -lt 17) {
        Add-Result 'JDK 17+' 'FAIL' "found JDK $major" `
            'AGP 8.2 requires JDK 17+. Use Android Studio''s bundled JDK or install one from https://adoptium.net/.' $true
    }
    else {
        Add-Result 'JDK 17+' 'PASS' "JDK $major OK" '' $true
    }
}
catch {
    Add-Result 'JDK 17+' 'WARN' 'no java on PATH' `
        'Optional if you only use Android Studio (it bundles its own JDK 17). Install JDK 17 only if you want to run gradlew from terminal.' $false
}

# -----------------------------------------------------------------
# 2. git executable
# -----------------------------------------------------------------
try {
    $gitVer = (& git --version 2>$null).Trim()
    if ($LASTEXITCODE -eq 0 -and $gitVer) {
        Add-Result 'git installed' 'PASS' $gitVer '' $true
    }
    else { throw 'git missing' }
}
catch {
    Add-Result 'git installed' 'FAIL' 'not found' `
        'Install Git for Windows: https://git-scm.com/download/win' $true
}

# -----------------------------------------------------------------
# 3. >= 10 GB free on the repo's drive
# -----------------------------------------------------------------
try {
    $drive = (Get-Item $script:RepoRoot).PSDrive
    $freeGb = [math]::Round($drive.Free / 1GB, 1)
    if ($freeGb -ge 10) {
        Add-Result 'Disk space (>=10 GB free)' 'PASS' "$freeGb GB free on $($drive.Name):" '' $true
    }
    elseif ($freeGb -ge 5) {
        Add-Result 'Disk space' 'WARN' "$freeGb GB free on $($drive.Name): (10 GB recommended)" `
            'Gradle caches + emulator images can hit ~8 GB. Free up disk if you plan to develop heavily.' $true
    }
    else {
        Add-Result 'Disk space' 'FAIL' "$freeGb GB free on $($drive.Name): (need >=10 GB)" `
            'Free up disk before opening Android Studio — Gradle sync alone needs ~3 GB.' $true
    }
}
catch {
    Add-Result 'Disk space' 'WARN' 'unable to query drive' '' $false
}

# -----------------------------------------------------------------
# 4-6. Network connectivity to Maven Central / Google Maven / JitPack
#       (HEAD request, 5 s timeout each, no body downloaded)
# -----------------------------------------------------------------
function Test-NetEndpoint {
    param([string]$Url, [string]$Label, [bool]$Required = $true)
    try {
        $res = Invoke-WebRequest -Uri $Url -Method Head -TimeoutSec 8 `
                                 -UseBasicParsing -ErrorAction Stop
        $code = $res.StatusCode
        if ($code -ge 200 -and $code -lt 400) {
            Add-Result $Label 'PASS' "HTTP $code" '' $Required
        }
        else {
            Add-Result $Label 'WARN' "HTTP $code" `
                'Endpoint reached but returned non-2xx; likely a CDN tweak — Gradle should still resolve. Re-run later if unsure.' $Required
        }
    }
    catch {
        $msg = $_.Exception.Message
        if ($msg -match 'proxy|407|untrusted|SSL') {
            Add-Result $Label 'WARN' 'reachable but proxy blocks' `
                "Configure HTTPS proxy in gradle.properties (see TROUBLESHOOTING.md section 16)." $Required
        }
        else {
            Add-Result $Label 'WARN' 'unreachable' `
                "Gradle sync will fail. Check your network / VPN / corporate firewall. URL: $Url" $Required
        }
    }
}

Test-NetEndpoint 'https://repo.maven.apache.org/maven2/' 'Maven Central reachable'           $true
Test-NetEndpoint 'https://dl.google.com/dl/android/maven2/' 'Google Maven reachable'          $true
Test-NetEndpoint 'https://jitpack.io/'                       'JitPack reachable (MPAndroidChart)' $true

# -----------------------------------------------------------------
# 7. gradle-wrapper.jar exists + correct size
# -----------------------------------------------------------------
$wrapperJar = Join-Path $script:RepoRoot 'gradle\wrapper\gradle-wrapper.jar'
if (Test-Path $wrapperJar) {
    $size = (Get-Item $wrapperJar).Length
    if ($size -eq 43462) {
        Add-Result 'gradle-wrapper.jar OK' 'PASS' "43,462 bytes (Gradle 8.5 canonical)" '' $true
    }
    else {
        Add-Result 'gradle-wrapper.jar size' 'WARN' "$size bytes (expected 43,462)" `
            'A future Gradle bump may be in flight. If sync fails, restore from a fresh clone or run "gradle wrapper --gradle-version 8.5".' $true
    }
}
else {
    Add-Result 'gradle-wrapper.jar' 'FAIL' 'missing!' `
        'Restore from a fresh git clone, or run "gradle wrapper --gradle-version 8.5" if you have a system Gradle.' $true
}

# -----------------------------------------------------------------
# 8. local.properties exists OR template ready to copy
# -----------------------------------------------------------------
$localProps = Join-Path $script:RepoRoot 'local.properties'
$tmplProps  = Join-Path $script:RepoRoot 'local.properties.template'
if (Test-Path $localProps) {
    Add-Result 'local.properties present' 'PASS' 'exists (Android Studio may have generated it)' '' $true
}
elseif (Test-Path $tmplProps) {
    Add-Result 'local.properties template' 'PASS' 'template exists; AS will create local.properties on first sync' '' $true
}
else {
    Add-Result 'local.properties' 'WARN' 'neither file present' `
        'Android Studio should still auto-generate it on first sync, but having local.properties.template makes troubleshooting clearer.' $false
}

# -----------------------------------------------------------------
# 9. Android Studio detected
# -----------------------------------------------------------------
$asPaths = @(
    'C:\Program Files\Android\Android Studio\bin\studio64.exe',
    'C:\Program Files\Android\Android Studio Preview\bin\studio64.exe',
    "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\AndroidStudio\ch-0",
    "$env:LOCALAPPDATA\Programs\Android Studio\bin\studio64.exe"
)
$found = $asPaths | Where-Object { Test-Path $_ } | Select-Object -First 1
if ($found) {
    Add-Result 'Android Studio installed' 'PASS' $found '' $true
}
else {
    Add-Result 'Android Studio installed' 'WARN' 'not detected at standard paths' `
        'Install Android Studio Hedgehog 2023.1+ from https://developer.android.com/studio' $true
}

# -----------------------------------------------------------------
# 10. Optional: Android SDK + ANDROID_HOME (helpful but Android Studio
#     creates this on first sync)
# -----------------------------------------------------------------
$sdkRoot = $env:ANDROID_HOME
if (-not $sdkRoot) { $sdkRoot = $env:ANDROID_SDK_ROOT }
if (-not $sdkRoot) { $sdkRoot = "$env:LOCALAPPDATA\Android\Sdk" }
if (Test-Path $sdkRoot) {
    Add-Result 'Android SDK detected' 'PASS' $sdkRoot '' $false
}
else {
    Add-Result 'Android SDK detected' 'WARN' 'not found at standard path' `
        'AS will install it on first run. To set ANDROID_HOME manually: [Environment]::SetEnvironmentVariable("ANDROID_HOME","$env:LOCALAPPDATA\Android\Sdk","User")' $false
}

# -----------------------------------------------------------------
# Render results
# -----------------------------------------------------------------
foreach ($r in $script:Results) { Write-Check $r }

if ($script:JsonOutput) {
    $script:Results | ConvertTo-Json -Depth 4
    if (($script:Results | Where-Object { $_.Required -and $_.Status -eq 'FAIL' }).Count -gt 0) { exit 1 } else { exit 0 }
}

# -----------------------------------------------------------------
# Summary
# -----------------------------------------------------------------
$pass = ($script:Results | Where-Object { $_.Status -eq 'PASS' }).Count
$warn = ($script:Results | Where-Object { $_.Status -eq 'WARN' }).Count
$fail = ($script:Results | Where-Object { $_.Status -eq 'FAIL' }).Count
$reqFail = ($script:Results | Where-Object { $_.Required -and $_.Status -eq 'FAIL' }).Count
$total = $script:Results.Count

Write-Host ''
Write-Host '----------------------------------------------------------------------' -ForegroundColor Cyan
Write-Host ("  Summary:  {0} PASS  /  {1} WARN  /  {2} FAIL    (required failures: {3})" -f $pass, $warn, $fail, $reqFail) -ForegroundColor Cyan
Write-Host '----------------------------------------------------------------------' -ForegroundColor Cyan
Write-Host ''

if ($reqFail -eq 0) {
    Write-Host 'READY: Open the repo folder in Android Studio (File -> Open),' -ForegroundColor Green
    Write-Host '       trust the project, wait for Gradle sync to go green,'    -ForegroundColor Green
    Write-Host '       then hit Run on a Pixel 5 / API 34 emulator.'            -ForegroundColor Green
    Write-Host ''
    Write-Host 'Next: read TRY_THIS_FIRST.md (3-min happy path).' -ForegroundColor DarkCyan
    exit 0
}
else {
    Write-Host 'NOT READY: fix the FAIL items above, then re-run this script.' -ForegroundColor Red
    Write-Host '          See TROUBLESHOOTING.md for detailed remediation.'    -ForegroundColor Red
    exit 1
}
