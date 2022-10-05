package com.hdmovies.onlinecinema.Activities;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.applovin.mediation.ads.MaxAdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hdmovies.onlinecinema.Config.UiConfig;
import com.hdmovies.onlinecinema.Model.FeedbackModel;
import com.hdmovies.onlinecinema.Model.UserModel;
import com.hdmovies.onlinecinema.R;
import com.hdmovies.onlinecinema.Utils.AdNetwork;
import com.hdmovies.onlinecinema.Utils.NetworkChecks;
import com.hdmovies.onlinecinema.databinding.ActivityFeedBackBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class FeedBackActivity extends AppCompatActivity {

    ActivityFeedBackBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    String userName;
    private TextInputLayout sendInput;
    private TextInputLayout emailInput;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBackBinding.inflate(getLayoutInflater());
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
        sendInput = findViewById(R.id.feedback_input_text);
        emailInput = findViewById(R.id.emailFeedInput);
        //pro status
        ImageView proBadge = findViewById(R.id.proBadge);
        if (UiConfig.PRO_VISIBILITY_STATUS_SHOW) {
            proBadge.setVisibility(View.GONE);
        } else {
            proBadge.setVisibility(View.VISIBLE);
        }

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
        //instance
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        //toolbar
        setSupportActionBar(binding.toolbar2);
        FeedBackActivity.this.setTitle("Feedback");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //test
        /*binding.testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uniqueID = UUID.randomUUID().toString();
                database.getReference()
                        .child("UserData")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .child("uniqueID")
                        .setValue(uniqueID);
            }
        });*/

        //get database value
        //google sign in name and email data fetch with image
        database.getReference().child("UserData").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel user = snapshot.getValue(UserModel.class);
                            assert user != null;
                            userName = user.getUserName();
                            Picasso.get()
                                    .load(user.getProfile())
                                    .placeholder(R.drawable.ic_profile_default_image)
                                    .into(binding.profileImage);
                            binding.emailFeedEdit.setText(user.getEmail());
                            binding.userName.setText(userName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //send button click codes
        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(FeedBackActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    //check first if the fields are empty
                    if (sendMessageBtnValidation() && sendEmailBtnValidation()) {
                        FeedbackModel feedbackModel = new FeedbackModel();
                        feedbackModel.setFeedbackDescription(Objects.requireNonNull(binding.feedbackMessageEditText.getText()).toString());
                        feedbackModel.setFeedEmail(Objects.requireNonNull(binding.emailFeedEdit.getText()).toString());
                        feedbackModel.setUserName(userName);
                        feedbackModel.setFeedbackBy(auth.getUid());
                        database.getReference().child("Feedback")
                                .push()
                                .setValue(feedbackModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void unused) {

                            }
                        });
                        //after send feedback show a message
                        AlertDialog.Builder dialog = new AlertDialog.Builder(FeedBackActivity.this, R.style.AppCompatAlertDialogStyle);
                        dialog.setTitle(R.string.dialog_feedback);
                        dialog.setMessage(R.string.dialog_feed_message);
                        dialog.setPositiveButton(R.string.dialog_ok, ((dialogInterface, i) -> finish()));
                        dialog.show();

                    }

                } else {
                    networkChecks.noConnectionDialog();
                }
            }
        });

    }//end of onCreate

    //other codes
    //send button message validation
    public boolean sendMessageBtnValidation() {
        String sendTextInput = Objects.requireNonNull(sendInput.getEditText()).getText().toString().trim();
        if (sendTextInput.isEmpty()) {
            sendInput.setError("Message is required");
            return false;
        } else if (sendTextInput.length() > 2048) {
            sendInput.setError("Message is too long, tell us shortly within 2048 Letters.");
            return false;
        } else {
            sendInput.setError(null);
            return true;
        }
    }

    //sent button email validation
    public boolean sendEmailBtnValidation() {
        String sendEmailInput = Objects.requireNonNull(emailInput.getEditText()).getText().toString().trim();
        if (sendEmailInput.isEmpty()) {
            emailInput.setError("Email is required.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(sendEmailInput).matches()) {
            emailInput.setError("Please enter a valid email address");
            return false;
        } else {
            emailInput.setError(null);
            emailInput.setErrorEnabled(false);
            return true;
        }
    }

    //option menu item select
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(FeedBackActivity.this, MainActivity.class);
        startActivity(intent);
    }
}