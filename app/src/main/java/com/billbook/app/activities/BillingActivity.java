package com.billbook.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.billbook.app.R;
import com.billbook.app.adapters.BillingPurchaseAdapter;
import com.billbook.app.adapters.ExtraInputAdapter;
import com.billbook.app.database.models.Inventory;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.PrintInvoice;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.database.models.Specification;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.WebserviceResponseHandler;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.utils.CircleCheckBox;
import com.billbook.app.utils.Util;
import com.billbook.app.viewmodel.BillingManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

import static android.widget.Toast.LENGTH_SHORT;

public class BillingActivity extends AppCompatActivity
        implements View.OnClickListener, WebserviceResponseHandler {
    private static final String TAG = "BillingActivity";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    @SuppressLint("SimpleDateFormat")
    DateFormat formatter =
            new SimpleDateFormat("EEE, dd MMM yyyy");
    RelativeLayout ibGSTType;
    ArrayList<String> stringArrayList = new ArrayList<>();
    String callFromAdpter = "";
    View customView = null;
    float finalAmount = 0;
    private Date invoiceDate = new Date();
    private boolean isappstopped = false;
    private TextView tvVendorName, tvStoreAddress, txtSwipe, txtInvoiceDate, tvAmountBeforeTax,
            txtDiscountTotal, txtGSTTotal, tvTotal, tvGSTNo, txtSRNoOrDesc;
    private EditText edtName, edtAddress, edtEmail, paymentValue1, edtMobNo, edtGST, edtDiscount, edtSR, edtIMEI, edtCity, edtCGST, edtIGST, edtSGST;
    private Spinner spinnerState, paymentMethod1, paymentMethod2;
    private NestedScrollView scrollview;
    private long id;
    private Handler handler = new Handler();
    private LocationManager locationManager;
    private float mAmountWithoutGST = 0;
    private float mAmountWithGST = 0;
    //    private CheckedTextView checkedTextView;
    private boolean isGSTEdit = false;
    private ImageButton ibAddProducts, ibScanProduct, ibSearchProduct, ibScanIMEI;
    private Button btnSubmit, ibAddIMEI, ibAddSr;
    private BottomDialog bottomDialog;
    private ProgressDialog progressDialog;
    private float discountAmount = 0;
    private float gstAmount = 0, IGST = 0, SGST = 0;
    private String invoiceDateStr = "";
    private String gstType = "1";
    private Invoice invoice;
    private RequestInvoice requestInvoice;
    private PrintInvoice mPrintInvoiceObj = null;
    private RecyclerView mRecyclerView, mRecyclerViewExtraInput;
    private BillingPurchaseAdapter billingPurchaseAdapter;
    private ExtraInputAdapter extraInputAdapter;
    private ArrayList<Purchase> mBillingPurchaseArrayList = new ArrayList<>();
    private int totalMobileCategoryCount;
    private LocationListener locationListener = new LocationListener();

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing);
        scrollview = findViewById(R.id.scrollview);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        permissionForLocation();
        inIt();

        setUserData();

        billingPurchaseAdapterInitialization();

        addEdtiTextListnerForEditText();

        addSwipeToPurchaseItem();
        startSpotLight(txtInvoiceDate, "Invoice Date", "Please select Invoice date.");
    }

    @SuppressLint("InflateParams")
    public void inIt() {
        spinnerState = findViewById(R.id.spinnerState);
        paymentMethod1 = findViewById(R.id.paymentMethod1);
        paymentMethod2 = findViewById(R.id.paymentMethod2);
        paymentValue1 = findViewById(R.id.paymentValue1);
        paymentMethod1.setSelection(1);
        tvVendorName = findViewById(R.id.tvVendorName);
        tvStoreAddress = findViewById(R.id.tvStoreAddress);
        txtSwipe = findViewById(R.id.txtSwipe);
        tvGSTNo = findViewById(R.id.tvGSTNo);
        txtInvoiceDate = findViewById(R.id.txtInvoiceDate);
        tvAmountBeforeTax = findViewById(R.id.tvAmountBeforeTax);
        txtDiscountTotal = findViewById(R.id.txtDiscountTotal);
        txtGSTTotal = findViewById(R.id.txtGSTTotal);
        tvTotal = findViewById(R.id.tvTotal);
        txtSRNoOrDesc = findViewById(R.id.txtSRNoOrDesc);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtAddress = findViewById(R.id.edtAddress);
        //edtState = findViewById(R.id.edtState);
        edtMobNo = findViewById(R.id.edtMobNo);
        edtGST = findViewById(R.id.edtGST);
        edtCGST = findViewById(R.id.edtCGST);
        edtDiscount = findViewById(R.id.edtDiscount);
        edtCity = findViewById(R.id.edtCity);

        edtIMEI = findViewById(R.id.edtIMEI);
        edtSR = findViewById(R.id.edtSR);
        edtIMEI.setEnabled(true);


        ibAddIMEI = findViewById(R.id.ibAddIMEI);
        ibAddSr = findViewById(R.id.ibAddSr);
        ibGSTType = findViewById(R.id.ibGSTType);

        mRecyclerView = findViewById(R.id.recyclerViewBillingProducts);
        ibAddProducts = findViewById(R.id.ibAddProducts);
        ibScanProduct = findViewById(R.id.ibScanProduct);
        ibSearchProduct = findViewById(R.id.ibSearchProduct);
        btnSubmit = findViewById(R.id.btnSubmit);
        ibScanIMEI = findViewById(R.id.ibScanIMEI);
        edtSGST = findViewById(R.id.edtSGST);
        edtIGST = findViewById(R.id.edtIGST);

        ibAddProducts.setOnClickListener(this);
        ibScanProduct.setOnClickListener(this);
        ibSearchProduct.setOnClickListener(this);
        ibGSTType.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        ibAddIMEI.setOnClickListener(this);
        ibAddSr.setOnClickListener(this);

        ibScanIMEI.setOnClickListener(this);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<>(BillingActivity.this, android.R.layout.simple_spinner_item, MyApplication.getStateList());
        myAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
        spinnerState.setAdapter(myAdapter);

        mRecyclerViewExtraInput = findViewById(R.id.mRecyclerViewExtraInput);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerViewExtraInput.setLayoutManager(mLayoutManager);
        mRecyclerViewExtraInput.setItemAnimator(new DefaultItemAnimator());

        extraInputAdapter = new ExtraInputAdapter(BillingActivity.this, stringArrayList);
        mRecyclerViewExtraInput.setAdapter(extraInputAdapter);

        checkBoxFunction();

        mPrintInvoiceObj = new PrintInvoice();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            customView = inflater.inflate(R.layout.bottom_sheet_gst_type, null);

            if (customView != null) {

                final CircleCheckBox checkBoxGSTRed = customView.findViewById(R.id.checkBoxGSTRed);
                final CircleCheckBox checkBoxGSTGreen = customView.findViewById(R.id.checkBoxGSTGreen);

                checkBoxGSTGreen.setChecked(true);
                checkBoxGSTRed.setEnabled(false);

                checkBoxGSTRed.setOnCheckedChangeListener(new CircleCheckBox.OnCheckedChangeListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onCheckedChanged(CircleCheckBox view, boolean isChecked) {
                        if (isChecked) {
                            gstType = "0";
                            checkBoxGSTGreen.setEnabled(false);
                        } else {
                            checkBoxGSTGreen.setEnabled(true);
                        }
                    }
                });

                checkBoxGSTGreen.setOnCheckedChangeListener(new CircleCheckBox.OnCheckedChangeListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onCheckedChanged(CircleCheckBox view, boolean isChecked) {
                        if (isChecked) {
                            gstType = "1";
                            checkBoxGSTRed.setEnabled(false);
                        } else {
                            checkBoxGSTRed.setEnabled(true);
                        }
                    }
                });


            }
        }
    }

    private void checkBoxFunction() {
        CircleCheckBox checkBoxDiscount = findViewById(R.id.checkBoxDiscount);
        checkBoxDiscount.setChecked(true);
        checkBoxDiscount.setOnCheckedChangeListener(new CircleCheckBox.OnCheckedChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onCheckedChanged(CircleCheckBox view, boolean isChecked) {
                if (isChecked) {

                    edtDiscount.requestFocus();
                    edtDiscount.setEnabled(true);
                    edtDiscount.setText("");
                    discountAmount = 0;
//                    edtGST.setText("");
//                    txtDiscountTotal.setText("0");
//                    txtGSTTotal.setText("0");
//                    tvTotal.setText(String.format("%.2f", mAmountWithoutGST));
                } else {
                    edtDiscount.setEnabled(false);
                    edtDiscount.setText("");
//                    edtGST.setText("");
                    discountAmount = 0;
                    txtDiscountTotal.setText("0");
//                    txtGSTTotal.setText("0");
                    tvTotal.setText(String.format("%.2f", mAmountWithGST));
                }
            }
        });

        CircleCheckBox SGSTCheckbox = findViewById(R.id.SGSTCheckbox);
        SGSTCheckbox.setChecked(true);
        CircleCheckBox CGSTCheckbox = findViewById(R.id.CGSTCheckbox);
        CGSTCheckbox.setChecked(true);
