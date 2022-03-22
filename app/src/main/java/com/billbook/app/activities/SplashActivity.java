package com.billbook.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.billbook.app.BuildConfig;
import com.billbook.app.R;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.WebserviceResponseHandler;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.services.LocationService;
import com.billbook.app.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity implements WebserviceResponseHandler {
    private static final int SPLASH_SCREEN_TIMEOUT = 500;
    private static final String TAG = "SplashActivity";
    private JSONArray jsonArray;
    private String gpsPermission = "";
    private final int REQUEST_CODE_ASK_PERMISSIONS =111;
    private int hasWriteStoragePermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //checkPermission();a
//
//        Bundle params = new Bundle();
//        params.putString("test", "test");
//        params.putInt("level_difficulty", 4);
//        MyApplication.getFirebaseAnalytics(this.getApplicationContext()).logEvent("test",params);
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            checkVersion();
        }
    }

private void startSplash(){
    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject profile = new JSONObject(((MyApplication) getApplication()).getUserDetails());
                if (profile.has("userToken") && profile.has("shopName")) {
                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                SplashActivity.this.finish();
            } catch (JSONException e) {
                e.printStackTrace();
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }
    }, 2000);
}
    private void startLocationService() {
        Intent intent1 = new Intent(getApplicationContext(), LocationService.class);
        getApplicationContext().stopService(intent1);
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        startService(intent);
    }

    private void checkVersion() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Map<String, String> headerMap = new HashMap<>();

        Call<Object> call = apiService.getVersionUpdate(headerMap);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                final JSONObject body;
                try {
                    body = new JSONObject(new Gson().toJson(response.body()));
                    Log.d(TAG, "Version Body::" + body);
                    if (body.getBoolean("status") && body.has("data") && !body.isNull("data")) {
                        String versionName= body.getJSONObject("data").getString("versionName");

                        String buildVersion = BuildConfig.VERSION_NAME;
                        if (!buildVersion.equals(versionName)) {
                            DialogUtils.showToast(SplashActivity.this, "To continue, please update the app to latest version.");
                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        } else {
                            startSplash();
                        }
                    } else {
                        startSplash();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    startSplash();
                }

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                startSplash();

            }
        });

    }

    private void syncInvoices() {
        try {
            jsonArray = new JSONArray(MyApplication.getInVoiceOffline());
            Log.v("INVOICES", jsonArray.toString());

            if (jsonArray.length() > 0 && Util.isNetworkAvailable(this)) {
//                getLatestInvoice();
//                RequestInvoice  requestInvoice = new Gson().fromJson(jsonArray.get(0).toString(),RequestInvoice.class);
//                AppRepository.getInstance().putInvoiceAPI(SplashActivity.this, requestInvoice);
            } else {
                scheduleSplashScreen();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            scheduleSplashScreen();
        }

    }

    private void getLatestInvoice() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
//    headerMap.put("Content-Type", "application/json");

        Call<Object> call = apiService.getLatestInvoice(headerMap,null);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                final JSONObject body;
                try {
                    body = new JSONObject(new Gson().toJson(response.body()));
                    Log.d(TAG, "Invoice Body::" + body);
                    if (body.getBoolean("status")) {
                        int invoiceNo = body.getJSONObject("data").getInt("versionNo");
                        MyApplication.setInvoiceNumber(body.getInt("1") + 1);
                        MyApplication.setInvoiceNumberForNonGst(body.getInt("0") + 1);

                        RequestInvoice requestInvoice = new Gson().fromJson(jsonArray.get(0).toString(), RequestInvoice.class);
                        if (requestInvoice.getGst_type().equals("1"))
                            requestInvoice.setInvoice_no(invoiceNo + 1);
                        else
                            requestInvoice.setInvoice_no(body.getInt("0") + 1);
                        AppRepository.getInstance().putInvoiceAPI(SplashActivity.this, requestInvoice);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    scheduleSplashScreen();
                    DialogUtils.showToast(SplashActivity.this, "Failed to get latest invoice to sync");
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                scheduleSplashScreen();
                DialogUtils.showToast(SplashActivity.this, "Failed to get latest invoice to sync");
            }
        });

    }

    private void scheduleSplashScreen() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);
                String token = sharedPref.getString(getString(R.string.user_login_token), " ");

                if (token != null && !token.trim().isEmpty()) {
//                    gotoSyncScreen();
                    gotoLoginScreen();
                } else {
                    gotoLoginScreen();

                }
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }

    private void gotoLoginScreen() {
            Intent intentObj = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intentObj);
            finish();
    }

    private void gotoSyncScreen() {
        Intent intentObj = new Intent(SplashActivity.this, SyncActivity.class);
        startActivity(intentObj);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResponseSuccess(Object o) {
        jsonArray.remove(0);
        MyApplication.saveInVoiceOffline(jsonArray);
        startSplash();
    }

    @Override
    public void onResponseFailure() {
        scheduleSplashScreen();
    }

    private void sendWelcomeMessage() {
        String textMsg = "Dear Customer Welcome to mBill Family! mBill is a smart, simple & very easy to use app for your daily billing & accounting needs! Team mBill is here to help in case you have questions anytime! ‘mBill Support Team -you can reach us on 9319595452";
        final String mdgURL = "https://japi.instaalerts.zone/httpapi/QueryStringReceiver?ver=1.0&key=IVNWWjJpv04CL2Mkv36hvw==&encrpt=0&dest=" + "919096603564" +
                "&send=MARXEL&text=" + textMsg;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(mdgURL);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null)
                        urlConnection.disconnect();
                }
            }
        });
//        Toast.makeText(this, "OTP sent successfully", Toast.LENGTH_LONG).show();
    }


    private void checkPermission(){
        int hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
//                    DialogUtils.showToast(this, "WRITE_EXTERNAL Permission Granted");
                    checkVersion();

                } else {
                    // Permission Denied
//                    DialogUtils.showToast(this, "WRITE_EXTERNAL Permission Denied");
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
