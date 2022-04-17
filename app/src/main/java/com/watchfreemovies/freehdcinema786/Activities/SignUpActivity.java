package com.watchfreemovies.freehdcinema786.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.watchfreemovies.freehdcinema786.Config.UiConfig;
import com.watchfreemovies.freehdcinema786.Model.UserModel;
import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.databinding.ActivitySignUpBinding;

import java.util.Objects;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    //password validation patterns
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    //"(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z-0-9-@#$%^!~*&+=])" + //any letter or number or special
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,15}" +               //at least 6 characters
                    "$");

    ActivitySignUpBinding binding;//binding
    ProgressDialog dialog;
    //firebase code
    FirebaseAuth auth;
    FirebaseDatabase database;
    LayoutInflater inflater;
    TextView toastText;
    View toastLayout;
    Toast toast;
    //InputVariables
    private TextInputLayout textInputUserName;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //binding
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //custom toast
        inflater = getLayoutInflater();
        toastLayout = inflater.inflate(R.layout.custom_toast_layout, findViewById(R.id.toastLayout));
        toastText = toastLayout.findViewById(R.id.toastText);
        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        //dialog
        dialog = new ProgressDialog(this, ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Sign Up");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //initialize id
        textInputUserName = findViewById(R.id.text_input_username);
        textInputEmail = findViewById(R.id.text_input_email);
        textInputPassword = findViewById(R.id.text_input_password);

        //firebase instance code
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //Register Button OnClickListener
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(SignUpActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    //input fields validations check
                    if (!validateUsername() | !validateEmail() | !validatePassword()) {
                        return;
                    }
                    //hide keyboard
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    //other code start here
                    String userName = binding.editTextUsername.getText().toString();
                    String email = binding.editTextEmail.getText().toString();
                    String password = binding.editTextPassword.getText().toString();
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        } catch (FirebaseAuthWeakPasswordException weakPassword) {
                                            Log.d("TAG", "onComplete: weak_password");
                                            toastText.setText(R.string.WeakPassword);
                                            toast.show();
                                        } catch (FirebaseAuthInvalidCredentialsException malformedEmail) {
                                            Log.d("TAG", "onComplete: malformed_email");
                                        } catch (FirebaseAuthUserCollisionException existEmail) {
                                            Log.d("TAG", "onComplete: exist_email");
                                            toastText.setText(R.string.EmailAlreadyExist);
                                            toast.show();
                                        } catch (Exception e) {
                                            Log.d("TAG", "onComplete: " + e.getMessage());
                                        }
                                    } else {
                                        dialog.show();
                                        UserModel userModel = new UserModel(userName, email, password,"","","","","","","");
                                        database.getReference().child("UserData")
                                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                                .setValue(userModel);
                                        if (UiConfig.SEND_EMAIL_VERIFICATION_STATUS){
                                            sendEmailVerification();
                                        }else {
                                            toastText.setText(R.string.SignUpSuccessfully);
                                            toast.show();
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            });
                    dialog.dismiss();

                } else {
                    noConnectionDialog();
                }
            }

        });
        //Goto sign in activity
        binding.goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
        //Data Policy OnClickListener
        binding.dataPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //network check
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(MoviesDetailsActivity.CONNECTIVITY_SERVICE);
                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                    //we are connected to a network
                    Intent intent1 = new Intent(SignUpActivity.this, WebviewActivity.class);
                    intent1.putExtra("IMDbLink","https://shrcreation.com/privacy.html");
                    intent1.putExtra("movieName","Privacy & Policy");
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);

                } else {
                    noConnectionDialog();
                }
            }
        });


    }

    //send email verification
    private void sendEmailVerification(){
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    toastText.setText(R.string.sendEmailVerification);
                    toast.show();
                    startActivity(new Intent(SignUpActivity.this,SignInActivity.class));
                    finish();
                }
            });
        }else {
            toastText.setText(R.string.failed_to_send_verification_email);
            toast.show();
        }
    }

    //Other Methods
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

    //username Validation
    private boolean validateUsername() {
        String usernameInput = textInputUserName.getEditText().getText().toString().trim();
        if (usernameInput.isEmpty()) {
            textInputUserName.setError("Username is required.");
            return false;
        } else {
            textInputUserName.setError(null);
            return true;
        }
    }

    //email Validation
    private boolean validateEmail() {
        String emailInput = textInputEmail.getEditText().getText().toString().trim();
        if (emailInput.isEmpty()) {
            textInputEmail.setError("Email is required.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            textInputEmail.setError("Please enter a valid email address");
            return false;
        } else {
            textInputEmail.setError(null);
            //textInputEmail.setErrorEnabled(false);
            return true;
        }
    }

    //password Validation
    private boolean validatePassword() {
        String passwordInput = textInputPassword.getEditText().getText().toString().trim();
        if (passwordInput.isEmpty()) {
            textInputPassword.setError("Password is required.");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            if (passwordInput.length() < 6) {
                textInputPassword.setError("At least 6 Characters");
            } else if (passwordInput.length() > 15) {
                textInputPassword.setError("Password too long");
            } else {
                textInputPassword.setError("No white spaces");
            }
            return false;
        } else {
            textInputPassword.setError(null);
            //textInputEmail.setErrorEnabled(false);
            return true;
        }
    }
}