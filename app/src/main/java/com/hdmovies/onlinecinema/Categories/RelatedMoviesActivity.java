package com.hdmovies.onlinecinema.Categories;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.hdmovies.onlinecinema.Adapter.MoviesAdapter;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.Model.MoviesModel;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.AdNetwork;
import com.hdmovies.onlinecinema.Utils.NetworkChecks;
import com.hdmovies.onlinecinema.databinding.ActivityRelatedMoviesBinding;

import java.util.ArrayList;
import java.util.Collections;

public class RelatedMoviesActivity extends AppCompatActivity {

    ActivityRelatedMoviesBinding binding;
    ShimmerRecyclerView recyclerView;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<MoviesModel> moviesList;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView toastText;
    View toastLayout;
    Toast toast;
    LayoutInflater inflater;
    View empty;
    TextView emptyText,emptySubText;
    Intent intent;
    String name, year, genre,genre1,genre2,keywords;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRelatedMoviesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //empty view
        empty = findViewById(R.id.emptyList);
        emptyText = findViewById(R.id.empty);
        emptySubText = findViewById(R.id.emptyListText);

        //toolbar
        setSupportActionBar(binding.toolbar2);
        RelatedMoviesActivity.this.setTitle("Related Movies");

        // Start loading ads here...
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

        //get intent data from movies details activity
        intent = getIntent();
        name = intent.getStringExtra("movieName");
        year = intent.getStringExtra("year");
        genre = intent.getStringExtra("genre");
        genre1 = intent.getStringExtra("genre1");
        genre2 = intent.getStringExtra("genre2");
        keywords = intent.getStringExtra("keywords");

        //firebase instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //recyclerview
        recyclerView = findViewById(R.id.rv_movie);
        recyclerView.showShimmerAdapter();

        moviesList = new ArrayList<>();
        MoviesAdapter moviesAdapter = new MoviesAdapter(moviesList, this);
        //reverse layout
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL);
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
            networkChecks.noConnectionDialog();
        }

        database.getReference().child("Movies")
                .limitToLast(300)
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
                            //set data to list
                            String names = model.getMovieName();
                            String genres = model.getGenre();
                            String genres1 = model.getGenre1();
                            String genres2 = model.getGenre2();

                            //year/genres
                            if (genre.equals(genres)){
                                if (!name.equals(names)){
                                    moviesList.add(model);
                                }
                            }else if (genre1.equals(genres1)){
                                if (!name.equals(names)){
                                    moviesList.add(model);
                                }
                            }else if (genre2.equals(genres2)){
                                if (!name.equals(names)){
                                    moviesList.add(model);
                                }
                            }

                        }
                        recyclerView.setAdapter(moviesAdapter);
                        recyclerView.hideShimmerAdapter();
                        moviesAdapter.notifyDataSetChanged();

                        //check if list is empty
                        if (moviesList.isEmpty()){
                            emptyText.setVisibility(View.VISIBLE);
                            emptySubText.setVisibility(View.VISIBLE);
                            empty.setVisibility(View.VISIBLE);
                        }else {
                            emptyText.setVisibility(View.GONE);
                            emptySubText.setVisibility(View.GONE);
                            empty.setVisibility(View.GONE);
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
                database.getReference().child("Movies")
                        .limitToLast(300)
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
                                    //set data to list
                                    String names = model.getMovieName();
                                    String genres = model.getGenre();
                                    String genres1 = model.getGenre1();
                                    String genres2 = model.getGenre2();

                                    //year/genres
                                    if (genre.equals(genres)){
                                        if (!name.equals(names)){
                                            moviesList.add(model);
                                        }
                                    }else if (genre1.equals(genres1)){
                                        if (!name.equals(names)){
                                            moviesList.add(model);
                                        }
                                    }else if (genre2.equals(genres2)){
                                        if (!name.equals(names)){
                                            moviesList.add(model);
                                        }
                                    }

                                }
                                recyclerView.setAdapter(moviesAdapter);
                                recyclerView.hideShimmerAdapter();
                                moviesAdapter.notifyDataSetChanged();

                                //check if list is empty
                                if (moviesList.isEmpty()){
                                    emptyText.setVisibility(View.VISIBLE);
                                    emptySubText.setVisibility(View.VISIBLE);
                                    empty.setVisibility(View.VISIBLE);
                                }else {
                                    emptyText.setVisibility(View.GONE);
                                    emptySubText.setVisibility(View.GONE);
                                    empty.setVisibility(View.GONE);
                                }

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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //for search menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem search = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint("Search anything...");
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
        return super.onCreateOptionsMenu(menu);
    }

    private void search(String str) {

        ArrayList<MoviesModel> myList = new ArrayList<>();
        for (MoviesModel object : moviesList){
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

        MoviesAdapter adapter = new MoviesAdapter(myList,RelatedMoviesActivity.this);
        recyclerView.setAdapter(adapter);

        if (myList.isEmpty()){
            emptyText.setVisibility(View.VISIBLE);
            emptySubText.setVisibility(View.VISIBLE);
            empty.setVisibility(View.VISIBLE);
        }else {
            emptyText.setVisibility(View.GONE);
            emptySubText.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
        }

    }
    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}