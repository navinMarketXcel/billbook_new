package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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

public class EditMobileNoActivity extends AppCompatActivity {
ImageView ivToolBarBack;
EditText etPhone;
Button btnGetOtp;
String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mobile);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        etPhone = findViewById(R.id.etPhone);
        btnGetOtp = findViewById(R.id.btnGetOtp);
        userid = getIntent().getStringExtra("userid");
        setonClick();
    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
        btnGetOtp.setOnClickListener(v -> {
            if(verifyData())getOTP();
        });
    }

    @Override
        public void onBackPressed() {
        finish();
    }
    private boolean verifyData() {

        if (etPhone.getText().toString().isEmpty() || etPhone.getText().toString().length() < 10) {
            etPhone.setError( "Invalid phone number");
            return false;
        }
        return true;
    }
    private void getOTP(){
        DialogUtils.startProgressDialog(this, "");
        try {
            ApiInterface apiService =
                    ApiClient.getClient(this).create(ApiInterface.class);
            Map<String, String> headerMap = new HashMap<>();
            Map<String, String> req = new HashMap<>();
            Log.v("Userid",userid);
            req.put("mobileNo",etPhone.getText().toString());
            req.put("isUpdateMobileNo","true");
            req.put("userid",userid);
            headerMap.put("Content-Type", "application/json");

            Call<Object> call = apiService.getOTP((HashMap<String, String>) req);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        if(body.getJSONObject("data").has("otp"))
                            startOTPActivity(body.getJSONObject("data").getString("otp"));
                        else
                            DialogUtils.showToast(EditMobileNoActivity.this,"Mobile number already registered");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(EditMobileNoActivity.this,"Failed to get OTP");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void startOTPActivity(String otp) {
        try {
            finish();
            Intent intent = new Intent(EditMobileNoActivity.this, OTPVerifayActivity.class);
            intent.putExtra("mobileNo",etPhone.getText().toString());
            intent.putExtra("userid",userid);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}