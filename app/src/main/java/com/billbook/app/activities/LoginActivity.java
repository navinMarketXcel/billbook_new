package com.billbook.app.activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.billbook.app.R;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.LoginRequest;
import com.billbook.app.networkcommunication.LoginResponse;
import com.billbook.app.utils.Util;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.gson.Gson;

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
    private static final int CRED_PICKER = 123;

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private Button btnWhatsappLogi;
    private LinearLayout llMainLayout;
    private ProgressDialog progressDialog;
    private int synchAPICount = 0;
    ProgressDialog whatsappDialog;
    private String mobilNo, otp, referrer_link;
    private InstallReferrerClient referrerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        llMainLayout = findViewById(R.id.llMainLayout);
        btnLogin = findViewById(R.id.btnLogin);
//        btnWhatsappLogi =findViewById(R.id.btnWhatsappLogi);
       // otpless = OtplessProvider.getInstance(this).init(this::onOtplessResult);

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        // Connection established.
                        ReferrerDetails response = null;
                        try {
                            response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();
                            setReferrerLink(referrerUrl);
                            OTPActivity otpActivity = new OTPActivity();
                            otpActivity.setReferrerLink(referrerUrl);
                        } catch ( RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        break;
                }
            }


            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }


        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateInput()) {


                    if (Util.isNetworkAvailable(getApplicationContext())) {
                        getOTP();
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

//        btnWhatsappLogi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (Util.isNetworkAvailable(getApplicationContext())) {
//                    whatsappDialog = new ProgressDialog(LoginActivity.this);
//                    whatsappDialog.setMessage("Please wait, signing you in!");
//                    whatsappDialog.setCancelable(false);
//                    whatsappDialog.setInverseBackgroundForced(false);
//                    whatsappDialog.show();
//                    getSignupUrl();
//                } else {
//                    DialogUtils.showToast(LoginActivity.this, getString(R.string.no_internet));
//
//                }
//            }
//        });

        HintRequest hintRequest = new HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build();
//        GetPhoneNumberHintIntentRequest request = GetPhoneNumberHintIntentRequest.builder().build();
//        ActivityResultLauncher phoneNumberHint = new ActivityResultLauncher() {
//            @Override
//            public void launch(Object input, @Nullable ActivityOptionsCompat options) {
//
//            }
//
//            @Override
//            public void unregister() {
//
//            }
//
//            @NonNull
//            @Override
//            public ActivityResultContract getContract() {
//                return null;
//            }
//        };
//                Identity.getSignInClient(LoginActivity.this).getPhoneNumberHintIntent(request)
//                .addOnSuccessListener(result ->
//        {
//            try {
//                phoneNumberHint.launch(result.getIntentSender());
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        })
//                .addOnFailureListener(e ->
//                {
//
//                });
        PendingIntent intent = Credentials.getClient(this).getHintPickerIntent(hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(),CRED_PICKER,null,0,0,0,new Bundle());
        }
        catch (IntentSender.SendIntentException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CRED_PICKER && resultCode == RESULT_OK)
        {
            Credential credentials = data.getParcelableExtra(Credential.EXTRA_KEY);
            edtUsername.setText(credentials.getId().substring(3));
        }
        else if (requestCode == CRED_PICKER && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE)
        {
            Toast.makeText(LoginActivity.this,"No Phone Numebers available",Toast.LENGTH_LONG).show();
        }
    }

    private void setReferrerLink(String referrerUrl) {
        this.referrer_link = referrerUrl;
    }

    public String getReferrerLink() {
        return this.referrer_link;
    }

