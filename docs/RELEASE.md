# Release status - 15 October 2026

The publisher-signed release APK is built with the production receipt verifier and its signature verifies. Amazon shows the entitlement Approved after submission to Test and Live, with all twelve available purchase-copy locales and icons. The public authentication key and Vercel verifier are configured. The shared secret and signing key remain outside Git.

Six offline Fire tests and a physical App Tester purchase/restore test pass. English offline speech synthesis passes. The final signed production APK installed and launched successfully on the physical Fire tablet. See VERIFICATION.md for evidence and limits.

Amazon confirms SUBMITTED after the publisher explicitly certified export compliance and authorized submission. Its displayed Appstore estimate is October 20, 2026, 10:30 AM PDT; this is not approval. All fifteen listing descriptions are Complete, with eight English screenshots, icons and banner plus localized additions for six marketplace locales. The privacy questionnaire is saved for review; the child-directed audience and no-advertising commitment are recorded. Amazon accepted the signed binary (version code 1) and twelve compatible Fire tablets are selected. store/amazon-submission.json records the latest verified console state.

## Remaining verification and review

- Amazon app review/approval remains pending. The premium item is Approved.
- Final publisher-signed APK installation/launch passed on KFTRWI, API 28. Earlier debug-build gameplay and sandbox purchase/restore also passed; see artifacts/fire-release-launch.json.
- Native educational and pronunciation review remains outstanding for ten non-English runtime variants and translated marketing copy. They are previews. Japanese and Swedish are listing-only translations; Chinese is purchase-copy only.
- Broader accessibility/performance, live commerce/refunds/account switching and child-usability checks remain outstanding.

Desktop speech WAVs were removed. Default builds exclude microphone commands and permission. Parent PIN protection is not legal verification of parental consent. No scientifically proven developmental improvement is claimed.

Optional animal recordings, mixed-category boards, timed challenges and world-specific content pools are not included. tools/check_release.py requires genuine human sign-off evidence; do not fabricate approval files. Store submission does not mean those independent checks have passed.
