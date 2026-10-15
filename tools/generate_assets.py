"""Original editable artwork. No downloaded or third-party visual assets."""
from pathlib import Path
import json, math, wave, struct
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'artifacts' / 'brand'
OUT.mkdir(parents=True, exist_ok=True)
fox = '''<path fill="#E98845" d="M18 37L17 8 42 27 58 27 83 8 82 39Q92 75 50 89Q10 75 18 37"/>
<path fill="#F0B2A3" d="M23 31L23 17 35 28ZM65 28L77 17 77 31Z"/>
<path fill="#FFF4D9" d="M19 52L50 65 81 52 72 76 50 89 28 76Z"/>
<g fill="#233D42"><ellipse cx="35" cy="48" rx="3" ry="4.5"/><ellipse cx="65" cy="48" rx="3" ry="4.5"/><ellipse cx="50" cy="67" rx="6" ry="4"/></g>
<path d="M44 75Q50 82 56 75" fill="none" stroke="#233D42" stroke-width="2.5" stroke-linecap="round"/>'''
def svg(body, view='0 0 100 100'):
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{view}">{body}</svg>'
def write(name, body, view='0 0 100 100'):
    (OUT / f'{name}.svg').write_text(svg(body,view), encoding='utf-8')
write('milo-head', fox)
for pose in ['welcome','thinking','celebrating','encouraging','explaining','completion']:
    arm = '17 45' if pose in ['welcome','celebrating','completion'] else '22 77'
    arm2 = '88 50' if pose == 'explaining' else '78 73'
    body = f'''<ellipse cx="50" cy="93" rx="37" ry="4" fill="#E5E7D3"/>
    <ellipse cx="74" cy="76" rx="18" ry="14" fill="#E98845"/><path d="M81 62L92 68 91 83 79 80Z" fill="#FFF4D9"/>
    <ellipse cx="50" cy="70" rx="19" ry="21" fill="#E98845"/><ellipse cx="50" cy="72" rx="11" ry="14" fill="#FFF4D9"/>
    <path d="M35 62L{arm}M65 62L{arm2}" stroke="#E98845" stroke-width="10" stroke-linecap="round"/>
    <g fill="#233D42"><ellipse cx="39" cy="90" rx="10" ry="4"/><ellipse cx="63" cy="90" rx="10" ry="4"/></g>
    <g transform="translate(9 -2) scale(.82 .72)">{fox}</g>'''
    if pose in ['celebrating','completion']:
        body += '<path fill="#F3BC4E" d="M12 9L15 15 22 17 16 21 17 28 12 24 6 28 7 21 2 17 9 15Z"/>'
    if pose == 'thinking': body += '<g fill="#F3BC4E"><circle cx="83" cy="22" r="3"/><circle cx="90" cy="12" r="5"/></g>'
    write(f'milo-{pose}', body)
write('app-icon-master','<rect width="100" height="100" rx="22" fill="#237D70"/><rect x="15" y="21" width="43" height="59" rx="9" fill="#FFF9EE" transform="rotate(-8 36 50)"/><rect x="53" y="28" width="34" height="59" rx="9" fill="#F3BC4E" transform="rotate(8 70 57)"/><g transform="translate(12 12) scale(.76)">'+fox+'</g>')
write('card-back','<rect width="100" height="100" rx="18" fill="#237D70"/><path fill="#F3BC4E" d="M50 20L58 40 80 42 63 57 68 80 50 68 31 80 36 57 20 42 42 40Z"/>')
for world,colour in [('woodland','#E4EFD9'),('ocean','#E6F2F7'),('starlight','#F0EBF8')]:
    write('world-'+world,f'<rect width="1280" height="800" fill="{colour}"/><circle cx="1030" cy="220" r="150" fill="#FFF9EE"/><path d="M0 650Q300 450 650 650T1280 620V800H0Z" fill="#237D70" opacity=".1"/>','0 0 1280 800')
# Short original, gently enveloped confirmation sound.
with wave.open(str(ROOT/'app/src/main/res/raw/chime.wav'),'w') as out:
    out.setparams((1,2,22050,0,'NONE','not compressed'))
    samples=[int(5500*math.sin(2*math.pi*(660 if i<3300 else 880)*i/22050)*math.sin(math.pi*i/6615)**2) for i in range(6615)]
    out.writeframes(struct.pack('<'+'h'*len(samples),*samples))
(OUT/'asset-manifest.json').write_text(json.dumps({'visual_origin':'Original geometric vector artwork authored for MatchMinds Kids','master_format':'SVG; in-app illustration source is ui/Artwork.kt','palette':['#237D70','#E98845','#FFF9EE','#F3BC4E','#233D42'],'mascot':'Milo the Fox','poses':['welcome','thinking','celebrating','encouraging','explaining','completion'],'audio':'Essential narration generated locally with Microsoft Hazel Desktop. Distribution rights require publisher review before release. Chime is original procedural audio.'},indent=2))
print(f'Wrote {len(list(OUT.glob("*.svg")))} editable SVG assets to {OUT}')
