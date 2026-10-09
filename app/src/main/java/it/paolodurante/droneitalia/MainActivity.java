package it.paolodurante.droneitalia;
import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.location.*;
import android.webkit.*;
import android.widget.*;
import android.view.View;
public class MainActivity extends Activity {
 private WebView web; private LocationManager manager; private LocationListener listener;
 @Override public void onCreate(Bundle state) {
 super.onCreate(state);
 LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
 box.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
 web=new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW); web.getSettings().setDomStorageEnabled(true);
 web.getSettings().setAllowFileAccess(false); web.getSettings().setAllowContentAccess(false);
 web.setWebViewClient(new WebViewClient(){
 @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest req){
 if(req.getUrl().toString().equals("https://app.local/index.html")) {try{return new WebResourceResponse("text/html","UTF-8",getAssets().open("index.html"));}catch(Exception e){return null;}} return null;
 }
 @Override public void onReceivedHttpError(WebView v,WebResourceRequest req,WebResourceResponse response){
 if(req.getUrl().getHost()!=null && (req.getUrl().getHost().contains("openstreetmap.org") || req.getUrl().getHost().contains("cloudflare.com"))) android.util.Log.w("DroneItaliaMap","HTTP "+response.getStatusCode()+" "+req.getUrl().getHost());
 }
 @Override public void onReceivedError(WebView v,WebResourceRequest req,WebResourceError error){android.util.Log.w("DroneItaliaMap","Risorsa non caricata: "+req.getUrl().getHost()+" / "+error.getDescription());}
 @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest req){
 if(!req.isForMainFrame())return false;
 Uri u=req.getUrl(); if(u.toString().equals("https://app.local/index.html"))return false;
 if("https".equals(u.getScheme()))startActivity(new Intent(Intent.ACTION_VIEW,u));return true;
 }});
 box.addView(web,new LinearLayout.LayoutParams(-1,0,1));
 Button gps=new Button(this);gps.setText("Mostra la mia posizione");gps.setOnClickListener(v->locate());box.addView(gps);
 setContentView(box);web.loadUrl("https://app.local/index.html");
 }
 private void locate(){
 if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10);return;}
 manager=(LocationManager)getSystemService(LOCATION_SERVICE);
 String provider=manager.isProviderEnabled(LocationManager.GPS_PROVIDER)?LocationManager.GPS_PROVIDER:LocationManager.NETWORK_PROVIDER;
 if(!manager.isProviderEnabled(provider)){Toast.makeText(this,"Attiva la posizione nelle impostazioni",Toast.LENGTH_LONG).show();return;}
 listener=new LocationListener(){public void onLocationChanged(Location l){web.evaluateJavascript("showPosition("+l.getLatitude()+","+l.getLongitude()+","+l.getAccuracy()+")",null);stopLocation();} public void onProviderDisabled(String p){} public void onProviderEnabled(String p){} public void onStatusChanged(String p,int s,Bundle b){}};
 try{manager.requestLocationUpdates(provider,1000,0,listener);Toast.makeText(this,"Ricerca posizione in corso",Toast.LENGTH_SHORT).show();web.postDelayed(()->{if(listener!=null){stopLocation();Toast.makeText(this,"Posizione non disponibile. Riprova all'aperto.",Toast.LENGTH_LONG).show();}},20000);}catch(SecurityException e){Toast.makeText(this,"Permesso posizione non disponibile",Toast.LENGTH_LONG).show();}
 }
 private void stopLocation(){if(manager!=null&&listener!=null){manager.removeUpdates(listener);listener=null;}}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==10){if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)locate();else Toast.makeText(this,"Puoi usare la mappa senza GPS",Toast.LENGTH_LONG).show();}}
 @Override protected void onStop(){stopLocation();super.onStop();}
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}