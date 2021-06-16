package com.billbook.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.BuildConfig;
import com.billbook.app.R;
import com.billbook.app.adapters.ExtraInputAdapter;
import com.billbook.app.adapters.InvoicePurchaseAdapter;
import com.billbook.app.database.models.PrintInvoice;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.PdfWriter;
import com.billbook.app.utils.Util;
import com.billbook.app.viewmodel.BillingManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class SendPDFActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BillingActivity";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    PrintInvoice mPrintInvoiceObj;
    ArrayList<String> stringArrayList = new ArrayList<>();
    private String filepath = null;
    private RecyclerView mRecyclerView, mRecyclerViewExtraInput;
    private TextView tvVendorName, tvStoreAddress, txtInvoiceNo, txtInvoiceDate, tvAmountBeforeTax,
            tvGSTHeader, tvGST, tvDiscountHeader, tvTotal, tvDiscount, tvGSTNo, tvAmountHeader, tvSGST, tvIGST, edtName, edtAddress;
    private TextView edtCity, edtState, edtMobNo, paymentAmount1, titlePayment1, titlePayment2, paymentAmount2;
    private Button fabSendPDF, btnPrint, btnSMS;
    private File pdfFile;
    private boolean showGst = false;
    private ExtraInputAdapter extraInputAdapter;
    private String DIALOG_MESSAGE = "Test MSG";
    private String SMS_BODY = "Test BODy";
    private boolean showGST = true;
    private NestedScrollView scollviewSendPDF;
    private SharedPreferences sharedPref;
    private boolean showInfo;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_pdf);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        showInfo = !sharedPref.getBoolean("isSendPdfscreenIntroShown", false);
        fabSendPDF = findViewById(R.id.btnSubmit);

        inIt();

        if (getIntent().hasExtra("mPrintInvoiceObj")) {

            mPrintInvoiceObj = (PrintInvoice) getIntent().getSerializableExtra("mPrintInvoiceObj");
            showGST = getIntent().getBooleanExtra("showGST", true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences sharedPref =
                            getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
                                    MODE_PRIVATE);
                    int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
                    final User user = MyApplication.getDatabase().userDao().getUser(userId);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView SGST = findViewById(R.id.SGST);
                            TextView CGST = findViewById(R.id.CGST);
                            TextView IGST = findViewById(R.id.IGST);
                            TextView padding1 = findViewById(R.id.padding1);
                            TextView padding2 = findViewById(R.id.padding2);
                            TextView padding3 = findViewById(R.id.padding3);
                            TextView label = findViewById(R.id.productLabel);
                            TextView paddingLabel = findViewById(R.id.paddingLabel);

                            if (user.getGst_no() == null || user.getGst_no().isEmpty()) {
                                showGst = false;
                                SGST.setVisibility(View.GONE);
                                CGST.setVisibility(View.GONE);
                                IGST.setVisibility(View.GONE);
                                padding1.setVisibility(View.GONE);
                                padding2.setVisibility(View.GONE);
                                padding3.setVisibility(View.GONE);
                                label.setLayoutParams(new LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 6f));
                                paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.MATCH_PARENT, 6f));
                            } else {
                                showGst = true;
                                SGST.setVisibility(View.VISIBLE);
                                CGST.setVisibility(View.VISIBLE);
                                IGST.setVisibility(View.VISIBLE);

                                padding1.setVisibility(View.VISIBLE);
                                padding2.setVisibility(View.VISIBLE);
                                padding3.setVisibility(View.VISIBLE);
                                label.setLayoutParams(new LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f));
                                paddingLabel.setLayoutParams(new LinearLayout.LayoutParams(
                                        0, LinearLayout.LayoutParams.MATCH_PARENT, 3f));
                            }
                            Log.v(TAG, "user::" + user);
                            setUserData(user);
                            try {
                                createPdfWrapper();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }).start();

