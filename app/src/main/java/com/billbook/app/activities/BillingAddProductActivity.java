package com.billbook.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Inventory;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.database.models.User;
import com.billbook.app.utils.Util;
import com.billbook.app.viewmodel.AddProductViewModel;
import com.billbook.app.viewmodel.BillingManager;

import java.util.ArrayList;
import java.util.Objects;

public class BillingAddProductActivity extends AppCompatActivity {
    private static final String TAG = "AddProductActivity";
    ArrayList<Brand> brandArrayList;
    ArrayList<Product> productArrayList;
    int mSelectedCategoryId;
    int mSelectedBrandId;
    int mSelectedProductId;
    String mSelectedCategoryName;
    String mSelectedBrandName;
    String mSelectedProductName;
    private Spinner mSpinnerCategory, mSpinnerBrand, mSpinnerProduct;
    private Button mBtnSubmit;
    private EditText edtRate, edtQuantities, edtDiscount;
    private TextView txtAmount;
    private AddProductViewModel mAddProductViewModel;
    private float gstValue, IgstValue;
    private EditText gst, Igst;
    private int userId;
    private ProductAndInventory productAndInventory;
    private Purchase purchaseObj;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_sheet_add_product);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();
    }

    private void initUI() {

        edtRate = findViewById(R.id.edtRate);
        edtQuantities = findViewById(R.id.edtQuantities);
        edtDiscount = findViewById(R.id.edtDiscount);
        txtAmount = findViewById(R.id.txtAmount);

        mSpinnerCategory = findViewById(R.id.spinnerCategory);
        mSpinnerBrand = findViewById(R.id.spinnerBrand);
        mSpinnerProduct = findViewById(R.id.spinnerProduct);
        mBtnSubmit = findViewById(R.id.btn_submit);

        mSpinnerCategory.setEnabled(false);
        mSpinnerBrand.setEnabled(false);
        mSpinnerProduct.setEnabled(false);
        gst = findViewById(R.id.edtGST);
        Igst = findViewById(R.id.edtIGST);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref =
                        getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
                                MODE_PRIVATE);
                int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
                User user = MyApplication.getDatabase().userDao().getUser(userId);
