package com.watchfreemovies.freehdcinema786.Activities;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.applovin.mediation.ads.MaxAdView;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Adapter.CommentAdapter;
import com.watchfreemovies.freehdcinema786.BuildConfig;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.CommentModel;
import com.watchfreemovies.freehdcinema786.Model.FeedsModel;
import com.watchfreemovies.freehdcinema786.Model.NotificationsModel;
import com.watchfreemovies.freehdcinema786.Model.ReportsModel;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.databinding.ActivityFeedsDetailsBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class FeedsDetailsActivity extends AppCompatActivity {

    ActivityFeedsDetailsBinding binding;
    ProgressDialog dialog;
    Intent intent;
    String postId, postedBy;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<CommentModel> list = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    ShimmerRecyclerView commentShimmer;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    String postDescriptions;
    int counter = 1;
    String commentedText;
    AdNetwork adNetwork;
    Dialog reportDialog;
    TextView btnCancel,btnOk;
    RadioGroup radioGroup;
    RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedsDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        reportDialog = new Dialog(this);

        //swipe refresh
        swipeRefreshLayout = findViewById(R.id.swipeRefreshComment);
        commentShimmer = findViewById(R.id.commentRv);

        //get data by intent
        intent = getIntent();
        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        //database instance
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        //progress dialog
        dialog = new ProgressDialog(FeedsDetailsActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Report Submitting");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        //network check
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FeedsDetailsActivity.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            toastText.setText(R.string.network_connected);
        } else {
            toastText.setText(R.string.no_connection_text);
            toast.show();
        }
        //user data
        database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel user = snapshot.getValue(UserModel.class);
                            assert user != null;
                            binding.report.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //network check
                                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FeedsDetailsActivity.CONNECTIVITY_SERVICE);
                                    if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                                        //we are connected to a network
                                        //report data
                                        reportDialog.setContentView(R.layout.custom_feeds_report_layout);
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
                                                model.setReportedBy(FirebaseAuth.getInstance().getUid());
                                                //database code
                                                database.getReference()
                                                        .child("Reports")
                                                        .child("Feeds")
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
                                        noConnectionDialog();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //get database value
        database.getReference()
                .child("Feeds")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FeedsModel model = snapshot.getValue(FeedsModel.class);
                //for share data
                assert model != null;
                postDescriptions = model.getPostDescription();

                //get id's data
                String time = TimeAgo.using(model.getPostedAt());
                String likes = model.getPostLikes() + "";
                String views = model.getPostViews() + "";
                String comments = model.getCommentCount() + "";
                String shares = model.getShareCount() + "";

                //set dynamic toolbar text
                if (!postDescriptions.isEmpty()) {
                    //toolbar
                    setSupportActionBar(binding.toolbar2);
                    FeedsDetailsActivity.this.setTitle(postDescriptions);
                    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                } else {
                    //toolbar
                    setSupportActionBar(binding.toolbar2);
                    FeedsDetailsActivity.this.setTitle("Post Details");
                    Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
                }

                //set id's data
                binding.time.setText(time);
                //check post description are empty
                String postDescriptions = model.getPostDescription();
                if (postDescriptions.equals("")) {
                    binding.postDescription.setVisibility(View.GONE);
                } else {
                    binding.postDescription.setText(model.getPostDescription());
                    binding.postDescription.setVisibility(View.VISIBLE);
                }
                //get image
                Picasso.get()
                        .load(model.getPostImage())
                        .placeholder(R.drawable.ic_placeholder_dark)
                        .into(binding.posImage);

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

                //post likes data
                FirebaseDatabase.getInstance().getReference()
                        .child("Feeds")
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
                                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FeedsDetailsActivity.CONNECTIVITY_SERVICE);
                                            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Feeds")
                                                        .child(postId)
                                                        .child("likes")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Feeds")
                                                                .child(postId)
                                                                .child("postLikes")
                                                                .setValue(model.getPostLikes() + 1)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        binding.countLikes.setImageResource(R.drawable.ic_like_icon_green);
                                                                        //for notification
                                                                        database.getReference()
                                                                                .child("Feeds")
                                                                                .child(postId)
                                                                                .addValueEventListener(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                        //notifications
                                                                                        NotificationsModel notifications = new NotificationsModel();
                                                                                        notifications.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                                        notifications.setNotificationAt(new Date().getTime());
                                                                                        notifications.setPostId(postId);
                                                                                        notifications.setPostedBy(postedBy);
                                                                                        notifications.setType("likes");

                                                                                        FirebaseDatabase.getInstance().getReference()
                                                                                                .child("Notifications")
                                                                                                .child(postedBy)
                                                                                                .push()
                                                                                                .setValue(notifications).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                                });
                                            } else {
                                                noConnectionDialog();
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
                        .child("Feeds")
                        .child(postId)
                        .child("Save")
                        .child(FirebaseAuth.getInstance().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    binding.savePost.setImageResource(R.drawable.ic_bookmark_icon_active);

                                } else {
                                    binding.savePost.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //network check
                                            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                                            if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

                                                FirebaseDatabase.getInstance().getReference()
                                                        .child("Feeds")
                                                        .child(postId)
                                                        .child("Save")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .setValue(true)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                database.getReference()
                                                                        .child("Feeds")
                                                                        .child(postId)
                                                                        .child("saved")
                                                                        .setValue(FirebaseAuth.getInstance().getUid())
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                binding.savePost.setImageResource(R.drawable.ic_bookmark_icon_active);
                                                                                toastText.setText("✔ Post Saved");
                                                                                toast.show();
                                                                            }
                                                                        });
                                                            }
                                                        });


                                            } else {
                                                noConnectionDialog();
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

        //get user data
        database.getReference()
                .child("UserData")
                .child(postedBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModel user = snapshot.getValue(UserModel.class);
                assert user != null;
                Picasso.get()
                        .load(user.getProfile())
                        .placeholder(R.drawable.ic_profile_default_image)
                        .into(binding.profileImage);
                binding.userName.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //userProfileView click data
        binding.userProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeedsDetailsActivity.this, UserProfilesActivity.class);
                intent.putExtra("postedBy", postedBy);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //users comment data
        binding.commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FeedsDetailsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //comment data
                    if (commentValidation()) {
                        CommentModel comment = new CommentModel();
                        comment.setCommentBody(binding.commentET.getText().toString());
                        comment.setCommentAt(new Date().getTime());
                        comment.setCommentedBy(FirebaseAuth.getInstance().getUid());

                        //get commented text for notifications
                        commentedText = binding.commentET.getText().toString();
                        //get comment data
                        database.getReference()
                                .child("Feeds")
                                .child(postId)
                                .child("comments")
                                .push()
                                .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {
                                database.getReference()
                                        .child("Feeds")
                                        .child(postId)
                                        .child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        int commentCount = 0;
                                        if (snapshot.exists()) {
                                            commentCount = snapshot.getValue(Integer.class);
                                        }
                                        database.getReference()
                                                .child("Feeds")
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
                                                //for notification
                                                /*database.getReference()
                                                        .child("Feeds")
                                                        .child(postId)
                                                        .addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                //notifications
                                                                NotificationsModel notifications = new NotificationsModel();
                                                                notifications.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                notifications.setNotificationAt(new Date().getTime());
                                                                notifications.setPostId(postId);
                                                                notifications.setPostedBy(postedBy);
                                                                notifications.setCommentedText(commentedText);
                                                                notifications.setType("comments");

                                                                FirebaseDatabase.getInstance().getReference()
                                                                        .child("Notifications")
                                                                        .child(postedBy)
                                                                        .push()
                                                                        .setValue(notifications).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(@NonNull Void unused) {
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });*/
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
                   noConnectionDialog();
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
                .child("Feeds")
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
                            comment.setPostedBy(postedBy);
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
                            .child("Feeds")
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
                                        comment.setPostedBy(postedBy);
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
                    noConnectionDialog();
                }
                swipeRefreshLayout.setRefreshing(false);
                commentShimmer.hideShimmerAdapter();

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

    //share data
    private void addShareData() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch HD Movies 2022");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Post Descriptions: " + postDescriptions + "\n" +
                "Be, happy with your love & watch movies & Tv-Series together.\n\n" +
                "Watch full post: \n" +
                "https://play.google.com/store/apps/details?id=" +
                BuildConfig.APPLICATION_ID);
        shareIntent.setType("text/plain");
        startActivity(shareIntent);
        //fetch firebase database
        //first get the share count values
        //when user click 2time then count 1 share
        if (counter == 1) {
            database.getReference()
                    .child("Feeds")
                    .child(postId)
                    .child("shareCount").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int shareCount = 0;
                    if (snapshot.exists()) {
                        shareCount = snapshot.getValue(Integer.class);
                    }
                    database.getReference()
                            .child("Feeds")
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
                        .child("Feeds")
                        .child(postId)
                        .child("shareCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int shareCount = 0;
                        if (snapshot.exists()) {
                            shareCount = snapshot.getValue(Integer.class);
                        }
                        database.getReference()
                                .child("Feeds")
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

}