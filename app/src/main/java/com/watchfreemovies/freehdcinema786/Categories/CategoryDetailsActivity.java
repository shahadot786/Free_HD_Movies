package com.watchfreemovies.freehdcinema786.Categories;

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
import com.watchfreemovies.freehdcinema786.Adapter.MoviesAdapter;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.MoviesModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.databinding.ActivityCategoryDetailsBinding;

import java.util.ArrayList;
import java.util.Collections;

public class CategoryDetailsActivity extends AppCompatActivity {

    ActivityCategoryDetailsBinding binding;
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
    String categoriesName;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryDetailsBinding.inflate(getLayoutInflater());
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

        //get intent data
        intent = getIntent();
        categoriesName = intent.getStringExtra("category");

        //toolbar
        setSupportActionBar(binding.toolbar2);
        CategoryDetailsActivity.this.setTitle("Genre: "+categoriesName);

        // Start loading ads here...
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

        //set categories value
        if (categoriesName.equals("Action")){

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
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Action")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Action")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Action")){
                                    moviesList.add(model);
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

        }else if (categoriesName.equals("TV")){
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
                                String type = model.getType();
                                Collections.shuffle(moviesList);
                                if (type.equals("TV")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2022")){
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
                                String year = model.getMovieYear();
                                if (year.contains("2022")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Thriller")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Thriller")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Thriller")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Thriller")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Romance")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Romance")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Romance")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Romance")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Drama")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Drama")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Drama")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Drama")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Adventure")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Adventure")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Adventure")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Adventure")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Korean")){
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
                                //get genres data
                                String keywords = model.getKeywords();
                                if (keywords.contains("Korean")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Chinese")){
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
                                //get genres data
                                String keywords = model.getKeywords();
                                if (keywords.contains("Chinese")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Horror")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Horror")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Horror")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Horror")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Comedy")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Comedy")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Comedy")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Comedy")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Crime")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Crime")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Crime")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Crime")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Fantasy")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Fantasy")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Fantasy")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Fantasy")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("War")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("War")){
                                    moviesList.add(model);
                                }else if (genre1.contains("War")){
                                    moviesList.add(model);
                                }else if (genre2.contains("War")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Sci-Fi")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.equals("Sci-Fi")){
                                    moviesList.add(model);
                                }else if (genre1.equals("Sci-Fi")){
                                    moviesList.add(model);
                                }else if (genre2.equals("Sci-Fi")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Family")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Family")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Family")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Family")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Animation")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.equals("Animation")){
                                    moviesList.add(model);
                                }else if (genre1.equals("Animation")){
                                    moviesList.add(model);
                                }else if (genre2.equals("Animation")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("History")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("History")){
                                    moviesList.add(model);
                                }else if (genre1.contains("History")){
                                    moviesList.add(model);
                                }else if (genre2.contains("History")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Music")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Music")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Music")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Music")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Documentary")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Documentary")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Documentary")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Documentary")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Biography")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Biography")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Biography")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Biography")){
                                    moviesList.add(model);
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

        }else if (categoriesName.equals("Western")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Western")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Western")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Western")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Mystery")){
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
                                //get genres data
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                if (genre.contains("Mystery")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Mystery")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Mystery")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2021")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2021")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Old Is Gold")){
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
                                String year = model.getMovieYear();
                                if (year.contains("1971")){
                                    moviesList.add(model);
                                }else if (year.contains("1972")){
                                    moviesList.add(model);
                                }else if (year.contains("1974")){
                                    moviesList.add(model);
                                }else if (year.contains("1978")){
                                    moviesList.add(model);
                                }else if (year.contains("1990")){
                                    moviesList.add(model);
                                }else if (year.contains("1994")){
                                    moviesList.add(model);
                                }else if (year.contains("1996")){
                                    moviesList.add(model);
                                }else if (year.contains("1999")){
                                    moviesList.add(model);
                                }else if (year.contains("1989")){
                                    moviesList.add(model);
                                }else if (year.contains("1997")){
                                    moviesList.add(model);
                                }else if (year.contains("1998")){
                                    moviesList.add(model);
                                }else if (year.contains("1953")){
                                    moviesList.add(model);
                                }else if (year.contains("1988")){
                                    moviesList.add(model);
                                }else if (year.contains("1940")){
                                    moviesList.add(model);
                                }else if (year.contains("1985")){
                                    moviesList.add(model);
                                }else if (year.contains("1986")){
                                    moviesList.add(model);
                                }else if (year.contains("1987")){
                                    moviesList.add(model);
                                }else if (year.contains("1980")){
                                    moviesList.add(model);
                                }else if (year.contains("1968")){
                                    moviesList.add(model);
                                }else if (year.contains("1995")){
                                    moviesList.add(model);
                                }else if (year.contains("2000")){
                                    moviesList.add(model);
                                }else if (year.contains("2001")){
                                    moviesList.add(model);
                                }else if (year.contains("2002")){
                                    moviesList.add(model);
                                }else if (year.contains("2003")){
                                    moviesList.add(model);
                                }else if (year.contains("2004")){
                                    moviesList.add(model);
                                }else if (year.contains("2005")){
                                    moviesList.add(model);
                                }else if (year.contains("2006")){
                                    moviesList.add(model);
                                }else if (year.contains("2006")){
                                    moviesList.add(model);
                                }else if (year.contains("2007")){
                                    moviesList.add(model);
                                }else if (year.contains("2008")){
                                    moviesList.add(model);
                                }else if (year.contains("2009")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("More")){
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
                                //get genres data
                                String year = model.getMovieYear();
                                String genre = model.getGenre();
                                String genre1 = model.getGenre1();
                                String genre2 = model.getGenre2();
                                String keywords = model.getKeywords();
                                if (genre.contains("Cartoon")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Cartoon")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Cartoon")){
                                    moviesList.add(model);
                                }else if (genre.contains("AI")){
                                    moviesList.add(model);
                                }else if (genre1.contains("AI")){
                                    moviesList.add(model);
                                }else if (genre2.contains("AI")){
                                    moviesList.add(model);
                                }else if (genre.contains("Motivational")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Motivational")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Motivational")){
                                    moviesList.add(model);
                                }else if (genre.contains("Spy")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Spy")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Spy")){
                                    moviesList.add(model);
                                }else if (genre.contains("Robots")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Robots")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Robots")){
                                    moviesList.add(model);
                                }else if (genre.contains("Sports")){
                                    moviesList.add(model);
                                }else if (genre1.contains("Sports")){
                                    moviesList.add(model);
                                }else if (genre2.contains("Sports")){
                                    moviesList.add(model);
                                }else if (keywords.contains("Cartoon")){
                                    moviesList.add(model);
                                }else if (keywords.contains("AI")){
                                    moviesList.add(model);
                                }else if (keywords.contains("Motivational")){
                                    moviesList.add(model);
                                }else if (keywords.contains("Spy")){
                                    moviesList.add(model);
                                }else if (keywords.contains("Robots")){
                                    moviesList.add(model);
                                }else if (keywords.contains("Sports")){
                                    moviesList.add(model);
                                }else if (keywords.contains("Musical")){
                                    moviesList.add(model);
                                }else if (keywords.contains("Artificial Intelligence")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("All Time Hits")){
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
                                String evergreenMovies = model.getKeywords();
                                if (evergreenMovies.contains("Evergreen")) {
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Flex Favorites")){
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
                                String type = model.getType();
                                Collections.shuffle(moviesList);
                                if (type.equals("Flex")) {
                                    moviesList.add(model);
                                }

                            }
                            recyclerView.setAdapter(moviesAdapter);
                            recyclerView.hideShimmerAdapter();
                            moviesAdapter.notifyDataSetChanged();

                            //check if list is empty
                            if (moviesList.isEmpty()) {
                                emptyText.setVisibility(View.VISIBLE);
                                emptySubText.setVisibility(View.VISIBLE);
                                empty.setVisibility(View.VISIBLE);
                            } else {
                                emptyText.setVisibility(View.GONE);
                                emptySubText.setVisibility(View.GONE);
                                empty.setVisibility(View.GONE);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else if (categoriesName.equals("Top Rated")){
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
                                String topRated = model.getKeywords();
                                float rating = model.getRating();
                                if (rating >= 7.0) {
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("Latest")){
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
                                String latest = model.getKeywords();
                                float rating = model.getRating();
                                String year = model.getMovieYear();

                                if (latest.contains("Latest Updated")) {
                                    moviesList.add(model);
                                }else if (rating >=8.5){
                                    moviesList.add(model);
                                }else if (year.equals("2022")){
                                    moviesList.add(model);
                                }else if (year.equals("2021")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2020")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2020")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2019")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2019")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2018")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2018")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2017")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2017")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2016")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2016")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2015")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2015")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2014")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2014")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2013")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2013")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2012")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2012")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2011")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2011")){
                                    moviesList.add(model);
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
        }else if (categoriesName.equals("2010")){
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

                                String year = model.getMovieYear();
                                if (year.contains("2010")){
                                    moviesList.add(model);
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
        }



        //find swipe refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshDiscuss);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRefresh() {
                //set categories value
                if (categoriesName.equals("Action")){

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
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Action")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Action")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Action")){
                                            moviesList.add(model);
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

                }else if (categoriesName.equals("TV")){
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
                                        String type = model.getType();
                                        Collections.shuffle(moviesList);
                                        if (type.equals("TV")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2022")){
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
                                        String year = model.getMovieYear();
                                        if (year.contains("2022")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Thriller")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Thriller")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Thriller")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Thriller")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Romance")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Romance")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Romance")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Romance")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Drama")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Drama")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Drama")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Drama")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Adventure")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Adventure")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Adventure")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Adventure")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Korean")){
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
                                        //get genres data
                                        String keywords = model.getKeywords();
                                        if (keywords.contains("Korean")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Chinese")){
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
                                        //get genres data
                                        String keywords = model.getKeywords();
                                        if (keywords.contains("Chinese")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Horror")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Horror")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Horror")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Horror")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Comedy")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Comedy")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Comedy")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Comedy")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Crime")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Crime")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Crime")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Crime")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Fantasy")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Fantasy")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Fantasy")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Fantasy")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("War")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("War")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("War")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("War")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Sci-Fi")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.equals("Sci-Fi")){
                                            moviesList.add(model);
                                        }else if (genre1.equals("Sci-Fi")){
                                            moviesList.add(model);
                                        }else if (genre2.equals("Sci-Fi")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Family")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Family")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Family")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Family")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Animation")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.equals("Animation")){
                                            moviesList.add(model);
                                        }else if (genre1.equals("Animation")){
                                            moviesList.add(model);
                                        }else if (genre2.equals("Animation")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("History")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("History")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("History")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("History")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Music")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Music")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Music")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Music")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Documentary")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Documentary")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Documentary")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Documentary")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Biography")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Biography")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Biography")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Biography")){
                                            moviesList.add(model);
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

                }else if (categoriesName.equals("Western")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Western")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Western")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Western")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Mystery")){
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
                                        //get genres data
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        if (genre.contains("Mystery")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Mystery")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Mystery")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2021")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2021")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Old Is Gold")){
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
                                        String year = model.getMovieYear();
                                        if (year.contains("1971")){
                                            moviesList.add(model);
                                        }else if (year.contains("1972")){
                                            moviesList.add(model);
                                        }else if (year.contains("1974")){
                                            moviesList.add(model);
                                        }else if (year.contains("1978")){
                                            moviesList.add(model);
                                        }else if (year.contains("1990")){
                                            moviesList.add(model);
                                        }else if (year.contains("1994")){
                                            moviesList.add(model);
                                        }else if (year.contains("1996")){
                                            moviesList.add(model);
                                        }else if (year.contains("1999")){
                                            moviesList.add(model);
                                        }else if (year.contains("1989")){
                                            moviesList.add(model);
                                        }else if (year.contains("1997")){
                                            moviesList.add(model);
                                        }else if (year.contains("1998")){
                                            moviesList.add(model);
                                        }else if (year.contains("1953")){
                                            moviesList.add(model);
                                        }else if (year.contains("1988")){
                                            moviesList.add(model);
                                        }else if (year.contains("1940")){
                                            moviesList.add(model);
                                        }else if (year.contains("1985")){
                                            moviesList.add(model);
                                        }else if (year.contains("1986")){
                                            moviesList.add(model);
                                        }else if (year.contains("1987")){
                                            moviesList.add(model);
                                        }else if (year.contains("1980")){
                                            moviesList.add(model);
                                        }else if (year.contains("1968")){
                                            moviesList.add(model);
                                        }else if (year.contains("1995")){
                                            moviesList.add(model);
                                        }else if (year.contains("2000")){
                                            moviesList.add(model);
                                        }else if (year.contains("2001")){
                                            moviesList.add(model);
                                        }else if (year.contains("2002")){
                                            moviesList.add(model);
                                        }else if (year.contains("2003")){
                                            moviesList.add(model);
                                        }else if (year.contains("2004")){
                                            moviesList.add(model);
                                        }else if (year.contains("2005")){
                                            moviesList.add(model);
                                        }else if (year.contains("2006")){
                                            moviesList.add(model);
                                        }else if (year.contains("2006")){
                                            moviesList.add(model);
                                        }else if (year.contains("2007")){
                                            moviesList.add(model);
                                        }else if (year.contains("2008")){
                                            moviesList.add(model);
                                        }else if (year.contains("2009")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("More")){
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
                                        //get genres data
                                        String year = model.getMovieYear();
                                        String genre = model.getGenre();
                                        String genre1 = model.getGenre1();
                                        String genre2 = model.getGenre2();
                                        String keywords = model.getKeywords();
                                        if (genre.contains("Cartoon")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Cartoon")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Cartoon")){
                                            moviesList.add(model);
                                        }else if (genre.contains("AI")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("AI")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("AI")){
                                            moviesList.add(model);
                                        }else if (genre.contains("Motivational")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Motivational")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Motivational")){
                                            moviesList.add(model);
                                        }else if (genre.contains("Spy")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Spy")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Spy")){
                                            moviesList.add(model);
                                        }else if (genre.contains("Robots")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Robots")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Robots")){
                                            moviesList.add(model);
                                        }else if (genre.contains("Sports")){
                                            moviesList.add(model);
                                        }else if (genre1.contains("Sports")){
                                            moviesList.add(model);
                                        }else if (genre2.contains("Sports")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("Cartoon")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("AI")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("Motivational")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("Spy")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("Robots")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("Sports")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("Musical")){
                                            moviesList.add(model);
                                        }else if (keywords.contains("Artificial Intelligence")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("All Time Hits")){
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
                                        String evergreenMovies = model.getKeywords();
                                        if (evergreenMovies.contains("Evergreen")) {
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Flex Favorites")){
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
                                        String type = model.getType();
                                        Collections.shuffle(moviesList);
                                        if (type.equals("Flex")) {
                                            moviesList.add(model);
                                        }

                                    }
                                    recyclerView.setAdapter(moviesAdapter);
                                    recyclerView.hideShimmerAdapter();
                                    moviesAdapter.notifyDataSetChanged();

                                    //check if list is empty
                                    if (moviesList.isEmpty()) {
                                        emptyText.setVisibility(View.VISIBLE);
                                        emptySubText.setVisibility(View.VISIBLE);
                                        empty.setVisibility(View.VISIBLE);
                                    } else {
                                        emptyText.setVisibility(View.GONE);
                                        emptySubText.setVisibility(View.GONE);
                                        empty.setVisibility(View.GONE);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }else if (categoriesName.equals("Top Rated")){
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
                                        String topRated = model.getKeywords();
                                        float rating = model.getRating();
                                        if (rating >= 7.0) {
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("Latest")){
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
                                        String latest = model.getKeywords();
                                        float rating = model.getRating();
                                        String year = model.getMovieYear();

                                        if (latest.contains("Latest Updated")) {
                                            moviesList.add(model);
                                        }else if (rating >=8.5){
                                            moviesList.add(model);
                                        }else if (year.equals("2022")){
                                            moviesList.add(model);
                                        }else if (year.equals("2021")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2020")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2020")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2019")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2019")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2018")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2018")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2017")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2017")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2016")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2016")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2015")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2015")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2014")){
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

                                        String year = model.getMovieYear();
                                        if (year.contains("2014")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2013")){
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
                                        String year = model.getMovieYear();
                                        if (year.contains("2013")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2012")){
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
                                        String year = model.getMovieYear();
                                        if (year.contains("2012")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2011")){
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
                                        String year = model.getMovieYear();
                                        if (year.contains("2011")){
                                            moviesList.add(model);
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
                }else if (categoriesName.equals("2010")){
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
                                        String year = model.getMovieYear();
                                        if (year.contains("2010")){
                                            moviesList.add(model);
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
                }
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

        MoviesAdapter adapter = new MoviesAdapter(myList,CategoryDetailsActivity.this);
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