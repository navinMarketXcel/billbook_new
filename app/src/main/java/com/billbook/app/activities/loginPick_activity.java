package com.billbook.app.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.otpless.main.IntentType;
import com.otpless.main.Otpless;
import com.otpless.main.OtplessIntentRequest;
import com.otpless.main.OtplessProvider;
import com.otpless.main.OtplessTokenData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class loginPick_activity extends AppCompatActivity {

    private Button phoneButton;
    private Otpless otpless;
    private Button btnWhatsappLogi;
    private String mobilNo, otp,referrer_link;
    ProgressDialog whatsappDialog;
    private InstallReferrerClient referrerClient;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phoneButton=findViewById(R.id.btnLoginPhone);
        btnWhatsappLogi =findViewById(R.id.btnWhatsappLogi);
        setContentView(R.layout.activity_login_pick);
        otpless = OtplessProvider.getInstance(this).init(this::onOtplessResult);

       // phoneButton.setText("Force Crash");
//        phoneButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                throw new RuntimeException("Test Crash");
//            }
//        });
//        btnWhatsappLogi.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (Util.isNetworkAvailable(getApplicationContext())) {
//                    whatsappDialog = new ProgressDialog(loginPick_activity.this);
//                    whatsappDialog.setMessage("Please wait, signing you in!");
//                    whatsappDialog.setCancelable(false);
//                    whatsappDialog.setInverseBackgroundForced(false);
//                    whatsappDialog.show();
//                    getSignupUrl();
//                } else {
//                    DialogUtils.showToast(loginPick_activity.this, getString(R.string.no_internet));
//
//                }
//            }
//        });
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

        }
        public void whatsButtonClick(View v)
        {
            if (Util.isNetworkAvailable(getApplicationContext())) {
                    whatsappDialog = new ProgressDialog(loginPick_activity.this);
                    whatsappDialog.setMessage("Please wait, signing you in!");
                    whatsappDialog.setCancelable(false);
                    whatsappDialog.setInverseBackgroundForced(false);
                    whatsappDialog.show();
                    getSignupUrl();
                } else {
                    DialogUtils.showToast(loginPick_activity.this, getString(R.string.no_internet));

                }
        }
    private void setReferrerLink(String referrerUrl) {
        this.referrer_link = referrerUrl;
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
                        DialogUtils.showToast(loginPick_activity.this,"Please try again.");
                    } else {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        JSONObject data = body.getJSONObject("data");
                        initiateOtplessFlow(data.getString("url"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                whatsappDialog.hide();
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(loginPick_activity.this,"Signing failed! Please try again.");
            }
        });
    }
    private void initiateOtplessFlow(String intentUri) {
        final OtplessIntentRequest request = new OtplessIntentRequest(intentUri)
                .setLoadingText("Please wait...");
        request.setIntentType(IntentType.TEXT);
        otpless.openOtpless(request);
    }
    private void onOtplessResult(@Nullable OtplessTokenData response) {
        if (response == null) return;
        if(response.getToken() == null){
            whatsappDialog.hide();
            return;
        }
        //Send this token to your backend end api to fetch user details from otpless service
        HashMap<String,String> token = new HashMap<>();
        token.put("token",response.getToken());
        ApiInterface apiServiceOtpLessCreds =
                ApiClient.getClient(this).create(ApiInterface.class);
        Call<Object> whats_url = apiServiceOtpLessCreds.getUserDetails(token);
        whats_url.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> whats_url, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
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
                                if (body.has("data")) {
                                    if (body.getJSONObject("data").has("userid")) {
                                        ((MyApplication) getApplication()).saveUserDetails(body.getJSONObject("data").toString());
                                        JSONObject data = body.getJSONObject("data");
                                        String userToken = data.getString("userToken");
                                        MyApplication.saveUserToken(userToken);
                                        if(body.getJSONObject("data").has("isGST")) {
                                            Double newData = new Double((Double) body.getJSONObject("data").get("isGST"));
                                            int k = newData.intValue();
                                            ((MyApplication) getApplication()).setShowGstPopup(k);
                                        }
                                        gotoHomeScreen();
                                    }else {
                                        gotoRegistration();
                                    }
                                    whatsappDialog.hide();
                                } else {
                                    whatsappDialog.hide();
                                    DialogUtils.showToast(loginPick_activity.this, "Failed to sign in!");
                                }
                            } catch (JSONException e) {
                                whatsappDialog.hide();
                                DialogUtils.showToast(loginPick_activity.this, "Failed to sign in!");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<Object> call, Throwable t) {
                            whatsappDialog.hide();
                            DialogUtils.stopProgressDialog();
                            DialogUtils.showToast(loginPick_activity.this, "Failed to sign in!");
                        }
                    });
                } catch (JSONException e) {
                    whatsappDialog.hide();
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(loginPick_activity.this, "URL Expired! Please try again.");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                whatsappDialog.hide();
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(loginPick_activity.this,"Failed to sign in!");
            }
        });
    }
    public void gotoRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("mobileNo",mobilNo);
        intent.putExtra("otp",otp);
        intent.putExtra("referrer_link", referrer_link);
        startActivity(intent);
        finish();
    }
    public void gotoHomeScreen() {
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        intent.putExtra("mobileNo", mobilNo);
        startActivity(intent);
        finish();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        String currentDate = sdf.format(c.getTime());
        MyApplication.setLogoutDaily(false);
        MyApplication.saveScheduleLogOutDate(currentDate);
    }


    public void PhoneonClick(View v)
    {
        Intent intent=new Intent(loginPick_activity.this,LoginActivity.class);
        startActivity(intent);
    }


}