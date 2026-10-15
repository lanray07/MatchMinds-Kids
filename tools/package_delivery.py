from pathlib import Path
import zipfile, shutil, json, hashlib
root=Path(__file__).resolve().parents[1]
out=root/'artifacts/delivery';out.mkdir(parents=True,exist_ok=True)
source=out/'MatchMindsKids-source.zip'
with zipfile.ZipFile(source,'w',zipfile.ZIP_DEFLATED) as archive:
    for name in ['app','core','gradle','tools','server','store','localization','docs']:
        for file in (root/name).rglob('*'):
            if file.is_file() and not file.name.startswith('.env') and file.suffix not in ['.jks','.keystore','.key'] and not any(part in ['build','__pycache__'] for part in file.relative_to(root).parts): archive.write(file,file.relative_to(root))
    for file in (root/'artifacts/brand').glob('*'):
        if file.is_file(): archive.write(file,file.relative_to(root))
    for file in (root/'artifacts/screenshots').rglob('manifest.json'): archive.write(file,file.relative_to(root))
    for name in ['README.md','.gitignore','settings.gradle.kts','build.gradle.kts','gradle.properties','gradlew','gradlew.bat']:
        archive.write(root/name,name)
    if (root/'artifacts/verification.json').exists(): archive.write(root/'artifacts/verification.json','artifacts/verification.json')
    if (root/'artifacts/final-build.log').exists(): archive.write(root/'artifacts/final-build.log','artifacts/final-build.log')
debug=root/'app/build/outputs/apk/debug/app-debug.apk'
if debug.exists(): shutil.copy2(debug,out/'MatchMindsKids-debug.apk')
release=root/'app/build/outputs/apk/release/app-release-unsigned.apk'
if release.exists(): shutil.copy2(release,out/'MatchMindsKids-release-UNSIGNED.apk')
manifest=[{'file':f.name,'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest()} for f in out.iterdir() if f.suffix in ['.zip','.apk']]
(out/'manifest.json').write_text(json.dumps(manifest,indent=2))
print(json.dumps(manifest,indent=2))