//        SGSTCheckbox.setOnCheckedChangeListener(new CircleCheckBox.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CircleCheckBox view, boolean isChecked) {
//                if(isChecked){
//                    edtSGST.setEnabled(true);
//                    edtSGST.setFocusable(true);
//                    edtSGST.setText("");
//                } else{
//                    edtSGST.setEnabled(false);
//                    edtSGST.setFocusable(false);
//                    edtSGST.setText("");
//                }
//            }
//        });

        CircleCheckBox IGSTCheckbox = findViewById(R.id.IGSTCheckbox);
        IGSTCheckbox.setOnCheckedChangeListener(new CircleCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CircleCheckBox view, boolean isChecked) {
                if (isChecked) {
                    edtIGST.setEnabled(true);
                    edtIGST.setFocusable(true);
                    edtIGST.setText("");
                } else {
                    edtIGST.setEnabled(false);
                    edtIGST.setFocusable(false);
                    edtIGST.setText("");
                }
            }
        });


        CircleCheckBox checkBoxEdtGST = findViewById(R.id.checkBoxEdtGST);
//        checkBoxEdtGST.setChecked(true);
        checkBoxEdtGST.setOnCheckedChangeListener(new CircleCheckBox.OnCheckedChangeListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onCheckedChanged(CircleCheckBox view, boolean isChecked) {
                if (isChecked) {
                    edtGST.requestFocus();
                    edtGST.setEnabled(false);
                    edtGST.setText("");
                    edtDiscount.setText("");
                    discountAmount = 0;
                    txtDiscountTotal.setText("0");
                    txtGSTTotal.setText("0");
                    refreshAmount();
                } else {
                    edtGST.setText("");
                    edtGST.setEnabled(false);
                    discountAmount = 0;
                    edtDiscount.setText("");
                    txtDiscountTotal.setText("0");
                    txtGSTTotal.setText("0");
                    refreshAmount();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setUserData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref =
                        getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
                                MODE_PRIVATE);
                int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
                User user = MyApplication.getDatabase().userDao().getUser(userId);
                Log.v(TAG, "user::" + user);
                if (user != null) {
                    if (user.getShop_name() != null) {
                        Objects.requireNonNull(getSupportActionBar()).setTitle("Billing");
                        tvVendorName.setText(user.getShop_name());
                    }

                    if (user.getAddress() != null) {
                        tvStoreAddress.setText(user.getAddress());
                    }
                    if (user.getGst_no() != null) {
                        tvGSTNo.setText("GST NO : " + user.getGst_no());
                    }
                }
            }
        }).start();

        tvVendorName.setText("");
        invoiceDate = Calendar.getInstance().getTime();
        // Display a date in day, month, year format

        invoiceDateStr = formatter.format(invoiceDate);
        txtInvoiceDate.setText(invoiceDateStr);
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
                            new SimpleDateFormat("EEE, dd MMM yyyy");
                    invoiceDateStr = formatter.format(invoiceDate);
                    txtInvoiceDate.setText(invoiceDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.v(TAG, "selctedFromDate::" + invoiceDate);

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    public void billingPurchaseAdapterInitialization() {

        billingPurchaseAdapter = new BillingPurchaseAdapter(this, mBillingPurchaseArrayList);

        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(billingPurchaseAdapter);
    }

    private void addSwipeToPurchaseItem() {
        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    static final float ALPHA_FULL = 1.0f;

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState,
                                            boolean isCurrentlyActive) {

                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                            View itemView = viewHolder.itemView;

                            Paint p = new Paint();
                            Bitmap icon;

                            if (dX > 0) {

                                //color : left side (swiping towards right)
                                p.setARGB(255, 255, 255, 255);
                                c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                                        (float) itemView.getBottom(), p);

                                // icon : left side (swiping towards right)
                                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                        R.drawable.ic_delete);
                                c.drawBitmap(icon, (float) itemView.getLeft() + convertDpToPx(16),
                                        (float) itemView.getTop()
                                                + ((float) itemView.getBottom()
                                                - (float) itemView.getTop()
                                                - icon.getHeight()) / 2, p);
                            } else {

                                //color : right side (swiping towards left)
                                p.setARGB(255, 255, 255, 255);

                                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                        (float) itemView.getRight(), (float) itemView.getBottom(), p);

                                //icon : left side (swiping towards right)
                                icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                        R.drawable.ic_edit);
                                c.drawBitmap(icon,
                                        (float) itemView.getRight() - convertDpToPx(16) - icon.getWidth(),
                                        (float) itemView.getTop()
                                                + ((float) itemView.getBottom()
                                                - (float) itemView.getTop()
                                                - icon.getHeight()) / 2, p);
                            }

                            // Fade out the view when it is swiped out of the parent
                            final float alpha =
                                    ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                            viewHolder.itemView.setAlpha(alpha);
                            viewHolder.itemView.setTranslationX(dX);
                        } else {
                            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
                                    isCurrentlyActive);
                        }
                    }

                    private int convertDpToPx(int dp) {
                        return Math.round(
                                dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                        final int position = viewHolder.getAdapterPosition(); //swiped position

                        if (direction == ItemTouchHelper.LEFT) { //swipe left

                            Toast.makeText(BillingActivity.this, "Update Purchase Item", Toast.LENGTH_SHORT)
                                    .show();

                            startActivity(
                                    new Intent(BillingActivity.this, BillingAddProductActivity.class).putExtra(
                                            "purchaseObj", mBillingPurchaseArrayList.get(position)));

                            BillingManager.remove(position);
                            mBillingPurchaseArrayList.remove(position);
                            billingPurchaseAdapter.notifyItemRemoved(position);
                        } else if (direction == ItemTouchHelper.RIGHT) {//swipe right

                            BillingManager.remove(position);
                            Purchase purchase = mBillingPurchaseArrayList.remove(position);
//                            purchase.getQuantity()
                            insertInventory(purchase);
                            mRecyclerView.removeViewAt(position);
                            billingPurchaseAdapter.notifyItemRemoved(position);
                            billingPurchaseAdapter.notifyDataSetChanged();
                            mAmountWithoutGST = grandTotal(mBillingPurchaseArrayList);

                            refreshAmount();
                            checkCategoryForIMEI();

                            if (mBillingPurchaseArrayList.size() > 0) {
                                txtSwipe.setVisibility(View.VISIBLE);
                            } else {
                                txtSwipe.setVisibility(View.GONE);
                            }
                        }
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    // Get the results:

    private void insertInventory(Purchase purchase) {
        final Inventory inventory = new Inventory();
        inventory.setId(0);
        inventory.setProduct_id(purchase.getProduct_id());
        inventory.setProduct_name(purchase.getProduct_name());

        inventory.setCategory(purchase.getCategory());
        inventory.setCategory_name(purchase.getCategory_name());

        inventory.setBrand(purchase.getBrand());
        inventory.setCategory_name(purchase.getBrand_name());

        inventory.setUser(MyApplication.getUserID());
        inventory.setQuantity(purchase.getQuantity());
        inventory.setPrice(purchase.getPrice());
        inventory.setTax(0.0f);

        AppRepository.getInstance().insertInventory(inventory,getApplicationContext());
    }

    private void addEdtiTextListnerForEditText() {

        edtDiscount.addTextChangedListener(new TextWatcher() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //if (edtDiscount.getText().toString().trim().equals("")) {
                //  float edtDiscountAmount = 0;
                //  float grandTotal = mAmountWithoutGST - edtDiscountAmount;
                //  tvTotal.setText(String.format("%.2f", grandTotal));
                //}
                //
                //if (edtDiscount.getText().toString().trim().length() > 0) {
                //  //float edtDiscountPercent = Float.parseFloat(edtDiscount.getText().toString().trim());
                //  //discountAmount = Util.calculateDiscountAmount(edtDiscountPercent, mAmountWithoutGST);
                //  discountAmount = Float.parseFloat(edtDiscount.getText().toString().trim());
                //  if (count > 0) {
                //    System.out.println("Print discountAmount Values : " + discountAmount);
                //    txtDiscountTotal.setText(String.format("%.2f", discountAmount));
                //  }
                //} else {
                //  txtDiscountTotal.setText("0");
                //}
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtDiscount.getText().toString().trim().equals("")) {
                    float edtDiscountAmount = 0;
                    if (mAmountWithoutGST <= 0) {
                        Toast.makeText(BillingActivity.this, "Please Add Product First", LENGTH_SHORT).show();
                        return;
                    } else {
                        float grandTotal = mAmountWithGST - edtDiscountAmount;
                        tvTotal.setText(String.format("%.2f", grandTotal));
                    }
                }

                if (edtDiscount.getText().toString().trim().length() > 0) {
                    //float edtDiscountPercent = Float.parseFloat(edtDiscount.getText().toString().trim());
                    //discountAmount = Util.calculateDiscountAmount(edtDiscountPercent, mAmountWithoutGST);
                    discountAmount = Float.parseFloat(edtDiscount.getText().toString().trim());
                    if (mAmountWithoutGST <= 0) {
                        Toast.makeText(BillingActivity.this, "Please Add Product First", LENGTH_SHORT).show();
                        edtDiscount.setText("");
                    } else {

                        if (discountAmount > mAmountWithGST) {
                            Toast.makeText(BillingActivity.this,
                                    "You are typing discount value greater than total amount", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            txtDiscountTotal.setText(String.format("%.2f", discountAmount));
                            if (mAmountWithGST > mAmountWithoutGST) {
                                tvTotal.setText(String.format("%.2f", mAmountWithGST - discountAmount));
                            } else {
                                tvTotal.setText(String.format("%.2f", mAmountWithGST - discountAmount));
                            }
                        }
                    }
                } else {
                    txtDiscountTotal.setText("0");
                }

                // TODO Auto-generated method stub
            }
        });

        edtGST.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (edtGST.getText().toString().trim().equals("")) {
                    float edtGSTAmount = 0;
                    float gstAmount = Util.calculateGSTAmount(edtGSTAmount, mAmountWithoutGST);
                    float grandTotal = gstAmount + mAmountWithoutGST;
                    tvTotal.setText(String.format("%.2f", grandTotal));
                    edtSGST.setText("0");
                    edtCGST.setText("0");
                }

                if (edtGST.getText().toString().trim().length() > 0) {

                    float edtGSTAmount = Float.parseFloat(edtGST.getText().toString().trim());
//                    edtGSTAmount = edtGSTAmount+IGST+SGST;
                    gstAmount = Util.calculateGSTAmount(edtGSTAmount, mAmountWithoutGST);
                    mAmountWithGST = gstAmount + mAmountWithoutGST;
                    edtSGST.setText(String.format("%.2f", edtGSTAmount / 2));
                    edtCGST.setText(String.format("%.2f", edtGSTAmount / 2));
                    if (count > 0) {
                        System.out.println("Print Values : " + gstAmount + " " + mAmountWithGST);

                        if (mAmountWithoutGST <= 0) {
                            Toast.makeText(BillingActivity.this, "Please Add Product First", LENGTH_SHORT).show();
                            edtGST.setText("");
                        } else {

                            txtGSTTotal.setText(String.format("%.2f", gstAmount));
                            tvTotal.setText(String.format("%.2f", mAmountWithGST - discountAmount));
                        }
                    }
                } else {
                    tvTotal.setText("0");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                isGSTEdit = true;
                if (edtGST.getText().toString().trim().equals("")) {
                    tvTotal.setText(String.format("%.2f", mAmountWithGST));
                }
                if (edtGST.getText().toString().trim().length() > 0) {
                    float edtGSTAmount = Float.parseFloat(edtGST.getText().toString().trim());
//                    edtGSTAmount = edtGSTAmount+IGST+SGST;
                    float gstAmount = Util.calculateGSTAmount(edtGSTAmount, mAmountWithoutGST);
                    mAmountWithGST = gstAmount + mAmountWithoutGST;

                    System.out.println("Print Values : " + gstAmount + " " + mAmountWithGST);

                    tvTotal.setText(String.format("%.2f", mAmountWithGST - discountAmount));
                }
            }
        });

        edtIGST.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//               if (edtGST.getText().toString().trim().equals("")) {
