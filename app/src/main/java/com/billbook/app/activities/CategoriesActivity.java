package com.billbook.app.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.billbook.app.adapters.CategoriesAdapter;
import com.billbook.app.adapters.SearchProductItemAdapter;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Product;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.viewmodel.CategoriesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class CategoriesActivity extends AppCompatActivity {
    private static final String TAG = "CategoriesActivity";
    CategoriesAdapter mCategoriesAdapter;
    FloatingActionButton mFabAddProduct;
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


        private void getSearcgResultFromDb(String searchText) {
            searchText = "%" + searchText + "%";
            AppRepository.getInstance().getSearchProductList(searchText)
                    .observe(CategoriesActivity.this, new Observer<List<Product>>() {
                        @Override
                        public void onChanged(@Nullable List<Product> productList) {
                            if (productList == null) {
                                return;
                            }
                            Log.v(TAG, "inside observe");
                            ArrayList<Product> productArrayList = (ArrayList<Product>) productList;
                            SearchProductItemAdapter adapter = new SearchProductItemAdapter(CategoriesActivity.this, productArrayList);
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
        setContentView(R.layout.activity_categories);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();

        mCategoriesViewModel = ViewModelProviders.of(this).get(CategoriesViewModel.class);
        mCategoriesViewModel.getCategoriesList().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(@Nullable List<Category> categoriesList) {
                ArrayList<Category> categoryArrayList = (ArrayList<Category>) categoriesList;
                if (categoriesList == null) {
                    categoryArrayList = new ArrayList<>();
                }
                Log.v(TAG, "categoryArrayList::" + categoryArrayList);
                Category category = new Category();
                category.setName("Other");
                category.setId(0);
                categoryArrayList.add(category);
                mCategoriesAdapter = new CategoriesAdapter(CategoriesActivity.this, categoryArrayList);
                mRecyclerViewCategories.setAdapter(mCategoriesAdapter);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startSpotLight(mRecyclerViewCategories.getChildAt(0), "Category", "Click here to see brands inside category");
                    }
                }, 500);

            }
        });
    }

    private void initUI() {
        mRecyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        mFabAddProduct = findViewById(R.id.fabAddProduct);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerViewCategories.setLayoutManager(mLayoutManager);
        mRecyclerViewCategories.setItemAnimator(new DefaultItemAnimator());

//        mFabAddProduct.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(CategoriesActivity.this, AddProductActivity.class);
//                startActivity(intent);
//            }
//        });
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
        mSearchAutoComplete = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);


    }

    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isCategoryscreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.fabAddProduct) {
                                SharedPreferences sharedPref =
                                        CategoriesActivity.this.getSharedPreferences(CategoriesActivity.this.getString(R.string.preference_file_key),
                                                CategoriesActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isCategoryscreenIntroShown", true);
                                editor.commit();
                            } else {
                                startSpotLight(mFabAddProduct, "Add Inventory", "Click here to add inventory");
                            }
                        }
                    })
                    .build()
                    .show();
        }
    }
}
