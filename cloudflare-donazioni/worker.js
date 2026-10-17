const PAYPAL_API = 'https://api-m.paypal.com';
const json = (value, status = 200) => new Response(JSON.stringify(value), {status, headers:{'Content-Type':'application/json; charset=utf-8','Cache-Control':'no-store','Access-Control-Allow-Origin':'*','Access-Control-Allow-Methods':'POST, OPTIONS, GET','Access-Control-Allow-Headers':'Content-Type'}});
const validToken = x => typeof x === 'string' && /^[a-f0-9]{32,64}$/i.test(x);
const validOrder = x => typeof x === 'string' && /^[A-Z0-9]{10,30}$/.test(x);
async function oauth(env) {
  if (!env.PAYPAL_CLIENT_ID || !env.PAYPAL_CLIENT_SECRET) throw new Error('Missing PayPal credentials');
  const r = await fetch(PAYPAL_API + '/v1/oauth2/token', {method:'POST',headers:{Authorization:'Basic '+btoa(env.PAYPAL_CLIENT_ID+':'+env.PAYPAL_CLIENT_SECRET),'Content-Type':'application/x-www-form-urlencoded'},body:'grant_type=client_credentials'});
  if (!r.ok) throw new Error('PayPal OAuth '+r.status);
  return (await r.json()).access_token;
}
async function paypal(token, path, method='GET', body, idempotencyKey) {
  const headers = {Authorization:'Bearer '+token,'Content-Type':'application/json',Accept:'application/json'};
  if (idempotencyKey) headers['PayPal-Request-Id'] = idempotencyKey;
  const r = await fetch(PAYPAL_API+path,{method,headers,...(body!==undefined?{body:JSON.stringify(body)}:{})});
  const data=await r.json().catch(()=>({}));
  if (!r.ok) throw new Error('PayPal '+r.status+' '+JSON.stringify(data).slice(0,250));
  return data;
}
function paid(order, token) {
  if (order.status !== 'COMPLETED') return false;
  return (order.purchase_units || []).some(unit =>
    unit.custom_id === token && unit.amount?.currency_code === 'EUR' &&
    (unit.payments?.captures || []).some(c => c.status === 'COMPLETED' && c.amount?.currency_code === 'EUR' && Number(c.amount.value) >= 1)
  );
}
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === 'OPTIONS') return json({ok:true});
    if (request.method === 'GET' && url.pathname === '/health') return json({ok:true,service:'Drone Italia donations'});
    if (request.method !== 'POST' || !['/create-order','/capture-order','/verify'].includes(url.pathname)) return json({error:'Not found'},404);
    let input;
    try { input=await request.json(); } catch { return json({error:'Invalid JSON'},400); }
    if (!validToken(input?.donationId)) return json({error:'Invalid donationId'},400);
    try {
      const token=await oauth(env);
      if (url.pathname === '/create-order') {
        const amount=input.amount;
        if (typeof amount !== 'number' || !Number.isFinite(amount) || amount < 1 || amount > 500 || Math.round(amount*100)!==amount*100) return json({error:'Invalid EUR amount'},400);
        const order=await paypal(token,'/v2/checkout/orders','POST',{
          intent:'CAPTURE',purchase_units:[{custom_id:input.donationId,description:'Donazione volontaria a Drone Italia',amount:{currency_code:'EUR',value:amount.toFixed(2)}}],
          payment_source:{paypal:{experience_context:{user_action:'PAY_NOW',return_url:url.origin+'/return',cancel_url:url.origin+'/cancel'}}}
        },crypto.randomUUID());
        const approvalUrl=order.links?.find(x=>x.rel==='payer-action'||x.rel==='approve')?.href;
        if (!approvalUrl) throw new Error('Missing approval link');
        return json({orderId:order.id,approvalUrl});
      }
      if (!validOrder(input.orderId)) return json({error:'Invalid orderId'},400);
      const path='/v2/checkout/orders/'+encodeURIComponent(input.orderId);
      let order=await paypal(token,path);
      // Check ownership BEFORE capturing: another installation must not be able to capture an order.
      if (!order.purchase_units?.some(unit=>unit.custom_id===input.donationId)) return json({verified:false},403);
      if (url.pathname==='/capture-order' && order.status==='APPROVED') {
        await paypal(token,path+'/capture','POST',{},'capture-'+input.orderId);
        order=await paypal(token,path);
      }
      return json({verified:paid(order,input.donationId),orderId:input.orderId});
    } catch (e) {
      console.error('Donation request failed:',e.message);
      return json({error:'PayPal request failed'},502);
    }
  }
};
