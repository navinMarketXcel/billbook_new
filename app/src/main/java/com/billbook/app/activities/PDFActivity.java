package com.billbook.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

public class PDFActivity extends AppCompatActivity implements View.OnClickListener{
    private JSONObject requestInv;
    private RecyclerView recyclerViewInvoiceProducts;
    private TextView txtInvoiceDate,txtInvoiceNo,edtName, edtAddress,edtMobNo,tvAmountBeforeTax,tvTotal,tvGSTNo,SGST,CGST,IGST,totalGST;
    private List<NewInvoiceModels>  items;
    private Gson gson = new Gson();
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    private String filepath = null;
    private File pdfFile;
    private static final String TAG = "PDFActivity";
    private Button btnSubmit,btnPrint,closeBtn;
    private JSONObject profile;
    private boolean isGSTAvailable;
    private LinearLayout gstTotalLayout,totalAmountBeforeTaxLayout,custGstLayout;
    private TextView padding1,padding2,padding3,label,paddingLabel,mobileNoRetailer,GSTTitle,
    tvVendorName,tvStoreAddress,customer_gst;
    private ImageView shopImage;
    private JSONObject invoiceServer;
    private int invID =0;
    private int invoiceNumber;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        initUI();
        setData();

    }

    private void initUI(){
        txtInvoiceDate = findViewById(R.id.txtInvoiceDate);
        txtInvoiceNo = findViewById(R.id.txtInvoiceNo);
        edtName= findViewById(R.id.edtName);
        edtAddress = findViewById(R.id.edtAddress);
        edtMobNo = findViewById(R.id.edtMobNo);
        tvAmountBeforeTax = findViewById(R.id.tvAmountBeforeTax);
        tvTotal= findViewById(R.id.tvTotal);
        tvGSTNo = findViewById(R.id.tvGSTNo);
        recyclerViewInvoiceProducts = findViewById(R.id.recyclerViewInvoiceProducts);
        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(this);
        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(this);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);
        gstTotalLayout = findViewById(R.id.gstTotalLayout);
        SGST = findViewById(R.id.SGST);
        CGST= findViewById(R.id.CGST);
        IGST= findViewById(R.id.IGST);
         padding1 = findViewById(R.id.padding1);
         padding2 = findViewById(R.id.padding2);
         padding3 = findViewById(R.id.padding3);
         label = findViewById(R.id.productLabel);
         paddingLabel = findViewById(R.id.paddingLabel);
        totalGST = findViewById(R.id.totalGST);
        totalAmountBeforeTaxLayout=findViewById(R.id.totalAmountBeforeTaxLayout);
        tvVendorName = findViewById(R.id.tvVendorName);
        tvStoreAddress = findViewById(R.id.tvStoreAddress);
        custGstLayout = findViewById(R.id.custGstLayout);
        customer_gst = findViewById(R.id.customer_gst);
        mobileNoRetailer = findViewById(R.id.mobileNoRetailer);
        GSTTitle = findViewById(R.id.GSTTitle);
        shopImage = findViewById(R.id.shopImage);
    }
    private void setData(){
        try {
            requestInv = new JSONObject(getIntent().getExtras().getString("invoice"));
            invoiceServer = new JSONObject(getIntent().getExtras().getString("invoiceServer"));

            profile = new JSONObject (MyApplication.getUserDetails());
            if(profile.has("gstNo") && profile.getString("gstNo") != null && !profile.getString("gstNo").isEmpty()){
                isGSTAvailable=true;
                gstTotalLayout.setVisibility(View.VISIBLE);
                totalAmountBeforeTaxLayout.setVisibility(View.VISIBLE);
                invoiceNumber = invoiceServer.getJSONObject("invoice").getInt("gstBillNo");
            }else{
                TextView taxorBillTv = findViewById(R.id.taxorBillTv);
                taxorBillTv.setText("Bill");
                gstTotalLayout.setVisibility(View.GONE);
                totalAmountBeforeTaxLayout.setVisibility(View.GONE);
                invoiceNumber = invoiceServer.getJSONObject("invoice").getInt("nonGstBillNo");
            }
            imageURL = profile.has("profileImage")?profile.getString("profileImage"):null;
            if(imageURL ==null)
                shopImage.setVisibility(View.GONE);
            else{
                Picasso.get()
                        .load(imageURL.replace("https","http"))
                        .resize(100, 100)
                        .centerCrop()
                        .into(shopImage);
            }
            tvVendorName.setText(profile.getString("shopName"));
            tvStoreAddress.setText(profile.getString("shopAddr")+ " "+profile.getString("city")
                  +" "+ profile.getString("state")  +" - "+profile.getString("pincode"));
            tvGSTNo.setText(profile.has("gstNo")?profile.getString("gstNo"):"");
            mobileNoRetailer.setText("Mobile No- "+profile.getString("mobileNo"));


            invID = invoiceServer.getJSONObject("invoice").getInt("id");

            if(requestInv.getString("gstType").equals("CGST/SGST (Local customer)") && isGSTAvailable){
                IGST.setVisibility(View.GONE);
                CGST.setVisibility(View.VISIBLE);
                SGST.setVisibility(View.VISIBLE);
                padding3.setVisibility(View.GONE);
                label.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 3.5f));
                paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 3.5f));
            }else if(requestInv.getString("gstType").equals("IGST (Central/outstation customer)") && isGSTAvailable){
                IGST.setVisibility(View.VISIBLE);
                padding1.setVisibility(View.GONE);
                padding2.setVisibility(View.GONE);
                CGST.setVisibility(View.GONE);
                SGST.setVisibility(View.GONE);
                label.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 4.5f));
                paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f));
            }else{
                IGST.setVisibility(View.GONE);
                CGST.setVisibility(View.GONE);
                SGST.setVisibility(View.GONE);
                padding1.setVisibility(View.GONE);
                padding2.setVisibility(View.GONE);
                padding3.setVisibility(View.GONE);
                label.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 5.5f));
                paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));
            }
            if(requestInv.getString("GSTNo").isEmpty()){
                custGstLayout.setVisibility(View.INVISIBLE);

            }else{
                custGstLayout.setVisibility(View.VISIBLE);
                customer_gst.setText(requestInv.getString("GSTNo"));
            }
            txtInvoiceDate.setText(requestInv.getString("invoiceDate"));
            txtInvoiceNo.setText(""+invoiceNumber);
            float gst =0 ;
            if(isGSTAvailable)
             gst = Float.parseFloat(requestInv.getString("totalAmount")) -
                    Float.parseFloat(requestInv.getString("totalAmountBeforeGST")) ;
            totalGST.setText(Util.formatDecimalValue( gst));
            edtName.setText(requestInv.getString("customerName")+" ");
            edtAddress.setText(requestInv.getString("customerAddress")+" ");
            edtMobNo.setText(requestInv.getString("customerMobileNo")+" ");
