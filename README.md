# MatchMinds Kids

Native Kotlin / Jetpack Compose game for Android and Fire tablets (Android 8 / API 26 or newer). “Little matches. Big discoveries.”

## Run

Open this directory in Android Studio, let Gradle sync, and run `app`. Or use Java 17 and an Android SDK:

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug package is `com.matchminds.kids.debug`; production is `com.matchminds.kids`. `local.properties` is machine-specific and excluded from Git. There is no dependency on Google Play Services, no tracking, no accounts, no ads and no subscription.

## Implemented

- Splash, welcome, four local profiles, three themed world selections, six modes, matching boards, pattern questions, completion, badges, progress, parent PIN, settings, language selection, accessibility and purchase management.
- Seeded immutable matching engine: 4–6 / 12 / 20 cards, distinct quantities and numerals, uppercase/lowercase English pairs, deterministic AB / AAB / ABCB patterns. Twelve levels per mode, per world and difficulty. Pattern levels have five questions. Worlds are cosmetic destinations using the same educational pool.
- All twelve animal and ten object illustrations, eight shapes with colour names and distinct geometry, original Milo vector art with six poses. In-app vector masters: `app/src/main/java/com/matchminds/kids/ui/Artwork.kt`. Standalone editable brand SVGs and PNG exports: `artifacts/brand`.
- DataStore persistence for profiles, preferences, best stars and unfinished sessions. Rotation retains the ViewModel; process restart offers Continue. Completed level rewards use stable keys and best-star updates.
- Installed offline TTS selection, visible instruction text, adjustable narration volume, original feedback chime and reduced motion. Speech falls back to visible text when a suitable installed offline voice is unavailable.
- Push-to-talk recognition implementation restricted to Android's explicit on-device recogniser. It is off by default AND build-gated pending device/privacy review (`-PvoiceApproved=true` only after that review). Many Fire tablets cannot provide this recogniser. No cloud recognition fallback. Touch and narration remain available.
- Parent PIN uses random salt + PBKDF2, persistent lockout after repeated failed attempts, and session closure on backgrounding. First setup requires a private six-digit PIN and adult reading confirmation. This gate is not evidence of legally verified parental consent; policy validation is still required.
- Amazon Appstore SDK 3.0.9 adapter, store price retrieval, purchase and paginated restoration, RVS-backed entitlement verification, cancellation handling and offline entitlement persistence. The public PEM is included and the HTTPS verifier is deployed; sandbox purchase and restore passed; live commerce remains a separate release requirement.

## Structure

`core` is a platform-independent domain module. `app/data` owns storage; `GameViewModel` coordinates state and coroutines; Compose renders state; `voice` and `billing` isolate device/store APIs. `server` supplies the RVS proxy source. `tools` handles translation candidates, asset exports, screenshots and release checks. Framework UI, device speech and Amazon callbacks require device validation.

## Tests and checks

```powershell
.\gradlew.bat :core:test :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
python -m unittest discover -s tools -p 'test_*.py'
python -m unittest discover -s server -p 'test_*.py'
python tools/localize.py
# Requires a booted emulator / connected device:
.\gradlew.bat :app:connectedDebugAndroidTest
python tools/capture_screenshots.py --variant tablet
python tools/capture_screenshots.py --variant phone
```

The six original domain tests cover 9,000 matching boards, 3,600 pattern puzzles, invalid taps, match resolution, counts, access rules, commands and receipt identities. Android JVM tests cover persistence, profile scoping and PIN hashing. Instrumented UI tests are included; compilation is distinct from execution. See `docs/VERIFICATION.md` for the actual run results.

## Billing setup

The Amazon one-time ENTITLED SKU is `matchminds.premium.lifetime`, with a GBP base price of 3.99 in the console. The app displays Amazon's localized price. The public `AppstoreAuthenticationKey.pem` is included.

Production verification is deployed at `https://matchminds-kids-verifier.vercel.app/verify`; `/api/sandbox` is a separate App Tester handler. `server/vercel` contains the handlers and request limits. The shared secret is encrypted on Vercel and never belongs in the app or Git. Receipt identifiers are processed transiently; see PRIVACY.md for provider metadata handling.

A physical App Tester purchase and restore passed without real payment. Live commerce/refunds/account switching and LAT remain separate checks. Build a production release with `tools/release-signing.ps1 -Build -VerificationUrl https://matchminds-kids-verifier.vercel.app/verify`. The existing publisher key and DPAPI-protected password are outside this repository; preserve them securely.

For QA only: `adb shell am start -n com.matchminds.kids.debug/com.matchminds.kids.MainActivity --ez qa_premium true --es qa_screen NUMBERS`. These extras are ignored by release builds. Seeded screenshot scenes make real moves through the game engine; they do not grant or simulate a store purchase.

## Localisation

The app offers English UK/US and translation previews for Spanish, French, German, Italian, Brazilian/European Portuguese, Dutch, Polish, Arabic and Hindi. Run `python tools/localize_store.py` to compile the TSV catalogs and store-copy previews. Dynamic names and prices remain opaque; Arabic uses RTL layout and bidi-isolated arguments. A–Z remains explicitly English. The ten translated variants require native educational and pronunciation review before being advertised as fully supported. See `localization/runtime-report.json`; localized screenshot previews are in `artifacts/localized-store-assets`.

## Artwork and audio

Run `python tools/generate_assets.py` to regenerate editable brand assets and the original chime; `node tools/export_brand.cjs` with `sharp` installed exports PNGs. Previously generated desktop speech WAVs have been removed from the app pending redistribution review. Narration uses suitable installed offline voices. Speech quality and pronunciation still need human review.

## Release status

This is a buildable implementation, not a store-approved release. Read `docs/RELEASE.md`, `docs/PRIVACY-REVIEW.md` and `docs/VERIFICATION.md`. Native host renders now supply sixteen SEO-captioned store screenshots, editable masters, square Amazon icon variants and a promotional banner; see `docs/STORE-ASSETS.md`. The Amazon draft is created with the supplied public support address; live console progress is recorded in `store/amazon-submission.json`. Do not publish until remaining device, billing, content, legal and asset checks are complete. Run `python tools/check_release.py`; missing evidence deliberately fails the check.
