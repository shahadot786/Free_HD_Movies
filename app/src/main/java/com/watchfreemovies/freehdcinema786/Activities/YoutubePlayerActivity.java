package com.watchfreemovies.freehdcinema786.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.ads.MaxAdView;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class YoutubePlayerActivity extends AppCompatActivity {

    String trailerLink, mName;
    Intent intent;
    YouTubePlayerView youTubePlayerView;
    ProgressDialog dialog;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //find id
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(youTubePlayerView);

        //get intent data from previous activity
        intent = getIntent();
        trailerLink = intent.getStringExtra("trailerLink");
        mName = intent.getStringExtra("movieName");

        //progress dialog
        dialog = new ProgressDialog(YoutubePlayerActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(mName);
        dialog.setMessage("Please wait while loading...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                dialog.dismiss();
                String videoId = trailerLink;
                youTubePlayer.loadVideo(videoId, 0);
                youTubePlayer.play();
            }
        });
        youTubePlayerView.enterFullScreen();
        youTubePlayerView.exitFullScreen();
        youTubePlayerView.isFullScreen();
        youTubePlayerView.toggleFullScreen();

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
    }

    @Override
    public void onBackPressed() {
        youTubePlayerView.release();
        super.onBackPressed();
    }
}