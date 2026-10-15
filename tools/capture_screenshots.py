"""Capture actual debug gameplay via adb and produce editable SVG + PNG store layouts.
Requires a running Android device/emulator and Pillow. Never creates fake device captures.
"""
from pathlib import Path
import argparse, subprocess, json, time, html, hashlib, textwrap, os, re
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parents[1]
def run(args,**kwargs): return subprocess.run(args,check=True,**kwargs)
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--adb',default=os.environ.get('ANDROID_HOME','C:/Users/User/AppData/Local/Android/Sdk')+'/platform-tools/adb.exe');parser.add_argument('--variant',choices=['tablet','phone'],default='tablet');args=parser.parse_args()
    out=ROOT/'artifacts/screenshots'/args.variant;out.mkdir(parents=True,exist_ok=True)
    manifest={'variant':args.variant,'locale':'en-GB','captures':[],'status':'not captured','source':'Actual debug APK rendered on connected Android device. Premium-only scenes use debug QA access, not purchase success.'}
    display_changed=False
    original_display={}
    try:
        devices=subprocess.check_output([args.adb,'devices'],text=True)
        if not any(line.endswith('\tdevice') for line in devices.splitlines()): raise RuntimeError('No authorised Android device or booted emulator connected.')
        package='com.matchminds.kids.debug'
        run([args.adb,'install','-r',str(ROOT/'app/build/outputs/apk/debug/app-debug.apk')],capture_output=True)
        for setting in ['size','density']:
            current=subprocess.check_output([args.adb,'shell','wm',setting],text=True)
            override=re.search(r'Override '+setting+r':\s*(\S+)',current)
            original_display[setting]=override.group(1) if override else 'reset'
        display_changed=True
        run([args.adb,'shell','wm','size','1280x800' if args.variant=='tablet' else '720x1280'])
        run([args.adb,'shell','wm','density','160' if args.variant=='tablet' else '240'])
        for scene in json.loads((ROOT/'store/screenshots.json').read_text()):
            run([args.adb,'shell','am','force-stop',package])
            run([args.adb,'shell','am','start','-n',package+'/com.matchminds.kids.MainActivity','--ez','qa_premium','true','--es','qa_screen',scene['scene']],capture_output=True)
            time.sleep(7 if scene['scene']=='rewards' else 4)
            activities=subprocess.check_output([args.adb,'shell','dumpsys','activity','activities'],text=True)
            if not any(package in line and ('mResumedActivity' in line or 'topResumedActivity' in line) for line in activities.splitlines()):
                raise RuntimeError('The app is not the resumed activity; no gameplay capture was recorded.')
            raw=out/(scene['id']+'-raw.png')
            with raw.open('wb') as file: run([args.adb,'exec-out','screencap','-p'],stdout=file)
            im=Image.open(raw).convert('RGB');im.verify() if hasattr(im,'verify') else None
            image=Image.new('RGB',(1280,800) if args.variant=='tablet' else (720,1280),'#FFF9EE');draw=ImageDraw.Draw(image)
            fontpath='C:/Windows/Fonts/seguisb.ttf'; smallpath='C:/Windows/Fonts/segoeui.ttf'
            big=ImageFont.truetype(fontpath,42 if args.variant=='tablet' else 36);small=ImageFont.truetype(smallpath,22)
            headline=scene['headline'];support=scene['support']
            if scene['id']=='04-numbers': support='Match numbers with groups of stars.'
            lines=textwrap.wrap(headline,48 if args.variant=='tablet' else 29)
            top=24
            for line in lines: draw.text((32,top),line,font=big,fill='#233D42');top+=51
            support_lines=textwrap.wrap(support,85 if args.variant=='tablet' else 44)
            for line in support_lines:draw.text((32,top+4),line,font=small,fill='#237D70');top+=29
            margin=32;available=(image.width-margin*2,image.height-top-36)
            im=Image.open(raw).convert('RGB');im.thumbnail(available,Image.Resampling.LANCZOS)
            x=(image.width-im.width)//2;y=top+22
            image.paste(im,(x,y));png=out/(scene['id']+'.png');image.save(png)
            # Editable composition references the real capture, leaving headline and support as text.
            texts=''.join(f'<text x="32" y="{60+i*51}" font-family="Segoe UI,sans-serif" font-size="42" fill="#233D42">{html.escape(line)}</text>' for i,line in enumerate(lines))
            support_text=''.join(f'<text x="32" y="{24+51*len(lines)+26+i*29}" font-family="Segoe UI,sans-serif" font-size="22" fill="#237D70">{html.escape(line)}</text>' for i,line in enumerate(support_lines))
            master=f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 {image.width} {image.height}"><rect width="100%" height="100%" fill="#FFF9EE"/>{texts}{support_text}<image href="{raw.name}" x="{x}" y="{y}" width="{im.width}" height="{im.height}"/></svg>'
            (out/(scene['id']+'.svg')).write_text(master,encoding='utf-8')
            manifest['captures'].append({'id':scene['id'],'source':raw.name,'png':png.name,'width':image.width,'height':image.height,'sha256':hashlib.sha256(raw.read_bytes()).hexdigest(),'approved':False})
        manifest['status']='captured; human visual and feature review required'
    except Exception as error:
        manifest['error']=str(error)
        print('Screenshot capture incomplete:',error)
    finally:
        if display_changed:
            for setting,value in original_display.items(): subprocess.run([args.adb,'shell','wm',setting,value],check=False,capture_output=True)
        (out/'manifest.json').write_text(json.dumps(manifest,indent=2))
    return 0 if len(manifest['captures'])==8 else 1
if __name__=='__main__': raise SystemExit(main())
