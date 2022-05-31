package com.billbook.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.billbook.app.adapters.InvoiceListAdapter;
import com.billbook.app.database.models.Customer;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.model.InvoicesData;
import com.billbook.app.utils.OnDownloadClick;
import com.borax12.materialdaterangepicker.date.DatePickerDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.adapters.SearchInvoiceListAdapterNew;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class SearchInvoiceActivity extends AppCompatActivity implements View.OnClickListener,SearchInvoiceListAdapterNew.SearchInvoiceItemClickListener, DatePickerDialog.OnDateSetListener, OnDownloadClick {
    private static final String TAG = "SearchInvoiceActivity";
    private SearchInvoiceListAdapterNew searchInvoiceListAdapter;
    private RecyclerView recyclerViewInvoice;
    private EditText edtMobileNo;
    private TextView tvRecordNotFound,toAndFromDate;
    private Button btnSearch, downloadAll;
    private ProgressDialog progressDialog;
    private int page=1;
    private JSONObject userProfile;
    private int userid;
    private Button sortTv;
    private InvoiceListAdapter invoiceListAdapter;
    private boolean isCheckFlag = false;
    private JSONArray invoices = new JSONArray();
    private Date to,from;
    private Spinner dateSpinner;
    List<String> items;
    ArrayList<InvoicesData> invoicesList=new ArrayList<InvoicesData>();
    private int hasWriteStoragePermission;
    private final int REQUEST_CODE_ASK_PERMISSIONS =111;
    private final int REQUEST_CODE_ASK_PERMISSIONS_SAVE_INVOICE =112;
    private int saveInvoiceId = -1;
    DateFormat formatter1 =
            new SimpleDateFormat("dd MMM yyyy");
    DateFormat formatter2 =
            new SimpleDateFormat("dd/MM/yyyy");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_invoice);
        initUI();
        sortTv = findViewById(R.id.sortTv);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        startSpotLight(edtMobileNo, "Mobile No", "Enter Mobile no.");
        hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        dateSpinner = findViewById(R.id.dateSpinner);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Custom Period"))
                {
                    showDatePickerDialog(view);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }

    public void clickSelect(View v)
    {
        TextView selecttv= findViewById(R.id.selectTv);
        TextView sortTv = findViewById(R.id.sortTv);
        Button delete = findViewById(R.id.deleteButton);
        Button download = findViewById(R.id.downloadAll);
        selecttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isCheckFlag)
                {
                    download.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                    sortTv.setVisibility(View.VISIBLE);
                    selecttv.setText("Select");
                    isCheckFlag=false;
                }
                else
                {
                    download.setVisibility(View.VISIBLE);
                    delete.setVisibility(View.VISIBLE);
                    sortTv.setVisibility(View.GONE);
                    selecttv.setText("Cancel");
                    isCheckFlag=true;
                }
                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList, SearchInvoiceActivity.this,isCheckFlag);
                recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);

            }

        });
    }

    public void clickSort(View v)
    {
        sortTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView bill_no,bdate,dateMod;
                BottomSheetDialog sortSheet = new BottomSheetDialog(SearchInvoiceActivity.this,R.style.BottomSheetDialogTheme);
                View sortBottomSheet = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sort_layout,(LinearLayout)findViewById(R.id.sortLayout));
                bill_no = sortBottomSheet.findViewById(R.id.billno);
                bill_no.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