//                    float edtGSTAmount = 0+
//                            (edtIGST.getText().toString().isEmpty()?0:Float.parseFloat(edtIGST.getText().toString().trim()));
//                    float gstAmount = Util.calculateGSTAmount(edtGSTAmount, mAmountWithoutGST);
//                    float grandTotal = gstAmount + mAmountWithoutGST;
//                    tvTotal.setText(String.format("%.2f", grandTotal));
//                    return;
//                }

//                if (edtGST.getText().toString().trim().length() > 0) {

                float edtGSTAmount = (edtGST.getText().toString().isEmpty() ?
                        0 : Float.parseFloat(edtGST.getText().toString().trim())) +
                        (edtIGST.getText().toString().isEmpty() ?
                                0 : Float.parseFloat(edtIGST.getText().toString().trim()));
//                    edtGSTAmount = edtGSTAmount+IGST+SGST;
                gstAmount = Util.calculateGSTAmount(edtGSTAmount, mAmountWithoutGST);
                mAmountWithGST = gstAmount + mAmountWithoutGST;

//                    if (count > 0) {
                System.out.println("Print Values : " + gstAmount + " " + mAmountWithGST);

                if (mAmountWithoutGST <= 0) {
                    Toast.makeText(BillingActivity.this, "Please Add Product First", LENGTH_SHORT).show();
                } else {

                    txtGSTTotal.setText(String.format("%.2f", gstAmount));
                    tvTotal.setText(String.format("%.2f", mAmountWithGST - discountAmount));
                }
