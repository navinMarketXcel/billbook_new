package com.billbook.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.CountDownTimer;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.billbook.app.receiver.OtpReceiver;
import com.billbook.app.utils.Util;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OTPActivity extends AppCompatActivity {
    private final String TAG = "OTP";
    private static final int REQ_USER = 200;
    OtpReceiver smsBroadcastReciever;
    private String mobilNo,OTP,referrer_link;
    private EditText otpEdt;
    CountDownTimer cTimer = null;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private  boolean mVerificationInProgress;
    private String mVerificationId;
    private boolean fromEditProfile;
    private TextView etMobNo;
    private Button btnRegister;
    TextView timer,btnResend;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");


    private InstallReferrerClient referrerClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        setTitle("OTP");
        FirebaseApp.initializeApp(this);
        btnRegister=findViewById(R.id.btnRegister);
        etMobNo=findViewById(R.id.etMobNo);
        timer = findViewById(R.id.timer);
        mAuth = FirebaseAuth.getInstance();
        btnResend = findViewById(R.id.btnResend);

        otpEdt = findViewById(R.id.otpEdt);
        startSmartUserConsent();

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

        otpEdt.addTextChangedListener(loginWatcher);


    }
    void startTimer() {
        cTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                timer.setText(String.valueOf(millisUntilFinished/1000));
                btnResend.setEnabled(false);
                btnResend.setTextColor(Color.GRAY);

            }
            public void onFinish() {
                cancelTimer();
                btnResend.setEnabled(true);
                btnResend.setTextColor(Color.DKGRAY);
                btnResend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        resendOTP();
                        startTimer();
                    }
                });
            }
        };
        cTimer.start();
    }


    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    private void startSmartUserConsent()
    {
        SmsRetrieverClient client = SmsRetriever.getClient(this);
        client.startSmsUserConsent(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_USER)
        {
            if((resultCode==RESULT_OK)&&(data!=null))
            {
                String message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE);

                getOtpFromMessage(message);
            }
        }
    }

    private void getOtpFromMessage(String message) {

        Pattern otpPattern = Pattern.compile("(|^)\\d{6}");
        Matcher matcher = otpPattern.matcher(message);
        Log.v("message",message);
        if (matcher.find())
        {
            otpEdt.setText(matcher.group(0));
            Log.v("otp",matcher.group(0));
        }
    }

    private void registerBroadcastReceiver(){
        smsBroadcastReciever = new OtpReceiver();
        smsBroadcastReciever.smsBroadcastListener = new OtpReceiver.SmsBroadcastListener() {
            @Override
            public void onSuccess(Intent intent) {
                startActivityForResult(intent,REQ_USER);
                Log.v("In success","sucess");
            }

            @Override
            public void onFailure() {

            }
        };
        IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);
        registerReceiver(smsBroadcastReciever,intentFilter);

    }

    private TextWatcher loginWatcher= new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String otp = otpEdt.getText().toString();
            if(otp.length()==6)
            {
                btnRegister.setBackground(getDrawable(R.drawable.button_click_structure));
                btnRegister.setTextColor(getResources().getColor(R.color.white));
                btnRegister.setEnabled(true);




            }
            else
            {
                btnRegister.setBackground(getDrawable(R.drawable.new_structure));
                btnRegister.setTextColor(getResources().getColor(R.color.Silver));
                btnRegister.setEnabled(false);


            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    public void setMobileNo(String mobilNo){
        this.mobilNo = mobilNo;
    }

    public void setOTP(String otp){
        this.OTP = otp;
    }

    public void setReferrerLink(String referrerUrl) {
        this.referrer_link = referrerUrl;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTimer();
        etMobNo.setText(getIntent().getExtras().getString("mobileNo"));
//        startPhoneNumberVerification("+91"+mobilNo);
        registerBroadcastReceiver();
    }
    @Override
    protected void onStop()
    {
        super.onStop();
        unregisterReceiver(smsBroadcastReciever);
    }

    private void setData(){

        mobilNo = getIntent().getExtras().getString("mobileNo");
        OTP = getIntent().getExtras().getString("OTP");
        referrer_link = getIntent().getExtras().getString("referrer_link");
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
    public void resendOTP(){
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
        intent.putExtra("referrer_link", referrer_link);
        startActivity(intent);
    }
    public void gotoHomeScreen() {
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        intent.putExtra("mobileNo",mobilNo);
        startActivity(intent);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        String currentDate = sdf.format(c.getTime());
        MyApplication.setLogoutDaily(false);
        MyApplication.saveScheduleLogOutDate(currentDate);

    }



    private void verifyOTP(){
//        DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient(this).create(ApiInterface.class);
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
                ApiClient.getClient(this).create(ApiInterface.class);
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






