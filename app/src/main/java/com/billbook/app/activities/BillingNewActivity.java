package com.billbook.app.activities;

import static com.billbook.app.activities.MyApplication.context;
import static com.google.android.datatransport.runtime.dagger.internal.InstanceFactory.create;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.core.content.ContextCompat;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.databinding.ActivityBillingNewBinding;
import com.billbook.app.databinding.LayoutItemBillBinding;
import com.billbook.app.networkcommunication.NetworkType;
import com.billbook.app.viewmodel.InvoiceItemsViewModel;
import com.billbook.app.viewmodel.InvoiceViewModel;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.billbook.app.R;
import com.billbook.app.adapters.ModelAdapter;
import com.billbook.app.adapters.NewBillingAdapter;
import com.billbook.app.database.models.Model;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.viewmodel.ModelViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.lang.Math.min;

public class BillingNewActivity extends AppCompatActivity implements NewBillingAdapter.onItemClick {
    private ModelViewModel modelViewModel;
    private InvoiceItemsViewModel invoiceItemViewModel;
    final String TAG = "BillingNewActivity";
    private ArrayList models = new ArrayList<Model>();
    //    private ArrayList<NewInvoiceModels> newInvoiceModels = new ArrayList<>();
    private ArrayList<InvoiceItems> invoiceItemsList = new ArrayList<>();
    private ArrayList<InvoiceItems> invoiceItemEditModel = new ArrayList<>();
    private InvoiceModelV2 currInvoiceToUpdate;
    private InvoiceViewModel invoiceViewModel;
    private ModelAdapter modelAdapter;
    private NewBillingAdapter newBillingAdapter;
    private float total = 0, totalBeforeGST = 0, discountPercent = 0, discountAmt = 0, shortBillGstAmt = 0;
    private String invoiceDateStr, gstBllNo, nonGstBillNo;
    private Date invoiceDate;
    private Gson gson = new Gson();
    private List<String> gstTypeList, measurementUnitTypeList;
    private JSONArray gstList1, nonGstList1;
    private ArrayList<String> gstList2, nonGstList2;
    private JSONObject profile;
    private ArrayList<String> gstList, nonGstList;
    private boolean isGSTAvailable;
    private String gstNo;
    private int editPosition = -1, quantityCount = 0;
    private int invoiceIdIfEdit = -1;
    private int serialNumber = 0;
    private int hasWriteStoragePermission, isFirstReq = 1;
    private final int GRANT_STORAGE_PERMISSION =1;
    private boolean isEdit = false;
    private JSONObject invoice;
    private BottomSheetClass customDialogClass,customDialogClass1;

    private ActivityBillingNewBinding binding;
    private LayoutItemBillBinding billItemBinding;
    private long localInvoiceId, idInLocalDb;
    private ArrayList<String> itemsList = new ArrayList<>();
    private ArrayList<String> customerList = new ArrayList<>();
    private ArrayList<Integer> unitList = new ArrayList<>();
    ArrayAdapter<String> itemAdapter;
    ArrayAdapter<String> customerAdapter;
    //variables for Checking Contact Permission
    private ImageButton imageButton;
    private EditText edtname;
    private EditText edtMobNo, billNo,bill_date;
    private TextView additemTv;
    private static final int Contact_code=123;
    private static final int Contact_Pick_code=111;
    private boolean ischeckDisc = true;
    private int count = 0;
    private LinearLayout itemslay;
    private NetworkType nt;
    private String networkType;
    String gstListString;
    String nonGstListString;
    CleverTapAPI cleverTapAPI;
    private String networkSpeed,apiName;
    private TextView viewDets;
    private  int countIsEdit =0 ;
    // idInLocalDb = column with name "id" in local db android

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        binding = ActivityBillingNewBinding.inflate(getLayoutInflater());
        billItemBinding = binding.includedLayoutItemBill;
        invoiceItemViewModel = ViewModelProviders.of(this).get(InvoiceItemsViewModel.class);
        //imageButton=findViewById(R.id.button1);
        edtname= findViewById(R.id.edtName);
        edtMobNo= findViewById(R.id.edtMobNo);
        nt = new NetworkType();
        networkType = nt.getNetworkClass(this);
        nt = new NetworkType();
        billNo = findViewById(R.id.billNo);
        bill_date = findViewById(R.id.bill_date);
        viewDets = findViewById(R.id.viewDets);
        itemslay=findViewById(R.id.itemsLayout);
        try {
            gstBllNo = getIntent().hasExtra("gstBillNoList")?getIntent().getExtras().getString("gstBillNoList"): String.valueOf(1);
            nonGstBillNo =getIntent().hasExtra("nonGstBillNoList")? getIntent().getExtras().getString("nonGstBillNoList"): String.valueOf(1);
            gstBllNo = gstBllNo.substring(1, gstBllNo.length() - 1);
            nonGstBillNo =nonGstBillNo.substring(1, nonGstBillNo.length() - 1);
            nonGstList = new ArrayList<>(Arrays.asList(nonGstBillNo.split(",")));
            gstList = new ArrayList<>(Arrays.asList(gstBllNo.split(",")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        View view = binding.getRoot();
        setContentView(view);

        boolean medit = getIntent().hasExtra("edit");
        if(medit)
        {
            isEdit = true;
            int gstBillNo = 0;
            try {
                invoice = new JSONObject(getIntent().getStringExtra("invoice"));
                gstBillNo = invoice.getInt("gstBillNo");
                if(gstBillNo>0)
                {
                    isGSTAvailable=true;
                    Log.v("gstBillNo in if load", String.valueOf(gstBillNo));
                }
                else
                {
                    isGSTAvailable = false;
                    Log.v("gstBillNo load", String.valueOf(gstBillNo));
                }
            }

            catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else
        {
            isEdit = false;
            isGSTAvailable = MyApplication.getIsGst();
        }
        Log.v("isedit in oncre", String.valueOf(medit));
        Log.v("isgst avail oncr", String.valueOf(isGSTAvailable));
        if(!isEdit){
            localInvoiceId = MyApplication.getLocalInvoiceId();
            MyApplication.setLocalInvoiceId(localInvoiceId+1);
        }


        internalStoragePermission();
        getUSerData();
        getMeasurementUnit();
        initUI();
        checkIsEdit();
        loadDataForInvoice();
        getInvoiceItemsFromDatabase();
        searchItemAutoComplete();
        customerNumberAutoComplete();
        setonClick();
//        onKeyDown(13,);
    }
    public void onClickViewDets(View v)
    {
        viewDets = findViewById(R.id.viewDets);
        ScrollView sv = findViewById(R.id.mainSv);
        sv.scrollTo(0, sv.getMaxScrollAmount ());
    }
    public void setonClick(){
        binding.ivToolBarBack.setOnClickListener(v -> {
            DialogUtils.showAlertDialog(BillingNewActivity.this, getResources().getString(R.string.yes), getResources().getString(R.string.no), getResources().getString(R.string.billing_back_button), new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    finish();
                }

                @Override
                public void negativeButtonClick() {

                }
            });

        });
        binding.lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(BillingNewActivity.this);
        });
        binding.lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(BillingNewActivity.this);
        });
        billItemBinding.addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItem();
            }
        });
        binding.edtName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() >= (binding.edtName.getRight() - binding.edtName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        clickButton();
                        return true;
                    }
                }
                return false;
            }
        });
    }


    //Functions For Checking Contact permissions
    public void clickButton()
    {
        // check for contact permission
        if(checkContactPermission())
        {
            pickContactIntent();
        }
        else {
            requestContactPermission();
        }

    }
    private boolean checkContactPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)==(PackageManager.PERMISSION_GRANTED);
        //requestContactPermission();
        return result;
        // returns true if permission is granted else false
    }
    private void requestContactPermission()
    {
        String[] permission={Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this,permission,Contact_code);
    }
    private void pickContactIntent()
    {
        Intent intent=new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
       // startActivityForResult(intent,Contact_Pick_code);
        contactPickerResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==Contact_code)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                //Permission is granted here
                pickContactIntent();
            }
            else
            {
                //permission denied here
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initUI() {
        DateFormat formatter =
                new SimpleDateFormat("yyyy-MM-dd");
        invoiceDateStr = formatter.format(new Date());
        binding.billDate.setText(invoiceDateStr);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.invoiceItems.setLayoutManager(mLayoutManager);
        binding.invoiceItems.setItemAnimator(new DefaultItemAnimator());

        gstTypeList = Arrays.asList(getResources().getStringArray(R.array.gst_type));

        if(measurementUnitTypeList==null){
            measurementUnitTypeList = Arrays.asList(getResources().getStringArray(R.array.measurementUnit));
        }

        if (isGSTAvailable)
            serialNumber = MyApplication.getInVoiceNumber();
        else
            serialNumber = MyApplication.getInVoiceNumberForNonGst();

        binding.billNo.setText(""+ serialNumber);

        int showGstPopup = MyApplication.showGstPopup();
        //binding.GSTError.setVisibility(showGstPopup != 2 ? serialNumber < 3 ? View.VISIBLE : View.GONE : View.GONE);

        binding.gstType.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.tvAmountGST.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        Log.v("isGstAvail", String.valueOf(isGSTAvailable));
        binding.taxLabelTV.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.GSTTypeLblTV.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.gstTypeLayout.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.gstTitle.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);


        // billItemBinding.itemPriceET.setWidth(isGSTAvailable ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT);

        billItemBinding.gstPercentageTV.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        billItemBinding.gstPerLayout.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
       billItemBinding.hsnLayout.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);

        Spinner spinner = (Spinner) billItemBinding.unit;
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, measurementUnitTypeList);
        spinner.setAdapter(dataAdapter);

        billItemBinding.gstPercentage.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        if (isGSTAvailable) {
            billItemBinding.priceLblTV.setText(R.string.price_including_gst);
            billItemBinding.itemPriceET.setHint(R.string.enter_price_including_gst);
        }
        else
        {
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            buttonLayoutParams.setMargins(15, 0, 10, 0);
            billItemBinding.priceLblTV.setLayoutParams(buttonLayoutParams);

            billItemBinding.addPriceLayout.setLayoutParams(buttonLayoutParams);
            billItemBinding.itemPriceET.setLayoutParams(buttonLayoutParams);
        }

        // Discount Percentage will have high priority then Discount Amount on recalculation if total price is being edited.
        binding.edtDiscountPercent.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String substr;
                    if (getCurrentFocus() ==  binding.edtDiscountPercent) {
                        if (s.length() > 0) {
                            substr = s.toString();
                            if(substr.endsWith("%"))
                                substr = substr.substring(0, substr.length() - 1);

                            discountPercent = Float.parseFloat(substr);
                            discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, totalBeforeGST);
                            binding.edtDiscountAmt.setText(String.valueOf(discountAmt));
                            if (discountPercent > 100.00)
                                setEditTextError(binding.edtDiscountPercent, "Discount should be less than or equal to 100%");
                            else
                                setEditTextError(binding.edtDiscountPercent, "");
                        } else {
                            binding.edtDiscountAmt.getText().clear();
                            discountPercent = 0;
                            discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, totalBeforeGST);
                        }
                        setTotalAfterDiscount();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.edtDiscountAmt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (getCurrentFocus() == binding.edtDiscountAmt) {

                        if (s.toString().length() > 0) {
                            discountAmt = Float.parseFloat(s.toString());
                            discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, totalBeforeGST);
                            binding.edtDiscountPercent.setText(discountPercent + "%");

                            if (discountAmt > total)
                                setEditTextError(binding.edtDiscountAmt, "Discount value should be less than or equal to total");
                            else
                                setEditTextError(binding.edtDiscountAmt, "");
                        } else {
                            binding.edtDiscountPercent.getText().clear();
                            discountAmt = 0;
                            discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, total);
                        }
                        setTotalAfterDiscount();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



        binding.billNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0 && !isEdit){

                    try {

                        if(isGSTAvailable)
                        {

                            Log.v("Gst Bills list",gstList.toString());
                            if(gstList.contains(s.toString())){
                                binding.billNo.setError("Bill number already exist");

                                binding.nextBtn.setVisibility(View.GONE);
                            } else{
                                binding.nextBtn.setVisibility(View.VISIBLE);
                            }
                        }
                        else
                        {
                            Log.v("Non Gst Bills list",nonGstList.toString());
                            if(nonGstList.contains(s.toString())){

                                binding.billNo.setError("Bill number already exist");

                                binding.nextBtn.setVisibility(View.GONE);
                            } else{
                                binding.nextBtn.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }



                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.edtDiscountPercent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(binding.edtDiscountPercent.getText().length()>0)
                    binding.edtDiscountPercent.setText(discountPercent + "%");
            }
        });

