package com.hdmovies.onlinecinema.Activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.applovin.mediation.ads.MaxAdView;
import com.google.android.material.snackbar.Snackbar;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.AdNetwork;
import com.hdmovies.onlinecinema.databinding.ActivityVideoDownloadBinding;

import java.io.File;
import java.util.Objects;

public class ActivityVideoDownload extends AppCompatActivity {

    ActivityVideoDownloadBinding binding;
    public static final int PERMISSIONS_REQUEST = 102;
    String downloadUrl;
    String movieName;
    Activity activity;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoDownloadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        downloadUrl = getIntent().getStringExtra("downloadUrl");
        movieName = getIntent().getStringExtra("movieName");

        //toolbar
        setSupportActionBar(binding.toolbar2);
        ActivityVideoDownload.this.setTitle(movieName);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        activity = ActivityVideoDownload.this;
        adNetwork = new AdNetwork(this);
        Button download=findViewById(R.id.btnDownload);
        //ad initialization
        MaxAdView mRecAd = findViewById(R.id.mRec);
        adNetwork.loadMrecAd();
        //ads disabled code
        //check premium
        if (UiConfig.BANNER_AD_VISIBILITY) {
            mRecAd.setVisibility(View.VISIBLE);
            mRecAd.startAutoRefresh();
        } else {
            mRecAd.setVisibility(View.GONE);
            mRecAd.stopAutoRefresh();
        }

        //btn download click
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });


    }//ends of onCreate

    public void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(ActivityVideoDownload.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                downloadMovies();
            }
        } else {
            downloadMovies();
        }
    }

    public void downloadMovies() {
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrl);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setTitle(movieName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedOverRoaming(true)
                    .setAllowedOverMetered(true)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, File.separator + movieName + ".mp4");
            dm.enqueue(request);
            Snackbar.make(activity.findViewById(android.R.id.content), "Movie download started.", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Snackbar.make(activity.findViewById(android.R.id.content), "Movie download failed.", Snackbar.LENGTH_SHORT).show();
        }
    }


    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}