"""Premium store layouts built around unchanged native app renders.
Renders are produced by StoreCaptureTest; no gameplay is recreated by this script.
"""
from pathlib import Path
import base64, hashlib, html, json
from PIL import Image, ImageFont

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'artifacts/store-assets'
OUT.mkdir(parents=True,exist_ok=True)
SCENES=json.loads((ROOT/'store/screenshots.json').read_text(encoding='utf-8'))

def data_image(path): return 'data:image/png;base64,'+base64.b64encode(path.read_bytes()).decode()
def text(x,y,value,size=40,weight=400,colour='#233D42'):
    return f'<text x="{x}" y="{y}" font-family="Segoe UI,Arial,sans-serif" font-size="{size}" font-weight="{weight}" fill="{colour}">{html.escape(value)}</text>'
def wrap(value,width,size,bold=False):
    font=ImageFont.truetype('C:/Windows/Fonts/seguisb.ttf' if bold else 'C:/Windows/Fonts/segoeui.ttf',size)
    lines=[];current=''
    for word in value.split():
        proposed=(current+' '+word).strip()
        if current and font.getlength(proposed)>width: lines.append(current);current=word
        else: current=proposed
    return lines+[current]
def svg(width,height,body): return f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">{body}</svg>'

# Amazon store icons use square edge-to-edge key art rather than a rounded outer mask.
icon=(ROOT/'artifacts/brand/app-icon-master.svg').read_text().replace('width="100" height="100" rx="22"','width="100" height="100"')
(OUT/'store-icon-master.svg').write_text(icon,encoding='utf-8')
milo=data_image(ROOT/'artifacts/brand/milo-welcome.png')
banner='<rect width="1024" height="500" fill="#237D70"/>'
banner+='<circle cx="838" cy="250" r="180" fill="#FFF9EE" opacity=".09"/>'
banner+=text(60,220,'MatchMinds',82,800,'#FFF9EE')+text(62,315,'Kids',76,800,'#F3BC4E')
banner+=f'<image href="{milo}" x="690" y="100" width="285" height="305" preserveAspectRatio="xMidYMid meet"/>'
(OUT/'promotional-1024x500.svg').write_text(svg(1024,500,banner),encoding='utf-8')

manifest={'provenance':'Unchanged screenshots of the production MainActivity/Compose UI, rendered with Robolectric Native Graphics at Android SDK 34, using seeded QA gameplay. These are host renders, not physical Fire-device captures.','locale':'en-GB','screenshots':[],'status':'awaiting native renders'}
for variant,width,height in [('tablet',2560,1600),('phone',1600,2560)]:
    directory=OUT/variant;directory.mkdir(exist_ok=True)
    for scene in SCENES:
        raw=ROOT/f'artifacts/screenshots/host-{variant}/{scene["id"]}-raw.png'
        if not raw.exists(): continue
        with Image.open(raw) as image: rw,rh=image.size
        margin=100 if variant=='tablet' else 90
        headline=scene['headline'];support=scene['support']
        if scene['id']=='04-numbers':support='Match numbers with groups of stars.'
        font_size=76 if variant=='tablet' else 82
        body=f'<rect width="{width}" height="{height}" fill="#FFF9EE"/><rect width="{width}" height="18" fill="#237D70"/>'
        body+=text(margin,82,'MATCHMINDS KIDS',32,800,'#237D70')
        y=180
        for line in wrap(headline,width-margin*2,font_size,True):body+=text(margin,y,line,font_size,800);y+=font_size+14
        for line in wrap(support,width-margin*2,38):body+=text(margin,y+8,line,38,400,'#237D70');y+=50
        top=y+45;available_w=width-margin*2;available_h=height-top-115
        scale=min(available_w/rw,available_h/rh);iw=int(rw*scale);ih=int(rh*scale)
        x=(width-iw)//2;y_image=top+(available_h-ih)//2
        body+=f'<defs><filter id="shadow" x="-20%" y="-20%" width="140%" height="150%"><feDropShadow dx="0" dy="15" stdDeviation="20" flood-color="#233D42" flood-opacity=".12"/></filter></defs>'
        body+=f'<rect x="{x-5}" y="{y_image-5}" width="{iw+10}" height="{ih+10}" rx="24" fill="#FFFFFF" filter="url(#shadow)"/>'
        body+=f'<image href="{data_image(raw)}" x="{x}" y="{y_image}" width="{iw}" height="{ih}"/>'
        footer='Little matches. Big discoveries.'
        if scene['scene'] in ['NUMBERS','LETTERS']:footer='More adventures with the one-time premium unlock.'
        body+=text(margin,height-45,footer,28,600,'#237D70')
        master=directory/(scene['id']+'.svg');master.write_text(svg(width,height,body),encoding='utf-8')
        manifest['screenshots'].append({'id':scene['id'],'variant':variant,'raw':str(raw.relative_to(ROOT)),'raw_width':rw,'raw_height':rh,'raw_sha256':hashlib.sha256(raw.read_bytes()).hexdigest(),'master':str(master.relative_to(OUT)),'png':str(master.with_suffix('.png').relative_to(OUT)),'width':width,'height':height,'reviewed':False})
if len(manifest['screenshots'])==16:manifest['status']='composed; raster export and visual review required'
(OUT/'manifest.json').write_text(json.dumps(manifest,indent=2))
print(f'{len(manifest["screenshots"])} screenshot masters composed from real native renders. Brand icon and promotional masters ready.')
