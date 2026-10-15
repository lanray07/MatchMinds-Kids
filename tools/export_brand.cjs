// SVG masters remain editable. Sharp rasterises the same original vectors.
const fs=require('fs');const path=require('path');const sharp=require('sharp');
const root=path.resolve(__dirname,'..','artifacts','brand');
(async()=>{
 for(const file of fs.readdirSync(root).filter(x=>x.endsWith('.svg'))){
   const input=path.join(root,file);const width=file.startsWith('world-')?1280:600;
   await sharp(input).resize({width}).png().toFile(input.replace('.svg','.png'));
 }
 for(const width of [114,512,1024]) await sharp(path.join(root,'app-icon-master.svg')).resize(width,width).png().toFile(path.join(root,`app-icon-${width}.png`));
 console.log('Original SVG masters exported to PNG.');
})();
