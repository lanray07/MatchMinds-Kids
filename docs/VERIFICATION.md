# Verification record — 15 October 2026

The source was compiled with Java 17, Gradle 8.13, Android Gradle Plugin 8.11.1, Kotlin 2.1.20 and Android SDK 36 on Windows.

## Evidence

- Core JVM tests: six tests, including 9,000 solvable boards across five matching modes/three difficulties/twelve levels/fifty seeds, and 3,600 deterministic pattern puzzles.
- Android JVM tests: persistence round trips, corruption fallback, profile/world scoping, salted PIN hashing, and a pattern-save difficulty regression.
- Translation validator: three Python tests for placeholder preservation, protected product names, missing entries and empty strings.
- RVS proxy: three Python tests for valid/cancelled receipts, wrong products/receipts, HTTP 410 revocation and server-error rejection. These use mocked Amazon HTTP responses; they are not live store verification.
- Debug APK and Android instrumented-test APK are built. Test source includes child navigation/pause, parent gate, persistence and PIN checks.
- Two opt-in Robolectric Native Graphics render tests pass and produce sixteen actual production-UI renders (eight tablet, eight phone). Game matches and reward acquisition use the real ViewModel and engine. A test-only no-op Amazon broadcast receiver bypasses its JVM bytecode incompatibility; commerce is not tested by this fixture. See `artifacts/host-render-tests.xml` and `docs/STORE-ASSETS.md`.
- Final build/lint log: `artifacts/final-build.log`. Machine-readable test counts, APK sizes and SHA-256 values: `artifacts/verification.json` (generated from Gradle XML, executed Python suites and the actual APK files).

An early lint run caught a local Windows SDK-path property escaping error; the property was corrected. Functional lint warnings about backup configuration and reflected raw-resource lookup were fixed by adding explicit cloud/device-transfer exclusions and direct resource IDs. Remaining tooling/dependency notices and the Amazon SDK's D8/R8 stack-map warnings require tracking; they are not evidence of device compatibility. No warning baseline was used to hide failures.

An overlapping build caused a Gradle file-access conflict in release mapping output. After the older build exited, app build outputs were cleaned and the final build/checks were rerun serially. The earlier failed log is retained as `artifacts/packaging-conflict.log`; final status comes from the subsequent serial run.

## Not verified on a device

`adb devices` reported no authorised devices. Launching the installed tablet emulator first failed with “Android Emulator hypervisor driver is not installed on this machine”. A software-emulation attempt also exited without producing an ADB device. No system drivers or BIOS settings were changed.

Consequently, instrumented UI tests were **compiled, not executed**. No physical-device captures, full accessibility audit, Fire performance measurement, live audio/recognition checks, microphone-permission scenarios or offline device test succeeded. `artifacts/screenshots/tablet/manifest.json` and `phone/manifest.json` explicitly record zero physical captures and the reason. The separate `host-tablet` and `host-phone` folders now contain actual production-UI screenshots rendered under Robolectric. They do not constitute physical Fire-device verification. The phone header and footer wrapping issues found in these renders were repaired before composing store assets.

## No live commerce or publication

The Amazon authentication PEM, SKU configuration, HTTPS verification deployment, developer secret, publisher signing key and release approvals are absent. Billing therefore remains unavailable in the default build. No purchase success, sandbox purchase, Live App Test, title reservation, submission or publication is claimed. The release APK, if present, is unsigned.

## Reproduce

Run `tools/verify_all.ps1` from PowerShell. Add `-DeviceTests` after connecting an authorised device/emulator. Then run both `tools/capture_screenshots.py` variants, inspect every real PNG, and complete the checklist in `docs/RELEASE.md`. `tools/check_release.py` intentionally fails until genuine publisher evidence is recorded.
