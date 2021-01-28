package com.billbook.app.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.billbook.app.R;
import com.billbook.app.adapters.LedgerDetailAdapter;
import com.billbook.app.adapters.SellingDetailAdapter;
import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndCount;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.WebserviceResponseHandler;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.utils.Util;
import com.billbook.app.viewmodel.SellingDetailViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class SellingDetailActivity extends AppCompatActivity implements View.OnClickListener, WebserviceResponseHandler {

    private static final String TAG = "SellingDetailActivity";
    int selectedDayspos = 0;
    boolean isBrandsUpdate = false;
    BarChart chart;
    ArrayList<BarEntry> BARENTRY = new ArrayList<>();
    ArrayList<String> BarEntryLabels = new ArrayList<>();
    BarDataSet Bardataset;
    BarData BARDATA;
    private SellingDetailViewModel mSellingDetailViewModel;
    private Spinner mSpinnerCategory, mSpinnerBrand, mSpinnerDaysFilter;//mSpinnerProduct;
    private RecyclerView recyclerViewFilter;
    private TextView tvFromDate, tvToDate, tvProductNameHeader, tvProductPriceHeader, tvTotalPrice;
    private ProgressDialog progressDialog;
    private ArrayList<Category> categoryArrayList;
    private ArrayList<Brand> brandArrayList = new ArrayList<>();
    private ArrayList<Product> productArrayList;
    private ArrayList<String> filterDaysList;
    private String selctedFromDate, selctedToDate;
    private JSONArray searchResult = new JSONArray();
    private SellingDetailAdapter mSellingDetailAdapter;
    private int mSelectedCategoryId = 0;
    private int mSelectedBrandId = 0;
    private int mSelectedProductId;
    private long selectedFromDateLong, selectedToDateLong;
    private String mSelectedCategoryName;
    private String mSelectedBrandName;
    private String mSelectedProductName;
    private User user;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selling_detail);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mSellingDetailViewModel = ViewModelProviders.of(this).get(SellingDetailViewModel.class);
        initUI();
        user = MyApplication.getUser();
        startSpotLight(tvFromDate, "Select Date", "Select from date");
        getDataFromServer(mSelectedCategoryId, mSelectedBrandId);

    }

    private void initUI() {
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        tvProductNameHeader = findViewById(R.id.tvProductNameHeader);
        tvProductPriceHeader = findViewById(R.id.tvProductPriceHeader);
        tvProductNameHeader.setText("Brands");
        tvProductPriceHeader.setVisibility(View.GONE);

        tvFromDate.setText(Util.getTodaysdate());
        tvToDate.setText(Util.getToDateBack(-1));
        selctedToDate = Util.getToDateBack(-1);
        selctedFromDate = Util.getTodaysdate();

        tvFromDate.setOnClickListener(this);
        tvToDate.setOnClickListener(this);

        recyclerViewFilter = findViewById(R.id.recyclerViewFilter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewFilter.setLayoutManager(mLayoutManager);
        recyclerViewFilter.setItemAnimator(new DefaultItemAnimator());


        mSpinnerCategory = findViewById(R.id.spinnerCategory);
        mSpinnerBrand = findViewById(R.id.spinnerBrand);
        mSpinnerDaysFilter = findViewById(R.id.spinnerFilter);

        chart = findViewById(R.id.barchart);

        BARENTRY = new ArrayList<>();

        BarEntryLabels = new ArrayList<String>();
        filterDaysList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.dayFilter_array)));
        ArrayAdapter<String> daysFilterAdapter = new ArrayAdapter<>(SellingDetailActivity.this, android.R.layout.simple_spinner_item, filterDaysList);
        daysFilterAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
        mSpinnerDaysFilter.setAdapter(daysFilterAdapter);

        mSellingDetailViewModel.getCategoriesList().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categoriesList) {
                categoryArrayList = (ArrayList<Category>) categoriesList;
                Category category = new Category();
                category.setName("Select Category");
                categoryArrayList.add(0, category);
                ArrayAdapter<Category> myAdapter = new ArrayAdapter<>(SellingDetailActivity.this, android.R.layout.simple_spinner_item, categoryArrayList);
                myAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                mSpinnerCategory.setAdapter(myAdapter);
            }
        });

        mSpinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.v(TAG, "inside mSpinnerCategory onItemSelected pos::" + pos);
