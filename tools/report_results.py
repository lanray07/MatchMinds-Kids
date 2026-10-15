"""Record real test XML and APK hashes; never invent a passing device result."""
from pathlib import Path
import json, hashlib, xml.etree.ElementTree as ET, subprocess, sys, re
root=Path(__file__).resolve().parents[1]
results={'date':'2026-10-15','jvm_suites':[],'python_suites':[],'apks':[],'device_tests':'Not run: no authorised device/emulator; hardware acceleration unavailable and software emulator crashes.','screenshots':'16 host-native production-UI renders. Physical device capture count remains zero.'}
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
log=root/'artifacts/final-build.log'
results['final_gradle_build_passed']=log.exists() and 'BUILD SUCCESSFUL' in log.read_text(encoding='utf-8',errors='replace')
for file in (root/'app/build/outputs/apk').rglob('*.apk'):
    results['apks'].append({'path':str(file.relative_to(root)),'bytes':file.stat().st_size,'sha256':hashlib.sha256(file.read_bytes()).hexdigest(),'release_signed':False if 'release' in file.parts else 'debug key'})
(root/'artifacts/verification.json').write_text(json.dumps(results,indent=2))
print(json.dumps(results,indent=2))