//                    }
//                } else {
//                    tvTotal.setText("0");
//                }*/
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
              /*  float gst = 0;
                if (edtGST.getText().toString().trim().equals("")) {
                    gst =0;
                } else{
                    gst = Float.parseFloat(edtGST.getText().toString().trim());
                }
                if (!edtIGST.getText().toString().trim().equals("")) {
                    gst = gst+Float.parseFloat(edtIGST.getText().toString().trim());
                }
                edtGST.setText(gst+"");*/

                isGSTEdit = true;
//                if (edtGST.getText().toString().trim().equals("")) {
//                    tvTotal.setText(String.format("%.2f", mAmountWithGST));
//                }
//                if (edtGST.getText().toString().trim().length() > 0) {
                float edtGSTAmount = (edtGST.getText().toString().isEmpty() ?
                        0 : Float.parseFloat(edtGST.getText().toString().trim())) +
                        (edtIGST.getText().toString().isEmpty() ?
                                0 : Float.parseFloat(edtIGST.getText().toString().trim()));
//                    edtGSTAmount = edtGSTAmount+IGST+SGST;
                float gstAmount = Util.calculateGSTAmount(edtGSTAmount, mAmountWithoutGST);
                mAmountWithGST = gstAmount + mAmountWithoutGST;

                System.out.println("Print Values : " + gstAmount + " " + mAmountWithGST);

                tvTotal.setText(String.format("%.2f", mAmountWithGST - discountAmount));
