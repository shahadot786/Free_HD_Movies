package com.watchfreemovies.freehdcinema786.activities;

import static com.watchfreemovies.freehdcinema786.config.AppConfig.DELAY_SPLASH_SCREEN;
import static com.watchfreemovies.freehdcinema786.utils.Constant.LOCALHOST_ADDRESS;
import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.AD_STATUS_ON;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.callbacks.CallbackSettings;
import com.watchfreemovies.freehdcinema786.config.AppConfig;
import com.watchfreemovies.freehdcinema786.database.prefs.AdsPref;
import com.watchfreemovies.freehdcinema786.database.prefs.SharedPref;
import com.watchfreemovies.freehdcinema786.models.Ads;
import com.watchfreemovies.freehdcinema786.models.License;
import com.watchfreemovies.freehdcinema786.models.Settings;
import com.watchfreemovies.freehdcinema786.rests.RestAdapter;
import com.watchfreemovies.freehdcinema786.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    ProgressBar progressBar;
    SharedPref sharedPref;
    Call<CallbackSettings> callbackCall = null;
    AdsPref adsPref;
    Settings settings;
    Ads ads;
    License license;
    LinearLayout linearLayout;
    TextView appVersion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);

        appVersion = findViewById(R.id.app_version);
        linearLayout = findViewById(R.id.splash_bg);

        appVersion.setText(new StringBuilder().append(BuildConfig.VERSION_NAME).append("").toString());

        if (sharedPref.getIsDarkTheme()){
            linearLayout.getResources().getColor(R.color.colorBackgroundDark);
        }else{
            linearLayout.getResources().getColor(R.color.colorWhite);
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (adsPref.getAdType().equals(ADMOB) && adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            if (!adsPref.getAdMobAppOpenAdId().equals("0")) {
                Application application = getApplication();
                ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::requestConfig);
            } else {
                requestConfig();
            }
        } else {
            requestConfig();
        }

    }

    private void requestConfig() {
        String decode = Tools.decodeBase64(AppConfig.SERVER_KEY);
        String data = Tools.decrypt(decode);
        String[] results = data.split("_applicationId_");
        String baseUrl = results[0].replace("http://localhost", LOCALHOST_ADDRESS);
        String applicationId = results[1];
        sharedPref.setBaseUrl(baseUrl);

        if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
            if (Tools.isConnect(this)) {
                requestAPI(baseUrl);
            } else {
                startMainActivity();
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Whoops! invalid server key or applicationId, please check your configuration")
                    .setPositiveButton(getString(R.string.dialog_ok), (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
        Log.d(TAG, baseUrl);
        Log.d(TAG, applicationId);
    }

    private void requestAPI(String apiUrl) {
        this.callbackCall = RestAdapter.createAPI(apiUrl).getSettings(AppConfig.REST_API_KEY);
        this.callbackCall.enqueue(new Callback<CallbackSettings>() {
            public void onResponse(Call<CallbackSettings> call, Response<CallbackSettings> response) {
                CallbackSettings resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    settings = resp.settings;
                    ads = resp.ads;
                    license = resp.license;

                    adsPref.saveAds(
                            ads.ad_status.replace("on", "1"),
                            ads.ad_type,
                            ads.backup_ads,
                            ads.admob_publisher_id,
                            ads.admob_app_id,
                            ads.admob_banner_unit_id,
                            ads.admob_interstitial_unit_id,
                            ads.admob_native_unit_id,
                            ads.admob_app_open_ad_unit_id,
                            ads.startapp_app_id,
                            ads.unity_game_id,
                            ads.unity_banner_placement_id,
                            ads.unity_interstitial_placement_id,
                            ads.applovin_banner_ad_unit_id,
                            ads.applovin_interstitial_ad_unit_id,
                            ads.applovin_native_ad_manual_unit_id,
                            ads.applovin_banner_zone_id,
                            ads.applovin_banner_zone_id,
                            ads.mopub_banner_ad_unit_id,
                            ads.mopub_interstitial_ad_unit_id,
                            ads.interstitial_ad_interval,
                            ads.native_ad_interval,
                            ads.native_ad_index
                    );

                    sharedPref.setConfig(
                            settings.privacy_policy,
                            settings.publisher_info,
                            settings.login_feature,
                            settings.comment_approval,
                            settings.video_menu,
                            settings.more_apps_url,
                            settings.youtube_api_key,
                            license.item_id,
                            license.item_name,
                            license.license_type
                    );

                    startMainActivity();

                }
            }

            public void onFailure(Call<CallbackSettings> call, Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                startMainActivity();
            }
        });
    }

    private void startMainActivity() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }, DELAY_SPLASH_SCREEN);
    }

}
