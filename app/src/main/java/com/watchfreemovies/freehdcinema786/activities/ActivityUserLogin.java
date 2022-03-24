package com.watchfreemovies.freehdcinema786.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.watchfreemovies.freehdcinema786.R;
import com.watchfreemovies.freehdcinema786.callbacks.CallbackLogin;
import com.watchfreemovies.freehdcinema786.database.prefs.SharedPref;
import com.watchfreemovies.freehdcinema786.models.User;
import com.watchfreemovies.freehdcinema786.rests.ApiInterface;
import com.watchfreemovies.freehdcinema786.rests.RestAdapter;
import com.watchfreemovies.freehdcinema786.utils.Constant;
import com.watchfreemovies.freehdcinema786.utils.Tools;

import id.solodroid.validationlibrary.Rule;
import id.solodroid.validationlibrary.Validator;
import id.solodroid.validationlibrary.annotation.Email;
import id.solodroid.validationlibrary.annotation.Password;
import id.solodroid.validationlibrary.annotation.Required;
import id.solodroid.validationlibrary.annotation.TextRule;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityUserLogin extends AppCompatActivity implements Validator.ValidationListener {

    String strEmail, strPassword;
    @Required(order = 1)
    @Email(order = 2, message = "Please Check and Enter a valid Email Address")
    EditText edtEmail;

    @Required(order = 3)
    @Password(order = 4, message = "Enter a Valid Password")
    @TextRule(order = 5, minLength = 6, message = "Enter a Password Correctly")
    EditText edtPassword;

    private Validator validator;
    Button btnSingIn, btnSignUp;
    MyApplication MyApp;
    TextView txtForgot;

    SharedPref sharedPref;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_user_login);
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);
        MyApp = MyApplication.getInstance();
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnSingIn = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_create);
        txtForgot = findViewById(R.id.txt_forgot);

        btnSingIn.setOnClickListener(v -> {
            validator.validateAsync();
            MyApp.saveType("normal");

        });

        txtForgot.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityUserLogin.this, ActivityForgotPassword.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), ActivityUserRegister.class));
            finish();
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        if (Tools.isConnect(ActivityUserLogin.this)) {
            doLogin(strEmail, strPassword);
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();
        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(this, "Record Not Saved", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doLogin(String email, String password) {

        progressDialog = new ProgressDialog(ActivityUserLogin.this);
        progressDialog.setTitle(getResources().getString(R.string.title_please_wait));
        progressDialog.setMessage(getResources().getString(R.string.login_process));
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<CallbackLogin> call = apiInterface.userLogin(email, password);
        call.enqueue(new Callback<CallbackLogin>() {
            @Override
            public void onResponse(Call<CallbackLogin> call, Response<CallbackLogin> response) {
                CallbackLogin resp = response.body();
                if (resp != null) {
                    User user = resp.user;
                    new Handler().postDelayed(() -> {
                        if (resp.status.equals("ok")) {
                            if (user.status.equals("1")) {
                                MyApp.saveIsLogin(true);
                                MyApp.saveLogin(user.id, user.name, user.email);
                                dialogSuccessLogin();
                            } else if (user.status.equals("0")) {
                                dialogAccountDisabled();
                            } else {
                                dialogError();
                            }
                        } else if (resp.status.equals("failed")) {
                            dialogFailedLogin();
                        } else {
                            dialogError();
                        }
                        progressDialog.dismiss();
                    }, Constant.DELAY_REFRESH);
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<CallbackLogin> call, Throwable t) {
                t.printStackTrace();
                dialogError();
                progressDialog.dismiss();
                Log.d("rawr", t.getMessage());

            }
        });
    }

//    @SuppressWarnings("deprecation")
//    private class MyTaskLoginNormal extends AsyncTask<String, Void, String> {
//
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressDialog = new ProgressDialog(ActivityUserLogin.this);
//            progressDialog.setTitle(getResources().getString(R.string.title_please_wait));
//            progressDialog.setMessage(getResources().getString(R.string.login_process));
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            return NetworkCheck.getJSONString(params[0]);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            if (null == result || result.length() == 0) {
//
//            } else {
//
//                try {
//                    JSONObject mainJson = new JSONObject(result);
//                    JSONArray jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME);
//                    JSONObject objJson = null;
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        objJson = jsonArray.getJSONObject(i);
//                        if (objJson.has(Constant.MSG)) {
//                            strMessage = objJson.getString(Constant.MSG);
//                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
//                        } else {
//                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
//                            strName = objJson.getString(Constant.USER_NAME);
//                            strPassengerId = objJson.getString(Constant.USER_ID);
//                            //strImage = objJson.getString("normal");
//
//                        }
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//                new Handler().postDelayed(() -> {
//                    if (null != progressDialog && progressDialog.isShowing()) {
//                        progressDialog.dismiss();
//                    }
//                    setResult();
//                }, Constant.DELAY_PROGRESS_DIALOG);
//            }
//
//        }
//    }

    private void dialogSuccessLogin() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.login_title);
        dialog.setMessage(R.string.login_success);
        dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> finish());
        dialog.setCancelable(false);
        dialog.show();
    }

    private void dialogFailedLogin() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.whops);
        dialog.setMessage(R.string.login_failed);
        dialog.setPositiveButton(R.string.dialog_ok, null);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void dialogError() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.whops);
        dialog.setMessage(R.string.msg_no_network);
        dialog.setPositiveButton(R.string.dialog_ok, null);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void dialogAccountDisabled() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.whops);
        dialog.setMessage(R.string.login_disabled);
        dialog.setPositiveButton(R.string.dialog_ok, null);
        dialog.setCancelable(false);
        dialog.show();
    }

//    public void setResult() {
//
//        if (Constant.GET_SUCCESS_MSG == 0) {
//
//
//        } else if (Constant.GET_SUCCESS_MSG == 2) {
//
//
//        } else {
//
//
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

}
