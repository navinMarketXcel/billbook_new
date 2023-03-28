package com.billbook.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.adapters.NewInvoiceShortBillInvoiceProductAdapter;
import com.billbook.app.database.daos.InvoiceItemDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.databinding.ActivityPdfBinding;
import com.billbook.app.databinding.InvoiceAmountLayoutUpdatedBinding;
import com.billbook.app.databinding.PdfContentNewBinding;
import com.billbook.app.databinding.PdfContentOldBinding;
import com.billbook.app.databinding.ShortBillItemLayoutBinding;
import com.billbook.app.databinding.ShortBillLayoutBinding;
import com.billbook.app.viewmodel.InvoiceItemsViewModel;
import com.billbook.app.viewmodel.InvoiceViewModel;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
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
    private List<InvoiceItems> curItemsShortUI=null;
    private InvoiceItemsViewModel invoiceItemViewModel;
    private InvoiceViewModel invoiceViewModel;
    private Gson gson = new Gson();
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    private String filepath = null;
    private String filepath1 = null;
    private File pdfFile,pdfFile1;
    private static final String TAG = "PDFActivity";
    private JSONObject profile;
    private boolean isGSTAvailable;
    private int invID = -1;
    private HashMap<String,Object> printerType;
    private long localInvoiceId=0;
    private int invoiceNumber;
    private String GSTType = "";
    private BluetoothAdapter bluetoothAdapter;
    private String imageURL,signatureURL;
    private String layout = "";
    private String layoutThreeInch = "";
    private ActivityPdfBinding binding;
    private PdfContentNewBinding pdfBinding;
    //private PdfContentOldBinding pdfContentOldBinding;
    private ShortBillItemLayoutBinding shortBillItemLayoutBinding;
    private ShortBillLayoutBinding  shortBillLayoutBinding;

    private InvoiceAmountLayoutUpdatedBinding invoiceAmountLayoutUpdatedBinding;
    private String vendorName = "MArcn Technology", vendorAddress = "Shop 10,82/86,Abdul Rehman Street", vendorLocation = "MUMBAI-400003(Maharastra)", billType = "short";
    private boolean hasCompanyLogo = false, hasSignatureLogo = false, shortBillPrint = false, longBillPrint = false;
    private boolean loadedCompanyLogo = false, loadedSignatureLogo = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfBinding.inflate(getLayoutInflater());
       // pdfContentOldBinding = binding.includedLayoutPdfContentOld;
        pdfBinding = binding.includedLayoutPdfContent;
        shortBillLayoutBinding = binding.includedLayoutShortBill;
        invoiceAmountLayoutUpdatedBinding = pdfBinding.invoiceAmountLayoutUpdated;
        setContentView(binding.getRoot());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //setContentView(R.layout.activity_pdf);
        invoiceViewModel = ViewModelProviders.of(this).get(InvoiceViewModel.class);
        invoiceItemViewModel = ViewModelProviders.of(this).get(InvoiceItemsViewModel.class);
        binding.btnLongPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.login_button_structure));
        binding.btnLongPdf.setTextColor(ContextCompat.getColor(this,R.color.white));
        shortBillPrint = true;
        initUI();
        setProfileData();
        setData();
        setShortFormatData();
        setonClick();
    }
    public void setonClick(){
        binding.ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
        binding.lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(PDFActivity.this);
        });
        binding.lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(PDFActivity.this);
        });


    }

    private void initUI() {
        recyclerViewInvoiceProducts = pdfBinding.recyclerViewInvoiceProducts;
        recyclerViewShortBillInvoiceProducts = shortBillLayoutBinding.recyclerViewShortBillInvoiceProducts;
        binding.btnPrintBill.setOnClickListener(this);
        binding.btnHome.setOnClickListener(this);
        binding.btnShare.setOnClickListener(this);
        binding.btnWhatsappShare.setOnClickListener(this);
        binding.btnLongPdf.setOnClickListener(this);
        binding.btnShortPdf.setOnClickListener(this);
        pdfBinding.zoomClick.setOnClickListener(this);
       // binding.btnLong2Pdf.setOnClickListener(this);
        //pdfContentOldBinding.zoomClickOld.setOnClickListener(this);

    }

    public void setDataAfterInvoiceItems(List<InvoiceItems> invoiceItems,Context context,boolean isGSTAvailable, RecyclerView recyclerViewInvoiceProducts, String GSTType){
        System.out.println("Invoices item:"+invoiceItems.size());
        float taxSlab5=0,taxSlab3=0,taxSlab12=0,taxSlab18=0,taxSlab28=0;
        if(isGSTAvailable)
        {
            for(int i=0;i<invoiceItems.size();i++)
            {

                if(invoiceItems.get(i).getGst()==3)
                    taxSlab3=(taxSlab3+invoiceItems.get(i).getTotalAmount())-invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==5)
                    taxSlab5=(taxSlab5+invoiceItems.get(i).getTotalAmount())-invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==12)
                    taxSlab12=(taxSlab12+invoiceItems.get(i).getTotalAmount())-invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==18)
                    taxSlab18=taxSlab18+invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==28)
                    taxSlab28=taxSlab28+invoiceItems.get(i).getGstAmount();

            }

            Log.v("tax slabs",taxSlab3+" "+taxSlab5+" "+taxSlab12+" "+taxSlab18+" "+taxSlab28);
        }
        if(taxSlab3>0)
        {
            pdfBinding.totalCGST15.setText(""+Util.formatDecimalValue(taxSlab3/2));
            pdfBinding.totalSGST15.setText(""+Util.formatDecimalValue(taxSlab3/2));
            pdfBinding.invoiceIGST3.setText("IGST@ 3%");
            pdfBinding.invoiceIGSTAmt3.setText(Util.formatDecimalValue(taxSlab3));



        }
        else {
            pdfBinding.cgstLL15.setVisibility(View.GONE);
            pdfBinding.sgstLL15.setVisibility(View.GONE);
            pdfBinding.igst3ll.setVisibility(View.GONE);
        }
        if(taxSlab5>0)
        {
            pdfBinding.totalCGST25.setText(""+Util.formatDecimalValue(taxSlab5/2));
            pdfBinding.totalSGST25.setText(""+Util.formatDecimalValue(taxSlab5/2));
            pdfBinding.invoiceIGST5.setText("IGST@ 5%");
            pdfBinding.invoiceIGSTAmt5.setText(Util.formatDecimalValue(taxSlab5));
        }
        else {
            pdfBinding.cgstLL25.setVisibility(View.GONE);
            pdfBinding.sgstLL25.setVisibility(View.GONE);
            pdfBinding.igst5ll.setVisibility(View.GONE);
        }
        if(taxSlab12>0)
        {
            pdfBinding.totalCGST60.setText(""+Util.formatDecimalValue(taxSlab12/2));
            pdfBinding.totalSGST60.setText(""+Util.formatDecimalValue(taxSlab12/2));
            pdfBinding.invoiceIGST12.setText("IGST@ 12%");
            pdfBinding.invoiceIGSTAmt12.setText(Util.formatDecimalValue(taxSlab12));
        }
        else {
            pdfBinding.cgstLL60.setVisibility(View.GONE);
            pdfBinding.sgstLL60.setVisibility(View.GONE);
            pdfBinding.igst12ll.setVisibility(View.GONE);
        }
        if(taxSlab18>0)
        {
            pdfBinding.totalCGST90.setText(""+Util.formatDecimalValue(taxSlab18/2));
            pdfBinding.totalSGST90.setText(""+Util.formatDecimalValue(taxSlab18/2));
            pdfBinding.invoiceIGST18.setText("IGST@ 18%");
            pdfBinding.invoiceIGSTAmt18.setText(Util.formatDecimalValue(taxSlab18));
        }
        else {
            pdfBinding.cgstLL90.setVisibility(View.GONE);
            pdfBinding.sgstLL90.setVisibility(View.GONE);
            pdfBinding.igst18ll.setVisibility(View.GONE);
        }
        if(taxSlab28>0)
        {
            pdfBinding.totalCGST.setText(""+Util.formatDecimalValue(taxSlab28/2));
            pdfBinding.totalSGST.setText(""+Util.formatDecimalValue(taxSlab28/2));
            pdfBinding.invoiceIGST28.setText("IGST@ 28%");
            pdfBinding.invoiceIGSTAmt28.setText(Util.formatDecimalValue(taxSlab28));
        }
        else {
            pdfBinding.cgstLL14.setVisibility(View.GONE);
            pdfBinding.sgstLL14.setVisibility(View.GONE);
            pdfBinding.igst28ll.setVisibility(View.GONE);

        }
        Log.v("tax slabs 2",taxSlab3+" "+taxSlab5+" "+taxSlab12+" "+taxSlab18+" "+taxSlab28);



        NewInvoicePurchaseAdapter newInvoicePurchaseAdapter = new NewInvoicePurchaseAdapter(context, invoiceItems, isGSTAvailable,GSTType);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        recyclerViewInvoiceProducts.setLayoutManager(mLayoutManager);
        recyclerViewInvoiceProducts.setAdapter(newInvoicePurchaseAdapter);
    }

    public void setDataAfterInvoiceItemsShort(List<InvoiceItems> invoiceItems,Context context,boolean isGSTAvailable, RecyclerView recyclerViewShortBillInvoiceProducts, String GSTType){
        System.out.println("Invoices item:"+invoiceItems.size());
        float taxSlab5=0,taxSlab3=0,taxSlab12=0,taxSlab18=0,taxSlab28=0;
        if(isGSTAvailable)
        {
            for(int i=0;i<invoiceItems.size();i++)
            {

                if(invoiceItems.get(i).getGst()==3)
                    taxSlab3=(taxSlab3+invoiceItems.get(i).getTotalAmount())-invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==5)
                    taxSlab5=(taxSlab5+invoiceItems.get(i).getTotalAmount())-invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==12)
                    taxSlab12=(taxSlab12+invoiceItems.get(i).getTotalAmount())-invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==18)
                    taxSlab18=taxSlab18+invoiceItems.get(i).getGstAmount();
                else if (invoiceItems.get(i).getGst()==28)
                    taxSlab28=taxSlab28+invoiceItems.get(i).getGstAmount();

            }

            Log.v("tax slabs",taxSlab3+" "+taxSlab5+" "+taxSlab12+" "+taxSlab18+" "+taxSlab28);
        }
        if(taxSlab3>0)
        {
            shortBillLayoutBinding.totalCGST15.setText(""+Util.formatDecimalValue(taxSlab3/2));
            shortBillLayoutBinding.totalSGST15.setText(""+Util.formatDecimalValue(taxSlab3/2));
            shortBillLayoutBinding.invoiceIGST3.setText("IGST@ 3%");
            shortBillLayoutBinding.invoiceIGSTAmt3.setText(Util.formatDecimalValue(taxSlab3));
        }
        else {
            shortBillLayoutBinding.cgstLL15.setVisibility(View.GONE);
            shortBillLayoutBinding.sgstLL15.setVisibility(View.GONE);
            shortBillLayoutBinding.igst3ll.setVisibility(View.GONE);
        }
        if(taxSlab5>0)
        {
            shortBillLayoutBinding.totalCGST25.setText(""+Util.formatDecimalValue(taxSlab5/2));
            shortBillLayoutBinding.totalSGST25.setText(""+Util.formatDecimalValue(taxSlab5/2));
            shortBillLayoutBinding.invoiceIGST5.setText("IGST@ 5%");
            shortBillLayoutBinding.invoiceIGSTAmt5.setText(Util.formatDecimalValue(taxSlab5));
        }
        else {
            shortBillLayoutBinding.cgstLL25.setVisibility(View.GONE);
            shortBillLayoutBinding.sgstLL25.setVisibility(View.GONE);
            shortBillLayoutBinding.igst5ll.setVisibility(View.GONE);

        }
        if(taxSlab12>0)
        {
            shortBillLayoutBinding.totalCGST60.setText(""+Util.formatDecimalValue(taxSlab12/2));
            shortBillLayoutBinding.totalSGST60.setText(""+Util.formatDecimalValue(taxSlab12/2));
            shortBillLayoutBinding.invoiceIGST12.setText("IGST@ 12%");
            shortBillLayoutBinding.invoiceIGSTAmt12.setText(Util.formatDecimalValue(taxSlab12));
        }
        else {
            shortBillLayoutBinding.cgstLL60.setVisibility(View.GONE);
            shortBillLayoutBinding.sgstLL60.setVisibility(View.GONE);
            shortBillLayoutBinding.igst12ll.setVisibility(View.GONE);

        }
        if(taxSlab18>0)
        {
            shortBillLayoutBinding.totalCGST90.setText(""+Util.formatDecimalValue(taxSlab18/2));
            shortBillLayoutBinding.totalSGST90.setText(""+Util.formatDecimalValue(taxSlab18/2));
            shortBillLayoutBinding.invoiceIGST18.setText("IGST@ 18%");
            shortBillLayoutBinding.invoiceIGSTAmt18.setText(Util.formatDecimalValue(taxSlab18));
        }
        else {
            shortBillLayoutBinding.cgstLL90.setVisibility(View.GONE);
            shortBillLayoutBinding.sgstLL90.setVisibility(View.GONE);
            shortBillLayoutBinding.igst18ll.setVisibility(View.GONE);

        }
        if(taxSlab28>0)
        {
            shortBillLayoutBinding.totalCGST.setText(""+Util.formatDecimalValue(taxSlab28/2));
            shortBillLayoutBinding.totalSGST.setText(""+Util.formatDecimalValue(taxSlab28/2));
            shortBillLayoutBinding.invoiceIGST28.setText("IGST@ 28%");
            shortBillLayoutBinding.invoiceIGSTAmt28.setText(Util.formatDecimalValue(taxSlab28));
        }
        else {
            shortBillLayoutBinding.cgstLL14.setVisibility(View.GONE);
            shortBillLayoutBinding.sgstLL14.setVisibility(View.GONE);
            shortBillLayoutBinding.igst28ll.setVisibility(View.GONE);

        }
        Log.v("tax slabs 2",taxSlab3+" "+taxSlab5+" "+taxSlab12+" "+taxSlab18+" "+taxSlab28);
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
                pdfBinding.tvAdditionalDetails.setVisibility(View.VISIBLE);
                pdfBinding.tvAdditionalDetails.setText(profile.getString("additionalData"));
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
                        TextView taxorBillTv = findViewById(R.id.taxorBillTv);
                        taxorBillTv.setText("Tax Invoice");
                        pdfBinding.gstTotalLayout.setVisibility(View.VISIBLE);
                        pdfBinding.totalAmountBeforeTaxLayout.setVisibility(View.VISIBLE);
                        invoiceNumber = getIntent().getExtras().getInt("gstBillNo");

                    } else {
                        isGSTAvailable = false;
                        pdfBinding.tvGSTNo.setVisibility(View.GONE);
                        pdfBinding.gstTotalLayout.setVisibility(View.GONE);
                        pdfBinding.totalAmountBeforeTaxLayout.setVisibility(View.VISIBLE);
                        invoiceNumber = getIntent().getExtras().getInt("nonGstBillNo");
                    }

                    pdfBinding.tvVendorName.setText(profile.getString("shopName"));
                    pdfBinding.tvStoreAddress.setText(profile.getString("shopAddr") + " " + profile.getString("city")
                            + " " + profile.getString("state") + " - " + profile.getString("pincode"));
                    pdfBinding.tvGSTNo.setText(profile.has("gstNo") ? ("GSTIN: "+profile.getString("gstNo")) : "");
                    pdfBinding.mobileNoRetailer.setText("Mobile No- " + profile.getString("mobileNo"));
                    invID = getIntent().getExtras().getInt("id");

