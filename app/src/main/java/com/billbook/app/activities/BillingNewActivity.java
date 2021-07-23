package com.billbook.app.activities;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.databinding.ActivityBillingNewBinding;
import com.billbook.app.databinding.LayoutItemBillBinding;
import com.billbook.app.viewmodel.InvoiceItemsViewModel;
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
    private InvoiceItemsViewModel invoiceItemViewModel;
    final String TAG = "BillingNewActivity";
    private ArrayList models = new ArrayList<Model>();
//    private ArrayList<NewInvoiceModels> newInvoiceModels = new ArrayList<>();
    private ArrayList<InvoiceItems> invoiceItemModel = new ArrayList<>();
    private ModelAdapter modelAdapter;
    private NewBillingAdapter newBillingAdapter;
    private float total, totalBeforeGST;
    private String invoiceDateStr;
    private Date invoiceDate;
    private Gson gson = new Gson();
    private List<String> gstTypeList, measurementUnitTypeList;
    private JSONObject profile;
    private boolean isGSTAvailable;
    private int editPosition = -1;
    private int invoiceIdIfEdit = -1;
    private int serialNumber = 0;
    private int hasWriteStoragePermission;
    private boolean isEdit = false;
    private JSONObject invoice;
    private CustomDialogClass customDialogClass;

    private ActivityBillingNewBinding binding;
    private LayoutItemBillBinding billItemBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillingNewBinding.inflate(getLayoutInflater());
        billItemBinding = binding.includedLayoutItemBill;

        View view = binding.getRoot();
        setContentView(view);

        setTitle("Billing");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        checkIsEdit();
        //getModelData();
        getInvoiceItemsFromDatabase();
        internalStoragePermission();
        initUI();
        loadDataForInvoice();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initUI() {
        DateFormat formatter =
                new SimpleDateFormat("dd MMM yyyy");
        invoiceDateStr = formatter.format(new Date());
        binding.billDate.setText("Bill Date: " + invoiceDateStr);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.invoiceItems.setLayoutManager(mLayoutManager);
        binding.invoiceItems.setItemAnimator(new DefaultItemAnimator());
        newBillingAdapter = new NewBillingAdapter(invoiceItemModel, this, isGSTAvailable);
        binding.invoiceItems.setAdapter(newBillingAdapter);

        gstTypeList = Arrays.asList(getResources().getStringArray(R.array.gst_type));
        measurementUnitTypeList = Arrays.asList(getResources().getStringArray(R.array.measurementUnit));
        if (isGSTAvailable)
            serialNumber = MyApplication.getInVoiceNumber();
        else
            serialNumber = MyApplication.getInVoiceNumberForNonGst();

        binding.billNo.setText("Bill Number:" + serialNumber);

        int showGstPopup = MyApplication.showGstPopup();
        binding.GSTError.setVisibility(showGstPopup != 2 ? serialNumber < 3 ? View.VISIBLE : View.GONE : View.GONE);

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
        billItemBinding.hsnLayout.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);

        billItemBinding.gstPercentage.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
        if (isGSTAvailable) {
            billItemBinding.itemPriceET.setHint(R.string.enter_price_without_gst);
            billItemBinding.priceLblTV.setText(R.string.price);
        }
    }

    public boolean checkPermission() {
        hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void internalStoragePermission() {
        try {
            if (checkPermission() == true) {
                return;
            } else {
                startActivity(new Intent(getApplicationContext(), StoragePermissionRequestActivity.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBillNo(View v) {
        new BillNumberUpdateDialog(this).show();
    }

    public void addMoreItem(View view) {
        Util.postEvents("Add More Item", "Add More Item", this.getApplicationContext());
        customDialogClass = new CustomDialogClass(this, null);
        customDialogClass.show();
    }

    public void addItem(View view) {
        if (addItemVerify()) {
            Util.postEvents("Add Item", "Add Item", this.getApplicationContext());

            addItemToDatabase(billItemBinding.itemNameET.getText().toString(),
                    Float.parseFloat(billItemBinding.itemPriceET.getText().toString()),
                    Float.parseFloat(billItemBinding.gstPercentage.getSelectedItemPosition() > 0 ? billItemBinding.gstPercentage.getSelectedItem().toString() : "0")
                    , Float.parseFloat(billItemBinding.itemQtyET.getText().toString()),
                    true,
                    billItemBinding.imeiNo.getText().toString(),
                    billItemBinding.hsnNo.getText().toString(),
                    billItemBinding.unit.getSelectedItemPosition());

            binding.cardItemList.setVisibility(View.VISIBLE);
            binding.layoutBillItemInitial.setVisibility(View.GONE);

        }


    }

    public void addItemToDatabase(final String modelName, final float price, final float gst, final float quantity, boolean isNew, String imei, String hsnNo, final int measurementUnitId){

        if(isNew==false){
            int localId = invoiceItemModel.get(editPosition).getLocalid();
            InvoiceItems newInvoiceItem  = new InvoiceItems(measurementUnitId, modelName, quantity, price, "1", ((price * 100) / (100 + gst)) * quantity, gst, true, 0, hsnNo, imei,quantity * price,invoiceIdIfEdit,1,localId,-1);
            invoiceItemViewModel.updateByLocalId(newInvoiceItem);
            getInvoiceItemsFromDatabase();
            return;
        }

        if(isEdit)return;
        invoiceItemViewModel = new ViewModelProvider(this).get(InvoiceItemsViewModel.class);
        InvoiceItems newInvoiceItem;
        if(isGSTAvailable){ newInvoiceItem = new InvoiceItems(measurementUnitId, modelName, quantity, price, gstTypeList.get(binding.gstType.getSelectedItemPosition()), ((price * 100) / (100 + gst)) * quantity, gst, true, 0, hsnNo, imei,quantity * price,invoiceIdIfEdit,1,1,-1); }
        else{ newInvoiceItem = new InvoiceItems(measurementUnitId, modelName, quantity, price, gstTypeList.get(binding.gstType.getSelectedItemPosition()), ((price * 100) / (100 + gst)) * quantity, gst, true, 0,hsnNo,imei,quantity * price,invoiceIdIfEdit,1,1,-1); }
        invoiceItemViewModel.insert(newInvoiceItem);
        setTotal(newInvoiceItem, true);
        calculateAmountBeforeGST(newInvoiceItem, true);
        newBillingAdapter.notifyDataSetChanged();
        getInvoiceItemsFromDatabase();
    }

    private void getInvoiceItemsFromDatabase(){
        invoiceItemViewModel = ViewModelProviders.of(this).get(InvoiceItemsViewModel.class);
        invoiceItemViewModel.getInvoices().observe(this, new Observer<List<InvoiceItems>>() {
            @Override
            public void onChanged(List<InvoiceItems> invoiceItems) {
                invoiceItemModel.clear();
                for(int i =0;i<invoiceItems.size();i++)
                invoiceItemModel.add(invoiceItems.get(i));
            }
        });

        try {
            profile = new JSONObject(MyApplication.getUserDetails());
            if (profile.has("gstNo") && profile.getString("gstNo") != null && !profile.getString("gstNo").isEmpty() && !isEdit) {
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
//        newInvoiceModel.setInvoiceid(invoiceIdIfEdit);
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
            editPosition = position;
            new CustomDialogClass(this, invoiceItemModel.get(position)).show();
        } else {
            DialogUtils.showAlertDialog(this, "Yes", "No", "Are you sure you want to delete?", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    setTotal(invoiceItemModel.get(position), false);
                    calculateAmountBeforeGST(invoiceItemModel.get(position), false);
                    invoiceItemViewModel.delete(invoiceItemModel.get(position));
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
        private EditText priceEt, quantityEt, imeiNo, hsnNo;
        private AutoCompleteTextView modelName;
        private TextView gstLabelTV;
        private Spinner gstPercentage, measurementUnitSpinner;
        private TextInputLayout priceEdtInputLayout;
        private InvoiceItems newInvoiceModel;

        public CustomDialogClass(Activity a, InvoiceItems newInvoiceModel) {
            super(a);
            // TODO Auto-generated constructor stub
            this.c = a;
            this.newInvoiceModel = newInvoiceModel;
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
            imeiNo = findViewById(R.id.imeiNo);
            gstLabelTV = findViewById(R.id.gstLabelTV);
            hsnNo = findViewById(R.id.hsnNo);
            measurementUnitSpinner = findViewById(R.id.unit);
            priceEdtInputLayout = findViewById(R.id.priceEdtInputLayout);
            modelName.setAdapter(modelAdapter);
            if (isGSTAvailable) {
                hsnNo.setVisibility(View.VISIBLE);
                priceEdtInputLayout.setHint(getString(R.string.enter_price));
            } else {
                hsnNo.setVisibility(View.GONE);
                priceEdtInputLayout.setHint(getString(R.string.enter_price_without_gst));
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
                priceEt.setText( this.newInvoiceModel.getPrice() + "");
                quantityEt.setText(this.newInvoiceModel.getQuantity() + "");
                hsnNo.setText(newInvoiceModel.getSerial_no());
                imeiNo.setText(newInvoiceModel.getImei());
                List<String> gstPList = Arrays.asList(getResources().getStringArray(R.array.gstPercentage));
                measurementUnitSpinner.setSelection(this.newInvoiceModel.getMeasurementId());
                gstPercentage.setSelection(gstPList.indexOf((int) this.newInvoiceModel.getGst() + ""));
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add:
                    if (verifyData()) {

                        

                        addItemToDatabase(modelName.getText().toString(),
                                Float.parseFloat(priceEt.getText().toString()),
                                Float.parseFloat(gstPercentage.getSelectedItemPosition() > 0 ? gstPercentage.getSelectedItem().toString() : "0")
                                , Float.parseFloat(billItemBinding.itemQtyET.getText().toString()),
                                this.newInvoiceModel == null ? true : false,
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
                            new SimpleDateFormat("dd MMM yyyy");
                    invoiceDateStr = formatter.format(invoiceDate);
                    binding.billDate.setText("Bill Date: " + invoiceDateStr);
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

    private void setTotal(InvoiceItems newInvoiceModel, boolean add) {
        if (add)
            total = total + newInvoiceModel.getTotalAmount();
        else
            total = total - newInvoiceModel.getTotalAmount();
        binding.tvTotal.setText(Util.formatDecimalValue(total));

    }

    private void calculateAmountBeforeGST(InvoiceItems newInvoiceModel, boolean add) {
        totalBeforeGST = 0;
                if (add)
            totalBeforeGST = totalBeforeGST + newInvoiceModel.getGstAmount();
        else
            totalBeforeGST = totalBeforeGST - newInvoiceModel.getGstAmount();

        binding.tvAmountBeforeTax.setText(Util.formatDecimalValue(totalBeforeGST));
        binding.tvAmountGST.setText(Util.formatDecimalValue(total - totalBeforeGST));
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
                requestObj.put("totalAmount", total);
                requestObj.put("userid", profile.getString("userid"));
                requestObj.put("invoiceDate", invoiceDateStr);
                requestObj.put("totalAmountBeforeGST", totalBeforeGST);
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
                String items = gson.toJson(invoiceItemModel);
                requestObj.put("items", new JSONArray(items));
                if (isEdit && invoiceItemModel.size() == 0) {
                    requestObj.put("is_active", false);
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
                Log.i("aman",items);
                Log.v("TEST", requestObj.toString());
                if (Util.isNetworkAvailable(this)) {
                    sendInvoice(requestObj);
                } else {
                    DialogUtils.showToast(this, "invoice saved in offline mode.");

                    Intent intent = new Intent(BillingNewActivity.this, PDFActivity.class);
                    intent.putExtra("invoice", requestObj.toString());

                    JSONObject data = new JSONObject();
                    requestObj.put("id", -1);
                    data.put("invoice", requestObj);
                    data.put("items", requestObj.getJSONArray("items"));
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

    private boolean verify() {
        if (invoiceItemModel.size() == 0 ) {
            DialogUtils.showToast(this, isEdit? "Please add atleast one product" : "Please add product for billing");
            return false;
        } else if (!binding.edtMobNo.getText().toString().isEmpty() && binding.edtMobNo.getText().toString().length() < 10) {
            DialogUtils.showToast(this, "Please enter valid mobile number");
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
        } else if (isGSTAvailable && billItemBinding.gstPercentage.getSelectedItemPosition() == 0) {
            DialogUtils.showToast(this, "Please select GST %");
            return false;
        }
        return true;
    }

    private void sendInvoice(final JSONObject invoice) {
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
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
                        if ((body.getJSONObject("data").has("models") &&
                                body.getJSONObject("data").getJSONArray("models").length() > 0) ||
                                (isEdit && body.getJSONObject("data").has("masterItems")
                                        && body.getJSONObject("data").getJSONArray("masterItems").length() > 0)) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<Model>>() {
                            }.getType();
                            if (!isEdit) {
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
                        if (isEdit) {
                            object.put("invoice", body.getJSONObject("data"));
                            object.put("items", body.getJSONObject("data").getJSONArray("masterItems"));
                        }


                        Intent intent = new Intent(BillingNewActivity.this, PDFActivity.class);
                        intent.putExtra("invoice", invoice.toString());
                        intent.putExtra("invoiceServer", isEdit ? object.toString() : body.getJSONObject("data").toString());
                        startActivity(intent);
                        if (!isEdit && isGSTAvailable)
                            MyApplication.setInvoiceNumber(serialNumber + 1);
                        else if (!isEdit)
                            MyApplication.setInvoiceNumberForNonGst(serialNumber + 1);
                        BillingNewActivity.this.finish();

                    } else {
                        Util.postEvents("Make Bill Fail", "Make Bill Fail:elseStatus", getApplicationContext());
                        Util.logErrorApi("/v1/invoice", jsonObject, "/invoice => status :false", null, (JsonObject) jsonParser.parse(body.toString()));
                        DialogUtils.showToast(BillingNewActivity.this, "Failed save invoice server");
                    }
                } catch (JSONException e) {
                    assert body != null;
                    Util.logErrorApi("/v1/invoice", jsonObject, null, Arrays.toString(e.getStackTrace()), (JsonObject) jsonParser.parse(body.toString()));
                    Util.postEvents("Make Bill Fail", "Make Bill Fail:catch", getApplicationContext());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Util.postEvents("Make Bill Fail", "Make Bill Fail:onFailure", getApplicationContext());
                Util.logErrorApi("/v1/invoice", jsonObject, Arrays.toString(t.getStackTrace()), null, null);
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
                        binding.billNo.setText("Bill Number:" + invSerialNo.getText().toString());
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
                isEdit = true;
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

    private void loadDataForInvoice() {
        if (getIntent().hasExtra("edit")) {
            try {
                binding.nextBtn.setText("Update Invoice");
                if (isGSTAvailable)
                    serialNumber = invoice.getInt("gstBillNo");
                else
                    serialNumber = invoice.getInt("nonGstBillNo");
                binding.billNo.setText("Bill Number: " + serialNumber);
//                bill_no.setEnabled(false);
                invoiceIdIfEdit = invoice.getInt("id");
                invoiceDateStr = Util.getFormatedDate(invoice.getString("invoiceDate"));
                binding.billDate.setText("Bill Date: " + invoiceDateStr);
//                bill_date.setEnabled(false);

                binding.edtName.setText(invoice.getString("customerName"));
                binding.edtAddress.setText(invoice.getString("customerAddress"));
                binding.tvTotal.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmount")));
                binding.edtGST.setText(invoice.getString("GSTNo"));
                binding.edtMobNo.setText(invoice.getString("customerMobileNo"));

                invoiceItemModel = gson.fromJson(invoice.getJSONArray("masterItems").toString(), new TypeToken<List<InvoiceItems>>() {
                }.getType());
                newBillingAdapter = new NewBillingAdapter(invoiceItemModel, this, isGSTAvailable);
                binding.invoiceItems.setAdapter(newBillingAdapter);
                total = (float) invoice.getDouble("totalAmount");
                totalBeforeGST = 0;
                if (isGSTAvailable) {
                    for (int i = 0; i < invoiceItemModel.size(); i++) {
                        totalBeforeGST = totalBeforeGST + invoiceItemModel.get(i).getGstAmount();
                    }

                } else {
                    totalBeforeGST = total;
                }

                binding.tvAmountBeforeTax.setText(Util.formatDecimalValue(totalBeforeGST));
                binding.tvAmountGST.setText(Util.formatDecimalValue(total - totalBeforeGST));
                binding.cardItemList.setVisibility(View.VISIBLE);
                binding.layoutBillItemInitial.setVisibility(View.GONE);

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
        if (checkPermission() == false) finish();
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
                if (binding.layoutBillItemInitial.getVisibility() == View.VISIBLE) {
                    billItemBinding.imeiNo.setText(billItemBinding.imeiNo.getText().toString().isEmpty() ? result.getContents() : billItemBinding.imeiNo.getText().toString() + "," + result.getContents());
                } else if (customDialogClass.isShowing()) {
                    customDialogClass.imeiNo.setText(billItemBinding.imeiNo.getText().toString().isEmpty() ? result.getContents() : billItemBinding.imeiNo.getText().toString() + "," + result.getContents());
                }
            }

        }
    }
}
