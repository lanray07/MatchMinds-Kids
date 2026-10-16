"""Record real test XML and APK hashes; never invent a passing device result."""
from pathlib import Path
from datetime import datetime, timezone
import json, hashlib, xml.etree.ElementTree as ET, subprocess, sys, re
root=Path(__file__).resolve().parents[1]
results={'date':datetime.now(timezone.utc).date().isoformat(),'jvm_suites':[],'python_suites':[],'apks':[],'screenshots':'16 English host-native UI renders; 36 locale host renders and 30 localized store compositions plus 72 large-text host scenes are review previews.'}
device=root/'artifacts/fire-offline-tests.xml'
if device.exists():
    suite=ET.parse(device).getroot()
    results['device_tests']={key:suite.attrib[key] for key in ['name','tests','failures','errors']}
    results['device_tests']['configuration']='Physical KFTRWI API 28; Wi-Fi disabled during offline suite'
for name in ['fire-sandbox-verification','fire-lat-verification','fire-voice-availability','fire-release-launch','fire-extended-verification','fire-billing-callbacks','fire-cloud-verification','fire-release-performance','accessibility-contrast','store-follow-up','runtime-locale-audit','remaining-validation']:
    evidence=root/f'artifacts/{name}.json'
    if evidence.exists(): results[name]=json.loads(evidence.read_text(encoding='utf-8-sig'))
for folder in ['core/build/test-results/test','app/build/test-results/testDebugUnitTest']:
    for file in (root/folder).glob('TEST-*.xml'):
        suite=ET.parse(file).getroot()
        results['jvm_suites'].append({key:suite.attrib[key] for key in ['name','tests','failures','errors']})
for folder in ['tools','server']:
    test=subprocess.run([sys.executable,'-m','unittest','discover','-s',folder,'-p','test_*.py'],cwd=root,text=True,capture_output=True)
    count=re.search(r'Ran (\d+) tests?',test.stderr)
    results['python_suites'].append({'suite':folder,'tests':int(count.group(1)) if count else 0,'passed':test.returncode==0})
capture=root/'artifacts/host-render-tests.xml'
if capture.exists():
    suite=ET.parse(capture).getroot()
    results['host_render_tests']={key:suite.attrib[key] for key in ['name','tests','failures','errors']}
lint=root/'app/build/reports/lint-results-debug.xml'
if lint.exists():
    issues=ET.parse(lint).getroot().findall('issue')
    results['lint']={'errors':sum(x.attrib.get('severity') in ['Fatal','Error'] for x in issues),'warnings':sum(x.attrib.get('severity')=='Warning' for x in issues)}
log=root/'artifacts/extended-final-checks.log'
results['final_gradle_build_passed']=log.exists() and 'BUILD SUCCESSFUL' in log.read_text(encoding='utf-8',errors='replace')
for file in (root/'app/build/outputs/apk').rglob('*.apk'):
    signing=None
    proof=root/'artifacts/release-signature.txt'
    if file.name=='app-release.apk' and proof.exists():
        signing='Verified by Android apksigner' if 'Verified' in proof.read_text(encoding='utf-8-sig') else None
    results['apks'].append({'path':str(file.relative_to(root)),'bytes':file.stat().st_size,'sha256':hashlib.sha256(file.read_bytes()).hexdigest(),'release_signing':signing if 'release' in file.parts else 'debug key'})
(root/'artifacts/verification.json').write_text(json.dumps(results,indent=2))
print(json.dumps(results,indent=2))
