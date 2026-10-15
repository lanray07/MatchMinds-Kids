# MatchMinds Kids privacy policy

Updated: 15 October 2026. Publisher: Lanray. Contact: [banksmi@mail.com](mailto:banksmi@mail.com).

This policy describes MatchMinds Kids and its current release configuration. The app is designed for children and family play. Adults should manage settings, purchases and support requests.

## Playing without an account

The app does not require a child account, real name, date of birth, email address, photograph or location. Choose a made-up explorer nickname. There are no advertisements, advertising trackers, social features, public profiles or chat. The app does not sell children's information or use it for advertising.

## Information stored on your device

The app stores explorer nicknames and avatars, difficulty and language preferences, sound and motion settings, completed levels, stars, stickers and unfinished games in its private device storage. These records support play and local progress. They are not sent to the publisher by the app. The app excludes its play data from Android cloud backup and device transfer.

The parent PIN is stored as a salted password hash, rather than as readable PIN digits. Failed PIN attempts and a temporary lockout time are stored to protect the parent area. Play records remain until you delete them or clear the app's storage. Removing a profile or clearing progress removes the corresponding local play records.

## Spoken instructions and microphone

Instructions appear as text and can be spoken using an installed voice in the selected language marked as usable offline, with no additional voice-data download required. If no suitable voice is installed, the app keeps the text instructions available. The app does not distribute packaged speech recordings. The app does not send narration text to a publisher speech server. Device text-to-speech services are supplied by the device or voice provider.

Microphone commands are disabled in the current release configuration; the microphone permission is excluded from that build. The app does not record or upload microphone audio in this configuration. Touch controls are available throughout. Any future microphone-enabled release requires a separate review and an updated disclosure before activation.

## Amazon purchases

Premium purchasing and restoration require Amazon Appstore availability and an internet connection. Development builds or an unavailable store product may keep purchases unavailable. Amazon handles checkout and payment details; the app does not receive payment-card details.

Purchase verification sends an Amazon customer identifier and receipt identifier over HTTPS to **https://matchminds-kids-verifier.vercel.app/verify**, operated by Lanray and hosted by **Vercel**. That service checks the receipt with Amazon and returns the premium entitlement. The app does not attach a child's nickname, avatar, PIN or gameplay history to the request. The resulting entitlement and purchasing Amazon identifier are retained in app-private storage for offline access and account-switch checks. Deleting play data does not cancel an Amazon purchase or remove that entitlement cache; clearing app storage removes the cache. Amazon controls its own transaction retention.

The verification handler processes identifiers in memory and does not intentionally save request bodies, receipts or user identifiers to a database or application logs. Vercel processes connection and operational metadata, such as IP address and request timing, to host and protect the service; provider-controlled infrastructure records may be retained under its service settings and policies. Contact the publisher to ask about a verification or support record. No child-play records are held by this verification service. For provider practices, see [Vercel's Privacy Notice](https://vercel.com/legal/privacy-policy) and [Amazon's Privacy Notice](https://www.amazon.com/privacy). Purchase information is used for app access, restoration and receipt-security checks, not advertising.

## Support and privacy requests

An adult can email [banksmi@mail.com](mailto:banksmi@mail.com) for support or a privacy request. Information you choose to send by email is handled through the support email service and is used to respond to the request. Do not send children's real names, recordings, PINs, payment-card details or passwords. Start with the app version, device model and a brief description; we can discuss any further information needed privately.

For local data removal and support requests, see [Data deletion](DATA-DELETION.md). The publisher cannot remotely retrieve or erase play records that exist only on your device. Amazon controls its own account and transaction records.

## Changes

Updates to data handling will be reflected on this page. Check the update date above and the disclosures accompanying the version you install. Contact the publisher at the address above with questions about this policy.
