# Remaining human and store checks

The submitted release remains distinct from debug QA fixtures. Native review packs are in `localization/native-review`. Their status is PENDING until an actual reviewer records contextual approval. Do not infer native approval from host screenshots, automated checks or machine-generated translations.

## Amazon production-environment testing

A dedicated LAT copies release 1.0.0 and its signed APK. Amazon confirms **Test in-progress** on 16 October 2026, with one tester in MatchMinds Kids Release Test. The publisher confirmed adulthood, consent and sharing rights, export certification and submission authorization. Amazon marks the authorized tester invitation Delivered. Keep the tester address and account screenshots out of public Git history.

Amazon documents that purchases in LAT are free. Confirm the installed app is the LAT version before initiating checkout. A live-store checkout that may charge money requires an explicit spending limit or the user's participation. Never log customer or receipt identifiers.

| Scenario | Required evidence | Current evidence |
| --- | --- | --- |
| First purchase | Store checkout, production RVS verification, premium unlocked | App Tester sandbox and Amazon-installed LAT purchase passed, 16 October |
| Restore | Clear only local test entitlement or reinstall test app, restore using original account | LAT purchase-updates and receipt re-verification passed, 16 October; recovery after clearing local entitlement remains pending |
| Different account | Owner A unlocks; an authorized account B cannot inherit A's premium; switching back restores A | SDK callback simulation passed on physical Fire; real account switching requires two adult accounts |
| Cancel checkout | Exit before confirmation; no new premium granted | Failed callback simulation passed on physical Fire; real LAT checkout cancellation pending |
| Revoke/refund | Amazon test-item reset or actual refund produces cancellation and revokes access on synchronization | RVS cancelled/HTTP 410 mocked tests pass; store-driven case pending |
| Offline | Previously verified entitlement persists; unverified receipt cannot grant access | Offline Fire gameplay passed; store-specific offline transitions pending |

Use Amazon's LAT tester controls to reset only this app's disposable test entitlement. A test-item reset is not evidence that a real refund was processed. Sign-in or switching Amazon accounts requires the account owner; never collect passwords in the repository.

## Accessibility and hardware

Automated semantic-name and touch-target checks and 150% font host renders supplement human testing. A real screen-reader user should check focus order, card state announcements, polite live messages, all parent PIN fields, purchase prices and error states. Do not activate an accessibility service with broad device access without the required action-time permission.

Review the Amazon Quality Central device results individually. A queued test is not a pass, and virtual layouts are not new physical Fire models. Long-session debug gameplay uses explicit QA premium access to exercise all activities; it is not purchase evidence.

Quality Central completed on 16 October: six compatible FOS 7/8 models, two FOS 5 installation failures below the release minimum SDK. See artifacts/fire-cloud-verification.json. The host review generated 72 additional scenes at 150% text, across 12 locales in portrait and landscape. It found minor header clipping (including Portuguese branding at 600dp) and the locked-content helper label's source-palette contrast is 3.82:1. These are recorded findings, not full accessibility approval; the submitted APK has not been replaced. Raw host text-layout warnings over-report clipping and require visual review.

## Publication

When Amazon reports Live, obtain its actual public listing link. Verify title, category, free-app pricing, all available localized copy, asset order, privacy/deletion/support links and premium product availability. Record marketplace and date. Screenshots must accurately represent the app; translations remain previews until contextual review.

References: [LAT setup](https://developer.amazon.com/docs/app-testing/live-app-testing-getting-started.html), [LAT in-app purchasing](https://developer.amazon.com/docs/in-app-purchasing/iap-lat.html), [Quality Central](https://developer.amazon.com/docs/app-testing/get-started-appstore-quality-central.html).

On 16 October, sustained physical Fire debug gameplay passed for 15 minutes (151 games, all 18 activity/difficulty combinations). The signed installed release also passed five process-cold launches with its APK hash checked. These close the baseline sustained-session and release-launch measurements; forced low-memory pressure, screen-reader usability, native review, clean-state entitlement recovery and remaining store-driven LAT cases remain pending.

## LAT purchase and restore — 16 October 2026

A clean installation from Amazon TEST 1 is confirmed by installer com.amazon.venezia. The earlier sideloaded copy could not load price or restore; a clean Amazon installation resolved that connection. The owner expressly authorized the initial data erasure and privately recreated the parent PIN.

Before purchase, restore correctly reported no owned premium. Amazon then supplied the GBP 3.99 product price. The owner completed the final VERIFY AND PAY confirmation directly; no no-charge notice was visible. Actual billing is unverified and must not be reported as zero without evidence.

The release displayed Premium is ready / All adventures are ready after purchase verification, then Restore finished. Verified purchases have been applied after a new restore request. Ocean discoveries / Number garden opened successfully. See artifacts/fire-lat-verification.json. This verifies a fresh restore request with the entitlement still cached, not recovery after a second data erasure. Account switching and refunds remain separate checks. Never publish receipt or customer IDs.

Billing follow-up: a charge was reported after the intended LAT purchase. Functional purchase/restore results stand, but the no-charge LAT criterion is **FAILED**, not fully validated. Specific billing confirmation remains private. Amazon documents no-charge LAT purchases, so app/tester/account/IAP mode requires investigation before another checkout. No refund has been requested or submitted. See https://developer.amazon.com/docs/in-app-purchasing/iap-testing-overview.html#live-app-testing-lat-mode .