//        TextView titleGST = findViewById(R.id.titleGST);
                if (user.getGst_no() == null || user.getGst_no().isEmpty()) {
                    gst.setVisibility(View.GONE);
                    Igst.setVisibility(View.GONE);
//          DialogUtils.showToast(BillingAddProductActivity.this,"To Enable GST option please update your gst number in  your profile");
                } else {
                    gst.setVisibility(View.VISIBLE);
                    Igst.setVisibility(View.VISIBLE);
                }
                Log.v(TAG, "user::" + user);
            }
        }).start();

        gst.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty())
                    Igst.setEnabled(false);
                else
                    Igst.setEnabled(true);

            }
        });

        Igst.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty())
                    gst.setEnabled(false);
                else
                    gst.setEnabled(true);

            }
        });

        //edtQuantities.addTextChangedListener(new TextWatcher() {
        //  @SuppressLint("SetTextI18n") @Override
        //  public void onTextChanged(CharSequence s, int start, int before, int count) {
        //
        //    if (edtQuantities.getText().toString().trim().equals("")) {
        //      return;
        //    }
        //
        //    if (count > 0) {
        //      int qty = Integer.parseInt(edtQuantities.getText().toString().trim());
        //      float rate = Integer.parseInt(edtRate.getText().toString().trim());
        //      float discountAmount = Util.calculateDiscountAmount(
        //          Float.parseFloat(edtDiscount.getText().toString().trim()), rate);
        //
        //      if (qty > 0 && rate > 0 && discountAmount >= 0) {
        //        txtAmount.setText(qty * (rate - discountAmount) + "");
        //      }
        //    }
        //  }
        //
        //  @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //    txtAmount.setText("0");
        //    // TODO Auto-generated method stub
        //  }
        //
        //  @Override public void afterTextChanged(Editable s) {
        //
        //    // TODO Auto-generated method stub
        //  }
        //});
        //
        //edtRate.addTextChangedListener(new TextWatcher() {
        //  @SuppressLint("SetTextI18n") @Override
        //  public void onTextChanged(CharSequence s, int start, int before, int count) {
        //
        //    if (edtRate.getText().toString().trim().equals("")) {
        //      return;
        //    }
        //
        //    if (count > 0) {
        //      int qty = Integer.parseInt(edtQuantities.getText().toString().trim());
        //      float rate = Integer.parseInt(edtRate.getText().toString().trim());
        //      float discountAmount = Util.calculateDiscountAmount(
        //          Float.parseFloat(edtDiscount.getText().toString().trim()), rate);
        //
        //      if (qty > 0 && rate > 0 && discountAmount >= 0) {
        //        txtAmount.setText(qty * (rate - discountAmount) + "");
        //      }
        //    }
        //  }
        //
        //  @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //    txtAmount.setText("0");
        //    // TODO Auto-generated method stub
        //  }
        //
        //  @Override public void afterTextChanged(Editable s) {
        //
        //    // TODO Auto-generated method stub
        //  }
        //});
        //
        //edtDiscount.addTextChangedListener(new TextWatcher() {
        //  @SuppressLint("SetTextI18n") @Override
        //  public void onTextChanged(CharSequence s, int start, int before, int count) {
        //
        //    if (count > 0) {
        //      int qty = Integer.parseInt(edtQuantities.getText().toString().trim());
        //      float rate = Float.parseFloat(edtRate.getText().toString().trim());
        //      float discountAmount = Util.calculateDiscountAmount(
        //          Float.parseFloat(edtDiscount.getText().toString().trim()), rate);
        //
        //      if (qty > 0 && rate > 0 && discountAmount >= 0) {
        //        txtAmount.setText(qty * (rate - discountAmount) + "");
        //      }
        //    }
        //  }
        //
        //  @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //    txtAmount.setText("0");
        //    // TODO Auto-generated method stub
        //  }
        //
        //  @Override public void afterTextChanged(Editable s) {
        //
        //    // TODO Auto-generated method stub
        //  }
        //});

        if (getIntent().hasExtra("productAndInventory")) {
            productAndInventory =
                    (ProductAndInventory) getIntent().getSerializableExtra("productAndInventory");

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    final Category category = MyApplication.getDatabase()
                            .categoriesDao()
                            .loadCategoryByCategoryId(productAndInventory.getProduct().getCategory());
                    final Brand brand = MyApplication.getDatabase()
                            .brandDao()
                            .getBrandByProduct(productAndInventory.getProduct().getBrand());

                    final Product product = MyApplication.getDatabase()
                            .productDao()
                            .getProduct(productAndInventory.getProduct().getId());


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<Category> categoryAdapter = null;
                            if (category != null) {
                                ArrayList<Category> categoryArrayList = new ArrayList<>();
                                categoryArrayList.add(category);

                                categoryAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                        android.R.layout.simple_spinner_item, categoryArrayList);
                                categoryAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                            }
                            mSpinnerCategory.setAdapter(categoryAdapter);
                            mSpinnerCategory.setSelection(0);

                            ArrayAdapter<Brand> brandAdapter = null;
                            if (brand != null) {
                                ArrayList<Brand> categoryArrayList = new ArrayList<>();
                                categoryArrayList.add(brand);
                                brandAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                        android.R.layout.simple_spinner_item, categoryArrayList);
                                brandAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                            }
                            mSpinnerBrand.setAdapter(brandAdapter);
                            mSpinnerBrand.setSelection(0);

                            ArrayAdapter<Product> productAdapter = null;
                            if (productAndInventory != null) {
                                ArrayList<Product> categoryArrayList = new ArrayList<>();
                                categoryArrayList.add(product);
                                productAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                        android.R.layout.simple_spinner_item, categoryArrayList);
                                productAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                            }
                            mSpinnerProduct.setAdapter(productAdapter);
                            mSpinnerProduct.setSelection(0);
                        }
                    });
                }
            });

            mBtnSubmit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (gst.getText().toString().isEmpty()) {
                        gstValue = 0;
                    } else
                        gstValue = Float.parseFloat(gst.getText().toString());
                    if (Igst.getText().toString().isEmpty()) {
                        IgstValue = 0;
                    } else
                        IgstValue = Float.parseFloat(Igst.getText().toString());
                    Log.v(TAG, "mSelectedCategoryId::" + mSelectedCategoryId);
                    Log.v(TAG, "mSelectedBrandId::" + mSelectedBrandId);
                    Log.v(TAG, "mSelectedProductId::" + mSelectedProductId);
                    String rateString = edtRate.getText().toString().trim();
                    String quantitiesString = edtQuantities.getText().toString().trim();
                    userId = MyApplication.getUserID();

                    if (rateString.isEmpty() || rateString.equalsIgnoreCase("0")) {
                        Toast.makeText(BillingAddProductActivity.this, "Please enter Price.", Toast.LENGTH_LONG)
                                .show();
                    } else if (quantitiesString.isEmpty() || quantitiesString.equalsIgnoreCase("0")) {
                        Toast.makeText(BillingAddProductActivity.this, "Please enter Quantity.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "" + quantitiesString + "  " + rateString);
                        final int qty = Integer.parseInt(quantitiesString);
                        final float rate = Float.parseFloat(rateString);
                        final float discountAmount = Util.calculateDiscountAmount(
                                Float.parseFloat(edtDiscount.getText().toString().trim()), rate);

                        Log.d(TAG, "" + qty + "  " + rate);

                        if (productAndInventory.getInventoryList().size() > 0) {
                            Inventory inventory = productAndInventory.getInventoryList().get(0);

                            Purchase purchase = new Purchase();
                            purchase.setIgst(IgstValue);
                            purchase.setGst(gstValue);
                            purchase.setTax(IgstValue + gstValue);
                            float total = rate * qty;
                            float igst = (total - (total / (1 + ((IgstValue) / 100))));
                            float gst = (total - (total / (1 + ((gstValue) / 100))));
                            purchase.setTaxAmount(igst + gst);
//              purchase.setTaxAmount(total - (total/(1+((IgstValue+gstValue)/100))));
                            purchase.setBrand(inventory.getBrand());
                            purchase.setBrand_name(inventory.getBrand_name());
                            purchase.setCategory(inventory.getCategory());
                            purchase.setProduct(inventory.getProduct());
                            purchase.setCategory_name(inventory.getCategory_name());
                            purchase.setProduct_name(inventory.getProduct_name());
                            purchase.setSerial_no(inventory.getSerial_no());
                            purchase.setUser(userId);
                            purchase.setQuantity(qty);
                            purchase.setPrice(rate);
                            purchase.setDiscount(discountAmount);

                            BillingManager.getInstance().getCurrentPurchaseList().add(purchase);
                            finish();
                            //delete purchase product from inventory
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = 0; i < qty; i++) {
                                        Inventory inventoryToDelete = productAndInventory.getInventoryList().get(i);
                                        MyApplication.getDatabase().inventoryDao().delete(inventoryToDelete);
                                    }
                                }
                            });
                        } else {

                            //TODO Temporary Change for Client

                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    Brand brand = MyApplication.getDatabase()
                                            .brandDao()
                                            .getBrandByProduct(productAndInventory.getProduct().getBrand());

                                    Category category = MyApplication.getDatabase()
                                            .categoriesDao()
                                            .loadCategoryByCategoryId(productAndInventory.getProduct().getCategory());

                                    final Purchase purchase = new Purchase();
                                    purchase.setIgst(IgstValue);
                                    purchase.setGst(gstValue);
                                    purchase.setTax(IgstValue + gstValue);
                                    float total = rate * qty;
                                    float igst = (total - (total / (1 + ((IgstValue) / 100))));
                                    float gst = (total - (total / (1 + ((gstValue) / 100))));
                                    purchase.setTaxAmount(igst + gst);
//                  purchase.setTaxAmount(total - (total/(1+((IgstValue+gstValue)/100))));
                                    purchase.setBrand(productAndInventory.getProduct().getBrand());
                                    purchase.setBrand_name(brand.getName());
                                    purchase.setCategory(productAndInventory.getProduct().getCategory());
                                    purchase.setCategory_name(category.getName());
                                    purchase.setProduct(productAndInventory.getProduct().getId());
                                    purchase.setProduct_name(productAndInventory.getProduct().getName());
                                    purchase.setSerial_no(productAndInventory.getProduct().getItem_id());
                                    purchase.setUser(userId);
                                    purchase.setQuantity(qty);
                                    purchase.setPrice(rate);
                                    purchase.setDiscount(discountAmount);

                                    Log.d(TAG, "Temporary Change" + purchase.toString());
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "Temporary Change2" + purchase.toString());
                                            BillingManager.getInstance().getCurrentPurchaseList().add(purchase);
                                            finish();
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                }
            });
        } else if (getIntent().hasExtra("inventory") || getIntent().hasExtra("product")) {
            final Inventory inventory = getIntent().hasExtra("inventory") ? (Inventory) getIntent().getSerializableExtra("inventory") : null;
            final Product mProduct = getIntent().hasExtra("product") ?
                    ((Product) getIntent().getSerializableExtra("product")) : null;

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    Category category = MyApplication.getDatabase()
                            .categoriesDao()
                            .loadCategoryByCategoryId(inventory != null ? inventory.getCategory() : mProduct.getCategory());
                    Brand brand =
                            MyApplication.getDatabase().brandDao().getBrandByProduct(inventory != null ? inventory.getBrand() : mProduct.getBrand());

                    Product product = null;
                    if (inventory != null)
                        product = MyApplication.getDatabase().productDao().getProduct(inventory.getProduct_id());
                    else
                        product = mProduct;


                    mSelectedBrandId = brand.getId();
                    mSelectedBrandName = brand.getName();

                    mSelectedCategoryId = category.getId();
                    mSelectedCategoryName = category.getName();

                    mSelectedProductId = mProduct.getId();
                    mSelectedProductName = mProduct.getName();

                    ArrayAdapter<Category> categoryAdapter = null;
                    if (category != null) {
                        ArrayList<Category> categoryArrayList = new ArrayList<>();
                        categoryArrayList.add(category);

                        categoryAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                android.R.layout.simple_spinner_item, categoryArrayList);
                        categoryAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                    }
                    mSpinnerCategory.setAdapter(categoryAdapter);
                    mSpinnerCategory.setSelection(0);

                    ArrayAdapter<Brand> brandAdapter = null;
                    if (brand != null) {
                        ArrayList<Brand> categoryArrayList = new ArrayList<>();
                        categoryArrayList.add(brand);
                        brandAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                android.R.layout.simple_spinner_item, categoryArrayList);
                        brandAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                    }
                    mSpinnerBrand.setAdapter(brandAdapter);
                    mSpinnerBrand.setSelection(0);

                    ArrayAdapter<Product> productAdapter = null;
                    if (product != null) {
                        ArrayList<Product> categoryArrayList = new ArrayList<>();
                        categoryArrayList.add(product);
                        productAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                android.R.layout.simple_spinner_item, categoryArrayList);
                        productAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                    }
                    mSpinnerProduct.setAdapter(productAdapter);
                    mSpinnerProduct.setSelection(0);
                    if (inventory != null)
                        edtRate.setText("" + inventory.getSelling_price());

                }
            });

            mBtnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (gst.getText().toString().isEmpty()) {
                        gstValue = 0;
                    } else
                        gstValue = Float.parseFloat(gst.getText().toString());
                    if (Igst.getText().toString().isEmpty()) {
                        IgstValue = 0;
                    } else
                        IgstValue = Float.parseFloat(Igst.getText().toString());
                    Log.v(TAG, "mSelectedCategoryId::" + mSelectedCategoryId);
                    Log.v(TAG, "mSelectedBrandId::" + mSelectedBrandId);
                    Log.v(TAG, "mSelectedProductId::" + mSelectedProductId);
                    String rateString = edtRate.getText().toString().trim();
                    String quantitiesString = edtQuantities.getText().toString().trim();
                    userId = MyApplication.getUserID();

                    if (rateString.isEmpty()
                            || rateString.equalsIgnoreCase("0")
                            || quantitiesString.isEmpty()
                            || quantitiesString.equalsIgnoreCase("0")) {
                        Toast.makeText(BillingAddProductActivity.this, "Please enter valid details first.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "" + quantitiesString + "  " + rateString);
                        final int qty = Integer.parseInt(quantitiesString);
                        final float rate = Float.parseFloat(rateString);
                        final float discountAmount = Util.calculateDiscountAmount(
                                Float.parseFloat(edtDiscount.getText().toString().trim()), rate);

                        Log.d(TAG, "" + qty + "  " + rate);

                        Purchase purchase = new Purchase();
                        purchase.setIgst(IgstValue);
                        purchase.setGst(gstValue);
                        purchase.setTax(IgstValue + gstValue);
                        float total = rate * qty;
                        float igst = (total - (total / (1 + ((IgstValue) / 100))));
                        float gst = (total - (total / (1 + ((gstValue) / 100))));
                        purchase.setTaxAmount(igst + gst);
//            purchase.setTaxAmount(total - (total/(1+((IgstValue+gstValue)/100))));
                        purchase.setBrand(mSelectedBrandId);
                        purchase.setBrand_name(mSelectedBrandName);
                        purchase.setCategory(mSelectedCategoryId);
                        purchase.setCategory_name(mSelectedCategoryName);
                        purchase.setProduct(mSelectedProductId);
                        purchase.setProduct_name(mSelectedProductName);
                        purchase.setSerial_no(inventory != null ? inventory.getSerial_no() : mProduct.getBarcode());
                        purchase.setUser(userId);
                        purchase.setQuantity(qty);
                        purchase.setPrice(rate);
                        purchase.setDiscount(discountAmount);

                        BillingManager.getInstance().getCurrentPurchaseList().add(purchase);
                        finish();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                Inventory inventory = (Inventory) getIntent().getSerializableExtra("inventory");
                                if (inventory != null) {
                                    ArrayList<Inventory> inventories = (ArrayList<Inventory>) MyApplication.getDatabase().inventoryDao().getListInventoryBySerialNo(inventory.getSerial_no());
                                    for (int i = 0; i < qty; i++) {
                                        MyApplication.getDatabase().inventoryDao().delete(inventories.get(i));
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } else if (getIntent().hasExtra("purchaseObj")) {

            purchaseObj = (Purchase) getIntent().getSerializableExtra("purchaseObj");

            edtRate.setText(purchaseObj.getPrice() + "");
            edtQuantities.setText(purchaseObj.getQuantity() + "");

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {

                    Category category = MyApplication.getDatabase()
                            .categoriesDao()
                            .loadCategoryByCategoryId(purchaseObj.getCategory());
                    Brand brand =
                            MyApplication.getDatabase().brandDao().getBrandByProduct(purchaseObj.getBrand());

                    Product product =
                            MyApplication.getDatabase().productDao().getProduct(purchaseObj.getProduct_id());

                    ArrayAdapter<Category> categoryAdapter = null;
                    if (category != null) {
                        ArrayList<Category> categoryArrayList = new ArrayList<>();
                        categoryArrayList.add(category);

                        categoryAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                android.R.layout.simple_spinner_item, categoryArrayList);
                        categoryAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                    }
                    mSpinnerCategory.setAdapter(categoryAdapter);
                    mSpinnerCategory.setSelection(0);

                    ArrayAdapter<Brand> brandAdapter = null;
                    if (brand != null) {
                        ArrayList<Brand> categoryArrayList = new ArrayList<>();
                        categoryArrayList.add(brand);
                        brandAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                android.R.layout.simple_spinner_item, categoryArrayList);
                        brandAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                    }
                    mSpinnerBrand.setAdapter(brandAdapter);
                    mSpinnerBrand.setSelection(0);

                    ArrayAdapter<Product> productAdapter = null;
                    if (purchaseObj != null) {
                        ArrayList<Product> categoryArrayList = new ArrayList<>();
                        categoryArrayList.add(product);
                        productAdapter = new ArrayAdapter<>(BillingAddProductActivity.this,
                                android.R.layout.simple_spinner_item, categoryArrayList);
                        productAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                    }
                    mSpinnerProduct.setAdapter(productAdapter);
                    mSpinnerProduct.setSelection(0);
                }
            });

            mBtnSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (gst.getText().toString().isEmpty()) {
                        gstValue = 0;
                    } else
                        gstValue = Float.parseFloat(gst.getText().toString());
                    if (Igst.getText().toString().isEmpty()) {
                        IgstValue = 0;
                    } else
                        IgstValue = Float.parseFloat(Igst.getText().toString());


                    Log.v(TAG, "mSelectedCategoryId::" + mSelectedCategoryId);
                    Log.v(TAG, "mSelectedBrandId::" + mSelectedBrandId);
                    Log.v(TAG, "mSelectedProductId::" + mSelectedProductId);
                    String rateString = edtRate.getText().toString().trim();
                    String quantitiesString = edtQuantities.getText().toString().trim();
                    userId = MyApplication.getUserID();

                    if (rateString.isEmpty() || rateString.equalsIgnoreCase("0")) {
                        Toast.makeText(BillingAddProductActivity.this, "Please enter Price.", Toast.LENGTH_LONG)
                                .show();
                    } else if (quantitiesString.isEmpty() || quantitiesString.equalsIgnoreCase("0")) {
                        Toast.makeText(BillingAddProductActivity.this, "Please enter Quantity.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "" + quantitiesString + "  " + rateString);
                        final int qty = Integer.parseInt(quantitiesString);
                        final float rate = Float.parseFloat(rateString);
                        final float discountAmount = Util.calculateDiscountAmount(
                                Float.parseFloat(edtDiscount.getText().toString().trim()), rate);

                        purchaseObj.setQuantity(qty);
                        purchaseObj.setPrice(rate);
                        purchaseObj.setIgst(IgstValue);
                        purchaseObj.setGst(gstValue);
                        purchaseObj.setTax(IgstValue + gstValue);
//            purchaseObj.setTaxAmount(rate*qty*((IgstValue+gstValue)/100));
                        float total = rate * qty;
                        purchaseObj.setTaxAmount(total - (total / (1 + ((IgstValue + gstValue) / 100))));

                        BillingManager.getInstance().getCurrentPurchaseList().add(purchaseObj);
                        Intent intent = new Intent(BillingAddProductActivity.this, BillingActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (purchaseObj != null) {
            BillingManager.getInstance().getCurrentPurchaseList().add(purchaseObj);
            finish();
        }
        super.onBackPressed();
    }

    private void getProduct() {

    }
}
