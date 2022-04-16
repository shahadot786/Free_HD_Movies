package com.watchfreemovies.freehdcinema786.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    public static final String PREF_FILE = "hd_movies";
    public static final String PURCHASE_KEY = "hd_movies_lifetime_product";
    String PREFS_NAME = "fire_movies_for_new_user";//change every update
    FirebaseUser currentUser;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference movies;
    DatabaseReference feeds;
    DatabaseReference users;
    DatabaseReference notifications;
    Animation bottomAnim;
    LayoutInflater inflater;
    TextView toastText,fireMovies;
    View toastLayout;
    Toast toast;
    AdNetwork adNetwork;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 40);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        String buildVersion = BuildConfig.VERSION_NAME;
        PREFS_NAME = PREFS_NAME + buildVersion;
        //Ads Initialize
        adNetwork = new AdNetwork(this);
        // Make sure to set the mediation provider value to "max" to ensure proper functionality
        AppLovinSdk.getInstance( this ).setMediationProvider( "max" );
        AppLovinSdk.initializeSdk( this, new AppLovinSdk.SdkInitializationListener() {
            @Override
            public void onSdkInitialized(final AppLovinSdkConfiguration configuration)
            {
                // AppLovin SDK is initialized, start loading ads
                adNetwork.loadInterstitialAd();
            }
        } );
        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(SplashActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            toastText.setText(R.string.network_connected);
        } else {
            toastText.setText(R.string.no_connection_text);
            toast.show();
        }
        //animations
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        //hooks
        fireMovies = findViewById(R.id.fireMovies);
        fireMovies.setAnimation(bottomAnim);
        //firebase init
        database = FirebaseDatabase.getInstance();
        //get offline data
        database.setPersistenceEnabled(true);
        //sync online data
        movies = database.getReference().child("Movies");
        feeds = database.getReference().child("Feeds");
        users = database.getReference().child("UserData");
        notifications = database.getReference().child("Notifications");
        movies.keepSynced(true);
        feeds.keepSynced(true);
        users.keepSynced(true);
        notifications.keepSynced(true);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

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
        //splash times
        ///check first time installer
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        int SPLASH_SCREEN = 4000;
        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            //Log.d("Comments", "First time");
            // first time task
            //handler for change activity
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashActivity.this, SignUpActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_SCREEN);

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent;
                    //check user currently sign in or not
                    if (currentUser != null) {
                        //after upgrade to pro
                        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
                            if (UiConfig.SPLASH_SCREEN_PRO){
                                intent = new Intent(SplashActivity.this, PremiumActivity.class);
                            }else {
                                intent = new Intent(SplashActivity.this, MainActivity.class);
                            }
                        } else {
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        }
                    } else {
                        intent = new Intent(SplashActivity.this, SignInActivity.class);
                    }
                    startActivity(intent);
                }
            }, SPLASH_SCREEN);
        }

    }//ends of onCreate
    private SharedPreferences getPreferenceObject() {
        return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
    }

    private boolean getPurchaseValueFromPref() {
        return getPreferenceObject().getBoolean(PURCHASE_KEY, false);
    }
}