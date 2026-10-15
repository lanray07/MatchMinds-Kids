# Verification record — 15 October 2026

## Current continuation results

These results supersede the earlier baseline sections below:

- Six instrumented tests passed on a physical KFTRWI Fire tablet (API 28) with Wi-Fi disabled, then Wi-Fi was restored. The tests cover gameplay/pause, persistence, PIN storage, parent access and setting/persisting French. See `artifacts/fire-offline-tests.xml`.
- Twelve runtime locale catalogs are integrated. Ten non-English variants are translation previews awaiting native educational and pronunciation approval. A host render test exercised welcome, animal and shape screens in all twelve locales (36 renders); 30 localized store compositions are previews.
- The entitlement SKU and public Amazon PEM are configured. Production and sandbox receipt-verification endpoints are deployed to Vercel, with the shared secret held server-side. Eleven mocked server tests pass, covering cancelled and mismatched receipts, HTTP 410 and upstream failures. The physical App Tester purchase and restore test passed through the sandbox verifier; no real payment was made. See `artifacts/fire-sandbox-verification.json`. Live commerce, refunds and account switching remain separate checks.
- The Fire tablet synthesized both English variants with installed offline voices. No suitable installed offline voice was present for the other ten variants; those use visible text. See `artifacts/fire-voice-availability.json`. This is synthesis evidence, not human pronunciation approval.
- The release build passed and Android apksigner verified the publisher signature (v2). Certificate SHA-256: becf54eb54fd88500eebed60c3c69a37313d6db7edba42854f4161f417964aad. The key and protected password remain outside Git. See artifacts/release-signature.txt. The signed production APK installed and launched on the physical KFTRWI (API 28); its welcome screen was visually reviewed, it remained foreground, and no matching crash-log entry was found. See artifacts/fire-release-launch.json.
- Packaged desktop narration recordings were removed. The app selects installed offline voices and falls back to visible text. Microphone commands and their permission are excluded from the default build.

## Continuation checks

The combined build passed 21 JVM tests, including 72 new host-rendered scenes at 150% font in portrait and landscape; debug APK/test assembly and lint passed (zero errors). Fourteen Python tests passed (eleven server, three tooling). Physical Fire instrumentation passed one test covering five simulated Amazon callback cases: cached access, changed fixture owner, failed purchase, failed restore and failed user lookup. This does not replace real account-switch or checkout tests. See `artifacts/fire-billing-callbacks.json`.

Native review packs contain 289 entries for each of ten runtime variants plus 163 store/purchase fields. No conflicting duplicate translation sources were found. Every human decision remains PENDING. Header clipping at large font and 3.82:1 contrast on the locked-content helper label are recorded in LIVE-VALIDATION.md. These audits do not certify accessibility.

Amazon production is Under Review. A dedicated Live App Test is Test submitted with one authorized adult tester, and the Quality Central Fire Tablets Functional/Performance run is In progress. Submitted or queued tests are not passes; see `artifacts/store-follow-up.json`.

## Remaining verification

Live commerce, refunds, real account switching, completed Live App Testing, full accessibility, child usability, native translation review and long-session/low-RAM performance remain unverified. A long-session attempt lost its Compose hierarchy when device focus was lost; a subsequent attempt was blocked by the lock screen. No passing soak is claimed. Amazon SDK R8 stack-map warnings remain visible. No store approval or publication is claimed. Latest combined unit tests and lint passed in artifacts/extended-final-checks.log. Run tools/report_results.py for actual suite counts and APK hashes.