//                        Log.i(TAG, "setData: GST => " + isGSTAvailable);
                    pdfBinding.tvPreTax.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
                    //pdfBinding.llForHeader.setWeightSum(isGSTAvailable ? (float) 10.5 : 9);pdfBinding.footer.setWeightSum(isGSTAvailable ? (float) 10.5 : 9);
                    // pdfBinding.paddingLabelGst.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);

                    if (invoice.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailable) {
                        GSTType = "CGST/SGST (Local customer)";

                        //pdfBinding.llForHeader.setWeightSum(12f);
                        pdfBinding.IGST.setVisibility(View.GONE);
                        pdfBinding.GSTField4Layout.setVisibility(View.GONE);
                        pdfBinding.tvBeforeDiscountHeader.setVisibility(View.VISIBLE);
                        pdfBinding.tvBeforeDiscount.setVisibility(View.VISIBLE);
                        pdfBinding.CGST.setVisibility(View.VISIBLE);
                        pdfBinding.SGST.setVisibility(View.VISIBLE);
                        pdfBinding.tvPreTax.setVisibility(View.VISIBLE);
                       // Log.v("taxslab5", String.valueOf(PDFActivity.taxSlab5));
                        checkTaxSlabs();

                        //pdfBinding.padding3.setVisibility(View.GONE);
                       /*pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.5f));*/
                       /* pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 3.5f));*/
                    } else if (invoice.getString("gstType").equals("IGST (Central/outstation customer)") && isGSTAvailable) {
                        GSTType = "IGST (Central/outstation customer)";
                        //pdfBinding.llForHeader.setWeightSum(10f);
                        pdfBinding.IGST.setVisibility(View.VISIBLE);
                        pdfBinding.GSTField4Layout.setVisibility(View.VISIBLE);
                        pdfBinding.sgstcsgtLL.setVisibility(View.GONE);
                        //pdfBinding.sgstLL.setVisibility(View.GONE);

                        // pdfBinding.padding1.setVisibility(View.GONE);
                        // pdfBinding.padding2.setVisibility(View.GONE);
                        pdfBinding.CGST.setVisibility(View.GONE);
                        pdfBinding.SGST.setVisibility(View.GONE);
                        pdfBinding.tvBeforeDiscountHeader.setVisibility(View.VISIBLE);
                        pdfBinding.tvBeforeDiscount.setVisibility(View.VISIBLE);
                        pdfBinding.llForHeader.setWeightSum(10.0f);

                       /*pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 4.5f));*/
                       /* pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f));*/
                    } else {
                        if(!isGSTAvailable)
                        {
                            // LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,5.0f);
                            // LinearLayout.LayoutParams prodLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1.05f);
                            //LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,2.09f);
                            //LinearLayout.LayoutParams qtyLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1.04f);
                            //LinearLayout.LayoutParams rateLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1.049f);

                            //buttonLayoutParams.setMargins(10, 10, 10, 10);

                            //pdfBinding.amtBill.setLayoutParams(buttonLayoutParams);
                            // pdfBinding.productNumber.setLayoutParams(prodLayoutParams);
                            // pdfBinding.productLabel.setLayoutParams(itemLayoutParams);
                            // pdfBinding.qtyBill.setLayoutParams(qtyLayoutParams);
                            // pdfBinding.rateBill.setLayoutParams(rateLayoutParams);
//                            TextView tv = findViewById(R.id.tvAmount);
//                            tv.setLayoutParams(buttonLayoutParams);
                        }
                        pdfBinding.IGST.setVisibility(View.GONE);
                        pdfBinding.CGST.setVisibility(View.GONE);
                        pdfBinding.SGST.setVisibility(View.GONE);
                        pdfBinding.HsnNoOnBillHeader.setVisibility(View.GONE);
                        pdfBinding.llForHeader.setWeightSum(7.0f);

                        //pdfBinding.padding1.setVisibility(View.GONE);
                        // pdfBinding.padding2.setVisibility(View.GONE);
                        // pdfBinding.padding3.setVisibility(View.GONE);
                       /* pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.5f));*/
                       /* pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));*/
                    }
                    if (invoice.getString("GSTNo").isEmpty()) {
                        //pdfBinding.custGstLayout.setVisibility(View.INVISIBLE);
                    } else {
                        // pdfBinding.custGstLayout.setVisibility(View.VISIBLE);
                        // pdfBinding.customerGst.setText(invoice.getString("GSTNo"));
                    }

