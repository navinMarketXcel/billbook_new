package com.billbook.app.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.viewmodel.AddProductViewModel;
import com.billbook.app.viewmodel.BillingManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BillingScannedProductActivity extends AppCompatActivity {
    private static final String TAG = "AddProductActivity";
    ArrayList<Category> categoryArrayList;
    ArrayList<Brand> brandArrayList;
    ArrayList<Product> productArrayList;
    int mSelectedCategoryId;
    int mSelectedBrandId;
    int mSelectedProductId;
    String mSelectedCategoryName;
    String mSelectedBrandName;
    String mSelectedProductName;
    private Spinner mSpinnerCategory, mSpinnerBrand, mSpinnerProduct;
    private Button mBtnSave;
    private EditText mEdtProductCount, mEdtProductPrice;
    private AddProductViewModel mAddProductViewModel;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAddProductViewModel = ViewModelProviders.of(this).get(AddProductViewModel.class);
        initUI();
        mAddProductViewModel.getCategoriesList().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categoriesList) {
                categoryArrayList = (ArrayList<Category>) categoriesList;

                Category category = new Category();
                category.setName("Select Category");
                categoryArrayList.add(0, category);

                ArrayAdapter<Category> myAdapter = new ArrayAdapter<>(BillingScannedProductActivity.this,
                        android.R.layout.simple_spinner_item, categoryArrayList);
                myAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                mSpinnerCategory.setAdapter(myAdapter);
            }
        });
    }

    private void initUI() {
        mSpinnerCategory = findViewById(R.id.spinnerCategory);
        mSpinnerBrand = findViewById(R.id.spinnerBrand);
        mSpinnerProduct = findViewById(R.id.spinnerProduct);
        mBtnSave = findViewById(R.id.btnSave);
        mEdtProductCount = findViewById(R.id.edtProductCount);
        mEdtProductPrice = findViewById(R.id.edtProductPrice);

        mSpinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                if (pos != 0) {
                    mSelectedCategoryId = categoryArrayList.get(pos).getId();
                    mSelectedCategoryName = categoryArrayList.get(pos).getName();

                    mAddProductViewModel.getBrandListByCategoryID(mSelectedCategoryId)
                            .observe(BillingScannedProductActivity.this, new Observer<List<Brand>>() {
                                @Override
                                public void onChanged(@Nullable List<Brand> productsList) {
                                    brandArrayList = (ArrayList<Brand>) productsList;
                                    mSelectedBrandId = 0;

                                    Brand brand = new Brand();
                                    brand.setName("Select Brand");
                                    brandArrayList.add(0, brand);
                                    if (brandArrayList != null && brandArrayList.size() > 0) {
                                        mSelectedBrandId = brandArrayList.get(0).getId();
                                        mSelectedBrandName = brandArrayList.get(0).getName();
                                    }
                                    ArrayAdapter<Brand> myAdapter =
                                            new ArrayAdapter<>(BillingScannedProductActivity.this,
                                                    android.R.layout.simple_spinner_item, brandArrayList);
                                    myAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                                    mSpinnerBrand.setAdapter(myAdapter);
                                }
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                if (pos != 0) {
                    mSelectedBrandId = brandArrayList.get(pos).getId();
                    mSelectedProductId = 0;
                    mAddProductViewModel.getProductListByCategoryIDAndBrand(mSelectedCategoryId,
                            mSelectedBrandId)
                            .observe(BillingScannedProductActivity.this, new Observer<List<Product>>() {
                                @Override
                                public void onChanged(@Nullable List<Product> productsList) {
                                    productArrayList = (ArrayList<Product>) productsList;

                                    Product product = new Product();
                                    product.setName("Select Model");
                                    productArrayList.add(0, product);

                                    if (productArrayList != null && productArrayList.size() > 0) {
                                        mSelectedProductId = productArrayList.get(0).getId();
                                        mSelectedProductName = productArrayList.get(0).getName();
                                    }
                                    ArrayAdapter<Product> myAdapter =
                                            new ArrayAdapter<>(BillingScannedProductActivity.this,
                                                    android.R.layout.simple_spinner_item, productArrayList);
                                    myAdapter.setDropDownViewResource(R.layout.spinner_textview_layout);
                                    mSpinnerProduct.setAdapter(myAdapter);
                                }
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpinnerProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                if (pos != 0) {
                    mSelectedProductId = productArrayList.get(pos).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(TAG, "mSelectedCategoryId::" + mSelectedCategoryId);
                Log.v(TAG, "mSelectedBrandId::" + mSelectedBrandId);
                Log.v(TAG, "mSelectedProductId::" + mSelectedProductId);
                if (validateInput()) {
                    int productCount = Integer.parseInt(mEdtProductCount.getText().toString().trim());
                    int productPrice = Integer.parseInt(mEdtProductPrice.getText().toString().trim());
                    final Purchase purchase = new Purchase();
                    purchase.setId(0);
                    purchase.setProduct_id(mSelectedProductId);
                    purchase.setProduct_name(mSelectedProductName);

                    purchase.setCategory(mSelectedCategoryId);
                    purchase.setCategory_name(mSelectedCategoryName);

                    purchase.setBrand(mSelectedBrandId);
                    purchase.setCategory_name(mSelectedBrandName);

                    purchase.setUser(MyApplication.getUserID());
                    purchase.setQuantity(productCount);
                    purchase.setPrice(productPrice);
                    purchase.setTax(0.0f);
                    purchase.setSelling_price(0.0f);

                    BillingManager.getInstance().getCurrentPurchaseList().add(purchase);
                    finish();
                }
            }
        });
    }

    private boolean validateInput() {
        boolean isValidate = true;
        if (mSelectedCategoryId == 0) {
            isValidate = false;
            Toast.makeText(BillingScannedProductActivity.this, "Please select category.",
                    Toast.LENGTH_SHORT).show();
        } else if (mSelectedBrandId == 0) {
            isValidate = false;
            Toast.makeText(BillingScannedProductActivity.this, "Please select brand.", Toast.LENGTH_SHORT)
                    .show();
        } else if (mSelectedProductId == 0) {
            isValidate = false;
            Toast.makeText(BillingScannedProductActivity.this, "Please select model.", Toast.LENGTH_SHORT)
                    .show();
        } else if (mEdtProductCount.getText().toString().trim().isEmpty()) {

            isValidate = false;
            Toast.makeText(BillingScannedProductActivity.this, "Please enter model count.",
                    Toast.LENGTH_SHORT).show();
        } else if (Integer.parseInt(mEdtProductCount.getText().toString().trim()) == 0) {
            isValidate = false;
            Toast.makeText(BillingScannedProductActivity.this, "Please enter valid model count.",
                    Toast.LENGTH_SHORT).show();
        } else if (mEdtProductPrice.getText().toString().trim().isEmpty()) {

            isValidate = false;
            Toast.makeText(BillingScannedProductActivity.this, "Please enter model price.",
                    Toast.LENGTH_SHORT).show();
        } else if (Integer.parseInt(mEdtProductPrice.getText().toString().trim()) == 0) {
            isValidate = false;
            Toast.makeText(BillingScannedProductActivity.this, "Please enter valid model price.",
                    Toast.LENGTH_SHORT).show();
        }

        return isValidate;
    }
}
