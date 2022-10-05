package com.hdmovies.onlinecinema.Activities;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.NetworkChecks;
import com.hdmovies.onlinecinema.Utils.Security;
import com.hdmovies.onlinecinema.databinding.ActivityPremiumBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PremiumActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    ActivityPremiumBinding binding;
    public static final String PREF_FILE = "hd_movies";
    public static final String PURCHASE_KEY = "hd_movies_lifetime_product";
    public static final String PRODUCT_ID = "shr_hd_movies";
    FirebaseDatabase database;
    FirebaseAuth auth;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    Activity activity = this;
    private BillingClient billingClient;

    //Acknowledge
    AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
                savePurchaseValueToPref(true);
                activity.recreate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //firebase instance
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        NetworkChecks networkChecks = new NetworkChecks(this);
        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 150);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        //pro cancel button
        binding.proCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PremiumActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        //privacy
        binding.privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(PremiumActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent1 = new Intent(PremiumActivity.this, WebviewActivity.class);
                    intent1.putExtra("IMDbLink","https://shrcreation.com/privacy.html");
                    intent1.putExtra("movieName","Privacy & Policy");
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                } else {
                    networkChecks.noConnectionDialog();
                }
            }
        });
        //refund policy
        binding.refundPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(PremiumActivity.this, R.style.AppCompatAlertDialogStyle);
                dialog.setIcon(R.drawable.ic_main_icon_round);
                dialog.setTitle(R.string.refund_policy);
                dialog.setMessage(R.string.refund_message);
                dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> finishActivity(11));
                dialog.show();
            }
        });
        // Establish connection to billing client
        //check subscription status from google play store cache
        //to check if item is already Subscribed or subscription is not renewed and cancelled
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    //check if service is already connected
                    //get product details
                    ProductDetails();
                    //handle purchase
                    billingClient.queryPurchasesAsync(INAPP, new PurchasesResponseListener() {
                        @Override
                        public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> myPurchase) {
                            if (!myPurchase.isEmpty()) {
                                handlePurchases(myPurchase);
                            }
                            //if no item in purchase list means subscription is not subscribed
                            //Or subscription is cancelled and not renewed for next month
                            // so update pref in both cases
                            // so next time on app launch our premium content will be locked
                            else {
                                savePurchaseValueToPref(false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                toastText.setText(R.string.Service_Disconnected);
                toast.show();
            }
        });
        //subscribe button click listener
        binding.btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(PremiumActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    purchase();
                } else {
                    networkChecks.noConnectionDialog();
                }
            }
        });

        //item subscribed
        if (getPurchaseValueFromPref()) {
            UiConfig.REWARDED__AD_VISIBILITY = false;
            UiConfig.INTERSTITIAL__AD_VISIBILITY = false;
            UiConfig.BANNER_AD_VISIBILITY = false;
            UiConfig.PRO_VISIBILITY_STATUS_SHOW = false;
            UiConfig.ENABLE_EXIT_DIALOG = false;

        }
        //item not subscribed
        else {
            UiConfig.REWARDED__AD_VISIBILITY = true;
            UiConfig.INTERSTITIAL__AD_VISIBILITY = true;
            UiConfig.BANNER_AD_VISIBILITY = true;
            UiConfig.PRO_VISIBILITY_STATUS_SHOW = true;
            UiConfig.ENABLE_EXIT_DIALOG = true;
        }
    }//ends of OnCreate

    //get product details method
    public void ProductDetails() {
        if (billingClient.isReady()) {
            getProductsDetails();
        } else {
            billingClient = BillingClient.newBuilder(activity).enablePendingPurchases().setListener(PremiumActivity.this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        getProductsDetails();
                    } else {
                        toastText.setText(R.string.Error + billingResult.getDebugMessage());
                        toast.show();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    toastText.setText(R.string.Service_Disconnected);
                    toast.show();
                }
            });
        }
    }

    //subscribe on click method
    public void purchase() {
        //check if service is already connected
        if (billingClient.isReady()) {
            initiatePurchase();
        }
        //else reconnect service
        else {
            billingClient = BillingClient.newBuilder(activity).enablePendingPurchases().setListener(PremiumActivity.this).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        initiatePurchase();
                    } else {
                        toastText.setText(R.string.Error + billingResult.getDebugMessage());
                        toast.show();
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    toastText.setText(R.string.Service_Disconnected);
                    toast.show();
                }
            });
        }
    }

    //app shared preference
    private SharedPreferences getPreferenceObject() {
        return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
    }

    private SharedPreferences.Editor getPreferenceEditObject() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_FILE, 0);
        return pref.edit();
    }

    private boolean getPurchaseValueFromPref() {
        return getPreferenceObject().getBoolean(PURCHASE_KEY, false);
    }

    private void savePurchaseValueToPref(boolean value) {
        getPreferenceEditObject().putBoolean(PURCHASE_KEY, value).commit();
    }

    //get product details method
    private void getProductsDetails() {
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                TextView itemPrice = findViewById(R.id.itemPrice);
                                TextView itemDescription = findViewById(R.id.itemDes);
                                SkuDetails itemInfo = skuDetailsList.get(0);
                                itemPrice.setText(itemInfo.getPrice());
                                itemDescription.setText(itemInfo.getTitle());
                            } else {
                                //try to add item/product id "purchase" inside managed product in google play console
                                toastText.setText(R.string.PurchaseItemnotFound);
                                toast.show();
                            }
                        } else {
                            toastText.setText(R.string.Error + billingResult.getDebugMessage());
                            toast.show();
                        }
                    }
                });
    }

    //before purchase method
    private void initiatePurchase() {
        List<String> skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                TextView itemPrice = findViewById(R.id.itemPrice);
                                SkuDetails itemInfo = skuDetailsList.get(0);
                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0))
                                        .build();
                                billingClient.launchBillingFlow(activity, flowParams);
                                itemPrice.setText(itemInfo.getPrice());
                            } else {
                                //try to add item/product id "purchase" inside managed product in google play console
                                toastText.setText(R.string.PurchaseItemnotFound);
                                toast.show();
                            }
                        } else {
                            toastText.setText(R.string.Error + billingResult.getDebugMessage());
                            toast.show();
                        }
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        //if item subscribed
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
            //other codes
            UiConfig.REWARDED__AD_VISIBILITY = false;
            UiConfig.INTERSTITIAL__AD_VISIBILITY = false;
            UiConfig.BANNER_AD_VISIBILITY = false;
            UiConfig.PRO_VISIBILITY_STATUS_SHOW = false;
            UiConfig.ENABLE_EXIT_DIALOG = false;
            //set database value
            /*database.getReference()
                    .child("UserData")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .child("checkPremium")
                    .setValue(true);
            //set 2nd verification
            database.getReference()
                    .child("UserData")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                    .child("checkPremiumID")
                    .setValue(VERIFY_PURCHASE_KEY);*/
            //start activity
            startActivity(new Intent(activity, MainActivity.class));
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);

        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            toastText.setText(R.string.PurchaseCanceled);
            toast.show();
        }
        // Handle any other error msgs
        else {
            toastText.setText(R.string.Error + billingResult.getDebugMessage());
            toast.show();
        }
    }

    void handlePurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            //if item is purchased
            if (PRODUCT_ID.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                    // Invalid purchase
                    // show error to user
                    toastText.setText(R.string.ErrorinvalidPurchase);
                    toast.show();
                    return;
                }
                // else purchase is valid
                //if item is purchased and not acknowledged
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                }
                //else item is purchased and also acknowledged
                else {
                    // Grant entitlement to the user on item purchase
                    // restart activity
                    if (!getPurchaseValueFromPref()) {
                        savePurchaseValueToPref(true);
                        toastText.setText(R.string.ItemPurchased);
                        toast.show();
                        this.recreate();
                    }
                }
            }
            //if purchase is pending
            else if (PRODUCT_ID.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                toastText.setText(R.string.PurchaseisPending);
                toast.show();
            }
            //if purchase is unknown mark false
            else if (PRODUCT_ID.equals(purchase.getSkus().get(0)) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                savePurchaseValueToPref(false);
                UiConfig.REWARDED__AD_VISIBILITY = true;
                UiConfig.INTERSTITIAL__AD_VISIBILITY = true;
                UiConfig.BANNER_AD_VISIBILITY = true;
                UiConfig.PRO_VISIBILITY_STATUS_SHOW = true;
                UiConfig.ENABLE_EXIT_DIALOG = true;
                toastText.setText(R.string.PurchaseStatusUnknown);
                toast.show();
            }
        }
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = getResources().getString(R.string.play_console_license_key);
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(activity, MainActivity.class);
        startActivity(intent);
    }
}