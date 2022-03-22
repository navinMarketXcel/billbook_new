package com.billbook.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.daos.InvoiceItemDao;
import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.viewmodel.InvoiceViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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
    private TextView syncText;

    @Override
    public void onCreate() {
        super.onCreate();
//        invoiceViewModel = ViewModelProviders.
       invoiceViewModel = new InvoiceViewModel(this.getApplication());
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
                Log.d(TAG, "doInBackground: expense " + req);
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
        new FetchInvoice(MyApplication.getDatabase().newInvoiceDao()).execute();
    }


    private class FetchInvoice extends AsyncTask<Void,Void, List<InvoiceModelV2>>{
        NewInvoiceDao newInvoiceDao;
        private FetchInvoice(NewInvoiceDao newInvoiceDao){
            this.newInvoiceDao =newInvoiceDao;
        }
        @Override
        protected List<InvoiceModelV2> doInBackground(Void... voids) {
            return newInvoiceDao.getAllOffLineInvoice();
        }

        @Override
        protected void onPostExecute(List<InvoiceModelV2> invoiceModelV2List) {
            super.onPostExecute(invoiceModelV2List);
            for(int i = 0; i< invoiceModelV2List.size(); i++){
                InvoiceModelV2 curInvoice = invoiceModelV2List.get(i);
                new FetchInvoiceItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao(),curInvoice).execute(curInvoice);
            }

        }
    }


    private class FetchInvoiceItemsAsyncTask extends AsyncTask<InvoiceModelV2,Void,List<InvoiceItems>>{
        private InvoiceItemDao invoiceItemDao;
        private InvoiceModelV2 curInvoice;
        public String filePath;

        FetchInvoiceItemsAsyncTask(InvoiceItemDao invoiceItemDao, InvoiceModelV2 curInvoice){
            this.invoiceItemDao = invoiceItemDao;
            this.curInvoice = curInvoice;
        }

        @Override
        protected List<InvoiceItems> doInBackground(InvoiceModelV2... invoiceModelV2s) {
            return invoiceItemDao.getCurrentItems(invoiceModelV2s[0].getId());
        }
        
        @Override
        protected void onPostExecute(List<InvoiceItems> invoiceItemsList) {
            try {
                JSONObject requestObj = new JSONObject(new Gson().toJson(this.curInvoice));
                String items = new Gson().toJson(invoiceItemsList);
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

                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        JSONObject body = null;
                        try {
                            if (response.code() == 200) {
                                body = new JSONObject(new Gson().toJson(response.body()));
                                invoiceViewModel.updateIsSync(curInvoice.getId());
                                invoiceViewModel.updateInvoiceId(curInvoice.getId(),body.getJSONObject("data").getJSONObject("invoice").getInt("id") );
                            }
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                        }

                            // for uploading pdf to server :- to be implemented in later stage
//                            try {
//                                JSONObject   body = new JSONObject(new Gson().toJson(response.body()));
//                                if(body.getBoolean("status")){
//                                    // upload pdf
//
//                                    ApiInterface apiService =
//                                            ApiClient.getClient().create(ApiInterface.class);
//                                    Map<String, String> headerMap = new HashMap<>();
//                                    File pdfFile =  new File(filePath).getAbsoluteFile();
//                                    Log.d(TAG, "syncc onResponse: pdfFile " + pdfFile);
//                                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", "invoive", RequestBody.create(MediaType.parse("*/*"),pdfFile));
//
//                                    Call<Object> call1 = apiService.updateInvoicePdf(headerMap, (int) body.getJSONObject("data").getJSONObject("invoice").getLong("id"), filePart);
//                                    call1.enqueue(new Callback<Object>() {
//                                        @Override
//                                        public void onResponse(Call<Object> call, Response<Object> response) {
//                                            Log.d(TAG, "syncc onResponse: inner" + response.body());
//                                            DialogUtils.stopProgressDialog();
//                                            try {
//                                                JSONObject body = new JSONObject(new Gson().toJson(response.body()));
//                                                Log.v("RESP", body.toString());
//                                                if (body.getBoolean("status")) {
//
//                                                }
//                                                else { }
//                                            }
//                                            catch (JSONException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        @Override
//                                        public void onFailure(Call<Object> call, Throwable t) { }
//                                    });
//                                }
//
//                            }
//                            catch (JSONException e) {
//                                e.printStackTrace();
//                            }

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

// Not needed in MVVM
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
