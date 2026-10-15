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
- Offline installed TTS selection, packaged essential English audio, visible instruction text, adjustable narration volume, feedback chime and reduced motion.
- Push-to-talk recognition implementation restricted to Android's explicit on-device recogniser. It is off by default AND build-gated pending device/privacy review (`-PvoiceApproved=true` only after that review). Many Fire tablets cannot provide this recogniser. No cloud recognition fallback. Touch and narration remain available.
- Parent PIN uses random salt + PBKDF2, persistent lockout after repeated failed attempts, and session closure on backgrounding. First setup requires a private six-digit PIN and adult reading confirmation. This gate is not evidence of legally verified parental consent; policy validation is still required.
- Amazon Appstore SDK 3.0.9 adapter, real price retrieval, purchase and paginated restoration, RVS-backed entitlement verification, cancellation handling and offline entitlement persistence. No simulated success in production. Transactions stay disabled until the publisher supplies its Amazon PEM and HTTPS verifier.

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

The six domain tests cover 9,000 matching boards, 3,600 pattern puzzles, invalid taps, match resolution, counts, access rules, commands and receipt identities. Android JVM tests cover persistence, profile scoping and PIN hashing. Instrumented UI tests are included; compilation is distinct from execution. See `docs/VERIFICATION.md` for the actual run results.

## Billing setup

1. Create the app and a one-time ENTITLED IAP with SKU `matchminds.premium.lifetime` in your Amazon developer account. The proposed £3.99 belongs in the store console, not production UI.
2. Download the app's `AppstoreAuthenticationKey.pem` into `app/src/main/assets/`. Debug package suffix must match any separately registered testing app. The public key is not a developer secret.
3. Deploy `server/verify.py` behind HTTPS, rate limiting and request size controls. Set `AMAZON_SHARED_SECRET` on the server only and `AMAZON_RVS_ENV=sandbox` for App Tester or `production` for LAT/production. Keep IDs and secret-bearing RVS URLs out of access/error logs. Production hosting, retention and support contacts require publisher decisions.
4. Build with `-PverificationUrl=https://your-domain.example/verify`. The app sends the Amazon user ID and receipt ID; the server returns verified SKU, identities and cancellation state. Network failures preserve the prior verified entitlement; they never grant a new one. Cancelled verified receipts revoke it. Restore with the purchasing Amazon account after reinstalling.
5. Run App Tester, sandbox receipt tests, DevTest and Live App Testing. Configure actual release signing separately; no publisher signing key is invented or borrowed.

For QA only: `adb shell am start -n com.matchminds.kids.debug/com.matchminds.kids.MainActivity --ez qa_premium true --es qa_screen NUMBERS`. These extras are ignored by release builds. Seeded screenshot scenes make real moves through the game engine; they do not grant or simulate a store purchase.

## Localisation

English UK and US are selectable; the US display adapter changes spelling and the TTS language tag. Other requested languages are explicitly unapproved. `tools/localize.py` extracts UI/domain strings, Android resources, voice commands and listing text into a review catalogue. Supply an HTTPS provider using `MM_TRANSLATION_URL` and `MM_TRANSLATION_KEY`, then `--translate es` (or another target). It preserves interpolation tokens and brand terms and writes candidates plus a missing-translation report. Candidates are NOT automatically loaded into the app. Runtime resource integration, full plurals, human educational review, voice checks and Arabic RTL QA are release work before enabling any further locale. No machine-translated alphabet/phonics lessons are shipped. Store captions and UI must be approved together.

## Artwork and audio

Run `python tools/generate_assets.py` to regenerate editable brand assets and the original chime; `node tools/export_brand.cjs` with `sharp` installed exports PNGs. The bundled local runtime also supplies Sharp. Essential WAVs were generated on this machine with Microsoft Hazel Desktop. Confirm redistribution rights or replace with licensed recordings before release. No external artwork or competitor branding was copied. Speech quality and pronunciation still need device and human checks.

## Release status

This is a buildable implementation, not a store-approved release. Read `docs/RELEASE.md`, `docs/PRIVACY-REVIEW.md` and `docs/VERIFICATION.md`. Native host renders now supply sixteen SEO-captioned store screenshots, editable masters, square Amazon icon variants and a promotional banner; see `docs/STORE-ASSETS.md`. The Amazon draft is created with the supplied public support address; live console progress is recorded in `store/amazon-submission.json`. Do not publish until remaining device, billing, content, legal and asset checks are complete. Run `python tools/check_release.py`; missing evidence deliberately fails the check.
