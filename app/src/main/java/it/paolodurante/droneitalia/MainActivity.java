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
import android.print.PrintManager;
import android.print.PrintAttributes;
import android.content.Context;
import android.app.DownloadManager;
import android.os.Environment;
import android.util.Base64;
import java.io.OutputStream;
public class MainActivity extends Activity {
 private WebView web; private LocationManager manager; private LocationListener listener;
 private ValueCallback<Uri[]> fileCallback; private static final int FILE_PICK=301, BACKUP_SAVE=302, MAPS_SAVE=303;
 private byte[] pendingBackup;
 @Override public void onCreate(Bundle state) {
 super.onCreate(state);
 LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
 box.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
 web=new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW); web.getSettings().setDomStorageEnabled(true);
 web.getSettings().setAllowFileAccess(false); web.getSettings().setAllowContentAccess(true);
 web.setWebChromeClient(new WebChromeClient(){
 @Override public boolean onShowFileChooser(WebView view,ValueCallback<Uri[]> callback,FileChooserParams params){
 if(fileCallback!=null)fileCallback.onReceiveValue(null);
 fileCallback=callback;
 try{Intent intent=params.createIntent();intent.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(intent,FILE_PICK);return true;}
 catch(Exception e){fileCallback=null;callback.onReceiveValue(null);Toast.makeText(MainActivity.this,"Selezione foto non disponibile",Toast.LENGTH_LONG).show();return false;}
 }
 });
 web.setDownloadListener((url,userAgent,contentDisposition,mimeType,contentLength)->{
 if(url.startsWith("blob:")){Toast.makeText(this,"Per il backup usa Esporta backup JSON",Toast.LENGTH_LONG).show();return;}
 if(!url.startsWith("https://"))return;
 try{DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS,"drone-italia-download");((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(request);}catch(Exception e){Toast.makeText(this,"Download non disponibile",Toast.LENGTH_LONG).show();}
 });
 web.addJavascriptInterface(new Object(){
 @JavascriptInterface public void locate(){runOnUiThread(()->MainActivity.this.locate());}
 @JavascriptInterface public void saveBackup(String encoded){
 try{byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);if(bytes.length>12000000)throw new IllegalArgumentException("Backup troppo grande");runOnUiThread(()->{pendingBackup=bytes;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/json");intent.putExtra(Intent.EXTRA_TITLE,"drone-italia-backup.json");startActivityForResult(intent,BACKUP_SAVE);});}catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Backup non disponibile",Toast.LENGTH_LONG).show());}
 }

 @JavascriptInterface public void saveMaps(String encoded){
 try{byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);if(bytes.length>5000000)throw new IllegalArgumentException("Archivio mappe troppo grande");runOnUiThread(()->{pendingBackup=bytes;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/json");intent.putExtra(Intent.EXTRA_TITLE,"drone-italia-mappe.json");startActivityForResult(intent,MAPS_SAVE);});}catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Esportazione mappe non disponibile",Toast.LENGTH_LONG).show());}
 }
 @JavascriptInterface public void printReport(String html){runOnUiThread(()->{
 WebView printView=new WebView(MainActivity.this);printView.getSettings().setJavaScriptEnabled(false);
 printView.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String url){PrintManager pm=(PrintManager)getSystemService(Context.PRINT_SERVICE);pm.print("Rapporto Drone Italia",v.createPrintDocumentAdapter("Rapporto Drone Italia"),new PrintAttributes.Builder().build());}});
 printView.loadDataWithBaseURL("https://app.local/",html,"text/html","UTF-8",null);
 });}
 }, "DroneItaliaNative");
 web.setWebViewClient(new WebViewClient(){
 @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest req){
 String url=req.getUrl().toString();
 if(url.equals("https://app.local/index.html")) {try{return new WebResourceResponse("text/html","UTF-8",getAssets().open("index.html"));}catch(Exception e){return null;}}
 if(url.equals("https://app.local/home-landscape.svg")) {try{return new WebResourceResponse("image/svg+xml","UTF-8",getAssets().open("home-landscape.svg"));}catch(Exception e){return null;}}
 return null;
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
 setContentView(box);web.loadUrl("https://app.local/index.html");
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==FILE_PICK&&fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}
 if(request==BACKUP_SAVE||request==MAPS_SAVE){if(result==RESULT_OK&&data!=null&&data.getData()!=null&&pendingBackup!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new Exception("Destinazione non disponibile");out.write(pendingBackup);Toast.makeText(this,"Backup salvato",Toast.LENGTH_LONG).show();}catch(Exception e){Toast.makeText(this,"Errore salvataggio backup",Toast.LENGTH_LONG).show();}}pendingBackup=null;}}
 private void locate(){
 boolean fine=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;
 boolean coarse=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
 if(!fine&&!coarse){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10);return;}
 stopLocation();
 manager=(LocationManager)getSystemService(LOCATION_SERVICE);
 boolean gps=fine&&manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 boolean network=manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 if(!gps&&!network){Toast.makeText(this,"Attiva la posizione nelle impostazioni",Toast.LENGTH_LONG).show();return;}
 final Location[] best={null};
 listener=new LocationListener(){
 public void onLocationChanged(Location l){
 if(l==null)return;
 if(best[0]==null||l.getAccuracy()<best[0].getAccuracy())best[0]=l;
 if(l.hasAccuracy()&&l.getAccuracy()<=100){showLocation(l);stopLocation();}
 }
 public void onProviderDisabled(String p){}
 public void onProviderEnabled(String p){}
 public void onStatusChanged(String p,int s,Bundle b){}
 };
 try{
 if(network)manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,listener);
 if(gps)manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,listener);
 Toast.makeText(this,"Ricerca posizione in corso",Toast.LENGTH_SHORT).show();
 final LocationListener active=listener;
 web.postDelayed(()->{
 if(listener!=active)return;
 Location chosen=best[0];
 if(chosen==null){
 Location recent=null;
 if(network)recent=manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 if(gps){Location g=manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(g!=null&&(recent==null||g.getTime()>recent.getTime()))recent=g;}
 if(recent!=null&&System.currentTimeMillis()-recent.getTime()<120000)chosen=recent;
 }
 stopLocation();
 if(chosen!=null)showLocation(chosen);
 else Toast.makeText(this,"Posizione non disponibile. Attiva la posizione precisa e riprova all'aperto.",Toast.LENGTH_LONG).show();
 },25000);
 }catch(SecurityException e){stopLocation();Toast.makeText(this,"Permesso posizione non disponibile",Toast.LENGTH_LONG).show();}
 }
 private void showLocation(Location l){
 final double lat=l.getLatitude(),lng=l.getLongitude(),acc=l.hasAccuracy()?l.getAccuracy():1000;
 web.evaluateJavascript("showPosition("+lat+","+lng+","+acc+")",null);
 }
 private void stopLocation(){if(manager!=null&&listener!=null){manager.removeUpdates(listener);listener=null;}}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==10){if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)locate();else Toast.makeText(this,"Puoi usare la mappa senza GPS",Toast.LENGTH_LONG).show();}}
 @Override protected void onStop(){stopLocation();super.onStop();}
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}