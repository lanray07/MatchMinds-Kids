# Native review instructions

These CSV files contain translation candidates, not approved translations. Each locale requires a fluent native reviewer with experience in language used with children. Brazilian and European Portuguese need separate reviews. No reviewer has been hired, contacted or represented as having signed off.

Review each source/candidate pair, then inspect it in the app and in the corresponding screenshots under `artifacts/localization-renders`. Review all six activities and parent, privacy, purchase, recovery and accessibility screens. Translate neither private nicknames nor store-supplied prices. Preserve placeholders and MatchMinds, Milo and Amazon names.

Record ACCEPT or REVISE, the proposed revision, educational/cultural notes, reviewer identity and date. Check natural instructions for ages 3–8, animal/shape/object terminology, counts, grammatical forms, text fitting, Arabic direction and punctuation, Hindi conjuncts and line breaks. The letter activity intentionally teaches English A–Z; do not describe it as native phonics.

Separately review spoken output with the actual matching offline voice installed on the target Fire device. If no matching voice is installed, verify visible text remains usable; do not approve pronunciation based on English speech or a completed synthesis callback.

Review storefront copy in `store/amazon-locales`, purchase copy in `store/iap-locales.json`, and captions in `store/localized`. Japanese and Swedish are storefront-only; Chinese is purchase-copy only. Their copy must not imply corresponding app UI support.

`store-and-purchase-copy.csv` provides 163 individual review rows for the available marketplace and purchase fields, including those three languages. Runtime workbooks contain 289 rows per variant. No checkbox or generated audit substitutes for the reviewer name and contextual decision.

The generator's structural checks and unchanged-English flags are triage aids. They cannot approve educational quality, pronunciation or cultural suitability. Keep all translated runtime variants labeled as previews until contextual review is complete.

On 16 October 2026, the owner confirmed no reviewers are available yet. `python tools/audit_runtime_locales.py` passed the catalog structural checks; see `artifacts/runtime-locale-audit.json`. Its two exact Arabic number-word exceptions and unchanged-English flags remain visible for contextual review. No workbook decisions or reviewer identities were filled by the agent.
