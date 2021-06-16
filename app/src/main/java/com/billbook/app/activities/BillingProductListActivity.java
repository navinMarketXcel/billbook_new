package com.billbook.app.activities;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.billbook.app.R;
import com.billbook.app.adapters.BillingProductListAdapter;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.database.models.User;
import com.billbook.app.viewmodel.ProductViewModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BillingProductListActivity extends AppCompatActivity {
    RecyclerView recyclerViewCategories;
    ProductViewModel mProductViewModel;
    BillingProductListAdapter mProductListAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private String TAG = "BillingProductListActivity";
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
        user = MyApplication.getUser();

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();
        user = MyApplication.getUser();
        final int categoriID = getIntent().getIntExtra("CategoryID", 0);
        final int brandID = getIntent().getIntExtra("BrandID", 0);

        mProductViewModel = ViewModelProviders.of(this).get(ProductViewModel.class);
        mProductViewModel.getProductListByCategoryIDAndBrandIDNew(categoriID, brandID)
                .observe(this, new Observer<List<ProductAndInventory>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndInventory> products) {
                        ArrayList<ProductAndInventory> getProductListByCategoryID = null;
                        if (getProductListByCategoryID == null || getProductListByCategoryID.size() == 0)
                            if (getProductListByCategoryID == null)
                                getProductListByCategoryID = new ArrayList<>();

                        try {
                            JSONArray jsonArray = new JSONArray(user.getProduct());
                            if (jsonArray.length() > 0) {
                                for (int j = 0; j < products.size(); j++)
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        if (jsonArray.getInt(i) == products.get(j).getProduct().getId()) {
                                            getProductListByCategoryID.add(getProductListByCategoryID.size(), products.get(j));
                                        }
                                    }
                            } else {
                                getProductListByCategoryID =
                                        (ArrayList<ProductAndInventory>) products;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        ProductAndInventory productAndInventory = new ProductAndInventory();
                        Product product = new Product();
                        product.setBrand(brandID);
                        product.setCategory(categoriID);
                        product.setName("Other");
                        productAndInventory.setProduct(product);
                        getProductListByCategoryID.add(getProductListByCategoryID.size(), productAndInventory);

                        mProductListAdapter =
                                new BillingProductListAdapter(BillingProductListActivity.this,
                                        getProductListByCategoryID);
                        recyclerViewCategories.setAdapter(mProductListAdapter);
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
                searchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(R.drawable.search_bg);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, BillingBrandActivity.class);
        final int categoriID = getIntent().getIntExtra("CategoryID", 0);
        final int brandID = getIntent().getIntExtra("BrandID", 0);
        intent.putExtra("CategoryID", categoriID);
        intent.putExtra("BrandID", brandID);

        startActivity(intent);
        super.onBackPressed();
    }
}
