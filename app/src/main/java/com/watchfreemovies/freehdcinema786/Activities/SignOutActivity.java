package com.watchfreemovies.freehdcinema786.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.ads.MaxAdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.Utils.AdNetwork;
import com.watchfreemovies.freehdcinema786.databinding.ActivitySignOutBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class SignOutActivity extends AppCompatActivity {

    ActivitySignOutBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //instance of firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        //ad initialization
        adNetwork = new AdNetwork(this);
        MaxAdView mRecAd = findViewById(R.id.mRec);
        adNetwork.loadMrecAd();
        //ads disabled code
        //check premium
        if (UiConfig.BANNER_AD_VISIBILITY) {
            mRecAd.setVisibility(View.VISIBLE);
            mRecAd.startAutoRefresh();
        } else {
            mRecAd.setVisibility(View.GONE);
            mRecAd.stopAutoRefresh();
        }
        //toolbar
        setSupportActionBar(binding.toolbar4);
        SignOutActivity.this.setTitle("Sign Out");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get database value
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
                                    .into(binding.profileImage);
                            binding.userName.setText(user.getUserName());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //cancel button
        binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(SignOutActivity.this, SignInActivity.class);
                startActivity(intent);
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