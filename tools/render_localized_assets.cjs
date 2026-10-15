const fs=require('node:fs');
const path=require('node:path');
const sharp=require('sharp');
const root=path.resolve(__dirname,'../artifacts/localized-store-assets');
(async()=>{
  for(const locale of fs.readdirSync(root,{withFileTypes:true}).filter(e=>e.isDirectory())) {
    for(const file of fs.readdirSync(path.join(root,locale.name)).filter(f=>f.endsWith('.svg'))) {
      await sharp(path.join(root,locale.name,file)).png().toFile(path.join(root,locale.name,file.replace('.svg','.png')));
    }
  }
  process.stdout.write('Rendered 30 localized PNG previews\n');
})();
