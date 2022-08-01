package com.watchfreemovies.freehdcinema786.Activities;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.watchfreemovies.freehdcinema786.Adapter.CommentAdapter;
import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.Categories.CategoryDetailsActivity;
import com.watchfreemovies.freehdcinema786.Categories.RelatedMoviesActivity;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.CommentModel;
import com.watchfreemovies.freehdcinema786.Model.MoviesModel;
import com.watchfreemovies.freehdcinema786.Model.ReportsModel;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.Utils.NetworkChecks;
import com.watchfreemovies.freehdcinema786.databinding.ActivityMoviesDetailsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MoviesDetailsActivity extends AppCompatActivity implements MaxRewardedAdListener{

    ActivityMoviesDetailsBinding binding;
    String movieName, movieYear, genre, genre1, genre2, movieDescription, imdbLink, trailerLink, keywords, imageUri;
    String server_1, server_2, server_3, server_4;
    float movieRating;
    Intent intent;
    String postId;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<CommentModel> list = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    ShimmerRecyclerView commentShimmer;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    int counter = 1;
    Activity activity;
    ProgressDialog dialog;
    int serverClick;
    String clickBtn = "";
    Dialog serverDialog;
    Dialog reportDialog;
    TextView btnCancel, btnOk;
    RadioGroup radioGroup;
    RadioButton radioButton;
    AdNetwork adNetwork;
    private MaxRewardedAd rewardedAd;
    String unityRewardedAd = "Rewarded_Android";
    private int retryAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMoviesDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 200);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);

        activity = MoviesDetailsActivity.this;
        serverDialog = new Dialog(this);
        reportDialog = new Dialog(this);

        NetworkChecks networkChecks = new NetworkChecks(this);


        //swipe refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshComment);
        commentShimmer = findViewById(R.id.commentRv);

        //get data by intent
        intent = getIntent();
        postId = intent.getStringExtra("postId");

        //progress dialog
        dialog = new ProgressDialog(MoviesDetailsActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Report Submitting");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //database instance
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //ads init
        adNetwork = new AdNetwork(this);
        //adNetwork.loadBannerAd();
        adNetwork.loadInterstitialAd();
        //banner
        MaxAdView bannerAd = findViewById(R.id.adView);
        LinearLayout unityBannerAd = findViewById(R.id.banner_ad);
        adNetwork.loadBannerAd();
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

        /*rewardedAd = MaxRewardedAd.getInstance(getResources().getString(R.string.rewarded_ad_unit_id), this);
        rewardedAd.setListener(this);
        rewardedAd.loadAd();*/

        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            toastText.setText(R.string.network_connected);
        } else {
            toastText.setText(R.string.no_connection_text);
            toast.show();
        }

        //get Movie database values
        database.getReference()
                .child("Movies")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MoviesModel model = snapshot.getValue(MoviesModel.class);

                //for share data
                assert model != null;
                movieName = model.getMovieName();
                movieYear = model.getMovieYear();
                movieDescription = model.getDescription();
                movieRating = model.getRating();
                imdbLink = model.getImdbLink();
                trailerLink = model.getTrailerLink();
                genre = model.getGenre();
                genre1 = model.getGenre1();
                genre2 = model.getGenre2();
                keywords = model.getKeywords();
                imageUri = model.getMovieImage();
                server_1 = model.getServer_1();
                server_2 = model.getServer_2();
                server_3 = model.getServer_3();
                server_4 = model.getServer_4();

                //toolbar
                setSupportActionBar(binding.toolbar2);
                activity.setTitle(movieName);
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

                //get id's data
                String time = TimeAgo.using(model.getPostedAt());
                String likes = model.getLikesCount() + "";
                String views = model.getPostViews() + "";
                String comments = model.getCommentCount() + "";
                String shares = model.getShareCount() + "";
                String ratings = String.valueOf(model.getRating());

                //set id's data
                binding.movieName.setText(movieName);
                binding.mDescriptions.setText(movieDescription);
                binding.rating.setText(ratings);
                binding.time.setText(time);

                if (UiConfig.EDIT_MOVIES_BUTTON) {
                    binding.editMovie.setVisibility(View.VISIBLE);
                } else {
                    binding.editMovie.setVisibility(View.GONE);
                }

                //edit movie details
                binding.editMovie.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MoviesDetailsActivity.this, EditMoviesActivity.class);
                        intent.putExtra("postId", postId);
                        intent.putExtra("movieName", movieName);
                        intent.putExtra("movieYear", movieYear);
                        intent.putExtra("movieImage", imageUri);
                        intent.putExtra("movieDes", movieDescription);
                        intent.putExtra("movieRating", movieRating);
                        intent.putExtra("imdbLink", imdbLink);
                        intent.putExtra("trailerLink", trailerLink);
                        intent.putExtra("keywords", keywords);
                        intent.putExtra("g1", genre);
                        intent.putExtra("g2", genre1);
                        intent.putExtra("g3", genre2);
                        intent.putExtra("server_1", server_1);
                        intent.putExtra("server_2", server_2);
                        intent.putExtra("server_3", server_3);
                        intent.putExtra("server_4", server_4);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });

                //user report data
                database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    UserModel user = snapshot.getValue(UserModel.class);
                                    assert user != null;
                                    //report data
                                    binding.report.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //network check
                                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                                                //we are connected to a network
                                                reportDialog.setContentView(R.layout.custom_movies_report_layout);
                                                //find id's
                                                btnCancel = reportDialog.findViewById(R.id.btnCancel);
                                                radioGroup = reportDialog.findViewById(R.id.rdGroup);
                                                btnOk = reportDialog.findViewById(R.id.reportOk);

                                                //btn ok
                                                btnOk.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        int selectedId = radioGroup.getCheckedRadioButtonId();
                                                        radioButton = reportDialog.findViewById(selectedId);
                                                        String values = radioButton.getText().toString();
                                                        //set database values
                                                        ReportsModel model = new ReportsModel();
                                                        model.setUserName(user.getUserName());
                                                        model.setPostId(postId);
                                                        model.setUserEmail(user.getEmail());
                                                        model.setSelectIssue(values);
                                                        model.setMovieName(movieName);
                                                        model.setReportedBy(FirebaseAuth.getInstance().getUid());
                                                        //database code
                                                        database.getReference()
                                                                .child("Reports")
                                                                .child("Movies")
                                                                .push()
                                                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                reportDialog.dismiss();
                                                                toastText.setText(R.string.report_submitted);
                                                                toast.show();

                                                            }
                                                        });

                                                    }
                                                });

                                                //cancel
                                                btnCancel.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        reportDialog.dismiss();
                                                    }
                                                });

                                                reportDialog.setCancelable(false);
                                                reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                reportDialog.show();
                                            } else {
                                                networkChecks.noConnectionDialog();
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });


                //set movie year
                binding.movieYear.setText(movieYear);
                binding.movieYear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (movieYear) {
                            case "2021":
                                Intent intent = new Intent(activity, CategoryDetailsActivity.class);
                                intent.putExtra("category", "2021");
                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2022":
                                Intent intent2022 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2022.putExtra("category", "2022");
                                intent2022.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2022);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2020":
                                Intent intent2020 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2020.putExtra("category", "2020");
                                intent2020.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2020);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2019":
                                Intent intent2019 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2019.putExtra("category", "2019");
                                intent2019.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2019);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2018":
                                Intent intent2018 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2018.putExtra("category", "2018");
                                intent2018.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2018);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2017":
                                Intent intent2017 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2017.putExtra("category", "2017");
                                intent2017.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2017);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2016":
                                Intent intent2016 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2016.putExtra("category", "2016");
                                intent2016.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2016);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2015":
                                Intent intent2015 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2015.putExtra("category", "2015");
                                intent2015.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2015);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2014":
                                Intent intent2014 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2014.putExtra("category", "2014");
                                intent2014.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2014);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2013":
                                Intent intent2013 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2013.putExtra("category", "2013");
                                intent2013.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2013);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2012":
                                Intent intent2012 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2012.putExtra("category", "2012");
                                intent2012.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2012);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2011":
                                Intent intent2011 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2011.putExtra("category", "2011");
                                intent2011.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2011);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "2010":
                                Intent intent2010 = new Intent(activity, CategoryDetailsActivity.class);
                                intent2010.putExtra("category", "2010");
                                intent2010.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent2010);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;

                            case "2009":
                            case "2008":
                            case "2007":
                            case "2006":
                            case "2005":
                            case "2004":
                            case "2003":
                            case "2002":
                            case "2001":
                            case "2000":
                            case "1999":
                            case "1998":
                            case "1997":
                            case "1996":
                            case "1995":
                            case "1994":
                            case "1993":
                            case "1992":
                            case "1991":
                            case "1990":
                            case "1989":
                            case "1988":
                            case "1987":
                            case "1986":
                            case "1985":
                            case "1971":
                            case "1972":
                            case "1973":
                            case "1974":
                            case "1953":
                            case "1940":
                            case "1978":
                            case "1984":
                            case "1983":
                            case "1982":
                            case "1981":
                            case "1980":
                                Intent oldIsGold = new Intent(activity, CategoryDetailsActivity.class);
                                oldIsGold.putExtra("category", "Old Is Gold");
                                oldIsGold.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(oldIsGold);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            default:
                                Intent more = new Intent(activity, CategoryDetailsActivity.class);
                                more.putExtra("category", "More");
                                more.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(more);
                                binding.movieYear.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.movieYear.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                        }
                        adNetwork.showInterstitialAd();

                    }
                });
                //set genre values
                binding.genre.setText(genre);
                binding.genre1.setText(genre1);
                binding.genre2.setText(genre2);
                //set genre click logic
                binding.genre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (genre) {
                            case "Comedy":
                                Intent intent = new Intent(activity, CategoryDetailsActivity.class);
                                intent.putExtra("category", "Comedy");
                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Drama":
                                Intent Drama = new Intent(activity, CategoryDetailsActivity.class);
                                Drama.putExtra("category", "Drama");
                                Drama.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Drama);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Romance":
                                Intent Romance = new Intent(activity, CategoryDetailsActivity.class);
                                Romance.putExtra("category", "Romance");
                                Romance.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Romance);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Korean":
                                Intent Korean = new Intent(activity, CategoryDetailsActivity.class);
                                Korean.putExtra("category", "Korean");
                                Korean.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Korean);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Crime":
                                Intent Crime = new Intent(activity, CategoryDetailsActivity.class);
                                Crime.putExtra("category", "Crime");
                                Crime.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Crime);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Chinese":
                                Intent Chinese = new Intent(activity, CategoryDetailsActivity.class);
                                Chinese.putExtra("category", "Chinese");
                                Chinese.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Chinese);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Horror":
                                Intent Horror = new Intent(activity, CategoryDetailsActivity.class);
                                Horror.putExtra("category", "Horror");
                                Horror.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Horror);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Action":
                                Intent Action = new Intent(activity, CategoryDetailsActivity.class);
                                Action.putExtra("category", "Action");
                                Action.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Action);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Adventure":
                                Intent Adventure = new Intent(activity, CategoryDetailsActivity.class);
                                Adventure.putExtra("category", "Adventure");
                                Adventure.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Adventure);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Thriller":
                                Intent Thriller = new Intent(activity, CategoryDetailsActivity.class);
                                Thriller.putExtra("category", "Thriller");
                                Thriller.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Thriller);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "War":
                                Intent War = new Intent(activity, CategoryDetailsActivity.class);
                                War.putExtra("category", "War");
                                War.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(War);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Fantasy":
                                Intent Fantasy = new Intent(activity, CategoryDetailsActivity.class);
                                Fantasy.putExtra("category", "Fantasy");
                                Fantasy.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Fantasy);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Sci-Fi":
                                Intent SciFi = new Intent(activity, CategoryDetailsActivity.class);
                                SciFi.putExtra("category", "Sci-Fi");
                                SciFi.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(SciFi);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Family":
                                Intent Family = new Intent(activity, CategoryDetailsActivity.class);
                                Family.putExtra("category", "Family");
                                Family.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Family);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Animation":
                                Intent Animation = new Intent(activity, CategoryDetailsActivity.class);
                                Animation.putExtra("category", "Animation");
                                Animation.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Animation);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "History":
                                Intent History = new Intent(activity, CategoryDetailsActivity.class);
                                History.putExtra("category", "History");
                                History.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(History);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Music":
                                Intent Music = new Intent(activity, CategoryDetailsActivity.class);
                                Music.putExtra("category", "Music");
                                Music.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Music);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Documentary":
                                Intent Documentary = new Intent(activity, CategoryDetailsActivity.class);
                                Documentary.putExtra("category", "Documentary");
                                Documentary.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Documentary);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Western":
                                Intent Western = new Intent(activity, CategoryDetailsActivity.class);
                                Western.putExtra("category", "Western");
                                Western.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Western);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Mystery":
                                Intent Mystery = new Intent(activity, CategoryDetailsActivity.class);
                                Mystery.putExtra("category", "Mystery");
                                Mystery.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Mystery);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Biography":
                                Intent Biography = new Intent(activity, CategoryDetailsActivity.class);
                                Biography.putExtra("category", "Biography");
                                Biography.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Biography);
                                binding.genre.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            default:
                                Intent more = new Intent(activity, CategoryDetailsActivity.class);
                                more.putExtra("category", "More");
                                more.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(more);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));

                        }
                        adNetwork.showInterstitialAd();
                    }
                });
                binding.genre1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (genre1) {
                            case "Comedy":
                                Intent intent = new Intent(activity, CategoryDetailsActivity.class);
                                intent.putExtra("category", "Comedy");
                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Drama":
                                Intent Drama = new Intent(activity, CategoryDetailsActivity.class);
                                Drama.putExtra("category", "Drama");
                                Drama.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Drama);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Romance":
                                Intent Romance = new Intent(activity, CategoryDetailsActivity.class);
                                Romance.putExtra("category", "Romance");
                                Romance.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Romance);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Korean":
                                Intent Korean = new Intent(activity, CategoryDetailsActivity.class);
                                Korean.putExtra("category", "Korean");
                                Korean.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Korean);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Crime":
                                Intent Crime = new Intent(activity, CategoryDetailsActivity.class);
                                Crime.putExtra("category", "Crime");
                                Crime.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Crime);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Chinese":
                                Intent Chinese = new Intent(activity, CategoryDetailsActivity.class);
                                Chinese.putExtra("category", "Chinese");
                                Chinese.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Chinese);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Horror":
                                Intent Horror = new Intent(activity, CategoryDetailsActivity.class);
                                Horror.putExtra("category", "Horror");
                                Horror.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Horror);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Action":
                                Intent Action = new Intent(activity, CategoryDetailsActivity.class);
                                Action.putExtra("category", "Action");
                                Action.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Action);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Adventure":
                                Intent Adventure = new Intent(activity, CategoryDetailsActivity.class);
                                Adventure.putExtra("category", "Adventure");
                                Adventure.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Adventure);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Thriller":
                                Intent Thriller = new Intent(activity, CategoryDetailsActivity.class);
                                Thriller.putExtra("category", "Thriller");
                                Thriller.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Thriller);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "War":
                                Intent War = new Intent(activity, CategoryDetailsActivity.class);
                                War.putExtra("category", "War");
                                War.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(War);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Fantasy":
                                Intent Fantasy = new Intent(activity, CategoryDetailsActivity.class);
                                Fantasy.putExtra("category", "Fantasy");
                                Fantasy.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Fantasy);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Sci-Fi":
                                Intent SciFi = new Intent(activity, CategoryDetailsActivity.class);
                                SciFi.putExtra("category", "Sci-Fi");
                                SciFi.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(SciFi);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Family":
                                Intent Family = new Intent(activity, CategoryDetailsActivity.class);
                                Family.putExtra("category", "Family");
                                Family.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Family);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Animation":
                                Intent Animation = new Intent(activity, CategoryDetailsActivity.class);
                                Animation.putExtra("category", "Animation");
                                Animation.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Animation);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "History":
                                Intent History = new Intent(activity, CategoryDetailsActivity.class);
                                History.putExtra("category", "History");
                                History.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(History);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Music":
                                Intent Music = new Intent(activity, CategoryDetailsActivity.class);
                                Music.putExtra("category", "Music");
                                Music.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Music);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Documentary":
                                Intent Documentary = new Intent(activity, CategoryDetailsActivity.class);
                                Documentary.putExtra("category", "Documentary");
                                Documentary.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Documentary);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Western":
                                Intent Western = new Intent(activity, CategoryDetailsActivity.class);
                                Western.putExtra("category", "Western");
                                Western.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Western);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Mystery":
                                Intent Mystery = new Intent(activity, CategoryDetailsActivity.class);
                                Mystery.putExtra("category", "Mystery");
                                Mystery.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Mystery);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Biography":
                                Intent Biography = new Intent(activity, CategoryDetailsActivity.class);
                                Biography.putExtra("category", "Biography");
                                Biography.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Biography);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            default:
                                Intent more = new Intent(activity, CategoryDetailsActivity.class);
                                more.putExtra("category", "More");
                                more.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(more);
                                binding.genre1.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre1.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                        }
                        adNetwork.showInterstitialAd();
                    }
                });
                binding.genre2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (genre2) {
                            case "Comedy":
                                Intent intent = new Intent(activity, CategoryDetailsActivity.class);
                                intent.putExtra("category", "Comedy");
                                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Drama":
                                Intent Drama = new Intent(activity, CategoryDetailsActivity.class);
                                Drama.putExtra("category", "Drama");
                                Drama.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Drama);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Romance":
                                Intent Romance = new Intent(activity, CategoryDetailsActivity.class);
                                Romance.putExtra("category", "Romance");
                                Romance.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Romance);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Korean":
                                Intent Korean = new Intent(activity, CategoryDetailsActivity.class);
                                Korean.putExtra("category", "Korean");
                                Korean.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Korean);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Crime":
                                Intent Crime = new Intent(activity, CategoryDetailsActivity.class);
                                Crime.putExtra("category", "Crime");
                                Crime.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Crime);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Chinese":
                                Intent Chinese = new Intent(activity, CategoryDetailsActivity.class);
                                Chinese.putExtra("category", "Chinese");
                                Chinese.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Chinese);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Horror":
                                Intent Horror = new Intent(activity, CategoryDetailsActivity.class);
                                Horror.putExtra("category", "Horror");
                                Horror.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Horror);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Action":
                                Intent Action = new Intent(activity, CategoryDetailsActivity.class);
                                Action.putExtra("category", "Action");
                                Action.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Action);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Adventure":
                                Intent Adventure = new Intent(activity, CategoryDetailsActivity.class);
                                Adventure.putExtra("category", "Adventure");
                                Adventure.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Adventure);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Thriller":
                                Intent Thriller = new Intent(activity, CategoryDetailsActivity.class);
                                Thriller.putExtra("category", "Thriller");
                                Thriller.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Thriller);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "War":
                                Intent War = new Intent(activity, CategoryDetailsActivity.class);
                                War.putExtra("category", "War");
                                War.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(War);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Fantasy":
                                Intent Fantasy = new Intent(activity, CategoryDetailsActivity.class);
                                Fantasy.putExtra("category", "Fantasy");
                                Fantasy.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Fantasy);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Sci-Fi":
                                Intent SciFi = new Intent(activity, CategoryDetailsActivity.class);
                                SciFi.putExtra("category", "Sci-Fi");
                                SciFi.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(SciFi);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Family":
                                Intent Family = new Intent(activity, CategoryDetailsActivity.class);
                                Family.putExtra("category", "Family");
                                Family.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Family);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Animation":
                                Intent Animation = new Intent(activity, CategoryDetailsActivity.class);
                                Animation.putExtra("category", "Animation");
                                Animation.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Animation);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "History":
                                Intent History = new Intent(activity, CategoryDetailsActivity.class);
                                History.putExtra("category", "History");
                                History.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(History);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Music":
                                Intent Music = new Intent(activity, CategoryDetailsActivity.class);
                                Music.putExtra("category", "Music");
                                Music.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Music);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Documentary":
                                Intent Documentary = new Intent(activity, CategoryDetailsActivity.class);
                                Documentary.putExtra("category", "Documentary");
                                Documentary.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Documentary);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Western":
                                Intent Western = new Intent(activity, CategoryDetailsActivity.class);
                                Western.putExtra("category", "Western");
                                Western.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Western);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Mystery":
                                Intent Mystery = new Intent(activity, CategoryDetailsActivity.class);
                                Mystery.putExtra("category", "Mystery");
                                Mystery.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Mystery);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            case "Biography":
                                Intent Biography = new Intent(activity, CategoryDetailsActivity.class);
                                Biography.putExtra("category", "Biography");
                                Biography.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(Biography);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                                break;
                            default:
                                Intent more = new Intent(activity, CategoryDetailsActivity.class);
                                more.putExtra("category", "More");
                                more.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                startActivity(more);
                                binding.genre2.setTextColor(getResources().getColor(R.color.colorWhite));
                                binding.genre2.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dc4036")));
                        }
                        adNetwork.showInterstitialAd();
                    }
                });

                //set movie image
                Picasso.get()
                        .load(model.getMovieImage())
                        .placeholder(R.drawable.ic_placeholder_dark)
                        .into(binding.movieImage);
                //1K and 1M likes logic
                int like = Integer.parseInt(likes);
                if (like >= 1000) {
                    binding.likes.setText((like / 1000) + "." + ((like % 1000) / 100) + "K");
                } else {
                    binding.likes.setText(likes);
                }
                if (like >= 1000000) {
                    binding.likes.setText((like / 1000000) + "." + ((like % 1000000) / 10000) + "M");
                }
                //1K and 1M views logic
                int view = Integer.parseInt(views);
                if (view >= 1000) {
                    binding.views.setText((view / 1000) + "." + ((view % 1000) / 100) + "K");
                } else {
                    binding.views.setText(views);
                }
                if (view >= 1000000) {
                    binding.views.setText((view / 1000000) + "." + ((view % 1000000) / 10000) + "M");
                }
                //1K and 1M comments logic
                int comment = Integer.parseInt(comments);
                if (comment >= 1000) {
                    binding.comment.setText((comment / 1000) + "." + ((comment % 1000) / 100) + "K");
                } else {
                    binding.comment.setText(comments);
                }
                if (comment >= 1000000) {
                    binding.comment.setText((comment / 1000000) + "." + ((comment % 1000000) / 10000) + "M");
                }
                //1K and 1M shares logic
                int share = Integer.parseInt(shares);
                if (share >= 1000) {
                    binding.share.setText((share / 1000) + "." + ((share % 1000) / 100) + "K");
                } else {
                    binding.share.setText(shares);
                }
                if (share >= 1000000) {
                    binding.share.setText((share / 1000000) + "." + ((share % 1000000) / 10000) + "M");
                }

                //imdb info data
                binding.moreInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //network check
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                            Intent intent = new Intent(activity, WebviewActivity.class);
                            intent.putExtra("IMDbLink", imdbLink);
                            intent.putExtra("movieName", movieName);
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            networkChecks.noConnectionDialog();
                        }
                    }
                });
                //download btn data
                binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //network check
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                            //click check
                            clickBtn = "download";
                            //showed popup
                            ShowDownloadPopup();

                        } else {
                            networkChecks.noConnectionDialog();
                        }
                    }
                });
                //trailer btn data
                binding.trailerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //network check
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                            Intent intent = new Intent(activity, YoutubePlayerActivity.class);
                            intent.putExtra("trailerLink", trailerLink);
                            intent.putExtra("movieName", movieName);
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            networkChecks.noConnectionDialog();
                        }
                    }
                });
                //play button
                binding.playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //network check
                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                            //click check
                            clickBtn = "play";
                            //showed popup
                            ShowPopup();
                        } else {
                            networkChecks.noConnectionDialog();
                        }
                    }
                });
                //related movies
                binding.relatedMovies.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        binding.relatedMovies.setTextColor(getResources().getColor(R.color.colorWhite));
                        Intent intent = new Intent(activity, RelatedMoviesActivity.class);
                        intent.putExtra("movieName", movieName);
                        intent.putExtra("year", movieYear);
                        intent.putExtra("genre", genre);
                        intent.putExtra("genre1", genre1);
                        intent.putExtra("genre2", genre2);
                        intent.putExtra("keywords", keywords);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        /*if (rewardedAd.isReady()){
                            rewardedAd.showAd();
                        }*/
                        adNetwork.showInterstitialAd();
                    }
                });
                //movies likes data
                FirebaseDatabase.getInstance().getReference()
                        .child("Movies")
                        .child(postId)
                        .child("likes")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    binding.countLikes.setImageResource(R.drawable.ic_like_icon_green);
                                } else {
                                    binding.countLikes.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //network check
                                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Movies")
                                                        .child(postId)
                                                        .child("likes")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Movies")
                                                                .child(postId)
                                                                .child("likesCount")
                                                                .setValue(model.getLikesCount() + 1)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        binding.countLikes.setImageResource(R.drawable.ic_like_icon_green);
                                                                    }
                                                                });
                                                    }
                                                });
                                            } else {
                                                networkChecks.noConnectionDialog();
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                //bookmarks
                FirebaseDatabase.getInstance().getReference()
                        .child("Movies")
                        .child(postId)
                        .child("Save")
                        .child(FirebaseAuth.getInstance().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    binding.saveImage.setImageResource(R.drawable.ic_bookmark_icon_active);
                                    binding.saveText.setText("Saved");

                                } else {
                                    binding.saveImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //network check
                                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Movies")
                                                        .child(postId)
                                                        .child("Save")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .setValue(true)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                database.getReference()
                                                                        .child("Movies")
                                                                        .child(postId)
                                                                        .child("saved")
                                                                        .setValue(FirebaseAuth.getInstance().getUid())
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                binding.saveImage.setImageResource(R.drawable.ic_bookmark_icon_active);
                                                                                binding.saveText.setText("Saved");
                                                                                toastText.setText("✔ Added to favorites");
                                                                                toast.show();
                                                                            }
                                                                        });
                                                            }
                                                        });


                                            } else {
                                                networkChecks.noConnectionDialog();
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //users comment data
        binding.commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    if (commentValidation()) {
                        CommentModel comment = new CommentModel();
                        comment.setCommentBody(binding.commentET.getText().toString());
                        comment.setCommentAt(new Date().getTime());
                        comment.setCommentedBy(FirebaseAuth.getInstance().getUid());
                        //get comment data
                        database.getReference()
                                .child("Movies")
                                .child(postId)
                                .child("comments")
                                .push()
                                .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {
                                database.getReference()
                                        .child("Movies")
                                        .child(postId)
                                        .child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int commentCount = 0;
                                        if (snapshot.exists()) {
                                            commentCount = snapshot.getValue(Integer.class);
                                        }
                                        database.getReference()
                                                .child("Movies")
                                                .child(postId)
                                                .child("commentCount")
                                                .setValue(commentCount + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(@NonNull Void unused) {
                                                //hide keyboard
                                                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                                //set the comment value blank
                                                binding.commentET.setText("");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });

                    }

                } else {
                    networkChecks.noConnectionDialog();
                }
            }
        });

        //get commented data
        CommentAdapter adapter = new CommentAdapter(this, list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.commentRv.setLayoutManager(layoutManager);
        binding.commentRv.setAdapter(adapter);

        //get database data
        database.getReference()
                .child("Movies")
                .child(postId)
                .child("comments")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CommentModel comment = dataSnapshot.getValue(CommentModel.class);
                            assert comment != null;
                            comment.setPostID(postId);
                            comment.setCommentID(dataSnapshot.getKey());
                            list.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //swipe refresh layout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onRefresh() {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                    commentShimmer.showShimmerAdapter();
                    database.getReference()
                            .child("Movies")
                            .child(postId)
                            .child("comments")
                            .addValueEventListener(new ValueEventListener() {
                                @SuppressLint("NotifyDataSetChanged")
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    list.clear();
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        CommentModel comment = dataSnapshot.getValue(CommentModel.class);
                                        assert comment != null;
                                        comment.setPostID(postId);
                                        comment.setCommentID(dataSnapshot.getKey());
                                        list.add(comment);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    adapter.notifyDataSetChanged();

                } else {
                    networkChecks.noConnectionDialog();
                }
                swipeRefreshLayout.setRefreshing(false);
                commentShimmer.hideShimmerAdapter();

            }
        });




    }//ends of onCreate

    private void ShowDownloadPopup() {

        ImageView btnCancel;
        TextView btn_1, btn_2, btn_3, btn_4;
        serverDialog.setContentView(R.layout.custom_server_layout);

        //find id's
        btnCancel = serverDialog.findViewById(R.id.btnCancle);
        btn_1 = serverDialog.findViewById(R.id.btnServer_1);
        btn_2 = serverDialog.findViewById(R.id.btnServer_2);
        btn_3 = serverDialog.findViewById(R.id.btnServer_3);
        btn_4 = serverDialog.findViewById(R.id.btnServer_4);

        //server 1
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 1;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_1);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_1);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                }
            }
        });
        //server 2
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 2;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_2);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_2);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                }
            }
        });
        //server 3
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 3;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_3);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_3);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                }
            }
        });
        //server 4
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 4;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_4);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_4);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                }
            }
        });

        //cancel btn
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverDialog.dismiss();
            }
        });
        serverDialog.setCancelable(false);
        serverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        serverDialog.show();
    }

    private void ShowPopup() {

        ImageView btnCancel;
        TextView btn_1, btn_2, btn_3, btn_4;
        serverDialog.setContentView(R.layout.custom_server_layout);

        //find id's
        btnCancel = serverDialog.findViewById(R.id.btnCancle);
        btn_1 = serverDialog.findViewById(R.id.btnServer_1);
        btn_2 = serverDialog.findViewById(R.id.btnServer_2);
        btn_3 = serverDialog.findViewById(R.id.btnServer_3);
        btn_4 = serverDialog.findViewById(R.id.btnServer_4);

        //server 1
        btn_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 1;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_1);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_1);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                }
            }
        });
        //server 2
        btn_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 2;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_2);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_2);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                }
            }
        });
        //server 3
        btn_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 3;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_3);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_3);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                }
            }
        });
        //server 4
        btn_4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverClick = 4;
                //show rewarded ad
                if (UiConfig.REWARDED__AD_VISIBILITY) {
                    if (UnityAds.isInitialized()) {
                        DisplayUnityRewardedAd();
                    }else {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_4);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                    }
                } else {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_4);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                }
            }
        });

        //cancel btn
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverDialog.dismiss();
            }
        });
        serverDialog.setCancelable(false);
        serverDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        serverDialog.show();
    }

    //comment button empty validation
    public boolean commentValidation() {
        String comment = binding.commentET.getText().toString();
        if (comment.isEmpty()) {
            binding.commentET.setError("Text is required");
            return false;
        } else {
            binding.commentET.setError(null);
            return true;
        }
    }

    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //share button click
        //get post data
        if (item.getItemId() == R.id.shareQA) {
            addShareData();
            return true;
        } else {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addShareData() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch HD Movies 2022");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Movie Name: " + movieName + "\n" +
                "Genre: " + genre + "/" + genre1 + "/" + genre2 + "\n" +
                "Rating: " + movieRating + "\n" +
                "Descriptions: " + movieDescription + "\n\n" +
                "IMDb: " + imdbLink + "\n\n" +
                "Be, happy with your love & watch movies together.\n\n" +
                "DOWNLOAD NOW: \n" +
                "https://play.google.com/store/apps/details?id=" +
                BuildConfig.APPLICATION_ID);
        shareIntent.setType("text/plain");
        startActivity(shareIntent);
        //fetch firebase database
        //first get the share count values
        //when user click 4time then count 1 share
        if (counter == 1) {
            database.getReference()
                    .child("Movies")
                    .child(postId)
                    .child("shareCount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int shareCount = 0;
                    if (snapshot.exists()) {
                        shareCount = snapshot.getValue(Integer.class);
                    }
                    database.getReference()
                            .child("Movies")
                            .child(postId)
                            .child("shareCount")
                            .setValue(shareCount + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void unused) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            counter++;
            if (counter == UiConfig.SHARE_CLICK_COUNT) {
                database.getReference()
                        .child("Movies")
                        .child(postId)
                        .child("shareCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int shareCount = 0;
                        if (snapshot.exists()) {
                            shareCount = snapshot.getValue(Integer.class);
                        }
                        database.getReference()
                                .child("Movies")
                                .child(postId)
                                .child("shareCount")
                                .setValue(shareCount + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                counter = 1;
            }
        } else {
            counter = 1;
        }

        //one user count one share
        /*database.getReference()
                .child("Movies")
                .child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        MoviesModel model = snapshot.getValue(MoviesModel.class);
                        assert model != null;
                        String shares = model.getShareCount() + "";
                        //then get the shares boolean value
                        database.getReference()
                                .child("Movies")
                                .child(postId)
                                .child("shares")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //if user shares before
                                        if (snapshot.exists()) {
                                            binding.share.setText(shares);
                                        }
                                        //else new user share
                                        else {
                                            database.getReference()
                                                    .child("Movies")
                                                    .child(postId)
                                                    .child("shares")
                                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                                    .setValue(true)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            database.getReference()
                                                                    .child("Movies")
                                                                    .child(postId)
                                                                    .child("shareCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    int shareCount = 0;
                                                                    if (snapshot.exists()) {
                                                                        shareCount = snapshot.getValue(Integer.class);
                                                                    }
                                                                    database.getReference()
                                                                            .child("Movies")
                                                                            .child(postId)
                                                                            .child("shareCount")
                                                                            .setValue(shareCount + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(@NonNull Void unused) {

                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });*/
    }

    //load unity rewarded ad
    IUnityAdsLoadListener iUnityAdsLoadListener = new IUnityAdsLoadListener() {
        @Override
        public void onUnityAdsAdLoaded(String s) {
            UnityAds.show(activity, unityRewardedAd,new UnityAdsShowOptions(), iUnityAdsShowListener);
        }

        @Override
        public void onUnityAdsFailedToLoad(String s, UnityAds.UnityAdsLoadError unityAdsLoadError, String s1) {

        }
    };

    //unity rewarded ads
    IUnityAdsShowListener iUnityAdsShowListener = new IUnityAdsShowListener() {
        @Override
        public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
            // Rewarded ad was displayed and user should receive the reward
            if (clickBtn.equals("play")) {
                if (serverClick == 1) {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_1);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                } else if (serverClick == 2) {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_2);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                } else if (serverClick == 3) {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_3);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                } else if (serverClick == 4) {
                    Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                    intent.putExtra("serverUrl", server_4);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                }
            } else {
                if (serverClick == 1) {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_1);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                } else if (serverClick == 2) {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_2);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                } else if (serverClick == 3) {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_3);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                } else if (serverClick == 4) {
                    Intent intent = new Intent(activity, ActivityVideoDownload.class);
                    intent.putExtra("downloadUrl", server_4);
                    intent.putExtra("movieName", movieName);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                }
            }
        }

        @Override
        public void onUnityAdsShowStart(String s) {

        }

        @Override
        public void onUnityAdsShowClick(String s) {

        }

        @Override
        public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
            if (unityAdsShowCompletionState.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)){
                // Rewarded ad was displayed and user should receive the reward
                if (clickBtn.equals("play")) {
                    if (serverClick == 1) {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_1);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                    } else if (serverClick == 2) {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_2);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                    } else if (serverClick == 3) {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_3);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                    } else if (serverClick == 4) {
                        Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                        intent.putExtra("serverUrl", server_4);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                    }
                } else {
                    if (serverClick == 1) {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_1);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
                    } else if (serverClick == 2) {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_2);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
                    } else if (serverClick == 3) {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_3);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
                    } else if (serverClick == 4) {
                        Intent intent = new Intent(activity, ActivityVideoDownload.class);
                        intent.putExtra("downloadUrl", server_4);
                        intent.putExtra("movieName", movieName);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
                    }
                }
            }else {
                Toast.makeText(activity, "Watch Full Ad", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void DisplayUnityRewardedAd(){
        UnityAds.load(unityRewardedAd,iUnityAdsLoadListener);
    }

    // MAX Ad Listener
    @Override
    public void onAdLoaded(final MaxAd maxAd) {
        // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'

        // Reset retry attempt
        retryAttempt = 0;
    }

    @Override
    public void onAdLoadFailed(final String adUnitId, final MaxError error) {
        // Rewarded ad failed to load
        // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

        retryAttempt++;
        long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rewardedAd.loadAd();
            }
        }, delayMillis);
    }

    @Override
    public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {
        // Rewarded ad failed to display. We recommend loading the next ad
        rewardedAd.loadAd();
    }

    @Override
    public void onAdDisplayed(final MaxAd maxAd) {
    }

    @Override
    public void onAdClicked(final MaxAd maxAd) {
    }

    @Override
    public void onAdHidden(final MaxAd maxAd) {
        // rewarded ad is hidden. Pre-load the next ad
        rewardedAd.loadAd();
    }

    @Override
    public void onRewardedVideoStarted(final MaxAd maxAd) {
    }

    @Override
    public void onRewardedVideoCompleted(final MaxAd maxAd) {
    }

    @Override
    public void onUserRewarded(final MaxAd maxAd, final MaxReward maxReward) {
        // Rewarded ad was displayed and user should receive the reward
        if (clickBtn.equals("play")) {
            if (serverClick == 1) {
                Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                intent.putExtra("serverUrl", server_1);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
            } else if (serverClick == 2) {
                Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                intent.putExtra("serverUrl", server_2);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
            } else if (serverClick == 3) {
                Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                intent.putExtra("serverUrl", server_3);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
            } else if (serverClick == 4) {
                Intent intent = new Intent(activity, ActivityVideoPlayer.class);
                intent.putExtra("serverUrl", server_4);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
            }
        } else {
            if (serverClick == 1) {
                Intent intent = new Intent(activity, ActivityVideoDownload.class);
                intent.putExtra("downloadUrl", server_1);
                intent.putExtra("movieName", movieName);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_1)));
            } else if (serverClick == 2) {
                Intent intent = new Intent(activity, ActivityVideoDownload.class);
                intent.putExtra("downloadUrl", server_2);
                intent.putExtra("movieName", movieName);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_2)));
            } else if (serverClick == 3) {
                Intent intent = new Intent(activity, ActivityVideoDownload.class);
                intent.putExtra("downloadUrl", server_3);
                intent.putExtra("movieName", movieName);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_3)));
            } else if (serverClick == 4) {
                Intent intent = new Intent(activity, ActivityVideoDownload.class);
                intent.putExtra("downloadUrl", server_4);
                intent.putExtra("movieName", movieName);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(server_4)));
            }
        }
    }
}