"""Build copy for the fifteen listing locale choices observed in Amazon's console.

Listing translation does not certify full in-app language support.
"""
from pathlib import Path
import json

root = Path(__file__).resolve().parents[1]
output = root / 'store/amazon-locales'
output.mkdir(exist_ok=True)
mapping = {'de-DE':'de','en-AU':'en-GB','en-GB':'en-GB','en-IN':'en-GB','en-US':'en-US',
           'es-ES':'es','es-MX':'es','fr-FR':'fr','it-IT':'it','nl-NL':'nl',
           'pl-PL':'pl','pt-BR':'pt-BR','pt-PT':'pt-PT'}
for target, source in mapping.items():
    path = root / ('store/'+source+'.json' if source.startswith('en-') else 'store/localized/'+source+'.json')
    content = json.loads(path.read_text(encoding='utf-8-sig'))
    content['locale'] = target
    content['review_status'] = 'Listing draft; native review pending where translated. In-app languages are separately documented.'
    (output / f'{target}.json').write_text(json.dumps(content,ensure_ascii=False,indent=2),encoding='utf-8')

additional = {
 'ja-JP': {
  'title':'MatchMinds Kids - 子どもの絵合わせ・記憶ゲーム',
  'short_description':'キツネのMiloと楽しく絵合わせ。動物や形の入門レベルを無料で遊べます。追加の遊びは1回限りのプレミアム購入で。',
  'description':'Miloと一緒に、カードをめくって同じ絵を探しましょう。主に3〜8歳の子ども向けの、時間制限や間違いへのペナルティがない絵合わせゲームです。\n\n動物と形の入門レベルは無料。1回限りのプレミアム購入で、動物、形と色、数字と数量、英語の大文字と小文字、身近な物、繰り返し模様の6つの遊びを楽しめます。難易度は3段階、テーマの世界は3つ。\n\n広告、定期購入、子ども用オンラインアカウントはありません。プロフィールと進行状況は端末内に保存されます。保護者用PINで設定と購入画面を保護します。\n\n含まれるゲームはオフラインで遊べます。購入と復元にはインターネット接続とAmazon Appstoreが必要です。読み上げは、選択言語の対応するオフライン音声が端末にインストールされている場合に利用できます。音声がない場合も画面の文字で案内します。\n\nこの日本語訳はストア紹介文です。アプリ内の日本語には対応していません。アプリ内では英語と10言語の翻訳プレビューを選べます。文字の遊びは英語のA〜Zです。翻訳のネイティブレビューは未完了です。科学的に実証された認知能力向上をうたうものではありません。',
  'features':['動物と形の入門レベルを無料でプレイ','1回限りのプレミアム購入で6つの遊びを追加','広告や定期購入、子ども用アカウントなし','プロフィールと進行状況を端末内に保存','保護者用PINで設定と購入画面を保護']
 },
 'sv-SE': {
  'title':'MatchMinds Kids - Minnesspel för barn',
  'short_description':'Para ihop djur och former med räven Milo. Startnivåer ingår gratis. Fler aktiviteter finns med ett enda premiumköp.',
  'description':'Vänd kort och hitta par med räven Milo! MatchMinds Kids erbjuder lugna matchningsspel främst för barn i åldern 3–8 år, utan tidspress eller straff för misstag.\n\nStartnivåer med djur och former ingår gratis. Ett enda premiumköp låser upp sex aktiviteter: djurpar, former och färger, siffror och antal, stora och små engelska bokstäver, vardagsföremål och upprepade mönster. Välj mellan tre svårighetsgrader och tre temavärldar.\n\nIngen reklam, inga abonnemang och inga barnkonton online. Profiler och framsteg sparas på enheten. En privat föräldrakod skyddar inställningar och köp.\n\nDe inkluderade spelen fungerar offline. Köp och återställning kräver internet och Amazon Appstore. Uppläsning kräver en lämplig installerad offlineröst för det valda språket. Annars visas instruktionerna som text.\n\nDenna svenska översättning gäller butikstexten. Appens gränssnitt finns inte på svenska. Engelska och översättningsförhandsvisningar i tio språkvarianter kan väljas i appen. Bokstavsaktiviteten använder engelska A–Z. Översättningarna väntar på granskning av modersmålstalare. Inga vetenskapligt bevisade förbättringar av kognitiv utveckling utlovas.',
  'features':['Startnivåer med djur och former ingår gratis','Ett enda premiumköp för sex aktiviteter','Ingen reklam och inga abonnemang','Lokala profiler och sparade framsteg utan barnkonto','Föräldrakod skyddar inställningar och köp']
 }
}
for locale, content in additional.items():
    for i, feature in enumerate(content.pop('features'),1): content[f'feature_{i}']=feature
    content.update(locale=locale,review_status='Agent-authored store translation preview; native review pending. No Japanese/Swedish in-app support claim.')
    (output/f'{locale}.json').write_text(json.dumps(content,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'Wrote {len(mapping)+len(additional)} Amazon listing drafts')
