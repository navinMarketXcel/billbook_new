package com.billbook.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.LoginRequest;
import com.billbook.app.networkcommunication.LoginResponse;
import com.billbook.app.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private RelativeLayout llMainLayout;
    private ProgressDialog progressDialog;

    private int synchAPICount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        llMainLayout = findViewById(R.id.llMainLayout);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateInput()) {


                    if (Util.isNetworkAvailable(getApplicationContext())) {
                        getOTP();
//                        startOTPActivity();
                    } else {
                        DialogUtils.showToast(LoginActivity.this, getString(R.string.no_internet));

                    }
                }
            }
        });

        findViewById(R.id.tvForgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void startOTPActivity(String opt){
        Intent intent = new Intent(this,OTPActivity.class);
        intent.putExtra("mobileNo",edtUsername.getText().toString());
        intent.putExtra("OTP",opt);
        startActivity(intent);
    }

    private void doLogin() {

        progressDialog = DialogUtils.startProgressDialog(LoginActivity.this, "");
        Log.v(TAG, "edtusername.getText().toString()::" + edtUsername.getText().toString());
        Log.v(TAG, "edtpass.getText().toString()::" + edtPassword.getText().toString());

        final String username = "test004";
        final String password = "test004";

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<User> users = (ArrayList<User>) MyApplication.getDatabase().userDao().getAll();
                if (users.size() > 0 && !users.get(0).getUsername().equalsIgnoreCase(username)) {
                    Log.v(TAG, "New user Login delete all data username::" + username);
                    MyApplication.saveGetCategoriesLAST_SYNC_TIMESTAMP(0);
                    MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(0);
                    MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(0);
                    MyApplication.saveGetInventoryLAST_SYNC_TIMESTAMP(0);
                    MyApplication.saveGetInvoiceLAST_SYNC_TIMESTAMP(0);
                    MyApplication.getDatabase().inventoryDao().deleteAll();
                    MyApplication.getDatabase().productDao().deleteAll();
                    MyApplication.getDatabase().brandDao().deleteAll();
                    MyApplication.getDatabase().categoriesDao().deleteAll();
                    MyApplication.getDatabase().userDao().deleteAll();
                    MyApplication.setInvoiceNumber(0);
                    MyApplication.setInvoiceNumberForNonGst(0);

                }
                callLogin(username, password);
            }
        });
    }

    private void callLogin(String username, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        Call<LoginResponse> call = apiService.doLogin(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();

                Log.v(TAG, "loginResponse::" + loginResponse);
                if (loginResponse != null && loginResponse.getToken() != null && (!loginResponse.getToken()
                        .isEmpty())) {
                    saveToken(loginResponse.getToken());
                    gotoSyncActivity();
                } else {
                   DialogUtils.showToast(LoginActivity.this, "Login failed because of incoorect username or password");
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
                showFailureMsg();
                progressDialog.dismiss();
            }
        });
    }

    private void gotoSyncActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void showFailureMsg() {
        DialogUtils.showToast(LoginActivity.this, "Login failed");
    }

    private void saveToken(String token) {

        SharedPreferences sharedPref =
                getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
                        getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.user_login_token), "JWT " + token);
        editor.commit();
    }

    private boolean validateInput() {
        if (edtUsername.getText().toString().trim().isEmpty()  || edtUsername.getText().toString().trim().length()!=10) {
            DialogUtils.showToast(LoginActivity.this, "Please enter 10 digit mobile number.");
            return false;
        }
        return true;
    }
    private void getOTP(){
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Map<String, String> req = new HashMap<>();
        req.put("mobileNo",edtUsername.getText().toString());
        headerMap.put("Content-Type", "application/json");

        Call<Object> call = apiService.getOTP((HashMap<String, String>) req);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();

//
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP",body.toString());
                    startOTPActivity(body.getJSONObject("data").getString("otp"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(LoginActivity.this,"Failed to get OTP");
            }
        });

    }

}
