param([switch]$CreateKey, [switch]$Build, [string]$VerificationUrl = '')
$ErrorActionPreference = 'Stop'
$releaseHome = Join-Path $env:USERPROFILE '.android/matchminds-release'
$releaseKey = Join-Path $releaseHome 'publisher.p12'
$releasePasswordFile = Join-Path $releaseHome 'password.dpapi'
if ($CreateKey -and !(Test-Path -LiteralPath $releaseKey)) {
    New-Item -ItemType Directory -Force -Path $releaseHome | Out-Null
    $releaseIdentity = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
    & icacls.exe $releaseHome /inheritance:r /grant:r "${releaseIdentity}:(OI)(CI)F" | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Could not restrict signing directory permissions.' }
    $releaseBytes = [byte[]]::new(36)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($releaseBytes)
    $releasePassword = [Convert]::ToBase64String($releaseBytes)
    ConvertTo-SecureString $releasePassword -AsPlainText -Force | ConvertFrom-SecureString | Set-Content -LiteralPath $releasePasswordFile
    $env:MM_RELEASE_PASSWORD = $releasePassword
    try {
        & "$env:JAVA_HOME/bin/keytool.exe" -genkeypair -keystore $releaseKey -storetype PKCS12 -alias publisher -keyalg RSA -keysize 4096 -validity 10000 -dname 'CN=MatchMinds Kids, O=Lanray' -storepass:env MM_RELEASE_PASSWORD -keypass:env MM_RELEASE_PASSWORD -noprompt
        if ($LASTEXITCODE -ne 0) { throw 'Key generation failed.' }
    } finally { Remove-Item Env:MM_RELEASE_PASSWORD -ErrorAction SilentlyContinue; $releasePassword = $null }
}
if (!(Test-Path -LiteralPath $releaseKey)) { throw 'Signing key is missing. Run with -CreateKey or configure an existing publisher key.' }
if ($Build) {
    $releaseSecure = Get-Content -LiteralPath $releasePasswordFile | ConvertTo-SecureString
    $env:MM_RELEASE_PASSWORD = [System.Net.NetworkCredential]::new('', $releaseSecure).Password
    $env:MM_RELEASE_KEYSTORE = $releaseKey
    try {
        $releaseArgs = @(':app:assembleRelease', '--no-daemon', '--max-workers=2', '--console=plain')
        if ($VerificationUrl) { $releaseArgs += "-PverificationUrl=$VerificationUrl" }
        & "$PSScriptRoot/../gradlew.bat" @releaseArgs
        if ($LASTEXITCODE -ne 0) { throw 'Release build failed.' }
    } finally { Remove-Item Env:MM_RELEASE_PASSWORD, Env:MM_RELEASE_KEYSTORE -ErrorAction SilentlyContinue }
}
Write-Output "Publisher signing key: $releaseKey"
Write-Output 'The password is protected by Windows DPAPI for this Windows account. Back up the key and export the password securely before changing machines. Neither belongs in Git.'
