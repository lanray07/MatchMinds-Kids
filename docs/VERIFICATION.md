# Verification record — 15 October 2026

## Current continuation results

These results supersede the earlier baseline sections below:

- Six instrumented tests passed on a physical KFTRWI Fire tablet (API 28) with Wi-Fi disabled, then Wi-Fi was restored. The tests cover gameplay/pause, persistence, PIN storage, parent access and setting/persisting French. See `artifacts/fire-offline-tests.xml`.
- Twelve runtime locale catalogs are integrated. Ten non-English variants are translation previews awaiting native educational and pronunciation approval. A host render test exercised welcome, animal and shape screens in all twelve locales (36 renders); 30 localized store compositions are previews.
- The entitlement SKU and public Amazon PEM are configured. Production and sandbox receipt-verification endpoints are deployed to Vercel, with the shared secret held server-side. Seven mocked server tests pass. The physical App Tester purchase and restore test passed through the sandbox verifier; no real payment was made. See `artifacts/fire-sandbox-verification.json`. Live commerce, refunds and account switching remain separate checks.
- The Fire tablet synthesized both English variants with installed offline voices. No suitable installed offline voice was present for the other ten variants; those use visible text. See `artifacts/fire-voice-availability.json`. This is synthesis evidence, not human pronunciation approval.
- The release build passed and Android apksigner verified the publisher signature (v2). Certificate SHA-256: becf54eb54fd88500eebed60c3c69a37313d6db7edba42854f4161f417964aad. The key and protected password remain outside Git. See artifacts/release-signature.txt. The signed production APK installed and launched on the physical KFTRWI (API 28); its welcome screen was visually reviewed, it remained foreground, and no matching crash-log entry was found. See artifacts/fire-release-launch.json.
- Packaged desktop narration recordings were removed. The app selects installed offline voices and falls back to visible text. Microphone commands and their permission are excluded from the default build.

## Remaining verification

Live commerce, refunds, account switching, Live App Testing, full accessibility, child usability, native translation review and long-session/low-RAM performance remain unverified. Amazon SDK R8 stack-map warnings remain visible. No store approval or publication is claimed. Final unit tests and lint passed in artifacts/final-localized-checks.log. Run tools/report_results.py for actual suite counts and APK hashes.
