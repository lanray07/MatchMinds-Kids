param([switch]$DeviceTests)
$ErrorActionPreference='Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & .\gradlew.bat :core:test :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug :app:assembleRelease --console=plain --max-workers=2
    if($LASTEXITCODE -ne 0) { throw 'Android build/checks failed' }
    & python -m unittest discover -s tools -p 'test_*.py'
    if($LASTEXITCODE -ne 0) { throw 'Translation tests failed' }
    & python -m unittest discover -s server -p 'test_*.py'
    if($LASTEXITCODE -ne 0) { throw 'Receipt verifier tests failed' }
    & python tools/localize.py
    if($LASTEXITCODE -ne 0) { throw 'Localisation report failed' }
    if($DeviceTests) {
        & .\gradlew.bat :app:connectedDebugAndroidTest --console=plain
        if($LASTEXITCODE -ne 0) { throw 'Instrumented tests failed' }
    }
} finally { Pop-Location }
