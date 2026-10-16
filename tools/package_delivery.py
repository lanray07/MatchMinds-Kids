from pathlib import Path
import zipfile, shutil, json, hashlib
root=Path(__file__).resolve().parents[1]
out=root/'artifacts/delivery';out.mkdir(parents=True,exist_ok=True)
source=out/'MatchMindsKids-source.zip'
with zipfile.ZipFile(source,'w',zipfile.ZIP_DEFLATED) as archive:
    for name in ['app','core','gradle','tools','server','store','localization','docs']:
        for file in (root/name).rglob('*'):
            if file.is_file() and not file.name.startswith('.env') and file.suffix not in ['.jks','.keystore','.key','.p12','.dpapi'] and not any(part in ['build','__pycache__','.vercel','node_modules','.venv'] for part in file.relative_to(root).parts): archive.write(file,file.relative_to(root))
    for file in (root/'artifacts/brand').glob('*'):
        if file.is_file(): archive.write(file,file.relative_to(root))
    for file in (root/'artifacts/screenshots').rglob('manifest.json'): archive.write(file,file.relative_to(root))
    for folder in ['artifacts/store-assets','artifacts/screenshots/host-tablet','artifacts/screenshots/host-phone','artifacts/localization-renders','artifacts/localized-store-assets','artifacts/accessibility-renders']:
        for file in (root/folder).rglob('*'):
            if file.is_file(): archive.write(file,file.relative_to(root))
    if (root/'artifacts/host-render-tests.xml').exists(): archive.write(root/'artifacts/host-render-tests.xml','artifacts/host-render-tests.xml')
    for name in ['README.md','PRIVACY.md','DATA-DELETION.md','.gitignore','settings.gradle.kts','build.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        archive.write(root/name,name)
    if (root/'artifacts/verification.json').exists(): archive.write(root/'artifacts/verification.json','artifacts/verification.json')
    for evidence in ['runtime-locale-audit.json','remaining-validation.json']:
        item=root/'artifacts'/evidence
        if item.exists(): archive.write(item,'artifacts/'+evidence)
    for evidence in ['fire-offline-tests.xml','fire-release-launch.json','fire-sandbox-verification.json','fire-lat-verification.json','fire-voice-availability.json','release-signature.txt','final-localized-checks.log','signed-release-build.log','extended-final-checks.log','fire-extended-verification.json','fire-billing-callbacks.json','fire-cloud-verification.json','fire-release-performance.json','accessibility-contrast.json','store-follow-up.json']:
        item=root/'artifacts'/evidence
        if item.exists(): archive.write(item,'artifacts/'+evidence)
debug=root/'app/build/outputs/apk/debug/app-debug.apk'
if debug.exists(): shutil.copy2(debug,out/'MatchMindsKids-debug.apk')
release=root/'app/build/outputs/apk/release/app-release-unsigned.apk'
if release.exists(): shutil.copy2(release,out/'MatchMindsKids-release-UNSIGNED.apk')
signed=root/'app/build/outputs/apk/release/app-release.apk'
if signed.exists():
    shutil.copy2(signed,out/'MatchMindsKids-release.apk')
    (out/'MatchMindsKids-release-UNSIGNED.apk').unlink(missing_ok=True)
manifest=[{'file':f.name,'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in out.iterdir() if f.suffix in ['.zip','.apk']]
(out/'manifest.json').write_text(json.dumps(manifest,indent=2))
print(json.dumps(manifest,indent=2))
