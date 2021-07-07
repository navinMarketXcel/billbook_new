package com.billbook.app.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.billbook.app.R;
import com.billbook.app.adapters.DayBookAdapter;
import com.billbook.app.database.models.DayBook;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayBookActivity extends AppCompatActivity {
    private TextView startDateTV,endDateTV,totalExpenseTV;
    private RecyclerView dayBookRV;
    private Date starDate,endDate;
    private String startDateStr,endDateStr;
    final private String TAG = "DayBook";
    private ArrayList<DayBook> dayBookArrayList = new ArrayList<>();
    private int userId =0;
    private String email;
    private int totalIn=0, totalOut;
    private DayBookAdapter dayBookAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_book);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Day Book");
        initUI();
        SimpleDateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        startDateStr = myFormat1.format(new Date());
        endDateStr = myFormat1.format(new Date());
        getDayBook(startDateStr,endDateStr);
        startDateTV.setText("Start Date - "+myFormat1.format(new Date()));
        endDateTV.setText("End Date - "+myFormat1.format(new Date()));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initUI(){
        startDateTV = findViewById(R.id.startDate);
        endDateTV = findViewById(R.id.endDate);
        totalExpenseTV = findViewById(R.id.totalExpense);
        dayBookRV = findViewById(R.id.dayBookRV);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        dayBookRV.setLayoutManager(mLayoutManager);
        dayBookRV.setItemAnimator(new DefaultItemAnimator());
        try {
            JSONObject userProfile = new JSONObject(MyApplication.getUserDetails());
            userId = userProfile.getInt("userid");
            email = userProfile.isNull("email")?null:userProfile.getString("email");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void getDayBookData(View v){
        getDayBook(startDateStr,endDateStr);
    }
    public void startDate (View v){
        startDateDialog(true);
    }
    public void endDate (View v){
        startDateDialog(false);
    }
    private void startDateDialog(final boolean isStartDate){
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                String dateToUse = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                if (dayOfMonth < 10) {
                    dateToUse = year + "-" + (monthOfYear + 1) + "-0" + dayOfMonth;
                } else if ((monthOfYear + 1) < 10) {
                    dateToUse = year + "-0" + (monthOfYear + 1) + "-" + dayOfMonth;
                } else if (dayOfMonth < 10 && (monthOfYear + 1) < 10) {
                    dateToUse = year + "-0" + (monthOfYear + 1) + "-0" + dayOfMonth;
                }
                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                try {
                    dateToUse = dateToUse + "T00:00:00.001Z";
                    if(isStartDate) {
                        starDate = myFormat.parse(dateToUse);
                        DateFormat formatter =
                                new SimpleDateFormat("dd MMM yyyy");
                        startDateStr = formatter.format(starDate);
                        startDateTV.setText("Start Date: " + startDateStr);
                        getDayBook(startDateStr,endDateStr);
                    }else{
                        endDate = myFormat.parse(dateToUse);
                        DateFormat formatter =
                                new SimpleDateFormat("dd MMM yyyy");
                        endDateStr = formatter.format(endDate);
                        endDateTV.setText("End Date: " + endDateStr);
                        getDayBook(startDateStr,endDateStr);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void getDayBook(String startdate, String endDate){

        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("startDate",startdate);
            jsonObject.put("endDate",endDate);
            JsonObject req = new JsonParser().parse(jsonObject.toString()).getAsJsonObject();
            Call<Object> call = null;
            call = apiService.getDayBook(headerMap,userId,req);

            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));

                        if(body.getBoolean("status")){
                            totalIn =0;
                            totalOut =0;
                            dayBookArrayList = new ArrayList<>();
                            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            JSONArray invoices = body.getJSONObject("data").getJSONArray("invoices");
                            for(int i=0;i<invoices.length();i++){
                                DayBook dayBook = new DayBook();
                                dayBook.setName("Invoice "+ ( invoices.getJSONObject(i).getString("gstType").isEmpty()?
                                        invoices.getJSONObject(i).getInt("nonGstBillNo"):
                                        invoices.getJSONObject(i).getInt("gstBillNo")));
                                dayBook.setAmount(invoices.getJSONObject(i).getInt("totalAmount"));
                                dayBook.setDate(myFormat.parse(invoices.getJSONObject(i).getString("invoiceDate")));
                                dayBook.setExpense(false);
                                dayBookArrayList.add(dayBook);
                                Log.v("BEFORE TOTALIN::", ""+totalIn);
                                totalIn =totalIn+dayBook.getAmount();
                                Log.v("AFTER TOTALIN::", ""+totalIn);

                            }
                            JSONArray expenseList = body.getJSONObject("data").getJSONArray("expenseList");
                            SimpleDateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
                            for(int i=0;i<expenseList.length();i++){
                                DayBook dayBook = new DayBook();
                                dayBook.setName(expenseList.getJSONObject(i).getString("name"));
                                dayBook.setAmount(expenseList.getJSONObject(i).getInt("amount"));
                                dayBook.setDate(myFormat1.parse(expenseList.getJSONObject(i).getString("expenseDate")));
                                dayBook.setExpense(true);
                                dayBookArrayList.add(dayBook);
                                totalOut =totalOut+dayBook.getAmount();
                            }
                            Collections.sort(dayBookArrayList, new Comparator<DayBook>() {
                                public int compare(DayBook o1, DayBook o2) {
                                    return o1.getDate().getTime()< o2.getDate().getTime() ? -1 : 1;
                                }
                            });
                            int profit=(totalIn-totalOut);
                            totalExpenseTV.setText("PROFIT : " + Util.formatDecimalValue((float)totalIn-totalOut));
                            if(profit>0)
                                totalExpenseTV.setTextColor(Color.GREEN);
                            else
                                totalExpenseTV.setTextColor(Color.RED);
                            dayBookAdapter = new DayBookAdapter(DayBookActivity.this,dayBookArrayList);
                            dayBookRV.setAdapter(dayBookAdapter);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(DayBookActivity.this,"Failed to save expense data");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void sendReportBtnClick(View v){
        Util.postEvents("Export Report","Export Report",this.getApplicationContext());
        sendReport(startDateStr,endDateStr,email);
    }

    public void startDownloading(List<String> downloadLink, String path){
        for(int i = 0;i<downloadLink.size();i++){
            DownloadManager.Request r = null;
            r = new DownloadManager.Request(Uri.parse(downloadLink.get(i)));
            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path);
            r.allowScanningByMediaScanner();
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(r);
        }
    }


    public void sendReport(String startdate, String endDate,String email){
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email",email);
            jsonObject.put("startDate",startdate);
            jsonObject.put("endDate",endDate);
            JsonObject req = new JsonParser().parse(jsonObject.toString()).getAsJsonObject();
            Call<Object> call = null;
            call = apiService.exportDayBook(headerMap,userId,req);

            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));

                        if(body.getBoolean("status")){ DialogUtils.showToast(DayBookActivity.this,"Report sent over the email"); }
                        else{ DialogUtils.showToast(DayBookActivity.this,"Failed to send the email"); }

                        if(ActivityCompat.checkSelfPermission(DayBookActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            DialogUtils.showToast(DayBookActivity.this,"Please give permission to storage");
                        }
                        else if(body.has("data")){
                            JSONObject data = body.getJSONObject("data");
                            String downloadLink = data.getString("dayBookLink").replace("http", "https");
                            String path = data.getString("dayBookLink").split("/")[4];
                            if(downloadLink!=null){
                                List<String>downloadList = new ArrayList<String>();
                                downloadList.add(downloadLink.toString());
                                startDownloading(downloadList,path);
                                DialogUtils.showToast(DayBookActivity.this,"Daybook Downloaded");
                            }
                        }
                        else DialogUtils.showToast(DayBookActivity.this,"Try again later");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(DayBookActivity.this,"Failed to save expense data");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}