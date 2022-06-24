package com.watchfreemovies.freehdcinema786.Activities;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
import com.watchfreemovies.freehdcinema786.Utils.NetworkChecks;
import com.watchfreemovies.freehdcinema786.databinding.ActivitySearchBinding;

import java.util.ArrayList;
import java.util.Collections;

public class SearchActivity extends AppCompatActivity {

    ActivitySearchBinding binding;
    ShimmerRecyclerView searchRv;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<MoviesModel> moviesList;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView toastText;
    View toastLayout;
    Toast toast;
    View empty;
    TextView emptyText, emptySubText;
    LayoutInflater inflater;
    SearchView searchView;
    MoviesAdapter moviesAdapter;
    StaggeredGridLayoutManager gridLayoutManager;
    int mSpanCount = 3;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //empty view
        empty = findViewById(R.id.emptyList);
        emptyText = findViewById(R.id.empty);
        emptySubText = findViewById(R.id.emptyListText);

        searchView = findViewById(R.id.search_view);

        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //ads init
        adNetwork = new AdNetwork(this);
        NetworkChecks networkChecks = new NetworkChecks(this);
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

        //firebase instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //recyclerview
        searchRv = findViewById(R.id.rv_movie);
        searchRv.showShimmerAdapter();
        moviesList = new ArrayList<>();

        moviesAdapter = new MoviesAdapter(moviesList, this);
        //reverse layout
        gridLayoutManager = new StaggeredGridLayoutManager(mSpanCount, LinearLayoutManager.VERTICAL);
        gridLayoutManager.setReverseLayout(false);
        //set value to recyclerView
        searchRv.setLayoutManager(gridLayoutManager);
        searchRv.setHasFixedSize(true);
        searchRv.setNestedScrollingEnabled(false);

        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CategoryDetailsActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
        } else {
            networkChecks.noConnectionDialog();
        }

        database.getReference().child("Movies")
                .limitToFirst(210)
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
                            moviesList.add(model);
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

                        searchRv.setAdapter(moviesAdapter);
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
                searchRv.showShimmerAdapter();
                searchRv.setLayoutManager(gridLayoutManager);
                database.getReference().child("Movies")
                        .limitToLast(210)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                moviesList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    Collections.shuffle(moviesList);
                                    model.setPostId(dataSnapshot.getKey());
                                    moviesList.add(model);
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

                                searchRv.setAdapter(moviesAdapter);
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



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return true;
            }
        });

        searchView.clearFocus();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }//ends of onCreate

    private void search(String str) {
        ArrayList<MoviesModel> myList = new ArrayList<>();
        for (MoviesModel object : moviesList) {
            if (object.getMovieName().toLowerCase().contains(str.toLowerCase())) {
                myList.add(object);
            } else if (object.getDescription().toLowerCase().contains(str.toLowerCase())) {
                myList.add(object);
            } else if (object.getMovieYear().toLowerCase().contains(str.toLowerCase())) {
                myList.add(object);
            } else if (object.getGenre().toLowerCase().contains(str.toLowerCase())) {
                myList.add(object);
            } else if (object.getGenre1().toLowerCase().contains(str.toLowerCase())) {
                myList.add(object);
            } else if (object.getGenre2().toLowerCase().contains(str.toLowerCase())) {
                myList.add(object);
            } else if (object.getKeywords().toLowerCase().contains(str.toLowerCase())) {
                myList.add(object);
            }
        }

        MoviesAdapter moviesAdapter = new MoviesAdapter(myList, SearchActivity.this);
        searchRv.setAdapter(moviesAdapter);

        if (myList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptySubText.setVisibility(View.VISIBLE);
            empty.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
            emptySubText.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
        }
    }
}