package com.watchfreemovies.freehdcinema786.Activities;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.watchfreemovies.freehdcinema786.Adapter.MoviesAdapter;
import com.watchfreemovies.freehdcinema786.Categories.CategoryDetailsActivity;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.MoviesModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.databinding.ActivityFavoriteListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class FavoriteListActivity extends AppCompatActivity {

    ActivityFavoriteListBinding binding;
    ShimmerRecyclerView recyclerView;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<MoviesModel> moviesList;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView toastText;
    View toastLayout;
    Toast toast;
    LayoutInflater inflater;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavoriteListBinding.inflate(getLayoutInflater());
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
        FavoriteListActivity.this.setTitle("Favourites");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAd();
        adNetwork.loadInterstitialAd();
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

        //firebase instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //recyclerview
        recyclerView = findViewById(R.id.rv_movie);
        recyclerView.showShimmerAdapter();
        moviesList = new ArrayList<>();


        MoviesAdapter moviesAdapter = new MoviesAdapter(moviesList, this);
        //reverse layout
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3,LinearLayoutManager.VERTICAL);
        gridLayoutManager.setReverseLayout(false);
        //set value to recyclerView
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CategoryDetailsActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
        } else {
            toastText.setText(R.string.no_connection_text);
            toast.show();
        }

        database.getReference().child("Movies")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        moviesList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(moviesList);
                            String currentUser = FirebaseAuth.getInstance().getUid();
                            String saved = model.getSaved();
                            assert currentUser != null;
                            if (currentUser.equals(saved)){
                                moviesList.add(model);
                            }

                        }

                        //check if list is empty
                        if (moviesList.isEmpty()){
                            binding.emptyList.setVisibility(View.VISIBLE);
                            binding.emptyListText.setVisibility(View.VISIBLE);
                            binding.empty.setVisibility(View.VISIBLE);
                        }else {
                            binding.emptyList.setVisibility(View.GONE);
                            binding.emptyListText.setVisibility(View.GONE);
                            binding.empty.setVisibility(View.GONE);
                        }

                        recyclerView.setAdapter(moviesAdapter);
                        recyclerView.hideShimmerAdapter();
                        moviesAdapter.notifyDataSetChanged();

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
                database.getReference().child("Movies")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                moviesList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(moviesList);
                                    Collections.reverse(moviesList);
                                    String currentUser = FirebaseAuth.getInstance().getUid();
                                    String saved = model.getSaved();
                                    assert currentUser != null;
                                    if (currentUser.equals(saved)){
                                        moviesList.add(model);
                                    }
                                }

                                //check if list is empty
                                if (moviesList.isEmpty()){
                                    binding.emptyList.setVisibility(View.VISIBLE);
                                    binding.emptyListText.setVisibility(View.VISIBLE);
                                    binding.empty.setVisibility(View.VISIBLE);
                                }else {
                                    binding.emptyList.setVisibility(View.GONE);
                                    binding.emptyListText.setVisibility(View.GONE);
                                    binding.empty.setVisibility(View.GONE);
                                }

                                recyclerView.setAdapter(moviesAdapter);
                                moviesAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                moviesAdapter.notifyDataSetChanged();
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