package com.billbook.app.activities;

import android.app.ProgressDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
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
import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.viewmodel.AllProductViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BillingAllProductListActivity extends AppCompatActivity {
    RecyclerView recyclerViewAllProducts;
    AllProductViewModel allProductViewModel;
    BillingProductListAdapter mProductListAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private String TAG = "BillingProductListActivity";
    private ProgressDialog progressDialog;
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
        setContentView(R.layout.bottom_sheet_all_products);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();
        progressDialog = DialogUtils.startProgressDialog(BillingAllProductListActivity.this, "");
        allProductViewModel = ViewModelProviders.of(this).get(AllProductViewModel.class);
        allProductViewModel.getAllProductList()
                .observe(this, new Observer<List<ProductAndInventory>>() {
                    @Override
                    public void onChanged(@Nullable List<ProductAndInventory> products) {
                        ArrayList<ProductAndInventory> getAllProductList =
                                (ArrayList<ProductAndInventory>) products;
                        mProductListAdapter = new BillingProductListAdapter(BillingAllProductListActivity.this,
                                getAllProductList);
                        progressDialog.dismiss();
                        recyclerViewAllProducts.setAdapter(mProductListAdapter);
                    }
                });
    }

    private void initUI() {
        recyclerViewAllProducts = findViewById(R.id.recyclerViewAllProducts);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getApplicationContext());
        recyclerViewAllProducts.setLayoutManager(mLayoutManager);
        recyclerViewAllProducts.setItemAnimator(new DefaultItemAnimator());
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
}
