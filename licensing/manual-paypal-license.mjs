#!/usr/bin/env node
// Strumento locale per licenze firmate Drone Italia. Node.js 20+.
// La chiave privata non deve mai essere pubblicata né inserita nell'APK.
import { generateKeyPairSync, sign, verify, createPublicKey, randomUUID } from 'node:crypto';
import { readFileSync, writeFileSync, existsSync } from 'node:fs';
const [command,...args]=process.argv.slice(2);
const flag=(name)=>{const i=args.indexOf(name);return i>=0?args[i+1]:undefined};
const encode=(v)=>Buffer.from(v).toString('base64url');
const decode=(v)=>Buffer.from(v,'base64url');
function usage(){console.log('Uso:\n  node manual-paypal-license.mjs keygen --private /percorso/privata.pem --public pubblica.pem\n  node manual-paypal-license.mjs issue --private /percorso/privata.pem --order ID_ORDINE --tier standard|supporter --customer ID_CLIENTE\n  node manual-paypal-license.mjs verify --public pubblica.pem --code CODICE');}
try{
 if(command==='keygen'){
  const privatePath=flag('--private'),publicPath=flag('--public');
  if(!privatePath||!publicPath)throw Error('Specificare --private e --public');
  if(existsSync(privatePath)||existsSync(publicPath))throw Error('File già esistente: non sovrascrivo chiavi');
  const {privateKey,publicKey}=generateKeyPairSync('ed25519');
  writeFileSync(privatePath,privateKey.export({type:'pkcs8',format:'pem'}),{mode:0o600,flag:'wx'});
  writeFileSync(publicPath,publicKey.export({type:'spki',format:'pem'}),{flag:'wx'});
  console.log('Chiavi create. Conservare la privata fuori da GitHub e fare backup sicuro.');
 }else if(command==='issue'){
  const privatePath=flag('--private'),order=flag('--order'),tier=flag('--tier'),customer=flag('--customer');
  if(!privatePath||!order||!customer||!['standard','supporter'].includes(tier))throw Error('Richiesti --private, --order, --customer e --tier standard|supporter');
  const payload={v:1,product:'drone_italia_full',licenseId:randomUUID(),orderId:order,customerId:customer,tier,issuedAt:new Date().toISOString(),permanent:true};
  const body=encode(JSON.stringify(payload));
  const signature=encode(sign(null,Buffer.from(body),readFileSync(privatePath)));
  console.log('CODICE LICENZA (consegnare solo dopo verifica manuale PayPal):\nDI1.'+body+'.'+signature);
  console.log('Conservare separatamente ordine e licenseId nel registro privato. Lo script non controlla PayPal e non previene doppie emissioni.');
 }else if(command==='verify'){
  const publicPath=flag('--public'),code=flag('--code');
  if(!publicPath||!code)throw Error('Richiesti --public e --code');
  const parts=code.split('.');
  if(parts.length!==3||parts[0]!=='DI1')throw Error('Formato codice non valido');
  const valid=verify(null,Buffer.from(parts[1]),createPublicKey(readFileSync(publicPath)),decode(parts[2]));
  if(!valid)throw Error('Firma non valida');
  const data=JSON.parse(decode(parts[1]).toString('utf8'));
  if(data.v!==1||data.product!=='drone_italia_full'||data.permanent!==true)throw Error('Dati licenza non validi');
  console.log('Firma valida; licenza:',JSON.stringify(data,null,2));
 }else{usage();process.exitCode=1}
}catch(err){console.error('Errore:',err.message);process.exitCode=1}
