package it.paolodurante.droneitalia;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.*;
import android.util.Base64;
import java.nio.ByteBuffer;
import java.util.*;
import org.json.JSONObject;

/** Foreground-only passive Remote ID reception. No connections, uploads or history. */
final class RemoteIdScanner {
 interface Sink { void status(String message, boolean active); void packet(String json); }
 private final Activity activity; private final Sink sink;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private BluetoothLeScanner ble; private WifiManager wifi; private BroadcastReceiver receiver;
 private boolean active=false,bleActive=false,wifiActive=false,pending=false;
 private static final ParcelUuid RID=new ParcelUuid(UUID.fromString("0000fffa-0000-1000-8000-00805f9b34fb"));
 RemoteIdScanner(Activity activity,Sink sink){this.activity=activity;this.sink=sink;}
 private boolean granted(String permission){return activity.checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED;}
 void start(){
  if(active||pending)return;
  ArrayList<String> permissions=new ArrayList<>();
  if(!granted(Manifest.permission.ACCESS_FINE_LOCATION)){permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);}
  if(Build.VERSION.SDK_INT>=31){if(!granted(Manifest.permission.BLUETOOTH_SCAN))permissions.add(Manifest.permission.BLUETOOTH_SCAN);if(!granted(Manifest.permission.BLUETOOTH_CONNECT))permissions.add(Manifest.permission.BLUETOOTH_CONNECT);}
  if(!permissions.isEmpty()){pending=true;sink.status("Consenti posizione precisa e dispositivi nelle vicinanze per cercare segnali Remote ID.",false);activity.requestPermissions(permissions.toArray(new String[0]),42);return;}
  begin();
 }
 void permissionsResult(){if(!pending)return;pending=false;if(!granted(Manifest.permission.ACCESS_FINE_LOCATION)||(Build.VERSION.SDK_INT>=31&&(!granted(Manifest.permission.BLUETOOTH_SCAN)||!granted(Manifest.permission.BLUETOOTH_CONNECT)))){sink.status("Permessi non concessi. Abilita posizione precisa e dispositivi nelle vicinanze nelle impostazioni dell’app.",false);return;}begin();}
 private void begin(){
  try{
   LocationManager location=(LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
   if(location==null||(!location.isProviderEnabled(LocationManager.GPS_PROVIDER)&&!location.isProviderEnabled(LocationManager.NETWORK_PROVIDER))){sink.status("Attiva la posizione nelle impostazioni del telefono e riprova.",false);return;}
   active=true;StringBuilder capabilities=new StringBuilder(Build.MANUFACTURER+" "+Build.MODEL+" · ");
   BluetoothManager manager=(BluetoothManager)activity.getSystemService(Context.BLUETOOTH_SERVICE);BluetoothAdapter adapter=manager==null?null:manager.getAdapter();
   if(adapter!=null&&adapter.isEnabled()&&activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
    ble=adapter.getBluetoothLeScanner();if(ble!=null){
     ScanSettings.Builder settings=new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
     boolean extended=adapter.isLeCodedPhySupported()&&adapter.isLeExtendedAdvertisingSupported();
     if(extended)settings.setLegacy(false).setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED);
     ble.startScan(Collections.singletonList(new ScanFilter.Builder().setServiceData(RID,new byte[]{0x0d}).build()),settings.build(),callback);bleActive=true;
     capabilities.append(extended?"Bluetooth 4/5 attivo":"Bluetooth 4 attivo");
    }
   }
   if(!bleActive)capabilities.append("Bluetooth non disponibile o spento");
   wifi=(WifiManager)activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
   if(Build.VERSION.SDK_INT>=30&&wifi!=null&&wifi.isWifiEnabled()){
    receiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){if(active&&i.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,false))wifiResults();}};
    IntentFilter filter=new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    if(Build.VERSION.SDK_INT>=33)activity.registerReceiver(receiver,filter,Context.RECEIVER_EXPORTED);else activity.registerReceiver(receiver,filter);
    wifiActive=true;capabilities.append(" · Wi-Fi Beacon attivo (aggiornamenti limitati da Android)");handler.post(wifiTick);
   }else capabilities.append(" · Wi-Fi Beacon richiede Android 11+ e Wi-Fi acceso");
   capabilities.append(" · Wi-Fi NAN non incluso");
   if(!bleActive&&!wifiActive){stopSilently();sink.status(capabilities+". Accendi Bluetooth o Wi-Fi e riprova.",false);return;}
   sink.status(capabilities.toString(),true);handler.postDelayed(autoStop,5*60*1000);
  }catch(Exception e){stopSilently();sink.status("Scansione non disponibile. Controlla permessi, Bluetooth e Wi-Fi.",false);}
 }
 private final ScanCallback callback=new ScanCallback(){
  @Override public void onScanResult(int type,ScanResult result){if(!active||!bleActive||result.getScanRecord()==null)return;byte[] bytes=result.getScanRecord().getServiceData(RID);if(bytes==null||bytes.length<27||(bytes[0]&255)!=13)return;try{emit("ble:"+result.getDevice().getAddress(),bytes,2,"Bluetooth",System.currentTimeMillis());}catch(SecurityException e){stop();}}
  @Override public void onBatchScanResults(List<ScanResult> results){for(ScanResult r:results)onScanResult(0,r);}
  @Override public void onScanFailed(int code){bleActive=false;if(!wifiActive){stopSilently();sink.status("Scansione Bluetooth non riuscita ("+code+"). Attendi qualche secondo e riprova.",false);}else sink.status("Bluetooth non disponibile ("+code+"). Ricezione Wi-Fi Beacon attiva.",true);}
 };
 private final Runnable wifiTick=new Runnable(){public void run(){if(!active||!wifiActive)return;try{wifi.startScan();}catch(SecurityException e){stop();return;}handler.postDelayed(this,30000);}};
 private final Runnable autoStop=()->{stopSilently();sink.status("Scansione terminata dopo 5 minuti. Premi Cerca droni per ricominciare.",false);};
 private final Map<String,Long> seenWifi=new HashMap<>();
 private void wifiResults(){
  try{for(android.net.wifi.ScanResult r:wifi.getScanResults()){
   if(r.timestamp<=0||SystemClock.elapsedRealtime()*1000-r.timestamp>60000000L)continue;
   Long previous=seenWifi.get(r.BSSID);if(previous!=null&&r.timestamp<=previous)continue;
   for(android.net.wifi.ScanResult.InformationElement element:r.getInformationElements()){
    if(element.getId()!=221)continue;ByteBuffer buffer=element.getBytes().duplicate();if(buffer.remaining()<30)continue;byte[] bytes=new byte[buffer.remaining()];buffer.get(bytes);
    if((bytes[0]&255)==0xfa&&(bytes[1]&255)==0x0b&&(bytes[2]&255)==0xbc&&(bytes[3]&255)==13){
     emit("wifi:"+r.BSSID,bytes,5,"Wi-Fi Beacon",System.currentTimeMillis()-(SystemClock.elapsedRealtime()*1000-r.timestamp)/1000);seenWifi.put(r.BSSID,r.timestamp);
    }
   }
  }}catch(Exception e){sink.status("Ricezione Wi-Fi limitata dal telefono. Bluetooth "+(bleActive?"attivo.":"non disponibile."),active);}
 }
 private void emit(String key,byte[] bytes,int offset,String transport,long received){
  if(!active||offset<0||bytes.length-offset<25||bytes.length-offset>250)return;
  try{JSONObject data=new JSONObject();data.put("key",key);data.put("transport",transport);data.put("received",received);data.put("payload",Base64.encodeToString(Arrays.copyOfRange(bytes,offset,bytes.length),Base64.NO_WRAP));sink.packet(data.toString());}catch(Exception ignored){}
 }
 void stop(){stopSilently();sink.status("Scansione fermata. I segnali precedenti non sono più mostrati.",false);}
 private void stopSilently(){
  active=false;pending=false;handler.removeCallbacks(wifiTick);handler.removeCallbacks(autoStop);
  if(ble!=null)try{ble.stopScan(callback);}catch(Exception ignored){}ble=null;bleActive=false;wifiActive=false;
  if(receiver!=null)try{activity.unregisterReceiver(receiver);}catch(Exception ignored){}receiver=null;seenWifi.clear();
 }
}
