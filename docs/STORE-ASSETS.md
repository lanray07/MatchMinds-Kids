# Amazon store screenshots and form preparation

The actual Amazon new-app form was prepared with title `MatchMinds Kids Memory Games and Learning`, SKU `matchminds-kids`, and category **Kids → Education**. Amazon rejected the original punctuated title. The plain-text title passes that validation, but saving is blocked by the required public support email. The account has no default support contact. No app draft, image upload or submission has succeeded. See `store/amazon-submission.json` for the exact status.

## Native render pipeline

The installed emulator crashes before ADB becomes available (Windows exit code `-1073741819`, access violation). Host rendering now provides actual screenshots of the production `MainActivity` and Compose UI, using Robolectric Native Graphics at Android API 34. The capture test uses the real game engine to make matches and earn the reward shown; it does not paint a replacement gameplay board or fabricate ratings. Premium content uses the existing debug QA entitlement, not a simulated purchase. The source rendering fixtures are test-only and never enter the APK.

Amazon's SDK receiver lacks JVM stack-map frames and cannot be loaded by the host JVM. A no-op receiver exists in `src/test` solely to allow the Activity to render. Billing callbacks are not tested by this fixture; actual Amazon purchase testing remains a separate device task.

The renderer exposed a phone-header wrapping defect. The production UI now uses a compact header, shorter board header and stacked welcome footer on phones. Captures must be regenerated after visual UI changes.

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests com.matchminds.kids.StoreCaptureTest -PstoreCapture=true --no-daemon --max-workers=2
python tools/compose_store_assets.py
# Use the bundled workspace Node packages or install Sharp:
node tools/render_store_assets.cjs
```

Ordinary JVM tests exclude these opt-in screenshot tests. The existing `tools/capture_screenshots.py` remains available for real connected-device captures. Host-rendered screenshots do not establish Fire hardware compatibility, audio quality, microphone behaviour, performance or full accessibility compliance.

## Assets

- Eight tablet PNGs at **2560×1600** and eight portrait PNGs at **1600×2560** in `artifacts/store-assets`.
- Each screenshot has an editable SVG master with its actual app capture embedded, a readable feature headline and supporting text.
- Square 114px and 512px Amazon store icons and an editable icon master; the outer rounded mask was removed for the store variants.
- A 1024×500 promotional image and editable SVG master, featuring the original Milo character and app name.
- A manifest records each raw capture's dimensions and SHA-256. The images are host renders, not claimed physical-device captures.

The eight topics are kids memory matching, animal matching, shapes and colours, number quantities, English letters, spoken guidance, offline included gameplay, and rewards. Number captions describe the implemented star quantities. Spoken-guidance imagery does not advertise unavailable microphone commands. No ratings, awards, discounts or scientifically proven outcomes are invented.

Use the eight tablet images for the Fire tablet listing. Amazon's shared tablet-image section allows 3–10 screenshots, so do not upload all sixteen variants into one listing. Portrait variants are a separate reusable asset set.

Current requirements were checked against [Amazon's listing documentation](https://developer.amazon.com/docs/app-submission/appstore-details.html): the selected landscape/portrait sizes, icon dimensions and 1024×500 promotional format are supported. Recheck the actual upload validators when the draft is created. Store upload and final publication remain pending the required contact and release configuration.
