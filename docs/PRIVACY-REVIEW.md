# Data flows and child privacy review

This document records implementation choices and unresolved review work. It is not a certification of legal compliance.

| Data | Location and flow | Retention and controls |
| --- | --- | --- |
| Nickname/avatar/difficulty | App-private DataStore on device | Parent can remove profiles or erase all play data. No real name, birth date, photo or email requested |
| Stars/levels/preferences/unfinished game | Same local DataStore | Parent can clear progress; no cloud sync. Backups and device transfers excluded in manifest/XML |
| Parent PIN | Salt and PBKDF2 hash in app-private storage | No plaintext PIN. Six digits, minimum three distinct digits, persistent 30-second cooldown after repeated failures. Parent session closes on backgrounding |
| Narration text | Installed offline TTS voice, with visible text fallback | No app server request. Voices requiring a network or missing downloads are excluded. Desktop speech WAVs were removed. English synthesis passed on KFTRWI; native pronunciation review remains |
| Voice audio | Optional explicit Android on-device SpeechRecognizer API, only if release review flag is enabled | No raw audio or transcripts written by the app. Microphone defaults off, parent enabled, permission required, tap-to-speak, visible state and stop control. No cloud recognition API fallback. Actual recogniser internals and vendor terms need review before enabling |
| Amazon user ID and receipt ID | Amazon SDK to app; app to publisher HTTPS verifier; verifier to Amazon RVS | No child nickname or play data attached. Local entitlement and Amazon user ID survive play-data deletion. Uninstall/clear app data removes that cache. Server does not intentionally persist requests or log them; hosting/proxy logs must be reviewed and retention set by publisher |
| Purchase credentials | Amazon-owned checkout | The app does not receive card details. Restore purchases requires the purchasing Amazon account |

The app has no ads, behavioural analytics, social features, public content, accounts or location access. INTERNET exists for Amazon billing and receipt verification. Speech commands are disabled by the default build flag until the outstanding review is complete. Touch controls and spoken guidance ship independently of microphone support.

## Parental gate assessment

The implementation uses private parent knowledge rather than an arithmetic puzzle. First setup adds an adult reading/typing task, which is friction rather than proof of adulthood. A reading child can potentially complete setup, and a shared PIN can defeat the gate. The parent must establish and keep the PIN private. Purchases then additionally use Amazon's own checkout controls. Validate this experience with Amazon and the target audience before publishing. Never treat the gate as verifiable parental consent for collection of children's personal information.

## Applicable-regime review

The app intentionally targets under-13 children. The publisher must assess COPPA and Amazon's child-directed policy, as well as UK GDPR/Children's Code and GDPR where distributed. Default high privacy and local-only play reduce data flows but do not themselves settle obligations. Voice recordings and device-service processing need special consideration. Confirm the publisher's controller role, Amazon/hosting/recogniser roles, legal bases, disclosures, consent requirements where relevant, international flows, retention and data-rights handling. A DPIA and device-specific speech assessment may be appropriate. The safe release path while unresolved is the default: microphone commands unavailable, touch plus narration retained.

Sources checked 15 October 2026: [Amazon child-directed policy](https://developer.amazon.com/docs/policy-center/privacy-children.html), [FTC COPPA guidance](https://www.ftc.gov/business-guidance/privacy-security/childrens-privacy), [FTC voice guidance](https://www.ftc.gov/news-events/news/press-releases/2017/10/ftc-provides-additional-guidance-coppa-voice-recordings), [ICO code standards](https://ico.org.uk/for-organisations/uk-gdpr-guidance-and-resources/childrens-information/childrens-code-guidance-and-resources/age-appropriate-design-a-code-of-practice-for-online-services/code-standards/), [Android SpeechRecognizer API](https://developer.android.com/reference/android/speech/SpeechRecognizer). Current legal interpretation and deployment-specific compliance remain publisher review items.
