# Release checklist and requirement status

The source project and debug APK are delivered for development and testing. Store launch is not complete.

| Area | Implementation | Evidence still needed |
| --- | --- | --- |
| Native Android foundation | Kotlin, Compose, MVVM, separate domain module, DataStore, coroutines | Cold start, smoothness, RAM/GPU and thermal measurements on low-powered Fire hardware |
| Navigation | All thirteen requested screen types connected | Visual/device QA on portrait/landscape, large fonts and TalkBack |
| Matching | Five pair-based categories; sixth mode uses deterministic repeating-pattern rules | Real child usability and educational vocabulary review |
| Content | 12 animals, 8 shapes with labelled colours, numbers 1–20, English A–Z case pairs, 10 objects | Optional animal sound recordings are not included; numbers currently use stars rather than varied quantity objects |
| Difficulty | 4–6, 12 and 20 cards; parent override, no forced timer | Mixed-category boards and optional timed challenges are not included |
| Progress | Best stars, six achievement thresholds, completed-level counts, profile/category breakdown | No developmental assessment or scientifically proven outcomes claimed |
| Profiles | Up to four local nicknames/avatars, per-profile difficulty, deletion | Avatar set is fixed; collectible badges are not playable characters |
| Worlds | Three cosmetic destinations, per-world progression | No separate world-specific content pools; additional themes can follow |
| Voice output | Offline TTS filtering, essential packaged WAVs, visible text fallback | Pronunciation/voice quality checks, packaged-audio redistribution rights |
| Voice input | Constrained English commands, on-device capability check, parent switch, permission, explicit tap and stop, no app recording/transcript persistence | Build flag off until privacy/device review; supported recogniser may be absent from Fire devices. Do not advertise offline commands until independently verified |
| Gate | Private 6-digit PIN, salted hash, persistent attempt cooldown, background lock | Adult first-setup assurance and Amazon policy review; this is not legal consent verification |
| Billing | SDK 3.0.9, localised prices, restore pagination, RVS proxy, cancellation handling | Publisher PEM, real product, verifier deployment, signing, App Tester/DevTest/LAT; no successful live transaction tested |
| Localisation | English UK/US selection; provider-configurable extraction/candidate/validation reports | Ten requested non-English locales remain unapproved; runtime string integration, native content and speech review, plurals and RTL QA remain |
| Assets | Vector in-app illustrations; six editable Milo poses, icon layers, card-back, backgrounds and PNG brand exports | More elaborate original illustrations/animation and full store banner set can be commissioned; visual UI review not performed on device |
| Screenshots | Eight tablet and eight phone compositions from actual production-UI host renders, editable SVGs, square store icons and a 1024×500 banner; connected-device pipeline retained | Physical Fire captures and hardware QA remain blocked; see docs/STORE-ASSETS.md for host-render provenance |
| Listing | English descriptions, feature bullets, keyword CSV, captions, release notes, parent copy | Non-English listings, title availability, trademark clearance and console validation |
| Privacy | Minimal local data, backup/transfer exclusions, no ads/tracking, no cloud recogniser fallback | Named publisher/contact details, hosting policy, jurisdictional assessment and legal approval |

## Publisher actions before submission

1. Review the build on actual supported Fire tablets, including a low-RAM device. Measure cold launch and long sessions; test Airplane mode, rotation, process death, deny/revoke microphone permission, missing TTS and unavailable recognition.
2. Choose and document the legal publisher, support email, privacy policy URL, verifier host, server retention and distribution territories. Replace all marked placeholders in the draft HTML pages.
3. Confirm narration distribution rights or replace all essential WAVs with licensed audio. Review educational labels and all voice pronunciations.
4. Validate the parent gate and speech data flows against applicable Amazon, COPPA and UK/EU requirements. Do not enable microphone commands until established compliant on each supported device/configuration.
5. Configure the one-time ENTITLED SKU, public authentication PEM, verifier URL and server-only shared secret. Validate failed, cancelled, pending, already-owned, refunded, wrong-account and restored purchases, including multi-page responses.
6. Sign with a publisher-controlled release keystore kept outside this repository. Run the release build and review R8/Amazon SDK warnings on real hardware. Neither an unsigned APK nor a debug key is a production release.
7. Capture both device variants using the supplied script. Review every composition and set `approved` only for actual valid captures. Check the image specs in the current console again before uploading.
8. Approve English UK/US spelling and all listing claims. Leave other languages disabled until the full UI, vocabulary, letter content, narration, commands, captions and store copy are reviewed in context.
9. Complete Amazon target-audience, privacy-label and content-rating forms, including SDK disclosures. Confirm title availability and current limits in the developer console. No listing or account was created by this work.
10. Run `python tools/check_release.py` after recording genuine sign-off evidence. It intentionally reports blockers today.

## Official sources checked 15 October 2026

- [Appstore listing details](https://developer.amazon.com/docs/app-submission/appstore-details.html): short description 2,000 bytes, long description 4,000 characters, 3–5 feature bullets; tablet icons 114×114 and 512×512; screenshots allow listed landscape or portrait dimensions including 1280×800 and 1280×720. The capture workflow uses 1280×800 tablet and 720×1280 portrait. Check console upload rules again before publishing. A numeric display-title limit was not established from the checked public page.
- [SDK integration](https://developer.amazon.com/docs/appstore-sdk/integrate-appstore-sdk.html): uses Maven SDK 3.0.9 and the publisher's PEM. [SDK downloads](https://www.developer.amazon.com/apps-and-games/sdks) says the Billing Compatibility SDK is no longer supported from 15 September 2026; this app uses Appstore SDK IAP instead.
- [IAP implementation](https://developer.amazon.com/docs/in-app-purchasing/iap-implement-iap.html), [RVS sandbox](https://www.developer.amazon.com/docs/in-app-purchasing/rvs-cloud-sandbox.html), [receipt verification](https://developer.amazon.com/docs/in-app-purchasing/iap-rvs-for-android-apps.html), [testing](https://developer.amazon.com/docs/in-app-purchasing/iap-testing-overview.html).
- [Child-directed policy](https://developer.amazon.com/docs/policy-center/privacy-children.html) and [target audience](https://developer.amazon.com/docs/app-submission/target-app.html).

A public search did not establish the proposed title's availability. Absence of a search result is not availability or trademark clearance.
