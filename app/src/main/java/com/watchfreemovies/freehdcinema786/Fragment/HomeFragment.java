package com.watchfreemovies.freehdcinema786.Fragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Activities.AddMoviesActivity;
import com.watchfreemovies.freehdcinema786.Activities.FeedBackActivity;
import com.watchfreemovies.freehdcinema786.Activities.SearchActivity;
import com.watchfreemovies.freehdcinema786.Activities.UserProfilesActivity;
import com.watchfreemovies.freehdcinema786.Adapter.MoviesAdapter;
import com.watchfreemovies.freehdcinema786.Categories.CategoryDetailsActivity;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.MoviesModel;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    //recycle view
    ShimmerRecyclerView evergreenRv;
    ShimmerRecyclerView actionRv;
    ShimmerRecyclerView topRatedRv;
    ShimmerRecyclerView latestRv;
    ShimmerRecyclerView sciFiRv;
    ShimmerRecyclerView horrorRv;
    ShimmerRecyclerView comedyRv;
    ShimmerRecyclerView crimeRv;
    ShimmerRecyclerView koreanRv;
    ShimmerRecyclerView adventureRv;
    ShimmerRecyclerView year2015Rv;
    ShimmerRecyclerView mysteryRv;
    ShimmerRecyclerView romanticRv;
    ShimmerRecyclerView thrillerRv;
    ShimmerRecyclerView animationRv;
    ShimmerRecyclerView tvSeriesRv;
    ShimmerRecyclerView flexFavRv;
    ShimmerRecyclerView chineseRv;
    //list
    ArrayList<MoviesModel> moviesList;
    ArrayList<MoviesModel> actionList;
    ArrayList<MoviesModel> topRatedList;
    ArrayList<MoviesModel> latestList;
    ArrayList<MoviesModel> sciFiList;
    ArrayList<MoviesModel> horrorList;
    ArrayList<MoviesModel> comedyList;
    ArrayList<MoviesModel> crimeList;
    ArrayList<MoviesModel> koreanList;
    ArrayList<MoviesModel> adventureList;
    ArrayList<MoviesModel> year2015List;
    ArrayList<MoviesModel> mysteryList;
    ArrayList<MoviesModel> romanticList;
    ArrayList<MoviesModel> thrillerList;
    ArrayList<MoviesModel> animationList;
    ArrayList<MoviesModel> tvSeriesList;
    ArrayList<MoviesModel> flexFavList;
    ArrayList<MoviesModel> chineseList;

    FirebaseAuth auth;
    FirebaseDatabase database;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView toastText;
    View toastLayout;
    Toast toast;
    TextView addMovies;
    View empty;
    TextView emptyText, emptySubText;
    TextView evergreenMore,actionMore,topRatedMore,latestMore,sciFiMore,horrorMore,tvSeriesMore,flexFavMore,chineseMore;
    TextView comedyMore,crimeMore,koreanMore,adventureMore,year2015More,mysteryMore,romanticMore,thrillerMore,animationMore;
    Dialog noConnection;
    TextView btnClose;
    private MaxNativeAdLoader nativeAdLoader;
    private MaxAd nativeAd;
    FrameLayout nativeAdContainer, nativeAdContainer2,nativeAdContainer3,nativeAdContainer4;
    private int retry;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        //find id
        addMovies = view.findViewById(R.id.addMovies);
        TextView userName = view.findViewById(R.id.userName);
        ImageView profileImage = view.findViewById(R.id.profileImage);

        nativeAdContainer = view.findViewById( R.id.native_ad_layout );
        nativeAdContainer3 = view.findViewById( R.id.native_ad_layout2 );
        nativeAdContainer2 = view.findViewById( R.id.native_medium_ad_layout );
        nativeAdContainer4 = view.findViewById( R.id.native_medium_ad_layout2 );

        loadNativeAd();
        loadMediumNativeAd();
        loadNativeAd2();
        loadMediumNativeAd2();
        //pro check
        if (UiConfig.BANNER_AD_VISIBILITY){
            nativeAdContainer.setVisibility(View.VISIBLE);
            nativeAdContainer2.setVisibility(View.VISIBLE);
            nativeAdContainer3.setVisibility(View.VISIBLE);
            nativeAdContainer4.setVisibility(View.VISIBLE);
        }else {
            nativeAdContainer.setVisibility(View.GONE);
            nativeAdContainer2.setVisibility(View.GONE);
            nativeAdContainer3.setVisibility(View.GONE);
            nativeAdContainer4.setVisibility(View.GONE);
        }

        //mores id
        evergreenMore = view.findViewById(R.id.evergreenMore);
        actionMore = view.findViewById(R.id.actionMore);
        topRatedMore = view.findViewById(R.id.topRatedMore);
        latestMore = view.findViewById(R.id.latestMore);
        sciFiMore = view.findViewById(R.id.sciFiMore);
        horrorMore = view.findViewById(R.id.horrorMore);
        comedyMore = view.findViewById(R.id.comedyMore);
        crimeMore = view.findViewById(R.id.crimeMore);
        koreanMore = view.findViewById(R.id.koreanMore);
        adventureMore = view.findViewById(R.id.adventureMore);
        year2015More = view.findViewById(R.id.year2015More);
        mysteryMore = view.findViewById(R.id.mysteryMore);
        romanticMore = view.findViewById(R.id.romanticMore);
        thrillerMore = view.findViewById(R.id.thrillerMore);
        animationMore = view.findViewById(R.id.animationMore);
        tvSeriesMore = view.findViewById(R.id.tvSeriesMore);
        flexFavMore = view.findViewById(R.id.flexFavMore);
        chineseMore = view.findViewById(R.id.chineseMore);

        //empty view
        empty = view.findViewById(R.id.emptyList);
        emptyText = view.findViewById(R.id.empty);
        emptySubText = view.findViewById(R.id.emptyListText);

        //custom toast
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, view.findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getActivity().getBaseContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        //custom dialog
        noConnection = new Dialog(getActivity());

        //pro status
        ImageView proBadge = view.findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }
        //Hide Add Movies
        if (UiConfig.HIDE_MOVIES_TV) {
            addMovies.setVisibility(View.VISIBLE);
        } else {
            addMovies.setVisibility(View.GONE);
        }
        //addMovies Click Listener
        addMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddMoviesActivity.class));
            }
        });

        //all recycle view and shimmer
        //evergreen
        evergreenRv = view.findViewById(R.id.rv_movie);
        evergreenRv.showShimmerAdapter();
        //action
        actionRv = view.findViewById(R.id.rv_movie_action);
        actionRv.showShimmerAdapter();
        //topRated
        topRatedRv = view.findViewById(R.id.rv_movie_topRated);
        topRatedRv.showShimmerAdapter();
        //latest
        latestRv = view.findViewById(R.id.rv_movie_latest);
        latestRv.showShimmerAdapter();
        //sciFi
        sciFiRv = view.findViewById(R.id.rv_movie_sciFi);
        sciFiRv.showShimmerAdapter();
        //horror
        horrorRv = view.findViewById(R.id.rv_movie_horror);
        horrorRv.showShimmerAdapter();
        //comedy
        comedyRv = view.findViewById(R.id.rv_movie_comedy);
        comedyRv.showShimmerAdapter();
        //crime
        crimeRv = view.findViewById(R.id.rv_movie_crime);
        crimeRv.showShimmerAdapter();
        //korean
        koreanRv = view.findViewById(R.id.rv_movie_korean);
        koreanRv.showShimmerAdapter();
        //adventure
        adventureRv = view.findViewById(R.id.rv_movie_adventure);
        adventureRv.showShimmerAdapter();
        //year2015
        year2015Rv = view.findViewById(R.id.rv_movie_year2015);
        year2015Rv.showShimmerAdapter();
        //mystery
        mysteryRv = view.findViewById(R.id.rv_movie_mystery);
        mysteryRv.showShimmerAdapter();
        //romantic
        romanticRv = view.findViewById(R.id.rv_movie_romantic);
        romanticRv.showShimmerAdapter();
        //thriller
        thrillerRv = view.findViewById(R.id.rv_movie_thriller);
        thrillerRv.showShimmerAdapter();
        //animation
        animationRv = view.findViewById(R.id.rv_movie_animation);
        animationRv.showShimmerAdapter();
        //tvSeries
        tvSeriesRv = view.findViewById(R.id.rv_movie_tvSeries);
        tvSeriesRv.showShimmerAdapter();
        //flexFav
        flexFavRv = view.findViewById(R.id.rv_movie_flexFav);
        flexFavRv.showShimmerAdapter();
        //chinese
        chineseRv = view.findViewById(R.id.rv_movie_chinese);
        chineseRv.showShimmerAdapter();

        //firebase instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel user = snapshot.getValue(UserModel.class);
                            assert user != null;
                            Picasso.get()
                                    .load(user.getProfile())
                                    .placeholder(R.drawable.ic_profile_default_image)
                                    .into(profileImage);
                            userName.setText(user.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //userProfileView click data
        View view1 = view.findViewById(R.id.userProfileView);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserProfilesActivity.class);
                intent.putExtra("postedBy", auth.getUid());
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //list
        moviesList = new ArrayList<>();
        actionList = new ArrayList<>();
        topRatedList = new ArrayList<>();
        latestList = new ArrayList<>();
        sciFiList = new ArrayList<>();
        horrorList = new ArrayList<>();
        comedyList = new ArrayList<>();
        crimeList = new ArrayList<>();
        koreanList = new ArrayList<>();
        adventureList = new ArrayList<>();
        year2015List = new ArrayList<>();
        mysteryList = new ArrayList<>();
        romanticList = new ArrayList<>();
        thrillerList = new ArrayList<>();
        animationList = new ArrayList<>();
        tvSeriesList = new ArrayList<>();
        flexFavList = new ArrayList<>();
        chineseList = new ArrayList<>();

        //adapter
        MoviesAdapter moviesAdapter = new MoviesAdapter(moviesList, getContext());
        MoviesAdapter actionAdapter = new MoviesAdapter(actionList, getContext());
        MoviesAdapter topRatedAdapter = new MoviesAdapter(topRatedList, getContext());
        MoviesAdapter latestAdapter = new MoviesAdapter(latestList, getContext());
        MoviesAdapter sciFiAdapter = new MoviesAdapter(sciFiList, getContext());
        MoviesAdapter horrorAdapter = new MoviesAdapter(horrorList, getContext());
        MoviesAdapter comedyAdapter = new MoviesAdapter(comedyList, getContext());
        MoviesAdapter crimeAdapter = new MoviesAdapter(crimeList, getContext());
        MoviesAdapter koreanAdapter = new MoviesAdapter(koreanList, getContext());
        MoviesAdapter adventureAdapter = new MoviesAdapter(adventureList, getContext());
        MoviesAdapter year2015Adapter = new MoviesAdapter(year2015List, getContext());
        MoviesAdapter mysteryAdapter = new MoviesAdapter(mysteryList, getContext());
        MoviesAdapter romanticAdapter = new MoviesAdapter(romanticList, getContext());
        MoviesAdapter thrillerAdapter = new MoviesAdapter(thrillerList, getContext());
        MoviesAdapter animationAdapter = new MoviesAdapter(animationList, getContext());
        MoviesAdapter tvSeriesAdapter = new MoviesAdapter(tvSeriesList, getContext());
        MoviesAdapter flexFavAdapter = new MoviesAdapter(flexFavList, getContext());
        MoviesAdapter chineseAdapter = new MoviesAdapter(chineseList, getContext());


        // StaggeredGridLayoutManager reverse layout
        //set value to evergreen
        StaggeredGridLayoutManager evergreen = new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL);
        evergreen.setReverseLayout(false);
        evergreenRv.setLayoutManager(evergreen);
        evergreenRv.setHasFixedSize(true);
        evergreenRv.setNestedScrollingEnabled(false);

        //set value to action
        StaggeredGridLayoutManager action = new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL);
        action.setReverseLayout(false);
        actionRv.setLayoutManager(action);
        actionRv.setHasFixedSize(true);
        actionRv.setNestedScrollingEnabled(false);

        //set value to topRated
        StaggeredGridLayoutManager TopRated = new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL);
        TopRated.setReverseLayout(false);
        topRatedRv.setLayoutManager(TopRated);
        topRatedRv.setHasFixedSize(true);
        topRatedRv.setNestedScrollingEnabled(false);

        //set value to latest
        StaggeredGridLayoutManager latest = new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL);
        latest.setReverseLayout(false);
        latestRv.setLayoutManager(latest);
        latestRv.setHasFixedSize(true);
        latestRv.setNestedScrollingEnabled(false);

        //set value to sciFi
        StaggeredGridLayoutManager sciFi = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        sciFi.setReverseLayout(false);
        sciFiRv.setLayoutManager(sciFi);
        sciFiRv.setHasFixedSize(true);
        sciFiRv.setNestedScrollingEnabled(false);

        //set value to horror
        StaggeredGridLayoutManager horror = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        horror.setReverseLayout(false);
        horrorRv.setLayoutManager(horror);
        horrorRv.setHasFixedSize(true);
        horrorRv.setNestedScrollingEnabled(false);

        //set value to comedy
        StaggeredGridLayoutManager comedy = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        comedy.setReverseLayout(false);
        comedyRv.setLayoutManager(comedy);
        comedyRv.setHasFixedSize(true);
        comedyRv.setNestedScrollingEnabled(false);

        //set value to crime
        StaggeredGridLayoutManager crime = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        crime.setReverseLayout(false);
        crimeRv.setLayoutManager(crime);
        crimeRv.setHasFixedSize(true);
        crimeRv.setNestedScrollingEnabled(false);

        //set value to korean
        StaggeredGridLayoutManager korean = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        korean.setReverseLayout(false);
        koreanRv.setLayoutManager(korean);
        koreanRv.setHasFixedSize(true);
        koreanRv.setNestedScrollingEnabled(false);

        //set value to adventure
        StaggeredGridLayoutManager adventure = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        adventure.setReverseLayout(false);
        adventureRv.setLayoutManager(adventure);
        adventureRv.setHasFixedSize(true);
        adventureRv.setNestedScrollingEnabled(false);

        //set value to year2015
        StaggeredGridLayoutManager year2015 = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        year2015.setReverseLayout(false);
        year2015Rv.setLayoutManager(year2015);
        year2015Rv.setHasFixedSize(true);
        year2015Rv.setNestedScrollingEnabled(false);

        //set value to mystery
        StaggeredGridLayoutManager mystery = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        mystery.setReverseLayout(false);
        mysteryRv.setLayoutManager(mystery);
        mysteryRv.setHasFixedSize(true);
        mysteryRv.setNestedScrollingEnabled(false);

        //set value to romantic
        StaggeredGridLayoutManager romantic = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        romantic.setReverseLayout(false);
        romanticRv.setLayoutManager(romantic);
        romanticRv.setHasFixedSize(true);
        romanticRv.setNestedScrollingEnabled(false);

        //set value to thriller
        StaggeredGridLayoutManager thriller = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        thriller.setReverseLayout(false);
        thrillerRv.setLayoutManager(thriller);
        thrillerRv.setHasFixedSize(true);
        thrillerRv.setNestedScrollingEnabled(false);

        //set value to animation
        StaggeredGridLayoutManager animation = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        animation.setReverseLayout(false);
        animationRv.setLayoutManager(animation);
        animationRv.setHasFixedSize(true);
        animationRv.setNestedScrollingEnabled(false);

        //set value to tvSeries
        StaggeredGridLayoutManager tvSeries = new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL);
        tvSeries.setReverseLayout(false);
        tvSeriesRv.setLayoutManager(tvSeries);
        tvSeriesRv.setHasFixedSize(true);
        tvSeriesRv.setNestedScrollingEnabled(false);

        //set value to flexFav
        StaggeredGridLayoutManager flexFav = new StaggeredGridLayoutManager(2, LinearLayoutManager.HORIZONTAL);
        flexFav.setReverseLayout(false);
        flexFavRv.setLayoutManager(flexFav);
        flexFavRv.setHasFixedSize(true);
        flexFavRv.setNestedScrollingEnabled(false);

        //set value to chinese
        StaggeredGridLayoutManager chinese = new StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);
        chinese.setReverseLayout(false);
        chineseRv.setLayoutManager(chinese);
        chineseRv.setHasFixedSize(true);
        chineseRv.setNestedScrollingEnabled(false);

        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(FeedBackActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
        } else {
            //showed dialog
            noConnectionDialog();
        }

        //for evergreen movies
        database.getReference().child("Movies")
                .limitToLast(120)
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
                        evergreenRv.setAdapter(moviesAdapter);
                        evergreenRv.hideShimmerAdapter();
                        moviesAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for action movies
        database.getReference().child("Movies")
                .limitToLast(120)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        actionList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(actionList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Action")) {
                                actionList.add(model);
                            } else if (genre1.contains("Action")) {
                                actionList.add(model);
                            } else if (genre2.contains("Action")) {
                                actionList.add(model);
                            }
                        }

                        actionRv.setAdapter(actionAdapter);
                        actionRv.hideShimmerAdapter();
                        actionAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //for topRated movies
        database.getReference().child("Movies")
                .limitToLast(210)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        topRatedList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(topRatedList);
                            float rating = model.getRating();
                            if (rating >= 7.0) {
                                topRatedList.add(model);
                            }

                        }

                        topRatedRv.setAdapter(topRatedAdapter);
                        topRatedRv.hideShimmerAdapter();
                        topRatedAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for latest movies
        database.getReference().child("Movies")
                .limitToLast(400)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        latestList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(latestList);
                            String latest = model.getKeywords();
                            float rating = model.getRating();
                            String year = model.getMovieYear();
                            if (latest.contains("Latest Updated")) {
                                latestList.add(model);
                            }else if (rating >=8.5){
                                latestList.add(model);
                            }else if (year.equals("2022")){
                                latestList.add(model);
                            }else if (year.equals("New Movies")){
                                latestList.add(model);
                            }else if (year.equals("2021")){
                                latestList.add(model);
                            }

                        }

                        latestRv.setAdapter(latestAdapter);
                        latestRv.hideShimmerAdapter();
                        latestAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for sciFi movies
        database.getReference().child("Movies")
                .limitToLast(300)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sciFiList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(sciFiList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Sci-Fi")) {
                                sciFiList.add(model);
                            } else if (genre1.contains("Sci-Fi")) {
                                sciFiList.add(model);
                            } else if (genre2.contains("Sci-Fi")) {
                                sciFiList.add(model);
                            }
                        }

                        sciFiRv.setAdapter(sciFiAdapter);
                        sciFiRv.hideShimmerAdapter();
                        sciFiAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for horror movies
        database.getReference().child("Movies")
                .limitToLast(300)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        horrorList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(horrorList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Horror")) {
                                horrorList.add(model);
                            } else if (genre1.contains("Horror")) {
                                horrorList.add(model);
                            } else if (genre2.contains("Horror")) {
                                horrorList.add(model);
                            }
                        }

                        horrorRv.setAdapter(horrorAdapter);
                        horrorRv.hideShimmerAdapter();
                        horrorAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for comedy movies
        database.getReference().child("Movies")
                .limitToFirst(300)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        comedyList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(comedyList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Comedy")) {
                                comedyList.add(model);
                            } else if (genre1.contains("Comedy")) {
                                comedyList.add(model);
                            } else if (genre2.contains("Comedy")) {
                                comedyList.add(model);
                            }
                        }

                        comedyRv.setAdapter(comedyAdapter);
                        comedyRv.hideShimmerAdapter();
                        comedyAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for crime movies
        database.getReference().child("Movies")
                .limitToLast(300)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        crimeList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(crimeList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Crime")) {
                                crimeList.add(model);
                            } else if (genre1.contains("Crime")) {
                                crimeList.add(model);
                            } else if (genre2.contains("Crime")) {
                                crimeList.add(model);
                            }
                        }

                        crimeRv.setAdapter(crimeAdapter);
                        crimeRv.hideShimmerAdapter();
                        crimeAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for korean movies
        database.getReference().child("Movies")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        koreanList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(koreanList);
                            String keywords = model.getKeywords();
                            if (keywords.contains("Korean")){
                                koreanList.add(model);
                            }
                        }

                        koreanRv.setAdapter(koreanAdapter);
                        koreanRv.hideShimmerAdapter();
                        koreanAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for adventure movies
        database.getReference().child("Movies")
                .limitToLast(210)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        adventureList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(adventureList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Adventure")) {
                                adventureList.add(model);
                            } else if (genre1.contains("Adventure")) {
                                adventureList.add(model);
                            } else if (genre2.contains("Adventure")) {
                                adventureList.add(model);
                            }
                        }

                        adventureRv.setAdapter(adventureAdapter);
                        adventureRv.hideShimmerAdapter();
                        adventureAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for year2015 movies
        database.getReference().child("Movies")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        year2015List.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(year2015List);
                            String year = model.getMovieYear();
                            if (year.contains("1971")){
                                year2015List.add(model);
                            }else if (year.contains("1972")){
                                year2015List.add(model);
                            }else if (year.contains("1974")){
                                year2015List.add(model);
                            }else if (year.contains("1978")){
                                year2015List.add(model);
                            }else if (year.contains("1990")){
                                year2015List.add(model);
                            }else if (year.contains("1994")){
                                year2015List.add(model);
                            }else if (year.contains("1996")){
                                year2015List.add(model);
                            }else if (year.contains("1999")){
                                year2015List.add(model);
                            }else if (year.contains("1989")){
                                year2015List.add(model);
                            }else if (year.contains("1997")){
                                year2015List.add(model);
                            }else if (year.contains("1998")){
                                year2015List.add(model);
                            }else if (year.contains("1953")){
                                year2015List.add(model);
                            }else if (year.contains("1988")){
                                year2015List.add(model);
                            }else if (year.contains("1940")){
                                year2015List.add(model);
                            }else if (year.contains("1985")){
                                year2015List.add(model);
                            }else if (year.contains("1986")){
                                year2015List.add(model);
                            }else if (year.contains("1987")){
                                year2015List.add(model);
                            }else if (year.contains("1980")){
                                year2015List.add(model);
                            }else if (year.contains("1968")){
                                year2015List.add(model);
                            }else if (year.contains("1995")){
                                year2015List.add(model);
                            }

                        }

                        year2015Rv.setAdapter(year2015Adapter);
                        year2015Rv.hideShimmerAdapter();
                        year2015Adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for mystery movies
        database.getReference().child("Movies")
                .limitToLast(300)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        mysteryList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(mysteryList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Mystery")) {
                                mysteryList.add(model);
                            } else if (genre1.contains("Mystery")) {
                                mysteryList.add(model);
                            } else if (genre2.contains("Mystery")) {
                                mysteryList.add(model);
                            }
                        }

                        mysteryRv.setAdapter(mysteryAdapter);
                        mysteryRv.hideShimmerAdapter();
                        mysteryAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for romantic movies
        database.getReference().child("Movies")
                .limitToLast(300)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        romanticList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(romanticList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Romance")) {
                                romanticList.add(model);
                            } else if (genre1.contains("Romance")) {
                                romanticList.add(model);
                            } else if (genre2.contains("Romance")) {
                                romanticList.add(model);
                            }
                        }

                        romanticRv.setAdapter(romanticAdapter);
                        romanticRv.hideShimmerAdapter();
                        romanticAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for thriller movies
        database.getReference().child("Movies")
                .limitToLast(210)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        thrillerList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(thrillerList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Thriller")) {
                                thrillerList.add(model);
                            } else if (genre1.contains("Thriller")) {
                                thrillerList.add(model);
                            } else if (genre2.contains("Thriller")) {
                                thrillerList.add(model);
                            }
                        }

                        thrillerRv.setAdapter(thrillerAdapter);
                        thrillerRv.hideShimmerAdapter();
                        thrillerAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for animation movies
        database.getReference().child("Movies")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        animationList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(animationList);
                            String genre = model.getGenre();
                            String genre1 = model.getGenre1();
                            String genre2 = model.getGenre2();
                            if (genre.contains("Animation")) {
                                animationList.add(model);
                            } else if (genre1.contains("Animation")) {
                                animationList.add(model);
                            } else if (genre2.contains("Animation")) {
                                animationList.add(model);
                            }
                        }

                        animationRv.setAdapter(animationAdapter);
                        animationRv.hideShimmerAdapter();
                        animationAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for tvSeries movies
        database.getReference().child("Movies")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvSeriesList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(tvSeriesList);
                            String type = model.getType();
                            Collections.shuffle(tvSeriesList);
                            if (type.equals("TV")){
                                tvSeriesList.add(model);
                            }
                        }

                        tvSeriesRv.setAdapter(tvSeriesAdapter);
                        tvSeriesRv.hideShimmerAdapter();
                        tvSeriesAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //for flexFav movies
        database.getReference().child("Movies")
                .limitToLast(210)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        flexFavList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(flexFavList);
                            String type = model.getType();
                            Collections.shuffle(flexFavList);
                            if (type.equals("Flex")){
                                flexFavList.add(model);
                            }
                        }

                        flexFavRv.setAdapter(flexFavAdapter);
                        flexFavRv.hideShimmerAdapter();
                        flexFavAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //for chinese movies
        database.getReference().child("Movies")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chineseList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                            assert model != null;
                            model.setPostId(dataSnapshot.getKey());
                            Collections.shuffle(chineseList);
                            String keywords = model.getKeywords();
                            if (keywords.contains("Chinese")){
                                chineseList.add(model);
                            }
                        }

                        chineseRv.setAdapter(chineseAdapter);
                        chineseRv.hideShimmerAdapter();
                        chineseAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        //find evergreen swipe refresh
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshDiscuss);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRefresh() {
                //evergreen
                evergreenRv.showShimmerAdapter();
                actionRv.showShimmerAdapter();
                evergreenRv.setLayoutManager(evergreen);
                actionRv.setLayoutManager(action);

                //for evergreen movies
                database.getReference().child("Movies")
                        .limitToLast(120)
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
                                evergreenRv.setAdapter(moviesAdapter);
                                evergreenRv.hideShimmerAdapter();
                                moviesAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for action movies
                database.getReference().child("Movies")
                        .limitToLast(120)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                actionList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(actionList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Action")) {
                                        actionList.add(model);
                                    } else if (genre1.contains("Action")) {
                                        actionList.add(model);
                                    } else if (genre2.contains("Action")) {
                                        actionList.add(model);
                                    }
                                }

                                actionRv.setAdapter(actionAdapter);
                                actionRv.hideShimmerAdapter();
                                actionAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                //for topRated movies
                database.getReference().child("Movies")
                        .limitToLast(210)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                topRatedList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(topRatedList);
                                    float rating = model.getRating();
                                    if (rating >= 7.0) {
                                        topRatedList.add(model);
                                    }

                                }

                                topRatedRv.setAdapter(topRatedAdapter);
                                topRatedRv.hideShimmerAdapter();
                                topRatedAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for latest movies
                database.getReference().child("Movies")
                        .limitToLast(400)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                latestList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(latestList);
                                    String latest = model.getKeywords();
                                    float rating = model.getRating();
                                    String year = model.getMovieYear();
                                    if (latest.contains("Latest Updated")) {
                                        latestList.add(model);
                                    }else if (rating >=8.5){
                                        latestList.add(model);
                                    }else if (year.equals("2022")){
                                        latestList.add(model);
                                    }else if (year.equals("New Movies")){
                                        latestList.add(model);
                                    }else if (year.equals("2021")){
                                        latestList.add(model);
                                    }

                                }

                                latestRv.setAdapter(latestAdapter);
                                latestRv.hideShimmerAdapter();
                                latestAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for sciFi movies
                database.getReference().child("Movies")
                        .limitToLast(300)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                sciFiList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(sciFiList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Sci-Fi")) {
                                        sciFiList.add(model);
                                    } else if (genre1.contains("Sci-Fi")) {
                                        sciFiList.add(model);
                                    } else if (genre2.contains("Sci-Fi")) {
                                        sciFiList.add(model);
                                    }
                                }

                                sciFiRv.setAdapter(sciFiAdapter);
                                sciFiRv.hideShimmerAdapter();
                                sciFiAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for horror movies
                database.getReference().child("Movies")
                        .limitToLast(300)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                horrorList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(horrorList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Horror")) {
                                        horrorList.add(model);
                                    } else if (genre1.contains("Horror")) {
                                        horrorList.add(model);
                                    } else if (genre2.contains("Horror")) {
                                        horrorList.add(model);
                                    }
                                }

                                horrorRv.setAdapter(horrorAdapter);
                                horrorRv.hideShimmerAdapter();
                                horrorAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for comedy movies
                database.getReference().child("Movies")
                        .limitToFirst(300)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                comedyList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(comedyList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Comedy")) {
                                        comedyList.add(model);
                                    } else if (genre1.contains("Comedy")) {
                                        comedyList.add(model);
                                    } else if (genre2.contains("Comedy")) {
                                        comedyList.add(model);
                                    }
                                }

                                comedyRv.setAdapter(comedyAdapter);
                                comedyRv.hideShimmerAdapter();
                                comedyAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for crime movies
                database.getReference().child("Movies")
                        .limitToLast(300)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                crimeList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(crimeList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Crime")) {
                                        crimeList.add(model);
                                    } else if (genre1.contains("Crime")) {
                                        crimeList.add(model);
                                    } else if (genre2.contains("Crime")) {
                                        crimeList.add(model);
                                    }
                                }

                                crimeRv.setAdapter(crimeAdapter);
                                crimeRv.hideShimmerAdapter();
                                crimeAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for korean movies
                database.getReference().child("Movies")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                koreanList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(koreanList);
                                    String keywords = model.getKeywords();
                                    if (keywords.contains("Korean")){
                                        koreanList.add(model);
                                    }
                                }

                                koreanRv.setAdapter(koreanAdapter);
                                koreanRv.hideShimmerAdapter();
                                koreanAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for adventure movies
                database.getReference().child("Movies")
                        .limitToLast(210)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                adventureList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(adventureList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Adventure")) {
                                        adventureList.add(model);
                                    } else if (genre1.contains("Adventure")) {
                                        adventureList.add(model);
                                    } else if (genre2.contains("Adventure")) {
                                        adventureList.add(model);
                                    }
                                }

                                adventureRv.setAdapter(adventureAdapter);
                                adventureRv.hideShimmerAdapter();
                                adventureAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for year2015 movies
                database.getReference().child("Movies")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                year2015List.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(year2015List);
                                    String year = model.getMovieYear();
                                    if (year.contains("1971")){
                                        year2015List.add(model);
                                    }else if (year.contains("1972")){
                                        year2015List.add(model);
                                    }else if (year.contains("1974")){
                                        year2015List.add(model);
                                    }else if (year.contains("1978")){
                                        year2015List.add(model);
                                    }else if (year.contains("1990")){
                                        year2015List.add(model);
                                    }else if (year.contains("1994")){
                                        year2015List.add(model);
                                    }else if (year.contains("1996")){
                                        year2015List.add(model);
                                    }else if (year.contains("1999")){
                                        year2015List.add(model);
                                    }else if (year.contains("1989")){
                                        year2015List.add(model);
                                    }else if (year.contains("1997")){
                                        year2015List.add(model);
                                    }else if (year.contains("1998")){
                                        year2015List.add(model);
                                    }else if (year.contains("1953")){
                                        year2015List.add(model);
                                    }else if (year.contains("1988")){
                                        year2015List.add(model);
                                    }else if (year.contains("1940")){
                                        year2015List.add(model);
                                    }else if (year.contains("1985")){
                                        year2015List.add(model);
                                    }else if (year.contains("1986")){
                                        year2015List.add(model);
                                    }else if (year.contains("1987")){
                                        year2015List.add(model);
                                    }else if (year.contains("1980")){
                                        year2015List.add(model);
                                    }else if (year.contains("1968")){
                                        year2015List.add(model);
                                    }else if (year.contains("1995")){
                                        year2015List.add(model);
                                    }

                                }

                                year2015Rv.setAdapter(year2015Adapter);
                                year2015Rv.hideShimmerAdapter();
                                year2015Adapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for mystery movies
                database.getReference().child("Movies")
                        .limitToLast(300)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                mysteryList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(mysteryList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Mystery")) {
                                        mysteryList.add(model);
                                    } else if (genre1.contains("Mystery")) {
                                        mysteryList.add(model);
                                    } else if (genre2.contains("Mystery")) {
                                        mysteryList.add(model);
                                    }
                                }

                                mysteryRv.setAdapter(mysteryAdapter);
                                mysteryRv.hideShimmerAdapter();
                                mysteryAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for romantic movies
                database.getReference().child("Movies")
                        .limitToLast(300)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                romanticList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(romanticList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Romance")) {
                                        romanticList.add(model);
                                    } else if (genre1.contains("Romance")) {
                                        romanticList.add(model);
                                    } else if (genre2.contains("Romance")) {
                                        romanticList.add(model);
                                    }
                                }

                                romanticRv.setAdapter(romanticAdapter);
                                romanticRv.hideShimmerAdapter();
                                romanticAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for thriller movies
                database.getReference().child("Movies")
                        .limitToLast(210)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                thrillerList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(thrillerList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Thriller")) {
                                        thrillerList.add(model);
                                    } else if (genre1.contains("Thriller")) {
                                        thrillerList.add(model);
                                    } else if (genre2.contains("Thriller")) {
                                        thrillerList.add(model);
                                    }
                                }

                                thrillerRv.setAdapter(thrillerAdapter);
                                thrillerRv.hideShimmerAdapter();
                                thrillerAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for animation movies
                database.getReference().child("Movies")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                animationList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(animationList);
                                    String genre = model.getGenre();
                                    String genre1 = model.getGenre1();
                                    String genre2 = model.getGenre2();
                                    if (genre.contains("Animation")) {
                                        animationList.add(model);
                                    } else if (genre1.contains("Animation")) {
                                        animationList.add(model);
                                    } else if (genre2.contains("Animation")) {
                                        animationList.add(model);
                                    }
                                }

                                animationRv.setAdapter(animationAdapter);
                                animationRv.hideShimmerAdapter();
                                animationAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for tvSeries movies
                database.getReference().child("Movies")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                tvSeriesList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(tvSeriesList);
                                    String type = model.getType();
                                    Collections.shuffle(tvSeriesList);
                                    if (type.equals("TV")){
                                        tvSeriesList.add(model);
                                    }
                                }

                                tvSeriesRv.setAdapter(tvSeriesAdapter);
                                tvSeriesRv.hideShimmerAdapter();
                                tvSeriesAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                //for flexFav movies
                database.getReference().child("Movies")
                        .limitToLast(210)
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                flexFavList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(flexFavList);
                                    String type = model.getType();
                                    Collections.shuffle(flexFavList);
                                    if (type.equals("Flex")){
                                        flexFavList.add(model);
                                    }
                                }

                                flexFavRv.setAdapter(flexFavAdapter);
                                flexFavRv.hideShimmerAdapter();
                                flexFavAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                //for chinese movies
                database.getReference().child("Movies")
                        .addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                chineseList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    MoviesModel model = dataSnapshot.getValue(MoviesModel.class);
                                    assert model != null;
                                    model.setPostId(dataSnapshot.getKey());
                                    Collections.shuffle(chineseList);
                                    String keywords = model.getKeywords();
                                    if (keywords.contains("Chinese")){
                                        chineseList.add(model);
                                    }
                                }

                                chineseRv.setAdapter(chineseAdapter);
                                chineseRv.hideShimmerAdapter();
                                chineseAdapter.notifyDataSetChanged();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                moviesAdapter.notifyDataSetChanged();
                actionAdapter.notifyDataSetChanged();
                topRatedAdapter.notifyDataSetChanged();
                latestAdapter.notifyDataSetChanged();
                sciFiAdapter.notifyDataSetChanged();
                horrorAdapter.notifyDataSetChanged();
                comedyAdapter.notifyDataSetChanged();
                crimeAdapter.notifyDataSetChanged();
                koreanAdapter.notifyDataSetChanged();
                adventureAdapter.notifyDataSetChanged();
                year2015Adapter.notifyDataSetChanged();
                mysteryAdapter.notifyDataSetChanged();
                romanticAdapter.notifyDataSetChanged();
                thrillerAdapter.notifyDataSetChanged();
                animationAdapter.notifyDataSetChanged();
                tvSeriesAdapter.notifyDataSetChanged();
                flexFavAdapter.notifyDataSetChanged();
                chineseAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);


            }
        });

        //all more click listener
        evergreenMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","All Time Hits");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
        actionMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Action");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        topRatedMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Top Rated");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        latestMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Latest");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        sciFiMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Sci-Fi");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        horrorMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Horror");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        comedyMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Comedy");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        crimeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Crime");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        koreanMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Korean");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        adventureMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Adventure");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        year2015More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Old Is Gold");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mysteryMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Mystery");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        romanticMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Romance");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        thrillerMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Thriller");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        animationMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Animation");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        tvSeriesMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","TV");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        flexFavMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Flex Favorites");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        chineseMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CategoryDetailsActivity.class);
                intent.putExtra("category","Chinese");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        return view;
    }

    private void loadNativeAd2() {
        nativeAdLoader = new MaxNativeAdLoader(getResources().getString(R.string.native_small_ad_unit_id), getActivity());
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd);
                }
                // Save ad for cleanup.
                nativeAd = ad;
                // Add ad view to view.
                nativeAdContainer3.removeAllViews();
                nativeAdContainer3.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                // We recommend retrying with exponentially higher delays up to a maximum delay
                nativeAdContainer3.setVisibility(View.GONE);
                retry++;
                long delay = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retry)));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nativeAdLoader.loadAd();
                    }
                }, delay);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                // Optional click callback
            }
        });
        nativeAdLoader.loadAd();
    }

    private void loadMediumNativeAd2() {
        nativeAdLoader = new MaxNativeAdLoader(getResources().getString(R.string.native_medium_ad_unit_id), getActivity());
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd);
                }
                // Save ad for cleanup.
                nativeAd = ad;
                // Add ad view to view.
                nativeAdContainer4.removeAllViews();
                nativeAdContainer4.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                // We recommend retrying with exponentially higher delays up to a maximum delay
                nativeAdContainer4.setVisibility(View.GONE);
                retry++;
                long delay = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retry)));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nativeAdLoader.loadAd();
                    }
                }, delay);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                // Optional click callback
            }
        });
        nativeAdLoader.loadAd();
    }

    private void noConnectionDialog() {
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
    //load small native ad
    public void loadNativeAd() {
        nativeAdLoader = new MaxNativeAdLoader(getResources().getString(R.string.native_small_ad_unit_id), getActivity());
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd);
                }
                // Save ad for cleanup.
                nativeAd = ad;
                // Add ad view to view.
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                // We recommend retrying with exponentially higher delays up to a maximum delay
                nativeAdContainer.setVisibility(View.GONE);
                retry++;
                long delay = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retry)));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nativeAdLoader.loadAd();
                    }
                }, delay);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                // Optional click callback
            }
        });
        nativeAdLoader.loadAd();
    }
    //load medium native ad
    public void loadMediumNativeAd() {
        nativeAdLoader = new MaxNativeAdLoader(getResources().getString(R.string.native_medium_ad_unit_id), getActivity());
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd);
                }
                // Save ad for cleanup.
                nativeAd = ad;
                // Add ad view to view.
                nativeAdContainer2.removeAllViews();
                nativeAdContainer2.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                // We recommend retrying with exponentially higher delays up to a maximum delay
                nativeAdContainer2.setVisibility(View.GONE);
                retry++;
                long delay = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retry)));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nativeAdLoader.loadAd();
                    }
                }, delay);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                // Optional click callback
            }
        });
        nativeAdLoader.loadAd();
    }
    //onCreate Option Menu
    //Hide menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem notifications = menu.findItem(R.id.notification);
        notifications.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(getActivity(), SearchActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}