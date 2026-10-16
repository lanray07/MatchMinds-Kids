# Verification record â€” 15 October 2026

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

LAT purchase and receipt re-verification now pass (see below). Restore after local entitlement loss, refunds, real account switching, full accessibility, child usability, native translation review and forced low-memory performance remain unverified. The earlier interrupted/locked-device attempts are superseded by the successful 16 October 15-minute run below. Amazon SDK R8 stack-map warnings remain visible. No store approval or publication is claimed. Latest combined unit tests and lint passed in artifacts/extended-final-checks.log. Run tools/report_results.py for actual suite counts and APK hashes.

## Amazon follow-up — 16 October 2026

Production remains Under Review, with no requested changes and the unchanged October 20 estimate. LAT is Test in-progress; the one authorized tester's invitation is Delivered.

Quality Central completed its 43m12s run. Six models passed all three critical install/launch/exit checks with zero performance alerts: Fire HD 10 (2023), Fire Max 11 (2023), Fire 7 (2019), Fire HD 8 (2022), Fire HD 10 (2021), Fire HD 10 (2019). Average cold first-frame latency ranged 0.25–1.05s; average warm latency 0.06–0.28s. Average foreground memory ranged 110.46–168.12 MB. Full average/p50 foreground/background results are in `artifacts/fire-cloud-verification.json`.

Fire 7 (2017) and Fire (2015), both FOS 5, failed installation and skipped launch/exit. The release minimum is API 26; both installation logs explicitly say the devices have API 22. These legacy-device results do not establish a gameplay crash. No lowering of the supported minimum or replacement APK was needed. Cloud tests cover controlled launch/exit and pre-login memory; they do not replace sustained interactive gameplay, native translation review, accessibility review or real LAT purchase/restore cases.

## Physical Fire continuation — 16 October 2026

The authorized, unlocked KFTRWI/API 28 completed the sustained debug UI test in 907.996 seconds. Gameplay elapsed time was 905.704 seconds: 151 completed games, 1,979 UI actions, all 18 mode/difficulty combinations, pause/resume, and 2,508 enabled-control name/touch-bound checks (47dp minimum measurement tolerance). Sampled peak PSS was 140,828 KiB (137.5 MiB). The test passed; see `artifacts/fire-extended-verification.json`.

An unbounded Compose performScrollTo call stalled the original harness at medium animals/card 10. The minimized same-seed round passed in 11.502s using bounded touch swipes and an explicit visibility assertion, then the original full 15-minute loop passed. Only test tooling changed; the production APK was unchanged.

Five process-cold launches of the installed publisher-signed release all passed. Android am start TotalTime values were 366, 249, 232, 231 and 237 ms (median 237 ms). The final PSS snapshot was 67,017 KiB (65.4 MiB). Installed APK SHA-256 matched the signed release artifact. See `artifacts/fire-release-performance.json`. These measurements cover Activity launch, not OS-cold boot, full-content readiness or gameplay frame rate. Debug memory includes instrumentation overhead; neither run certifies absence of leaks or forced-low-memory behavior.

## Amazon-installed LAT commerce — 16 October 2026

Purchase, fresh restore request/receipt re-verification and premium gameplay access passed on KFTRWI API 28 using Amazon TEST 1, installed by com.amazon.venezia. The owner completed Amazon checkout personally. The release showed both purchase-success and verified-restore messages; Ocean discoveries / Number garden opened. See artifacts/fire-lat-verification.json.

The checkout displayed GBP 3.99 without an explicit no-charge notice; the owner confirmed that no real charge occurred. Restore after loss of all local entitlement data, real account switching and store-driven refunds/cancellation remain unverified. The prior sandbox and callback fixtures remain distinct evidence. No credentials, private PINs, customer IDs or receipt IDs are included in public records.
