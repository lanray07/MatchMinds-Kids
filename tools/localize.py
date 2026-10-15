"""Extract / translate / validate development candidates. Never auto-approve a locale.
Provider protocol: POST {sourceLocale,targetLocale,strings} -> {strings:{key:value}}.
URL and bearer key come from MM_TRANSLATION_URL and MM_TRANSLATION_KEY, not source.
"""
import argparse, json, os, re, urllib.request, xml.etree.ElementTree as ET
from pathlib import Path
from collections import Counter
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'localization'
LOCALES=['en-GB','en-US','es','fr','de','it','pt-BR','pt-PT','nl','pl','ar','hi']
def tokens(text):
    return Counter(re.findall(r'\$\{[^}]+\}|\$[A-Za-z_][\w.]*|%\d*\$?[sdf]|\{\d+\}',text))
def extract():
    strings={}
    for node in ET.parse(ROOT/'app/src/main/res/values/strings.xml').getroot():
        if node.tag=='string' and node.attrib.get('translatable')!='false': strings['res.'+node.attrib['name']]=node.text or ''
    # Keep Kotlin interpolation verbatim; full sentences are translated as units.
    for file in (ROOT/'app/src/main/java').rglob('*.kt'):
        if '/billing/' in file.as_posix() or '/data/' in file.as_posix(): continue
        for match in re.finditer(r'"((?:[^"\\]|\\.)*)"',file.read_text(encoding='utf-8')):
            text=match.group(1)
            if (' ' in text or '→' in text) and not text.startswith(('http','com.')):
                import hashlib
                strings['ui.'+hashlib.sha256(text.encode()).hexdigest()[:12]]=text
    for file in (ROOT/'core/src/main').rglob('*.kt'):
        for match in re.finditer(r'"([A-Za-z][A-Za-z &–?.!’,—-]+ [A-Za-z &–?.!’,—-]+)"',file.read_text()):
            text=match.group(1)
            import hashlib
            strings['core.'+hashlib.sha256(text.encode()).hexdigest()[:12]]=text
    for key,value in json.loads((ROOT/'store/en-GB.json').read_text(encoding='utf-8')).items():
        if isinstance(value,str): strings['store.'+key]=value
    for scene in json.loads((ROOT/'store/screenshots.json').read_text(encoding='utf-8')):
        for field in ['headline','support']: strings['screenshot.'+scene['id']+'.'+field]=scene[field]
    for command in ['start game','animals','play again','next level','repeat','help','pause','go back']: strings['command.'+command.replace(' ','_')]=command
    OUT.mkdir(exist_ok=True)
    (OUT/'source.json').write_text(json.dumps(strings,ensure_ascii=False,indent=2),encoding='utf-8')
    return strings
def validate(source,translated):
    errors=[]
    for key,value in source.items():
        if key not in translated: errors.append(f'missing: {key}'); continue
        candidate=translated[key]
        if not isinstance(candidate,str) or not candidate.strip(): errors.append(f'empty: {key}'); continue
        if tokens(value)!=tokens(candidate): errors.append(f'placeholders: {key}')
        for brand in ['MatchMinds Kids','MatchMinds','Milo']:
            if brand in value and brand not in candidate: errors.append(f'product-name: {key}')
        # Alphabet/phonics vocabulary is not sent to this provider at all.
    return errors
def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--translate',choices=LOCALES); args=parser.parse_args()
    source=extract()
    if args.translate:
        url=os.environ.get('MM_TRANSLATION_URL','')
        if not url.startswith('https://'): raise SystemExit('Configure an HTTPS MM_TRANSLATION_URL; no translation was sent.')
        request=urllib.request.Request(url,json.dumps({'sourceLocale':'en-GB','targetLocale':args.translate,'strings':source,'protectedTerms':['MatchMinds Kids','MatchMinds','Milo']}).encode(),{'Content-Type':'application/json','Authorization':'Bearer '+os.environ.get('MM_TRANSLATION_KEY','')})
        with urllib.request.urlopen(request,timeout=60) as response: result=json.load(response)['strings']
        errors=validate(source,result)
        if errors: raise SystemExit('\n'.join(errors))
        (OUT/f'{args.translate}.candidate.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
    report={}
    for locale in LOCALES:
        candidate=OUT/f'{locale}.candidate.json'
        errors=validate(source,json.loads(candidate.read_text(encoding='utf-8'))) if candidate.exists() else [f'{len(source)} untranslated entries']
        report[locale]={'approved':locale in ['en-GB','en-US'],'translation_errors':[] if locale in ['en-GB','en-US'] else errors,'human_review_required':['interface in context','educational vocabulary','native pronunciation','command recognition','alphabet content','RTL layout' if locale=='ar' else 'layout'],'plural_validation':'No Android plurals in source. Quantity cards use explicit numerals. Provider candidates must retain every placeholder.','runtime_integration':'Only English selectable. Candidate catalogues require resource integration and in-context QA before approval.'}
    (OUT/'report.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(f'Extracted {len(source)} strings; report: {OUT / "report.json"}')
if __name__=='__main__': main()