//      tvVendorName.setText("");
            Date date = Calendar.getInstance().getTime();
            // Display a date in day, month, year format
            @SuppressLint("SimpleDateFormat") DateFormat formatter =
                    new SimpleDateFormat("EEE, dd MMM yyyy");
            String today = formatter.format(date);
            txtInvoiceDate.setText(today);

            if (mPrintInvoiceObj != null) {

                if (mPrintInvoiceObj.getStringArrayList() != null && mPrintInvoiceObj.getStringArrayList().size() > 0) {

                    findViewById(R.id.ll_extra).setVisibility(View.VISIBLE);

                    stringArrayList.addAll(mPrintInvoiceObj.getStringArrayList());
                    extraInputAdapter.notifyDataSetChanged();
                } else {
                    findViewById(R.id.ll_extra).setVisibility(View.GONE);
                }

                if (mPrintInvoiceObj.getDescription() != null
                        && mPrintInvoiceObj.getDescription().length() > 0) {
                    findViewById(R.id.ll_Sr).setVisibility(View.VISIBLE);
                    TextView textView = findViewById(R.id.txtSRNoOrDesc);
                    textView.setText(mPrintInvoiceObj.getDescription());
                }
                if (!mPrintInvoiceObj.isGSTEnabled() && !mPrintInvoiceObj.isDiscountEnabled()) {
                    tvDiscount.setVisibility(View.GONE);
                    tvDiscountHeader.setVisibility(View.GONE);

                    tvGST.setVisibility(View.GONE);
                    tvGSTHeader.setVisibility(View.GONE);

                    tvAmountHeader.setVisibility(View.GONE);
                    tvAmountBeforeTax.setVisibility(View.GONE);
                }

                if (!mPrintInvoiceObj.isDiscountEnabled()) {
                    tvDiscount.setVisibility(View.GONE);
                    tvDiscountHeader.setVisibility(View.GONE);
                }

                if (!mPrintInvoiceObj.isGSTEnabled()) {
                    tvGST.setVisibility(View.GONE);
                    tvGSTHeader.setVisibility(View.GONE);
                }
                if (!getIntent().getBooleanExtra("isFormSearchBill", false)) {
                    if (showGST)
                        MyApplication.setInvoiceNumber(mPrintInvoiceObj.getInvoice().getInvoice_no() + 1);
                    else
                        MyApplication.setInvoiceNumberForNonGst(mPrintInvoiceObj.getInvoice().getInvoice_no() + 1);


                }
                txtInvoiceNo.setText("INV-" + mPrintInvoiceObj.getInvoice().getInvoice_no());

                txtInvoiceDate.setText(mPrintInvoiceObj.getInvoice().getInvoiceDate());
                tvGST.setText(mPrintInvoiceObj.getCgstAmount());
                tvSGST.setText(mPrintInvoiceObj.getSgstAmount());
                tvIGST.setText(mPrintInvoiceObj.getIgstAmount());
                tvAmountBeforeTax.setText(String.format("%.2f", Float.parseFloat(mPrintInvoiceObj.getAmountBeforeTax()) + Float.parseFloat(mPrintInvoiceObj.getGstAmount()) + Float.parseFloat(mPrintInvoiceObj.getDiscountAmount())));
                tvTotal.setText(mPrintInvoiceObj.getTotal());
                tvDiscount.setText(mPrintInvoiceObj.getDiscountAmount());

                titlePayment1.setText(mPrintInvoiceObj.getInvoice().getPaymentMethod1());
                paymentAmount1.setText(String.format("%.2f", mPrintInvoiceObj.getInvoice().getPaymentMethod1Amount()));
                LinearLayout paymentLL = findViewById(R.id.paymentLL);

                if (mPrintInvoiceObj.getInvoice().getPaymentMethod2() != null) {
                    paymentLL.setVisibility(View.VISIBLE);
                    paymentAmount2.setText(String.format("%.2f", mPrintInvoiceObj.getInvoice().getPaymentMethod2Amount()));
                    titlePayment2.setText(mPrintInvoiceObj.getInvoice().getPaymentMethod2());
                } else {
                    paymentLL.setVisibility(View.INVISIBLE);
                }

                edtName.setText(mPrintInvoiceObj.getInvoice().getInvoiceName()
                        + ((mPrintInvoiceObj.getInvoice().getInvoiceEmail() != null
                        && !mPrintInvoiceObj.getInvoice().getInvoiceEmail().isEmpty()) ?
                        " (" + mPrintInvoiceObj.getInvoice().getInvoiceEmail() + ") " : ""));
                edtState.setText(mPrintInvoiceObj.getInvoice().getInvoiceState());
                edtAddress.setText(mPrintInvoiceObj.getInvoice().getInvoiceAddress());
                edtCity.setText(mPrintInvoiceObj.getInvoice().getInvoiceCity());
                edtMobNo.setText(mPrintInvoiceObj.getInvoice().getInvoiceMobileNo());

                ArrayList<Purchase> mInvoicePurchaseArraylist = mPrintInvoiceObj.getPurchaseArrayList();
//        if(mInvoicePurchaseArraylist.size()>5){
//          LinearLayout linearLayout = findViewById(R.id.footer);
//          linearLayout.setLayout
//        }
                InvoicePurchaseAdapter invoicePurchaseAdapter =
                        new InvoicePurchaseAdapter(this, mInvoicePurchaseArraylist, MyApplication.showGST());

                LinearLayoutManager mLayoutManager =
                        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                mLayoutManager.setStackFromEnd(true);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(invoicePurchaseAdapter);
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scollviewSendPDF.fullScroll(View.FOCUS_DOWN);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startSpotLight(btnSMS, "SMS", "Click here to send the bill to whatsApp.");

                    }
                }, 300);
            }
        }, 500);
    }

    private void uploadPdf() {
        if (Util.isNetworkAvailable(this)) {
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("pdf", pdfFile.getName(), RequestBody.create(MediaType.parse("*/*"), pdfFile));
            final ProgressDialog progressDialog = DialogUtils.startProgressDialog(SendPDFActivity.this, "");
            String token = MyApplication.getUserToken();
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", token);
//    headerMap.put("Content-Type", "application/x-www-form-urlencoded");

            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            Call<RequestInvoice> call = apiService.uploadInvoicePDf(headerMap, mPrintInvoiceObj.getInvoice().getId(), filePart);
            call.enqueue(new Callback<RequestInvoice>() {
                @Override
                public void onResponse(Call<RequestInvoice> call, Response<RequestInvoice> response) {
                    final RequestInvoice requestInvoice = response.body();

                    if (requestInvoice != null) {
                        mPrintInvoiceObj.setPdfFile(requestInvoice.getPdf());
                        DialogUtils.showToast(SendPDFActivity.this, "file uploaded successfully");
                    } else {
                        DialogUtils.showToast(SendPDFActivity.this, "Failed to update the file");
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<RequestInvoice> call, Throwable t) {
                    progressDialog.dismiss();
                }
            });
        } else {
            DialogUtils.showToast(this, "Failed to upload pdf of bill.");
        }
    }

    @SuppressLint("InflateParams")
    public void inIt() {
        scollviewSendPDF = findViewById(R.id.scollviewSendPDF);
        tvVendorName = findViewById(R.id.tvVendorName);
        tvStoreAddress = findViewById(R.id.tvStoreAddress);
        txtInvoiceNo = findViewById(R.id.txtInvoiceNo);
        tvGSTNo = findViewById(R.id.tvGSTNo);
        txtInvoiceDate = findViewById(R.id.txtInvoiceDate);
        tvAmountBeforeTax = findViewById(R.id.tvAmountBeforeTax);
        tvGSTHeader = findViewById(R.id.tvGSTHeader);
        tvDiscountHeader = findViewById(R.id.tvDiscountHeader);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvAmountHeader = findViewById(R.id.tvAmountHeader);
        tvGST = findViewById(R.id.tvGST);
        tvTotal = findViewById(R.id.tvTotal);
        tvSGST = findViewById(R.id.tvSGST);
        tvIGST = findViewById(R.id.tvIGST);
        edtName = findViewById(R.id.edtName);
        edtAddress = findViewById(R.id.edtAddress);
        edtCity = findViewById(R.id.edtCity);
        edtState = findViewById(R.id.edtState);
        edtMobNo = findViewById(R.id.edtMobNo);

        btnPrint = findViewById(R.id.btnPrint);
        btnSMS = findViewById(R.id.btnSMS);
        paymentAmount1 = findViewById(R.id.paymentAmount1);
        paymentAmount2 = findViewById(R.id.paymentAmount2);
        titlePayment2 = findViewById(R.id.titlePayment2);
        titlePayment1 = findViewById(R.id.titlePayment1);


        mRecyclerViewExtraInput = findViewById(R.id.mRecyclerViewExtraInput);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerViewExtraInput.setLayoutManager(mLayoutManager);
        mRecyclerViewExtraInput.setItemAnimator(new DefaultItemAnimator());

        extraInputAdapter = new ExtraInputAdapter(SendPDFActivity.this, stringArrayList);
        mRecyclerViewExtraInput.setAdapter(extraInputAdapter);

        mRecyclerView = findViewById(R.id.recyclerViewInvoiceProducts);

        btnPrint.setOnClickListener(this);
        btnSMS.setOnClickListener(this);
        fabSendPDF.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSubmit:
                sharePdf(pdfFile);
//
//        try {
//          createPdfWrapper();
//        } catch (FileNotFoundException e) {
//          e.printStackTrace();
//        }
                break;

            case R.id.btnPrint:
                openPDF();
//        Toast.makeText(this, "Print Coming Soon", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnSMS:
                openSharer();
       /* int hasReadSMSPermission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (hasReadSMSPermission != PackageManager.PERMISSION_GRANTED) {

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] { Manifest.permission.SEND_SMS },
                    REQUEST_CODE_ASK_PERMISSIONS);
              }
              return;
            }

            requestPermissions(new String[] { Manifest.permission.SEND_SMS },
                REQUEST_CODE_ASK_PERMISSIONS);
          }
          return;
        } else {

          String phoneNumber = mPrintInvoiceObj.getInvoice().getInvoiceMobileNo();
          String smsBody = "Dear "
              + mPrintInvoiceObj.getInvoice().getInvoiceName()
              + ", This is invoice SMS. Your total payable amount is "
              + mPrintInvoiceObj.getTotal()
              + " and your invoice is at "+(mPrintInvoiceObj.getPdfFile() == null ?"":mPrintInvoiceObj.getPdfFile());

          String SMS_SENT = "SMS_SENT";
          String SMS_DELIVERED = "SMS_DELIVERED";

          PendingIntent sentPendingIntent =
              PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), 0);
          PendingIntent deliveredPendingIntent =
              PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), 0);

          // For when the SMS has been sent
          registerReceiver(new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {

              switch (getResultCode()) {
                case Activity.RESULT_OK:

                  Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
                  break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                  Toast.makeText(context, "Generic failure cause", Toast.LENGTH_SHORT).show();
                  break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                  Toast.makeText(context, "Service is currently unavailable", Toast.LENGTH_SHORT)
                      .show();
                  break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                  Toast.makeText(context, "No pdu provided", Toast.LENGTH_SHORT).show();
                  break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                  Toast.makeText(context, "Radio was explicitly turned off", Toast.LENGTH_SHORT)
                      .show();
                  break;
              }
            }
          }, new IntentFilter(SMS_SENT));

          // For when the SMS has been delivered
          registerReceiver(new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
              switch (getResultCode()) {
                case Activity.RESULT_OK:
                  Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                  break;
                case Activity.RESULT_CANCELED:
                  Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                  break;
              }
            }
          }, new IntentFilter(SMS_DELIVERED));

          // Get the default instance of SmsManager
          SmsManager smsManager = SmsManager.getDefault();
          // Send a text based SMS
          smsManager.sendTextMessage(phoneNumber, null, smsBody, sentPendingIntent,
              deliveredPendingIntent);
        }
*/
                break;
        }
    }

    @Override
    public void onBackPressed() {
//    BillingManager.reset();
        closeBill(null);
//    super.onBackPressed();
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

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(TAG, "Created a new directory for PDF");
        }

        Date date = Calendar.getInstance().getTime();

        // Display a date in day, month, year format
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String today = formatter.format(date);

        PdfWriter pdfWriter =
                new PdfWriter(SendPDFActivity.this, (ViewGroup) findViewById(R.id.ll_root));
        filepath = docsFolder.getAbsolutePath();
        filepath = filepath + "/" + edtName.getText().toString() + "_" + today + ".pdf";
        pdfFile = pdfWriter.exportPDF(filepath);
        if (!getIntent().getBooleanExtra("isFormSearchBill", false) || mPrintInvoiceObj.getPdfFile() == null) {
            uploadPdf();
        }
    }

    private void sharePdf(File pdfFile) {

//    Intent share = new Intent();
//    share.setAction(Intent.ACTION_SEND);
//    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//    share.setType("application/pdf");
//    Uri uri =
//        FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider",
//            pdfFile);
//    share.putExtra(Intent.EXTRA_STREAM, uri);
//    startActivity(Intent.createChooser(share, "Share Invoice By"));

//    finish();
        ArrayList<Purchase> mBillingPurchaseArrayList = mPrintInvoiceObj.getPurchaseArrayList();
        boolean showQuestion = false;
        for (Purchase purchase : mBillingPurchaseArrayList) {
            if (purchase.getCategory_name().toLowerCase().equals("mobile hand sets") ||
                    purchase.getCategory_name().toLowerCase().equals("tablets") ||
                    purchase.getCategory_name().toLowerCase().equals("mobile & tablets")) {
                showQuestion = true;
                break;
            }
        }
        String smsBody = null;
        if(showQuestion){
            smsBody  = "Dear "
                    + mPrintInvoiceObj.getInvoice().getInvoiceName()
                    + ", This is invoice SMS. Your total payable amount is "
                    + mPrintInvoiceObj.getTotal()
                    + " and your invoice is at " + "http://mbill.market-xcel.com:8000/?id="
                    +mPrintInvoiceObj.getInvoice().getId();

        }else
         smsBody = "Dear "
                + mPrintInvoiceObj.getInvoice().getInvoiceName()
                + ", This is invoice SMS. Your total payable amount is "
                + mPrintInvoiceObj.getTotal()
                + " and your invoice is at " + (mPrintInvoiceObj.getPdfFile() == null ? "" : mPrintInvoiceObj.getPdfFile());

        Util.sendWhatsAppMessage(mPrintInvoiceObj.getInvoice().getInvoiceMobileNo(), this, smsBody);

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
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void setUserData(User user) {
        if (user != null) {
            if (user.getShop_name() != null) {
                getSupportActionBar().setTitle("Share Invoice");
                tvVendorName.setText(user.getShop_name());
            }
            if (user.getAddress() != null) {
                tvStoreAddress.setText(user.getAddress() + (user.getMobile_no() != null ? " Contact No - " + user.getMobile_no() : ""));
            }
            if (user.getGst_no() != null &&
                    (Float.parseFloat(mPrintInvoiceObj.getGstAmount() != null ? mPrintInvoiceObj.getGstAmount() : "0") > 0)) {
                tvGSTNo.setText("GST # " + user.getGst_no());
            } else
                tvGSTNo.setVisibility(View.INVISIBLE);
        }
    }

    private void openPDF() {
        Uri uri = FileProvider.getUriForFile(SendPDFActivity.this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//      intent.setDataAndType(uri, "application/pdf");
//    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//      try
//      {
        startActivity(intent);
//      }
//      catch(ActivityNotFoundException e)
//      {
//              DialogUtils.showToast(this, "Print Coming Soon");
//      }
    }

    public void closeBill(View v) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        BillingManager.reset();
        this.finish();
    }

    private void openSharer() {
        Intent share = new Intent(Intent.ACTION_SEND);

        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        share.setType("*/*");

        // Make sure you put example png image named myImage.png in your
        // directory
//    Uri uri = Uri.fromFile(pdfFile);
//        if(mPrintInvoiceObj.getInvoice().get)
        ArrayList<Purchase> mBillingPurchaseArrayList = mPrintInvoiceObj.getPurchaseArrayList();
        boolean showQuestion = false;
        for (Purchase purchase : mBillingPurchaseArrayList) {
            if (purchase.getCategory_name().toLowerCase().equals("mobile hand sets") ||
                    purchase.getCategory_name().toLowerCase().equals("tablets") ||
                    purchase.getCategory_name().toLowerCase().equals("mobile & tablets")) {
                showQuestion = true;
                break;
            }
        }
//        if(showQuestion){
//
//        }else {
            Uri pdfUri = FileProvider.getUriForFile(this,
                    this.getApplicationContext().getPackageName() + ".provider",
                    pdfFile);
            share.putExtra(Intent.EXTRA_STREAM, pdfUri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        }
        startActivity(Intent.createChooser(share, "Share Bill!"));
    }

    private void startSpotLight(View view, String title, String description) {

        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.closeBtn) {
                                SharedPreferences sharedPref =
                                        SendPDFActivity.this.getSharedPreferences(SendPDFActivity.this.getString(R.string.preference_file_key),
                                                SendPDFActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isSendPdfscreenIntroShown", true);
                                editor.commit();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        scollviewSendPDF.fullScroll(View.FOCUS_UP);
                                    }
                                }, 300);
                            } else {
                                if (view.getId() == R.id.btnSMS) {
                                    startSpotLight(btnPrint, "Print", "Click here to print the bill in pdf.");
                                } else if (view.getId() == R.id.btnPrint) {
                                    startSpotLight(findViewById(R.id.closeBtn), "Close", "Click here to close this screen");
                                }
                            }
                        }
                    })
                    .build()
                    .show();
        }
    }
}
