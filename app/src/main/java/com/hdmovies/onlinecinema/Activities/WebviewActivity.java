package com.hdmovies.onlinecinema.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.ads.MaxAdView;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.AdNetwork;
import com.hdmovies.onlinecinema.databinding.ActivityWebviewBinding;

import java.util.Objects;

public class WebviewActivity extends AppCompatActivity {

    ActivityWebviewBinding binding;
    WebView webView;
    Intent intent;
    String imdbLink, name;
    ProgressDialog progressDialog;
    AdNetwork adNetwork;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //ads init
        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAd();
        //banner
        MaxAdView bannerAd = findViewById(R.id.adView);
        //check premium
        if (UiConfig.BANNER_AD_VISIBILITY) {
            bannerAd.setVisibility(View.VISIBLE);
            bannerAd.startAutoRefresh();
        } else {
            bannerAd.setVisibility(View.GONE);
            bannerAd.stopAutoRefresh();
        }

        intent = getIntent();
        imdbLink = intent.getStringExtra("IMDbLink");
        name = intent.getStringExtra("movieName");

        //toolbar
        setSupportActionBar(binding.toolbar2);
        WebviewActivity.this.setTitle(name);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        try {
            webView = findViewById(R.id.booksWebView);
            progressDialog = new ProgressDialog(this,ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(name);
            progressDialog.setMessage("Please wait while loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();

            LoadUrlWebView( imdbLink );
        }catch (Exception e){
            Log.w("TAG", "onCreate", e);
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void LoadUrlWebView(String imdbLink) {
        try {
            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new MyWebChromeClient( imdbLink ));
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setAllowContentAccess(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

            webView.loadUrl(imdbLink);
            webView.requestFocus();

        } catch (Exception e) {
            Log.w("TAG", "setUpNavigationView", e);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private class MyWebChromeClient extends WebChromeClient {
        private String urlAccount;

        public MyWebChromeClient(String urlAccount) {
            this.urlAccount = urlAccount;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            try {
                //Tools.LogCat(context, "INSIDE MyWebChromeClient | onProgressChanged / newProgress1:" + newProgress);
                progressDialog.setMessage(newProgress + "% " + "Please wait while loading...");
                if (newProgress < 100 && !progressDialog.isShowing()) {
                    if (progressDialog != null)
                        progressDialog.show();
                }
                if (newProgress == 100) {
                    if (progressDialog != null)
                        progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.w("onProgressChanged", e);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                WebviewActivity.this.setTitle(title);
            }
        }
    }

    /*private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return false;
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("intent")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null) {
                        view.loadUrl(fallbackUrl);
                        return true;
                    }
                } catch (URISyntaxException e) {
                    // Syntax problem with uri
                }
            }
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
            } else if (url.startsWith("mailto:")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                startActivity(intent);
            }
            view.loadUrl(url);
            dialog.dismiss();
            return true;
        }

    }*/
}