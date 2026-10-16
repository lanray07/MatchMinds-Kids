# Release status - 15 October 2026

The publisher-signed release APK is built with the production receipt verifier and its signature verifies. Amazon shows the entitlement Approved after submission to Test and Live, with all twelve available purchase-copy locales and icons. The public authentication key and Vercel verifier are configured. The shared secret and signing key remain outside Git.

Six offline Fire tests and a physical App Tester purchase/restore test pass. English offline speech synthesis passes. The final signed production APK installed and launched successfully on the physical Fire tablet. See VERIFICATION.md for evidence and limits.

Amazon confirms UNDER REVIEW after the publisher explicitly certified export compliance and authorized submission. Its displayed Appstore estimate is October 20, 2026, 10:30 AM PDT; this is not approval. All fifteen listing descriptions are Complete, with eight English screenshots, icons and banner plus localized additions for six marketplace locales. The privacy questionnaire is saved for review; the child-directed audience and no-advertising commitment are recorded. Amazon accepted the signed binary (version code 1) and twelve compatible Fire tablets are selected. store/amazon-submission.json records the latest verified console state.

## Remaining verification and review

- Amazon app review/approval remains pending. The premium item is Approved.
- Final publisher-signed APK installation/launch passed on KFTRWI, API 28. Earlier debug-build gameplay and sandbox purchase/restore also passed; see artifacts/fire-release-launch.json.
- Native educational and pronunciation review remains outstanding for ten non-English runtime variants and translated marketing copy. They are previews. Japanese and Swedish are listing-only translations; Chinese is purchase-copy only.
- Broader accessibility/performance, actual billing, clean-state restore, refunds/account switching and child-usability checks remain outstanding.

Desktop speech WAVs were removed. Default builds exclude microphone commands and permission. Parent PIN protection is not legal verification of parental consent. No scientifically proven developmental improvement is claimed.

Optional animal recordings, mixed-category boards, timed challenges and world-specific content pools are not included. tools/check_release.py requires genuine human sign-off evidence; do not fabricate approval files. Store submission does not mean those independent checks have passed.

## Extended validation follow-up

The dedicated LAT is Test submitted with one authorized adult tester. Amazon will invite that tester when live. Quality Central Fire Tablets Functional/Performance tests are In progress. An hourly thread follow-up checks meaningful changes and will inspect the public listing after approval. Native review workbooks and large-text accessibility findings are available; see LIVE-VALIDATION.md and VERIFICATION.md. No store-driven refund, real account-switch, additional physical Fire model or native reviewer approval is claimed.

On 16 October, LAT is in progress with invitation Delivered. Quality Central completed: six FOS 7/8 models passed install/launch/exit without performance alerts; two FOS 5 models cannot install the API 26 minimum release. See VERIFICATION.md and artifacts/fire-cloud-verification.json. Production remains Under Review with no requested action.

The 16 October physical Fire continuation passed 15 minutes of debug UI gameplay (151 games, all 18 mode/difficulty combinations). Five launches of the hash-matched installed signed release passed, median Activity TotalTime 237 ms. Human accessibility/native-language review, remaining LAT commerce/account-switching cases and forced low-memory checks remain separate. See VERIFICATION.md.

Amazon-installed LAT purchase, fresh restore verification and premium game access passed on 16 October. The owner completed checkout directly; actual billing is unconfirmed because the dialog displayed GBP 3.99 without a no-charge notice. Clean-state recovery, account switching and refunds remain separate. See artifacts/fire-lat-verification.json.

Billing follow-up: a charge was reported after the intended LAT purchase. Functional purchase/restore results stand, but the no-charge LAT criterion is **FAILED**, not fully validated. Specific billing confirmation remains private. Amazon documents no-charge LAT purchases, so app/tester/account/IAP mode requires investigation before another checkout. No refund has been requested or submitted. See https://developer.amazon.com/docs/in-app-purchasing/iap-testing-overview.html#live-app-testing-lat-mode .