//
                        invoicesList = (ArrayList<InvoicesData>)jsonArrayToList(invoices).stream().sorted(Comparator.comparing(InvoicesData::getNonGstBillNo)).collect(Collectors.toList());
                        for (int i=0;i<invoicesList.size();i++){
                            System.out.println(invoicesList.get(i).getTotalAmount());
                        }
                        searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList,null,isCheckFlag);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerViewInvoice.setLayoutManager(layoutManager);
                        recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
                        recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
                        sortSheet.dismiss();

                    }
                });
                bdate = sortBottomSheet.findViewById(R.id.bdate);
                bdate.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {

                       invoicesList= (ArrayList<InvoicesData>)jsonArrayToList(invoices).stream().sorted(Comparator.comparing(InvoicesData::getInvoiceDate)).collect(Collectors.toList());
                        for (int i=0;i<invoicesList.size();i++){
                            System.out.println(invoicesList.get(i).getInvoiceDate());
                        }
                        searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList,null,isCheckFlag);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                        recyclerViewInvoice.setLayoutManager(layoutManager);
                        recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
                        recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
                        sortSheet.dismiss();

                    }
                });
                sortBottomSheet.findViewById(R.id.doneSort).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortSheet.dismiss();
                    }
                });
                sortBottomSheet.findViewById(R.id.canelSort).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortSheet.dismiss();
                    }
                });

                sortSheet.setContentView(sortBottomSheet);
                sortSheet.show();
            }

        });

    }
    public ArrayList<InvoicesData> jsonArrayToList(JSONArray jsonArray)
    {
        ArrayList<InvoicesData> invoicesListData = new ArrayList<InvoicesData>();
        //Checking whether the JSON array has some value or not
        if (jsonArray != null) {
            //Iterating JSON array
            for (int i=0;i<jsonArray.length();i++){
                //Adding each element of JSON array into ArrayList
                InvoicesData invoiceData = new InvoicesData();
                try {
                    JSONObject   obj = jsonArray.getJSONObject(i);
                    JSONObject cuObj = jsonArray.getJSONObject(i).getJSONObject("customer");
                    invoiceData.setTotalAmount(obj.getInt("totalAmount"));
                    invoiceData.setTotalAfterDiscount(obj.getInt("totalAfterDiscount"));
                    invoiceData.setNonGstBillNo(obj.getInt("nonGstBillNo"));
                    invoiceData.setGstBillNo(obj.getInt("gstBillNo"));
                    invoiceData.setGSTNo(obj.getString("GSTNo"));
                    invoiceData.setPdfLink(obj.getString("pdfLink"));
                    invoiceData.setInvoiceDate(obj.getString("invoiceDate"));
                    invoiceData.setGstType(obj.getString("gstType"));
                    invoiceData.setUpdatedAt(obj.getString("updatedAt"));
                    invoiceData.setIsActive(obj.getBoolean("is_active"));
                    invoiceData.setId(obj.getInt("id"));
                    invoiceData.setCustomer(new Customer(cuObj.getString("name"),cuObj.getString("mobileNo"),false));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                invoicesListData.add(invoiceData);
            }
        }

        return invoicesListData;
        }




    private void getInvoicesCall(){
        Map<String, String> body = new HashMap<>();
        body.put("userid",userid+"");
        body.put("page",""+page);
        getInvoices(body);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getInvoicesCall();
    }
    public void showDatePickerDialog(View v){
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                SearchInvoiceActivity.this,
                now.get(Calendar.YEAR), // Initial year selection
                now.get(Calendar.MONTH), // Initial month selection
                now.get(Calendar.DAY_OF_MONTH) // Inital day selection
        );
        dpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
                to = null;
                from = null;
                toAndFromDate.setText("");
            }
        });
        dpd.setMaxDate(now);