//            tvGSTNo.setText(requestInv.getString("GSTNo")+" ");
            tvAmountBeforeTax.setText(Util.formatDecimalValue((float)requestInv.getDouble("totalAmountBeforeGST")));
            tvTotal.setText(Util.formatDecimalValue((float)requestInv.getDouble("totalAmount")));

            items = gson.fromJson(requestInv.getJSONArray("items").toString(), new TypeToken<List<NewInvoiceModels>>(){}.getType());
            NewInvoicePurchaseAdapter newInvoicePurchaseAdapter = new NewInvoicePurchaseAdapter(this,items,isGSTAvailable);
            LinearLayoutManager mLayoutManager =
                    new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            mLayoutManager.setStackFromEnd(true);
            recyclerViewInvoiceProducts.setLayoutManager(mLayoutManager);
            recyclerViewInvoiceProducts.setAdapter(newInvoicePurchaseAdapter);
            if(isGSTAvailable)
            GSTTitle.setText("GST"+(items.get(0).getGstType().equals("CGST/SGST (Local customer)")?" (SGST/CGST)":" (IGST)"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(filepath==null)
        try {
            createPdfWrapper();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

                filepath = filepath + "/" + edtName.getText().toString() + "_" + today + ".pdf";
                pdfFile = pdfWriter.exportPDF(filepath);

                if (invID > 0)
                uploadPDF();
                else
                    saveInvoiceOffline();

//        openPDF();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
                   DialogUtils.showToast(this, "WRITE_EXTERNAL Permission Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnPrint:
                Util.postEvents("Print","Print",this.getApplicationContext());
                openPDF();
                break;
            case R.id.btnSubmit:
                Util.postEvents("Share of Whatsapp","Share of Whatsapp",this.getApplicationContext());
                shareOnWhatsApp();
                break;
            case R.id.closeBtn:
                Util.postEvents("Close","Close",this.getApplicationContext());
                this.finish();
                break;

        }
    }
    private void shareOnWhatsApp(){
       try {
           if(requestInv.has("customerMobileNo")  && requestInv.getString("customerMobileNo") != null
                   && invoiceServer.has("pdfLink") && invoiceServer.getString("pdfLink") != null && invID>0){
               String smsBody = "Dear user"
                       + ", Your total payable amount is "
                       + requestInv.getDouble("totalAmount")
                       + " and your invoice is at " + invoiceServer.getString("pdfLink");

               Util.sendWhatsAppMessage(requestInv.getString("customerMobileNo"), this, smsBody);
           }else if (invID<0){
               Util.sendWhatsAppMessageasPDF(requestInv.getString("customerMobileNo"), this, pdfFile);
           }else{
            DialogUtils.showToast(this, "customer mobile number is not entered while creating bill");
           }
       }catch(JSONException e){

       }
    }
    private void openPDF() {
        Uri uri = FileProvider.getUriForFile(PDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void uploadPDF() {
            DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            Map<String, String> headerMap = new HashMap<>();
//        headerMap.put("Content-Type", "application/json");
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", pdfFile.getName(), RequestBody.create(MediaType.parse("*/*"), pdfFile));

            Call<Object> call = apiService.updateInvoicePdf(headerMap, invID, filePart);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP", body.toString());
                        if (body.getBoolean("status")) {
                            invoiceServer.put("pdfLink", body.getJSONObject("data").getString("pdfLink"));
                        } else {
                            DialogUtils.showToast(PDFActivity.this, "Failed upload pdf to server");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(PDFActivity.this, "Failed upload pdf to server");
                }
            });
        }

        private void saveInvoiceOffline(){
            try {

            JSONArray invoices = new JSONArray();
            if(!MyApplication.getUnSyncedInvoice().isEmpty()){
                    invoices = new JSONArray(MyApplication.getUnSyncedInvoice());
            }
            requestInv.put("pdfLink",filepath);
            invoices.put(requestInv);
            MyApplication.saveUnSyncedInvoices(invoices.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
}
