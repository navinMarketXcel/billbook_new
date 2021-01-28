package com.billbook.app.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.billbook.app.R;
import com.billbook.app.adapters.ProductListAdapter;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.database.models.User;
import com.billbook.app.viewmodel.ProductViewModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;

public class ProductListActivity extends AppCompatActivity {
    private static final String TAG = "ProductListActivity";
    RecyclerView recyclerViewCategories;
    ProductViewModel mProductViewModel;
    ProductListAdapter mProductListAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private User user;
    private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    mProductListAdapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.v(TAG, "newText::" + newText);
                    mProductListAdapter.getFilter().filter(newText);
                    return false;
                }
            };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        user = MyApplication.getUser();
        initUI();

        final int categoriID = getIntent().getIntExtra("CategoryID", 0);
        final int brandID = getIntent().getIntExtra("BrandID", 0);
       /* AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MyApplication.getDatabase().productDao().loadProductByCategoryIdAndBrandNew(categoriID,brandID).observe(ProductListActivity.this, new Observer<List<ProductAndInventory>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndInventory> productAndInventoryList) {
                         Log.v(TAG,"prod uctAndInventoryList::"+productAndInventoryList.toString());
                    }
                });
            }
        });*/

        mProductViewModel = ViewModelProviders.of(this).get(ProductViewModel.class);
        mProductViewModel.getProductListByCategoryIDAndBrandIDNew(categoriID, brandID)
                .observe(this, new Observer<List<ProductAndInventory>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndInventory> products) {
                        ArrayList<ProductAndInventory> getProductListByCategoryID = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(user.getProduct());
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    for (int j = 0; j < products.size(); j++)
                                        if (jsonArray.getInt(i) == products.get(j).getProduct().getId()) {
                                            getProductListByCategoryID.add(products.get(j));
                                        }
                                }
                            } else {
                                getProductListByCategoryID =
                                        (ArrayList<ProductAndInventory>) products;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Collections.sort(getProductListByCategoryID, new Comparator<ProductAndInventory>() {
                            @Override
                            public int compare(ProductAndInventory o1, ProductAndInventory o2) {
                                return o2.inventoryList.size() - o1.inventoryList.size();
                            }
                        });

//            if (products != null) {
//              Log.v(TAG, "products::" + products);
//              getProductListByCategoryID =
//                      (ArrayList<ProductAndInventory>) products;
//            }
                        ProductAndInventory productAndInventory = new ProductAndInventory();
                        Product product = new Product();
                        product.setBrand(brandID);
                        product.setCategory(categoriID);
                        product.setName("Other");
                        productAndInventory.setProduct(product);
                        getProductListByCategoryID.add(productAndInventory);
                        mProductListAdapter = new ProductListAdapter(ProductListActivity.this, getProductListByCategoryID);
                        recyclerViewCategories.setAdapter(mProductListAdapter);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startSpotLight(recyclerViewCategories.getChildAt(0), "Product", "This product with its count in your inventory.");
                            }
                        }, 500);
                    }
                });
    }

    private void initUI() {
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewCategories.setLayoutManager(mLayoutManager);
        recyclerViewCategories.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) search.getActionView();
        initSearchUI(searchView);
        return true;
    }

    private void initSearchUI(SearchView searchView) {

        //searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(onQueryTextListener);
        mSearchAutoComplete =
                searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(R.drawable.search_bg);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);
    }

    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isProductscreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .build()
                    .show();

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isProductscreenIntroShown", true);
            editor.commit();
        }
    }
}