//                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
//                    DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    String inputDateStr=invoice.getString("invoiceDate");
//                    Date date = inputFormat.parse(inputDateStr);
//                    String outputDateStr = outputFormat.format(date);
                    pdfBinding.txtInvoiceDate.setText(Util.formatDate(invoice.getString("invoiceDate")));
                    pdfBinding.txtInvoiceNo.setText("" + invoiceNumber);
                    float gst = 0;

//                    pdfBinding.totalCGST.setText(Util.formatDecimalValue(gst/2));
//                    pdfBinding.totalSGST.setText(Util.formatDecimalValue(gst/2));



                    Log.v("CustomerName", String.valueOf(invoice));
                    String custName= invoice.getString("customerName");
                    String custNo =invoice.getString("customerMobileNo");
                    String custAdd =invoice.getString("customerAddress");
                    String GSTNo =invoice.getString("GSTNo");


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
                   /* if(custName == null)pdfBinding.customerName.setText("Customer Name - N/A ");else pdfBinding.customerName.setText("Customer Name - "+custName);
                    if(custAdd == null)pdfBinding.customerAdd.setText("Customer Add - N/A ");else pdfBinding.customerAdd.setText("Customer Add - "+custAdd);
                    if(custNo == null)pdfBinding.cusMobNo.setText("Customer Mobile - N/A ");else pdfBinding.cusMobNo.setText("Customer Mobile - "+custNo);
                    if(GSTNo == null)pdfBinding.cusGst.setText("Customer GST - N/A ");else pdfBinding.cusGst.setText("Customer GST - "+GSTNo);
*/
                    pdfBinding.customerName.setText(custName);
                    pdfBinding.customerAdd.setText(custAdd);
                    pdfBinding.cusMobNo.setText(custNo);
                    if(GSTNo.isEmpty())
                    {
                        pdfBinding.cusGst.setVisibility(View.GONE);
                        pdfBinding.cusGst.setVisibility(View.GONE);
                        shortBillLayoutBinding.tvgstIn.setVisibility(View.GONE);
                        shortBillLayoutBinding.edtGSTIN.setVisibility(View.GONE);
                    }
                    else {
                        pdfBinding.cusGst.setText(GSTNo);
                        shortBillLayoutBinding.edtGSTIN.setText(GSTNo);

                    }



                    pdfBinding.edtName.setText(custName == null ?" ":custName+" ");
                    pdfBinding.edtAddress.setText(custAdd == null ?" ":custAdd+" ");
                    //pdfBinding.edtMobNo.setText(custNo == null ?" ":custNo+" ");
