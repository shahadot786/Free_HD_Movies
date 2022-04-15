package com.watchfreemovies.freehdcinema786.Activities;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.databinding.ActivityFollowUsBinding;

import java.util.Calendar;
import java.util.Objects;

public class FollowUsActivity extends AppCompatActivity {
    ActivityFollowUsBinding binding;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    Activity activity;
    String instagram,facebook,twitter, linkedIn,gitHub,playStore,webSite;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFollowUsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = FollowUsActivity.this;

        instagram = getResources().getString(R.string.link_instagram);
        facebook = getResources().getString(R.string.link_facebook);
        twitter = getResources().getString(R.string.link_twitter);
        linkedIn = getResources().getString(R.string.link_linkedin);
        gitHub = getResources().getString(R.string.link_github);
        playStore = getResources().getString(R.string.url_play_more_apps);
        webSite = getResources().getString(R.string.play_website);
        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        //pro status
        ImageView proBadge = findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }
        //toolbar
        setSupportActionBar(binding.toolbar3);
        FollowUsActivity.this.setTitle("Follow Us");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //work with version
        binding.fwVersion.setText(BuildConfig.VERSION_NAME);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        binding.fwCopyrights.setText("© " + year + " All Rights Reserved");

        //work with link
        binding.viewInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent = new Intent(activity, WebviewActivity.class);
                    intent.putExtra("IMDbLink", instagram);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    noConnectionDialog();
                }
            }
        });
        binding.viewFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent = new Intent(activity, WebviewActivity.class);
                    intent.putExtra("IMDbLink", facebook);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    noConnectionDialog();
                }
            }
        });
        binding.viewLinkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent = new Intent(activity, WebviewActivity.class);
                    intent.putExtra("IMDbLink", linkedIn);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    noConnectionDialog();
                }
            }
        });
        binding.viewTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent = new Intent(activity, WebviewActivity.class);
                    intent.putExtra("IMDbLink", twitter);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    noConnectionDialog();
                }
            }
        });
        binding.viewPlayStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_play_more_apps))));
                } else {
                    noConnectionDialog();
                }
            }
        });
        binding.viewGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent = new Intent(activity, WebviewActivity.class);
                    intent.putExtra("IMDbLink", gitHub);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    noConnectionDialog();
                }
            }
        });
        binding.viewWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent = new Intent(activity, WebviewActivity.class);
                    intent.putExtra("IMDbLink", webSite);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    noConnectionDialog();
                }
            }
        });
        binding.contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FollowUsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + "info@shrcreation.com")));
                } else {
                    noConnectionDialog();
                }
            }
        });
    }

    //no internet connection dialog
    private void noConnectionDialog() {
        //custom dialog
        Dialog noConnection;
        TextView btnClose;
        noConnection = new Dialog(this);
        noConnection.setContentView(R.layout.custom_no_connections_layout);
        noConnection.setCancelable(false);
        noConnection.setCanceledOnTouchOutside(false);
        noConnection.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        noConnection.show();
        btnClose = noConnection.findViewById(R.id.closeBtn);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noConnection.dismiss();
            }
        });
    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}