//    private void onOtplessResult(@Nullable OtplessTokenData response) {
//        if (response == null) return;
//        if(response.getToken() == null){
//            whatsappDialog.hide();
//            return;
//        }
//        //Send this token to your backend end api to fetch user details from otpless service
//        HashMap<String,String> token = new HashMap<>();
//        token.put("token",response.getToken());
//        ApiInterface apiServiceOtpLessCreds =
//                ApiClient.getClient(this).create(ApiInterface.class);
//        Call<Object> whats_url = apiServiceOtpLessCreds.getUserDetails(token);
//        whats_url.enqueue(new Callback<Object>() {
//            @Override
//            public void onResponse(Call<Object> whats_url, Response<Object> response) {
//                DialogUtils.stopProgressDialog();
//                try {
//                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
//                    mobilNo = String.valueOf(body.getJSONObject("data").getString("mobileNo"));
//                    otp = String.valueOf(body.getJSONObject("data").getString("otp"));
//                    token.clear();
//                    token.put("mobileNo",String.valueOf(body.getJSONObject("data").getString("mobileNo")));
//                    token.put("otp",String.valueOf(body.getJSONObject("data").getString("otp")));
//                    token.put("loginType","Whatsapp");
//                    Call<Object> call = apiServiceOtpLessCreds.verifyOTP((HashMap<String, String>) token);
//                    call.enqueue(new Callback<Object>() {
//                        @Override
//                        public void onResponse(Call<Object> call, Response<Object> response) {
//                            DialogUtils.stopProgressDialog();
//                            try {
//                                OTPActivity otpActivity = new OTPActivity();
//                                otpActivity.setMobileNo(String.valueOf(body.getJSONObject("data").getString("mobileNo")));
//                                otpActivity.setOTP(String.valueOf(body.getJSONObject("data").getString("otp")));
//                                JSONObject body = new JSONObject(new Gson().toJson(response.body()));
//                                if (body.has("data")) {
//                                    if (body.getJSONObject("data").has("userid")) {
//                                        ((MyApplication) getApplication()).saveUserDetails(body.getJSONObject("data").toString());
//                                        JSONObject data = body.getJSONObject("data");
//                                        String userToken = data.getString("userToken");
//                                        MyApplication.saveUserToken(userToken);
//                                        if(body.getJSONObject("data").has("isGST")) {
//                                            Double newData = new Double((Double) body.getJSONObject("data").get("isGST"));
//                                            int k = newData.intValue();
//                                            ((MyApplication) getApplication()).setShowGstPopup(k);
//                                        }
//                                        gotoHomeScreen();
//                                    }else {
//                                        gotoRegistration();
//                                    }
//                                    whatsappDialog.hide();
//                                } else {
//                                    whatsappDialog.hide();
//                                    DialogUtils.showToast(LoginActivity.this, "Failed to sign in!");
//                                }
//                            } catch (JSONException e) {
//                                whatsappDialog.hide();
//                                DialogUtils.showToast(LoginActivity.this, "Failed to sign in!");
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<Object> call, Throwable t) {
//                            whatsappDialog.hide();
//                            DialogUtils.stopProgressDialog();
//                            DialogUtils.showToast(LoginActivity.this, "Failed to sign in!");
//                        }
//                    });
//                } catch (JSONException e) {
//                    whatsappDialog.hide();
//                    DialogUtils.stopProgressDialog();
//                    DialogUtils.showToast(LoginActivity.this, "URL Expired! Please try again.");
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Object> call, Throwable t) {
//                whatsappDialog.hide();
//                DialogUtils.stopProgressDialog();
//                DialogUtils.showToast(LoginActivity.this,"Failed to sign in!");
//            }
//        });
//    }

    private void setReferrerLinkOTPLess(String referrerUrl) {
        this.referrer_link = referrerUrl;
    }

    private void startOTPActivity(String opt){
        Intent intent = new Intent(this,OTPActivity.class);
        intent.putExtra("mobileNo",edtUsername.getText().toString());
        intent.putExtra("OTP",opt);
        intent.putExtra("referrer_link", referrer_link);
        startActivity(intent);
    }
    public void gotoRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("mobileNo",mobilNo);
        intent.putExtra("otp",otp);
        intent.putExtra("referrer_link", referrer_link);
        startActivity(intent);
    }
    public void gotoHomeScreen() {
        Intent intent = new Intent(this, BottomNavigationActivity.class);
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
                    DialogUtils.showToast(LoginActivity.this, "Login failed because of incorrect username or password");
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
        Intent intent = new Intent(LoginActivity.this, BottomNavigationActivity.class);
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
        req.put("loginType","Otp Generated");
        headerMap.put("Content-Type", "application/json");

        Call<Object> call = apiService.getOTP((HashMap<String, String>) req);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    Util.pushEvent("Otp Received");
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
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


    private void getSignupUrl(){
        ApiInterface apiServiceOtpLessCreds =
                ApiClient.getClient(this).create(ApiInterface.class);
        Call<Object> whats_url = apiServiceOtpLessCreds.getSignupUrl();
        whats_url.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> whats_url, Response<Object> response) {
                try {
                    if (response.body() == null) {
                        whatsappDialog.hide();
                        DialogUtils.stopProgressDialog();
                        DialogUtils.showToast(LoginActivity.this,"Please try again.");
                    } else {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        JSONObject data = body.getJSONObject("data");
                        //initiateOtplessFlow(data.getString("url"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                whatsappDialog.hide();
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(LoginActivity.this,"Signing failed! Please try again.");
            }
        });
    }

//    private void initiateOtplessFlow(String intentUri) {
//        final OtplessIntentRequest request = new OtplessIntentRequest(intentUri)
//                .setLoadingText("Please wait...");
//        request.setIntentType(IntentType.TEXT);
//        otpless.openOtpless(request);
//    }

}
