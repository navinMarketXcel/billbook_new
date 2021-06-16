package com.billbook.app.activities;

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
import com.billbook.app.adapters.BillingCategoriesAdapter;
import com.billbook.app.adapters.SearchProductItemAdapter;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Product;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.viewmodel.CategoriesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BillingCategoriesActivity extends AppCompatActivity {
    private static final String TAG = "CategoriesActivity";
    BillingCategoriesAdapter mCategoriesAdapter;
    CategoriesViewModel mCategoriesViewModel;
    private RecyclerView mRecyclerViewCategories;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            //getSearcgResultFromDb(query);
            mCategoriesAdapter.getFilter().filter(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mCategoriesAdapter.getFilter().filter(newText);
            // getSearcgResultFromDb(newText);
            return false;
        }


        private void getSearchResultFromDb(String searchText) {
            searchText = "%" + searchText + "%";
            AppRepository.getInstance().getSearchProductList(searchText)
                    .observe(BillingCategoriesActivity.this, new Observer<List<Product>>() {
                        @Override
                        public void onChanged(@Nullable List<Product> productList) {
                            if (productList == null) {
                                return;
                            }
                            Log.v(TAG, "inside observe");
                            ArrayList<Product> productArrayList = (ArrayList<Product>) productList;
                            SearchProductItemAdapter adapter = new SearchProductItemAdapter(BillingCategoriesActivity.this, productArrayList);
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
        setContentView(R.layout.activity_billing_categories);
        this.setFinishOnTouchOutside(false);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();

        mCategoriesViewModel = ViewModelProviders.of(this).get(CategoriesViewModel.class);
        mCategoriesViewModel.getCategoriesList().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categoriesList) {
                ArrayList<Category> categoryArrayList = (ArrayList<Category>) categoriesList;
                if (categoriesList == null)
                    categoryArrayList = new ArrayList<>();
                Category category = new Category();
                category.setName("Other");
                category.setId(0);
                categoryArrayList.add(category);
                Log.v(TAG, "categoryArrayList::" + categoryArrayList);
                mCategoriesAdapter = new BillingCategoriesAdapter(BillingCategoriesActivity.this, categoryArrayList);
                mRecyclerViewCategories.setAdapter(mCategoriesAdapter);
            }
        });
    }

    private void initUI() {
        mRecyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerViewCategories.setLayoutManager(mLayoutManager);
        mRecyclerViewCategories.setItemAnimator(new DefaultItemAnimator());
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


    }

}