//            tvGSTNo.setText(invoice.getString("GSTNo")+" ");
                    pdfBinding.tvAmountBeforeTax.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmountBeforeGST")));
                    Log.v("totalAmountBeforeGST", String.valueOf((float) invoice.getDouble("totalAmountBeforeGST")));
                    pdfBinding.tvTotal.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmount")));
                    float totalAfterDiscount = 0, totalAmount = 0;
                    totalAmount = Float.parseFloat(String.valueOf(invoice.getDouble("totalAmount")));
                    if (invoice.has("totalAfterDiscount")) {
                        totalAfterDiscount = (float) invoice.getDouble("totalAfterDiscount");
                    } else {
                        totalAfterDiscount = totalAmount;
                    }
                    pdfBinding.tvBeforeDiscount.setText(Util.formatDecimalValue(totalAmount));
                    shortBillLayoutBinding.tvBeforeDiscountSf.setText(Util.formatDecimalValue(totalAmount));

                    pdfBinding.tvTotal.setText(Util.formatDecimalValue(totalAfterDiscount));
                    pdfBinding.tvDiscount.setText("- "+Util.formatDecimalValue(totalAmount - totalAfterDiscount));
                    shortBillLayoutBinding.invoiceTotalDiscount.setText("- "+Util.formatDecimalValue(totalAmount - totalAfterDiscount));
                    new getCurrentItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao(), getIntent().getExtras().getLong("idForItem"), PDFActivity.this, isGSTAvailable, recyclerViewInvoiceProducts, GSTType).execute();


