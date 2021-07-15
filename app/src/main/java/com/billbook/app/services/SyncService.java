package com.billbook.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.billbook.app.activities.MyApplication;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncService extends Service {
    private static final String TAG = "SyncService";
    private JSONArray invoices;
    private String filePath;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "SyncService onCreate ");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new FetchTask().execute();
        Log.v(TAG, "SyncService onConStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "SyncService onDestroy ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class FetchTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String expense = MyApplication.getUnSyncedExpenses();
            if(!expense.isEmpty()){
                    ApiInterface apiService =
                            ApiClient.getClient().create(ApiInterface.class);
                    Map<String, String> headerMap = new HashMap<>();

                    headerMap.put("Content-Type", "application/json");
                    JsonArray req = new JsonParser().parse(expense).getAsJsonArray();
                Call<Object> call = apiService.expensesInBulk(headerMap, req);
                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                       if(response.code() ==200) {
                           Log.v("EXPENSES", response.body().toString());
                           try {
                               JSONObject   body = new JSONObject(new Gson().toJson(response.body()));
                               if(body.getBoolean("status")){
                                   MyApplication.saveUnSyncedExpenses("");
                               }
                           } catch (JSONException e) {
                               e.printStackTrace();
                           }
                       }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {

                    }
                });
            }
            syncInvoices();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

    }
    private void syncInvoices(){
        String invoicesStr = MyApplication.getUnSyncedInvoice();
        if(!invoicesStr.isEmpty()){
            try {
                invoices = new JSONArray(invoicesStr);
                if(invoices.length()==0)
                    return;
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            Map<String, String> headerMap = new HashMap<>();

            headerMap.put("Content-Type", "application/json");
            JSONObject inv = (JSONObject) invoices.remove(0);

            if(inv.has("pdfLink"))
                filePath = inv.remove("pdfLink").toString();

            JsonObject req = new JsonParser().parse(inv.toString()).getAsJsonObject();

            Call<Object> call = apiService.invoice(req);

            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if(response.code() ==200) {
                        Log.v("Invoices", response.body().toString());
                        try {
                            JSONObject   body = new JSONObject(new Gson().toJson(response.body()));
                            if(body.getBoolean("status")){
                              // upload pdf

                                ApiInterface apiService =
                                        ApiClient.getClient().create(ApiInterface.class);
                                Map<String, String> headerMap = new HashMap<>();
                                MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "invoive", RequestBody.create(MediaType.parse("*/*"), new File(filePath)));

                                Call<Object> call1 = apiService.updateInvoicePdf(headerMap, (int) body.getJSONObject("data").getJSONObject("invoice").getLong("id"), filePart);
                                call1.enqueue(new Callback<Object>() {
                                    @Override
                                    public void onResponse(Call<Object> call, Response<Object> response) {
                                        DialogUtils.stopProgressDialog();
                                        try {
                                            JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                                            Log.v("RESP", body.toString());
                                            if (body.getBoolean("status")) {

                                                MyApplication.saveUnSyncedInvoices(invoices.toString());
                                                if(invoices!=null && invoices.length()>0)
                                                    syncInvoices();
                                            } else {

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<Object> call, Throwable t) {
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {

                }
            });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
