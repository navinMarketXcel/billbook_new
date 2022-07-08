package com.billbook.app.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.databinding.ActivityBillingNewBinding;
import com.billbook.app.databinding.LayoutItemBillBinding;
import com.billbook.app.viewmodel.InvoiceItemsViewModel;
import com.billbook.app.viewmodel.InvoiceViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.provider.Contacts;
import android.provider.ContactsContract;
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
import android.widget.ImageView;
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

import java.security.PrivateKey;
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
import java.util.Objects;
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
    private float total = 0, totalBeforeGST = 0, discountPercent = 0, discountAmt = 0;
    private String invoiceDateStr, gstBllNo, nonGstBillNo;
    private Date invoiceDate;
    private Gson gson = new Gson();
    private List<String> gstTypeList, measurementUnitTypeList;
    private JSONObject profile;
    private ArrayList<String> gstList, nonGstList;
    private boolean isGSTAvailable;
    private String gstNo;
    private int editPosition = -1;
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
    private EditText edtMobNo, billNo;
    private TextView additemTv,viewDets;
    private static final int Contact_code=123;
    private static final int Contact_Pick_code=111;
    private boolean ischeckDisc = true;
    private int count = 0;
    private LinearLayout itemslay;


    // idInLocalDb = column with name "id" in local db android

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillingNewBinding.inflate(getLayoutInflater());
        billItemBinding = binding.includedLayoutItemBill;
        invoiceItemViewModel = ViewModelProviders.of(this).get(InvoiceItemsViewModel.class);
        //imageButton=findViewById(R.id.button1);
        edtname= findViewById(R.id.edtName);
        edtMobNo= findViewById(R.id.edtMobNo);
        billNo = findViewById(R.id.billNo);
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
        isGSTAvailable = MyApplication.getIsGst();
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



    }
    public void onClickViewDets(View v)
    {
        viewDets = findViewById(R.id.viewDets);
        ScrollView sv = findViewById(R.id.mainSv);
        sv.scrollTo(0, sv.getMaxScrollAmount ());

    }
    public void setonClick(){
        binding.ivToolBarBack.setOnClickListener(v -> {
            finish();
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
    }


    //Functions For Checking Contact permissions
    public void clickButton(View view)
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
        startActivityForResult(intent,Contact_Pick_code);
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
        binding.tvAmountBeforeTax.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.tvAmountGST.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.amtBeforeTaxLabelTV.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.taxLabelTV.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.GSTTypeLblTV.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.gstTypeLayout.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        binding.gstTitle.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);

        billItemBinding.gstPercentageTV.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        billItemBinding.gstPerLayout.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
//        billItemBinding.hsnLayout.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);

        Spinner spinner = (Spinner) billItemBinding.unit;
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, measurementUnitTypeList);
        spinner.setAdapter(dataAdapter);

        billItemBinding.gstPercentage.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        if (isGSTAvailable) {
            billItemBinding.priceLblTV.setText(R.string.price_including_gst);
            billItemBinding.itemPriceET.setHint(R.string.enter_price_including_gst);
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
                            discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, total);
                            binding.edtDiscountAmt.setText(String.valueOf(discountAmt));
                            if (discountPercent > 100.00)
                                setEditTextError(binding.edtDiscountPercent, "Discount should be less than or equal to 100%");
                            else
                                setEditTextError(binding.edtDiscountPercent, "");
                        } else {
                            binding.edtDiscountAmt.getText().clear();
                            discountPercent = 0;
                            discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, total);
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
                            discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, total);
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
                startActivityForResult(intent, GRANT_STORAGE_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //For Picking A contact and loading it in the editText View
        try{
            if(resultCode==RESULT_OK)
            {
                if(requestCode==Contact_Pick_code)
                {
                    Cursor cursor1,cursor2;
                    Uri uri=data.getData();
                    cursor1=getContentResolver().query(uri,null,null,null,null);
                    if(cursor1.moveToFirst())
                    {

                        String contactName=cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String contactId = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));
                        String hasPhone = cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (Integer.parseInt(hasPhone) > 0) {
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                            String phoneNumber="";
                            while(phones.moveToNext())
                            { //iterate over all contact phone numbers
                                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                            if(phoneNumber.length()>10)
                            {
                                phoneNumber= phoneNumber.substring(phoneNumber.length()-10);
                            }
                            binding.edtMobNo.setText(phoneNumber);
                            phones.close();
                        }
                        binding.edtName.setText(contactName);
                    }
                    cursor1.close();
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
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
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if (billItemBinding.layoutBillItemInitial.getVisibility() == View.VISIBLE) {
                    billItemBinding.imeiNo.setText(billItemBinding.imeiNo.getText().toString().isEmpty() ? result.getContents() : billItemBinding.imeiNo.getText().toString() + "," + result.getContents());
//                } else if (BottomSheetClass.isShowing()) {
//                    BottomSheetClass..setText(billItemBinding.imeiNo.getText().toString().isEmpty() ? result.getContents() : billItemBinding.imeiNo.getText().toString() + "," + result.getContents());
                }
            }

        }
    }


    public void getMeasurementUnit(){
        try{
            List<String>onlineMeasurementUnit = MyApplication.getMeasurementUnits();
            if(onlineMeasurementUnit!=null)measurementUnitTypeList = onlineMeasurementUnit;
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
        viewNew.setText("Add New Item");
        TableRow table = customDialogClass.findViewById(R.id.deleteLayout);
        table.setVisibility(View.GONE);
        count = invoiceItemsList.size() + 1;
        binding.billDets.setText("Bill details"+"("+count+")");
        billItemBinding.items.setText("Items"+"("+count+")");



        //additemTv.setText("Add New Item");
    }

    public void addItem() {
        if (addItemVerify()) {
            Util.postEvents("Add Item", "Add Item", this.getApplicationContext());
            count = invoiceItemsList.size() + 1;
            binding.billDets.setText("Bill details"+"("+count+")");
            billItemBinding.items.setText("Items"+"("+count+")");
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
                    billItemBinding.imeiNo.getText().toString(),
                    billItemBinding.unit.getSelectedItemPosition(),-1);

            binding.cardItemList.setVisibility(View.VISIBLE);
            billItemBinding.layoutBillItemInitial.setVisibility(View.GONE);
//            binding.tvTotal.setText(billItemBinding.itemPriceET.getText().toString());
//
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
                newBillingAdapter = new NewBillingAdapter(invoiceItemsList, BillingNewActivity.this, isGSTAvailable,BillingNewActivity.this);
                binding.invoiceItems.setAdapter(newBillingAdapter);
                invoiceItemsList.clear();
                for(int i =0;i<invoiceItems.size();i++)invoiceItemsList.add(invoiceItems.get(i));
            }
        });
    }

    private void getUSerData(){
        try {
            profile = new JSONObject(MyApplication.getUserDetails());
            if (profile.has("gstNo") && profile.getString("gstNo") != null && !profile.getString("gstNo").isEmpty() && !isEdit) {
                gstNo = profile.getString("gstNo");
                isGSTAvailable = true;
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
            new BottomSheetClass(this, invoiceItemsList.get(position),measurementUnitTypeList).show();

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
        private TextView gstLabelTV;
        private Spinner gstPercentage, measurementUnitSpinner;
        private TextInputLayout priceEdtInputLayout;
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
            yes = (Button) findViewById(R.id.add);
            no = (Button) findViewById(R.id.cancel);
            yes.setOnClickListener(this);
            no.setOnClickListener(this);
            deleteRow = findViewById(R.id.deleteLayout);
            modelName = findViewById(R.id.modelName);
            priceEt = findViewById(R.id.priceEdt);
            quantityEt = findViewById(R.id.quantity);
            gstPercentage = findViewById(R.id.gstPercentage);
            imeiNo = findViewById(R.id.imeiNo);
            gstLabelTV = findViewById(R.id.gstLabelTV);
            hsnNo = findViewById(R.id.hsnNo);
            measurementUnitSpinner = findViewById(R.id.unit);
            priceEdtInputLayout = findViewById(R.id.priceEdtInputLayout);
            additemTv = findViewById(R.id.additemTv);
            additemTv.setText("Update Item");
            modelName.setAdapter(modelAdapter);



            Spinner spinner = measurementUnitSpinner;
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(BillingNewActivity.this,
                    android.R.layout.simple_spinner_item, this.measureUnitTypeList);
            spinner.setAdapter(dataAdapter);


            if (isGSTAvailable) {
                hsnNo.setVisibility(View.VISIBLE);
                priceEdtInputLayout.setHint(getString(R.string.enter_price_including_gst));
            } else {
                hsnNo.setVisibility(View.GONE);
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
                            binding.billDets.setText("Bills Details"+"("+count+")");
                            billItemBinding.items.setText("Items"+"("+count+")");
                            dismiss();


                        }
                        @Override
                        public void negativeButtonClick() {

                        }
                    });

                }

            });

        }

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
                                imeiNo.getText().toString(),
                                measurementUnitSpinner.getSelectedItemPosition(),-1);

                        dismiss();
                    }
                    break;
                case R.id.cancel:
                    dismiss();
                    count = count-1;
                    break;
                default:
                    break;
            }
        }

        private boolean verifyData() {
            if (modelName.getText().toString().isEmpty()) {
                DialogUtils.showToast(c, "Please enter item name");
                return false;
            } else if (priceEt.getText().toString().isEmpty() || Float.parseFloat(priceEt.getText().toString()) == 0) {
                DialogUtils.showToast(c, "Please enter price");
                return false;
            } else if (quantityEt.getText().toString().isEmpty() || Float.parseFloat(quantityEt.getText().toString()) == 0) {
                DialogUtils.showToast(c, "Please enter quantity");
                return false;
            } else if (isGSTAvailable && gstPercentage.getSelectedItemPosition() == 0) {
                DialogUtils.showToast(c, "Please select GST %");
                return false;
            }
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
            binding.additionalDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_circle, 0);
        } else {
            binding.GSTLayout.setVisibility(View.VISIBLE);
            binding.AddressLayout.setVisibility(View.VISIBLE);
            binding.additionalDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_circle, 0);
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
                discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, total);
            } else if (discountPercent > 0 && discountAmt == 0) {
                discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, total);
            } else {
                discountAmt = Util.calculateDiscountAmtFromPercent(discountPercent, total);
                discountPercent = Util.calculateDiscountPercentFromAmt(discountAmt, total);
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
                System.out.println("in set totalafterdisc"+totalAfterDiscount);
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
    }

    public void gotoPDFActivity(View v) {
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
                requestObj.put("discount", Float.parseFloat(Util.formatDecimalValue(discountPercent)));
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
                        sendInvoice(requestObj);
                } else {
                    //there is some use of makeing invoiceid = -1 in backend
                    invoiceViewModel.updateInvoiceId(localInvoiceId,-1);
                    DialogUtils.showToast(this, "Invoice saved in offline mode.");


                    JSONObject data = new JSONObject();
                    requestObj.put("id", -1);
                    data.put("invoice", requestObj);
                    data.put("items", requestObj.getJSONArray("items"));

                    Intent intent = new Intent(BillingNewActivity.this, RemotePDFActivity.class);
                    //intent.putExtra("invoice", requestObj.toString());
                    //intent.putExtra("invoiceServer", data.toString());
                    intent.putExtra("localInvId",localInvoiceId);
                    intent.putExtra("id",-1);
                    intent.putExtra("idForItem",localInvoiceId);


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
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        } else if (billItemBinding.itemPriceET.getText().toString().isEmpty() || Float.parseFloat(billItemBinding.itemPriceET.getText().toString()) == 0) {
            DialogUtils.showToast(this, "Please enter price");
            return false;
        } else if (billItemBinding.itemQtyET.getText().toString().isEmpty() || Float.parseFloat(billItemBinding.itemQtyET.getText().toString()) == 0) {
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
        Call<Object> call = null;
        if (!isEdit) {
            call = apiService.invoice(jsonObject);
        } else {
            try {
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
                            object.put("invoice", body.getJSONObject("data").getJSONObject("invoice"));
                            object.put("items", body.getJSONObject("data").getJSONObject("invoice").getJSONArray("masterItems"));
                            customerObject =  body.getJSONObject("data").getJSONObject("invoice").getJSONObject("customer");
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
                        }

                        invoiceViewModel.updateInvoiceId(localInvoiceId,isEdit ? object.getJSONObject("invoice").getInt("id") : body.getJSONObject("data").getJSONObject("invoice").getInt("id"));

                        Intent intent = new Intent(BillingNewActivity.this, RemotePDFActivity.class);
//                        intent.putExtra("invoice", invoice.toString());
                        intent.putExtra("shortHtml", body.getJSONObject("data").getString("shortHtml1"));
                        intent.putExtra("longHtml", body.getJSONObject("data").getString("longHtml1") );
                        intent.putExtra("pdflink", body.getJSONObject("data").getJSONObject("invoice").getString("pdfLink") );
                        intent.putExtra("invoiceId", body.getJSONObject("data").getJSONObject("invoice").getInt("id") );
                        if(isEdit && idInLocalDb >0) {
                            intent.putExtra("localInvId", idInLocalDb);
                        }
                        else
                            intent.putExtra("localInvId",localInvoiceId);

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
                            Util.logErrorApi("/v1/invoice", jsonObject, "/invoice => status :false", null, (JsonObject) jsonParser.parse(body.toString()),BillingNewActivity.this);
                        DialogUtils.showToast(BillingNewActivity.this, "Failed save invoice server");
                    }
                } catch (JSONException e) {
                    if(body!=null)
                        Util.logErrorApi("/v1/invoice", jsonObject, null, Arrays.toString(e.getStackTrace()), (JsonObject) jsonParser.parse(body.toString()), BillingNewActivity.this);
                    Util.postEvents("Make Bill Fail", "Make Bill Fail:catch", getApplicationContext());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Util.postEvents("Make Bill Fail", "Make Bill Fail:onFailure", getApplicationContext());
                Util.logErrorApi("/v1/invoice", jsonObject, Arrays.toString(t.getStackTrace()), null, null,BillingNewActivity.this);
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(BillingNewActivity.this, "Failed save invoice server");
            }
        });

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
                Button b = findViewById(R.id.addMoreItem);
                b.setVisibility(View.VISIBLE);
                binding.viewDetsLay.setVisibility(View.VISIBLE);
                isEdit = true;
                invoice = new JSONObject(getIntent().getStringExtra("invoice"));
                Log.v("EditINV", invoice.toString());
                isGSTAvailable = !invoice.getString("gstType").isEmpty();
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
        LinearLayout disc = findViewById(R.id.discountLayout);
                if(ischeckDisc)
                {
                    addDisc.setText("Cancel");
                    disc.setVisibility(View.VISIBLE);
                    ischeckDisc=false;
                }
                else
                {
                    addDisc.setText("Add Discount");
                    disc.setVisibility(View.GONE);
                    ischeckDisc=true;
                }
    }

    private void loadDataForInvoice() {

        if (getIntent().hasExtra("edit")) {
            try {
                Log.d(TAG, "loadDataForInvoice: " + invoice);
                binding.nextBtn.setText("Update Invoice");
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
                System.out.println("Total after dis"+invoice.getInt("totalAfterDiscount"));


                JSONArray masterItems = invoice.getJSONArray("masterItems");
                invoiceItemEditModel = gson.fromJson(masterItems.toString(), new TypeToken<List<InvoiceItems>>() {
                }.getType());

                invoiceItemViewModel.deleteAll(invoiceIdIfEdit);

                for(int i = 0;i<invoiceItemEditModel.size();i++){
                    InvoiceItems curItem = invoiceItemEditModel.get(i);
                    // addItemToDatabase(curItem);

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
        // isFirstReq == 0, to finish() BillingNewActivity only after evaluating the response in onActivityResult if storage permission isn't allowed i.e after executing StoragePermissionRequestActivity
        if (checkPermission() == false && isFirstReq==0) finish();
    }


}