//                        items = gson.fromJson(invoice.getJSONArray("items").toString(), new TypeToken<List<NewInvoiceModels>>() {
//                        }.getType());

                    if (isGSTAvailable)
                    {
//                        pdfBinding.CGSTTitle.setText("GST " + (invoice.getString("gstType").equals("CGST/SGST (Local customer)") ? "(SGST/CGST)" : "(IGST)"));
//                        pdfBinding.SGSTTitle.setText("GST " + (invoice.getString("gstType").equals("CGST/SGST (Local customer)") ? "(SGST/CGST)" : "(IGST)"));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }


    }
    private void checkTaxSlabs()
    {

    }

    private  void setShortFormatData(){
        try{
            localInvoiceId = getIntent().getExtras().getLong("localInvId");
            invoiceViewModel.getCurrentInvoice(localInvoiceId).observe(this, invoiceModelV2 -> {
                try{
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
                        if(profile.has("gstNo")){
                            if(profile.getString("gstNo").isEmpty()){
                                shortBillLayoutBinding.tvGSTNo.setVisibility(View.GONE);
                            } else {
                                shortBillLayoutBinding.tvGSTNo.setText("GSTIN: "+profile.getString("gstNo"));
                            }
                        } else{
                            shortBillLayoutBinding.tvGSTNo.setVisibility(View.GONE);
                        }

                    } else {
                        shortBillLayoutBinding.viewSubtotal.setVisibility(View.GONE);
                        shortBillLayoutBinding.productTax.setVisibility(View.GONE);
                        shortBillLayoutBinding.tvGSTNo.setVisibility(View.GONE);
                        shortBillLayoutBinding.sgstcsgtLL.setVisibility(View.GONE);

                        shortBillLayoutBinding.GSTField4Layout.setVisibility(View.GONE);
                        shortBillLayoutBinding.llForHeaderTax.setVisibility(View.GONE);
                        shortBillLayoutBinding.llForHeader.setWeightSum(3.0f);

                        invoiceNumber = getIntent().getExtras().getInt("nonGstBillNo");
                        shortBillLayoutBinding.invoiceType.setText("***Invoice***");
                    }
                    Log.v("Invoi2", String.valueOf(invoice));
//                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
//                    DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
//                    String inputDateStr=invoice.getString("invoiceDate");
//                    Date date = inputFormat.parse(inputDateStr);
//                    String outputDateStr = outputFormat.format(date);
                    shortBillLayoutBinding.txtInvoiceDate.setText("Date: "+Util.formatDate(invoice.getString("invoiceDate")));
                    shortBillLayoutBinding.txtInvoiceNo.setText("" + invoiceNumber);
                    String custName= invoice.getString("customerName");
                    String custNo =invoice.getString("customerMobileNo");
                    String custAdd =invoice.getString("customerAddress");
                    Log.v("Invoi3", String.valueOf(invoice));
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
                    shortBillLayoutBinding.edtMobNo.setText(custNo == null ?" ":custNo);
                    shortBillLayoutBinding.edtName.setText(custName == null ?" ":custName);
                    Log.v("Invoi4", String.valueOf(invoice));
                    if(custAdd == null ||  custAdd.length() == 0){
                        shortBillLayoutBinding.edtAddressLayout.setVisibility(View.GONE);
                    }else {
                        shortBillLayoutBinding.edtAddress.setText(custAdd == null?" ":custAdd);
                    }
                    Log.v("Invoi5", String.valueOf(invoice));
                    shortBillLayoutBinding.invoiceItems.setText(getIntent().getExtras().getString("itemsSize"));
                    shortBillLayoutBinding.invoiceItemsQty.setText(getIntent().getExtras().getString("quantityCount"));
                    float totalAfterDiscount = 0, totalAmount = 0;
                    totalAmount = Float.parseFloat(String.valueOf(invoice.getDouble("totalAmount")));
                    if (invoice.has("totalAfterDiscount")) {
                        totalAfterDiscount = (float) invoice.getDouble("totalAfterDiscount");
                    } else {
                        totalAfterDiscount = totalAmount;
                    }
                    Log.v("Invoi6", String.valueOf(invoice));
                    shortBillLayoutBinding.invoiceNetAmnt.setText(Util.formatDecimalValue(totalAmount));
                    shortBillLayoutBinding.invoiceNetSubTotal.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmountBeforeGST")));
                    //shortBillLayoutBinding.invoiceNetAmnt.setText(Util.formatDecimalValue(totalAfterDiscount));
                    shortBillLayoutBinding.invoiceNetAmntTotal.setText(Util.formatDecimalValue(totalAfterDiscount));
                    shortBillLayoutBinding.invoiceTotalDiscount.setText(Util.formatDecimalValue(totalAmount - totalAfterDiscount));
                    new getCurrentItemsAsyncTaskShort(MyApplication.getDatabase().invoiceItemDao(), getIntent().getExtras().getLong("idForItem"), PDFActivity.this, isGSTAvailable, recyclerViewShortBillInvoiceProducts, GSTType).execute();
                    if (invoice.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailable) {
                        GSTType = "CGST/SGST (Local customer)";
                        shortBillLayoutBinding.totalDiscLL.setVisibility(View.VISIBLE);
                        shortBillLayoutBinding.GSTField4Layout.setVisibility(View.GONE);
                    } else if (invoice.getString("gstType").equals("IGST (Central/outstation customer)") && isGSTAvailable) {
                        GSTType = "IGST (Central/outstation customer)";
                        shortBillLayoutBinding.totalDiscLL.setVisibility(View.VISIBLE);
                        shortBillLayoutBinding.sgstcsgtLL.setVisibility(View.GONE);
//                        shortBillLayoutBinding.invoiceIGST.setText("IGST@"+(float)invoice.get(i));





                    } else {
                        shortBillLayoutBinding.totalDiscLL.setVisibility(View.GONE);


                    }
                    Log.v("Invoi7", String.valueOf(invoice));
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
//                signatureText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            Picasso.get()
                    .load(signatureURL)
                    .resize(250, 100)
                    .into(pdfBinding.ivSignature, new com.squareup.picasso.Callback() {
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
//            PdfWriter pdfWriter1 =
//                    new PdfWriter(PDFActivity.this, (ViewGroup) findViewById(R.id.ll_root_old));
//            filepath1 = docsFolder.getAbsolutePath();

            filepath = filepath + "/" + "Invoice"+ invoiceNumber + "_" + today + ".pdf";
           //filepath1 = filepath1 + "/" + "Invoice"+ invoiceNumber + "_" + today + ".pdf";
            System.out.println("filepath ::"+filepath);
          //  System.out.println("filepath ::"+filepath1);
            pdfFile = pdfWriter.exportPDF(filepath);
           // pdfFile1 = pdfWriter1.exportPDF(filepath1);
            if (invID > 0)
                uploadPDF();
            else
                saveInvoiceOffline();

//                    openPDF();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPrintBill:
                MyApplication.cleverTapAPI.pushEvent("Clicked on Print");
                Util.postEvents("Print", "Print", this.getApplicationContext());
                if(billType.equals("long")){
                    openPDF();
                } else{
                    TraditionalListDialog();
                }
                break;
            case R.id.btnShare:
                MyApplication.cleverTapAPI.pushEvent("Clicked on Share");
                Util.postEvents("Share of Whatsapp", "Share of Whatsapp", this.getApplicationContext());
                Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();
                shareOnWhatsApp(1);
//                try {
//                    sharePdf(invoice.getString("pdfLink"));
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
                break;
            case R.id.btnHome:
                MyApplication.cleverTapAPI.pushEvent("Clicked on Home");
                Util.postEvents("Close", "Close", this.getApplicationContext());
                Log.v("Home", "Clicked");
                Intent intent = new Intent(this, BottomNavigationActivity.class);
                startActivity(intent);
                this.finish();
                break;
            case R.id.btnWhatsappShare:
                Util.pushEvent("Clicked on Whatsapp Share");
                shareOnWhatsApp(2);
                break;
            case R.id.btn_Long_pdf:
                MyApplication.cleverTapAPI.pushEvent("Clicked on Long Format");
                shortBillLayoutBinding.shortBill.setVisibility(View.GONE);
                binding.pdfLayout.setVisibility(View.VISIBLE);
                //binding.pdfLayoutOld.setVisibility(View.GONE);
                binding.btnLongPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.login_button_structure));
                binding.btnLongPdf.setTextColor(ContextCompat.getColor(this,R.color.white));
                binding.btnShortPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.pdf_format_structure));
                binding.btnShortPdf.setTextColor(ContextCompat.getColor(this,R.color.black));
                Uri urii = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                if (!pdfBinding.pdfView.isRecycled()) {
                    pdfBinding.pdfView.recycle();
                }
                pdfBinding.pdfView.fromUri(urii).load();
                pdfBinding.pdfView.setOnTouchListener(new View.OnTouchListener() {
                    GestureDetector gd = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener(){
                        @Override
                        public boolean onDoubleTap(MotionEvent e){
                            Toast.makeText(PDFActivity.this,"Double tap",Toast.LENGTH_LONG).show();
                            return super.onDoubleTap(e);

                        }

                    });
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                });
                billType = "long";
                break;
//            case R.id.btn_Long_2_pdf:
//                MyApplication.cleverTapAPI.pushEvent("Clicked on Long Format-2");
//                shortBillLayoutBinding.shortBill.setVisibility(View.GONE);
//                binding.pdfLayout.setVisibility(View.GONE);
//                //binding.pdfLayoutOld.setVisibility(View.VISIBLE);
//                binding.btnLong2Pdf.setBackground(ContextCompat.getDrawable(this,R.drawable.login_button_structure));
//                binding.btnLong2Pdf.setTextColor(ContextCompat.getColor(this,R.color.white));
//                binding.btnShortPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.pdf_format_structure));
//                binding.btnShortPdf.setTextColor(ContextCompat.getColor(this,R.color.black));
//                binding.btnLongPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.pdf_format_structure));
//                binding.btnLongPdf.setTextColor(ContextCompat.getColor(this,R.color.black));
//                Uri urii1 = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile1);
//                if (!pdfBinding.pdfViewOld.isRecycled()) {
//                    pdfBinding.pdfViewOld.recycle();
//                }
//                pdfBinding.pdfViewOld.fromUri(urii1).load();
//                pdfBinding.pdfViewOld.setOnTouchListener(new View.OnTouchListener() {
//                    GestureDetector gd = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener(){
//                        @Override
//                        public boolean onDoubleTap(MotionEvent e){
//                            Toast.makeText(PDFActivity.this,"Double tap",Toast.LENGTH_LONG).show();
//                            return super.onDoubleTap(e);
//
//                        }
//
//                    });
//                    @Override
//                    public boolean onTouch(View view, MotionEvent motionEvent) {
//                        return false;
//                    }
//                });
//                billType = "long";
//                break;
            case R.id.btn_Short_pdf:
                binding.pdfLayout.setVisibility(View.GONE);
                //binding.pdfLayoutOld.setVisibility(View.GONE);
                shortBillLayoutBinding.shortBill.setVisibility(View.VISIBLE);
                binding.btnShortPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.login_button_structure));
                binding.btnShortPdf.setTextColor(ContextCompat.getColor(this,R.color.white));
                binding.btnLongPdf.setBackground(ContextCompat.getDrawable(this,R.drawable.pdf_format_structure));
                binding.btnLongPdf.setTextColor(ContextCompat.getColor(this,R.color.black));
                billType = "short";
                break;
            case R.id.zoomClick:
                Uri uri = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                Intent intentZoom = new Intent(PDFActivity.this, TestPdfActivity.class);
                intentZoom.putExtra("uri", uri.toString());
                startActivity(intentZoom);
                break;
