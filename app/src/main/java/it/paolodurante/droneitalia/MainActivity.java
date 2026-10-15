package it.paolodurante.droneitalia;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.net.Uri;
import android.location.*;
import android.webkit.*;
import android.widget.*;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.print.PrintManager;
import android.print.PrintAttributes;
import android.content.Context;
import android.app.DownloadManager;
import android.os.Environment;
import android.util.Base64;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
public class MainActivity extends Activity {
 private WebView web; private LocationManager manager; private LocationListener listener;
 private ValueCallback<Uri[]> fileCallback; private static final int FILE_PICK=301, BACKUP_SAVE=302, MAPS_SAVE=303, ROUTE_SAVE=304;
 private byte[] pendingBackup;
 private volatile boolean darkTheme=false;
 private boolean weatherLocationRequest=false;
 private RemoteIdScanner remoteIdScanner;
 private DroneBilling droneBilling;

 private int noticeDp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
 private GradientDrawable noticeBackground(int color,int border,float radius){
 GradientDrawable background=new GradientDrawable();background.setColor(color);background.setCornerRadius(noticeDp(radius));
 if(border!=0)background.setStroke(noticeDp(1),border);return background;
 }
 private Button noticeButton(String text,boolean primary){
 Button button=new Button(this);button.setText(text);button.setAllCaps(false);button.setTextSize(15);button.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
 button.setTextColor(primary?Color.WHITE:darkTheme?Color.rgb(185,220,255):Color.rgb(19,78,123));button.setMinHeight(noticeDp(48));button.setMinimumHeight(noticeDp(48));
 button.setPadding(noticeDp(14),noticeDp(10),noticeDp(14),noticeDp(10));
 button.setBackground(noticeBackground(primary?Color.rgb(18,112,211):darkTheme?Color.rgb(34,58,80):Color.rgb(237,245,253),primary?0:Color.rgb(214,229,243),12));
 return button;
 }
 private void showAppNotice(String message,boolean confirmation,JsResult result){
 if(isFinishing()||isDestroyed()){result.cancel();return;}
 LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(noticeDp(24),noticeDp(24),noticeDp(24),noticeDp(22));
 panel.setBackground(noticeBackground(darkTheme?Color.rgb(25,42,59):Color.WHITE,0,22));
 LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
 TextView icon=new TextView(this);icon.setText("!");icon.setTextSize(24);icon.setTypeface(Typeface.DEFAULT,Typeface.BOLD);icon.setGravity(Gravity.CENTER);
 icon.setTextColor(Color.rgb(160,100,9));icon.setBackground(noticeBackground(Color.rgb(255,243,215),0,20));icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
 header.addView(icon,new LinearLayout.LayoutParams(noticeDp(40),noticeDp(40)));
 TextView title=new TextView(this);title.setText("ATTENZIONE!");title.setTextColor(darkTheme?Color.rgb(228,237,246):Color.rgb(23,48,68));title.setTextSize(20);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
 LinearLayout.LayoutParams titleParams=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);titleParams.setMargins(noticeDp(12),0,0,0);header.addView(title,titleParams);panel.addView(header);
 final int maxMessageHeight=Math.round(getResources().getDisplayMetrics().heightPixels*.40f);
 ScrollView scroll=new ScrollView(this){@Override protected void onMeasure(int widthSpec,int heightSpec){super.onMeasure(widthSpec,View.MeasureSpec.makeMeasureSpec(maxMessageHeight,View.MeasureSpec.AT_MOST));}};
 final boolean maintenanceNotice=confirmation&&message.startsWith("PROMEMORIA MANUTENZIONE\n");
 TextView body=new TextView(this);body.setText(maintenanceNotice?message.substring("PROMEMORIA MANUTENZIONE\n".length()):message);body.setTextColor(darkTheme?Color.rgb(175,192,209):Color.rgb(65,88,106));body.setTextSize(16);body.setLineSpacing(noticeDp(4),1);body.setPadding(0,0,0,noticeDp(2));scroll.addView(body);
 LinearLayout.LayoutParams bodyParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);bodyParams.setMargins(0,noticeDp(18),0,noticeDp(22));panel.addView(scroll,bodyParams);
 LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
 final AlertDialog dialog=new AlertDialog.Builder(this).setView(panel).create();
 final boolean[] resolved={false};
 if(confirmation){
 Button cancel=noticeButton(maintenanceNotice?"Chiudi":"Annulla",false);LinearLayout.LayoutParams cancelParams=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1);cancelParams.setMargins(0,0,noticeDp(10),0);actions.addView(cancel,cancelParams);
 cancel.setOnClickListener(v->{if(!resolved[0]){resolved[0]=true;result.cancel();}dialog.dismiss();});
 }
 Button accept=noticeButton(maintenanceNotice?"Apri manutenzioni":confirmation?"Conferma":"OK",true);actions.addView(accept,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
 accept.setOnClickListener(v->{if(!resolved[0]){resolved[0]=true;result.confirm();}dialog.dismiss();});
 panel.addView(actions);dialog.setCanceledOnTouchOutside(false);
 dialog.setOnDismissListener(d->{if(!resolved[0]){resolved[0]=true;result.cancel();}});
 dialog.show();
 Window window=dialog.getWindow();if(window!=null){
 window.setBackgroundDrawableResource(android.R.color.transparent);
 window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
 WindowManager.LayoutParams params=window.getAttributes();params.dimAmount=.48f;window.setAttributes(params);
 int width=Math.min(noticeDp(520),getResources().getDisplayMetrics().widthPixels-noticeDp(40));
 window.setLayout(width,ViewGroup.LayoutParams.WRAP_CONTENT);
 }
 }

 @Override public void onCreate(Bundle state) {
 super.onCreate(state);
 droneBilling=new DroneBilling(this);droneBilling.connect();
 LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
 box.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
 web=new WebView(this); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW); web.getSettings().setDomStorageEnabled(true);
 web.getSettings().setAllowFileAccess(false); web.getSettings().setAllowContentAccess(true);
 web.setWebChromeClient(new WebChromeClient(){
 @Override public boolean onJsAlert(WebView view,String url,String message,JsResult result){
 showAppNotice(message,false,result);return true;
 }
 @Override public boolean onJsConfirm(WebView view,String url,String message,JsResult result){
 showAppNotice(message,true,result);return true;
 }
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
 @JavascriptInterface public void shareFlight(String text,String photo){
 if(text==null||text.length()>20000)return;
 try{
 Uri attachment=null;
 if(photo!=null&&!photo.isEmpty()){
  if(photo.length()>3000000||!photo.startsWith("data:image/"))throw new IllegalArgumentException();
  int comma=photo.indexOf(',');if(comma<0)throw new IllegalArgumentException();
  byte[] data=Base64.decode(photo.substring(comma+1),Base64.DEFAULT);
  BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,bounds);
  if(bounds.outWidth<=0||bounds.outHeight<=0)throw new IllegalArgumentException();
  BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;
  while(Math.max(bounds.outWidth,bounds.outHeight)/options.inSampleSize>1600)options.inSampleSize*=2;
  Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,options);if(bitmap==null)throw new IllegalArgumentException();
  File dir=new File(getCacheDir(),"flight-shares");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException();
  File[] old=dir.listFiles();if(old!=null)for(File file:old)if(System.currentTimeMillis()-file.lastModified()>86400000L)file.delete();
  File file=new File(dir,"flight-"+System.currentTimeMillis()+".jpg");
  try(FileOutputStream out=new FileOutputStream(file)){if(!bitmap.compress(Bitmap.CompressFormat.JPEG,90,out))throw new IllegalStateException();}finally{bitmap.recycle();}
  attachment=Uri.parse("content://"+getPackageName()+".sharedflight/"+file.getName());
 }
 final Uri image=attachment;
 runOnUiThread(()->{
  try{
   Intent send=new Intent(Intent.ACTION_SEND);send.setType(image==null?"text/plain":"image/jpeg");send.putExtra(Intent.EXTRA_TEXT,text);send.putExtra(Intent.EXTRA_SUBJECT,"Volo Drone Italia");
   if(image!=null){send.putExtra(Intent.EXTRA_STREAM,image);send.setClipData(ClipData.newUri(getContentResolver(),"Foto del volo",image));send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);}
   startActivity(Intent.createChooser(send,"Condividi volo"));
  }catch(Exception e){Toast.makeText(MainActivity.this,"Condivisione non disponibile",Toast.LENGTH_LONG).show();}
 });
 }catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Foto non condivisibile. Riprova senza foto.",Toast.LENGTH_LONG).show());}
 }
 @JavascriptInterface public void setDarkTheme(boolean enabled){darkTheme=enabled;}
 @JavascriptInterface public void locate(){runOnUiThread(()->{weatherLocationRequest=false;MainActivity.this.locate();});}
 @JavascriptInterface public void startRemoteId(){runOnUiThread(()->{if(remoteIdScanner==null)remoteIdScanner=new RemoteIdScanner(MainActivity.this,new RemoteIdScanner.Sink(){public void status(String message,boolean active){runOnUiThread(()->web.evaluateJavascript("if(window.diRemoteIdStatus)window.diRemoteIdStatus("+org.json.JSONObject.quote(message)+","+active+")",null));}public void packet(String json){runOnUiThread(()->web.evaluateJavascript("if(window.diRemoteIdPacket)window.diRemoteIdPacket("+json+")",null));}});remoteIdScanner.start();});}
 @JavascriptInterface public void stopRemoteId(){runOnUiThread(()->{if(remoteIdScanner!=null)remoteIdScanner.stop();});}
 @JavascriptInterface public void locateWeather(){runOnUiThread(()->{weatherLocationRequest=true;MainActivity.this.locate();});}
 @JavascriptInterface public void saveBackup(String encoded){
 try{byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);if(bytes.length>12000000)throw new IllegalArgumentException("Backup troppo grande");runOnUiThread(()->{pendingBackup=bytes;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/json");intent.putExtra(Intent.EXTRA_TITLE,"drone-italia-backup.json");startActivityForResult(intent,BACKUP_SAVE);});}catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Backup non disponibile",Toast.LENGTH_LONG).show());}
 }

 @JavascriptInterface public void saveBackupZip(String encoded){
 try{
  if(encoded==null||encoded.length()>67000000)throw new IllegalArgumentException("Backup ZIP troppo grande");
  byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);if(bytes.length>50000000)throw new IllegalArgumentException("Backup ZIP troppo grande");
  runOnUiThread(()->{pendingBackup=bytes;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/zip");intent.putExtra(Intent.EXTRA_TITLE,"drone-italia-backup.zip");startActivityForResult(intent,BACKUP_SAVE);});
 }catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Backup ZIP non disponibile",Toast.LENGTH_LONG).show());}
 }
 @JavascriptInterface public void saveRouteGpx(String xml){
 try{
  if(xml==null||xml.length()>1000000||!xml.startsWith("<?xml"))throw new IllegalArgumentException("Percorso non valido");
  final byte[] bytes=xml.getBytes(java.nio.charset.StandardCharsets.UTF_8);
  runOnUiThread(()->{
   try{pendingBackup=bytes;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType("application/gpx+xml");intent.putExtra(Intent.EXTRA_TITLE,"drone-italia-percorso.gpx");startActivityForResult(intent,ROUTE_SAVE);}
   catch(Exception e){pendingBackup=null;Toast.makeText(MainActivity.this,"Esportazione GPX non disponibile",Toast.LENGTH_LONG).show();}
  });
 }catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Percorso non esportabile",Toast.LENGTH_LONG).show());}
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
 if(url.equals("https://app.local/easa-drone-catalog.json")){
  java.net.HttpURLConnection connection=null;
  try{
   java.net.URL source=new java.net.URL("https://www.easa.europa.eu/en/domains/drones-air-mobility/drones-evtol-designs/drones-eu-operations/export-json?_format=json&page=");
   connection=(java.net.HttpURLConnection)source.openConnection();
   connection.setConnectTimeout(6000);connection.setReadTimeout(10000);connection.setUseCaches(false);
   connection.setRequestProperty("Accept","application/json");connection.setRequestProperty("User-Agent","DroneItalia/1.0");
   if(connection.getResponseCode()!=200)throw new java.io.IOException("EASA unavailable");
   java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();
   try(java.io.InputStream input=connection.getInputStream()){
    byte[] chunk=new byte[8192];int count;
    while((count=input.read(chunk))!=-1){if(buffer.size()+count>2000000)throw new java.io.IOException("Catalog too large");buffer.write(chunk,0,count);}
   }
   byte[] bytes=buffer.toByteArray();
   new org.json.JSONArray(new String(bytes,java.nio.charset.StandardCharsets.UTF_8));
   return new WebResourceResponse("application/json","UTF-8",new java.io.ByteArrayInputStream(bytes));
  }catch(Exception error){
   return new WebResourceResponse("application/json","UTF-8",503,"Service Unavailable",java.util.Collections.singletonMap("Cache-Control","no-store"),new java.io.ByteArrayInputStream("[]".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
  }finally{if(connection!=null)connection.disconnect();}
 }

 if(url.equals("https://app.local/jszip.min.js")){try{return new WebResourceResponse("application/javascript","UTF-8",getAssets().open("jszip.min.js"));}catch(Exception e){return null;}}
 if(url.equals("https://app.local/home-background-hd.jpg")){try{return new WebResourceResponse("image/jpeg",null,getAssets().open("home-background-hd.jpg"));}catch(Exception e){return null;}}
 if(url.equals("https://app.local/index.html")) {try{return new WebResourceResponse("text/html","UTF-8",getAssets().open("index.html"));}catch(Exception e){return null;}}
 if(req.getUrl().getHost()!=null && req.getUrl().getHost().equals("app.local") && req.getUrl().getPath().equals("/drone-home-hero.jpg")) {try{return new WebResourceResponse("image/jpeg",null,getAssets().open("drone-home-hero.jpg"));}catch(Exception e){return null;}}
 if(url.equals("https://app.local/home-reference.svg")) {try{return new WebResourceResponse("image/svg+xml","UTF-8",getAssets().open("home-reference.svg"));}catch(Exception e){return null;}}
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
 if(request==BACKUP_SAVE||request==MAPS_SAVE||request==ROUTE_SAVE){if(result==RESULT_OK&&data!=null&&data.getData()!=null&&pendingBackup!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new Exception("Destinazione non disponibile");out.write(pendingBackup);Toast.makeText(this,request==ROUTE_SAVE?"Percorso GPX salvato":"Backup salvato",Toast.LENGTH_LONG).show();}catch(Exception e){Toast.makeText(this,request==ROUTE_SAVE?"Errore salvataggio percorso":"Errore salvataggio backup",Toast.LENGTH_LONG).show();}}pendingBackup=null;}}
 private void locate(){
 boolean fine=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;
 boolean coarse=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
 if(!fine&&!coarse){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},10);return;}
 stopLocation();
 manager=(LocationManager)getSystemService(LOCATION_SERVICE);
 boolean gps=fine&&manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 boolean network=manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 if(!gps&&!network){reportWeatherLocationError("Attiva la posizione nelle impostazioni e riprova.");Toast.makeText(this,"Attiva la posizione nelle impostazioni",Toast.LENGTH_LONG).show();return;}
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
 else {reportWeatherLocationError("Posizione non disponibile. Attiva la posizione precisa e riprova all’aperto.");Toast.makeText(this,"Posizione non disponibile. Attiva la posizione precisa e riprova all'aperto.",Toast.LENGTH_LONG).show();}
 },25000);
 }catch(SecurityException e){stopLocation();reportWeatherLocationError("Permesso posizione non disponibile. Consenti il GPS e riprova.");Toast.makeText(this,"Permesso posizione non disponibile",Toast.LENGTH_LONG).show();}
 }
 private void showLocation(Location l){
 final double lat=l.getLatitude(),lng=l.getLongitude(),acc=l.hasAccuracy()?l.getAccuracy():1000;
 final boolean weather=weatherLocationRequest;weatherLocationRequest=false;
 web.evaluateJavascript("showPosition("+lat+","+lng+","+acc+","+weather+")",null);
 }
 private void reportWeatherLocationError(String message){
  if(!weatherLocationRequest)return;weatherLocationRequest=false;
  web.evaluateJavascript("if(window.diWeatherGpsFailed)window.diWeatherGpsFailed("+org.json.JSONObject.quote(message)+");",null);
 }
 private void stopLocation(){if(manager!=null&&listener!=null){manager.removeUpdates(listener);listener=null;}}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==42){if(remoteIdScanner!=null)remoteIdScanner.permissionsResult();return;}if(r==10){if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)locate();else {reportWeatherLocationError("Permesso GPS negato. Consenti la posizione per aggiornare il meteo.");Toast.makeText(this,"Puoi usare la mappa senza GPS",Toast.LENGTH_LONG).show();}}}
 @Override protected void onStop(){if(remoteIdScanner!=null)remoteIdScanner.stop();stopLocation();super.onStop();}
 @Override protected void onResume(){
 super.onResume();
 if(web!=null)web.postDelayed(()->{if(!isFinishing()&&!isDestroyed())web.evaluateJavascript("if(window.diCheckMaintenanceAtStartup)window.diCheckMaintenanceAtStartup();",null);},1200);
 }
 @Override protected void onDestroy(){if(droneBilling!=null)droneBilling.close();web.destroy();super.onDestroy();}
}
