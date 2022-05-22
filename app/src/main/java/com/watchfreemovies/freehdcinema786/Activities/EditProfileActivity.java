package com.watchfreemovies.freehdcinema786.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.ads.MaxAdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.databinding.ActivityEditProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ProgressDialog dialog;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    private TextInputLayout professionInput;
    private TextInputLayout bioInput;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        //find id
        professionInput = findViewById(R.id.text_input_profession);
        bioInput = findViewById(R.id.text_input_bio);
        //check pro status
        ImageView proBadge = findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }

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

        //instance
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        //progress dialog
        dialog = new ProgressDialog(EditProfileActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Update Profile");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //toolbar
        setSupportActionBar(binding.toolbar2);
        EditProfileActivity.this.setTitle("Edit Profile");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //get database value
        //fetch update image
        database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            assert userModel != null;
                            Picasso.get()
                                    .load(userModel.getProfile())
                                    .placeholder(R.drawable.ic_profile_default_image)
                                    .into(binding.profileImage);
                            binding.userName.setText(userModel.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //if existing user update data
        database.getReference().child("UserData")
                .child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            assert userModel != null;
                            String userName = userModel.getUserName();
                            String profession = userModel.getProfession();
                            String bio = userModel.getUserBio();
                            String fb = userModel.getFbLink();
                            String insta = userModel.getInstaLink();
                            String github = userModel.getGithubLink();
                            String linkedin = userModel.getLinkedinLink();
                            String twitter = userModel.getTwitterLink();
                            //set profession,bio and link
                            binding.usernameEdit.setText(userName);
                            binding.professionEdit.setText(profession);
                            binding.bioEditText.setText(bio);
                            binding.fblinkEditText.setText(fb);
                            binding.instalinkEditText.setText(insta);
                            binding.githublinkEditText.setText(github);
                            binding.linkedinlinkEditText.setText(linkedin);
                            binding.twitterlinkEditText.setText(twitter);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(EditProfileActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    if (professionValidation() && bioValidation()) {
                        dialog.show();
                        String userName = binding.usernameEdit.getText().toString();
                        String profession = binding.professionEdit.getText().toString();
                        String fbLink = binding.fblinkEditText.getText().toString();
                        String instaLink = binding.instalinkEditText.getText().toString();
                        String githubLink = binding.githublinkEditText.getText().toString();
                        String linkinLink = binding.linkedinlinkEditText.getText().toString();
                        String twitterLink = binding.twitterlinkEditText.getText().toString();
                        String userBio = binding.bioEditText.getText().toString();

                        database.getReference().child("UserData")
                                .child(FirebaseAuth.getInstance().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        UserModel userModel = snapshot.getValue(UserModel.class);
                                        String email = userModel.getEmail();
                                        String password = userModel.getPassword();
                                        String profileImage = userModel.getProfile();
                                        String coverImage = userModel.getCoverPhoto();


                                        UserModel model = new UserModel(userName,email,password,coverImage,profileImage,profession,fbLink,instaLink,githubLink,linkinLink,twitterLink,userBio);
                                        //send data to database
                                        database.getReference().child("UserData")
                                                .child(auth.getUid())
                                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(@NonNull Void unused) {
                                                dialog.dismiss();
                                                finish();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                } else {
                    noConnectionDialog();
                }
            }
        });


    }//onCreate Ends

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

    public boolean professionValidation() {
        String sendTextInput = Objects.requireNonNull(professionInput.getEditText()).getText().toString().trim();
        if (sendTextInput.isEmpty()) {
            professionInput.setError("Profession is required");
            return false;
        } else {
            professionInput.setError(null);
            return true;
        }
    }

    public boolean bioValidation() {
        String sendTextInput = Objects.requireNonNull(bioInput.getEditText()).getText().toString().trim();
        if (sendTextInput.isEmpty()) {
            bioInput.setError("About is required");
            return false;
        } else {
            bioInput.setError(null);
            return true;
        }
    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}