//                }

//                IGST = edtIGST.getText().toString().isEmpty()?0:Float.parseFloat(edtIGST.getText().toString().trim());
            }
        });

//        edtSGST.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // TODO Auto-generated method stub
//                if(edtSGST.getText().toString().equals(""))
//                    SGST=0;
//                else
//                    SGST = Float.parseFloat(edtSGST.getText().toString().trim());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                float gst = 0;
//                if (edtGST.getText().toString().trim().equals("")) {
//                    gst =0;
//                } else{
//                    gst = Float.parseFloat(edtGST.getText().toString().trim());
//                }
//                if (!edtSGST.getText().toString().trim().equals("")) {
//                    gst = gst+Float.parseFloat(edtSGST.getText().toString().trim()) -SGST;
//                } else{
//                    gst = gst -SGST;
//                }
////                SGST = edtSGST.getText().toString().isEmpty()?0:Float.parseFloat(edtSGST.getText().toString().trim());
//                edtGST.setText(gst+"");
//
//            }
//        });
    }

    private boolean validateInput() {

        boolean result = true;
        if (edtName.getText().toString().trim().isEmpty()) {
            Toast.makeText(BillingActivity.this, "Please enter Customer Name.", Toast.LENGTH_LONG).show();
            result = false;
        } else if (!edtEmail.getText().toString().trim().isEmpty() &&
                !Util.isValidEmail(edtEmail.getText().toString().trim())) {
            Toast.makeText(BillingActivity.this, "Please enter valid email address.", Toast.LENGTH_LONG)
                    .show();
            result = false;
        } else if (edtAddress.getText().toString().trim().isEmpty()) {
            Toast.makeText(BillingActivity.this, "Please enter Customer Address.", Toast.LENGTH_LONG)
                    .show();
            result = false;
        } else if (edtCity.getText().toString().trim().isEmpty()) {
            Toast.makeText(BillingActivity.this, "Please enter Customer City.", Toast.LENGTH_LONG)
                    .show();
            result = false;
        } else if (spinnerState.getSelectedItemPosition() == 0) {

            Toast.makeText(BillingActivity.this, "Please select Customer State.", Toast.LENGTH_LONG)
                    .show();
            result = false;
        } else if (edtMobNo.getText().toString().trim().isEmpty()
                || edtMobNo.getText().toString().trim().length() < 10) {
            Toast.makeText(BillingActivity.this, "Please enter valid Customer mobile number.",
                    Toast.LENGTH_LONG).show();
            result = false;
        } /*else if (edtGST.isEnabled() && (edtGST.getText().toString().trim().isEmpty()
         *//*|| edtGST.getText().toString().trim().equalsIgnoreCase("0")*//*)) {
            Toast.makeText(BillingActivity.this, "Please enter GST.", Toast.LENGTH_LONG).show();
            result = false;
        }*/ else if (billingPurchaseAdapter.getItemCount() == 0) {
            Toast.makeText(BillingActivity.this, "Please add atleast one product for billing.",
                    Toast.LENGTH_LONG).show();
            result = false;
        } else if (Float.parseFloat(paymentValue1.getText().toString()) > finalAmount) {
            Toast.makeText(BillingActivity.this, "Please enter the value less than total amount.",
                    Toast.LENGTH_LONG).show();
            result = false;
        } else if (paymentMethod2.getSelectedItemPosition() == 0 && Float.parseFloat(paymentValue1.getText().toString()) < finalAmount) {
            Toast.makeText(BillingActivity.this, "Please enter the value equals to total amount.",
                    Toast.LENGTH_LONG).show();
            result = false;

        }
        //else if (gstType.equalsIgnoreCase("")) {
        //    Toast.makeText(BillingActivity.this, "Please select Bill Type for billing.",
        //            Toast.LENGTH_LONG).show();
        //    result = false;
        //}

        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        addInventories();
        BillingManager.reset();
    }

    public void addInventories() {
        while (mBillingPurchaseArrayList.size() > 0) {
            Purchase purchase = mBillingPurchaseArrayList.remove(0);
            insertInventory(purchase);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted

                } else {
                    // Permission Denied
                    Toast.makeText(this, "Location Permission Denied", LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onResume() {
        super.onResume();
        if (isappstopped) {
            return;
        }
        mBillingPurchaseArrayList.clear();
        mBillingPurchaseArrayList.addAll(BillingManager.getInstance().getCurrentPurchaseList());
        Log.d(TAG, mBillingPurchaseArrayList.toString());
        billingPurchaseAdapter.notifyDataSetChanged();
        mAmountWithoutGST = grandTotal(mBillingPurchaseArrayList);
        refreshAmount();
        checkCategoryForIMEI();

        if (mBillingPurchaseArrayList.size() > 0) {
            txtSwipe.setVisibility(View.VISIBLE);
        } else {
            txtSwipe.setVisibility(View.GONE);
        }
        paymentValue1.setText(String.format("%.2f", finalAmount));
    }

    private void checkCategoryForIMEI() {
        if (mobileCategoryCount(mBillingPurchaseArrayList) > 0) {
            findViewById(R.id.dividerView).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_IMEI).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.dividerView).setVisibility(View.GONE);
            findViewById(R.id.ll_IMEI).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                if (callFromAdpter.equalsIgnoreCase("callFromAdpter")) {

                    callFromAdpter = "";

                    edtIMEI.setText(result.getContents());
                } else {
                    scanProductBarcode(result.getContents());
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private float grandTotal(ArrayList<Purchase> items) {

        float totalPrice = 0;
        for (int i = 0; i < items.size(); i++) {
            totalPrice +=
                    (items.get(i).getPrice() - items.get(i).getDiscount()) * items.get(i).getQuantity();
        }
        return totalPrice;
    }

    private int mobileCategoryCount(ArrayList<Purchase> items) {
        totalMobileCategoryCount = 0;
        for (int i = 0; i < items.size(); i++) {

           /* if (items.get(i).getCategory() == 39 || items.get(i).getCategory() == 20) {
                totalMobileCategoryCount++;
            }*/
            if (items.get(i).getCategory_name().toLowerCase().equals("mobile") ||
                    items.get(i).getCategory_name().toLowerCase().equals("mobile hand sets") ||
                    items.get(i).getCategory_name().toLowerCase().equals("tablets") ||
                    items.get(i).getCategory_name().toLowerCase().equals("mobile hand tablet") ||
                    items.get(i).getCategory_name().toLowerCase().equals("mobile & tablets")) {
                totalMobileCategoryCount++;
            }
        }
        return totalMobileCategoryCount;
    }

    private float totalDiscount(ArrayList<Purchase> items) {

        float totalDiscount = 0;
        for (int i = 0; i < items.size(); i++) {
            totalDiscount += (items.get(i).getDiscount());
        }
        return totalDiscount;
    }

    public void scanProductBarcode(final String result) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //first search scan  product in inventory
                final Inventory inventory =
                        MyApplication.getDatabase().inventoryDao().getInventoryBySerialNo(result);

                Log.d("Billing Adapter :", "" + inventory);
                // if scan product not available in inventory search it in Product table
                Product product = null;
                if (inventory == null) {
                    product =
                            MyApplication.getDatabase().productDao().getProductByBarcode(result);
                }
                Intent intent = new Intent(BillingActivity.this, BillingAddProductActivity.class);
                if (inventory != null) {
                    intent.putExtra("inventory", inventory);
                } else if (product != null) {
                    intent.putExtra("product", product);
                }
                startActivity(intent);
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ibAddProducts:
                Intent intent = new Intent(this, BillingCategoriesActivity.class);
                startActivity(intent);

                break;

            case R.id.btnSubmit:
                if (validateInput()) {
                    if (edtIMEI.isEnabled() && extraInputAdapter.getItemCount() < totalMobileCategoryCount) {
                        Toast.makeText(this, "Please Enter IMEI No.", Toast.LENGTH_SHORT).show();
                    } else {
                        refreshAmount();
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                                getString(R.string.preference_file_key), MODE_PRIVATE);
                        final int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
                        progressDialog = DialogUtils.startProgressDialog(BillingActivity.this, "");
                        new Thread(new Runnable() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void run() {
                                invoice = new Invoice();
                                invoice.setInvoiceName(edtName.getText().toString().trim());
                                invoice.setInvoiceEmail(edtEmail.getText().toString().trim());
                                invoice.setInvoiceAddress(edtAddress.getText().toString().trim());
                                invoice.setInvoiceCity(edtCity.getText().toString().trim());
                                Log.v(TAG, "spinnerState.getSelectedItem" + MyApplication.getStateList().get(spinnerState.getSelectedItemPosition()));
                                String state = MyApplication.getStateList().get(spinnerState.getSelectedItemPosition());
                                invoice.setInvoiceState(state);
                                invoice.setInvoiceMobileNo(edtMobNo.getText().toString().trim());
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                                invoice.setInvoiceDate(formatter.format(invoiceDate));
                                invoice.setCreatedAt(invoiceDate);
                                invoice.setPaymentMethod1(paymentMethod1.getSelectedItem().toString());
                                invoice.setPaymentMethod1Amount(Float.parseFloat(paymentValue1.getText().toString()) - discountAmount);
                                if (paymentMethod2.getSelectedItemPosition() > 0 &&
                                        invoice.getPaymentMethod1Amount() < finalAmount) {
                                    invoice.setPaymentMethod2(paymentMethod2.getSelectedItem().toString());
                                    invoice.setPaymentMethod2Amount(finalAmount - invoice.getPaymentMethod1Amount());
                                }
                                int invNo = 0;

                                if (gstType.equalsIgnoreCase("1") &&
                                        gstAmount > 0)
                                    gstType = "1";
                                else
                                    gstType = "0";


                                if (gstType.equalsIgnoreCase("1"))
                                    invNo = MyApplication.getInVoiceNumber();
                                else
                                    invNo = MyApplication.getInVoiceNumberForNonGst();

                                invoice.setInvoice_no(invNo == 0 ? 1 : invNo);
                                invoice.setUser(userId);

                                id = MyApplication.getDatabase().invoiceDao().insertInvoice(invoice);
                                Log.v(TAG, "inserted id::" + id);
                                int cnt = 0;
                                for (Purchase purchase : mBillingPurchaseArrayList) {
                                    purchase.setLocalId((int) id);
                                    purchase.setInvoice((int) id);
                                    Specification specification = new Specification();
                                    if (purchase.getCategory() == 39 || purchase.getCategory() == 20) {
                                        specification.setKey("IMEI");
                                        specification.setValue(stringArrayList.get(cnt));
                                    }

                                    if (purchase.getCategory_name().toLowerCase().equals("mobile hand sets") ||
                                            purchase.getCategory_name().toLowerCase().equals("tablets") ||
                                            purchase.getCategory_name().toLowerCase().equals("mobile & tablets")) {
                                        specification.setValue(stringArrayList.get(cnt));
                                    }
                                    purchase.setSpecification(specification);
                                    MyApplication.getDatabase().purchaseDao().insertPurchase(purchase);
                                }

                                requestInvoice = new RequestInvoice();
                                requestInvoice.setCustomer_name(invoice.getInvoiceName());
                                requestInvoice.setCustomer_email(invoice.getInvoiceEmail());
                                requestInvoice.setCustomer_mobile_no(invoice.getInvoiceMobileNo());
                                requestInvoice.setLocal_id((int) id);
                                requestInvoice.setDescription(txtSRNoOrDesc.getText().toString());
                                requestInvoice.setInvoice_no(invoice.getInvoice_no());
                                requestInvoice.setInvoiceDate(invoice.getInvoiceDate());
                                requestInvoice.setUser(invoice.getUser());
                                requestInvoice.setPaymentMethod1(invoice.getPaymentMethod1());
                                requestInvoice.setPaymentMethod1Amount(Float.parseFloat(paymentValue1.getText().toString()) - discountAmount);
                                if (paymentMethod2.getSelectedItemPosition() > 0 &&
                                        requestInvoice.getPaymentMethod1Amount() < finalAmount) {
                                    requestInvoice.setPaymentMethod2(paymentMethod2.getSelectedItem().toString());
                                    requestInvoice.setPaymentMethod2Amount(finalAmount - requestInvoice.getPaymentMethod1Amount());
                                }
                                requestInvoice.setPurchase(mBillingPurchaseArrayList);
                                requestInvoice.setGst_type(Integer.valueOf(gstType));
                                requestInvoice.setInvoiceState(invoice.getInvoiceState());
                                requestInvoice.setCustomerAddress(invoice.getInvoiceAddress());
                                requestInvoice.setCity(invoice.getInvoiceCity());
                                requestInvoice.setGstPercentage(Float.parseFloat("0" + edtGST.getText().toString()));
                                requestInvoice.setDiscountAmount(Integer.parseInt("0" + edtDiscount.getText().toString()));
                                requestInvoice.setTotal_amount(Float.parseFloat("0" + tvTotal.getText().toString()));
                                requestInvoice.setCreatedAt(format.format(invoiceDate));
                                mPrintInvoiceObj.setPurchaseArrayList(mBillingPurchaseArrayList);
                                mPrintInvoiceObj.setInvoice(invoice);
                                mPrintInvoiceObj.setAmountBeforeTax(String.format("%.2f", mAmountWithoutGST));
                                mPrintInvoiceObj.setGstAmount(String.format("%.2f", gstAmount));
                                mPrintInvoiceObj.setDiscountAmount(String.format("%.2f", discountAmount));
                                mPrintInvoiceObj.setCgst(edtCGST.getText().toString().isEmpty() ? 0 : Float.parseFloat(edtCGST.getText().toString()));
                                mPrintInvoiceObj.setSgst(edtSGST.getText().toString().isEmpty() ? 0 : Float.parseFloat(edtSGST.getText().toString()));
                                mPrintInvoiceObj.setIgst(edtIGST.getText().toString().isEmpty() ? 0 : Float.parseFloat(edtIGST.getText().toString()));
                                mPrintInvoiceObj.setCgstAmount(String.format("%.2f", mAmountWithGST * (mPrintInvoiceObj.getCgst() / 100)));
                                mPrintInvoiceObj.setSgstAmount(String.format("%.2f", mAmountWithGST * (mPrintInvoiceObj.getSgst() / 100)));
                                mPrintInvoiceObj.setIgstAmount(String.format("%.2f", mAmountWithGST * (mPrintInvoiceObj.getIgst() / 100)));
                                mPrintInvoiceObj.setTotal(String.format("%.2f", finalAmount));
                                mPrintInvoiceObj.setDiscountEnabled(edtDiscount.isEnabled());
                                mPrintInvoiceObj.setGSTEnabled(edtGST.isEnabled());
                                mPrintInvoiceObj.setStringArrayList(stringArrayList);
                                mPrintInvoiceObj.setDescription(txtSRNoOrDesc.getText().toString() + "");

                                AppRepository.getInstance().putInvoiceAPI(BillingActivity.this, requestInvoice,BillingActivity.this);
                            }
                        }).start();
                    }
                }

                break;

            case R.id.ibScanProduct:
                new IntentIntegrator(this).setOrientationLocked(false)
                        .initiateScan(); // `this` is the current Activity

                break;

            case R.id.ibSearchProduct:
                startActivity(new Intent(this, BillingAllProductListActivity.class));
                break;

            case R.id.ibAddIMEI:

                if (!edtIMEI.getText().toString().equalsIgnoreCase("") && edtIMEI.getText().length() > 0) {
                    stringArrayList.add(edtIMEI.getText().toString());
                    edtIMEI.setText("");
                    extraInputAdapter.notifyDataSetChanged();
                }
                break;

            case R.id.ibAddSr:

                if (!edtSR.getText().toString().equalsIgnoreCase("") && edtSR.getText().length() > 0) {
                    txtSRNoOrDesc.setVisibility(View.VISIBLE);
                    txtSRNoOrDesc.append(edtSR.getText().toString() + ", ");
                    edtSR.setText("");
                }

                break;

            case R.id.ibScanIMEI:
                scanIMEI();
                break;

            case R.id.ibGSTType:
                showGSTTypeDialog();
                break;
        }
    }

    @Override
    public void onResponseSuccess(Object o) {

        progressDialog.dismiss();
        final RequestInvoice requestInvoice = (RequestInvoice) o;
        if (o != null) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    for (Purchase purchase : requestInvoice.getPurchase()) {
                        purchase.setInvoice(requestInvoice.getId());
                        mPrintInvoiceObj.setPdfFile(requestInvoice.getPdf());
                        MyApplication.getDatabase().purchaseDao().insertPurchase(purchase);
                    }
                }
            });
            mPrintInvoiceObj.getInvoice().setId(requestInvoice.getId());
            mPrintInvoiceObj.setStrInvoiceNo(requestInvoice.getInvoice_no());

            Intent intentSendPDF = new Intent(BillingActivity.this, SendPDFActivity.class);
            intentSendPDF.putExtra("mPrintInvoiceObj", mPrintInvoiceObj);
            if (gstType.equalsIgnoreCase("1") &&
                    Float.parseFloat(mPrintInvoiceObj.getGstAmount() != null ? mPrintInvoiceObj.getGstAmount() : "0") > 0)
                gstType = "1";
            else
                gstType = "0";
            intentSendPDF.putExtra("showGST", gstType.equals("1") ? true : false);
            startActivity(intentSendPDF);
            isappstopped = true;
