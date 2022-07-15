package com.billbook.app.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.adapters.NewInvoiceShortBillInvoiceProductAdapter;
import com.billbook.app.database.daos.InvoiceItemDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.databinding.ActivityPdfBinding;
import com.billbook.app.databinding.InvoiceAmountLayoutUpdatedBinding;
import com.billbook.app.databinding.PdfContentNewBinding;
import com.billbook.app.databinding.ShortBillItemLayoutBinding;
import com.billbook.app.databinding.ShortBillLayoutBinding;
import com.billbook.app.viewmodel.InvoiceItemsViewModel;
import com.billbook.app.viewmodel.InvoiceViewModel;
import com.google.gson.Gson;
import com.billbook.app.BuildConfig;
import com.billbook.app.R;
import com.billbook.app.adapters.NewInvoicePurchaseAdapter;
import com.billbook.app.database.models.NewInvoiceModels;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.PdfWriter;
import com.billbook.app.utils.Util;
//import com.squareup.picasso.BuildConfig;
import com.readystatesoftware.chuck.internal.ui.MainActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.dantsu.escposprinter.EscPosPrinter;
//import com.dantsu.escposprinter.connection.DeviceConnection;
//import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
//import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
//import com.dantsu.escposprinter.connection.tcp.TcpConnection;
//import com.dantsu.escposprinter.connection.usb.UsbConnection;
//import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
//import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
//import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
//import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
//import com.dantsu.escposprinter.exceptions.EscPosParserException;
//import com.dantsu.escposprinter.textparser.PrinterTextParserImg;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PDFActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_BLUETOOTH = 1;
    private static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    private static final int PERMISSION_BLUETOOTH_SCAN = 4;
    private JSONObject invoice;
    private RecyclerView recyclerViewInvoiceProducts,recyclerViewShortBillInvoiceProducts;
    private List<NewInvoiceModels> items;
    private List<InvoiceItems> curItems=null;
    private InvoiceItemsViewModel invoiceItemViewModel;
    private InvoiceViewModel invoiceViewModel;
    private Gson gson = new Gson();
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    private String filepath = null;
    private File pdfFile;
    private static final String TAG = "PDFActivity";
    private JSONObject profile;
    private boolean isGSTAvailable;
    private int invID = -1;
    private long localInvoiceId=0;
    private int invoiceNumber;
    private String GSTType = "";
    private String imageURL,signatureURL;
    private ActivityPdfBinding binding;
    private PdfContentNewBinding pdfBinding;
    private ShortBillItemLayoutBinding shortBillItemLayoutBinding;
    private ShortBillLayoutBinding  shortBillLayoutBinding;
    private InvoiceAmountLayoutUpdatedBinding invoiceAmountLayoutUpdatedBinding;
    private String vendorName = "MArcn Technology", vendorAddress = "Shop 10,82/86,Abdul Rehman Street", vendorLocation = "MUMBAI-400003(Maharastra)";
    private boolean hasCompanyLogo = false, hasSignatureLogo = false, shortBillPrint = false, longBillPrint = false;
    private boolean loadedCompanyLogo = false, loadedSignatureLogo = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfBinding.inflate(getLayoutInflater());
        pdfBinding = binding.includedLayoutPdfContent;
        shortBillLayoutBinding = binding.includedLayoutShortBill;
        invoiceAmountLayoutUpdatedBinding = pdfBinding.invoiceAmountLayoutUpdated;
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_pdf);
        invoiceViewModel = ViewModelProviders.of(this).get(InvoiceViewModel.class);
        invoiceItemViewModel = ViewModelProviders.of(this).get(InvoiceItemsViewModel.class);
        binding.btnLongPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.login_button_structure));
        binding.btnLongPdf.setTextColor(ContextCompat.getColor(this,R.color.white));
        longBillPrint = true;
        initUI();
        setProfileData();
        setData();
        setShortFormatData();
    }

    private void initUI() {
        recyclerViewInvoiceProducts = pdfBinding.recyclerViewInvoiceProducts;
        recyclerViewShortBillInvoiceProducts = shortBillLayoutBinding.recyclerViewShortBillInvoiceProducts;
        binding.btnPrintBill.setOnClickListener(this);
        binding.btnHome.setOnClickListener(this);
        binding.btnShare.setOnClickListener(this);
        binding.btnLongPdf.setOnClickListener(this);
        binding.btnShortPdf.setOnClickListener(this);
    }

    public static void setDataAfterInvoiceItems(List<InvoiceItems> invoiceItems,Context context,boolean isGSTAvailable, RecyclerView recyclerViewInvoiceProducts, String GSTType){
        NewInvoicePurchaseAdapter newInvoicePurchaseAdapter = new NewInvoicePurchaseAdapter(context, invoiceItems, isGSTAvailable,GSTType);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        recyclerViewInvoiceProducts.setLayoutManager(mLayoutManager);
        recyclerViewInvoiceProducts.setAdapter(newInvoicePurchaseAdapter);
    }

    public static void setDataAfterInvoiceItemsShort(List<InvoiceItems> invoiceItems,Context context,boolean isGSTAvailable, RecyclerView recyclerViewShortBillInvoiceProducts, String GSTType){
        NewInvoiceShortBillInvoiceProductAdapter newInvoiceShortBillInvoiceProductAdapter = new NewInvoiceShortBillInvoiceProductAdapter(context, invoiceItems, isGSTAvailable,GSTType);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        recyclerViewShortBillInvoiceProducts.setLayoutManager(mLayoutManager);
        recyclerViewShortBillInvoiceProducts.setAdapter(newInvoiceShortBillInvoiceProductAdapter);
    }

    private void setProfileData(){
        try{
            profile = new JSONObject(MyApplication.getUserDetails());
            Log.v("profile", String.valueOf(profile));
            if(profile.has("additionalData") && profile.getString("additionalData").length()!=0){
                invoiceAmountLayoutUpdatedBinding.tvAdditionalDetails.setVisibility(View.VISIBLE);
                invoiceAmountLayoutUpdatedBinding.tvAdditionalDetails.setText(profile.getString("additionalData"));
            }
            imageURL = profile.has("companyLogo") ? profile.getString("companyLogo") : null;
            signatureURL = profile.has("signatureImage") ? profile.getString("signatureImage").replaceAll("\\/","/") : null;
            loadAndSetCompanyLogo();
            loadAndSetSignatureImage();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private void setData() {
        try {
            localInvoiceId = getIntent().getExtras().getLong("localInvId");
            invoiceViewModel.getCurrentInvoice(localInvoiceId).observe(this, invoiceModelV2 -> {
                try {
                    invoice = new JSONObject(new Gson().toJson(invoiceModelV2));
                    if (invoice.has("gstType") && !invoice.getString("gstType").isEmpty()) {
                        isGSTAvailable = true;
                        invoiceAmountLayoutUpdatedBinding.gstTotalLayout.setVisibility(View.VISIBLE);
                        invoiceAmountLayoutUpdatedBinding.totalAmountBeforeTaxLayout.setVisibility(View.VISIBLE);
                        invoiceNumber = getIntent().getExtras().getInt("gstBillNo");

                    } else {
                        isGSTAvailable = false;
                        TextView taxorBillTv = findViewById(R.id.taxorBillTv);
                        taxorBillTv.setText("Bill");
                        invoiceAmountLayoutUpdatedBinding.gstTotalLayout.setVisibility(View.GONE);
                        invoiceAmountLayoutUpdatedBinding.totalAmountBeforeTaxLayout.setVisibility(View.GONE);
                        invoiceNumber = getIntent().getExtras().getInt("nonGstBillNo");
                    }

                    pdfBinding.tvVendorName.setText(profile.getString("shopName"));
                    pdfBinding.tvStoreAddress.setText(profile.getString("shopAddr") + " " + profile.getString("city")
                            + " " + profile.getString("state") + " - " + profile.getString("pincode"));
                    pdfBinding.tvGSTNo.setText(profile.has("gstNo") ? profile.getString("gstNo") : "");
                    pdfBinding.mobileNoRetailer.setText("Mobile No- " + profile.getString("mobileNo"));
                    invID = getIntent().getExtras().getInt("id");

//                        Log.i(TAG, "setData: GST => " + isGSTAvailable);
                  //  pdfBinding.tvPreTax.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
                  //  pdfBinding.llForHeader.setWeightSum(isGSTAvailable ? (float) 10.5 : 9);
                  //  pdfBinding.footer.setWeightSum(isGSTAvailable ? (float) 10.5 : 9);
                   // pdfBinding.paddingLabelGst.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);

                    if (invoice.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailable) {
                        GSTType = "CGST/SGST (Local customer)";
                        pdfBinding.IGST.setVisibility(View.GONE);
                        pdfBinding.CGST.setVisibility(View.VISIBLE);
                        pdfBinding.SGST.setVisibility(View.VISIBLE);
                       //pdfBinding.padding3.setVisibility(View.GONE);
                       /* pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.5f));*/
                        /*pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 3.5f));*/
                    } else if (invoice.getString("gstType").equals("IGST (Central/outstation customer)") && isGSTAvailable) {
                        GSTType = "IGST (Central/outstation customer)";
                        pdfBinding.IGST.setVisibility(View.VISIBLE);
                       // pdfBinding.padding1.setVisibility(View.GONE);
                       // pdfBinding.padding2.setVisibility(View.GONE);
                        pdfBinding.CGST.setVisibility(View.GONE);
                        pdfBinding.SGST.setVisibility(View.GONE);
                       /* pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 4.5f));*/
                       /* pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f));*/
                    } else {
                        pdfBinding.IGST.setVisibility(View.GONE);
                        pdfBinding.CGST.setVisibility(View.GONE);
                        pdfBinding.SGST.setVisibility(View.GONE);
                        //pdfBinding.padding1.setVisibility(View.GONE);
                       // pdfBinding.padding2.setVisibility(View.GONE);
                       // pdfBinding.padding3.setVisibility(View.GONE);
                       /* pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.5f));*/
                       /* pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));*/
                    }
                    if (invoice.getString("GSTNo").isEmpty()) {
                       // pdfBinding.custGstLayout.setVisibility(View.INVISIBLE);
                    } else {
                       // pdfBinding.custGstLayout.setVisibility(View.VISIBLE);
                       // pdfBinding.customerGst.setText(invoice.getString("GSTNo"));
                    }
                    pdfBinding.txtInvoiceDate.setText(invoice.getString("invoiceDate"));
                    pdfBinding.txtInvoiceNo.setText("" + invoiceNumber);
                    float gst = 0;
                    if (isGSTAvailable)
                        gst = Float.parseFloat(invoice.getString("totalAmount")) -
                                Float.parseFloat(invoice.getString("totalAmountBeforeGST"));
                    invoiceAmountLayoutUpdatedBinding.totalGST.setText(Util.formatDecimalValue(gst));
                    Log.v("CustomerName", String.valueOf(invoice));
                    String custName= invoice.getString("customerName");
                    String custNo =invoice.getString("customerMobileNo");
                    String custAdd =invoice.getString("customerAddress");
                    pdfBinding.customerName.setText(custName);

                    if(custNo.isEmpty())
                    {
                        custNo=getIntent().getExtras().getString("customerMobileNo");
                    }
                    if(custName.isEmpty())
                    {
                        custName=getIntent().getExtras().getString("customerName");
                    }
                    if(custAdd.isEmpty())
                    {
                        custAdd=getIntent().getExtras().getString("customerAddress");
                    }

                    Log.v("shortformatInvoice", profile.getString("gstNo"));
                    pdfBinding.edtName.setText(custName.equals(null) ?" ":custName+" ");
                    pdfBinding.edtAddress.setText(custAdd.equals(null) ?" ":custAdd+" ");
                    pdfBinding.edtMobNo.setText(custNo.equals(null) ?" ":custNo+" ");
//            tvGSTNo.setText(invoice.getString("GSTNo")+" ");
                    invoiceAmountLayoutUpdatedBinding.tvAmountBeforeTax.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmountBeforeGST")));
//                        invoiceAmountLayoutUpdatedBinding.tvTotal.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmount")));
                    float totalAfterDiscount = 0, totalAmount = 0;
                    totalAmount = Float.parseFloat(invoice.getString("totalAmount"));
                    if (invoice.has("totalAfterDiscount")) {
                        totalAfterDiscount = (float) invoice.getDouble("totalAfterDiscount");
                    } else {
                        totalAfterDiscount = totalAmount;
                    }
                    invoiceAmountLayoutUpdatedBinding.tvTotal.setText(Util.formatDecimalValue(totalAfterDiscount));
                    invoiceAmountLayoutUpdatedBinding.tvDiscount.setText(Util.formatDecimalValue(totalAmount - totalAfterDiscount));
                    new getCurrentItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao(), getIntent().getExtras().getLong("idForItem"), PDFActivity.this, isGSTAvailable, recyclerViewInvoiceProducts, GSTType).execute();

//                        items = gson.fromJson(invoice.getJSONArray("items").toString(), new TypeToken<List<NewInvoiceModels>>() {
//                        }.getType());

                    if (isGSTAvailable)
                        invoiceAmountLayoutUpdatedBinding.GSTTitle.setText("GST " + (invoice.getString("gstType").equals("CGST/SGST (Local customer)") ? "(SGST/CGST)" : "(IGST)"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }


    }

    private  void setShortFormatData(){
        try{
            localInvoiceId = getIntent().getExtras().getLong("localInvId");
            invoiceViewModel.getCurrentInvoice(localInvoiceId).observe(this, invoiceModelV2 -> {
                try{
                    Log.v("Inshort", String.valueOf(profile.getString("email").isEmpty()));
                    invoice = new JSONObject(new Gson().toJson(invoiceModelV2));
                    shortBillLayoutBinding.tvVendorName.setText(profile.getString("shopName"));
                    shortBillLayoutBinding.tvStoreAddress.setText(profile.getString("shopAddr") + " " + profile.getString("city")
                            + " " + profile.getString("state") + " - " + profile.getString("pincode"));
                    shortBillLayoutBinding.mobileNoRetailer.setText(" Phone: " + profile.getString("mobileNo"));
                    if(profile.has("email") && !profile.getString("email").isEmpty()){
                        shortBillLayoutBinding.emailRetailer.setText(profile.getString("email"));
                    } else {
                        shortBillLayoutBinding.emailRetailer.setVisibility(View.GONE);
                    }

                    if(getIntent().getExtras().getInt("gstBillNo") != 0){
                        isGSTAvailable =true;
                        invoiceNumber = getIntent().getExtras().getInt("gstBillNo");
                        shortBillLayoutBinding.tvGSTNo.setText(profile.getString("gstNo"));
                    } else {
                        shortBillLayoutBinding.productTax.setVisibility(View.GONE);
                        shortBillLayoutBinding.tvGSTNo.setVisibility(View.GONE);
                        shortBillLayoutBinding.GSTField1Layout.setVisibility(View.GONE);
                        shortBillLayoutBinding.GSTField2Layout.setVisibility(View.GONE);
                        shortBillLayoutBinding.GSTField3Layout.setVisibility(View.GONE);
                        shortBillLayoutBinding.GSTField4Layout.setVisibility(View.GONE);
                        invoiceNumber = getIntent().getExtras().getInt("nonGstBillNo");
                        shortBillLayoutBinding.invoiceType.setText("***Invoice***");
                    }
                    shortBillLayoutBinding.txtInvoiceDate.setText(invoice.getString("invoiceDate"));
                    shortBillLayoutBinding.txtInvoiceNo.setText("" + invoiceNumber);
                    Log.v("formatInvoice", profile.getString("gstNo"));
                    String custName= invoice.getString("customerName");
                    String custNo =invoice.getString("customerMobileNo");
                    String custAdd =invoice.getString("customerAddress");
                    if(custNo.isEmpty())
                    {
                        custNo=getIntent().getExtras().getString("customerMobileNo");
                    }
                    if(custName.isEmpty())
                    {
                        custName=getIntent().getExtras().getString("customerName");
                    }
                    if(custAdd.isEmpty())
                    {
                        custAdd=getIntent().getExtras().getString("customerAddress");
                    }
                    shortBillLayoutBinding.edtMobNo.setText(custNo);
                    shortBillLayoutBinding.edtName.setText(custName);
                    if(custAdd.isEmpty()){
                        shortBillLayoutBinding.edtAddressLayout.setVisibility(View.GONE);
                    }else {
                        shortBillLayoutBinding.edtAddress.setText(custAdd);
                    }
                    shortBillLayoutBinding.invoiceItems.setText(getIntent().getExtras().getString("itemsSize"));
                    shortBillLayoutBinding.invoiceItemsQty.setText(getIntent().getExtras().getString("quantityCount"));
                    float totalAfterDiscount = 0, totalAmount = 0;
                    totalAmount = Float.parseFloat(invoice.getString("totalAmount"));
                    if (invoice.has("totalAfterDiscount")) {
                        totalAfterDiscount = (float) invoice.getDouble("totalAfterDiscount");
                    } else {
                        totalAfterDiscount = totalAmount;
                    }
                    shortBillLayoutBinding.invoiceNetAmnt.setText(Util.formatDecimalValue(totalAfterDiscount));
                    shortBillLayoutBinding.invoiceNetAmntTotal.setText(Util.formatDecimalValue(totalAfterDiscount));
                    shortBillLayoutBinding.invoiceTotalDiscount.setText(Util.formatDecimalValue(totalAmount - totalAfterDiscount));
                    new getCurrentItemsAsyncTaskShort(MyApplication.getDatabase().invoiceItemDao(), getIntent().getExtras().getLong("idForItem"), PDFActivity.this, isGSTAvailable, recyclerViewShortBillInvoiceProducts, GSTType).execute();
                    if (invoice.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailable) {
                        GSTType = "CGST/SGST (Local customer)";
                        shortBillLayoutBinding.invoiceSGSTAmt.setText(getIntent().getExtras().getString("shortBillGstAmt"));
                        shortBillLayoutBinding.invoiceCGSTAmt.setText(getIntent().getExtras().getString("shortBillGstAmt"));
                    } else if (invoice.getString("gstType").equals("IGST (Central/outstation customer)") && isGSTAvailable) {
                        GSTType = "IGST (Central/outstation customer)";
                        shortBillLayoutBinding.invoiceIGSTAmt.setText(getIntent().getExtras().getString("shortBillGstAmt"));
                    } else {

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                    });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadAndSetCompanyLogo(){
        if(imageURL!=null) {
            pdfBinding.shopImage.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(imageURL)
                    .resize(100, 100)
                    .into(pdfBinding.shopImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError(Exception e) {
                        }
                    });
        }
        else{
            pdfBinding.shopImage.setVisibility(View.GONE);
        }

    }
    protected void sharePdf(String Url) {
        Intent i=new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Bill Share");
        i.putExtra(android.content.Intent.EXTRA_TEXT, Url);
        startActivity(Intent.createChooser(i,"Share via"));

    }


//    private void loadAndSetCompanyLogo(){
//        if(imageURL!=null) {
//            pdfBinding.shopImage.setVisibility(View.VISIBLE);
//            Picasso.get()
//                    .load(imageURL)
//                    .resize(100, 100)
//                    .into(pdfBinding.shopImage, new com.squareup.picasso.Callback() {
//                        @Override
//                        public void onSuccess() {
//                            loadAndSetSignatureImage();
//                        }
//                        @Override
//                        public void onError(Exception e) {
//                            loadAndSetSignatureImage();
//                        }
//                    });
//        }
//        else{
//            pdfBinding.shopImage.setVisibility(View.GONE);
//            loadAndSetSignatureImage();
//        }
//
//    }
    private void loadAndSetSignatureImage() {

        if (signatureURL != null) {
            invoiceAmountLayoutUpdatedBinding.tvSignature.setVisibility(View.GONE);
            invoiceAmountLayoutUpdatedBinding.ivSignature.setVisibility(View.VISIBLE);
            invoiceAmountLayoutUpdatedBinding.tvSignatureIfImage.setVisibility(View.VISIBLE);
//                signatureText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            Picasso.get()
                    .load(signatureURL)
                    .resize(250, 100)
                    .into(invoiceAmountLayoutUpdatedBinding.ivSignature, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
//                            try {
//                                if (filepath == null) {
//                                    createPdfWrapper();
//                                }
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            }
                        }

                        @Override
                        public void onError(Exception e) {
//                            try {
//                                if (filepath == null){
//                                    createPdfWrapper();
//                                }
//                            } catch (FileNotFoundException error) {
//                                error.printStackTrace();
//                            }
                        }
                    });
        }
//        else{
//            try {
//                if (filepath == null){
//                    createPdfWrapper();
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void createPdfWrapper() throws FileNotFoundException {

        int hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            createPdf();
        }
    }

    private void createPdf() throws FileNotFoundException {
        try{

            String extStorageState = Environment.getExternalStorageState();

            File docsFolder = new File(getExternalCacheDir() + "/Documents");
            if (!docsFolder.exists()) {
                docsFolder.mkdir();
                Log.i(TAG, "Created a new directory for PDF");
            }

            Date date = Calendar.getInstance().getTime();

            // Display a date in day, month, year format
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String today = formatter.format(date);

            PdfWriter pdfWriter =
                    new PdfWriter(PDFActivity.this, (ViewGroup) findViewById(R.id.ll_root));
            filepath = docsFolder.getAbsolutePath();

                filepath = filepath + "/" + "Invoice"+ invoiceNumber + "_" + today + ".pdf";

            pdfFile = pdfWriter.exportPDF(filepath);
            if (invID > 0)
                uploadPDF();
            else
                saveInvoiceOffline();

            //        openPDF();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
//                   DialogUtils.showToast(this, "WRITE_EXTERNAL Permission Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPrintBill:
                Util.postEvents("Print", "Print", this.getApplicationContext());
                if(longBillPrint){
                    openPDF();
                } else if (shortBillPrint){
//                    thermalPrinter();
                }
                break;
            case R.id.btnShare:
                Util.postEvents("Share of Whatsapp", "Share of Whatsapp", this.getApplicationContext());
                shareOnWhatsApp();
                break;
            case R.id.btnHome:
                Util.postEvents("Close", "Close", this.getApplicationContext());
                Log.v("Home", "Clicked");
                Intent intent = new Intent(this, BottomNavigationActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.btn_Long_pdf:
                shortBillLayoutBinding.shortBill.setVisibility(View.GONE);
                binding.hsv.setVisibility(View.VISIBLE);
                binding.btnLongPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.login_button_structure));
                binding.btnLongPdf.setTextColor(ContextCompat.getColor(this,R.color.white));
                binding.btnShortPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.pdf_format_structure));
                binding.btnShortPdf.setTextColor(ContextCompat.getColor(this,R.color.black));
                longBillPrint = true;
                if(shortBillPrint){
                    shortBillPrint = false;
                }
                break;
            case R.id.btn_Short_pdf:
                binding.hsv.setVisibility(View.GONE);
                shortBillLayoutBinding.shortBill.setVisibility(View.VISIBLE);
                binding.btnShortPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.login_button_structure));
                binding.btnShortPdf.setTextColor(ContextCompat.getColor(this,R.color.white));
                binding.btnLongPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.pdf_format_structure));
                binding.btnLongPdf.setTextColor(ContextCompat.getColor(this,R.color.black));
                shortBillPrint = true;
                if(longBillPrint){
                    longBillPrint = false;
                }
                break;

        }
    }

    public void fetchCutlyLinkfromApi(){
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Call<Object> call = null;
        call = apiService.getCutlyUrl(headerMap,invID);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                try{
                    if(response.code()==200){
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        JSONObject data = body.getJSONObject("data");

                        if(data.has("pdfLink")&&data.getString("pdfLink")!=null){
                            createWhatsppsmsToShare(data.getString("pdfLink"));
                        } else{
                            createWhatsppsmsToShare(invoice.getString("pdfLink"));
                        }
                    }
                    else{ createWhatsppsmsToShare(invoice.getString("pdfLink")); }

                }
                catch(Exception e)
                { e.printStackTrace(); }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable throwable) {
                DialogUtils.showToast(PDFActivity.this, "Please Check Your Internet Connection");
            }
        });
    }

    private void shareOnWhatsApp() {
        try {
            if (invoice.has("pdfLink") && invoice.getString("pdfLink") != null && invID > 0) {
                fetchCutlyLinkfromApi();
            } else if (invID < 0) {
                Util.sendWhatsAppMessageasPDF(invoice.getString("customerMobileNo"), this, pdfFile);
            } else {
                DialogUtils.showToast(this, "customer mobile number is not entered while creating bill");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createWhatsppsmsToShare(String url){
        try{
            String smsBody = "Dear user"
                    + ", Your total payable amount is "
                    + invoice.getDouble("totalAmount")
                    + " and your invoice is at " + url;

            //Util.sendWhatsAppMessage(invoice.getString("customerMobileNo"), this, smsBody);
            sharePdf(smsBody);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void openPDF() {
        try{
            int hasWriteStoragePermission =
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
                DialogUtils.showToast(this, "Storage permission not given");
            } else {
                Uri uri = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void uploadPDF() {
//        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
//        headerMap.put("Content-Type", "application/json");
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", pdfFile.getName(), RequestBody.create(MediaType.parse("*/*"), pdfFile));

        Call<Object> call = apiService.updateInvoicePdf(headerMap, invID, filePart);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
//                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP", body.toString());
                    if (body.getBoolean("status")) {
                        invoice.put("pdfLink", body.getJSONObject("data").getJSONObject("invoice").getString("pdfLink"));
                    } else {
                        DialogUtils.showToast(PDFActivity.this, "Failed upload pdf to server");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
//                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(PDFActivity.this, "Failed upload pdf to server");
            }
        });
    }

//    private void thermalPrinter(){
//        try {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PDFActivity.PERMISSION_BLUETOOTH);
//            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PDFActivity.PERMISSION_BLUETOOTH_ADMIN);
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PDFActivity.PERMISSION_BLUETOOTH_CONNECT);
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PDFActivity.PERMISSION_BLUETOOTH_SCAN);
//            } else {
//                if(getIntent().getExtras().getInt("gstBillNo") != 0){
//                    EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
//                    printer
//                            .printFormattedText(
//                                    "[L]\n" +
//                                            "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[L]\n" +
//                                            "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
//                                            "[L]  + Size : S\n" +
//                                            "[L]\n" +
//                                            "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
//                                            "[L]  + Size : 57/58\n" +
//                                            "[L]\n" +
//                                            "[C]--------------------------------\n" +
//                                            "[R]TOTAL PRICE :[R]34.98e\n" +
//                                            "[R]TAX :[R]4.23e\n" +
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[L]\n" +
//                                            "[L]<font size='tall'>Customer :</font>\n" +
//                                            "[L]Raymond DUPONT\n" +
//                                            "[L]5 rue des girafes\n" +
//                                            "[L]31547 PERPETES\n" +
//                                            "[L]Tel : +33801201456\n" +
//                                            "[L]\n" +
//                                            "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//                                            "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
//                            );
//                } else{
//                    EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
//                    printer
//                            .printFormattedText(
//                                    "[L]\n" +
//                                            "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[L]\n" +
//                                            "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
//                                            "[L]  + Size : S\n" +
//                                            "[L]\n" +
//                                            "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
//                                            "[L]  + Size : 57/58\n" +
//                                            "[L]\n" +
//                                            "[C]--------------------------------\n" +
//                                            "[R]TOTAL PRICE :[R]34.98e\n" +
//                                            "[R]TAX :[R]4.23e\n" +
//                                            "[L]\n" +
//                                            "[C]================================\n" +
//                                            "[L]\n" +
//                                            "[L]<font size='tall'>Customer :</font>\n" +
//                                            "[L]Raymond DUPONT\n" +
//                                            "[L]5 rue des girafes\n" +
//                                            "[L]31547 PERPETES\n" +
//                                            "[L]Tel : +33801201456\n" +
//                                            "[L]\n" +
//                                            "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
//                                            "[C]<qrcode size='20'>http://www.developpeur-web.dantsu.com/</qrcode>"
//                            );
//                }
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }

    //once syncing starts from database (see SyncService.java class line: 120) , after that there will be no use of this function
    private void saveInvoiceOffline() {
        try {
//            invoiceViewModel.updatePdfPath(localInvoiceId,filepath);
//            JSONArray invoices = new JSONArray();
//            if (!MyApplication.getUnSyncedInvoice().isEmpty()) {
//                invoices = new JSONArray(MyApplication.getUnSyncedInvoice());
//            }
//            invoice.put("pdfLink", "");
//            invoices.put(invoice);
//            MyApplication.saveUnSyncedInvoices(invoices.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class getCurrentItemsAsyncTask extends AsyncTask<Void, Void, List<InvoiceItems>> {
        private InvoiceItemDao invoiceItemDao;
        private long invoiceId;
        private List<InvoiceItems> curItems;
        private Context context;
        private boolean isGSTAvailable;
        private RecyclerView recyclerViewInvoiceProducts;
        private String GSTType;

        private getCurrentItemsAsyncTask(InvoiceItemDao invoiceItemDao, long invoiceId, Context context, boolean isGSTAvailable, RecyclerView recyclerViewInvoiceProducts, String GSTType) {
            this.invoiceItemDao = invoiceItemDao;
            this.invoiceId = invoiceId;
            this.context = context;
            this.isGSTAvailable = isGSTAvailable;
            this.recyclerViewInvoiceProducts = recyclerViewInvoiceProducts;
            this.GSTType = GSTType;
        }

        @Override
        protected List<InvoiceItems> doInBackground(Void... voids) {
            curItems = invoiceItemDao.getCurrentItems(invoiceId);
            return curItems;
        }

        @Override
        protected void onPostExecute(List<InvoiceItems> invoiceItems) {
            super.onPostExecute(invoiceItems);
            setDataAfterInvoiceItems(invoiceItems, context, isGSTAvailable, recyclerViewInvoiceProducts, GSTType);
            try {
                loadAndSetCompanyLogo();
                loadAndSetSignatureImage();
                createPdfWrapper();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class getCurrentItemsAsyncTaskShort extends AsyncTask<Void, Void, List<InvoiceItems>> {
        private InvoiceItemDao invoiceItemDao;
        private long invoiceId;
        private List<InvoiceItems> curItems;
        private Context context;
        private boolean isGSTAvailable;
        private RecyclerView recyclerViewShortBillInvoiceProducts;
        private String GSTType;

        private getCurrentItemsAsyncTaskShort(InvoiceItemDao invoiceItemDao, long invoiceId, Context context, boolean isGSTAvailable, RecyclerView recyclerViewShortBillInvoiceProducts, String GSTType) {
            this.invoiceItemDao = invoiceItemDao;
            this.invoiceId = invoiceId;
            this.context = context;
            this.isGSTAvailable = isGSTAvailable;
            this.recyclerViewShortBillInvoiceProducts = recyclerViewShortBillInvoiceProducts;
            this.GSTType = GSTType;
        }

        @Override
        protected List<InvoiceItems> doInBackground(Void... voids) {
            curItems = invoiceItemDao.getCurrentItems(invoiceId);
            return curItems;
        }

        @Override
        protected void onPostExecute(List<InvoiceItems> invoiceItems) {
            super.onPostExecute(invoiceItems);
            setDataAfterInvoiceItemsShort(invoiceItems, context, isGSTAvailable, recyclerViewShortBillInvoiceProducts, GSTType);
            try {
                loadAndSetCompanyLogo();
                loadAndSetSignatureImage();
                createPdfWrapper();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



}
