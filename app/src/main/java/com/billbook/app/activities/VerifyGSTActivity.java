package com.billbook.app.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.billbook.app.R;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyGSTActivity extends AppCompatActivity {
    private Handler handler;
    private EditText gstNo;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_gst);
        gstNo = findViewById(R.id.gstEDT);
        handler = new Handler();

    }

    public void updateGST(View v) {
        if (!gstNo.getText().toString().isEmpty()) {
            getGSTno(gstNo.getText().toString());
        }
    }

    private void getGSTno(final String gst) {
        OkHttpClient client = new OkHttpClient();
        DialogUtils.startProgressDialog(this, "");
        Request request = new Request.Builder()
                .url("https://appyflow.in/api/verifyGST?gstNo=" + gst + "&key_secret=sOaxR1cOxDh0MxSen4BXQ7oUVm93")
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(VerifyGSTActivity.this, "Failed to verify the GST details.");
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                DialogUtils.stopProgressDialog();
                String resp = response.body().string();
                Log.v("RESP", resp);
                try {
                    final JSONObject object = new JSONObject(resp);
                    if (object.has("taxpayerInfo") &&
                            object.getJSONObject("taxpayerInfo").getString("lgnm") != null) {
                        final String name = object.getJSONObject("taxpayerInfo").getString("lgnm");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateGST(gstNo.getText().toString());
                            }
                        });

                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DialogUtils.showToast(VerifyGSTActivity.this, object.getString("message"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void updateGST(String gst) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
        progressDialog = DialogUtils.startProgressDialog(VerifyGSTActivity.this, "");
        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");

        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Call<User> call = null;
        JSONObject req = new JSONObject();
        try {
            req.put("gst_no", gst);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), req.toString());
            call = apiService.patchUser(headerMap, userId, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User user = response.body();
                if (user != null) {

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.saveLoginUserID(user.getId());
                            MyApplication.getDatabase().userDao().inserUser(user);
                            MyApplication.saveIsGSTVerifies(true);
                        }
                    });
                    DialogUtils.showToast(VerifyGSTActivity.this, "GST updated successfully");
                    VerifyGSTActivity.this.finish();
                } else {
                    DialogUtils.showToast(VerifyGSTActivity.this, "Failed to update the GST");
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                DialogUtils.showToast(VerifyGSTActivity.this, "Failed to update the GST");
                progressDialog.dismiss();
            }
        });
    }

}
