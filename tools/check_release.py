"""Fail-closed release checklist. Does not upload, sign or publish anything."""
from pathlib import Path
import json, sys
root=Path(__file__).resolve().parents[1]
checks={
    'Amazon public key supplied':(root/'app/src/main/assets/AppstoreAuthenticationKey.pem').exists(),
    'publisher privacy policy approved':(root/'release-approvals/privacy-approved.txt').exists(),
    'narration distribution rights confirmed':(root/'release-approvals/audio-rights.txt').exists(),
    'real Fire tablet QA signed off':(root/'release-approvals/fire-device-qa.txt').exists(),
    'parent gate and voice data flow review signed off':(root/'release-approvals/child-safety.txt').exists(),
    'Amazon sandbox and Live App Testing completed':(root/'release-approvals/billing-lat.txt').exists(),
}
for variant in ['phone','tablet']:
    path=root/f'artifacts/screenshots/{variant}/manifest.json'
    value=json.loads(path.read_text()) if path.exists() else {}
    checks[f'{variant} real screenshots reviewed']=len(value.get('captures',[]))==8 and all(x.get('approved') for x in value.get('captures',[]))
for name,ok in checks.items(): print(('PASS' if ok else 'BLOCKED')+' '+name)
sys.exit(0 if all(checks.values()) else 1)