// If you're calling this from a support Fragment
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }
    private void initUI() {
        edtMobileNo = findViewById(R.id.edtMobileNo);
        tvRecordNotFound = findViewById(R.id.tvRecordNotFound);
        tvRecordNotFound.setVisibility(View.GONE);
        btnSearch = findViewById(R.id.btnSearch);
        toAndFromDate=findViewById(R.id.toAndFromDate);
        btnSearch.setOnClickListener(this);
        recyclerViewInvoice = findViewById(R.id.recyclerViewInvoice);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewInvoice.setLayoutManager(mLayoutManager);
        recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
        searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(this,invoicesList, this,isCheckFlag);
        recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
        downloadAll = findViewById(R.id.downloadAll);
        setTitle("Search Bill");
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerViewInvoice.addItemDecoration(decoration);
        try {
            userProfile= new JSONObject (((MyApplication)getApplication()).getUserDetails());
            userid = userProfile.getInt("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSearch:
                if (!edtMobileNo.getText().toString().trim().isEmpty() || (to!=null&from!=null)) {
                    Map<String, String> body = new HashMap<>();
                    body.put("userid",userid+"");
                    body.put("customerMobileNo",edtMobileNo.getText().toString());
                    body.put("page",""+page);
                    if(to!=null && from !=null){
                        body.put("startDate",formatter1.format(from));
                        body.put("endDate",formatter1.format(to));

                    }
                    getInvoices(body);
                } else {
                    DialogUtils.showToast(SearchInvoiceActivity.this, "Please enter mobile no or customer name or Date select.");
                }
                break;
        }
    }

    private void getInvoices(Map<String, String> body ) {
        if (Util.isNetworkAvailable(SearchInvoiceActivity.this)) {
            progressDialog = DialogUtils.startProgressDialog(SearchInvoiceActivity.this, "");
            ApiInterface apiService =
                    ApiClient.getClient(this).create(ApiInterface.class);

            String token = MyApplication.getUserToken();

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", token);

            Call<Object> call = apiService.searchInvoice(headerMap,body);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    final JSONObject body;
                    try {
                        body = new JSONObject(new Gson().toJson(response.body()));
                        if(body.getJSONObject("data").has("invoices"))
                            if (body.getJSONObject("data").getJSONObject("invoices").has("count") && body.getJSONObject("data").getJSONObject("invoices").getInt("count")>0) {
                                invoices = body.getJSONObject("data").getJSONObject("invoices").getJSONArray("rows");

                                Log.d(TAG, "Invoice Body::" + body);
                                invoicesList =jsonArrayToList(invoices);
                                    searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList, SearchInvoiceActivity.this,isCheckFlag);
                                    recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);

                            }else if( body.getJSONObject("data").getJSONObject("invoices").getInt("count")==0){
                                DialogUtils.showToast(SearchInvoiceActivity.this,"No record found");
                            }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    progressDialog.dismiss();
                }
            });
        } else {
            DialogUtils.showToast(SearchInvoiceActivity.this, getString(R.string.no_internet));
        }
    }


    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isSearchInvscreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.btnSearch) {
                                SharedPreferences sharedPref =
                                        SearchInvoiceActivity.this.getSharedPreferences(SearchInvoiceActivity.this.getString(R.string.preference_file_key),
                                                SearchInvoiceActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isSearchInvscreenIntroShown", true);
                                editor.commit();
                            } else {
                                startSpotLight(findViewById(R.id.btnSearch), "Search", "Click here to search invoice.");

                            }
                        }
                    })
                    .build()
                    .show();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth, int yearEnd, int monthOfYearEnd, int dayOfMonthEnd) {
        String date = dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        try {
            from = formatter2.parse((dayOfMonth<=9?"0"+dayOfMonth:dayOfMonth)+"/"+((monthOfYear+1)<=9?"0"+(monthOfYear+1):(monthOfYear+1))+"/"+year);
            to = formatter2.parse((dayOfMonthEnd<=9?"0"+dayOfMonthEnd:dayOfMonthEnd)+"/"+((monthOfYearEnd+1)<=9?"0"+(monthOfYearEnd+1):(monthOfYearEnd+1))+"/"+yearEnd);
        } catch (ParseException e) {
            e.printStackTrace();
        };
        String date1 = " : "+dayOfMonthEnd+"/"+(monthOfYearEnd+1)+"/"+yearEnd;
        toAndFromDate.setText(date +date1);
    }

    private void startDownloadingInvoices() {
        Util.postEvents("Download Selected Invoices","Download Selected Invoices",this.getApplicationContext());

        for (int i=0;i<invoices.length();i++){
            try {
                if(invoices.getJSONObject(i).has("download") && invoices.getJSONObject(i).getBoolean("download")) {
                    DownloadManager.Request r = null;
                    if (invoices.getJSONObject(i).getString("pdfLink") != null && invoices.getJSONObject(i).getString("pdfLink").startsWith("http")) {
                        String downloadLink = invoices.getJSONObject(i).getString("pdfLink");
                        if(!downloadLink.contains("https://"))
                            downloadLink = downloadLink.replace("http://", "https://");
                        r = new DownloadManager.Request(Uri.parse(downloadLink));
                        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Invoice_" + invoices.getJSONObject(i).getLong("id") + ".pdf");
                        r.allowScanningByMediaScanner();
                        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(r);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        DialogUtils.showToast(this,"Downloading started");
    }
    public void downloadInvoice(View v) {
        // check to see if we have permission to storage
//        checkPermission();
        if(hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED){
            startDownloadingInvoices();
        }
        else{
            checkPermission(REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    private void checkPermission(int PERMISSION_CODE){
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            DialogUtils.showAlertDialog(SearchInvoiceActivity.this, "Allow", "Deny", "For downloading invoice we need write access to storage", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_CODE);
                            }
                            return;
                        }

                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_CODE);
                    }
                }

                @Override
                public void negativeButtonClick() {
                }
            });
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
                    hasWriteStoragePermission = PackageManager.PERMISSION_GRANTED;
                    startDownloadingInvoices();
                } else {
                    // Permission Denied
                    hasWriteStoragePermission = PackageManager.PERMISSION_DENIED;
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
                break;
            case REQUEST_CODE_ASK_PERMISSIONS_SAVE_INVOICE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    hasWriteStoragePermission = PackageManager.PERMISSION_GRANTED;
                    saveInvoice();
                } else {
                    // Permission Denied
                    hasWriteStoragePermission = PackageManager.PERMISSION_DENIED;
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onclick(int positon, JSONObject obj) {
        try {
            invoices.put(positon,obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveInvoice(){

        Util.postEvents("Save","Save",getApplicationContext());
        try {
            JSONObject currentInvoice = invoices.getJSONObject(saveInvoiceId);
            Intent i = new Intent(Intent.ACTION_VIEW);

            if(currentInvoice.get("pdfLink")!=null) {
                i.setData(Uri.parse(currentInvoice.getString("pdfLink")));
                this.startActivity(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSaveButtonClick(int invoicePosition) {
        saveInvoiceId = invoicePosition;
        Log.v("Invoice", "Saving invoice");
        if(hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED){
            saveInvoice();
        }else{
            checkPermission(REQUEST_CODE_ASK_PERMISSIONS_SAVE_INVOICE);
        }
    }

}