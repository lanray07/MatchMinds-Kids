"""Read-only structural audit. This cannot approve language or educational quality."""
from collections import Counter
from pathlib import Path
import hashlib
import json
import re
import unicodedata

ROOT = Path(__file__).resolve().parents[1]
SLOTS = re.compile(r'\{\d+\}')
BIDI = set('\u202a\u202b\u202c\u202d\u202e\u2066\u2067\u2068\u2069')
# Arabic singular labels express one in words. Keep this exact and visible in the
# report rather than allowing arbitrary number changes or claiming native approval.
NUMBER_WORDS = {('ar', 'Discover 1 level'): 'اكتشف مستوى واحدًا', ('ar', '1 star'): 'نجمة واحدة'}


def numbers(text):
    text = SLOTS.sub('', text)
    return Counter(''.join(str(unicodedata.decimal(c)) if c.isdecimal() else c for c in n)
                   for n in re.findall(r'\d+', text))


def audit():
    paths = sorted((ROOT / 'app/src/main/assets/locales').glob('*.json'))
    expected_locales = {'en-GB', 'en-US', 'es', 'fr', 'de', 'it', 'pt-BR', 'pt-PT', 'nl', 'pl', 'ar', 'hi'}
    issues = []
    if {p.stem for p in paths} != expected_locales:
        issues.append({'issue': 'locale file set differs from supported locales'})
    baseline = {'Little matches.\nBig discoveries.'}
    for source_file in sorted((ROOT / 'localization').glob('*.tsv')):
        for line in source_file.read_text(encoding='utf-8').splitlines():
            if not line or line.startswith('#'):
                continue
            source = line.split('|')[0].replace('\\n', '\n')
            baseline.add(source)
            if source.islower() and ' ' not in source:
                baseline.add(source.capitalize())
    report = {'scope': 'Static catalog structure only; English resolves from source literals with US spelling substitutions',
              'native_review_complete': False, 'locales': {}, 'issues': issues, 'numeric_word_triage': []}
    for path in paths:
        duplicates = []

        def pairs_hook(pairs):
            result = {}
            for key, value in pairs:
                if key in result:
                    duplicates.append(key)
                result[key] = value
            return result

        catalog = json.loads(path.read_text(encoding='utf-8'), object_pairs_hook=pairs_hook)
        for key in duplicates:
            issues.append({'locale': path.stem, 'source': key, 'issue': 'duplicate JSON key'})
        if not path.stem.startswith('en') and set(catalog) != baseline:
            issues.append({'locale': path.stem, 'issue': 'missing/extra catalog keys',
                           'missing': sorted(baseline-set(catalog)), 'extra': sorted(set(catalog)-baseline)})
        unchanged = []
        for source, target in catalog.items():
            reasons = []
            if not isinstance(target, str) or not target.strip():
                reasons.append('empty/non-string translation')
            else:
                if Counter(SLOTS.findall(source)) != Counter(SLOTS.findall(target)):
                    reasons.append('placeholder mismatch')
                if numbers(source) != numbers(target):
                    if NUMBER_WORDS.get((path.stem, source)) == target:
                        report['numeric_word_triage'].append({'locale': path.stem, 'source': source,
                            'target': target, 'reason': 'Arabic singular expresses one in words; native contextual approval still pending'})
                    else:
                        reasons.append('numeric tokens changed')
                if '\ufffd' in target or any(unicodedata.category(c) == 'Cc' and c not in '\n\t' for c in target):
                    reasons.append('replacement or unexpected control character')
                if any(c in BIDI for c in target):
                    reasons.append('embedded bidi control requires review')
                for brand in ['MatchMinds', 'Milo', 'Amazon']:
                    if brand in source and brand not in target:
                        reasons.append(f'missing brand {brand}')
                if source == target and re.search('[A-Za-z]{3}', source):
                    unchanged.append(source)
            for reason in reasons:
                issues.append({'locale': path.stem, 'source': source, 'issue': reason})
        report['locales'][path.stem] = {'entries': len(catalog), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
                                       'unchanged_english_for_manual_triage': unchanged}
    report['structural_pass'] = not issues
    destination = ROOT / 'artifacts/runtime-locale-audit.json'
    destination.write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
    print(json.dumps({'locales': len(paths), 'structural_pass': not issues, 'issues': issues}, ensure_ascii=True))
    return not issues


if __name__ == '__main__':
    raise SystemExit(0 if audit() else 1)
