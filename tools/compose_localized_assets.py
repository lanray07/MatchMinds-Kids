"""Editable caption layouts around actual locale test renders; no reconstructed app UI."""
import base64,html,json,textwrap,hashlib
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
out=ROOT/'artifacts/localized-store-assets';out.mkdir(parents=True,exist_ok=True)
manifest={'source':'Actual Robolectric Native Graphics renders of the production Activity. Translation previews; not native-language approval or physical-device captures.','locales':{}}
for file in sorted((ROOT/'store/localized').glob('*-screenshots.json')):
    locale=file.name.removesuffix('-screenshots.json')
    captions=json.loads(file.read_text(encoding='utf-8'))
    records=[]
    for index,suffix in [(0,'welcome'),(1,'game'),(2,'shapes')]:
        raw=ROOT/f'artifacts/localization-renders/{locale}-{suffix}.png'
        if not raw.exists(): raise SystemExit(f'Missing actual capture: {raw}')
        scene=captions[index]
        rtl=locale=='ar'
        x=1504 if rtl else 96
        attr='text-anchor="end" direction="rtl" unicode-bidi="plaintext"' if rtl else ''
        headings=textwrap.wrap(scene['headline'],36,break_long_words=False)
        supports=textwrap.wrap(scene['support'],66,break_long_words=False)
        texts=''.join(f'<text x="{x}" y="{150+i*88}" {attr} font-size="70" font-weight="700">{html.escape(line)}</text>' for i,line in enumerate(headings))
        texts+=''.join(f'<text x="{x}" y="{165+len(headings)*88+i*44}" {attr} font-size="34" fill="#237D70">{html.escape(line)}</text>' for i,line in enumerate(supports))
        embedded=base64.b64encode(raw.read_bytes()).decode()
        svg=f'<svg xmlns="http://www.w3.org/2000/svg" width="1600" height="2560" viewBox="0 0 1600 2560"><rect width="1600" height="2560" fill="#FFF9EE"/><rect x="64" y="36" width="1472" height="10" fill="#237D70"/><g font-family="Segoe UI, Nirmala UI, sans-serif" fill="#233D42"><text x="96" y="90" font-size="24" font-weight="700">MATCHMINDS KIDS</text>{texts}<image href="data:image/png;base64,{embedded}" x="175" y="430" width="1250" height="2000"/><text x="96" y="2500" font-size="24" fill="#237D70">{html.escape(locale)} · {html.escape(scene["id"])}</text></g></svg>'
        folder=out/locale;folder.mkdir(exist_ok=True)
        target=folder/(scene['id']+'.svg');target.write_text(svg,encoding='utf-8')
        records.append({'master':str(target.relative_to(ROOT)),'capture':str(raw.relative_to(ROOT)),'capture_sha256':hashlib.sha256(raw.read_bytes()).hexdigest(),'width':1600,'height':2560,'approved':False})
    manifest['locales'][locale]=records
(out/'manifest.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
print('Composed 30 localized masters from actual app renders')
