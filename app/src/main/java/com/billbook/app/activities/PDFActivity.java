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
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.database.daos.InvoiceItemDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.databinding.ActivityPdfBinding;
import com.billbook.app.databinding.InvoiceAmountLayoutUpdatedBinding;
import com.billbook.app.databinding.PdfContentNewBinding;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private JSONObject invoice;
    private RecyclerView recyclerViewInvoiceProducts;
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
    private String imageURL;
    private ActivityPdfBinding binding;
    private PdfContentNewBinding pdfBinding;
    private InvoiceAmountLayoutUpdatedBinding invoiceAmountLayoutUpdatedBinding;
    private boolean hasCompanyLogo = false, hasSignatureLogo = false;
    private boolean loadedCompanyLogo = false, loadedSignatureLogo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfBinding.inflate(getLayoutInflater());
        pdfBinding = binding.includedLayoutPdfContent;
        invoiceAmountLayoutUpdatedBinding = pdfBinding.invoiceAmountLayoutUpdated;
        setContentView(binding.getRoot());
        //setContentView(R.layout.activity_pdf);
        invoiceViewModel = ViewModelProviders.of(this).get(InvoiceViewModel.class);
        invoiceItemViewModel = ViewModelProviders.of(this).get(InvoiceItemsViewModel.class);
        initUI();
        setProfileData();
        setData();
    }

    private void initUI() {
        recyclerViewInvoiceProducts = pdfBinding.recyclerViewInvoiceProducts;
        pdfBinding.btnPrint.setOnClickListener(this);
        pdfBinding.closeBtn.setOnClickListener(this);
        pdfBinding.btnSubmit.setOnClickListener(this);
    }

    public static void setDataAfterInvoiceItems(List<InvoiceItems> invoiceItems,Context context,boolean isGSTAvailable, RecyclerView recyclerViewInvoiceProducts, String GSTType){
        NewInvoicePurchaseAdapter newInvoicePurchaseAdapter = new NewInvoicePurchaseAdapter(context, invoiceItems, isGSTAvailable,GSTType);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        recyclerViewInvoiceProducts.setLayoutManager(mLayoutManager);
        recyclerViewInvoiceProducts.setAdapter(newInvoicePurchaseAdapter);
    }

    private void setProfileData(){
        try{
            profile = new JSONObject(MyApplication.getUserDetails());
            if(profile.has("additionalData") && profile.getString("additionalData").length()!=0){
                invoiceAmountLayoutUpdatedBinding.tvAdditionalDetails.setVisibility(View.VISIBLE);
                invoiceAmountLayoutUpdatedBinding.tvAdditionalDetails.setText(profile.getString("additionalData"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private void setData() {
        try {
            localInvoiceId = getIntent().getExtras().getLong("localInvId");
            invoiceViewModel.getCurrentInvoice(localInvoiceId).observe(this, new Observer<InvoiceModelV2>() {
                @Override
                public void onChanged(InvoiceModelV2 invoiceModelV2) {
                    try{

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
                        pdfBinding.tvPreTax.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);
                        pdfBinding.llForHeader.setWeightSum(isGSTAvailable ? (float) 10.5 : 9);
                        pdfBinding.footer.setWeightSum(isGSTAvailable ? (float) 10.5 : 9);
                        pdfBinding.paddingLabelGst.setVisibility(isGSTAvailable ? View.VISIBLE : View.GONE);



                        if (invoice.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailable) {
                            GSTType = "CGST/SGST (Local customer)";
                            pdfBinding.IGST.setVisibility(View.GONE);
                            pdfBinding.CGST.setVisibility(View.VISIBLE);
                            pdfBinding.SGST.setVisibility(View.VISIBLE);
                            pdfBinding.padding3.setVisibility(View.GONE);
                            pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.5f));
                            pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.MATCH_PARENT, 3.5f));
                        } else if (invoice.getString("gstType").equals("IGST (Central/outstation customer)") && isGSTAvailable) {
                            GSTType = "IGST (Central/outstation customer)";
                            pdfBinding.IGST.setVisibility(View.VISIBLE);
                            pdfBinding.padding1.setVisibility(View.GONE);
                            pdfBinding.padding2.setVisibility(View.GONE);
                            pdfBinding.CGST.setVisibility(View.GONE);
                            pdfBinding.SGST.setVisibility(View.GONE);
                            pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 4.5f));
                            pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f));
                        } else {
                            pdfBinding.IGST.setVisibility(View.GONE);
                            pdfBinding.CGST.setVisibility(View.GONE);
                            pdfBinding.SGST.setVisibility(View.GONE);
                            pdfBinding.padding1.setVisibility(View.GONE);
                            pdfBinding.padding2.setVisibility(View.GONE);
                            pdfBinding.padding3.setVisibility(View.GONE);
                            pdfBinding.productLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.5f));
                            pdfBinding.paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));
                        }
                        if (invoice.getString("GSTNo").isEmpty()) {
                            pdfBinding.custGstLayout.setVisibility(View.INVISIBLE);
                        } else {
                            pdfBinding.custGstLayout.setVisibility(View.VISIBLE);
                            pdfBinding.customerGst.setText(invoice.getString("GSTNo"));
                        }
                        pdfBinding.txtInvoiceDate.setText(invoice.getString("invoiceDate"));
                        pdfBinding.txtInvoiceNo.setText("" + invoiceNumber);
                        float gst = 0;
                        if (isGSTAvailable)
                            gst = Float.parseFloat(invoice.getString("totalAmount")) -
                                    Float.parseFloat(invoice.getString("totalAmountBeforeGST"));
                        invoiceAmountLayoutUpdatedBinding.totalGST.setText(Util.formatDecimalValue(gst));
                        pdfBinding.edtName.setText(invoice.getString("customerName") + " ");
                        pdfBinding.edtAddress.setText(invoice.getString("customerAddress") + " ");
                        pdfBinding.edtMobNo.setText(invoice.getString("customerMobileNo") + " ");
