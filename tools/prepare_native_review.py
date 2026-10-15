"""Produce reviewer workbooks without treating automated checks as human sign-off."""
from pathlib import Path
import csv
import hashlib
import json
import re

ROOT = Path(__file__).resolve().parents[1]


def prepare():
    output = ROOT / 'localization/native-review'
    output.mkdir(exist_ok=True)
    report = {'human_review_complete': False, 'locales': {}, 'conflicting_duplicates': []}
    sources = {}
    for path in sorted((ROOT / 'localization').glob('*.tsv')):
        for line_number, line in enumerate(path.read_text(encoding='utf-8').splitlines(), 1):
            if not line or line.startswith('#'):
                continue
            values = [x.replace('\\n', '\n') for x in line.split('|')]
            if values[0] in sources and sources[values[0]][0] != values[1:]:
                report['conflicting_duplicates'].append({'source': values[0], 'files': [sources[values[0]][1], f'{path.name}:{line_number}']})
            sources[values[0]] = (values[1:], f'{path.name}:{line_number}')
    for path in sorted((ROOT / 'app/src/main/assets/locales').glob('*.json')):
        if path.stem.startswith('en'):
            continue
        catalog = json.loads(path.read_text(encoding='utf-8'))
        identical = []
        with (output / (path.stem + '.csv')).open('w', newline='', encoding='utf-8-sig') as handle:
            writer = csv.writer(handle)
            writer.writerow(['locale', 'source_english', 'candidate_translation', 'source_location', 'review_decision', 'suggested_revision', 'educational_or_cultural_notes', 'reviewer_name', 'review_date'])
            for source, target in catalog.items():
                writer.writerow([path.stem, source, target, sources.get(source, ('', 'generated variant'))[1], 'PENDING', '', '', '', ''])
                if source == target and re.search('[a-zA-Z]{3}', source) and source not in ['MatchMinds', 'Milo', 'Amazon']:
                    identical.append(source)
        report['locales'][path.stem] = {'entries': len(catalog), 'catalog_sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'status': 'Awaiting named native reviewer', 'identical_to_english_for_review': identical, 'screens': f'artifacts/localization-renders/{path.stem}-*.png', 'store_copy': f'store/localized/{path.stem}.json'}
    # Marketplace-only languages still need native approval of their public copy.
    marketing = output / 'store-and-purchase-copy.csv'
    rows = []
    for path in sorted((ROOT / 'store/amazon-locales').glob('*.json')):
        data = json.loads(path.read_text(encoding='utf-8'))
        for key, value in data.items():
            if key not in ['locale', 'review_status'] and isinstance(value, str):
                rows.append([path.stem, str(path.relative_to(ROOT)), key, value, 'PENDING', '', '', ''])
    purchase = ROOT / 'store/iap-locales.json'
    for locale, values in json.loads(purchase.read_text(encoding='utf-8'))['copy'].items():
        for key, value in zip(['title', 'description'], values):
            rows.append([locale, str(purchase.relative_to(ROOT)), key, value, 'PENDING', '', '', ''])
    with marketing.open('w', newline='', encoding='utf-8-sig') as handle:
        writer = csv.writer(handle)
        writer.writerow(['locale', 'source_file', 'field', 'candidate_translation', 'review_decision', 'suggested_revision', 'reviewer_name', 'review_date'])
        writer.writerows(rows)
    report['marketing_review'] = {'file': marketing.name, 'entries': len(rows), 'status': 'PENDING; includes Japanese, Swedish and Chinese purchase copy'}
    (output / 'audit.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'review_packs': len(report['locales']), 'conflicting_duplicates': report['conflicting_duplicates'], 'human_review_complete': False}, ensure_ascii=False))


if __name__ == '__main__':
    prepare()