//                if(pos!=0)
//                {
                mSelectedCategoryId = categoryArrayList.get(pos).getId();
//                    setFilterData(selectedDayspos, mSelectedCategoryId, 1);


//                }

                mSellingDetailViewModel.getBrandListByCategoryID(mSelectedCategoryId).observe(SellingDetailActivity.this, new Observer<List<Brand>>() {
                    @Override
                    public void onChanged(@Nullable List<Brand> productsList) {
                        //mSelectedBrandId = 0;
                        brandArrayList.clear();
                        Brand brand = new Brand();
                        brand.setName("Select Brand");
                        brandArrayList.add(0, brand);
                        if (brandArrayList != null && brandArrayList.size() > 0) {
                            //mSelectedBrandId = brandArrayList.get(0).getId();
                            //mSelectedBrandName = brandArrayList.get(0).getName();
                        }
                        JSONArray selectedBrands = null;
                        try {
                            selectedBrands = new JSONArray(user.getBrand() == null ? "" : user.getBrand());
                            if (selectedBrands.length() == 0)
                                brandArrayList = (ArrayList<Brand>) productsList;
                            else {
                                for (int i = 0; i < selectedBrands.length(); i++) {
                                    for (Brand brand1 : productsList
                                    ) {
                                        if (brand1.getId() == selectedBrands.getInt(i))
                                            brandArrayList.add(brand1);
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        ArrayAdapter<Brand> myAdapter = new ArrayAdapter<>(SellingDetailActivity.this,
                                android.R.layout.simple_spinner_item, brandArrayList);
                        myAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                        mSpinnerBrand.setAdapter(myAdapter);
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        mSpinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                Log.v(TAG, "inside mSpinnerBrand onItemSelected pos::" + pos);
//                if(pos!=0)
//                {
                mSelectedBrandId = brandArrayList.get(pos).getId();
//                    setFilterData(selectedDayspos, mSelectedBrandId, 2);

//                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        mSpinnerDaysFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                selectedDayspos = 0;
                switch (pos) {
                    case 0:
                        setFilterData(0, 0, 4);
                        break;

                    case 1:
                        setFilterData(6, 0, 4);
                        break;
                    case 2:
                        setFilterData(29, 0, 4);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    void setFilterData(int daysBack, int filterId, int filterfor) {
     /*  String fromDate = Util.getTodaysdate();
       Log.v(TAG, "fromDate" + fromDate);
       String toDate = Util.getToDateBack(daysBack);
       Log.v(TAG, "toDate" + toDate);*/
        String fromDate = selctedFromDate;
        String toDate = selctedToDate;
        switch (filterfor) {
            case 1://cat
                MyApplication.getDatabase().invoiceDao().fetchInvoiceBetweenDateNewForCategory(fromDate, toDate, filterId).observe(SellingDetailActivity.this, new Observer<List<ProductAndCount>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndCount> productAndCountList) {
                        Log.v(TAG, "productAndCountList::" + productAndCountList);
                        tvProductNameHeader.setText("Brands");
                        tvProductPriceHeader.setVisibility(View.GONE);
                        if (productAndCountList != null) {
                            mSellingDetailAdapter = new SellingDetailAdapter((ArrayList<ProductAndCount>) productAndCountList, false);
                            recyclerViewFilter.setAdapter(mSellingDetailAdapter);


                            if (productAndCountList.size() > 0) {
                                float priceTotal = 0;
                                for (ProductAndCount productAndCount : productAndCountList) {
                                    Log.v(TAG, "productAndCount getPrice()::" + productAndCount.getPrice());
                                    priceTotal = priceTotal + productAndCount.getPrice();
                                }
                                setTotalPrice(priceTotal);
                            } else {
                                setTotalPrice(0);
                            }


                        } else {
                            setTotalPrice(0);
                        }
                    }
                });
                break;
            case 2:
                //brand
                MyApplication.getDatabase().invoiceDao().fetchInvoiceBetweenDateNewForBrand(fromDate, toDate, filterId).observe(SellingDetailActivity.this, new Observer<List<ProductAndCount>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndCount> productAndCountList) {
                        Log.v(TAG, "productAndCountList::" + productAndCountList);
                        tvProductNameHeader.setText("Product");
                        tvProductPriceHeader.setVisibility(View.VISIBLE);
                        if (productAndCountList != null) {

                            mSellingDetailAdapter = new SellingDetailAdapter((ArrayList<ProductAndCount>) productAndCountList, true);
                            recyclerViewFilter.setAdapter(mSellingDetailAdapter);
                            if (productAndCountList.size() > 0) {
                                float priceTotal = 0;
                                for (ProductAndCount productAndCount : productAndCountList) {
                                    Log.v(TAG, "productAndCount getPrice()::" + productAndCount.getPrice());
                                    priceTotal = priceTotal + productAndCount.getPrice();
                                }
                                setTotalPrice(priceTotal);
                            } else {
                                setTotalPrice(0);
                            }
                        } else {
                            setTotalPrice(0);
                        }
                    }
                });
                break;
            case 3:
                //product
                MyApplication.getDatabase().invoiceDao().fetchInvoiceBetweenDateNewForProduct(fromDate, toDate, filterId).observe(SellingDetailActivity.this, new Observer<List<ProductAndCount>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndCount> productAndCountList) {
                        Log.v(TAG, "productAndCountList::" + productAndCountList);
                        if (productAndCountList != null) {

                            mSellingDetailAdapter = new SellingDetailAdapter((ArrayList<ProductAndCount>) productAndCountList, true);
                            recyclerViewFilter.setAdapter(mSellingDetailAdapter);
                        }
                    }
                });
                break;
            case 4:
                //days
                MyApplication.getDatabase().invoiceDao().fetchInvoiceBetweenDateNew(fromDate, toDate).observe(SellingDetailActivity.this, new Observer<List<ProductAndCount>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndCount> productAndCountList) {
                        Log.v(TAG, "productAndCountList::" + productAndCountList);
                        if (productAndCountList != null) {

                            mSellingDetailAdapter = new SellingDetailAdapter((ArrayList<ProductAndCount>) productAndCountList, true);
                            recyclerViewFilter.setAdapter(mSellingDetailAdapter);
                        }
                    }
                });
                break;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvFromDate:
                showDatePickerDialog(1);
                break;
            case R.id.tvToDate:
                showDatePickerDialog(2);
                break;
        }
    }

    private void showDatePickerDialog(final int forDate) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(SellingDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
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


                if (forDate == 1) {
                    tvFromDate.setText(dateToUse);
                    selctedFromDate = dateToUse;
                } else {
                    selctedToDate = dateToUse;
                    tvToDate.setText(dateToUse);


                }
                Log.v(TAG, "selctedFromDate::" + selctedFromDate + " selctedToDate::" + selctedToDate);

                if (validateDate(selctedFromDate, selctedToDate)) {
                    //setFilterData(selectedDayspos,mSelectedCategoryId,1);
                    mSpinnerCategory.setSelection(0);
                    mSpinnerBrand.setSelection(0);


                } else {
                    DialogUtils.showToast(SellingDetailActivity.this, "From Date should be less than To Date");
                }

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean validateDate(String selctedFromDate, String selctedToDate) {
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        boolean isValidate = true;
        try {
            Date selFrDate = myFormat.parse(selctedFromDate);
            Date selToDate = myFormat.parse(selctedToDate);
            Log.v(TAG, "selFrDate::" + selFrDate.getTime());
            Log.v(TAG, "selToDate::" + selToDate.getTime());
            if (selFrDate.getTime() > selToDate.getTime()) {
                isValidate = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            isValidate = false;
        }
        Log.v(TAG, "isValidate::" + isValidate);
        return isValidate;
    }

    void setTotalPrice(float totalPrice) {
        tvTotalPrice.setText(String.format("%.2f", totalPrice));
    }

    public void searchLedger(View view) {
        getDataFromServer(mSelectedCategoryId, mSelectedBrandId);
    }

    public void getDataFromServer(int mSelectedCategoryId, int mSelectedBrandId) {
        try {
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date selFrDate = myFormat.parse(selctedFromDate);
            Date selToDate = myFormat.parse(selctedToDate);
            selectedFromDateLong = selFrDate.getTime() / 1000;
            selectedToDateLong = selToDate.getTime() / 1000;
            progressDialog = DialogUtils.startProgressDialog(this, "");
            AppRepository.getInstance().getLedgerDetails(this, selectedFromDateLong, selectedToDateLong, mSelectedCategoryId, mSelectedBrandId, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponseSuccess(Object o) {
        progressDialog.dismiss();
        float totalAmount = 0;
        int totalQty = 0;
        if (o != null) {
            try {
                searchResult = new JSONArray();
                JSONObject response = new JSONObject(o.toString());
                Iterator<String> iter = response.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        JSONObject value = response.getJSONObject(key);
                        totalAmount += value.getDouble("totalPrice");
                        totalQty += value.getInt("qty");
                        searchResult.put(value);
                        tvTotalPrice.setText("" + totalAmount);
                    } catch (JSONException e) {
                        // Something went wrong!
                    }
                }
                LedgerDetailAdapter ledgerDetailAdapter = new LedgerDetailAdapter(searchResult);
                recyclerViewFilter.setAdapter(ledgerDetailAdapter);
//                showGraph(null);
                processData();
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }
    }

    @Override
    public void onResponseFailure() {

    }

    public void showGraph(View v) {
        if (searchResult.length() > 0) {
            Intent intent = new Intent(this, BarGraph.class);
            intent.putExtra("data", searchResult.toString());
            startActivity(intent);
        }
    }

    private void processData() {
        try {
            BARENTRY = new ArrayList<>();
//            brandArrayList.clear();
            BarEntryLabels = new ArrayList<>();
            for (int i = 0; i < searchResult.length(); i++) {
                BARENTRY.add(new BarEntry(i + 1, searchResult.getJSONObject(i).getInt("totalPriceAfterGST")));
                BarEntryLabels.add(searchResult.getJSONObject(i).getString("name"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        Bardataset = new BarDataSet(BARENTRY, "Sales Report");
        Bardataset.setColors(ColorTemplate.COLORFUL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(Bardataset);

        BARDATA = new BarData(dataSets);
        chart.setPinchZoom(true);
        chart.setData(BARDATA);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis()
                .setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(BarEntryLabels));
        chart.animateY(1000);

    }

    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isLedgerscreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.btnSearchLedger) {
                                SharedPreferences sharedPref =
                                        SellingDetailActivity.this.getSharedPreferences(SellingDetailActivity.this.getString(R.string.preference_file_key),
                                                SellingDetailActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isLedgerscreenIntroShown", true);
                                editor.commit();
                            } else {
                                if (view.getId() == R.id.tvFromDate) {
                                    startSpotLight(tvToDate, "From Date", "Select to date.");
                                } else if (view.getId() == R.id.tvToDate) {
                                    startSpotLight(mSpinnerCategory, "Select Category", "Select category for filtering ledger details.");
                                } else if (view.getId() == R.id.spinnerCategory) {
                                    startSpotLight(mSpinnerBrand, "Select Brand", "Select brand for filtering ledger details.");
                                } else if (view.getId() == R.id.spinnerBrand) {
                                    startSpotLight(findViewById(R.id.btnSearchLedger), "Submit", "Click to get ledger details");
                                }
                            }
                        }
                    })
                    .build()
                    .show();
        }
    }
}
