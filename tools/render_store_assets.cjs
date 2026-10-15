const fs=require('fs');const path=require('path');const sharp=require('sharp');
const root=path.resolve(__dirname,'..','artifacts','store-assets');
(async()=>{
 const manifest=JSON.parse(fs.readFileSync(path.join(root,'manifest.json'),'utf8'));
 for(const item of manifest.screenshots){
  const source=path.join(root,item.master),output=path.join(root,item.png);
  await sharp(source,{limitInputPixels:false}).flatten({background:'#FFF9EE'}).png().toFile(output);
 }
 for(const size of [114,512])await sharp(path.join(root,'store-icon-master.svg')).resize(size,size).png().toFile(path.join(root,`store-icon-${size}.png`));
 await sharp(path.join(root,'promotional-1024x500.svg')).png().toFile(path.join(root,'promotional-1024x500.png'));
 console.log(`${manifest.screenshots.length} screenshots exported; icon variants and promotional image exported.`);
})();
