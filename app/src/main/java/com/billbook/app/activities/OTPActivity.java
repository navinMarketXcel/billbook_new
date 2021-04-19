package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.billbook.app.utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OTPActivity extends AppCompatActivity {
    private final String TAG = "OTP";
    private String mobilNo,OTP;
    private EditText otpEdt;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private  boolean mVerificationInProgress;
    private String mVerificationId;
    private boolean fromEditProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        setTitle("OTP");
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        otpEdt = findViewById(R.id.otpEdt);
        fromEditProfile = getIntent().hasExtra("fromEditProfile")?getIntent().getExtras().getBoolean("fromEditProfile"):false;

        if(!fromEditProfile){
            Util.postEvents("Registration","Get OTP",this.getApplicationContext());
        }
        setData();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationFailed:" + credential);
                DialogUtils.stopProgressDialog();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d(TAG, "onVerificationFailed:" + e.getMessage());
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(OTPActivity.this,"Failed to send OTP");

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(OTPActivity.this,"OTP sent succesfully");
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
//        startPhoneNumberVerification("+91"+mobilNo);
    }

    private void setData(){
        mobilNo = getIntent().getExtras().getString("mobileNo");
        OTP = getIntent().getExtras().getString("OTP");

    }
    public void verifyOTP(View v){
    // verify OTP
        if(otpEdt.getText().toString().length() ==6){
//            verifyOTP();
            if(!fromEditProfile){
                Util.postEvents("Registration","Submit OTP",this.getApplicationContext());
            }

            DialogUtils.startProgressDialog(this, "");
            verifyOTP();
//            verifyPhoneNumberWithCode(mVerificationId,otpEdt.getText().toString());
        }else{
            DialogUtils.showToast(this,"OTP should be of 6 digits");
        }
    }
    public void resendOTP(View v){
         //call API to do resend OTP
        if(!fromEditProfile)
        Util.postEvents("Registration","Resend OTP",this.getApplicationContext());
        DialogUtils.startProgressDialog(OTPActivity.this,"Resending OTP");
//        resendVerificationCode("+91"+mobilNo,mResendToken);
        resendOtp();
    }


    public void gotoRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("mobileNo",mobilNo);
        intent.putExtra("otp",OTP);
        startActivity(intent);
    }
    public void gotoHomeScreen() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("mobileNo",mobilNo);
        startActivity(intent);
    }



    private void verifyOTP(){
//        DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            Map<String, String> headerMap = new HashMap<>();
            Map<String, String> req = new HashMap<>();
            req.put("mobileNo", mobilNo);
            req.put("otp", otpEdt.getText().toString());
            headerMap.put("Content-Type", "application/json");

            Call<Object> call = apiService.verifyOTP((HashMap<String, String>) req);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP", body.toString());
                        if (body.has("data")) {
                            if (fromEditProfile) {
                                Intent intent = new Intent();
                                intent.putExtra("OTP_VERIFIED", true);
                                setResult(RESULT_OK, intent);
                                OTPActivity.this.finish();
                            }else{
                                if (body.getJSONObject("data").has("userid")) {
                                    Log.v("GST", body.toString());
                                    ((MyApplication) getApplication()).saveUserDetails(body.getJSONObject("data").toString());
                                    // Set it here and in registration too.
                                    if(body.getJSONObject("data").has("isGST")) {
                                        Log.v("GST", body.getJSONObject("data").get("isGST").toString());
                                        Double newData = new Double((Double) body.getJSONObject("data").get("isGST"));
                                        int k = newData.intValue();
                                        ((MyApplication) getApplication()).setShowGstPopup(k);
                                    }
                                        gotoHomeScreen();
                                }else {
                                    gotoRegistration();
                                }
                            }
                        } else {
                            DialogUtils.showToast(OTPActivity.this, "OTP not correct");
                        }
                    } catch (JSONException e) {
                        DialogUtils.showToast(OTPActivity.this, "Please check your OTP");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(OTPActivity.this, "Failed to get OTP");
                }
            });

    }
    private void resendOtp(){
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Map<String, String> req = new HashMap<>();
        req.put("mobileNo",mobilNo);
        headerMap.put("Content-Type", "application/json");

        Call<Object> call = apiService.getOTP((HashMap<String, String>) req);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP",body.toString());
                    if(body.getJSONObject("data").getString("otp").length() > 0){
                        DialogUtils.showToast(OTPActivity.this,"OTP sent.");
                    }else {
                        DialogUtils.showToast(OTPActivity.this,"Failed to send OTP");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(OTPActivity.this,"Failed to send OTP");
            }
        });

    }
    private void startPhoneNumberVerification(String phoneNumber) {
        DialogUtils.startProgressDialog(this,"Sending OTP");
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            if(fromEditProfile){
                                Intent intent = new Intent();
                                intent.putExtra("OTP_VERIFIED",true);
                                setResult(RESULT_OK, intent);
                                OTPActivity.this.finish();
                            }else
                            verifyOTP();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            DialogUtils.stopProgressDialog();
                            DialogUtils.showToast(OTPActivity.this,"Failed to verify OTP");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            }
                        }
                    }
                });
    }
    // [START resend_verification]
    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

}






