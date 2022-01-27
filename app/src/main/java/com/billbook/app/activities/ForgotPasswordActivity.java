package com.billbook.app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.LoginRequest;
import com.billbook.app.networkcommunication.LoginResponse;
import com.billbook.app.utils.Util;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    EditText edtEmail;
    Button btnSubmit;
    ProgressDialog progressDialog;
    String strEmail;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        edtEmail = findViewById(R.id.edtEmail);

        btnSubmit = findViewById(R.id.btnSubmit);


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateInputInformation()) {
                    if (Util.isNetworkAvailable(getApplicationContext())) {
                        doForgotPassword(strEmail);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }

    private void doForgotPassword(String email) {

        progressDialog = DialogUtils.startProgressDialog(ForgotPasswordActivity.this, "");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(email);


        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Call<LoginResponse> call = apiService.doLogin(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse loginResponse = response.body();

                progressDialog.dismiss();

                Log.v(TAG, "loginResponse::" + loginResponse);
                if (loginResponse != null && loginResponse.getToken() != null && (!loginResponse.getToken().isEmpty())) {


                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "failed", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
                Toast.makeText(ForgotPasswordActivity.this, "Failed", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }

    @SuppressLint("HardwareIds")
    private boolean validateInputInformation() {

        strEmail = edtEmail.getText().toString();

        boolean result = true;
        if (strEmail.equalsIgnoreCase("")) {
            Toast.makeText(this, "Email cannot be blank.", Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }


}
