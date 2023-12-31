package com.billbook.app.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewjapar.rangedatepicker.CalendarPicker;
import com.billbook.app.utils.Util;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.billbook.app.R;
import com.billbook.app.adapters.DayBookAdapter;
import com.billbook.app.database.models.DayBook;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.inmobi.media.et;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayBookActivity extends AppCompatActivity {
    private TextView startDateTV,endDateTV,totalExpenseTV;
    private RecyclerView dayBookRV;
    private Date starDate,endDate;
    private int hasWriteStoragePermission, isFirstReq = 1;
    private final int GRANT_STORAGE_PERMISSION =1;
    private String startDateStr,endDateStr;
    final private String TAG = "DayBook";
    private ArrayList<DayBook> dayBookArrayList = new ArrayList<>();
    private int userId =0;

    String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};
    String quart[] = {"Mar-May","JUN-AUG","SEP-NOV","DEC-FEB"};
    String quart1[] = {"APR-JUN","JUL-SEP","OCT-DEC","JAN-MAR"};
    private String email;
    private float totalIn=0, totalOut;
    private DayBookAdapter dayBookAdapter;
    LinearLayout lnNoRecordFound,lnContent;
    RelativeLayout bottomSheetContainer;
    EditText etCalender;
    ImageView ivToolBarBack;
    LinearLayout lnHelp,lnYouTube;
    SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    String date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_book);
        // Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        //  setTitle("Day Book");
        initUI();
        internalStoragePermission();
        SimpleDateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        startDateStr = myFormat1.format(new Date());
        endDateStr = myFormat1.format(new Date());
        getDayBook(startDateStr,endDateStr);
        startDateTV.setText("Start Date - "+myFormat1.format(new Date()));
        endDateTV.setText("End Date - "+myFormat1.format(new Date()));
        etCalender.setOnClickListener(v -> {
            try {

                optionsPopupMenu(etCalender);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
        lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(DayBookActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(DayBookActivity.this);
        });
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
        lnNoRecordFound = findViewById(R.id.lnNoRecordFound);
        lnContent = findViewById(R.id.lnContent);
        etCalender = findViewById(R.id.etCalender);
        bottomSheetContainer = findViewById(R.id.bottomSheet);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        lnHelp= findViewById(R.id.lnHelp);
        lnYouTube= findViewById(R.id.lnYouTube);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        dayBookRV.setLayoutManager(mLayoutManager);
        dayBookRV.setItemAnimator(new DefaultItemAnimator());
        final Calendar c = Calendar.getInstance();
        String year = String.valueOf(c.get(Calendar.YEAR)).substring(2);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        etCalender.setText("Today (" + mDay + " " + months[mMonth] + "'" + year + ")");
        try {
            JSONObject userProfile = new JSONObject(MyApplication.getUserDetails());
            userId = userProfile.getInt("userid");
            email = userProfile.isNull("email")?null:userProfile.getString("email");
           // Log.v("email",email);
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
                                new SimpleDateFormat("yyyy-MM-dd");
                        startDateStr = formatter.format(starDate);
                        startDateTV.setText("Start Date: " + startDateStr);
                        getDayBook(startDateStr,endDateStr);
                    }else{
                        endDate = myFormat.parse(dateToUse);
                        DateFormat formatter =
                                new SimpleDateFormat("yyyy-MM-dd");
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
                ApiClient.getClient(this).create(ApiInterface.class);
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
                    DialogUtils.stopProgressDialog(DayBookActivity.this);
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
                                if(invoices.getJSONObject(i).has("totalAfterDiscount")&& invoices.getJSONObject(i).getDouble("totalAfterDiscount")!=0)
                                    dayBook.setAmount((float)invoices.getJSONObject(i).getDouble("totalAfterDiscount"));
                                else
                                    dayBook.setAmount((float)invoices.getJSONObject(i).getDouble("totalAmount"));
                                dayBook.setDate(myFormat.parse(invoices.getJSONObject(i).getString("invoiceDate")));
                                dayBook.setExpense(false);
                                dayBookArrayList.add(dayBook);
                                totalIn =totalIn+dayBook.getAmount();
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
                            float profit = (totalIn-totalOut);

                            totalExpenseTV.setText(getResources().getString(R.string.Rs)+" " + Util.formatDecimalValue((float)totalIn-totalOut));
                            if(profit>0)
                                totalExpenseTV.setTextColor(getResources().getColor(R.color.income));
                            else
                                totalExpenseTV.setTextColor(Color.RED);
                            dayBookAdapter = new DayBookAdapter(DayBookActivity.this,dayBookArrayList);
                            dayBookRV.addItemDecoration(new DividerItemDecoration(dayBookRV.getContext(), DividerItemDecoration.VERTICAL));
                            if(dayBookArrayList.size()>0){
                                lnNoRecordFound.setVisibility(View.GONE);
                                lnContent.setVisibility(View.VISIBLE);
                            }else{
                                lnNoRecordFound.setVisibility(View.VISIBLE);
                                lnContent.setVisibility(View.GONE);
                            }
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
                    DialogUtils.showToast(DayBookActivity.this,"Check Internet Connection");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public boolean checkPermission() {
        hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    public void internalStoragePermission() {
        try {
            if (checkPermission() == true) {
                return;
            } else {
                Intent intent = new Intent(DayBookActivity.this, StoragePermissionRequestActivity.class);
              //  startActivityForResult(intent, GRANT_STORAGE_PERMISSION);
                someActivityResultLauncher.launch(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if (data.getBooleanExtra("GRANT_STORAGE_PERMISSION", true)) {
                        isFirstReq = 1;
                    } else {
                        isFirstReq = 0;
                        finish();
                    }
                } else {
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                    }



            });



    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //For Picking A contact and loading it in the editText View

        if (requestCode == GRANT_STORAGE_PERMISSION && resultCode == RESULT_OK && null != data) {
            if (data.getBooleanExtra("GRANT_STORAGE_PERMISSION", true)) {
                isFirstReq = 1;
            } else {
                isFirstReq = 0;
                finish();
            }
        } else if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }

        }
    }*/

    public void sendReportBtnClick(View v){
        Util.pushEvent("Clicked on export report");
        Util.postEvents("Export Report","Export Report",this.getApplicationContext());
//        if(email!=null && !email.isEmpty() && email.contains("@"))
//            Log.i(TAG, "sendReportBtnClick: Email => " + email);
//        else
//            DialogUtils.showToast(DayBookActivity.this,"Please update your email address in profile");
        if(email == null || email.isEmpty() )
        {
            downloadDaybook(startDateStr,endDateStr);
            Util.pushEvent("Report downloaded");
            Toast.makeText(this, "Daybook Downloading. Please Update Your Email in your Profile. ", Toast.LENGTH_LONG).show();

        }
        else
        {
            sendReport(startDateStr,endDateStr,email);
            Util.pushEvent("Report sent over mail and downloaded");

        }


    }

    public void optionsPopupMenu(EditText edt){
        PopupMenu popupMenu = new PopupMenu(DayBookActivity.this, edt);
        HashMap<String, Object>  spinnerDaybook = new HashMap<String, Object>();
        popupMenu.getMenuInflater().inflate(R.menu.custom_picker, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if(menuItem.getItemId()==R.id.m_custom){
                spinnerDaybook.put("Clicked Spinner","Clicked on Custom Period in Daybook");
                Log.v("spinner in cust",spinnerDaybook.toString());
               // Util.pushEvent("Clicked on Custom Period in Daybook");
                //calenderRangePicker();
                MaterialDatePicker datePicker =
                        MaterialDatePicker.Builder.dateRangePicker()
                                .setSelection(new Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                        MaterialDatePicker.todayInUtcMilliseconds()))
                                .setTitleText("Select dates")
                                .build();
                datePicker.show(getSupportFragmentManager(), "date");


                datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection -> {
                    Long startDate = selection.first;
                    Long endDate = selection.second;
                    String startDateString = SimpleDateFormat.getDateInstance().format(startDate);
                    String endDateString = SimpleDateFormat.getDateInstance().format(endDate);
                    String date = "Start: " + startDateString + " End: " + endDateString;
                    Toast.makeText(DayBookActivity.this, date, Toast.LENGTH_LONG).show();
                    String strDate = myFormat.format(startDate);
                    String endsDate = myFormat.format(endDate);
                    popupMenu.getMenu().findItem(R.id.m_custom).setTitle("Custom period" + date);
                    getDayBook(strDate,endsDate);
                   
                });




            }
            else if(menuItem.getItemId()==R.id.m_month){
                spinnerDaybook.put("Clicked Spinner","Clicked on This Month in Daybook");
                // Util.pushEvent("Clicked on This Month in Daybook");
                Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                int month_Number = startDate.get(Calendar.MONTH);
                String year = String.valueOf(startDate.get(Calendar.YEAR)).substring(2);
                startDate.set(Calendar.DATE, 1);
                startDate = setDay(startDate);
                String strDate = myFormat.format(startDate.getTime());
                String endsDate = myFormat.format(endDate.getTime());
                startDateStr = strDate;
                endDateStr = endsDate;
                getDayBook(strDate,endsDate);
                popupMenu.getMenu().findItem(R.id.m_month).setTitle("This Month " + "("+ months[month_Number] +"'"+ year + ")");

            }
            else if(menuItem.getItemId()==R.id.m_today){
               // Util.pushEvent("Clicked on Today in Daybook");
                spinnerDaybook.put("Clicked Spinner","Clicked on Today in Daybook");

                Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                String year = String.valueOf(startDate.get(Calendar.YEAR)).substring(2);
                int mMonth = startDate.get(Calendar.MONTH);
                int mDay = startDate.get(Calendar.DAY_OF_MONTH);
                String strDate = myFormat.format(startDate.getTime());
                String endsDate = myFormat.format(endDate.getTime());
                startDateStr = strDate;
                endDateStr = endsDate;
                getDayBook(strDate,endsDate);
                popupMenu.getMenu().findItem(R.id.m_today).setTitle("Today (" + mDay + " " +months[mMonth]  + "'" + year + ")");
            }
            else if(menuItem.getItemId()==R.id.m_week){
//                Util.pushEvent("Clicked on this Week in Daybook");
                spinnerDaybook.put("Clicked Spinner","Clicked on This Week in Daybook");
                Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                int day = startDate.get(Calendar.DAY_OF_WEEK);
                switch (day) {
                    case Calendar.SUNDAY:
                        startDate.add(Calendar.DATE, -7);
                        break;
                    case Calendar.MONDAY:
                        startDate.add(Calendar.DATE, -0);
                        break;
                    case Calendar.TUESDAY:
                        startDate.add(Calendar.DATE, -1);
                        break;
                    case Calendar.WEDNESDAY:
                        startDate.add(Calendar.DATE, -2);
                        break;
                    case Calendar.THURSDAY:
                        startDate.add(Calendar.DATE, -3);
                        break;
                    case Calendar.FRIDAY:
                        startDate.add(Calendar.DATE, -4);
                        break;
                    case Calendar.SATURDAY:
                        startDate.add(Calendar.DATE, -5);
                        break;
                }
                Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                startDate = setDay(startDate);
                int mMonth = startDate.get(Calendar.MONTH);
                int mDay = startDate.get(Calendar.DAY_OF_MONTH);
                String year = String.valueOf(startDate.get(Calendar.YEAR)).substring(2);
                String strDate = myFormat.format(startDate.getTime());
                String endsDate = myFormat.format(endDate.getTime());
                startDateStr = strDate;
                endDateStr = endsDate;
                getDayBook(strDate,endsDate);
                popupMenu.getMenu().findItem(R.id.m_week).setTitle("This Week (" + mDay + " " +months[mMonth]  + "'" + year +"-"+ endDate.get(Calendar.DAY_OF_MONTH)+ " " +months[mMonth]  + "'" + year + ")");
            }
            else if(menuItem.getItemId()==R.id.m_quarter){
//                Util.pushEvent("Clicked on Quater in Daybook");
                spinnerDaybook.put("Clicked Spinner","Clicked on This Quater in Daybook");
                Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                DateFormat dateFormat = new SimpleDateFormat("MM");
                Date date = new Date();
                String year = String.valueOf(startDate.get(Calendar.YEAR)).substring(2);
                Log.v("Month",dateFormat.format(date));
                String month= dateFormat.format(date);
                if(month.equals("01") || month.equals("02") || month.equals("03"))
                {
                    startDate.set(Calendar.MONTH, 0);
                    startDate.set(Calendar.DATE,1);
                    endDate.set(Calendar.MONTH,2);
                    endDate.set(Calendar.DATE,31);
                    popupMenu.getMenu().findItem(R.id.m_quarter).setTitle("This Quarter (" + quart1[3] + ")" +  "'" + year);
                    Log.v("quater check",String.valueOf( quart1[3]));
                }
                else if(month.equals("04") || month.equals("05") || month.equals("06"))
                {

                    startDate.set(Calendar.MONTH, 3);
                    startDate.set(Calendar.DATE,1);
                    endDate.set(Calendar.MONTH,5);
                    endDate.set(Calendar.DATE,31);
                    popupMenu.getMenu().findItem(R.id.m_quarter).setTitle("This Quarter (" + quart1[0] + ")" +  "'" + year);
                    Log.v("quater check",String.valueOf( quart1[0]));
                }else if(month.equals("07") || month.equals("08") || month.equals("09"))
                {
                    startDate.set(Calendar.MONTH, 6);
                    startDate.set(Calendar.DATE,1);
                    endDate.set(Calendar.MONTH,8);
                    endDate.set(Calendar.DATE,31);
                    popupMenu.getMenu().findItem(R.id.m_quarter).setTitle("This Quarter (" + quart1[1] + ")" +  "'" + year);
                    Log.v("quater check",String.valueOf( quart1[1]));
                }else if(month.equals("10") || month.equals("11") || month.equals("12") )
                {
                    startDate.set(Calendar.MONTH, 9);
                    startDate.set(Calendar.DATE,1);
                    endDate.set(Calendar.MONTH,11);
                    endDate.set(Calendar.DATE,31);
                    popupMenu.getMenu().findItem(R.id.m_quarter).setTitle("This Quarter (" + quart1[2] + ")" +  "'" + year);
                    Log.v("quater check",String.valueOf( quart1[2]));
                }
                String strDate = myFormat.format(startDate.getTime());
                String endsDate = myFormat.format(endDate.getTime());
                startDateStr = strDate;
                endDateStr = endsDate;
                getDayBook(strDate,endsDate);

            }
            else if(menuItem.getItemId()==R.id.m_year){
//                Util.pushEvent("Clicked on This Year in Daybook");
                spinnerDaybook.put(" Clicked Spinner","Clicked on This Year in Daybook");
                Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                startDate.set(Calendar.MONTH,00);
                startDate.set(Calendar.DATE, 1);
                startDate = setDay(startDate);
                Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                String strDate = myFormat.format(startDate.getTime());
                String endsDate = myFormat.format(endDate.getTime());
                startDateStr = strDate;
                endDateStr = endsDate;
                getDayBook(strDate,endsDate);
                popupMenu.getMenu().findItem(R.id.m_year).setTitle("This Year (" + endDate.get(Calendar.YEAR) + ")");
            }

            MyApplication.cleverTapAPI.pushEvent("Daybook Spinner Clicked",spinnerDaybook);
            Log.v("spinner",spinnerDaybook.toString());
            edt.setText(menuItem.getTitle());
            return true;
        });
        popupMenu.show();
    }

    public Calendar setDay(Calendar startDate){
        startDate.set(Calendar.HOUR_OF_DAY,00);
        startDate.set(Calendar.MINUTE, 00);
        startDate.set(Calendar.SECOND, 00);
        startDate.set(Calendar.MILLISECOND, 00);
        return startDate;
    }

    public  void calenderRangePicker(){
        MaterialDatePicker datePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setSelection(new Pair(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                                MaterialDatePicker.todayInUtcMilliseconds()))
                        .setTitleText("Select dates")
                        .build();
        datePicker.show(getSupportFragmentManager(), "date");

        datePicker.addOnPositiveButtonClickListener((MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;
            String startDateString = SimpleDateFormat.getDateInstance().format(startDate);
            String endDateString = SimpleDateFormat.getDateInstance().format(endDate);
            String date = "Start: " + startDateString + " End: " + endDateString;
            Toast.makeText(DayBookActivity.this, date, Toast.LENGTH_SHORT).show();
            String strDate = myFormat.format(startDate);
            String endsDate = myFormat.format(endDate);
            getDayBook(strDate,endsDate);
        });


//        BottomSheetDialog gstSheet = new BottomSheetDialog(DayBookActivity.this, R.style.BottomSheetDialogTheme);
//        View bottomSheet = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_date_range_picker,null);
//        CalendarPicker calendarPicker = bottomSheet. findViewById(R.id.calendar_view);
//        TextView txtFrom=bottomSheet. findViewById(R.id.txtFrom);
//        TextView txtTo=bottomSheet. findViewById(R.id.txtTo);
//        String startDates = "",endDates="";
//        Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
//        Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
//        startDate.add(Calendar.MONTH, -6);
//        calendarPicker. setRangeDate(startDate.getTime(),endDate.getTime() );
//        calendarPicker.setMode(CalendarPicker.SelectionMode.RANGE);
//        calendarPicker.scrollToDate(startDate.getTime());
//        calendarPicker.setOnRangeSelectedListener((Date date, Date date2, String s1, String s2) -> {
//                    txtFrom.setText(s1);
//                    txtTo.setText(s2);
//                    //  DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
//
//                    String strDate = myFormat.format(date);
//                    String endsDate = myFormat.format(date2);
//                    getDayBook(strDate,endsDate);
//                    gstSheet.dismiss();
//                    return null;
//                }
//
//
//        );
//        calendarPicker.setOnStartSelectedListener((date, s) ->
//        {
//            txtFrom.setText(s);
//            txtTo.setText("-");
//            return null;
//        });
//        gstSheet.setContentView(bottomSheet);
//        gstSheet.show();
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
        DialogUtils.startProgressDialog(this, "Sending to email");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email",email);
            Log.v("email",email);
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
                            //DialogUtils.showToast(DayBookActivity.this,"Please give permission to storage");
                        }
                        else if(body.has("data")){
                            JSONObject data = body.getJSONObject("data");
                            String downloadLink = data.getString("dayBookLink");
                            if(!downloadLink.contains("https://"))
                                downloadLink = downloadLink.replace("http://", "https://");
                            String path = data.getString("dayBookLink").split("/")[3];
                            Log.v("path1",path);
                            //String path = data.getString("dayBookLink");
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
                    DialogUtils.showToast(DayBookActivity.this,"Check Internet Connection");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void onResume()
    {
        super.onResume();
        if (!checkPermission() && isFirstReq==0) finish();
        Util.dailyLogout(DayBookActivity.this);
    }

    public void downloadDaybook(String startdate, String endDate){
        DialogUtils.startProgressDialog(this, "Sending to email");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("Content-Type", "application/json");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email",email);
//            Log.v("email",email);
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

//                        if(body.getBoolean("status")){ DialogUtils.showToast(DayBookActivity.this,"Report sent over the email"); }
//                        else{ DialogUtils.showToast(DayBookActivity.this,"Failed to send the email"); }

                        if(ActivityCompat.checkSelfPermission(DayBookActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                            //DialogUtils.showToast(DayBookActivity.this,"Please give permission to storage");
                        }
                        else if(body.has("data")){
                            JSONObject data = body.getJSONObject("data");
                            String downloadLink = data.getString("dayBookLink");
                            if(!downloadLink.contains("https://"))
                                downloadLink = downloadLink.replace("http://", "https://");
                            String path = data.getString("dayBookLink").split("/")[3];
                            Log.v("path1",path);
                            //String path = data.getString("dayBookLink");
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
                    DialogUtils.showToast(DayBookActivity.this,"Check Internet Connection");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