//        billItemBinding.itemNameET.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                try {
//                    HashMap<String,String> req = new HashMap<>();
//                    req.put("userid", profile.getString("userid"));
//                    req.put("name", itemAdapter.getItem(position));
//                    fetchMeasurementIdApiCall(req,billItemBinding.unit);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        });

    }


    // for autocompletion on search Item (addition of first item)
    public void showAlertDialog()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Bill number already exist!");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    private void searchItemAutoComplete() {
        try {
//            ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,demo);
//            billItemBinding.itemNameET.setAdapter(itemAdapter);

            Map<String, String> req = new HashMap<>();
            req.put("userid", profile.getString("userid"));
            final Call<Object>[] call = new Call[]{null};

            billItemBinding.itemNameET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try{
                        //billItemBinding.itemNameET.showDropDown();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (s.length() > 2) {
                            req.put("name", s.toString());
                            //searchItemApiCall(call, (HashMap<String, String>) req,billItemBinding.itemNameET);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchItemApiCall(Call<Object>[] call, HashMap<String, String> req, AutoCompleteTextView view) {
        try {
            ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);

            call[0] = apiService.searchItem((HashMap<String, String>) req);
            call[0].enqueue(new Callback<Object>() {

                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    try {
                        if (response.body() != null) {
                            JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                            JSONObject data = body.getJSONObject("data");
                            JSONArray items = data.getJSONArray("items");
                            itemsList.clear();
                            for (int i = 0; i < min(items.length(),2); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                itemsList.add(obj.getString("name"));
                            }
                            itemAdapter = new ArrayAdapter<String>(BillingNewActivity.this, android.R.layout.simple_dropdown_item_1line, itemsList);
                            view.setAdapter(itemAdapter);
                            itemAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {

                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void fetchMeasurementIdApiCall(HashMap<String, String> req, Spinner unit) {
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        Call call = apiService.fetchMeasurementIdForItem((HashMap<String, String>) req);

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    JSONObject data = body.getJSONObject("data");
                    JSONArray items = data.getJSONArray("items");
                    if(items.length()>0) {
                        JSONObject obj = items.getJSONObject(0);
                        int index = obj.getInt("measurementId");
                        if (index > 0) {
                            unit.setSelection(index - 1);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    private void customerNumberAutoComplete(){
        try {
//            ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,demo);
//            billItemBinding.itemNameET.setAdapter(itemAdapter);

            Map<String, String> req = new HashMap<>();
            req.put("userid", profile.getString("userid"));
            final Call<Object>[] call = new Call[]{null};

            binding.edtName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try{
                        //binding.edtName.showDropDown();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        if (s.length() > 2) {
                            req.put("customerName", s.toString());
                            //searchCustomerApiCall(call, (HashMap<String, String>) req,binding.edtName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void searchCustomerApiCall(Call<Object>[] call, HashMap<String, String> req, AutoCompleteTextView view){
        try{
            ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);

            call[0] = apiService.findCustomer((HashMap<String, String>) req);
            call[0].enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    try {
                        if (response.body() != null) {
                            JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                            JSONObject data = body.getJSONObject("data");
                            Log.v("Customers", String.valueOf(body));
                            JSONArray items = data.getJSONArray("customer");
                            customerList.clear();
                            String customer = new String();
                            for (int i = 0; i < min(items.length(),2); i++) {
                                JSONObject obj = items.getJSONObject(i);
                                customer = new String();
                                String mobNo= obj.getString("mobileNo");
                                customer = customer + obj.getString("name")+" ";
                                if(!mobNo.equals(""))
                                {
                                    customer=customer+ "\n ("+ obj.getString("mobileNo")+")";

                                }

                                customerList.add(customer);
                            }
                            customerAdapter = new ArrayAdapter<String>(BillingNewActivity.this, android.R.layout.simple_list_item_1
                                    , customerList);
                            view.setAdapter(customerAdapter);
                            customerAdapter.notifyDataSetChanged();
                            view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    String customerData = customerList.get(position);
                                    int sizeCustomerdata=customerData.length();
                                    if(customerData.charAt(sizeCustomerdata-1)==')')
                                    {
                                        binding.edtName.setText(customerData.substring(0,customerData.length()-15));
                                        binding.edtMobNo.setText(customerData.substring(customerData.length() - 11, customerData.length()));
                                    }
                                    else
                                        binding.edtName.setText(customerData.substring(0,customerData.length()-1));

                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setEditTextError(EditText editText, String text) {
        if (text.length() <= 0)
            editText.setError(null);
        else
            editText.setError(text);
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
                Intent intent = new Intent(BillingNewActivity.this, StoragePermissionRequestActivity.class);
              //  startActivityForResult(intent, GRANT_STORAGE_PERMISSION);
                storagePermissionResultLauncher.launch(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    ActivityResultLauncher<Intent> contactPickerResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent data = result.getData();

                        Cursor cursor1,cursor2;
                        Uri uri=data.getData();
                        cursor1=getContentResolver().query(uri,null,null,null,null);
                        if(cursor1.moveToFirst())
                        {

                            @SuppressLint("Range") String contactName=cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            @SuppressLint("Range") String contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                            @SuppressLint("Range") String hasPhone = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                            if (Integer.parseInt(hasPhone) > 0) {
                                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                                String phoneNumber="";
                                while(phones.moveToNext())
                                { //iterate over all contact phone numbers
                                    phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                }
                                if(phoneNumber.length()>10)
                                {
                                    phoneNumber= phoneNumber.replaceAll("[^0-9]","");
                                    phoneNumber= phoneNumber.replaceAll("[//s]","");
                                    phoneNumber= phoneNumber.substring(phoneNumber.length()-10);
                                }
                                binding.edtMobNo.setText(phoneNumber);
                                phones.close();
                            }
                            binding.edtName.setText(contactName);
                        }
                        cursor1.close();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                }


            });

    ActivityResultLauncher<Intent> storagePermissionResultLauncher = registerForActivityResult(
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
                }else{
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();

                }


            });

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
         if (result != null) {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if (billItemBinding.layoutBillItemInitial.getVisibility() == View.VISIBLE) {
                    // billItemBinding.imeiNo.setText(billItemBinding.imeiNo.getText().toString().isEmpty() ? result.getContents() : billItemBinding.imeiNo.getText().toString() + "," + result.getContents());
                    billItemBinding.imeiNo.setText(result.getContents());
                } else if (customDialogClass.isShowing()) {
                    customDialogClass.imeiNo.setText(result.getContents());
                    //customDialogClass.imeiNo.setText(billItemBinding.imeiNo.getText().toString().isEmpty() ? result.getContents() : billItemBinding.imeiNo.getText().toString() + "," + result.getContents());
                }
        }
    }


    public void getMeasurementUnit(){
        try{
            List<String>onlineMeasurementUnit = MyApplication.getMeasurementUnits();
            if(onlineMeasurementUnit!=null)
                measurementUnitTypeList = onlineMeasurementUnit;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public void updateBillNo(View v) {
        new BillNumberUpdateDialog(this).show();
    }

    public void addMoreItem(View view) {
        Util.postEvents("Add More Item", "Add More Item", this.getApplicationContext());
        customDialogClass = new BottomSheetClass(this, null,measurementUnitTypeList);
        additemTv = findViewById(R.id.additemTv);
        customDialogClass.show();
        TextView viewNew = customDialogClass.findViewById(R.id.additemTv);
        Button button = customDialogClass.findViewById(R.id.add);
        viewNew.setText("Add New Item");
        TableRow table = customDialogClass.findViewById(R.id.deleteLayout);
        table.setVisibility(View.GONE);
        if(invoiceItemsList.size() == 0)
        {
            count = 0;
        }
        else
        {
            count = invoiceItemsList.size() + 1;
        }

//        binding.billDets.setText("Bill details"+"("+count+")");
//        billItemBinding.items.setText("Items"+"("+count+")");
        //additemTv.setText("Add New Item");
        countIsEdit = count;

    }

    public void addItem() {
        if (addItemVerify()) {
            Util.postEvents("Add Item", "Add Item", this.getApplicationContext());
            Toast.makeText(this, "Item successfully Added!", Toast.LENGTH_SHORT).show();

            count = invoiceItemsList.size() + 1;
            if(invoiceItemsList.size() < 0)
            {
                count =0;
            }
            else
            {
                binding.billDets.setText("Bill details"+"("+count+")");
                billItemBinding.items.setText("Items"+"("+count+")");
            }
            countIsEdit = count;
            TextView tv1 = findViewById(R.id.items);
            tv1.setVisibility(View.VISIBLE);
            LinearLayout ll = findViewById(R.id.viewDetsLay);
            ll.setVisibility(View.VISIBLE);
            billItemBinding.itemsLayout.setVisibility(View.VISIBLE);
//            addItem(itemNameET.getText().toString(),
//                    Float.parseFloat(itemPriceET.getText().toString()),
//                    Float.parseFloat(gstPercentage.getSelectedItemPosition()>0?gstPercentage.getSelectedItem().toString():"0")
//                    ,Float.parseFloat(itemQtyET.getText().toString()),
//                    true,
//                    imeiNo.getText().toString(),
//                    hsnNo.getText().toString(),
//                    measurementUnit.getSelectedItemPosition());
//            cardItemList.setVisibility(View.VISIBLE);
//            layoutBillItem_initial.setVisibility(View.GONE);
//            final String modelName, final float price, final float gst, final float quantity, boolean isNew, String imei, String hsnNo, final int measurementUnitId
            Button button =findViewById(R.id.addMoreItem);
            button.setVisibility(View.VISIBLE);
            addItemToDatabase(billItemBinding.itemNameET.getText().toString(),
                    Float.parseFloat(billItemBinding.itemPriceET.getText().toString()),
                    Float.parseFloat(billItemBinding.gstPercentage.getSelectedItemPosition() > 0 ? billItemBinding.gstPercentage.getSelectedItem().toString() : "0"),
                    Float.parseFloat(billItemBinding.itemQtyET.getText().toString()),
                    true,
                    billItemBinding.imeiNo.getText().toString(),
                    billItemBinding.HsnNoET.getText().toString(),
                    billItemBinding.unit.getSelectedItemPosition(),-1);


            binding.cardItemList.setVisibility(View.VISIBLE);
            billItemBinding.layoutBillItemInitial.setVisibility(View.GONE);
//            binding.tvTotal.setText(billItemBinding.itemPriceET.getText().toString());

        }


    }

    public void addItemToDatabase(final String modelName, final float price,final float gst, final float quantity, boolean isNew, String imei, String hsnNo, final int measurementUnitId, long masterItemId){
        // When editing an invoice item
        if(isNew==false){
            int localId = invoiceItemsList.get(editPosition).getLocalid();
            long curLocalInvoiceId = invoiceItemsList.get(editPosition).getLocalInvoiceId();
            setTotal(invoiceItemsList.get(editPosition), false);
            calculateAmountBeforeGST(invoiceItemsList.get(editPosition), false);
            InvoiceItems newInvoiceItem  = new InvoiceItems(measurementUnitId, modelName, quantity, price, gstTypeList.get(binding.gstType.getSelectedItemPosition()), ((price * 100) / (100 + gst)) * quantity,gst, true, 0, hsnNo, imei,quantity * price,invoiceIdIfEdit,0,localId,curLocalInvoiceId,masterItemId);
            invoiceItemViewModel.updateByLocalId(newInvoiceItem);
            setTotal(newInvoiceItem, true);
            calculateDiscount();
            calculateAmountBeforeGST(newInvoiceItem, true);
            return;
        }

        InvoiceItems newInvoiceItem = new InvoiceItems(measurementUnitId, modelName, quantity, price, gstTypeList.get(binding.gstType.getSelectedItemPosition()), ((price * 100) / (100 + gst)) * quantity, gst, true, 0,hsnNo,imei,quantity * price,invoiceIdIfEdit,0,1,localInvoiceId,masterItemId);
        invoiceItemViewModel.insert(newInvoiceItem);
        setTotal(newInvoiceItem, true);
        calculateDiscount();
        calculateAmountBeforeGST(newInvoiceItem, true);
    }

    private void getInvoiceItemsFromDatabase(){
        invoiceItemViewModel = ViewModelProviders.of(this).get(InvoiceItemsViewModel.class);
        invoiceItemViewModel.getInvoiceItems(localInvoiceId).observe(this, new Observer<List<InvoiceItems>>() {
            @Override
            public void onChanged(List<InvoiceItems> invoiceItems) {
                //invoiceItems is List but we need Array list
                // newBillingAdapter = new NewBillingAdapter(invoiceItems, BillingNewActivity.this, isGSTAvailable);
                // binding.invoiceItems.setAdapter(newBillingAdapter);
                // invoiceItemsList = invoiceItems;
                invoiceItemsList.clear();
                for(int i =0;i<invoiceItems.size();i++)
                    invoiceItemsList.add(invoiceItems.get(i));
                binding.billDets.setText("Bill details"+"("+invoiceItemsList.size()+")");
                billItemBinding.items.setText("Items"+"("+invoiceItemsList.size()+")");

                newBillingAdapter = new NewBillingAdapter(invoiceItemsList, BillingNewActivity.this, isGSTAvailable,BillingNewActivity.this);
                binding.invoiceItems.setAdapter(newBillingAdapter);

            }
        });
    }

    private void getUSerData(){
        try {
            profile = new JSONObject(MyApplication.getUserDetails());
            if (profile.has("isGST") && profile.getString("isGST").equals(1)  && !isEdit) {
                gstNo = profile.getString("gstNo");
                //isGSTAvailable = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void getModelData() {
//        modelViewModel = ViewModelProviders.of(this).get(ModelViewModel.class);
//        modelViewModel.getModels().observe(this, new Observer<List<Model>>() {
//            @Override
//            public void onChanged(@Nullable List<Model> modelList) {
//                models = (ArrayList<Model>) modelList;
//                if (modelList == null) {
//                    models = new ArrayList<>();
//                }
//                modelAdapter = new ModelAdapter(BillingNewActivity.this, R.layout.spinner_textview_layout, models);
//                billItemBinding.itemNameET.setAdapter(modelAdapter);
//                Log.v(TAG, "models::" + models);
//            }
//        });
//        try {
//            profile = new JSONObject(MyApplication.getUserDetails());
//            if (profile.has("gstNo") && profile.getString("gstNo") != null && !profile.getString("gstNo").isEmpty() && !isEdit) {
//                isGSTAvailable = true;
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

//    public void addItem(final String modelName, final float price, final float gst, final float quantity, boolean isNew, String imei, String hsnNo, final int measurementUnitId) {
//
//        NewInvoiceModels newInvoiceModel = isNew ? new NewInvoiceModels() : newInvoiceModels.get(editPosition);
//        setTotal(newInvoiceModel, false);
//        calculateAmountBeforeGST(newInvoiceModel, false);
//        newInvoiceModel.setLocalInvoiceId(invoiceIdIfEdit);
//        newInvoiceModel.setName(modelName);
//        newInvoiceModel.setIs_active(true);
//        newInvoiceModel.setPrice(price);
//        newInvoiceModel.setGst(gst);
//        newInvoiceModel.setGstAmount(((price * 100) / (100 + gst)) * quantity);
//        newInvoiceModel.setQuantity(quantity);
//        newInvoiceModel.setSerial_no(hsnNo);
//        newInvoiceModel.setTotalAmount(quantity * price);
//        newInvoiceModel.setImei(imei);
//        newInvoiceModel.setGstType(gstTypeList.get(binding.gstType.getSelectedItemPosition()));
//        Log.v("UNIT", measurementUnitId + " ");
//        newInvoiceModel.setMeasurementId(measurementUnitId);
//        if (isNew)
//            newInvoiceModels.add(newInvoiceModel);
//        setTotal(newInvoiceModel, true);
//        calculateAmountBeforeGST(newInvoiceModel, true);
//        newBillingAdapter.notifyDataSetChanged();
//
//    }



    @Override
    public void itemClick(final int position, boolean isEdit) {
        if (isEdit) {


            //additemTv.setText("Update Item");
            editPosition = position;
            customDialogClass =    new BottomSheetClass(this, invoiceItemsList.get(position),measurementUnitTypeList);
            customDialogClass.show();
        } else {

            DialogUtils.showAlertDialog(this, "Yes", "No", "Are you sure you want to delete?", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    setTotal(invoiceItemsList.get(position), false);
                    calculateAmountBeforeGST(invoiceItemsList.get(position), false);
                    invoiceItemViewModel.delete(invoiceItemsList.get(position));
                    newBillingAdapter.notifyDataSetChanged();
                    calculateDiscount();
                }
                @Override
                public void negativeButtonClick() {

                }
            });
        }
    }



    public class
    BottomSheetClass extends BottomSheetDialog implements
            android.view.View.OnClickListener {
        public Activity c;
        public Dialog d;
        public Button yes, no;
        private TableRow deleteRow;
        private EditText priceEt, quantityEt, imeiNo, hsnNo;
        private AutoCompleteTextView modelName;
        private TextView gstLabelTV,priceLblTVBs,hsnTv;
        private Spinner gstPercentage, measurementUnitSpinner;
        private TextInputLayout priceEdtInputLayout;
        private LinearLayout hsnLay;
        private InvoiceItems newInvoiceModel;
        private List<String> measureUnitTypeList;
        private int pos;
        public BottomSheetClass(Activity a, InvoiceItems newInvoiceModel,List<String> measureUnitTypeList) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
            //this.pos = pos;
            this.newInvoiceModel = newInvoiceModel;
            this.measureUnitTypeList = measureUnitTypeList;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.add_model);
            LinearLayout ll = findViewById(R.id.addPriceLayout);
            LinearLayout hsnLay = findViewById(R.id.hsnLayout);

            yes = (Button) findViewById(R.id.add);
            no = (Button) findViewById(R.id.cancel);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);
            priceLblTVBs = findViewById(R.id.priceLblTVBs);
            deleteRow = findViewById(R.id.deleteLayout);
            modelName = findViewById(R.id.modelName);
            priceEt = findViewById(R.id.priceEdt);
            quantityEt = findViewById(R.id.quantity);
            gstPercentage = findViewById(R.id.gstPercentage);
            imeiNo = findViewById(R.id.imeiNo);
            hsnNo = findViewById(R.id.HsnNoET);
            gstLabelTV = findViewById(R.id.gstLabelTV);
            measurementUnitSpinner = findViewById(R.id.unit);
            priceEdtInputLayout = findViewById(R.id.priceEdtInputLayout);
            additemTv = findViewById(R.id.additemTv);
            if(isGSTAvailable)
            {
                priceLblTVBs.setText(R.string.enter_price_including_gst_bs);

            }
            additemTv.setText("Update Item");
            modelName.setAdapter(modelAdapter);


            Spinner spinner = measurementUnitSpinner;
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(BillingNewActivity.this,
                    android.R.layout.simple_spinner_item, this.measureUnitTypeList);
            spinner.setAdapter(dataAdapter);


            if (isGSTAvailable) {
                //billItemBinding.itemPriceET.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                assert hsnLay != null;
                hsnLay.setVisibility(View.VISIBLE);
                priceEdtInputLayout.setHint(getString(R.string.enter_price_including_gst));
            } else {

                LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                buttonLayoutParams.setMargins(15, 0, 10, 0);

                ll.setLayoutParams(buttonLayoutParams);
                priceLblTVBs.setLayoutParams(buttonLayoutParams);
                priceEt.setLayoutParams(buttonLayoutParams);
                assert hsnLay != null;
                hsnLay.setVisibility(View.GONE);
                priceEdtInputLayout.setHint(getString(R.string.enter_price));
            }


            if (isGSTAvailable) {

                this.gstPercentage.setVisibility(View.VISIBLE);
                this.gstLabelTV.setVisibility(View.VISIBLE);
            } else {
                this.gstPercentage.setVisibility(View.GONE);
                this.gstLabelTV.setVisibility(View.GONE);
            }
            if (this.newInvoiceModel != null) {
                modelName.setText(this.newInvoiceModel.getName());
                priceEt.setText(this.newInvoiceModel.getPrice() + "");
                quantityEt.setText(this.newInvoiceModel.getQuantity() + "");
                hsnNo.setText(newInvoiceModel.getSerial_no());
                imeiNo.setText(newInvoiceModel.getImei());
                List<String> gstPList = Arrays.asList(getResources().getStringArray(R.array.gstPercentage));
                measurementUnitSpinner.setSelection(this.newInvoiceModel.getMeasurementId());
                gstPercentage.setSelection(gstPList.indexOf((int) this.newInvoiceModel.getGst() + ""));
            }

            modelNameAutoComplete();
            modelName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        HashMap<String, String> req = new HashMap<>();
                        req.put("userid", profile.getString("userid"));
                        req.put("name", itemAdapter.getItem(position));
                        fetchMeasurementIdApiCall(req, measurementUnitSpinner);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            deleteRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogUtils.showAlertDialog(BillingNewActivity.this, "Yes", "No", "Are you sure you want to delete?", new DialogUtils.DialogClickListener() {
                        @Override
                        public void positiveButtonClick() {
                            setTotal(newInvoiceModel, false);
                            calculateAmountBeforeGST(newInvoiceModel, false);
                            invoiceItemViewModel.delete(newInvoiceModel);
                            newBillingAdapter.notifyDataSetChanged();
                            calculateDiscount();
                            count = count-1;
                            if(count >= 0)
                            {
                                binding.billDets.setText("Bills Details"+"("+count+")");
                                billItemBinding.items.setText("Items"+"("+count+")");
                            }
                            else
                            {
                                count =0;
                                binding.billDets.setText("Bills Details"+"("+count+")");
                                billItemBinding.items.setText("Items"+"("+count+")");
                            }
                            dismiss();
                        }
                        @Override
                        public void negativeButtonClick() {

                        }
                    });

                }
            });

        }
//        @Override
//        public boolean onKeyDown(int keyCode, KeyEvent event)  {
//            if (android.os.Build.VERSION.SDK_INT < 5
//                    && keyCode == KeyEvent.KEYCODE_BACK
//                    && event.getRepeatCount() == 0) {
//                DialogUtils.showAlertDialog(BillingNewActivity.this, "Yes", "No", "Are you sure you want to cancel the bill?", new DialogUtils.DialogClickListener() {
//                    @Override
//                    public void positiveButtonClick() {
//                        finish();
//                    }
//
//                    @Override
//                    public void negativeButtonClick() {
//
//                    }
//                });
//
//
//                onBackPressed();
//            }
//
//            return super.onKeyDown(keyCode, event);
//        }
//
//        @Override
//        public void onBackPressed() {
//            DialogUtils.showAlertDialog(BillingNewActivity.this, "Yes", "No", "Are you sure you want to cancel the bill?", new DialogUtils.DialogClickListener() {
//                @Override
//                public void positiveButtonClick() {
//                    finish();
//                }
//
//                @Override
//                public void negativeButtonClick() {
//
//                }
//            });
//
//        }


        private void modelNameAutoComplete() {
            try {

                Map<String, String> req = new HashMap<>();
                req.put("userid", profile.getString("userid"));
                final Call<Object>[] call = new Call[]{null};
                modelName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        modelName.showDropDown();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        try {
                            if (s.length() > 2) {
                                req.put("name", s.toString());
                                searchItemApiCall(call, (HashMap<String, String>) req, modelName);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                });

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add:
                    if (verifyData()) {
                        addItemToDatabase(modelName.getText().toString(),
                                Float.parseFloat(priceEt.getText().toString())
                                ,Float.parseFloat(gstPercentage.getSelectedItemPosition() > 0 ? gstPercentage.getSelectedItem().toString() : "0"),
                                Float.parseFloat(quantityEt.getText().toString()),
                                this.newInvoiceModel == null ? true : false,
                                imeiNo.getText().toString(),
                                hsnNo.getText().toString(),
                                measurementUnitSpinner.getSelectedItemPosition(),-1);
                        Toast.makeText(c, "Item successfully Added!", Toast.LENGTH_SHORT).show();

                        dismiss();
                    }
                    break;
                case R.id.cancel:
                    dismiss();
                    count = count - 1;
                    break;
                default:
                    break;
            }
        }

        private boolean verifyData() {
            if (modelName.getText().toString().isEmpty()) {
                DialogUtils.showToast(c, "Please enter item name");
                return false;
            }
            else if(priceEt.getText().toString().equals("."))
            {
                DialogUtils.showToast(c, "Please enter Valid amount");
                return false;
            } else if((quantityEt.getText().toString().equals(".")))
            {
                DialogUtils.showToast(c, "Please enter Valid Quantity");
                return false;
            }
            else if (priceEt.getText().toString().isEmpty() || Float.parseFloat(priceEt.getText().toString()) == 0) {
                DialogUtils.showToast(c, "Please enter price");
                return false;
            } else if (quantityEt.getText().toString().isEmpty() || Float.parseFloat(quantityEt.getText().toString()) == 0) {
                DialogUtils.showToast(c, "Please enter quantity");
                return false;
            }
            //else if (isGSTAvailable && gstPercentage.getSelectedItemPosition() == 0) {
//                DialogUtils.showToast(c, "Please select GST %");
//                return false;
//            }
            return true;
        }
    }


    public void showDatePickerDialog(View v) {
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
                    invoiceDate = myFormat.parse(dateToUse);
                    DateFormat formatter =
                            new SimpleDateFormat("yyyy-MM-dd");
                    invoiceDateStr = formatter.format(invoiceDate);
                    binding.billDate.setText(invoiceDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.v(TAG, "selctedFromDate::" + invoiceDate);

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void showHideAdditionalDetails(View view) {
        if (binding.AddressLayout.getVisibility() == View.VISIBLE) {
            binding.GSTLayout.setVisibility(View.GONE);
            binding.AddressLayout.setVisibility(View.GONE);
//         gstTypeLayout.setVisibility(View.GONE);
            binding.additionalDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_add_24, 0);
        } else {
            binding.GSTLayout.setVisibility(View.VISIBLE);
            binding.AddressLayout.setVisibility(View.VISIBLE);
            binding.additionalDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_remove_24, 0);
        }
    }
    // toggle function to show and hide discount
//    public void showHideDiscountBilling(View view) {
//        if (binding.discountBillingLayout.getVisibility() == View.VISIBLE) {
//            binding.discountBillingLayout.setVisibility(View.GONE);
//            binding.discountBilling.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_circle, 0);
//        } else {
//            binding.discountBillingLayout.setVisibility(View.VISIBLE);
//            binding.discountBilling.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_circle, 0);
//
//        }
//    }


    private void setTotal(InvoiceItems newInvoiceModel, boolean add) {
        if (add)
            total = total + newInvoiceModel.getTotalAmount();
        else
            total = total - newInvoiceModel.getTotalAmount();
        binding.tvTotal.setText("₹"+Util.formatDecimalValue(total));


    }

    private void calculateDiscount() {
        try {
            if (binding.edtDiscountPercent.getText().toString().length() > 0) {
                String substr = binding.edtDiscountPercent.getText().toString();
                if (substr.endsWith("%")) {
                    substr = substr.substring(0, substr.length() - 1);
                }
                discountPercent = Float.valueOf(substr);
            } else if (binding.edtDiscountAmt.getText().toString().length() > 0) {
                discountAmt = Float.valueOf(binding.edtDiscountAmt.getText().toString());
            }

            if (discountAmt > 0 && discountPercent == 0) {
                discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, totalBeforeGST);
            } else if (discountPercent > 0 && discountAmt == 0) {
                discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, totalBeforeGST);
            } else {
                discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, totalBeforeGST);
                discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, totalBeforeGST);
            }

            if (discountAmt > 0) {
                binding.edtDiscountAmt.setText(String.valueOf(discountAmt));
            }
            if (discountPercent > 0) {
                binding.edtDiscountPercent.setText(discountPercent + "%");
            }

            setTotalAfterDiscount();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setTotalAfterDiscount() {
        try {
            if (discountPercent > 100.00) {
                setEditTextError(binding.edtDiscountPercent, "Discount should be less than or equal to 100%");
            } else if (discountAmt > total) {
                setEditTextError(binding.edtDiscountAmt, "Discount value should be less than or equal to total");
            } else {
                float totalAfterDiscount = total - discountAmt;
                binding.tvTotal.setText("₹"+Util.formatDecimalValue(totalAfterDiscount));
                binding.tvTotalFinal.setText("₹"+Util.formatDecimalValue(totalAfterDiscount));
                setEditTextError(binding.edtDiscountAmt, "");
                setEditTextError(binding.edtDiscountPercent, "");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void calculateAmountBeforeGST(InvoiceItems newInvoiceModel, boolean add) {
        //totalBeforeGST = 0;
        if (add)
            totalBeforeGST = totalBeforeGST + newInvoiceModel.getGstAmount();
        else
            totalBeforeGST = totalBeforeGST - newInvoiceModel.getGstAmount();

        binding.tvAmountBeforeTax.setText("₹"+Util.formatDecimalValue(totalBeforeGST));
        binding.tvAmountGST.setText("₹"+Util.formatDecimalValue(total - totalBeforeGST));
        shortBillGstAmt = total - totalBeforeGST;
    }

    public void gotoPDFActivity(View v) {
        cleverTapAPI.createNotificationChannel(getApplicationContext(),"test","test","Your Channel Description", NotificationManager.IMPORTANCE_MAX,true);
        cleverTapAPI.pushEvent("Clicked Make Bill ");
        Util.dailyLogout(BillingNewActivity.this);
        startPDFActivity();
    }

    private void startPDFActivity() {
        String eventName = isEdit ? "Make Bill Edit" : "Make Bill";
        if (verify()) {

            Util.postEvents(eventName, eventName, this.getApplicationContext());
            JSONObject requestObj = new JSONObject();
            try {
                requestObj.put("customerName", binding.edtName.getText().toString());
                requestObj.put("customerMobileNo", binding.edtMobNo.getText().toString());
                requestObj.put("customerAddress", binding.edtAddress.getText().toString());
                requestObj.put("GSTNo", binding.edtGST.getText().toString());
                requestObj.put("userid", profile.getString("userid"));
                requestObj.put("invoiceDate", invoiceDateStr);
                requestObj.put("totalAmountBeforeGST", totalBeforeGST);
                try {
                    requestObj.put("discount", Float.parseFloat(Util.formatDecimalValue(discountPercent)));
                }
                catch (Exception e)
                {
                    requestObj.put("discount", 0);
                }
               // requestObj.put("discount", Float.parseFloat(Util.formatDecimalValue(discountPercent)));
                requestObj.put("totalAfterDiscount", total-discountAmt);
                requestObj.put("totalAmount", total);

                if (isGSTAvailable) {
                    requestObj.put("gstBillNo", serialNumber);
                    requestObj.put("nonGstBillNo", 0);
                } else {
                    requestObj.put("gstBillNo", 0);
                    requestObj.put("nonGstBillNo", serialNumber);
                }
                if (isGSTAvailable)
                    requestObj.put("gstType", gstTypeList.get(binding.gstType.getSelectedItemPosition()));
                else
                    requestObj.put("gstType", "");
                String items = gson.toJson(invoiceItemsList);

                requestObj.put("items", new JSONArray(items));
                Log.d(TAG, "startPDFActivity: " + items);
                if (isEdit && invoiceItemsList.size() == 0) {
                    requestObj.put("is_active", false);
                }
//                if(isEdit && requestObj.getJSONArray("items").length()>0){
//                    int cnt=0;
//                    while(requestObj.getJSONArray("items").length()>cnt){
//                        if(requestObj.getJSONArray("items").getJSONObject(cnt).getInt("idInLocalDb") >0){
//                            requestObj.getJSONArray("items").remove(cnt);
//                        }else
//                            cnt++;
//                    }
//                }

                if(isEdit) {
                    new getInvoiceModelByIdAsyncTask(MyApplication.getDatabase().newInvoiceDao(), localInvoiceId, requestObj).execute();
                }else {
                    saveInvoiceToLocalDatabase(requestObj);
                }
                if (Util.isNetworkAvailable(this)) {
                    if(!isEdit)
                    {
                        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo info = cm.getActiveNetworkInfo();
                        networkSpeed = nt.isConnectionFast(info.getType(),info.getSubtype());
                        Log.v("speeed of net",networkSpeed);
                        if(networkType.equals("2G") || networkType.equals("?") )
                        {
                            Toast.makeText(this, "Network is too slow", Toast.LENGTH_SHORT).show();
//                           saveInvoiceToLocalDatabase(requestObj);
                            saveInvoiceOffline(requestObj);
                        }
                        else
                        {
                            sendInvoice(requestObj);
                        }

                    }

                }else {
                    saveInvoiceOffline(requestObj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void saveInvoiceOffline(JSONObject requestObj)

    {

        try {
            if(localInvoiceId!=0)
            {
                invoiceViewModel.updateInvoiceId(localInvoiceId,-1);
                DialogUtils.showToast(this, "Invoice saved in offline mode.");
                JSONObject data = new JSONObject();
                requestObj.put("id", -1);

                data.put("invoice", requestObj);

                data.put("items", requestObj.getJSONArray("items"));

                Intent intent = new Intent(BillingNewActivity.this, PDFActivity.class);

                //intent.putExtra("invoice", requestObj.toString());

                //intent.putExtra("invoiceServer", data.toString());

                intent.putExtra("localInvId",localInvoiceId);

                intent.putExtra("id",-1);

                intent.putExtra("idForItem",localInvoiceId);
                intent.putExtra("isGstAvailable",isGSTAvailable);



                if (isGSTAvailable) {

                    intent.putExtra("gstBillNo", serialNumber);

                    intent.putExtra("nonGstBillNo", 0);

                } else {

                    intent.putExtra("gstBillNo", 0);

                    intent.putExtra("nonGstBillNo", serialNumber);

                }


                startActivity(intent);

                if (!isEdit && isGSTAvailable)

                    MyApplication.setInvoiceNumber(serialNumber + 1);

                else if (!isEdit)

                    MyApplication.setInvoiceNumberForNonGst(serialNumber + 1);

                BillingNewActivity.this.finish();
            }
            else
            {

            }

        }
        catch (JSONException e) {

            e.printStackTrace();

        }

    }


    private boolean verify() {
        if (invoiceItemsList.size() == 0 ) {
            DialogUtils.showToast(this, isEdit? "Please add atleast one product" : "Please add product for billing");
            return false;
        } else if (!binding.edtMobNo.getText().toString().isEmpty() && binding.edtMobNo.getText().toString().length() < 10) {
            DialogUtils.showToast(this, "Please enter valid mobile number");
            return false;
        } else if (discountAmt > total) {
            DialogUtils.showToast(this, "Please enter valid discount value");
            return false;
        }

        return true;
    }

    private boolean addItemVerify() {
        if (billItemBinding.itemNameET.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Please enter item name");
            return false;
        }
        else if((billItemBinding.itemQtyET.getText().toString().equals(".")))
        {
            DialogUtils.showToast(this, "Please enter Valid quantity");
            return false;
        }
        else if(billItemBinding.itemPriceET.getText().toString().equals("."))
        {
            DialogUtils.showToast(this, "Please enter Valid price");
            return false;
        }
        else if (billItemBinding.itemPriceET.getText().toString().isEmpty() || Float.parseFloat(billItemBinding.itemPriceET.getText().toString()) == 0 ) {
            DialogUtils.showToast(this, "Please enter price");
            return false;
        }
        else if (billItemBinding.itemQtyET.getText().toString().isEmpty() || Float.parseFloat(billItemBinding.itemQtyET.getText().toString()) ==0 ) {
            Log.v("quantity verify",(billItemBinding.itemQtyET.getText().toString()));
            DialogUtils.showToast(this, "Please enter quantity");
            return false;
//        } else if (isGSTAvailable && billItemBinding.gstPercentage.getSelectedItemPosition() == 0) {
//            DialogUtils.showToast(this, "Please select GST %");
//            return false;
        }

        return true;

    }

    void saveInvoiceToLocalDatabase(JSONObject invoice){
        try{
            InvoiceModelV2 curInvoice = new InvoiceModelV2(
                    localInvoiceId,
                    localInvoiceId,
                    invoice.has("customerName") ? invoice.getString("customerName") : "",
                    invoice.has("customerMobileNo") ? invoice.getString("customerMobileNo") : "",
                    invoice.has("customerAddress") ? invoice.getString("customerAddress") : "",
                    invoice.has("GSTNo") ? invoice.getString("GSTNo") : "",
                    invoice.has("totalAmount") ? (float) invoice.getDouble("totalAmount") : 0,
                    invoice.has("userid") ? invoice.getInt("userid") : 0,
                    invoice.has("invoiceDate") ? invoice.getString("invoiceDate") : "",
                    invoice.has("totalAmountBeforeGST") ? invoice.getInt("totalAmountBeforeGST") : 0,
                    invoice.has("gstBillNo") ? invoice.getInt("gstBillNo") : 0,
                    invoice.has("nonGstBillNo") ? invoice.getInt("nonGstBillNo") : 0,
                    invoice.has("gstType") ? invoice.getString("gstType") : "",
                    invoice.has("updatedAt") ? invoice.getString("updatedAt") : "",
                    invoice.has("createdAt") ? invoice.getString("createdAt") : "",
                    0,
                    invoice.has("pdfPath") ? invoice.getString("pdfPath") : "",
                    invoice.has("discount") ? (float) invoice.getDouble("discount") : 0,
                    invoice.has("totalAfterDiscount") ? (float) invoice.getDouble("totalAfterDiscount") : 0
            );

            invoiceViewModel = ViewModelProviders.of(this).get(InvoiceViewModel.class);
            invoiceViewModel.insert(curInvoice);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    // on edit invoice, update the existing entry of the invoice with new values
    public void setCurrInvoiceToUpdate(InvoiceModelV2 invoiceModelV2, JSONObject invoice){
        currInvoiceToUpdate = invoiceModelV2;
        try {
            // In case entry is not present in local db create new entry, else update existing one
            if(currInvoiceToUpdate==null) {
                saveInvoiceToLocalDatabase(invoice);
                idInLocalDb = -1;
            } else {
                currInvoiceToUpdate.setCustomerName(invoice.has("customerName")  ? invoice.getString("customerName") : "");
                currInvoiceToUpdate.setCustomerMobileNo(invoice.has("customerMobileNo") ? invoice.getString("customerMobileNo") : "");
                currInvoiceToUpdate.setCustomerAddress(invoice.has("customerAddress") ? invoice.getString("customerAddress") : "");
                currInvoiceToUpdate.setGSTNo(invoice.has("GSTNo") ? invoice.getString("GSTNo") : "");
                currInvoiceToUpdate.setTotalAmount(invoice.has("totalAmount") ? (float) invoice.getDouble("totalAmount") : 0);
                currInvoiceToUpdate.setUserid(invoice.has("userid") ? invoice.getInt("userid") : 0);
                currInvoiceToUpdate.setInvoiceDate(invoice.has("invoiceDate") ? invoice.getString("invoiceDate") : "");
                currInvoiceToUpdate.setTotalAmountBeforeGST(invoice.has("totalAmountBeforeGST") ? invoice.getInt("totalAmountBeforeGST") : 0);
                currInvoiceToUpdate.setGstBillNo(invoice.has("gstBillNo") ? invoice.getInt("gstBillNo") : 0);
                currInvoiceToUpdate.setNonGstBillNo(invoice.has("nonGstBillNo") ? invoice.getInt("nonGstBillNo") : 0);
                currInvoiceToUpdate.setGstType(invoice.has("gstType") ? invoice.getString("gstType") : "");
                currInvoiceToUpdate.setUpdatedAt(invoice.has("updatedAt") ? invoice.getString("updatedAt") : "");
                currInvoiceToUpdate.setCreatedAt(invoice.has("createdAt") ? invoice.getString("createdAt") : "");
                currInvoiceToUpdate.setIsSync(0);
                currInvoiceToUpdate.setPdfPath(invoice.has("pdfPath") ? invoice.getString("pdfPath") : "");
                currInvoiceToUpdate.setDiscount(invoice.has("discount") ? (float) invoice.getDouble("discount") : 0);
                currInvoiceToUpdate.setTotalAfterDiscount(invoice.has("totalAfterDiscount") ? (float) invoice.getDouble("totalAfterDiscount") : 0);

                invoiceViewModel = ViewModelProviders.of(this).get(InvoiceViewModel.class);
                invoiceViewModel.update(currInvoiceToUpdate);
                idInLocalDb = currInvoiceToUpdate.getId();
            }
            sendInvoice(invoice);
        }
        catch(JSONException e){
            e.printStackTrace();
        }

    }
    // to fetch Invoice present In local database by Id
    private class getInvoiceModelByIdAsyncTask extends AsyncTask<Void,Void,InvoiceModelV2> {
        private NewInvoiceDao newInvoiceDao;
        private long localInvoiceId;
        private InvoiceModelV2 currentInvoice;
        private JSONObject invoice;
        public getInvoiceModelByIdAsyncTask(NewInvoiceDao newInvoiceDao, long localInvoiceId, JSONObject invoice) {
            this.newInvoiceDao = newInvoiceDao;
            this.localInvoiceId = localInvoiceId;
            this.invoice = invoice;
        }

        @Override
        protected InvoiceModelV2 doInBackground(Void... voids) {
            currentInvoice = newInvoiceDao.getCurrentInvoiceByInvoiceId(localInvoiceId);
            return currentInvoice;
        }

        protected void onPostExecute(InvoiceModelV2 invoiceModelV2) {
            super.onPostExecute(invoiceModelV2);
            setCurrInvoiceToUpdate(invoiceModelV2,invoice);
        }

    }

    private void sendInvoice(final JSONObject invoice) {
        try {
            DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient(this).create(ApiInterface.class);
            Map<String, String>
                    headerMap = new HashMap<>();
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(invoice.toString());
            headerMap.put("Content-Type", "application/json");
            HashMap<String, Object> body = new HashMap();
            body.put("content", jsonObject);
            Gson gson = new Gson();
            String json = gson.toJson(invoice);
            JSONObject invoiceObject = new JSONObject(json);
            Call<Object> call = null;
            if (!isEdit) {
                apiName = "/v1/invoice/";
                call = apiService.invoice(jsonObject);
            } else {
                try {
                    apiName = "/v1/invoice/";
                    call = apiService.updateInvoice(headerMap, this.invoice.getLong("id"), jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
//        Call<Object> call = apiService.invoice(jsonObject);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    JSONObject body = null;
                    try {
                        body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP", body.toString());
                        if (body.getBoolean("status")) {
                            JSONObject object = new JSONObject();
                            JSONObject customerObject = new JSONObject();
                            if (isEdit) {
                                object.put("invoice", body.getJSONObject("data"));
                                object.put("items", body.getJSONObject("data").getJSONArray("masterItems"));

//                                object.put("invoice", body.getJSONObject("data").getJSONObject("invoice"));
//                                object.put("items", body.getJSONObject("data").getJSONObject("invoice").getJSONArray("masterItems"));
                                customerObject =  body.getJSONObject("data").getJSONObject("customer");
                                object.getJSONObject("invoice").put("customerName",customerObject.getString("name"));
                                object.getJSONObject("invoice").put("customerMobileNo",customerObject.getString("mobileNo"));
                                object.getJSONObject("invoice").put("customerAddress",
                                        customerObject.has("address") ? customerObject.getString("address") : "");
                                if(idInLocalDb >0)
                                    invoiceViewModel.updateIsSync(idInLocalDb);
                                else
                                    invoiceViewModel.updateIsSync(localInvoiceId);

                            }
                            else {
                                invoiceViewModel.updateIsSync(localInvoiceId);
                                object.put("invoice", body.getJSONObject("data"));
                                // object.put("items", body.getJSONObject("data").getJSONArray("masterItems"));
                            }

                            Log.v("object 1840",object.toString());
                            invoiceViewModel.updateInvoiceId(localInvoiceId,isEdit ? object.getJSONObject("invoice").getInt("id") : body.getJSONObject("data").getJSONObject("invoice").getInt("id"));


//                            //invoiceViewModel.updateInvoiceId(localInvoiceId,isEdit ? object.getJSONObject("invoice").getInt("id") : object.getJSONObject("invoice").getJSONObject("data").getInt("id"));
//                          //invoiceViewModel.updateInvoiceId(localInvoiceId,isEdit ? object.getJSONObject("invoice").getInt("id") : object.getJSONObject("data").getJSONObject("invoice").getInt("id"));
//
//                             invoiceViewModel.updateInvoiceId(localInvoiceId,isEdit ? object.getInt("id") : body.getJSONObject("data").getInt("id"));
//                            //int edit_id=object.getJSONObject("invoice").getInt("id");
//                            int make_id=body.getJSONObject("data").getJSONObject("invoice").getInt("id");
//                           // Log.v("edit_id",String.valueOf(edit_id));
//                            Log.v("make_id",String.valueOf(make_id));


                            Intent intent = new Intent(BillingNewActivity.this, PDFActivity.class);
                            intent.putExtra("isGstAvailable",isGSTAvailable);
                            intent.putExtra("invoice", invoice.toString());
                            intent.putExtra("gstBillNo",isEdit ? object.getJSONObject("invoice").getInt("gstBillNo") : body.getJSONObject("data").getJSONObject("invoice").getInt("gstBillNo"));
                            intent.putExtra("nonGstBillNo",isEdit ? object.getJSONObject("invoice").getInt("nonGstBillNo") : body.getJSONObject("data").getJSONObject("invoice").getInt("nonGstBillNo"));
                            intent.putExtra("id",isEdit ? object.getJSONObject("invoice").getInt("id") : body.getJSONObject("data").getJSONObject("invoice").getInt("id"));
                            intent.putExtra("idForItem", isEdit ? (long) object.getJSONObject("invoice").getInt("id"):localInvoiceId);
                            intent.putExtra("customerName", isEdit & customerObject.has("name") ? customerObject.getString("name") : "");
                            intent.putExtra("customerMobileNo", isEdit & customerObject.has("mobileNo")? customerObject.getString("mobileNo"): "");
                            intent.putExtra("customerAddress", isEdit & customerObject.has("address")? customerObject.getString("address"): "");
                            intent.putExtra("itemsSize", String.valueOf(invoiceItemsList.size()));
                            for(int i = 0; i < invoiceItemsList.size();i++){
                                quantityCount += invoiceItemsList.get(i).getQuantity();
                            }
                            intent.putExtra("quantityCount", String.valueOf(quantityCount));
                            intent.putExtra("shortBillGstAmt",String.valueOf(Util.formatDecimalValue(shortBillGstAmt)));
                            if(isEdit && idInLocalDb >0) {
                                intent.putExtra("localInvId", idInLocalDb);
                            }
                            else
                                intent.putExtra("localInvId",localInvoiceId);

                           /* Intent intent = new Intent(BillingNewActivity.this, PDFActivity.class);
                            intent.putExtra("invoice", invoice.toString());
                           // intent.putExtra("shortHtml", body.getJSONObject("data").getString("shortHtml1"));
                           // intent.putExtra("longHtml", body.getJSONObject("data").getString("longHtml1") );
                           // intent.putExtra("pdflink", body.getJSONObject("data").getJSONObject("invoice").getString("pdfLink") );
                            intent.putExtra("invoiceId", body.getJSONObject("data").getJSONObject("invoice").getInt("id") );
                            if(isEdit && idInLocalDb >0) {
                                intent.putExtra("localInvId", idInLocalDb);
                            }
                            else
                                intent.putExtra("localInvId",localInvoiceId);*/

                            ///intent.putExtra("invoiceServer", isEdit ? object.toString() : body.getJSONObject("data").toString());
                            startActivity(intent);
                            if (!isEdit && isGSTAvailable)
                                MyApplication.setInvoiceNumber(serialNumber + 1);
                            else if (!isEdit)
                                MyApplication.setInvoiceNumberForNonGst(serialNumber + 1);
                            BillingNewActivity.this.finish();

                        } else {
                            Util.postEvents("Make Bill Fail", "Make Bill Fail:elseStatus", getApplicationContext());
                            if(body!=null)
                                Util.logErrorApi(apiName + "Make Bill Fail:elseStatus" + networkType, invoiceObject, "status :false", "Failed save invoice server", (JsonObject) jsonParser.parse(body.toString()),BillingNewActivity.this);                            DialogUtils.showToast(BillingNewActivity.this, "Failed save invoice server");
                        }
                    } catch (JSONException e) {
                        if(body!=null)
                            Util.logErrorApi(apiName + "Make Bill Fail:catch" + networkType, invoiceObject, Arrays.toString(e.getStackTrace()), "Make Bill Fail:catch", (JsonObject) jsonParser.parse(body.toString()), BillingNewActivity.this);                        Util.postEvents("Make Bill Fail", "Make Bill Fail:catch", getApplicationContext());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Util.postEvents("Make Bill Fail", "Make Bill Fail:onFailure", getApplicationContext());
                    Util.logErrorApi(apiName + "Make Bill Fail:onFailure" + networkSpeed, invoiceObject,Arrays.toString(t.getStackTrace()), t.toString() , null,BillingNewActivity.this);

                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(BillingNewActivity.this, "Failed save invoice server");
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public class BillNumberUpdateDialog extends Dialog implements
            android.view.View.OnClickListener {
        public Activity c;
        public Dialog d;
        public Button yes, no;
        private EditText invSerialNo;

        public BillNumberUpdateDialog(Activity a) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.serialno_update_dialog);
            invSerialNo = findViewById(R.id.invSerialNo);
            yes = (Button) findViewById(R.id.add);
            no = (Button) findViewById(R.id.cancel);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);
            invSerialNo.setText("" + serialNumber);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add:
                    if (invSerialNo.getText().toString().isEmpty()) {
                        DialogUtils.showToast(c, "Invalid bill no");
                    } else {
                        int no = 0;
                        if (isGSTAvailable)
                            no = MyApplication.getInVoiceNumber();
                        else
                            no = MyApplication.getInVoiceNumberForNonGst();
//                            if(Integer.parseInt(invSerialNo.getText().toString())<no){
//                                DialogUtils.showToast(c,"Bill number already exists");
//                            }else{
//                            }
                        serialNumber = Integer.parseInt(invSerialNo.getText().toString());
                        binding.billNo.setText(invSerialNo.getText().toString());
                    }
                    dismiss();
                    break;
                case R.id.cancel:
                    dismiss();
                    break;
                default:
                    break;
            }
        }

        private boolean verifyData() {

            return true;
        }
    }

    private void checkIsEdit() {
        if (getIntent().hasExtra("edit")) {
            try {
                //binding.addDisc.setText("Update discount");
                Button b = findViewById(R.id.addMoreItem);
                b.setVisibility(View.VISIBLE);
                binding.viewDetsLay.setVisibility(View.VISIBLE);
                isEdit = true;
                invoice = new JSONObject(getIntent().getStringExtra("invoice"));
                Log.v("EditINV", invoice.toString());
                count = invoiceItemEditModel.size() - 1;
                Log.v("in voice lst",String.valueOf(invoiceItemEditModel));
                if(invoiceItemEditModel.size() == 0)
                {
                    count = 0;
                }
                else
                {
                    count = invoiceItemEditModel.size() + 1;
                }

                binding.billDets.setText("Bill details"+"("+count+")");
                billItemBinding.items.setText("Items"+"("+count+")");
                int gstBillNo = invoice.getInt("gstBillNo");
                if(gstBillNo>0)
                {
                    isGSTAvailable=true;
                    Log.v("gstBillNo in if", String.valueOf(gstBillNo));
                }
                else
                {
                    isGSTAvailable = false;
                    Log.v("gstBillNo", String.valueOf(gstBillNo));
                }

                //  isGSTAvailable = !invoice.getString("gstType").isEmpty();
                if (isGSTAvailable)
                {
                    String gg = invoice.getString("gstType");
                    if(gg.equals( gstTypeList.get(0)))
                        binding.gstType.setSelection(0);
                    else
                        binding.gstType.setSelection(1);
                    serialNumber = invoice.getInt("gstBillNo");
                    binding.billNo.setText(String.valueOf(serialNumber));
                    binding.billNo.setEnabled(false);
                    Log.v("serialGst", String.valueOf(invoice.getInt("gstBillNo")));
                }
                else
                {
                    serialNumber = invoice.getInt("nonGstBillNo");
                    binding.billNo.setText(String.valueOf(serialNumber));
                    binding.billNo.setEnabled(false);
                    Log.v("serialNonGst", String.valueOf(invoice.getInt("nonGstBillNo")));
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void discVisible(View v)
    {

        TextView addDisc = findViewById(R.id.addDisc);
        View bildetsLine = findViewById(R.id.bildetsLine);
        LinearLayout disc = findViewById(R.id.discountLayout);
        if(ischeckDisc)
        {
            addDisc.setText("Cancel");
            bildetsLine.setVisibility(View.VISIBLE);
            disc.setVisibility(View.VISIBLE);
            ischeckDisc=false;
        } else
        {
            if(getIntent().hasExtra("edit")){
                addDisc.setText("Update Discount");
            } else {
                addDisc.setText("Add Discount");
            }

            binding.edtDiscountPercent.getText().clear();
            binding.edtDiscountAmt.getText().clear();
            bildetsLine.setVisibility(View.GONE);
            disc.setVisibility(View.GONE);
            discountAmt = 0;
            discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, total);

            ischeckDisc=true;
        }
    }


    private void loadDataForInvoice() {

        if (getIntent().hasExtra("edit")) {
            try {
                Log.d(TAG, "loadDataForInvoice: " + invoice);
                int gstBillNo = invoice.getInt("gstBillNo");
                if(gstBillNo>0)
                {
                    isGSTAvailable=true;
                    Log.v("gstBillNo in if load", String.valueOf(gstBillNo));
                }
                else
                {
                    isGSTAvailable = false;
                    Log.v("gstBillNo load", String.valueOf(gstBillNo));
                }
                binding.nextBtn.setText("Update Invoice");
                if(invoice.getInt("discount") > 0){
                    binding.addDisc.setText("Update Discount");
                }
                if (isGSTAvailable)
                    serialNumber = invoice.getInt("gstBillNo");
                else
                    serialNumber = invoice.getInt("nonGstBillNo");
                // binding.billNo.setText(String.valueOf(serialNumber));
//                bill_no.setEnabled(false);
                invoiceIdIfEdit = invoice.getInt("id");
                localInvoiceId = invoiceIdIfEdit;
                invoiceDateStr = Util.getFormatedDate(invoice.getString("invoiceDate"));
                binding.billDate.setText(invoiceDateStr);
//                bill_date.setEnabled(false);

                binding.edtName.setText(invoice.getJSONObject("customer").getString("name"));
                binding.edtAddress.setText(invoice.getJSONObject("customer").getString("address"));
                binding.tvTotal.setText("₹"+Util.formatDecimalValue((float) invoice.getDouble("totalAmount")));
                binding.edtGST.setText(invoice.getString("GSTNo"));
                binding.edtMobNo.setText(invoice.getJSONObject("customer").getString("mobileNo"));
                JSONArray masterItems = invoice.getJSONArray("masterItems");
                invoiceItemEditModel = gson.fromJson(masterItems.toString(), new TypeToken<List<InvoiceItems>>() {
                }.getType());

                invoiceItemViewModel.deleteAll(invoiceIdIfEdit);

                for(int i = 0;i<invoiceItemEditModel.size();i++){
                    InvoiceItems curItem = invoiceItemEditModel.get(i);
                    addItemToDatabase(curItem.getName(),
                            curItem.getPrice(),
                            curItem.getGst(),
                            curItem.getQuantity(),
                            true,
                            curItem.getImei(),
                            curItem.getSerial_no(),
                            curItem.getMeasurementId(),
                            (long)masterItems.getJSONObject(i).getInt("id")
                    );
                }
                total = (float) invoice.getDouble("totalAmount");
                totalBeforeGST = 0;
                if (isGSTAvailable) {
                    billItemBinding.priceLblTV.setText(R.string.price_including_gst);
                    billItemBinding.itemPriceET.setText(R.string.enter_price_including_gst);
                    for (int i = 0; i < invoiceItemEditModel.size(); i++) {
                        totalBeforeGST = totalBeforeGST + invoiceItemEditModel.get(i).getGstAmount();
                    }

                } else {
                    totalBeforeGST = total;
                }

                invoiceItemEditModel.clear();

                getInvoiceItemsFromDatabase();

                binding.tvAmountBeforeTax.setText(Util.formatDecimalValue(totalBeforeGST));
                binding.tvAmountGST.setText(Util.formatDecimalValue(total - totalBeforeGST));
                // for old invoices if discount key is not present
                float totalDiscount = 0, discountPercentLocal = 0;
                if (invoice.has("discount") && invoice.has("totalAfterDiscount")) {
//                    binding.discountBillingLayout.setVisibility(View.VISIBLE);
//                    binding.discountBilling.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_circle, 0);
                    discountPercent = (float) invoice.getDouble("discount");
                    discountPercentLocal = discountPercent;
                    totalDiscount = total - (float) invoice.getDouble("totalAfterDiscount");
                    discountAmt = totalDiscount;
                }
                binding.edtDiscountPercent.setText(discountPercentLocal + "%");
                binding.edtDiscountAmt.setText(String.valueOf(totalDiscount));
                binding.tvTotal.setText("₹"+Util.formatDecimalValue(total - totalDiscount));
                binding.tvTotalFinal.setText("₹"+Util.formatDecimalValue(invoice.getInt("totalAfterDiscount")));
                //binding.tvAmountGST.setText(Util.formatDecimalValue(invoice.getInt("gstAmount")));
                binding.cardItemList.setVisibility(View.VISIBLE);
                billItemBinding.layoutBillItemInitial.setVisibility(View.GONE);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    public void scanCode(View v) {
        Util.postEvents("Scan Button", "Scan Button", this.getApplicationContext());

        new IntentIntegrator(this).setOrientationLocked(false)
                .initiateScan(); // `this` is the current Activity
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.dailyLogout(BillingNewActivity.this);
        // isFirstReq == 0, to finish() BillingNewActivity only after evaluating the response in onActivityResult if storage permission isn't allowed i.e after executing StoragePermissionRequestActivity
        if (checkPermission() == false && isFirstReq==0) finish();
    }
    @Override
    public void onBackPressed() {
        DialogUtils.showAlertDialog(BillingNewActivity.this, "Yes", "No", "Are you sure you want to cancel the bill?", new DialogUtils.DialogClickListener() {
            @Override
            public void positiveButtonClick() {
                finish();
            }

            @Override
            public void negativeButtonClick() {

            }
        });
    }

}
