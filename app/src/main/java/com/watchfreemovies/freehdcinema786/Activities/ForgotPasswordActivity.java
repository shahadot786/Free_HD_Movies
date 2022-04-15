package com.watchfreemovies.freehdcinema786.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.databinding.ActivityForgotPasswordBinding;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {

    //binding
    ActivityForgotPasswordBinding binding;
    //firebase code
    FirebaseAuth auth;
    TextInputLayout textInputLayoutEmail;
    EditText emailReset;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        //firebase instance
        auth = FirebaseAuth.getInstance();
        //find id
        textInputLayoutEmail = findViewById(R.id.text_input_email);
        emailReset = findViewById(R.id.editTextEmail);
        binding.forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(ForgotPasswordActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    //check first email validation
                    if (!validateEmail()) {
                        return;
                    }
                    //hide keyboard
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    String email = emailReset.getText().toString().trim();
                    //other codes
                    if (TextUtils.isEmpty(email)) {
                        toastText.setText(R.string.enter_register_email);
                        toast.show();
                        return;
                    }
                    auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        toastText.setText(R.string.net_connection_check);
                                        toast.show();
                                        startActivity(new Intent(ForgotPasswordActivity.this, SignInActivity.class));
                                    } else {
                                        toastText.setText(R.string.correct_email);
                                        toast.show();
                                    }
                                }
                            });
                } else {
                    noConnectionDialog();
                }
            }
        });

        binding.goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                startActivity(intent);
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
    //others methods
    //email Validation
    private boolean validateEmail() {
        String emailInput = Objects.requireNonNull(textInputLayoutEmail.getEditText()).getText().toString().trim();
        if (emailInput.isEmpty()) {
            textInputLayoutEmail.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            textInputLayoutEmail.setError("Please enter a valid email address");
            return false;
        } else {
            textInputLayoutEmail.setError(null);
            //textInputEmail.setErrorEnabled(false);
            return true;
        }

    }
}