package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OTPVerifayActivity extends AppCompatActivity {
ImageView ivToolBarBack;
EditText etOTP;
Button btnVerify;
String mobileNumber;
TextView txtOTPMobile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verifay);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        etOTP = findViewById(R.id.etOTP);
        btnVerify = findViewById(R.id.btnVerify);
        txtOTPMobile = findViewById(R.id.txtOTPMobile);


        mobileNumber = getIntent().getExtras().getString("mobileNo");
        txtOTPMobile.setText("OTP sent to "+mobileNumber);
        setonClick();
    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
        btnVerify.setOnClickListener(v -> {
            if(verifyData()){
                verifyOTP();
            }
        });
    }
    private boolean verifyData() {

        if (etOTP.getText().toString().isEmpty()) {
            etOTP.setError( "Invalid OTP");
            return false;
        }
        return true;
    }
    private void verifyOTP(){
     DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Map<String, String> req = new HashMap<>();
        req.put("mobileNo", mobileNumber);
        req.put("otp", etOTP.getText().toString());
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

                            if (body.getJSONObject("data").has("userid")) {
                                Log.v("GST", body.toString());
                                ((MyApplication) getApplication()).saveUserDetails(body.getJSONObject("data").toString());

                                JSONObject data = body.getJSONObject("data");
                                String userToken = data.getString("userToken");
                                MyApplication.saveUserToken(userToken);


                                gotoContactDetails();

                        }
                    } else {
                        DialogUtils.showToast(OTPVerifayActivity.this, "OTP not correct");
                    }
                } catch (JSONException e) {
                    DialogUtils.showToast(OTPVerifayActivity.this, "Please check your OTP");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(OTPVerifayActivity.this, "Failed to get OTP");
            }
        });

    }
    public void gotoContactDetails() {
        finish();
        Intent intent = new Intent(this, ContactDetailsActivity.class);
        startActivity(intent);
    }
}