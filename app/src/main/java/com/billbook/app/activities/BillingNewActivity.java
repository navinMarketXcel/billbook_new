package com.billbook.app.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.billbook.app.database.models.NewInvoiceModels;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.viewmodel.ModelViewModel;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
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

public class BillingNewActivity extends AppCompatActivity implements NewBillingAdapter.onItemClick {
    private ModelViewModel modelViewModel;
    final String TAG = "BillingNewActivity";
    private ArrayList models=new ArrayList<Model>();
    private ArrayList<NewInvoiceModels> newInvoiceModels = new ArrayList<>();
    private ModelAdapter modelAdapter;
    private NewBillingAdapter newBillingAdapter;
    private RecyclerView invoiceItems;
    private float total,totalBeforeGST;
    private float discount;
    private String invoiceDateStr;
    private Date invoiceDate;
    private TextView bill_date,additionalDetails,tvTotal,tvAmountGST,tvAmountBeforeTax,
            GSTTypeLblTV,gstPercentageTV,amtBeforeTaxLabelTV,taxLabelTV,priceLblTV,bill_no,GSTError,gstTitle ;
    private LinearLayout addressLayout,GSTLayout,gstTypeLayout,hsnLayout;
    private TableRow gstPerLayout;
    private CardView layoutBillItem_initial,cardItemList;
    private EditText edtName,edtMobNo,edtAddress,edtGST,itemPriceET,itemQtyET,imeiNo,hsnNo;
    private AutoCompleteTextView itemNameET;
    private Gson gson = new Gson();
    private Spinner gstPercentage,gstType,measurementUnit;
    private List<String> gstTypeList, measurementUnitTypeList;
    private JSONObject profile;
    private boolean isGSTAvailable;
    private int editPosition=-1;
    private int invoiceIdIfEdit = -1;
    private int serialNumber=0;
    private int hasWriteStoragePermission;
    private boolean isEdit=false;
    private JSONObject invoice;
    private Button nextBtn;
    private CustomDialogClass customDialogClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_new);
        setTitle("Billing");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        checkIsEdit();
        getModelData();
        internalStoragePermission();
        initUI();
        loadDataForInvoice();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initUI(){
        bill_date = findViewById(R.id.bill_date);
        addressLayout = findViewById(R.id.AddressLayout);
        GSTLayout = findViewById(R.id.GSTLayout);
        additionalDetails = findViewById(R.id.additionalDetails);
        tvTotal = findViewById(R.id.tvTotal);
        tvAmountGST = findViewById(R.id.tvAmountGST);
        tvAmountBeforeTax = findViewById(R.id.tvAmountBeforeTax);
        invoiceItems = findViewById(R.id.invoiceItems);
        edtName = findViewById(R.id.edtName);
        GSTError = findViewById(R.id.GSTError);
        edtMobNo = findViewById(R.id.edtMobNo);
        edtAddress = findViewById(R.id.edtAddress);
        edtGST = findViewById(R.id.edtGST);
        gstPerLayout = findViewById(R.id.gstPerLayout);
        measurementUnit = findViewById(R.id.unit);
        nextBtn = findViewById(R.id.nextBtn);
        gstTitle= findViewById(R.id.gstTitle);
        DateFormat formatter =
                new SimpleDateFormat("dd MMM yyyy");
        invoiceDateStr = formatter.format(new Date());
        bill_date.setText("Bill Date: "+invoiceDateStr);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        invoiceItems.setLayoutManager(mLayoutManager);
        invoiceItems.setItemAnimator(new DefaultItemAnimator());
        newBillingAdapter = new NewBillingAdapter(newInvoiceModels,this,isGSTAvailable);
        invoiceItems.setAdapter(newBillingAdapter);
        gstTypeList=  Arrays.asList (getResources().getStringArray(R.array.gst_type));
        measurementUnitTypeList=  Arrays.asList (getResources().getStringArray(R.array.measurementUnit));
        gstTypeLayout = findViewById(R.id.gstTypeLayout);
        cardItemList= findViewById(R.id.cardItemList);
        layoutBillItem_initial=findViewById(R.id.layoutBillItem_initial);
        itemNameET= findViewById(R.id.itemNameET);
        itemPriceET= findViewById(R.id.itemPriceET);
        itemQtyET= findViewById(R.id.itemQtyET);
        gstPercentage = findViewById(R.id.gstPercentage);
        gstType = findViewById(R.id.gstType);
        gstPercentageTV= findViewById(R.id.gstPercentageTV);
        amtBeforeTaxLabelTV= findViewById(R.id.amtBeforeTaxLabelTV);
        taxLabelTV= findViewById(R.id.taxLabelTV);
        GSTTypeLblTV = findViewById(R.id.GSTTypeLblTV);
        imeiNo= findViewById(R.id.imeiNo);
        hsnNo= findViewById(R.id.hsnNo);
        priceLblTV= findViewById(R.id.priceLblTV);
        hsnLayout = findViewById(R.id.hsnLayout);
        bill_no= findViewById(R.id.bill_no);
//        itemNameET.set(R.color.color_black);
        if(isGSTAvailable)
            serialNumber = MyApplication.getInVoiceNumber();
        else
            serialNumber = MyApplication.getInVoiceNumberForNonGst();
        bill_no.setText("Bill Number:"+serialNumber);
        int showGstPopup = MyApplication.showGstPopup();
        GSTError.setVisibility(showGstPopup != 2 ? serialNumber < 3 ? View.VISIBLE : View.GONE : View.GONE);
        if(isGSTAvailable){
            gstType.setVisibility(View.VISIBLE);
            gstPercentage.setVisibility(View.VISIBLE);
            tvAmountBeforeTax.setVisibility(View.VISIBLE);
            tvAmountGST.setVisibility(View.VISIBLE);
            gstPercentageTV.setVisibility(View.VISIBLE);
            gstPerLayout.setVisibility(View.VISIBLE);
            amtBeforeTaxLabelTV.setVisibility(View.VISIBLE);
            taxLabelTV.setVisibility(View.VISIBLE);
            GSTTypeLblTV.setVisibility(View.VISIBLE);
            hsnLayout.setVisibility(View.VISIBLE);
            gstTypeLayout.setVisibility(View.VISIBLE);
            gstTitle.setVisibility(View.VISIBLE);

        }else{
            gstType.setVisibility(View.GONE);
            gstPercentage.setVisibility(View.GONE);
            tvAmountBeforeTax.setVisibility(View.GONE);
            tvAmountGST.setVisibility(View.GONE);
            gstPercentageTV.setVisibility(View.GONE);
            gstPercentageTV.setVisibility(View.GONE);
            gstPerLayout.setVisibility(View.GONE);
            gstTitle.setVisibility(View.GONE);
            amtBeforeTaxLabelTV.setVisibility(View.GONE);
            taxLabelTV.setVisibility(View.GONE);
            GSTTypeLblTV.setVisibility(View.GONE);
            hsnLayout.setVisibility(View.GONE);
            itemPriceET.setHint(R.string.enter_price_without_gst);
            priceLblTV.setText(R.string.price);
            gstTypeLayout.setVisibility(View.GONE);

        }

    }

    public boolean checkPermission(){
        hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED){ return true;}
        else{return false; }
    }

    public void internalStoragePermission(){
        try{
            if(checkPermission()==true){ return;}
            else{ startActivity(new Intent(getApplicationContext(), writePermissionAccess.class)); }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateBillNo (View v){
        new BillNumberUpdateDialog(this).show();
    }
    public void addMoreItem(View view){
        Util.postEvents("Add More Item","Add More Item",this.getApplicationContext());
        customDialogClass = new CustomDialogClass(this,null);
        customDialogClass.show();
    }
    public void addItem(View view){
        if(addItemVerify()) {
            Log.v("UNIT", "ADD Item");
            Log.v("UNIT", measurementUnit.getSelectedItemPosition() + " ");
            Util.postEvents("Add Item","Add Item",this.getApplicationContext());
            addItem(itemNameET.getText().toString(),
                    Float.parseFloat(itemPriceET.getText().toString()),
                    Float.parseFloat(gstPercentage.getSelectedItemPosition()>0?gstPercentage.getSelectedItem().toString():"0")
                    ,Float.parseFloat(itemQtyET.getText().toString()),
                    true,
                    imeiNo.getText().toString(),
                    hsnNo.getText().toString(),
                    measurementUnit.getSelectedItemPosition());
            cardItemList.setVisibility(View.VISIBLE);
            layoutBillItem_initial.setVisibility(View.GONE);
        }
    }

    private void getModelData(){
        modelViewModel = ViewModelProviders.of(this).get(ModelViewModel.class);
        modelViewModel.getModels().observe(this, new Observer<List<Model>>() {
            @Override
            public void onChanged(@Nullable List<Model> modelList) {
                models = (ArrayList<Model>) modelList;
                if (modelList == null) {
                    models = new ArrayList<>();
                }
                modelAdapter = new ModelAdapter(BillingNewActivity.this,R.layout.spinner_textview_layout,models);
                itemNameET.setAdapter(modelAdapter);
                Log.v(TAG, "models::" + models);
            }
        });
        try {
            profile = new JSONObject (MyApplication.getUserDetails());
            if(profile.has("gstNo") && profile.getString("gstNo") != null && !profile.getString("gstNo").isEmpty() && !isEdit){
                isGSTAvailable=true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void addItem(final String modelName, final float price,final float gst,final float quantity, boolean isNew,String imei,String hsnNo, final int measurementUnitId){

         NewInvoiceModels newInvoiceModel = isNew?new NewInvoiceModels():newInvoiceModels.get(editPosition);
        setTotal(newInvoiceModel,false);
        calculateAmountBeforeGST(newInvoiceModel,false);
        newInvoiceModel.setInvoiceid(invoiceIdIfEdit);
         newInvoiceModel.setName(modelName);
         newInvoiceModel.setIs_active(true);
         newInvoiceModel.setPrice(price);
         newInvoiceModel.setGst(gst);
         newInvoiceModel.setGstAmount(((price * 100) / (100 + gst)) * quantity);
         newInvoiceModel.setQuantity(quantity);
         newInvoiceModel.setSerial_no(hsnNo);
         newInvoiceModel.setTotalAmount(quantity * price);
        newInvoiceModel.setImei(imei);
        newInvoiceModel.setGstType(gstTypeList.get(gstType.getSelectedItemPosition()));
        Log.v("UNIT", measurementUnitId + " ");
        newInvoiceModel.setMeasurementId(measurementUnitId);
        if(isNew)
         newInvoiceModels.add(newInvoiceModel);
         setTotal(newInvoiceModel,true);
         calculateAmountBeforeGST(newInvoiceModel,true);
         newBillingAdapter.notifyDataSetChanged();

    }

    @Override
    public void itemClick(final int position, boolean isEdit) {
        if(isEdit){
            editPosition =position;
            new CustomDialogClass(this,newInvoiceModels.get(position)).show();
        }else {
            DialogUtils.showAlertDialog(this, "Yes", "No", "Are you sure you want to delete?", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    NewInvoiceModels newInvoiceModel = newInvoiceModels.remove(position);
                    setTotal(newInvoiceModel,false);
                    calculateAmountBeforeGST(newInvoiceModel,false);

                    newBillingAdapter.notifyDataSetChanged();
                }

                @Override
                public void negativeButtonClick() {

                }
            });
        }
    }

    public class CustomDialogClass extends Dialog implements
            android.view.View.OnClickListener {
        public Activity c;
        public Dialog d;
        public Button yes, no;
        private EditText priceEt,quantityEt,imeiNo,hsnNo;
        private AutoCompleteTextView modelName;
        private TextView gstLabelTV;
        private Spinner gstPercentage, measurementUnitSpinner;
        private TextInputLayout priceEdtInputLayout;
        private NewInvoiceModels newInvoiceModel;
        public CustomDialogClass(Activity a, NewInvoiceModels newInvoiceModel) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
            this.newInvoiceModel=newInvoiceModel;
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
            modelName = findViewById(R.id.modelName);
            priceEt = findViewById(R.id.priceEdt);
            quantityEt = findViewById(R.id.quantity);
            gstPercentage = findViewById(R.id.gstPercentage);
            imeiNo= findViewById(R.id.imeiNo);
            gstLabelTV = findViewById(R.id.gstLabelTV);
            hsnNo= findViewById(R.id.hsnNo);
            measurementUnitSpinner = findViewById(R.id.unit);
            priceEdtInputLayout =  findViewById(R.id.priceEdtInputLayout);
            modelName.setAdapter(modelAdapter);
            if(isGSTAvailable) {
                hsnNo.setVisibility(View.VISIBLE);
                priceEdtInputLayout.setHint(getString(R.string.enter_price));
            }
            else {
                hsnNo.setVisibility(View.GONE);
                priceEdtInputLayout.setHint(getString(R.string.enter_price_without_gst));
            }


            if(isGSTAvailable){
                this.gstPercentage.setVisibility(View.VISIBLE);
                this.gstLabelTV.setVisibility(View.VISIBLE);
            } else {
                this.gstPercentage.setVisibility(View.GONE);
                this.gstLabelTV.setVisibility(View.GONE);
            }
            if(this.newInvoiceModel != null){
                modelName.setText(this.newInvoiceModel.getName());
                priceEt.setText(((int)this.newInvoiceModel.getPrice())+"");
                quantityEt.setText(this.newInvoiceModel.getQuantity()+"");
                hsnNo.setText(newInvoiceModel.getSerial_no());
                imeiNo.setText(newInvoiceModel.getImei());
                List<String> gstPList =  Arrays.asList (getResources().getStringArray(R.array.gstPercentage));
                measurementUnitSpinner.setSelection(this.newInvoiceModel.getMeasurementId());
                gstPercentage.setSelection(gstPList.indexOf((int)this.newInvoiceModel.getGst()+""));
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add:
                    if(verifyData()){
                        addItem(modelName.getText().toString(),
                                Float.parseFloat(priceEt.getText().toString()),
                                Float.parseFloat(gstPercentage.getSelectedItemPosition()>0?gstPercentage.getSelectedItem().toString():"0")
                                ,Float.parseFloat(quantityEt.getText().toString()),
                                 this.newInvoiceModel == null ?true:false,
                                imeiNo.getText().toString(),
                                hsnNo.getText().toString(),
                                measurementUnitSpinner.getSelectedItemPosition());
                        dismiss();
                    }
                    break;
                case R.id.cancel:
                    dismiss();
                    break;
                default:
                    break;
            }
        }

        private boolean verifyData() {
            if(modelName.getText().toString().isEmpty()){
                DialogUtils.showToast(c,"Please enter item name");
                return false;
            }else if(priceEt.getText().toString().isEmpty()|| Float.parseFloat(priceEt.getText().toString())==0){
                DialogUtils.showToast(c,"Please enter price");
                return false;
            }else if(quantityEt.getText().toString().isEmpty() || Float.parseFloat(quantityEt.getText().toString())==0){
                DialogUtils.showToast(c,"Please enter quantity");
                return false;
            }else if(isGSTAvailable && gstPercentage.getSelectedItemPosition()==0){
                DialogUtils.showToast(c,"Please select GST %");
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
                            new SimpleDateFormat("dd MMM yyyy");
                    invoiceDateStr = formatter.format(invoiceDate);
                    bill_date.setText("Bill Date: " + invoiceDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.v(TAG, "selctedFromDate::" + invoiceDate);

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void showHideAdditionalDetails(View view){
     if(addressLayout.getVisibility() == View.VISIBLE){
         GSTLayout.setVisibility(View.GONE);
         addressLayout.setVisibility(View.GONE);
//         gstTypeLayout.setVisibility(View.GONE);
         additionalDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_circle, 0);
     }else{
         GSTLayout.setVisibility(View.VISIBLE);
         addressLayout.setVisibility(View.VISIBLE);
//         gstTypeLayout.setVisibility(View.VISIBLE);
         additionalDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_circle, 0);

     }
    }

    private  void setTotal(NewInvoiceModels newInvoiceModel, boolean add){
        if(add)
            total=total+newInvoiceModel.getTotalAmount();
        else
            total=total-newInvoiceModel.getTotalAmount();
        tvTotal.setText(Util.formatDecimalValue(total));

    }

    private void calculateAmountBeforeGST (NewInvoiceModels newInvoiceModel, boolean add){
        if(add)
            totalBeforeGST=totalBeforeGST+newInvoiceModel.getGstAmount();
        else
            totalBeforeGST=totalBeforeGST-newInvoiceModel.getGstAmount();
        tvAmountBeforeTax.setText(Util.formatDecimalValue( totalBeforeGST));
        tvAmountGST.setText(Util.formatDecimalValue( total - totalBeforeGST));
    }

    public void gotoPDFActivity(View v){
        startPDFActivity();
    }
    private void startPDFActivity(){
        String eventName = isEdit ? "Make Bill Edit" : "Make Bill";
        if(verify()) {
            Util.postEvents(eventName,eventName,this.getApplicationContext());
            JSONObject requestObj = new JSONObject();
            try {
                requestObj.put("customerName", edtName.getText().toString());
                requestObj.put("customerMobileNo", edtMobNo.getText().toString());
                requestObj.put("customerAddress", edtAddress.getText().toString());
                requestObj.put("GSTNo", edtGST.getText().toString());
                requestObj.put("totalAmount", total );
                requestObj.put("userid", profile.getString("userid") );
                requestObj.put("invoiceDate",invoiceDateStr);
                requestObj.put("totalAmountBeforeGST", totalBeforeGST );
                if(isGSTAvailable) {
                    requestObj.put("gstBillNo", serialNumber);
                    requestObj.put("nonGstBillNo", 0);
                }else{
                    requestObj.put("gstBillNo", 0);
                    requestObj.put("nonGstBillNo", serialNumber);
                }
                if(isGSTAvailable)
                    requestObj.put("gstType", gstTypeList.get(gstType.getSelectedItemPosition()));
                else
                    requestObj.put("gstType", "");
                String items = gson.toJson(newInvoiceModels);
                requestObj.put("items", new JSONArray(items));
                if(isEdit && newInvoiceModels.size()==0){
                    requestObj.put("is_active",false);
                }
//                if(isEdit && requestObj.getJSONArray("items").length()>0){
//                    int cnt=0;
//                    while(requestObj.getJSONArray("items").length()>cnt){
//                        if(requestObj.getJSONArray("items").getJSONObject(cnt).getInt("id") >0){
//                            requestObj.getJSONArray("items").remove(cnt);
//                        }else
//                            cnt++;
//                    }
//                }

                Log.v("TEST", requestObj.toString());
                if(Util.isNetworkAvailable(this)) {
                    sendInvoice(requestObj);
                }else{
                    DialogUtils.showToast(this,"invoice saved in offline mode.");

                    Intent intent = new Intent(BillingNewActivity.this, PDFActivity.class);
                    intent.putExtra("invoice", requestObj.toString());

                    JSONObject data = new JSONObject();
                    requestObj.put("id",-1);
                    data.put("invoice",requestObj);
                    data.put("items",requestObj.getJSONArray("items"));
                    intent.putExtra("invoiceServer", data.toString());

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
    private boolean verify(){
        if(newInvoiceModels.size()==0 && !isEdit){
            DialogUtils.showToast(this,"Please add product for billing");
            return false;
        }else if(!edtMobNo.getText().toString().isEmpty() &&edtMobNo.getText().toString().length()<10 ){
            DialogUtils.showToast(this,"Please enter valid mobile number");
            return false;
        }
        return true;
    }
    private boolean addItemVerify() {
        if(itemNameET.getText().toString().isEmpty()){
            DialogUtils.showToast(this,"Please enter item name");
            return false;
        }else if(itemPriceET.getText().toString().isEmpty()|| Float.parseFloat(itemPriceET.getText().toString())==0){
            DialogUtils.showToast(this,"Please enter price");
            return false;
        }else if(itemQtyET.getText().toString().isEmpty() || Float.parseFloat(itemQtyET.getText().toString())==0){
            DialogUtils.showToast(this,"Please enter quantity");
            return false;
        }else if(isGSTAvailable && gstPercentage.getSelectedItemPosition()==0){
            DialogUtils.showToast(this,"Please select GST %");
            return false;
        }
        return true;
    }

    private void sendInvoice(final JSONObject invoice){
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject)jsonParser.parse(invoice.toString());
        headerMap.put("Content-Type", "application/json");
        HashMap<String,Object> body=new HashMap();
        body.put("content",jsonObject);
        Call<Object> call = null;
        if(!isEdit){
            call = apiService.invoice(jsonObject);
        }else{
            try {
                call = apiService.updateInvoice(headerMap, this.invoice.getLong("id"),jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        Call<Object> call = apiService.invoice(jsonObject);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP",body.toString());
                    if(body.getBoolean("status")) {

                        if ((body.getJSONObject("data").has("models") &&
                                body.getJSONObject("data").getJSONArray("models").length() > 0) ||
                                (isEdit && body.getJSONObject("data").has("masterItems")
                                        && body.getJSONObject("data").getJSONArray("masterItems").length() > 0)) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<Model>>() {
                            }.getType();
                            if(!isEdit) {
                                final List<Model> myModelList = new Gson().fromJson(body.getJSONObject("data").getJSONArray("models").toString(),
                                        listType);
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        MyApplication.getDatabase().getModelDao().insertAll(myModelList);
                                    }
                                });
                            }
                        }
                        JSONObject object = new JSONObject();
                        if(isEdit){

                            object.put("invoice",body.getJSONObject("data"));
                            object.put("items", body.getJSONObject("data").getJSONArray("masterItems"));
                        }


                            Intent intent = new Intent(BillingNewActivity.this, PDFActivity.class);
                            intent.putExtra("invoice", invoice.toString());
                            intent.putExtra("invoiceServer", isEdit?object.toString():body.getJSONObject("data").toString());
                            startActivity(intent);
                            if (!isEdit && isGSTAvailable)
                                MyApplication.setInvoiceNumber(serialNumber + 1);
                            else if (!isEdit)
                                MyApplication.setInvoiceNumberForNonGst(serialNumber + 1);
                            BillingNewActivity.this.finish();

                    }
                    else{
                        Util.postEvents("Make Bill Fail","Make Bill Fail:elseStatus",getApplicationContext());
                        Util.logErrorApi("/v1/invoice", invoice, "/invoice => status :false", null ,body);
                        DialogUtils.showToast(BillingNewActivity.this,"Failed save invoice server");
                    }
                } catch (JSONException e) {
                    Util.logErrorApi("/v1/invoice", invoice, null,e.toString() ,null);
                    Util.postEvents("Make Bill Fail","Make Bill Fail:catch",getApplicationContext());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Util.postEvents("Make Bill Fail","Make Bill Fail:onFailure",getApplicationContext());
                Util.logErrorApi("/v1/invoice", invoice, t.toString(),null ,null);
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(BillingNewActivity.this,"Failed save invoice server");
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
            invSerialNo.setText(""+serialNumber);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add:
                        if(invSerialNo.getText().toString().isEmpty()){
                        DialogUtils.showToast(c,"Invalid bill no");
                        }else{
                            int no =0;
                            if(isGSTAvailable)
                                no = MyApplication.getInVoiceNumber();
                            else
                                no = MyApplication.getInVoiceNumberForNonGst();
//                            if(Integer.parseInt(invSerialNo.getText().toString())<no){
//                                DialogUtils.showToast(c,"Bill number already exists");
//                            }else{
//                            }
                            serialNumber = Integer.parseInt(invSerialNo.getText().toString());
                            bill_no.setText("Bill Number:"+invSerialNo.getText().toString());
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

        private boolean verifyData(){

            return true;
        }
    }
    private void checkIsEdit() {
        if (getIntent().hasExtra("edit")) {
            try {
                isEdit=true;
                invoice = new JSONObject(getIntent().getStringExtra("invoice"));
                Log.v("EditINV", invoice.toString());
                isGSTAvailable = !invoice.getString("gstType").isEmpty();
                if (isGSTAvailable)
                    serialNumber = invoice.getInt("gstBillNo");
                else
                    serialNumber = invoice.getInt("nonGstBillNo");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void loadDataForInvoice(){
        if(getIntent().hasExtra("edit")){
            try {
                nextBtn.setText("Update Invoice");
                if(isGSTAvailable)
                    serialNumber = invoice.getInt("gstBillNo");
                else
                    serialNumber = invoice.getInt("nonGstBillNo");
                bill_no.setText("Bill Number: "+serialNumber);
//                bill_no.setEnabled(false);
                invoiceIdIfEdit = invoice.getInt("id");
                invoiceDateStr = Util.getFormatedDate(invoice.getString("invoiceDate"));
                bill_date.setText("Bill Date: "+invoiceDateStr);
//                bill_date.setEnabled(false);

                edtName.setText(invoice.getString("customerName"));
                edtAddress.setText(invoice.getString("customerAddress"));
                tvTotal.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmount")));
                edtGST.setText(invoice.getString("GSTNo"));
                edtMobNo.setText(invoice.getString("customerMobileNo"));

                newInvoiceModels = gson.fromJson(invoice.getJSONArray("masterItems").toString(), new TypeToken<List<NewInvoiceModels>>(){}.getType());
                newBillingAdapter = new NewBillingAdapter(newInvoiceModels,this,isGSTAvailable);
                invoiceItems.setAdapter(newBillingAdapter);
                total = (float)invoice.getDouble("totalAmount");
                totalBeforeGST = 0;
                if(isGSTAvailable){
                    for(int i=0;i<newInvoiceModels.size();i++){
                        totalBeforeGST=totalBeforeGST+newInvoiceModels.get(i).getGstAmount();
                    }

                }else{
                    totalBeforeGST = total;
                }

                tvAmountBeforeTax.setText(Util.formatDecimalValue( totalBeforeGST));
                tvAmountGST.setText(Util.formatDecimalValue( total - totalBeforeGST));
                cardItemList.setVisibility(View.VISIBLE);
                layoutBillItem_initial.setVisibility(View.GONE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void scanCode(View v){
        Util.postEvents("Scan Button","Scan Button",this.getApplicationContext());

        new IntentIntegrator(this).setOrientationLocked(false)
                .initiateScan(); // `this` is the current Activity
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermission()==false)finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if(layoutBillItem_initial.getVisibility() == View.VISIBLE){
                    imeiNo.setText(imeiNo.getText().toString().isEmpty()? result.getContents():imeiNo.getText().toString()+","+result.getContents());
            }else if(customDialogClass.isShowing()){
                    customDialogClass.imeiNo.setText(imeiNo.getText().toString().isEmpty()? result.getContents():imeiNo.getText().toString()+","+result.getContents());
                }
        }

        }
    }
}
