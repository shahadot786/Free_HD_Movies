package com.watchfreemovies.freehdcinema786.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.callbacks.CallbackAds;
import com.watchfreemovies.freehdcinema786.config.AppConfig;
import com.watchfreemovies.freehdcinema786.config.UiConfig;
import com.watchfreemovies.freehdcinema786.models.Ads;
import com.watchfreemovies.freehdcinema786.rests.RestAdapter;
import com.watchfreemovies.freehdcinema786.utils.AdsPref;
import com.watchfreemovies.freehdcinema786.utils.NetworkCheck;
import com.watchfreemovies.freehdcinema786.utils.ThemePref;
import com.watchfreemovies.freehdcinema786.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    Boolean isCancelled = false;
    private ProgressBar progressBar;
    long nid = 0;
    String url = "";
    ThemePref themePref;
    ImageView img_splash;
    Call<CallbackAds> callbackCall = null;
    AdsPref adsPref;
    Ads ads;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);

        themePref = new ThemePref(this);
        adsPref = new AdsPref(this);

        /*img_splash = findViewById(R.id.img_splash);
        if (themePref.getIsDarkTheme()) {
            img_splash.setImageResource(R.drawable.pi_splash_dark);
        } else {
            img_splash.setImageResource(R.drawable.pi_splash_light);
        }*/

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (getIntent().hasExtra("nid")) {
            nid = getIntent().getLongExtra("nid", 0);
            url = getIntent().getStringExtra("external_link");
        }

        if (NetworkCheck.isConnect(this)) {
            requestAds();
        } else {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }, UiConfig.SPLASH_TIME + 1000);
        }

    }

    private void requestAds() {
        this.callbackCall = RestAdapter.createAPI().getAds(AppConfig.API_KEY);
        this.callbackCall.enqueue(new Callback<CallbackAds>() {
            public void onResponse(Call<CallbackAds> call, Response<CallbackAds> response) {
                CallbackAds resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    ads = resp.ads;
                    if (ads.date_time.equals(adsPref.getDateTime())) {
                        Log.d("Response", "Ads Data already updated");
                    } else {
                        adsPref.saveAds(
                                ads.ad_status,
                                ads.ad_type,
                                ads.admob_publisher_id,
                                ads.admob_app_id,
                                ads.admob_banner_unit_id,
                                ads.admob_interstitial_unit_id,
                                ads.admob_native_unit_id,
                                ads.fan_banner_unit_id,
                                ads.fan_interstitial_unit_id,
                                ads.fan_native_unit_id,
                                ads.startapp_app_id,
                                ads.interstitial_ad_interval,
                                ads.native_ad_interval,
                                ads.native_ad_index,
                                ads.date_time,
                                ads.youtube_api_key
                        );
                        Log.d("Response", "Ads Data is saved");
                    }
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }, UiConfig.SPLASH_TIME);
                } else {
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }, UiConfig.SPLASH_TIME + 1000);
                }
            }

            public void onFailure(Call<CallbackAds> call, Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }, UiConfig.SPLASH_TIME);
            }
        });
    }

}
