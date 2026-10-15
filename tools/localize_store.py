"""Generate editable store-localization previews; never assert native-language approval."""
import json
from pathlib import Path
from build_locales import compile_catalogs, ROOT
catalogs=compile_catalogs()
features=['Animal and shape starter levels included free.','One-time premium unlock for six activities and three difficulty levels.','Local profiles and progress without a child account.','Text instructions and optional installed offline narration.','No ads or subscriptions. Parent PIN protects settings and purchases.']
scenes=json.loads((ROOT/'store/screenshots.json').read_text(encoding='utf-8'))
out=ROOT/'store/localized';out.mkdir(parents=True,exist_ok=True)
for locale,strings in catalogs.items():
    if locale.startswith('en'): continue
    def t(text): return strings[text]
    data={
        'title':'MatchMinds Kids - '+t('Kids Memory Matching Game'),
        'short_description':t('Fun matching adventures for little learners.')+' '+t(features[0]),
        'description':'\n\n'.join(t(text) for text in ['Welcome to MatchMinds!','Fun matching adventures for little learners.',features[0], 'Six activities, twelve levels each, three worlds, advanced difficulty and more stickers.',features[1], 'Play included games without an internet connection.',features[2],features[3],features[4], 'Purchases and restoration need a connection and the Amazon Appstore. Amazon will confirm payment. Keep your parent PIN private.', 'Translations are previews pending native-language review. English A–Z matching remains an English-letter activity. Narration needs an installed offline voice.']),
        **{'feature_'+str(i+1):t(text) for i,text in enumerate(features)},
        'release_notes':t('Translation previews'),
        'review_status':'Agent-authored translation preview. Native review and voice/device QA pending.'
    }
    (out/f'{locale}.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
    captions=[{'id':scene['id'],'headline':t(scene['headline']),'support':t('Match numbers with groups of stars.' if scene['id']=='04-numbers' else scene['support'])} for scene in scenes]
    (out/f'{locale}-screenshots.json').write_text(json.dumps(captions,ensure_ascii=False,indent=2),encoding='utf-8')
print('Generated ten store-copy and screenshot-caption preview sets')
