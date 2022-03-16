package com.billbook.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.LoginRequest;
import com.billbook.app.networkcommunication.LoginResponse;
import com.billbook.app.utils.Util;
import com.otpless.main.IntentType;
import com.otpless.main.Otpless;
import com.otpless.main.OtplessIntentRequest;
import com.otpless.main.OtplessProvider;
import com.otpless.main.OtplessTokenData;

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
    private ImageButton btnWhatsappLogin;
    private RelativeLayout llMainLayout;
    private ProgressDialog progressDialog;
    private Otpless otpless;
    private int synchAPICount = 0;
    private String authkey, clientid, clientsecret, mobilNo, otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        llMainLayout = findViewById(R.id.llMainLayout);
        btnLogin = findViewById(R.id.btnLogin);
        btnWhatsappLogin = (ImageButton)findViewById(R.id.btnWhatsappLogin);
        otpless = OtplessProvider.getInstance(this).init(this::onOtplessResult);
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

        btnWhatsappLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Util.isNetworkAvailable(getApplicationContext())) {
                    getSignupUrl();
                } else {
                    DialogUtils.showToast(LoginActivity.this, getString(R.string.no_internet));

                }
            }
        });
    }

    private void onOtplessResult(@Nullable OtplessTokenData response) {
        Log.v("OTPless_TOken", String.valueOf(response));
        if (response == null) return;
        //Send this token to your backend end api to fetch user details from otpless service
        HashMap<String,String> token = new HashMap<>();
        token.put("token",response.getToken());
        ApiInterface apiServiceOtpLessCreds =
                ApiClient.getClient().create(ApiInterface.class);
        Call<Object> whats_url = apiServiceOtpLessCreds.getUserDetails(token);
        whats_url.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> whats_url, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("user_deatils", String.valueOf(body.getJSONObject("data")));
                    mobilNo = String.valueOf(body.getJSONObject("data").getString("mobileNo"));
                    otp = String.valueOf(body.getJSONObject("data").getString("otp"));
                    token.clear();
                    token.put("mobileNo",String.valueOf(body.getJSONObject("data").getString("mobileNo")));
                    token.put("otp",String.valueOf(body.getJSONObject("data").getString("otp")));
                    Call<Object> call = apiServiceOtpLessCreds.verifyOTP((HashMap<String, String>) token);
                    call.enqueue(new Callback<Object>() {
                        @Override
                        public void onResponse(Call<Object> call, Response<Object> response) {
                            DialogUtils.stopProgressDialog();
                            try {
                                OTPActivity otpActivity = new OTPActivity();
                                otpActivity.setMobileNo(String.valueOf(body.getJSONObject("data").getString("mobileNo")));
                                otpActivity.setOTP(String.valueOf(body.getJSONObject("data").getString("otp")));
                                JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                                Log.v("RESP", body.toString());
                                if (body.has("data")) {
                                        if (body.getJSONObject("data").has("userid")) {
                                            Log.v("GST", body.toString());
                                            ((MyApplication) getApplication()).saveUserDetails(body.getJSONObject("data").toString());

                                            JSONObject data = body.getJSONObject("data");
                                            String userToken = data.getString("userToken");
                                            MyApplication.saveUserToken(userToken);

                                            // Set it here and in registration too.
                                            if(body.getJSONObject("data").has("isGST")) {
                                                Double newData = new Double((Double) body.getJSONObject("data").get("isGST"));
                                                int k = newData.intValue();
                                                ((MyApplication) getApplication()).setShowGstPopup(k);
                                            }

                                            gotoHomeScreen();
                                        }else {
                                            gotoRegistration();
                                        }
                                } else {
                                    DialogUtils.showToast(LoginActivity.this, "OTP not correct");
                                }
                            } catch (JSONException e) {
                                DialogUtils.showToast(LoginActivity.this, "Please check your OTP");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            DialogUtils.stopProgressDialog();
                            DialogUtils.showToast(LoginActivity.this, "Failed to get OTP");
                        }
                    });
//                    RegistrationActivity(String.valueOf(body.getJSONObject("data").getString("otp")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(LoginActivity.this,"Failed to get Url");
            }
        });
        Log.v("Not_null", "Please wait, signing you in!");
        Toast toast = Toast.makeText(getApplicationContext(),
                "Please wait, signing you in!",
                Toast.LENGTH_LONG);


        toast.show();
    }

    private void startOTPActivity(String opt){
        Intent intent = new Intent(this,OTPActivity.class);
        intent.putExtra("mobileNo",edtUsername.getText().toString());
        intent.putExtra("OTP",opt);
        startActivity(intent);
    }
    public void gotoRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("mobileNo",mobilNo);
        intent.putExtra("otp",otp);
        startActivity(intent);
    }
    public void gotoHomeScreen() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("mobileNo",mobilNo);
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

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
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
                ApiClient.getClient().create(ApiInterface.class);
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

//    private void createAuthKey(){
//        ApiInterface apiServiceOtpLessCreds =
//                ApiClient.getClient().create(ApiInterface.class);
//        Call<Object> authKeyCall = apiServiceOtpLessCreds.createAuthKeyForDb();
//        authKeyCall.enqueue(new Callback<Object>() {
//            @Override
//            public void onResponse(Call<Object> authKeyCall, Response<Object> response) {
//                try {
//                    if (response.body() == null) {
//                        Log.v("AuthKey", "Null Aya h");
//                    } else {
//                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
//                        JSONObject data = body.getJSONObject("data");
//                        authkey = data.getString("authkey");
//                        getOTPlessCreds(apiServiceOtpLessCreds, authkey);
//                        Log.v("authkey", authkey);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//            @Override
//            public void onFailure(Call<Object> call, Throwable t) {
//                DialogUtils.stopProgressDialog();
//                DialogUtils.showToast(LoginActivity.this,"Failed to get AuthKey");
//            }
//        });
//
//    }

    private void getSignupUrl(){
        ApiInterface apiServiceOtpLessCreds =
                ApiClient.getClient().create(ApiInterface.class);
        Call<Object> whats_url = apiServiceOtpLessCreds.getSignupUrl();
        whats_url.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> whats_url, Response<Object> response) {
                try {
                    if (response.body() == null) {
                        Log.v("Sala_url", "Null Aya h");
                    } else {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        JSONObject data = body.getJSONObject("data");
                        Log.v("Url_dekh_Lo", String.valueOf(body.getJSONObject("data")));
                        initiateOtplessFlow(data.getString("url"));
                        Log.v("Data_bhi_dekh", data.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(LoginActivity.this,"Failed to get AuthKey");
            }
        });
    }

    private void initiateOtplessFlow(String intentUri) {
    //While you create a request with otpless sdk you can define your own loading text and color
        final OtplessIntentRequest request = new OtplessIntentRequest(intentUri)
                .setLoadingText("Please wait...");
        request.setIntentType(IntentType.TEXT);

        otpless.openOtpless(request);
    }

}
