package com.hdmovies.onlinecinema.Activities;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applovin.mediation.ads.MaxAdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hdmovies.onlinecinema.Adapter.FeedsAdapter;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.Model.FeedsModel;
import com.hdmovies.onlinecinema.Model.UserModel;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.AdNetwork;
import com.hdmovies.onlinecinema.Utils.NetworkChecks;
import com.hdmovies.onlinecinema.databinding.ActivityUserProfilesBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class UserProfilesActivity extends AppCompatActivity {

    ActivityUserProfilesBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<FeedsModel> feedsList;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    Intent intent;
    String postedBy;
    Activity activity;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        activity = UserProfilesActivity.this;
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

        //get intent data
        intent = getIntent();
        postedBy = intent.getStringExtra("postedBy");

        //database instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //recyclerview
        recyclerView = findViewById(R.id.postRV);
        feedsList = new ArrayList<>();

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
            networkChecks.noConnectionDialog();
        }

        /*check if user is sign in or sign out*/
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(UserProfilesActivity.this, SignInActivity.class);
            startActivity(intent);
        } else {
            //google sign in data fetch with image
            database.getReference().child("UserData").child(postedBy)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                UserModel user = snapshot.getValue(UserModel.class);
                                assert user != null;
                                Picasso.get()
                                        .load(user.getProfile())
                                        .placeholder(R.drawable.ic_profile_default_image)
                                        .into(binding.profileImage);

                                Picasso.get()
                                        .load(user.getCoverPhoto())
                                        .placeholder(R.drawable.ic_placeholder_dark)
                                        .into(binding.coverPhoto);

                                String userName = user.getUserName();
                                String profession = user.getProfession();
                                String bio = user.getUserBio();
                                String fb = user.getFbLink();
                                String insta = user.getInstaLink();
                                String github = user.getGithubLink();
                                String linkedin = user.getLinkedinLink();
                                String twitter = user.getTwitterLink();

                                //toolbar
                                setSupportActionBar(binding.toolbar2);
                                UserProfilesActivity.this.setTitle("Profile: "+userName);
                                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

                                //set value
                                binding.userName.setText(userName);
                                if (profession.isEmpty()){
                                    binding.profession.setText(getResources().getString(R.string.profession));
                                }else {
                                    binding.profession.setText(profession);
                                }
                                if (bio.isEmpty()){
                                    binding.userBioText.setText(getResources().getString(R.string.about_yourself));
                                }else {
                                    binding.userBioText.setText(bio);
                                }
                                //insert link data
                                binding.linkFacebook.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (fb.isEmpty()) {
                                                toastText.setText(R.string.link_not_found);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(activity, WebviewActivity.class);
                                                intent.putExtra("IMDbLink", fb);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            networkChecks.noConnectionDialog();
                                        }
                                    }
                                });//facebook
                                binding.linkInstagram.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (insta.isEmpty()) {
                                                toastText.setText(R.string.link_not_found);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(activity, WebviewActivity.class);
                                                intent.putExtra("IMDbLink", insta);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            networkChecks.noConnectionDialog();
                                        }
                                    }
                                });//instagram
                                binding.linkGithub.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (github.isEmpty()) {
                                                toastText.setText(R.string.link_not_found);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(activity, WebviewActivity.class);
                                                intent.putExtra("IMDbLink", github);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            networkChecks.noConnectionDialog();
                                        }
                                    }
                                });//github
                                binding.linkLinkedIn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (linkedin.isEmpty()) {
                                                toastText.setText(R.string.link_not_found);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(activity, WebviewActivity.class);
                                                intent.putExtra("IMDbLink", linkedin);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            networkChecks.noConnectionDialog();
                                        }
                                    }
                                });//linkedin
                                binding.linkTwitter.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //network check
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                            if (twitter.isEmpty()) {
                                                toastText.setText(R.string.link_not_found);
                                                toast.show();
                                            }else{
                                                Intent intent = new Intent(activity, WebviewActivity.class);
                                                intent.putExtra("IMDbLink", twitter);
                                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                            }
                                        } else {
                                            networkChecks.noConnectionDialog();
                                        }
                                    }
                                });//twitter
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //post data get
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
                                String postBy = model.getPostedBy();
                                assert postBy != null;
                                if (postedBy.equals(postBy)) {
                                    feedsList.add(model);
                                }
                            }
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //find swipe refresh
            swipeRefreshLayout = findViewById(R.id.swipPostRefresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onRefresh() {
                    //google sign in data fetch with image
                    database.getReference().child("UserData").child(postedBy)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        UserModel user = snapshot.getValue(UserModel.class);
                                        assert user != null;
                                        Picasso.get()
                                                .load(user.getProfile())
                                                .placeholder(R.drawable.ic_profile_default_image)
                                                .into(binding.profileImage);

                                        Picasso.get()
                                                .load(user.getCoverPhoto())
                                                .placeholder(R.drawable.ic_placeholder_dark)
                                                .into(binding.coverPhoto);

                                        String userName = user.getUserName();
                                        String profession = user.getProfession();
                                        String bio = user.getUserBio();
                                        String fb = user.getFbLink();
                                        String insta = user.getInstaLink();
                                        String github = user.getGithubLink();
                                        String linkedin = user.getLinkedinLink();
                                        String twitter = user.getTwitterLink();

                                        //set value
                                        binding.userName.setText(userName);
                                        if (profession.isEmpty()){
                                            binding.profession.setText(getResources().getString(R.string.profession));
                                        }else {
                                            binding.profession.setText(profession);
                                        }
                                        if (bio.isEmpty()){
                                            binding.userBioText.setText(getResources().getString(R.string.about_yourself));
                                        }else {
                                            binding.userBioText.setText(bio);
                                        }
                                        //insert link data
                                        binding.linkFacebook.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //network check
                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                    if (fb.isEmpty()) {
                                                        toastText.setText(R.string.link_not_found);
                                                        toast.show();
                                                    }else{
                                                        Intent intent = new Intent(activity, WebviewActivity.class);
                                                        intent.putExtra("IMDbLink", fb);
                                                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                } else {
                                                    networkChecks.noConnectionDialog();
                                                }
                                            }
                                        });//facebook
                                        binding.linkInstagram.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //network check
                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                    if (insta.isEmpty()) {
                                                        toastText.setText(R.string.link_not_found);
                                                        toast.show();
                                                    }else{
                                                        Intent intent = new Intent(activity, WebviewActivity.class);
                                                        intent.putExtra("IMDbLink", insta);
                                                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                } else {
                                                    networkChecks.noConnectionDialog();
                                                }
                                            }
                                        });//instagram
                                        binding.linkGithub.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //network check
                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                    if (github.isEmpty()) {
                                                        toastText.setText(R.string.link_not_found);
                                                        toast.show();
                                                    }else{
                                                        Intent intent = new Intent(activity, WebviewActivity.class);
                                                        intent.putExtra("IMDbLink", github);
                                                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                } else {
                                                    networkChecks.noConnectionDialog();
                                                }
                                            }
                                        });//github
                                        binding.linkLinkedIn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //network check
                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                    if (linkedin.isEmpty()) {
                                                        toastText.setText(R.string.link_not_found);
                                                        toast.show();
                                                    }else{
                                                        Intent intent = new Intent(activity, WebviewActivity.class);
                                                        intent.putExtra("IMDbLink", linkedin);
                                                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                } else {
                                                    networkChecks.noConnectionDialog();
                                                }
                                            }
                                        });//linkedin
                                        binding.linkTwitter.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //network check
                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                    if (twitter.isEmpty()) {
                                                        toastText.setText(R.string.link_not_found);
                                                        toast.show();
                                                    }else{
                                                        Intent intent = new Intent(activity, WebviewActivity.class);
                                                        intent.putExtra("IMDbLink", twitter);
                                                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                } else {
                                                    networkChecks.noConnectionDialog();
                                                }
                                            }
                                        });//twitter
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    //post data get
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
                                        String postBy = model.getPostedBy();
                                        assert postBy != null;
                                        if (postedBy.equals(postBy)) {
                                            feedsList.add(model);
                                        }
                                    }
                                    recyclerView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });



        }//ends of else part


    }//ends of onCreate

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}