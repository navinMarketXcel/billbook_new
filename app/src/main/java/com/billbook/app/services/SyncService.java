package com.billbook.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;

import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.daos.InvoiceItemDao;
import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModel;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.viewmodel.InvoiceItemsViewModel;
import com.billbook.app.viewmodel.InvoiceViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
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
    private InvoiceViewModel invoiceViewModel;

    @Override
    public void onCreate() {
        super.onCreate();
//        invoiceViewModel = ViewModelProviders.
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
            //to be remove later
//            syncInvoices();
            syncOffLineInvoiceFromDatabase();
            //sync invoice from database is not properly tested
            //syncOffLineInvoiceFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

    }

    public  void syncOffLineInvoiceFromDatabase(){
        Log.d(TAG, "syncc syncOffLineInvoiceFromDatabase: done");
        new FetchInvoice(MyApplication.getDatabase().newInvoiceDao()).execute();
    }


    private class FetchInvoice extends AsyncTask<Void,Void, List<InvoiceModel>>{
        NewInvoiceDao newInvoiceDao;
        private FetchInvoice(NewInvoiceDao newInvoiceDao){
            this.newInvoiceDao =newInvoiceDao;
        }
        @Override
        protected List<InvoiceModel> doInBackground(Void... voids) {
            Log.d(TAG, "syncc doInBackground: fetchinvoice done");
            return newInvoiceDao.getAllOffLineInvoice();
        }

        @Override
        protected void onPostExecute(List<InvoiceModel> invoiceModelList) {
            super.onPostExecute(invoiceModelList);
            Log.d(TAG, "syncc onPostExecute: invoiceModelList ");
            for(int i = 0;i<invoiceModelList.size();i++){
                InvoiceModel curInvoice = invoiceModelList.get(i);
                new FetchInvoiceItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao(),curInvoice).execute(curInvoice);
            }

        }
    }


    private class FetchInvoiceItemsAsyncTask extends AsyncTask<InvoiceModel,Void,List<InvoiceItems>>{
        private InvoiceItemDao invoiceItemDao;
        private InvoiceModel curInvoice;
        public String filePath;

        FetchInvoiceItemsAsyncTask(InvoiceItemDao invoiceItemDao,InvoiceModel curInvoice){
            Log.d(TAG, "syncc FetchInvoiceItemsAsyncTask: ");
            this.invoiceItemDao = invoiceItemDao;
            this.curInvoice = curInvoice;
        }

        @Override
        protected List<InvoiceItems> doInBackground(InvoiceModel... invoiceModels) {
            Log.d(TAG, "syncc doInBackground: invoiceModels");
            return invoiceItemDao.getCurrentItems(invoiceModels[0].getId());
        }
        
        @Override
        protected void onPostExecute(List<InvoiceItems> invoiceItemsList) {
            Log.d(TAG, "syncc onPostExecute: heyyy");
            try {
                JSONObject requestObj = new JSONObject(new Gson().toJson(this.curInvoice));
                String items = new Gson().toJson(invoiceItemsList);
                Log.d(TAG, "syncc onPostExecute:  invoiceItemsList items " + items);
                requestObj.put("items", new JSONArray(items));
                ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
                Map<String, String> headerMap = new HashMap<>();

                headerMap.put("Content-Type", "application/json");
//                JsonObject req = new JsonParser().parse(requestObj.toString()).getAsJsonObject();
                if(requestObj.has("pdfPath"))
                    filePath = requestObj.remove("pdfPath").toString();
                requestObj.remove("createdAt");
                requestObj.remove("id");
                requestObj.remove("invoiceId");
                requestObj.remove("isSync");
                requestObj.remove("local_id");
                requestObj.remove("updatedAt");
                JsonObject req = new JsonParser().parse(requestObj.toString()).getAsJsonObject();

                Call<Object> call = apiService.invoice(req);
                Log.d(TAG, "syncc onPostExecute:  invoiceList " + req);

                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        Log.d(TAG, "syncc onResponse: " + response.body());
                        if(response.code() ==200) {
                            Log.d(TAG, "onResponse: curInvoice.getId()" + curInvoice.getId());
//                            invoiceViewModel.updateIsSync((long) curInvoice.getId());
                            Log.v("Invoices hyyy", response.body().toString());
                            try {
                                JSONObject   body = new JSONObject(new Gson().toJson(response.body()));
                                if(body.getBoolean("status")){
                                    // upload pdf

                                    ApiInterface apiService =
                                            ApiClient.getClient().create(ApiInterface.class);
                                    Map<String, String> headerMap = new HashMap<>();
                                    File pdfFile =  new File(filePath).getAbsoluteFile();
                                    Log.d(TAG, "syncc onResponse: pdfFile " + pdfFile);
                                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "invoive", RequestBody.create(MediaType.parse("*/*"),pdfFile));
                                    Log.d(TAG, "syncc uploadPDF: filepart " + filePart);

                                    Call<Object> call1 = apiService.updateInvoicePdf(headerMap, (int) body.getJSONObject("data").getJSONObject("invoice").getLong("id"), filePart);
                                    call1.enqueue(new Callback<Object>() {
                                        @Override
                                        public void onResponse(Call<Object> call, Response<Object> response) {
                                            Log.d(TAG, "syncc onResponse: filepath " + filePath);
                                            Log.d(TAG, "syncc onResponse: inner" + response.body());
                                            DialogUtils.stopProgressDialog();
                                            try {
                                                JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                                                Log.v("RESP", body.toString());
                                                if (body.getBoolean("status")) {

                                                }
                                                else { }
                                            }
                                            catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        @Override
                                        public void onFailure(Call<Object> call, Throwable t) { }
                                    });
                                }

                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) { }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            super.onPostExecute(invoiceItemsList);
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
                JsonObject req = new JsonParser().parse(inv.toString()).getAsJsonObject();
                if(inv.has("pdfLink"))
                    filePath = inv.remove("pdfLink").toString();

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