//            case R.id.zoomClickOld:
//                Uri uriOld = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile1);
//                Intent intentZoom1 = new Intent(PDFActivity.this, TestPdfActivity.class);
//                intentZoom1.putExtra("uri", uriOld.toString());
//                startActivity(intentZoom1);
//                break;
        }
    }

    public void TraditionalListDialog()
    {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(PDFActivity.this);
        builder.setTitle("Please choose a printer to print the short format bill");
        String[] options = {"2 inch printer", "3 inch printer"};
        //Pass the array list in Alert dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                printerType = new HashMap<>();
                switch (which) {
                    case 0: // Select option task
                        thermalPrinterTwoInch();
                        printerType.put("printerType"," 2 inch Was Chosen");
                        break;
                    case 1: // Config it as you need here
                        printerType.put("printerType"," 3 inch Was Chosen");
                        thermalPrinterThreeInch();
                        break;

                }
                MyApplication.cleverTapAPI.pushEvent("Printer Type",printerType);
                Log.v("pribterType",printerType.toString());
            }
        });
// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void fetchCutlyLinkfromApi(int id){
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
                            createWhatsppsmsToShare(data.getString("pdfLink"),id);


                        } else{
                            createWhatsppsmsToShare(invoice.getString("pdfLink"),id);


                        }
                    }
                    else{ createWhatsppsmsToShare(invoice.getString("pdfLink"),id);

                    }

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

    private void shareOnWhatsApp(int id) {
        try {
            if (invoice.has("pdfLink") && invoice.getString("pdfLink") != null && invID > 0) {
                fetchCutlyLinkfromApi(id);
            } else if (invID < 0) {
                Util.sendWhatsAppMessageasPDF(invoice.getString("customerMobileNo"), this, pdfFile);
            } else {
               // DialogUtils.showToast(this, "customer mobile number is not entered while creating bill");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createWhatsppsmsToShare(String url,int id) {
        try {
            Double totalAmount, totalAfterDiscount;
            totalAmount = Double.parseDouble(invoice.getString("totalAmount"));
            if (invoice.has("totalAfterDiscount")) {
                totalAfterDiscount = invoice.getDouble("totalAfterDiscount");
            } else {
                totalAfterDiscount = totalAmount;
            }
            String smsBody = "Dear user"
                    + ", Your total payable amount is "
                    + totalAfterDiscount
                    + " and your invoice is at " + url;
            if(id == 1)
            {
                sharePdf(smsBody);
                //Util.sendWhatsAppMessage(invoice.getString("customerMobileNo"), this, smsBody);

            }
            else{
                //sharePdf(smsBody);
                if(invoice.getString("customerMobileNo").isEmpty())
                {
                    DialogUtils.showToast(this, "customer mobile number is not entered while creating bill");
                }
                else {
                    Util.sendWhatsAppMessage(invoice.getString("customerMobileNo"), this, smsBody);
                }

            }
        } catch (Exception e) {
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

    private void thermalPrinterTwoInch(){
       // MyApplication.cleverTapAPI.pushEvent("Clicked on 2 inch printer");
        Log.v("Welcome","Printer");
        try{
            System.out.println("IN Pdf Activity");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PDFActivity.PERMISSION_BLUETOOTH);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PDFActivity.PERMISSION_BLUETOOTH_ADMIN);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PDFActivity.PERMISSION_BLUETOOTH_CONNECT);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PDFActivity.PERMISSION_BLUETOOTH_SCAN);
            } else {
                if(bluetoothAdapter == null){
                    Toast.makeText(PDFActivity.this, "This device doesn't support Bluetooth ", Toast.LENGTH_LONG).show();
                } else {
                    if(bluetoothAdapter.isEnabled()){
                        try{
                            EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 48f, 32);
                            printer.printFormattedText(layout);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    private void thermalPrinterThreeInch(){
       // MyApplication.cleverTapAPI.pushEvent("Clicked on 3 inch printer");
        Log.v("Welcome","Printer");
        try{
            System.out.println("IN Pdf Activity");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PDFActivity.PERMISSION_BLUETOOTH);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, PDFActivity.PERMISSION_BLUETOOTH_ADMIN);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PDFActivity.PERMISSION_BLUETOOTH_CONNECT);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PDFActivity.PERMISSION_BLUETOOTH_SCAN);
            } else {
                if(bluetoothAdapter == null){
                    Toast.makeText(PDFActivity.this, "This device doesn't support Bluetooth ", Toast.LENGTH_LONG).show();
                } else {
                    if(bluetoothAdapter.isEnabled()){
                        try{
                            EscPosPrinter printer = new EscPosPrinter(BluetoothPrintersConnections.selectFirstPaired(), 203, 70f, 46);
                            printer.printFormattedText(layoutThreeInch);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void generateShortBillTwoInch(List<InvoiceItems> invoiceItems){
        try{
            localInvoiceId = getIntent().getExtras().getLong("localInvId");
            invoiceViewModel.getCurrentInvoice(localInvoiceId).observe(this, invoiceModelV2 -> {
                try {
                    invoice = new JSONObject(new Gson().toJson(invoiceModelV2));
                    String shopName, shopAddress, userMobile, userEmail,  userGSTNo, billNo, billTo, billToAdd, billToMobNo, date, items, qty;
                    boolean isGSTAvailablePrint = false;
                    shopName = profile.getString("shopName");
                    layout = "[C]<b><font size='tall'> " + shopName +  "</font></b>\n";
                    shopAddress = profile.getString("shopAddr") + "," + profile.getString("city")
                            + "," + profile.getString("state") + " - " + profile.getString("pincode");
                    layout += "[L]"+ shopAddress + "\n";
                    userMobile = profile.getString("mobileNo");
                    layout += "[C]Phone: "+ userMobile + "\n";
                    if(profile.has("email") && !profile.getString("email").isEmpty()){
                        userEmail = profile.getString("email");
                        layout += "[C]Email: "+ userEmail + "\n";
                    }
                    userGSTNo = profile.has("gstNo") ? profile.getString("gstNo") : "";
                    if(invoice.has("gstType") && profile.has("gstNo") && !(userGSTNo.isEmpty())){
                        layout += "[C]GSTIN: "+ userGSTNo + "\n";
                    }
                    if (invoice.has("gstType") && !invoice.getString("gstType").isEmpty()) {
                        layout += "[C] ***TAX INVOICE***\n";
                        isGSTAvailablePrint =true;
                    } else {
                        layout += "[C] ***INVOICE***\n";
                    }
                    layout += "[C]--------------------------------\n";
                    if(invoice.has("gstType") && !invoice.getString("gstType").isEmpty()){
                        billNo = String.valueOf(getIntent().getExtras().getInt("gstBillNo"));
                    } else {
                        billNo = String.valueOf(getIntent().getExtras().getInt("nonGstBillNo"));
                    }
                    layout += "[L]Bill No:"+ billNo;
                    date = invoice.getString("invoiceDate");
                    layout += "[R] Date:"+ date+"\n";
                    billTo = invoice.getString("customerName");
                    billToMobNo =invoice.getString("customerMobileNo");
                    billToAdd = invoice.getString("customerAddress");
                    if(billTo.isEmpty())
                    {
                        billTo = getIntent().getExtras().getString("customerName");
                    }
                    if(billTo.length() != 0){
                        layout += "[L]Billed To:" + billTo + "\n";
                    } else {
                        layout += "[L]Billed To:\n";
                    }
                    if(billToAdd.isEmpty())
                    {
                        billToAdd=getIntent().getExtras().getString("customerAddress");
                    }
                    if(!billToAdd.isEmpty()){
                        layout += "[L]" + billToAdd + "\n";
                    }
                    if(billToMobNo.isEmpty())
                    {
                        billToMobNo=getIntent().getExtras().getString("customerMobileNo");
                    }
                    if(billToMobNo.length() != 0){
                        layout += "[L]Contact No:" + billToMobNo + "\n";
                    } else {
                        layout += "[L]Contact No:\n";
                    }
                    layout += "[L]\n";
                    layout += "[C]<b>--------------------------------</b>\n";
                    layout += "[L]Description \n";
                    layout += "[L]Qty     MRP     Rate     NetAmnt\n";
                    if(invoice.has("gstType") && !invoice.getString("gstType").isEmpty()){
                        layout += "[L]   Tax%\n";
                    }
                    layout += "[C]--------------------------------\n";
                    for(int i = 0; i < invoiceItems.size();i++){
                        String productName = "", productMRP = "", productQty = "", productRate = "", productNetAmt = "", productTax = "";
                        productName = invoiceItems.get(i).getName();
                        if(productName.length() != 0){
                            layout += "[L]" + productName + "\n";
                        }
                        productQty = String.valueOf((int)invoiceItems.get(i).getQuantity());
                        productMRP = (Util.formatDecimalValue(invoiceItems.get(i).getPrice()));
                        productRate = (Util.formatDecimalValue((int)invoiceItems.get(i).getGstAmount()));
                        productNetAmt = Util.formatDecimalValue((int)invoiceItems.get(i).getTotalAmount());
                        layout += "[L]" + productQty + "    " + productMRP + "    " + productRate + "    " + productNetAmt + "\n";
                        if(isGSTAvailablePrint){
                            productTax = String.valueOf((int)invoiceItems.get(i).getGst())+"%";
                            layout += "[L]" + productTax + "\n";
                        }
                    }
                    layout += "[C]--------------------------------\n";
                    items = getIntent().getExtras().getString("itemsSize");
                    qty = getIntent().getExtras().getString("quantityCount");
                    float totalAfterDiscount = 0, totalAmount = 0;
                    totalAmount = Float.parseFloat(String.valueOf(invoice.getDouble("totalAmount")));
                    if (invoice.has("totalAfterDiscount")) {
                        totalAfterDiscount = (float) invoice.getDouble("totalAfterDiscount");
                    } else {
                        totalAfterDiscount = totalAmount;
                    }
                    layout += "[L]Items: " + items + "[C]Qty: " + qty + "[R] "+ totalAmount + "\n";
                   // layout += "[L]Items: " + items + "[C]Qty: " + qty + "[R] "+ totalAfterDiscount + "\n";
                    layout += "[C]--------------------------------\n";
                    layout += "[L]Total Amount:[R]"+totalAfterDiscount + "\n";
                    layout += "[C]TOTAL SAVINGS: " + Util.formatDecimalValue(totalAmount - totalAfterDiscount) + "\n";
                    if(invoice.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailablePrint){
                        layout += "[L]GST" +   "[C]TAX AMT\n";
                        layout += "[L]SGST" + "[C]" + getIntent().getExtras().getString("shortBillGstAmt") + "\n";
                        layout += "[L]CGST" + "[C]" + getIntent().getExtras().getString("shortBillGstAmt") + "\n";
                        layout += "[L]IGST" + "[C]0.0\n";
                    } else if (invoice.getString("gstType").equals("IGST (Central/outstation customer") && isGSTAvailablePrint){
                        layout += "[L]GST" + "[C]TAX AMT\n";
                        layout += "[L]SGST" + "[C]0.0\n";
                        layout += "[L]CGST" + "[C]0.0\n";
                        layout += "[L]IGST" + "[C]" + getIntent().getExtras().getString("shortBillGstAmt") + "\n";
                    }
                    layout += "[C]--------------------------------\n";
                    layout += "[C]Made with BillBook\n";
                    layout += "[L]***Thank you for shopping!***";
                    layout += "[L]" + "\n";
                    layout += "[L]" + "\n";
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void generateShortBillThreeInch(List<InvoiceItems> invoiceItems){
        try{
            localInvoiceId = getIntent().getExtras().getLong("localInvId");
            invoiceViewModel.getCurrentInvoice(localInvoiceId).observe(this, invoiceModelV2 -> {
                try {
                    invoice = new JSONObject(new Gson().toJson(invoiceModelV2));
                    String shopName, shopAddress, userMobile, userEmail,  userGSTNo, billNo, billTo, billToAdd, billToMobNo, date, items, qty;
                    boolean isGSTAvailablePrint = false;
                    shopName = profile.getString("shopName");
                    layoutThreeInch = "[C]<b><font size='tall'> " + shopName +  "</font></b>\n";
                    shopAddress = profile.getString("shopAddr") + "," + profile.getString("city")
                            + "," + profile.getString("state") + " - " + profile.getString("pincode");
                    layoutThreeInch += "[L]"+ shopAddress + "\n";
                    userMobile = profile.getString("mobileNo");
                    layoutThreeInch += "[C]Phone: "+ userMobile + "\n";
                    if(profile.has("email") && !profile.getString("email").isEmpty()){
                        userEmail = profile.getString("email");
                        layoutThreeInch += "[C]Email: "+ userEmail + "\n";
                    }
                    userGSTNo = profile.has("gstNo") ? profile.getString("gstNo") : "";
                    if(invoice.has("gstType") && profile.has("gstNo") && !(userGSTNo.isEmpty())){
                        layoutThreeInch += "[C]GSTIN: "+ userGSTNo + "\n";
                    }
                    if (invoice.has("gstType") && !invoice.getString("gstType").isEmpty()) {
                        layoutThreeInch += "[C] ***TAX INVOICE***\n";
                        isGSTAvailablePrint =true;
                    } else {
                        layoutThreeInch += "[C] ***INVOICE***\n";
                    }
                    layoutThreeInch += "[C]----------------------------------------------\n";
                    if(invoice.has("gstType") && !invoice.getString("gstType").isEmpty()){
                        billNo = String.valueOf(getIntent().getExtras().getInt("gstBillNo"));
                    } else {
                        billNo = String.valueOf(getIntent().getExtras().getInt("nonGstBillNo"));
                    }
                    layoutThreeInch += "[L]Bill No:"+ billNo;
                    date = invoice.getString("invoiceDate");
                    layoutThreeInch += "[R] Date:"+ date+"\n";
                    billTo = invoice.getString("customerName");
                    billToMobNo =invoice.getString("customerMobileNo");
                    billToAdd = invoice.getString("customerAddress");
                    if(billTo.isEmpty())
                    {
                        billTo = getIntent().getExtras().getString("customerName");
                    }
                    if(billTo.length() != 0){
                        layoutThreeInch += "[L]Billed To:" + billTo + "\n";
                    } else {
                        layoutThreeInch += "[L]Billed To:\n";
                    }
                    if(billToAdd.isEmpty())
                    {
                        billToAdd=getIntent().getExtras().getString("customerAddress");
                    }
                    if(!billToAdd.isEmpty()){
                        layoutThreeInch += "[L]" + billToAdd + "\n";
                    }
                    if(billToMobNo.isEmpty())
                    {
                        billToMobNo=getIntent().getExtras().getString("customerMobileNo");
                    }
                    if(billToMobNo.length() != 0){
                        layoutThreeInch += "[L]Contact No:" + billToMobNo + "\n";
                    } else {
                        layoutThreeInch += "[L]Contact No:\n";
                    }
                    layoutThreeInch += "[L]\n";
                    layoutThreeInch += "[C]----------------------------------------------\n";
                    layoutThreeInch += "[L]Description \n";
                    layoutThreeInch += "[L]Qty         MRP         Rate         NetAmnt\n";
                    if(invoice.has("gstType") && !invoice.getString("gstType").isEmpty()){
                        layoutThreeInch += "[L]   Tax%\n";
                    }
                    layoutThreeInch += "[C]----------------------------------------------\n";
                    for(int i = 0; i < invoiceItems.size();i++){
                        String productName = "", productMRP = "", productQty = "", productRate = "", productNetAmt = "", productTax = "";
                        productName = invoiceItems.get(i).getName();
                        if(productName.length() != 0){
                            layoutThreeInch += "[L]" + productName + "\n";
                        }
                        productQty = String.valueOf((int)invoiceItems.get(i).getQuantity());
                        productMRP = (Util.formatDecimalValue(invoiceItems.get(i).getPrice()));
                        Log.v("mrp",productMRP.toString());
                        productRate = (Util.formatDecimalValue((int)invoiceItems.get(i).getGstAmount()));
                        Log.v("rate",productRate.toString());
                        productNetAmt = Util.formatDecimalValue((int)invoiceItems.get(i).getTotalAmount());
                        layoutThreeInch += "[L]" + productQty + "        " + productMRP + "        " + productRate + "        " + productNetAmt + "\n";
                        if(isGSTAvailablePrint){
                            productTax = String.valueOf((int)invoiceItems.get(i).getGst())+"%";
                            layoutThreeInch += "[L]" + productTax + "\n";
                        }
                    }
                    layoutThreeInch += "[C]----------------------------------------------\n";
                    items = getIntent().getExtras().getString("itemsSize");
                    qty = getIntent().getExtras().getString("quantityCount");
                    float totalAfterDiscount = 0, totalAmount = 0;
                    totalAmount = Float.parseFloat(invoice.getString("totalAmount"));
                    if (invoice.has("totalAfterDiscount")) {
                        totalAfterDiscount = (float) invoice.getDouble("totalAfterDiscount");
                    } else {
                        totalAfterDiscount = totalAmount;
                    }
                    layoutThreeInch += "[L]Items:    " + items + "[C]Qty:    " + qty + "[R]    "+ totalAmount + "\n";
                   // layoutThreeInch += "[L]Items:    " + items + "[C]Qty:    " + qty + "[R]    "+ totalAfterDiscount + "\n";
                    layoutThreeInch += "[C]----------------------------------------------\n";
                    layoutThreeInch += "[L]Total Amount:[R]"+totalAfterDiscount + "\n";
                    layoutThreeInch += "[C]TOTAL SAVINGS: " + Util.formatDecimalValue(totalAmount - totalAfterDiscount) + "\n";
                    if(invoice.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailablePrint){
                        layoutThreeInch += "[L]GST" + "[C]TAX AMT\n";
                        layoutThreeInch += "[L]SGST" + "[C]" + getIntent().getExtras().getString("shortBillGstAmt") + "\n";
                        layoutThreeInch += "[L]CGST" + "[C]" + getIntent().getExtras().getString("shortBillGstAmt") + "\n";
                        layoutThreeInch += "[L]IGST" + "[C]0.0\n";
                    } else if (invoice.getString("gstType").equals("IGST (Central/outstation customer") && isGSTAvailablePrint){
                        layoutThreeInch += "[L]GST" + "[C]TAX AMT\n";
                        layoutThreeInch += "[L]SGST  " + "[C]0.0\n";
                        layoutThreeInch += "[L]CGST  " + "[C]0.0\n";
                        layoutThreeInch += "[L]IGST  " + "[C]" + getIntent().getExtras().getString("shortBillGstAmt") + "\n";
                    }
                    layoutThreeInch += "[C]----------------------------------------------\n";
                    layoutThreeInch += "[C]Made with BillBook\n";
                    layoutThreeInch += "[L]***Thank you for shopping!***";
                    layoutThreeInch += "[L]" + "\n";
                    layoutThreeInch += "[L]" + "\n";
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        } catch (Exception e){
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
                        if(body.has("data"))
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
            try {
                setDataAfterInvoiceItems(invoiceItems, context, isGSTAvailable, recyclerViewInvoiceProducts, GSTType);
                loadAndSetCompanyLogo();
                loadAndSetSignatureImage();
                if(!MyApplication.getIsFirstBill())
                {
                    MyApplication.isFirstBill(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                createPdfWrapper();
                                Uri urii = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                                if (!pdfBinding.pdfView.isRecycled()) {
                                    pdfBinding.pdfView.recycle();
                                }
                                pdfBinding.pdfView.fromUri(urii).load();
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }


                        }
                    }, 4000);
                }
                else {
                    createPdfWrapper();
                    Uri urii = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
                    if (!pdfBinding.pdfView.isRecycled()) {
                        pdfBinding.pdfView.recycle();
                    }
                    pdfBinding.pdfView.fromUri(urii).load();
                }

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
            try {
                setDataAfterInvoiceItemsShort(invoiceItems, context, isGSTAvailable, recyclerViewShortBillInvoiceProducts, GSTType);
                generateShortBillTwoInch(invoiceItems);
                generateShortBillThreeInch(invoiceItems);
                loadAndSetCompanyLogo();
                loadAndSetSignatureImage();
                //  createPdfWrapper();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
