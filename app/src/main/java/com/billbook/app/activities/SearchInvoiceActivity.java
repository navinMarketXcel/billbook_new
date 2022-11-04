package com.billbook.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andrewjapar.rangedatepicker.CalendarPicker;
import com.billbook.app.adapter_bill_callback.BillCallback;
import com.billbook.app.adapters.InvoiceListAdapter;
import com.billbook.app.database.models.Customer;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.model.InvoicesData;
import com.billbook.app.model.MasterItem;
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
import com.google.gson.JsonParser;

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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;


public class SearchInvoiceActivity extends AppCompatActivity implements View.OnClickListener,SearchInvoiceListAdapterNew.SearchInvoiceItemClickListener, DatePickerDialog.OnDateSetListener, OnDownloadClick, BillCallback {
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
    private TextView sortTv, filterTv;
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
    SimpleDateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
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
        filterTv = findViewById(R.id.filterTv);
        //Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        startSpotLight(edtMobileNo, "Mobile No", "Enter Mobile no.");
        hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        dateSpinner = findViewById(R.id.dateSpinner);
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("click postion"+i);
                edtMobileNo.setText("");
                if(adapterView.getItemAtPosition(i).equals("Custom Period")) {
                    dateSpinner.setSelection(0);
                    BottomSheetDialog gstSheet = new BottomSheetDialog(SearchInvoiceActivity.this, R.style.BottomSheetDialogTheme);
                    View bottomSheet = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_date_range_picker,null);
                    CalendarPicker calendarPicker = bottomSheet. findViewById(R.id.calendar_view);
                    TextView txtFrom=bottomSheet. findViewById(R.id.txtFrom);
                    TextView txtTo=bottomSheet. findViewById(R.id.txtTo);
                    String startDates = "",endDates="";
                    Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    startDate.add(Calendar.MONTH, -6);
                    calendarPicker. setRangeDate(startDate.getTime(),endDate.getTime() );
                    calendarPicker.setMode(CalendarPicker.SelectionMode.RANGE);
                    calendarPicker.scrollToDate(startDate.getTime());
                    calendarPicker.setOnRangeSelectedListener((Date date, Date date2, String s1, String s2) -> {
                                txtFrom.setText(s1);
                                txtTo.setText(s2);
                                //  DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

                                String strDate = myFormat1.format(date);
                                String endsDate = myFormat1.format(date2);
                                getInvoicesCallSearch(strDate,endsDate);
                                gstSheet.dismiss();
                                return null;
                            }


                    );
                    calendarPicker.setOnStartSelectedListener((date, s) ->
                    {
                        txtFrom.setText(s);
                        txtTo.setText("-");
                        return null;
                    });
                    gstSheet.setContentView(bottomSheet);
                    gstSheet.show();

                }


                if(adapterView.getItemAtPosition(i).equals("Last 7 Days"))
                {
                    Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    startDate.add(Calendar.DATE, -7);
                    Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    String strDate = myFormat1.format(startDate.getTime());
                    String endsDate = myFormat1.format(endDate.getTime());
                    getInvoicesCallSearch(strDate,endsDate);
                }
                else if (adapterView.getItemAtPosition(i).equals("Last 30 Days"))
                {
                    Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    startDate.add(Calendar.MONTH, -1);
                    Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    String strDate = myFormat1.format(startDate.getTime());
                    String endsDate = myFormat1.format(endDate.getTime());
                    getInvoicesCallSearch(strDate,endsDate);
                }
                else if (adapterView.getItemAtPosition(i).equals("This Month"))
                {
                    Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    startDate.add(Calendar.MONTH, -1);
                    Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    String strDate = myFormat1.format(startDate.getTime());
                    String endsDate = myFormat1.format(endDate.getTime());
                    getInvoicesCallSearch(strDate,endsDate);
                }
                else if (adapterView.getItemAtPosition(i).equals("Last Month"))
                {
                    Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    startDate.add(Calendar.MONTH, -1);
                    Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    String strDate = myFormat1.format(startDate.getTime());
                    String endsDate = myFormat1.format(endDate.getTime());
                    getInvoicesCallSearch(strDate,endsDate);
                }
                else if (adapterView.getItemAtPosition(i).equals("Today"))
                {
                    Calendar startDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    // startDate.add(Calendar.MONTH, -1);
                    Calendar endDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
                    String strDate = myFormat1.format(startDate.getTime());
                    String endsDate = myFormat1.format(endDate.getTime());
                    getInvoicesCallSearch(strDate,endsDate);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        setonClick();


    }
    public void setonClick(){
        ImageView iv = findViewById(R.id.ivToolBarBack);
        LinearLayout help = findViewById(R.id.lnHelp);
        LinearLayout youTube = findViewById(R.id.lnYouTube);
        iv.setOnClickListener(v -> {
            finish();
        });
        help.setOnClickListener(v -> {
            Util. startHelpActivity(SearchInvoiceActivity.this);
        });
        youTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(SearchInvoiceActivity.this);
        });

    }

    public void clickSelect(View v)
    {
        TextView selecttv= findViewById(R.id.selectTv);
        TextView sortTv = findViewById(R.id.sortTv);
        TextView filterTv = findViewById(R.id.filterTv);
        Button delete = findViewById(R.id.deleteButton);
        Button download = findViewById(R.id.downloadAll);
        if(isCheckFlag)
        {
            download.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
            sortTv.setVisibility(View.VISIBLE);
            filterTv.setVisibility(View.VISIBLE);
            selecttv.setText("Select");
            isCheckFlag=false;
        }
        else
        {
            download.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            sortTv.setVisibility(View.GONE);
            filterTv.setVisibility(View.GONE);
            selecttv.setText("Cancel");
            isCheckFlag=true;

        }
        searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList, SearchInvoiceActivity.this,isCheckFlag,SearchInvoiceActivity.this);
        recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
    }

    public void deleteBulkBills(View v)
    {
        Boolean isSelected=false;
        for (int i=0;i<invoicesList.size();i++){
            if(invoicesList.get(i).isSelected()) {
                isSelected=true;
            }
        }

        if(isSelected) {
            InvoicesData data = new InvoicesData();
            System.out.println(invoicesList.size());
            for (int i = 0; i < invoicesList.size(); i++) {
                System.out.println("in delete bills" + invoicesList.get(i).isSelected());
            }
            DialogUtils.showAlertDialog((Activity) SearchInvoiceActivity.this, "Yes", "No", "Confirm if you want to Delete all these bill", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {

                    if (Util.isNetworkAvailable(SearchInvoiceActivity.this)) {
                        final ProgressDialog progressDialog = DialogUtils.startProgressDialog(SearchInvoiceActivity.this, "");
                        ApiInterface apiService =
                                ApiClient.getClient(SearchInvoiceActivity.this).create(ApiInterface.class);

                        String token = MyApplication.getUserToken();
                        Map<String, String> headerMap = new HashMap<>();
                        headerMap.put("Authorization", token);
                        Call<Object> call = null;
                        try {
                            JSONObject inv = new JSONObject();
                            JSONArray invoiceIds = new JSONArray();
                            for (int i = 0; i < invoicesList.size(); i++) {
                                if (invoicesList.get(i).isSelected) {
                                    invoiceIds.put(invoicesList.get(i).getId());
                                }
                            }
                            inv.put("invoiceIDs", invoiceIds);
                            JsonObject jsonObject = new JsonParser().parse(inv.toString()).getAsJsonObject();
                            call = apiService.deleteSearchBills(headerMap, jsonObject);
                            call.enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> call, Response<Object> response) {
                                    final JSONObject body;
                                    try {
                                        body = new JSONObject(new Gson().toJson(response.body()));

                                        if (body.getBoolean("status")) {
                                            data.isActive = false;
                                            page = 1;
                                            getInvoicesCall();
                                            Toast.makeText(SearchInvoiceActivity.this, "Bills Successfully Deleted.", Toast.LENGTH_LONG).show();
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
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(SearchInvoiceActivity.this, SearchInvoiceActivity.this.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void negativeButtonClick() {

                }
            });

        }else{
            Toast.makeText(SearchInvoiceActivity.this, "Please select Bill", Toast.LENGTH_LONG).show();

        }
    }
    public void clickSort(View v)
    {
        TextView bill_no,bdate,dateMod;
        BottomSheetDialog sortSheet = new BottomSheetDialog(SearchInvoiceActivity.this,R.style.BottomSheetDialogTheme);
        View sortBottomSheet = LayoutInflater.from(getApplicationContext()).inflate(R.layout.sort_layout,(LinearLayout)findViewById(R.id.sortLayout));
        Button bno,dm,bd;
        bno = sortBottomSheet.findViewById(R.id.bnButton);
        dm = sortBottomSheet.findViewById(R.id.dmButton);
        bd = sortBottomSheet.findViewById(R.id.bdButton);
        bill_no = sortBottomSheet.findViewById(R.id.billno);
        bill_no.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                bno.setVisibility(View.VISIBLE);
                dm.setVisibility(View.GONE);
                bd.setVisibility(View.GONE);

//
                invoicesList = (ArrayList<InvoicesData>)jsonArrayToList(invoices).stream().sorted(Comparator.comparing(InvoicesData::getBillNo)).collect(Collectors.toList());
                for (int i=0;i<invoicesList.size();i++){
                    System.out.println("BillNO:" + invoicesList.get(i).getBillNo() + " ,Amount" + invoicesList.get(i).getTotalAmount());
                }
                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList,null,isCheckFlag,SearchInvoiceActivity.this);
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
                bno.setVisibility(View.GONE);
                dm.setVisibility(View.GONE);
                bd.setVisibility(View.VISIBLE);

                invoicesList= (ArrayList<InvoicesData>)jsonArrayToList(invoices).stream().sorted(Comparator.comparing(InvoicesData::getInvoiceDate)).collect(Collectors.toList());
                for (int i=0;i<invoicesList.size();i++){
                    System.out.println(invoicesList.get(i).getInvoiceDate());
                }
                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList,null,isCheckFlag,SearchInvoiceActivity.this);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerViewInvoice.setLayoutManager(layoutManager);
                recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
                recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);

                sortSheet.dismiss();

            }
        });
        sortBottomSheet.findViewById(R.id.canelSort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortSheet.dismiss();
            }
        });
        dateMod = sortBottomSheet.findViewById(R.id.dateMod);
        dateMod.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                bno.setVisibility(View.GONE);
                dm.setVisibility(View.VISIBLE);
                bd.setVisibility(View.GONE);
                invoicesList= (ArrayList<InvoicesData>)jsonArrayToList(invoices).stream().sorted(Comparator.comparing(InvoicesData::getUpdatedAt)).collect(Collectors.toList());
                Collections.reverse(invoicesList);
                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList,null,isCheckFlag,SearchInvoiceActivity.this);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerViewInvoice.setLayoutManager(layoutManager);
                recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
                recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);

                sortSheet.dismiss();
            }
        });

        sortSheet.setContentView(sortBottomSheet);
        sortSheet.show();
    }

    public void clickFilter(View v){
        TextView gstBills, nonGstBills, allBills;
        BottomSheetDialog filterSheet = new BottomSheetDialog(SearchInvoiceActivity.this,R.style.BottomSheetDialogTheme);
        View filterBottomSheet = LayoutInflater.from(getApplicationContext()).inflate(R.layout.searchbill_filter_sort,(LinearLayout)findViewById(R.id.filter_Layout));
        gstBills = filterBottomSheet.findViewById(R.id.gstBills);
        gstBills.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                ArrayList<InvoicesData> nonGstBillList = new ArrayList<>();
                jsonArrayToList(invoices).stream().forEach(invoice -> {
                    if(invoice.getGstBillNo() > 0){
                        Log.v("Invoicesss", String.valueOf(invoice));
                        nonGstBillList.add(invoice);
                    }
                });
                Log.v("NonGSTBILLS", String.valueOf(nonGstBillList));
                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,nonGstBillList,null,isCheckFlag,SearchInvoiceActivity.this);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerViewInvoice.setLayoutManager(layoutManager);
                recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
                recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
                filterSheet.dismiss();
            }
        });
        nonGstBills = filterBottomSheet.findViewById(R.id.nongstBills);
        nonGstBills.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                ArrayList<InvoicesData> nonGstBillList = new ArrayList<>();
                jsonArrayToList(invoices).stream().forEach(invoice -> {
                    if(invoice.getGstBillNo() == 0){
                        Log.v("Invoicesss", String.valueOf(invoice));
                        nonGstBillList.add(invoice);
                    }
                });
                Log.v("GSTBILLS", String.valueOf(nonGstBillList));
                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,nonGstBillList,null,isCheckFlag,SearchInvoiceActivity.this);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerViewInvoice.setLayoutManager(layoutManager);
                recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
                recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
                filterSheet.dismiss();
            }
        });
        allBills = filterBottomSheet.findViewById(R.id.allBills);
        allBills.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList,null,isCheckFlag,SearchInvoiceActivity.this);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerViewInvoice.setLayoutManager(layoutManager);
                recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
                recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
                filterSheet.dismiss();
            }
        });
        filterSheet.setContentView(filterBottomSheet);
        filterSheet.show();
    }
    public ArrayList<InvoicesData> jsonArrayToList(JSONArray jsonArray)
    {
        ArrayList<InvoicesData> invoicesListData = new ArrayList<InvoicesData>();
        JSONArray masterItems = new JSONArray();


        //Checking whether the JSON array has some value or not
        if (jsonArray != null) {
            //Iterating JSON array

            for (int i=0;i<jsonArray.length();i++){
                //Adding each element of JSON array into ArrayList
                InvoicesData invoiceData = new InvoicesData();
                try {
                    JSONObject   obj = jsonArray.getJSONObject(i);
                    JSONObject cuObj = jsonArray.getJSONObject(i).getJSONObject("customer");
                    masterItems = jsonArray.getJSONObject(i).getJSONArray("masterItems");
                    System.out.println(masterItems);
                    invoiceData.setTotalAmount(obj.getDouble("totalAmount"));
                    invoiceData.setTotalAfterDiscount(obj.getDouble("totalAfterDiscount"));
                    invoiceData.setNonGstBillNo(obj.getInt("nonGstBillNo"));
                    invoiceData.setGstBillNo(obj.getInt("gstBillNo"));
                    invoiceData.setGSTNo(obj.getString("GSTNo"));
                    if(obj.has("pdfLink")){
                        invoiceData.setPdfLink(obj.getString("pdfLink"));
                    } else {
                        invoiceData.setPdfLink("");
                    }
                    invoiceData.setInvoiceDate(obj.getString("invoiceDate"));
                    invoiceData.setGstType(obj.getString("gstType"));
                    invoiceData.setUpdatedAt(obj.getString("updatedAt"));
                    invoiceData.setIsActive(obj.getBoolean("is_active"));
                    invoiceData.setId(obj.getInt("id"));
                    invoiceData.setCustomer(new Customer(cuObj.getString("name"),cuObj.getString("mobileNo"),false,cuObj.getString("address")));
                    invoiceData.setDiscount(obj.getDouble("discount"));
                    List<MasterItem> masterArrayList = new ArrayList<MasterItem>();
                    for(int j=0; j < masterItems.length();j++)
                    {
                        MasterItem master = new MasterItem();
                        JSONObject  masterObject = masterItems.getJSONObject(j);

                        master.setName(masterObject.getString("name"));
                        master.setId(masterObject.getInt("id"));
                        master.setGst(masterObject.getInt("gst"));
                        master.setGstAmount(masterObject.getDouble("gstAmount"));
                        master.setGstType(masterObject.getString("gstType"));
                        master.setTotalAmount(masterObject.getDouble("totalAmount"));
                        master.setInvoiceid(masterObject.getInt("invoiceid"));
                        master.setPrice(masterObject.getDouble("price"));
                        master.setQuantity(masterObject.getInt("quantity"));
                        masterArrayList.add(master);
                    }
                    invoiceData.setMasterItems(masterArrayList);
                    invoicesListData.add(invoiceData);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
    private void getInvoicesCallSearch(String startDate,String endDate){
        System.out.println("search bills api"+startDate+" ___________ "+endDate);
        Map<String, String> body = new HashMap<>();
        body.put("userid",userid+"");
        body.put("page",""+page);
        body.put("startDate",startDate);
        body.put("endDate",endDate);
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
        Util.dailyLogout(SearchInvoiceActivity.this);
        try {
            getInvoicesCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        btnSearch = findViewById(R.id.btnSearch);
        toAndFromDate=findViewById(R.id.toAndFromDate);
        btnSearch.setOnClickListener(this);
        recyclerViewInvoice = findViewById(R.id.recyclerViewInvoice);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewInvoice.setLayoutManager(mLayoutManager);
        recyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());
//        searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(this,invoicesList, this,isCheckFlag,SearchInvoiceActivity.this);
//        recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
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
                                searchInvoiceListAdapter = new SearchInvoiceListAdapterNew(SearchInvoiceActivity.this,invoicesList, SearchInvoiceActivity.this,isCheckFlag,SearchInvoiceActivity.this);
                                recyclerViewInvoice.setAdapter(searchInvoiceListAdapter);
                                searchInvoiceListAdapter.notifyDataSetChanged();
                                if(invoicesList.size()==0)
                                {
                                    TextView tv = findViewById(R.id.tvRecordNotFound);
                                    tv.setVisibility(View.VISIBLE);
                                    searchInvoiceListAdapter.notifyDataSetChanged();
                                }
                                else
                                {
                                    tvRecordNotFound.setVisibility(View.GONE);
                                }

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

        for (int i=0;i<invoicesList.size();i++){
            if(invoicesList.get(i).isSelected()){
                        DownloadManager.Request r = null;
                        if (!invoicesList.get(i).getPdfLink().isEmpty()  && invoicesList.get(i).getPdfLink().startsWith("http")) {
                            String downloadLink = invoicesList.get(i).getPdfLink();
                            if(!downloadLink.contains("https://"))
                                downloadLink = downloadLink.replace("http://", "https://");
                            r = new DownloadManager.Request(Uri.parse(downloadLink));
                            r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Invoice_" + invoicesList.get(i).getId() + ".pdf");
                            r.allowScanningByMediaScanner();
                            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                            dm.enqueue(r);
                        }

            }
/*
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
            }*/

        }
        DialogUtils.showToast(this,"Downloading started");
    }
    public void downloadInvoice(View v) {
        // check to see if we have permission to storage
//        checkPermission();
        Boolean isSelected=false;
        for (int i=0;i<invoicesList.size();i++){
            if(invoicesList.get(i).isSelected()) {
                isSelected=true;
            }
            }
        if(isSelected) {
            if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
                startDownloadingInvoices();
                Toast.makeText(SearchInvoiceActivity.this, "Bills Successfully Downloaded.", Toast.LENGTH_LONG).show();
            } else {
                checkPermission(REQUEST_CODE_ASK_PERMISSIONS);
            }
        }else{
            Toast.makeText(SearchInvoiceActivity.this, "Please select Bill", Toast.LENGTH_LONG).show();

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
    public void gotodeletebills(InvoicesData data) {
        DialogUtils.showAlertDialog(SearchInvoiceActivity.this, getResources().getString(R.string.yes), getResources().getString(R.string.no), getResources().getString(R.string.billing_delete_button), new DialogUtils.DialogClickListener() {
            @Override
            public void positiveButtonClick() {
                if (Util.isNetworkAvailable(SearchInvoiceActivity.this)) {
                    final ProgressDialog progressDialog = DialogUtils.startProgressDialog(SearchInvoiceActivity.this, "");
                    ApiInterface apiService =
                            ApiClient.getClient(SearchInvoiceActivity.this).create(ApiInterface.class);

                    String token = MyApplication.getUserToken();
                    Map<String, String> headerMap = new HashMap<>();
                    headerMap.put("Authorization", token);
                    Call<Object> call = null;
                    try {
                        JSONObject inv = new JSONObject();
                        inv.remove("masterItems");
                        inv.put("is_active", false);
                        JsonObject jsonObject = new JsonParser().parse(inv.toString()).getAsJsonObject();
                        call = apiService.updateInvoice(headerMap, data.id, jsonObject);
                        call.enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                final JSONObject body;
                                try {
                                    body = new JSONObject(new Gson().toJson(response.body()));

                                    if (body.getBoolean("status")) {
                                        data.isActive = false;
                                        page = 1;
                                        getInvoicesCall();
                                        Toast.makeText(SearchInvoiceActivity.this, "Bill Successfully Deleted.", Toast.LENGTH_LONG).show();
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
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(SearchInvoiceActivity.this, SearchInvoiceActivity.this.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void negativeButtonClick() {

            }
        });
    }
    public void goToEditBills(InvoicesData data)
    {

        JSONObject requestInvoice = new JSONObject();
        JSONArray masterItems = new JSONArray();
        JSONObject customer = new JSONObject();
        try {
            requestInvoice.put("totalAmount",data.getTotalAmount());
            requestInvoice.put("gstBillNo",data.getGstBillNo());
            requestInvoice.put("invoiceDate",data.getInvoiceDate());
            requestInvoice.put("id",data.getId());
            customer.put("address",data.getCustomer().getCustomerAddress());
            customer.put("name",data.getCustomer().getCustomerNameame());
            customer.put("mobileNo",data.getCustomer().getMobileNo());
            //customer.put("customerAddress",data.getCustomer().getCustomerAddress());
            requestInvoice.put("customer",customer);
            requestInvoice.put("totalAfterDiscount",data.getTotalAfterDiscount());
            requestInvoice.put("pdfLink",data.getPdfLink());
            requestInvoice.put("gstType",data.getGstType());
            requestInvoice.put("nonGstBillNo",data.getNonGstBillNo());
            requestInvoice.put("GSTNo",data.getGSTNo());
            requestInvoice.put("discount",data.getDiscount());
            for(int i =0;i<data.getMasterItems().size();i++)
            {
                JSONObject masterObject = new JSONObject();

                masterObject.put("name",data.getMasterItems().get(i).name);
                masterObject.put("invoiceid",data.getMasterItems().get(i).invoiceid);
                masterObject.put("totalAmount",data.getMasterItems().get(i).totalAmount);
                masterObject.put("price",data.getMasterItems().get(i).price);
                masterObject.put("gstAmount",data.getMasterItems().get(i).gstAmount);
                masterObject.put("userId",data.getMasterItems().get(i).userId);
                masterObject.put("gstType",data.getMasterItems().get(i).gstType);
                masterObject.put("id",data.getMasterItems().get(i).id);
                masterObject.put("quantity",data.getMasterItems().get(i).quantity);
                masterObject.put("gst",data.getMasterItems().get(i).gst);
                masterItems.put(masterObject);

            }
            requestInvoice.put("masterItems",masterItems);

//            requestInvoice.setTotalAfterDiscount(obj.getInt("totalAfterDiscount"));
//            requestInvoice.setNonGstBillNo(obj.getInt("nonGstBillNo"));
//            requestInvoice.setGstBillNo(obj.getInt("gstBillNo"));
//            requestInvoice.setGSTNo(obj.getString("GSTNo"));
//            if(obj.has("pdfLink")){
//                requestInvoice.setPdfLink(obj.getString("pdfLink"));
//            } else {
//                requestInvoice.setPdfLink(null);
//            }
//            requestInvoice.setInvoiceDate(obj.getString("invoiceDate"));
//            requestInvoice.setGstType(obj.getString("gstType"));
//            requestInvoice.setUpdatedAt(obj.getString("updatedAt"));
//            requestInvoice.setIsActive(obj.getBoolean("is_active"));
//            requestInvoice.setId(obj.getInt("id"));
//            requestInvoice.setCustomer(new Customer(cuObj.getString("name"),cuObj.getString("mobileNo"),false));
            //            "id":1413223,
//                    "name":"car",
//                    "quantity":11,
//                    "measurementId":1,
//                    "price":123,
//                    "gstType":"CGST\/SGST (Local customer)",
//                    "gstAmount":1353,
//                    "gst":0,
//                    "serial_no":"",
//                    "imei":"",
//                    "totalAmount":1353,
//                    "invoiceid":365285,
//                    "userId":187739,
//                    "is_active":true,
//                    "createdAt":"2022-06-10T12:13:44.000Z",
//                    "updatedAt":"2022-06-10T12:13:44.000Z"



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Util.postEvents("Edit","Edit",getApplicationContext());

        Intent intent = new Intent(this, BillingNewActivity.class);
        String gstNoList,NongstNoList;
        intent.putExtra("edit",true);
        intent.putExtra("gstBillNo",data.getGstBillNo());
        intent.putExtra("nonGstBillNo",data.getNonGstBillNo());

        intent.putExtra("gstBillNoList",data.getGstBillNo());
        gstNoList = getIntent().hasExtra("gstBillNoList")?getIntent().getExtras().getString("gstBillNoList"): String.valueOf(1);
        NongstNoList =getIntent().hasExtra("nonGstBillNoList")? getIntent().getExtras().getString("nonGstBillNoList"): String.valueOf(1);
        intent.putExtra("gstBillNoList",gstNoList);
        intent.putExtra("nonGstBillNoList",NongstNoList);
        intent.putExtra("invoice",requestInvoice.toString());





        startActivity(intent);

    }
    @Override
    public void callback(String action, InvoicesData data, Integer pos)
    {
        if(action.equals("delete"))
        {
            gotodeletebills(data);
        }
        if(action.equals("edit"))
        {
            System.out.println("size"+data.getMasterItems().size());
            goToEditBills(data);
        }
        if(action.equals("Selected")) {
            InvoicesData mData = data;
            mData.setSelected(true);
            invoicesList.set(pos, mData);
        }
        if(action.equals("unSelected")) {
            InvoicesData mData = data;
            mData.setSelected(false);
            invoicesList.set(pos, mData);
        }
    }


}