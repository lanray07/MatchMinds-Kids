# Remaining human and store checks

The submitted release remains distinct from debug QA fixtures. Native review packs are in `localization/native-review`. Their status is PENDING until an actual reviewer records contextual approval. Do not infer native approval from host screenshots, automated checks or machine-generated translations.

## Amazon production-environment testing

A dedicated LAT copies release 1.0.0 and its signed APK. Amazon confirms **Test submitted** on 15 October 2026, with one tester in MatchMinds Kids Release Test. The publisher confirmed adulthood, consent and sharing rights, export certification and submission authorization. Amazon sends the invitation when the test becomes live. Keep the tester address and account screenshots out of public Git history.

Amazon documents that purchases in LAT are free. Confirm the installed app is the LAT version before initiating checkout. A live-store checkout that may charge money requires an explicit spending limit or the user's participation. Never log customer or receipt identifiers.

| Scenario | Required evidence | Current evidence |
| --- | --- | --- |
| First purchase | Store checkout, production RVS verification, premium unlocked | App Tester sandbox purchase/restore passed; LAT pending |
| Restore | Clear only local test entitlement or reinstall test app, restore using original account | App Tester sandbox passed; LAT pending |
| Different account | Owner A unlocks; an authorized account B cannot inherit A's premium; switching back restores A | SDK callback simulation passed on physical Fire; real account switching requires two adult accounts |
| Cancel checkout | Exit before confirmation; no new premium granted | Failed callback simulation passed on physical Fire; real LAT checkout cancellation pending |
| Revoke/refund | Amazon test-item reset or actual refund produces cancellation and revokes access on synchronization | RVS cancelled/HTTP 410 mocked tests pass; store-driven case pending |
| Offline | Previously verified entitlement persists; unverified receipt cannot grant access | Offline Fire gameplay passed; store-specific offline transitions pending |

Use Amazon's LAT tester controls to reset only this app's disposable test entitlement. A test-item reset is not evidence that a real refund was processed. Sign-in or switching Amazon accounts requires the account owner; never collect passwords in the repository.

## Accessibility and hardware

Automated semantic-name and touch-target checks and 150% font host renders supplement human testing. A real screen-reader user should check focus order, card state announcements, polite live messages, all parent PIN fields, purchase prices and error states. Do not activate an accessibility service with broad device access without the required action-time permission.

Review the Amazon Quality Central device results individually. A queued test is not a pass, and virtual layouts are not new physical Fire models. Long-session debug gameplay uses explicit QA premium access to exercise all activities; it is not purchase evidence.

Quality Central's Functional/Performance Fire Tablets run is In progress as of 15 October. The host review generated 72 additional scenes at 150% text, across 12 locales in portrait and landscape. It found minor header clipping (including Portuguese branding at 600dp) and the locked-content helper label's source-palette contrast is 3.82:1. These are recorded findings, not full accessibility approval; the submitted APK has not been replaced. Raw host text-layout warnings over-report clipping and require visual review.

## Publication

When Amazon reports Live, obtain its actual public listing link. Verify title, category, free-app pricing, all available localized copy, asset order, privacy/deletion/support links and premium product availability. Record marketplace and date. Screenshots must accurately represent the app; translations remain previews until contextual review.

References: [LAT setup](https://developer.amazon.com/docs/app-testing/live-app-testing-getting-started.html), [LAT in-app purchasing](https://developer.amazon.com/docs/in-app-purchasing/iap-lat.html), [Quality Central](https://developer.amazon.com/docs/app-testing/get-started-appstore-quality-central.html).