//            tvGSTNo.setText(invoice.getString("GSTNo")+" ");
                        invoiceAmountLayoutUpdatedBinding.tvAmountBeforeTax.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmountBeforeGST")));
//                        invoiceAmountLayoutUpdatedBinding.tvTotal.setText(Util.formatDecimalValue((float) invoice.getDouble("totalAmount")));
                        float totalAfterDiscount = 0, totalAmount=0;
                        totalAmount = Float.parseFloat(invoice.getString("totalAmount"));
                        if(invoice.has("totalAfterDiscount")) {
                            totalAfterDiscount = (float) invoice.getDouble("totalAfterDiscount");
                        }
                        else{
                            totalAfterDiscount = totalAmount;
                        }
                        invoiceAmountLayoutUpdatedBinding.tvTotal.setText(Util.formatDecimalValue(totalAfterDiscount));
                        invoiceAmountLayoutUpdatedBinding.tvDiscount.setText(Util.formatDecimalValue(totalAmount-totalAfterDiscount));

                        new getCurrentItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao(),localInvoiceId,PDFActivity.this,isGSTAvailable,recyclerViewInvoiceProducts, GSTType).execute();

//                        items = gson.fromJson(invoice.getJSONArray("items").toString(), new TypeToken<List<NewInvoiceModels>>() {
//                        }.getType());

                        if (isGSTAvailable)
                            invoiceAmountLayoutUpdatedBinding.GSTTitle.setText("GST" + (items.get(0).getGstType().equals("CGST/SGST (Local customer)") ? " (SGST/CGST)" : " (IGST)"));
                        imageURL = profile.has("companyLogo") ? profile.getString("companyLogo") : null;

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            if (imageURL == null) {
                pdfBinding.shopImage.setVisibility(View.GONE);
                loadAndSetSignatureImage();
            }
            else {
                pdfBinding.shopImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(imageURL)
                        .resize(100, 100)
                        .into(pdfBinding.shopImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                loadAndSetSignatureImage();
                            }

                            @Override
                            public void onError(Exception e) {
                                loadAndSetSignatureImage();
                            }
                        });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadAndSetSignatureImage() {
        String signatureURL = null;
        try {
            signatureURL = profile.has("signatureImage") ? profile.getString("signatureImage").replaceAll("\\/","/") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                            try {
                                if (filepath == null) {
                                    createPdfWrapper();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            try {
                                if (filepath == null){
                                    createPdfWrapper();
                                }
                            } catch (FileNotFoundException error) {
                                error.printStackTrace();
                            }
                            e.printStackTrace();
                        }
                    });
        }else{
            try {
                if (filepath == null){
                    createPdfWrapper();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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

                filepath = filepath + "/" + (pdfBinding.edtName.getText().toString().substring(0,pdfBinding.edtName.getText().toString().length()-1)) + invoiceNumber + "_" + today + ".pdf";

            Log.d(TAG, "createPdf: filePath " + filepath);
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
            case R.id.btnPrint:
                Util.postEvents("Print", "Print", this.getApplicationContext());
                openPDF();
                break;
            case R.id.btnSubmit:
                Util.postEvents("Share of Whatsapp", "Share of Whatsapp", this.getApplicationContext());
                shareOnWhatsApp();
                break;
            case R.id.closeBtn:
                Util.postEvents("Close", "Close", this.getApplicationContext());
                this.finish();
                break;

        }
    }

    public void fetchCutlyLinkfromApi(){
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
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

                        if(data.has("pdfLink")&&data.getString("pdfLink")!=null){ createWhatsppsmsToShare(data.getString("pdfLink")); }
                        else{ createWhatsppsmsToShare(invoice.getString("pdfLink")); }
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
            if (invoice.has("customerMobileNo") && invoice.getString("customerMobileNo") != null
                    && invoice.has("pdfLink") && invoice.getString("pdfLink") != null && invID > 0) {
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

            Util.sendWhatsAppMessage(invoice.getString("customerMobileNo"), this, smsBody);
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
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
//        headerMap.put("Content-Type", "application/json");
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", pdfFile.getName(), RequestBody.create(MediaType.parse("*/*"), pdfFile));

        Call<Object> call = apiService.updateInvoicePdf(headerMap, invID, filePart);
        invoiceViewModel.updatePdfPath(localInvoiceId,filepath);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
//                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP", body.toString());
                    Log.d(TAG, "onResponse: pdfActivity " + body);
                    if (body.getBoolean("status")) {
                        invoice.put("pdfLink", body.getJSONObject("data").getString("pdfLink"));
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
            invoiceViewModel.updatePdfPath(localInvoiceId,filepath);
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

    private class getCurrentItemsAsyncTask extends AsyncTask<Void,Void,List<InvoiceItems>>{
        private InvoiceItemDao invoiceItemDao;
        private long invoiceId;
        private List<InvoiceItems> curItems;
        private Context context;
        private boolean isGSTAvailable;
        private RecyclerView recyclerViewInvoiceProducts;
        private String GSTType;

        private getCurrentItemsAsyncTask(InvoiceItemDao invoiceItemDao,long invoiceId, Context context,boolean isGSTAvailable,RecyclerView recyclerViewInvoiceProducts, String GSTType){
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
            setDataAfterInvoiceItems(invoiceItems,context,isGSTAvailable,recyclerViewInvoiceProducts, GSTType);
           try{
               createPdfWrapper();
           }
           catch(FileNotFoundException e){
               e.printStackTrace();
           }
        }
    }


}
