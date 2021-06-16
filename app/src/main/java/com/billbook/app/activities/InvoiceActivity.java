package com.billbook.app.activities;

import androidx.lifecycle.Observer;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.billbook.app.R;
import com.billbook.app.adapters.InvoiceListAdapter;
import com.billbook.app.adapters.SearchProductItemAdapter;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Product;
import com.billbook.app.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InvoiceActivity extends AppCompatActivity {
    private static final String TAG = "AppCompatActivity";
    InvoiceListAdapter mInvoiceAdapter;
    private RecyclerView mRecyclerViewInvoice;
    private SearchView searchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private Toolbar mTopToolbar;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            getSearcgResultFromDb(query);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            getSearcgResultFromDb(newText);
            return true;
        }


        private void getSearcgResultFromDb(String searchText) {
            searchText = "%" + searchText + "%";
            AppRepository.getInstance().getSearchProductList(searchText)
                    .observe(InvoiceActivity.this, new Observer<List<Product>>() {
                        @Override
                        public void onChanged(@Nullable List<Product> productList) {
                            if (productList == null) {
                                return;
                            }
                            Log.v(TAG, "inside observe");
                            ArrayList<Product> productArrayList = (ArrayList<Product>) productList;
                            SearchProductItemAdapter adapter = new SearchProductItemAdapter(InvoiceActivity.this, productArrayList);
                            mSearchAutoComplete.setAdapter(adapter);
                        }
                    });
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
        setContentView(R.layout.activity_invoice);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();
        mInvoiceAdapter = new InvoiceListAdapter(InvoiceActivity.this, new ArrayList<Category>());
        mRecyclerViewInvoice.setAdapter(mInvoiceAdapter);
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
        mSearchAutoComplete = searchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(R.drawable.search_bg);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);

    }

    private void initUI() {
        mRecyclerViewInvoice = findViewById(R.id.recyclerViewInvoice);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerViewInvoice.setLayoutManager(mLayoutManager);
        mRecyclerViewInvoice.setItemAnimator(new DefaultItemAnimator());

    }
}
