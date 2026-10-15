package it.paolodurante.droneitalia;

import android.app.Activity;
import android.widget.Toast;
import com.android.billingclient.api.*;
import java.util.Collections;
import java.util.List;

/**
 * Preparazione Play Billing. Nessun blocco trial attivo finché
 * verifica server-side e ripristino licenza non sono pronti.
 */
public final class DroneBilling {
 public static final String PRODUCT_ID = "drone_italia_full";
 public static final int TRIAL_DAYS = 5;
 private final Activity activity;
 private BillingClient client;
 private ProductDetails product;
 private boolean connected = false;
 private boolean owned = false;

 public DroneBilling(Activity activity) {
  this.activity=activity;
  client=BillingClient.newBuilder(activity).enablePendingPurchases(
    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
  ).setListener((result,purchases)->{
   if(result.getResponseCode()==BillingClient.BillingResponseCode.OK && purchases!=null)process(purchases);
  }).build();
 }
 public void connect(){
  if(connected){refresh();return;}
  client.startConnection(new BillingClientStateListener(){
   @Override public void onBillingSetupFinished(BillingResult result){
    if(result.getResponseCode()!=BillingClient.BillingResponseCode.OK)return;
    connected=true;refresh();loadProduct();
   }
   @Override public void onBillingServiceDisconnected(){connected=false;}
  });
 }
 public void refresh(){
  if(!connected)return;
  QueryPurchasesParams p=QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build();
  client.queryPurchasesAsync(p,(result,purchases)->{
   if(result.getResponseCode()==BillingClient.BillingResponseCode.OK)process(purchases);
  });
 }
 private void process(List<Purchase> purchases){
  boolean found=false;
  for(Purchase p:purchases){
   if(p.getProducts().contains(PRODUCT_ID)&&p.getPurchaseState()==Purchase.PurchaseState.PURCHASED){
    found=true;
    // La licenza non viene sbloccata localmente: il token va verificato
    // con Google Play Developer API da un backend autenticato.
    // Acknowledge solo dopo la verifica server-side.
   }
  }
  owned=found;
 }
 private void loadProduct(){
  QueryProductDetailsParams params=QueryProductDetailsParams.newBuilder().setProductList(
    Collections.singletonList(QueryProductDetailsParams.Product.newBuilder()
      .setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP).build())
  ).build();
  client.queryProductDetailsAsync(params,(result,details)->{
   if(result.getResponseCode()==BillingClient.BillingResponseCode.OK &&
      !details.getProductDetailsList().isEmpty())product=details.getProductDetailsList().get(0);
  });
 }
 public void purchase(){
  if(!connected||product==null){
   Toast.makeText(activity,"Acquisto non ancora disponibile su Google Play.",Toast.LENGTH_LONG).show();
   return;
  }
  BillingFlowParams.ProductDetailsParams item=BillingFlowParams.ProductDetailsParams.newBuilder()
    .setProductDetails(product).build();
  BillingFlowParams params=BillingFlowParams.newBuilder().setProductDetailsParamsList(Collections.singletonList(item)).build();
  client.launchBillingFlow(activity,params);
 }
 public boolean hasUnverifiedPurchase(){return owned;}
 public void close(){if(client!=null)client.endConnection();}
}
