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
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class LedgerLoginActivity extends AppCompatActivity {

    private static final String TAG = "LedgerLoginActivity";
    private Button btnLogin, btnSendOTP;
    private EditText edtPassword, edtOtp;
    private TextView tvForgotPassword;
    private String otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_login);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initUI();
        sendOTP();
        startSpotLight(edtPassword, "Password", "Enter ledger password.");
    }

    private void initUI() {
        btnLogin = findViewById(R.id.btnLogin);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        edtPassword = findViewById(R.id.edtPassword);
        edtOtp = findViewById(R.id.edtOTP);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOTP();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateLedgerPass(edtPassword.getText().toString());
            }
        });
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPasswordOfLedgerAccount();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void validateLedgerPass(final String pass) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final String userLegderPass = MyApplication.getDatabase().userDao().getUser(MyApplication.getUserID()).getLedger_password();
                Log.v(TAG, "userLegderPass::" + userLegderPass + "  enter pass::" + pass);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (userLegderPass != null && userLegderPass.equalsIgnoreCase(pass) &&
                                otp.equalsIgnoreCase(edtOtp.getText().toString())) {
                            Intent intent = null;
                            if (getIntent().hasExtra("editProfile")) {
                                intent = new Intent(LedgerLoginActivity.this, EditProfileActivity.class);
                            } else if (getIntent().hasExtra("verifyGST")) {
                                intent = new Intent(LedgerLoginActivity.this, VerifyGSTActivity.class);
                            } else if (getIntent().getBooleanExtra("startReportActivity", false))
                                intent = new Intent(LedgerLoginActivity.this, ReportActivity.class);
                            else
                                intent = new Intent(LedgerLoginActivity.this, SellingDetailActivity.class);
                            startActivity(intent);
                            LedgerLoginActivity.this.finish();
                        } else {
                            Toast.makeText(LedgerLoginActivity.this, "Please enter correct password or OTP.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }

    public void resetPasswordOfLedgerAccount() {

        final ProgressDialog dialog = DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<Object> call = apiService.forgotPasswordLedger(headerMap, "{}");
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, final Response<Object> response) {
                Log.v(TAG, "response::" + response.body());
                dialog.dismiss();
                DialogUtils.showToast(LedgerLoginActivity.this, "Password mail sent on your registred mail");
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                dialog.dismiss();
                DialogUtils.showToast(LedgerLoginActivity.this, "Error while resetting your password");
            }
        });
    }

   /* private void sendOTP(){
        otp = ""+((int)(Math.random()*900000)+100000);
        boolean type = getIntent().getBooleanExtra("startReportActivity",false);
        final String textMsg = "Dear dealer , one time password for opening the "+(type? "reports":"ledger account")+" is "+otp+" .Please do not share opt with anyone.";

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                URL url = null;

                final String mobile_no= MyApplication.getDatabase().userDao().getUser(MyApplication.getUserID()).getMobile_no();
                final String mdgURL = "https://japi.instaalerts.zone/httpapi/QueryStringReceiver?ver=1.0&key=IVNWWjJpv04CL2Mkv36hvw==&encrpt=0&dest="+mobile_no+
                        "&send=MARXEL&text="+textMsg;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(mdgURL);
                     urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if(urlConnection!= null)
                    urlConnection.disconnect();
                }
            }
            });
        Toast.makeText(this, "OTP sent successfully", Toast.LENGTH_LONG).show();
    }*/

    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isLedgerLoginscreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.btnLogin) {
                                SharedPreferences sharedPref =
                                        LedgerLoginActivity.this.getSharedPreferences(LedgerLoginActivity.this.getString(R.string.preference_file_key),
                                                LedgerLoginActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isLedgerLoginscreenIntroShown", true);
                                editor.commit();
                            } else {
                                if (view.getId() == R.id.edtPassword) {
                                    startSpotLight(edtOtp, "OPT", "Enter OTP here.");
                                } else if (view.getId() == R.id.edtOTP) {
                                    startSpotLight(btnSendOTP, "Resend", "Click to resend resend OTP.");
                                } else if (view.getId() == R.id.btnSendOTP) {
                                    startSpotLight(btnLogin, "Login", "Click here to login to sales report.");
                                }
                            }
                        }
                    })
                    .build()
                    .show();
        }
    }

    private void sendOTP() {
        final OkHttpClient client = new OkHttpClient();
        otp = "" + ((int) (Math.random() * 900000) + 100000);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                boolean type = getIntent().getBooleanExtra("startReportActivity", false);
                String typeStr = (type ? "reports" : "ledger account");
                typeStr = getIntent().hasExtra("editProfile") ? "Edit Profile" : typeStr;
                typeStr = getIntent().hasExtra("verifyGST") ? "Verifying GST" : typeStr;


                final String textMsg = "Dear dealer , one time password for opening the " + typeStr + " is " + otp + " .Please do not share opt with anyone.";
                final String mobile_no = MyApplication.getDatabase().userDao().getUser(MyApplication.getUserID()).getMobile_no();
                final String mdgURL = "https://japi.instaalerts.zone/httpapi/QueryStringReceiver?ver=1.0&key=IVNWWjJpv04CL2Mkv36hvw==&encrpt=0&dest=" + mobile_no +
                        "&send=MARXEL&text=" + textMsg;
                Request request = new Request.Builder()
                        .url(mdgURL)
                        .build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        DialogUtils.stopProgressDialog();
                        DialogUtils.showToast(LedgerLoginActivity.this, "Failed to send OTP");
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    }
                });
            }
        });

    }
}