//            finish();
        }
    }

    @Override
    public void onResponseFailure() {
        Toast.makeText(this, "Internal Server Error", LENGTH_SHORT).show();
        progressDialog.dismiss();
        try {
            JSONArray jsonArray = new JSONArray(MyApplication.getInVoiceOffline());
            jsonArray.put(new Gson().toJson(requestInvoice));
            MyApplication.saveInVoiceOffline(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPrintInvoiceObj.setStrInvoiceNo(mPrintInvoiceObj.getInvoice().getInvoice_no());
        Intent intentSendPDF = new Intent(BillingActivity.this, SendPDFActivity.class);
        intentSendPDF.putExtra("mPrintInvoiceObj", mPrintInvoiceObj);
        if (gstType.equalsIgnoreCase("1") &&
                Float.parseFloat(mPrintInvoiceObj.getGstAmount() != null ? mPrintInvoiceObj.getGstAmount() : "0") > 0)
            gstType = "1";
        else
            gstType = "0";
        intentSendPDF.putExtra("showGST", gstType.equals("1") ? true : false);
        startActivity(intentSendPDF);
    }

    @SuppressLint("DefaultLocale")
    public void refreshAmount() {
        txtGSTTotal.setText(String.format("%.2f", calculateTotalTax()));
        finalAmount = grandTotal(mBillingPurchaseArrayList) - discountAmount;//+ gstAmount;
        mAmountWithoutGST = finalAmount - gstAmount;
        tvAmountBeforeTax.setText(String.format("%.2f", mAmountWithoutGST));
//        if (edtGST.isEnabled() && edtDiscount.isEnabled()) {
        tvTotal.setText((String.format("%.2f", finalAmount)));
        mAmountWithGST = finalAmount; //+ gstAmount;
       /* } else if (edtGST.isEnabled() && !edtDiscount.isEnabled()) {
            finalAmount = grandTotal(mBillingPurchaseArrayList) + gstAmount;
            tvTotal.setText((String.format("%.2f", finalAmount)));
        } else {
            finalAmount = grandTotal(mBillingPurchaseArrayList);
            tvTotal.setText((String.format("%.2f", finalAmount)));
        }*/
    }

    public void scanIMEI() {
        callFromAdpter = "callFromAdpter";
        new IntentIntegrator(this).setOrientationLocked(false)
                .initiateScan(); // `this` is the current Activity
    }

    public void showGSTTypeDialog() {

        bottomDialog = new BottomDialog.Builder(BillingActivity.this).setTitle("Select Bill Type").setIcon(R.mipmap.ic_launcher)
                .setCustomView(customView).show();
    }

    private float calculateTotalTax() {
        gstAmount = 0;
        for (Purchase purchase : mBillingPurchaseArrayList
        ) {
            gstAmount += purchase.getTaxAmount();
        }
        return gstAmount;
    }

    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isBillcreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.btnSubmit) {
                                SharedPreferences sharedPref =
                                        BillingActivity.this.getSharedPreferences(BillingActivity.this.getString(R.string.preference_file_key),
                                                BillingActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isBillcreenIntroShown", true);
                                editor.commit();
                            } else {
                                if (view.getId() == R.id.txtInvoiceDate) {
                                    startSpotLight(edtName, "Name", "Enter customer name.");
                                } else if (view.getId() == R.id.edtName) {
                                    startSpotLight(edtEmail, "Email", "Enter email address");
                                } else if (view.getId() == R.id.edtEmail) {
                                    startSpotLight(edtAddress, "Address", "Enter customer address");
                                } else if (view.getId() == R.id.edtAddress) {
                                    startSpotLight(spinnerState, "State", "Select State.");
                                } else if (view.getId() == R.id.edtAddress) {
                                    startSpotLight(spinnerState, "State", "Select State.");
                                } else if (view.getId() == R.id.spinnerState) {
                                    startSpotLight(edtCity, "City", "Select city.");
                                } else if (view.getId() == R.id.edtCity) {
                                    startSpotLight(edtMobNo, "Mobile No", "Enter mobile number.");
                                } else if (view.getId() == R.id.edtMobNo) {
                                    scrollview.scrollTo(0, paymentMethod1.getBottom());
                                    startSpotLight(paymentMethod1, "Payment Method 1", "Select payment method mode 1.");
                                } else if (view.getId() == R.id.paymentMethod1) {
                                    scrollview.scrollTo(0, paymentMethod2.getBottom());
                                    startSpotLight(paymentMethod2, "Payment Method 2", "Select payment method mode.");
                                } else if (view.getId() == R.id.paymentMethod2) {
                                    scrollview.scrollTo(0, ibSearchProduct.getBottom());
                                    startSpotLight(ibSearchProduct, "Search model", "Search model.");
                                } else if (view.getId() == R.id.ibSearchProduct) {
                                    startSpotLight(ibScanProduct, "Scan product", "Scan product for billing.");
                                } else if (view.getId() == R.id.ibScanProduct) {
                                    startSpotLight(ibAddProducts, "Add product", "Add product.");
                                } else if (view.getId() == R.id.ibAddProducts) {
                                    scrollview.scrollTo(0, edtDiscount.getBottom());
                                    startSpotLight(edtDiscount, "Discount", "Enter discount in Rs.");
                                } else if (view.getId() == R.id.edtDiscount) {
                                    scrollview.scrollTo(0, btnSubmit.getBottom());
                                    startSpotLight(btnSubmit, "Submit", "Submit invoice.");
                                }/*&else if(view.getId()==R.id.spinnerState){
                                    startSpotLight(edtCity,"State","Select State.");
                                }else if(view.getId()==R.id.spinnerState){
                                    startSpotLight(edtCity,"State","Select State.");
                                }else if(view.getId()==R.id.spinnerState){
                                    startSpotLight(edtCity,"State","Select State.");
                                }else
                                {
                                    startSpotLight(btnGetSalesReport,"Sales Report","This will be used for sending the sales report to your registred email");
                                }*/
                            }
                        }
                    })
                    .build()
                    .show();


        }
    }

    private void permissionForLocation() {
        int hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                    return;
                }

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {

        // Acquire a reference to the system Location Manager

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); //LocationManager.GPS_PROVIDER
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener); //LocationManager.GPS_PROVIDER

//        locationManager.removeUpdates(locationListener);
    }

    private class LocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                final List<Address> addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            edtCity.setText(addresses.get(0).getLocality());
                            if (MyApplication.getStateList().contains(addresses.get(0).getAdminArea()))
                                spinnerState.setSelection(MyApplication.getStateList().indexOf(addresses.get(0).getAdminArea()));
                        }
                    });
                    locationManager.removeUpdates(locationListener);

                }


            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
