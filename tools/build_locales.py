"""Compile agent-authored translation previews; structural checks do not imply native review."""
import json
import re
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
LANGS = ['en-GB','en-US','es','fr','de','it','pt-BR','pt-PT','nl','pl','ar','hi']

def compile_catalogs():
    catalogs = {locale:{} for locale in LANGS}
    for file in sorted((ROOT/'localization').glob('*.tsv')):
        for number, line in enumerate(file.read_text(encoding='utf-8').splitlines(),1):
            if not line or line.startswith('#'): continue
            cells = [v.replace('\\n','\n') for v in line.split('|')]
            if len(cells) != 10: raise ValueError(f'{file.name}:{number}: expected 10 columns, got {len(cells)}')
            source = cells[0]
            for locale,index in zip(['es','fr','de','it','pt-BR','pt-PT','nl','pl','ar','hi'],[1,2,3,4,5,5,6,7,8,9]):
                text = cells[index]
                if not text.strip() or sorted(re.findall(r'\{\d+\}',source)) != sorted(re.findall(r'\{\d+\}',text)):
                    raise ValueError(f'{file.name}:{number}: placeholders or empty text in {locale}')
                for brand in ['MatchMinds','Milo','Amazon']:
                    if brand in source and brand not in text: raise ValueError(f'{file.name}:{number}: missing {brand}')
                if locale == 'pt-PT':
                    for old,new in [('usuário','utilizador'),('tela','ecrã'),('registro','registo'),('salvo','guardado'),('salva','guardada'),('assinatura','subscrição'),('adesivos','autocolantes'),('Meus','Os meus'),('Minhas','As minhas')]: text=text.replace(old,new)
                catalogs[locale][source]=text
                # The engine capitalises animal/object labels but not pattern labels.
                if source.islower() and ' ' not in source:
                    catalogs[locale][source.capitalize()]=text
    for locale in LANGS:
        catalogs[locale]['Little matches.\nBig discoveries.'] = (catalogs[locale].get('Little matches. Big discoveries.','Little matches. Big discoveries.')).replace('. ','.\n',1)
    out = ROOT/'app/src/main/assets/locales'; out.mkdir(parents=True,exist_ok=True)
    for locale,data in catalogs.items(): (out/f'{locale}.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
    report = {locale:{'entries':len(data),'status':'English baseline' if locale.startswith('en') else 'Integrated translation preview; native review pending','native_reviewed':locale.startswith('en'),'speech':'Installed offline matching-language TTS voice required; text fallback otherwise','letter_activity':'English A–Z case matching, explicitly labelled; no native phonics claim','rtl':locale=='ar'} for locale,data in catalogs.items()}
    (ROOT/'localization/runtime-report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    print(f'Compiled {len(catalogs)} locales; {len(catalogs["es"])} entries per non-English catalog')
    return catalogs
if __name__ == '__main__': compile_catalogs()
