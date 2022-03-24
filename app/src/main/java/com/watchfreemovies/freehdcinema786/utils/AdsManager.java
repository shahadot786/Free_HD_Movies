package com.watchfreemovies.freehdcinema786.utils;

import static com.watchfreemovies.freehdcinema786.config.AppConfig.USE_LEGACY_GDPR_EU_CONSENT;

import android.app.Activity;
import android.view.View;

import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.database.prefs.AdsPref;
import com.watchfreemovies.freehdcinema786.database.prefs.SharedPref;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;
import com.solodroid.ads.sdk.format.NativeAdFragment;
import com.solodroid.ads.sdk.gdpr.GDPR;
import com.solodroid.ads.sdk.gdpr.LegacyGDPR;

public class AdsManager {

    Activity activity;
    AdNetwork.Initialize adNetwork;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    NativeAd.Builder nativeAd;
    NativeAdFragment.Builder nativeAdView;
    SharedPref sharedPref;
    AdsPref adsPref;
    LegacyGDPR legacyGDPR;
    GDPR gdpr;

    public AdsManager(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
        this.adsPref = new AdsPref(activity);
        this.legacyGDPR = new LegacyGDPR(activity);
        this.gdpr = new GDPR(activity);
        adNetwork = new AdNetwork.Initialize(activity);
        bannerAd = new BannerAd.Builder(activity);
        interstitialAd = new InterstitialAd.Builder(activity);
        nativeAd = new NativeAd.Builder(activity);
        nativeAdView = new NativeAdFragment.Builder(activity);
    }

    public void initializeAd() {
        adNetwork.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobAppId(null)
                .setStartappAppId(adsPref.getStartappAppID())
                .setUnityGameId(adsPref.getUnityGameId())
                .setAppLovinSdkKey(null)
                .setMopubBannerId(adsPref.getMopubBannerAdUnitId())
                .setDebug(BuildConfig.DEBUG)
                .build();
    }

    public void loadBannerAd(int placement) {
        bannerAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobBannerId(adsPref.getAdMobBannerId())
                .setUnityBannerId(adsPref.getUnityBannerPlacementId())
                .setAppLovinBannerId(adsPref.getAppLovinBannerAdUnitId())
                .setAppLovinBannerZoneId(adsPref.getAppLovinBannerZoneId())
                .setMopubBannerId(adsPref.getMopubBannerAdUnitId())
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setPlacementStatus(placement)
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .build();
    }

    public void loadInterstitialAd(int placement, int interval) {
        interstitialAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobInterstitialId(adsPref.getAdMobInterstitialId())
                .setUnityInterstitialId(adsPref.getUnityInterstitialPlacementId())
                .setAppLovinInterstitialId(adsPref.getAppLovinInterstitialAdUnitId())
                .setAppLovinInterstitialZoneId(adsPref.getAppLovinInterstitialZoneId())
                .setMopubInterstitialId(adsPref.getMopubInterstitialAdUnitId())
                .setInterval(interval)
                .setPlacementStatus(placement)
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .build();
    }

    public void loadNativeAd(int placement) {
        nativeAd.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobNativeId(adsPref.getAdMobNativeId())
                .setAppLovinNativeId(adsPref.getAppLovinNativeAdManualUnitId())
                .setPlacementStatus(placement)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .build();
    }

    public void loadNativeAdView(View view, int placement) {
        nativeAdView.setAdStatus(adsPref.getAdStatus())
                .setAdNetwork(adsPref.getAdType())
                .setBackupAdNetwork(adsPref.getBackupAds())
                .setAdMobNativeId(adsPref.getAdMobNativeId())
                .setAppLovinNativeId(adsPref.getAppLovinNativeAdManualUnitId())
                .setPlacementStatus(placement)
                .setDarkTheme(sharedPref.getIsDarkTheme())
                .setLegacyGDPR(USE_LEGACY_GDPR_EU_CONSENT)
                .setView(view)
                .build();
    }

    public void showInterstitialAd() {
        interstitialAd.show();
    }

    public void updateConsentStatus() {
        if (USE_LEGACY_GDPR_EU_CONSENT) {
            legacyGDPR.updateLegacyGDPRConsentStatus(adsPref.getAdMobPublisherId(), sharedPref.getBaseUrl() + "/privacy.php");
        } else {
            gdpr.updateGDPRConsentStatus();
        }
    }

}
