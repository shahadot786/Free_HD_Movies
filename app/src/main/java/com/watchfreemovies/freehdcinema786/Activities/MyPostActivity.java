package com.watchfreemovies.freehdcinema786.Activities;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applovin.mediation.ads.MaxAdView;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Adapter.FeedsAdapter;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.FeedsModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.databinding.ActivityMyPostBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class MyPostActivity extends AppCompatActivity {

    ActivityMyPostBinding binding;
    ShimmerRecyclerView recyclerView;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<FeedsModel> feedsList;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView toastText;
    View toastLayout;
    Toast toast;
    LayoutInflater inflater;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //toolbar
        setSupportActionBar(binding.toolbar2);
        MyPostActivity.this.setTitle("My Posts");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //ads init
        adNetwork = new AdNetwork(this);
        //adNetwork.loadBannerAd();
        //banner
        MaxAdView bannerAd = findViewById(R.id.adView);
        LinearLayout unityBannerAd = findViewById(R.id.banner_ad);
        //adNetwork.loadBannerAd();
        adNetwork.loadUnityBannerAd();
        //check premium
        if (UiConfig.BANNER_AD_VISIBILITY) {
            bannerAd.setVisibility(View.VISIBLE);
            bannerAd.startAutoRefresh();
            unityBannerAd.setVisibility(View.VISIBLE);
        } else {
            bannerAd.setVisibility(View.GONE);
            bannerAd.stopAutoRefresh();
            unityBannerAd.setVisibility(View.GONE);
        }

        //firebase instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //recyclerview
        recyclerView = findViewById(R.id.rv_movie);
        recyclerView.showShimmerAdapter();
        feedsList = new ArrayList<>();

        //instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //set Adapter and layout
        FeedsAdapter adapter = new FeedsAdapter(feedsList, this);

        StaggeredGridLayoutManager feedsLayout = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        feedsLayout.setReverseLayout(false);
        recyclerView.setLayoutManager(feedsLayout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MyPostActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            toastText.setText(R.string.network_connected);
        } else {
            toastText.setText(R.string.no_connection_text);
            toast.show();
        }

        database.getReference().child("Feeds")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        feedsList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            FeedsModel model = dataSnapshot.getValue(FeedsModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(feedsList);
                            String posted = FirebaseAuth.getInstance().getUid();
                            String postedBy = model.getPostedBy();
                            assert posted != null;
                            if (posted.equals(postedBy)) {
                                feedsList.add(model);
                            }

                        }
                        recyclerView.setAdapter(adapter);
                        recyclerView.hideShimmerAdapter();
                        adapter.notifyDataSetChanged();

                        //check if list is empty
                        if (feedsList.isEmpty()) {
                            binding.emptyList.setVisibility(View.VISIBLE);
                            binding.emptyListText.setVisibility(View.VISIBLE);
                            binding.empty.setVisibility(View.VISIBLE);
                        } else {
                            binding.emptyList.setVisibility(View.GONE);
                            binding.emptyListText.setVisibility(View.GONE);
                            binding.empty.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //find swipe refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshDiscuss);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRefresh() {
                recyclerView.showShimmerAdapter();
                recyclerView.setLayoutManager(feedsLayout);
                database.getReference().child("Feeds")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                feedsList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    FeedsModel model = dataSnapshot.getValue(FeedsModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(feedsList);
                                    String posted = FirebaseAuth.getInstance().getUid();
                                    String postedBy = model.getPostedBy();
                                    assert posted != null;
                                    if (posted.equals(postedBy)) {
                                        feedsList.add(model);
                                    }
                                }
                                recyclerView.setAdapter(adapter);
                                recyclerView.hideShimmerAdapter();
                                adapter.notifyDataSetChanged();

                                //check if list is empty
                                if (feedsList.isEmpty()) {
                                    binding.emptyList.setVisibility(View.VISIBLE);
                                    binding.emptyListText.setVisibility(View.VISIBLE);
                                    binding.empty.setVisibility(View.VISIBLE);
                                } else {
                                    binding.emptyList.setVisibility(View.GONE);
                                    binding.emptyListText.setVisibility(View.GONE);
                                    binding.empty.setVisibility(View.GONE);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
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