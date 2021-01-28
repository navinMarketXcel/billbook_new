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
import com.billbook.app.adapters.BrandAdapter;
import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.User;
import com.billbook.app.viewmodel.BrandViewModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;

public class BrandActivity extends AppCompatActivity {
    private static final String TAG = "BrandActivity";
    RecyclerView mRecyclerViewBrand;
    BrandViewModel mBrandViewModel;
    BrandAdapter mBrandAdapter;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private User user;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

            mBrandAdapter.getFilter().filter(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Log.v(TAG, "newText::" + newText);
            mBrandAdapter.getFilter().filter(newText);
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
        setContentView(R.layout.activity_brand);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();
        user = MyApplication.getUser();
        final int catId = getIntent().getIntExtra("CategoryID", 0);
        mBrandViewModel = ViewModelProviders.of(this).get(BrandViewModel.class);
        mBrandViewModel.getBrandListByCategoryID(catId).observe(this, new Observer<List<Brand>>() {
            @Override
            public void onChanged(@Nullable List<Brand> brandsList) {
                ArrayList<Brand> brandArrayList = new ArrayList<>();
                if (brandArrayList == null)
                    brandArrayList = new ArrayList<>();

                Log.v(TAG, "categoryArrayList::" + brandArrayList);
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(user.getBrand());
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {

                            for (int j = 0; j < brandsList.size(); j++)
                                if (jsonArray.getInt(i) == brandsList.get(j).getId()) {
                                    brandArrayList.add(brandsList.get(j));
                                }
                        }
                    } else {
                        brandArrayList = (ArrayList<Brand>) brandsList;
                    }
                    Brand brand = new Brand();
                    brand.setCategory(catId);
                    brand.setName("Other");
                    brandArrayList.add(brand);

                    mBrandAdapter = new BrandAdapter(BrandActivity.this, brandArrayList);
                    mRecyclerViewBrand.setAdapter(mBrandAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startSpotLight(mRecyclerViewBrand.getChildAt(0), "Brand", "Click here to see products inside brand.");
                    }
                }, 500);
            }
        });
//        ProductViewModel productViewModel = ViewModelProviders.of(this).get(ProductViewModel.class);
//
//        productViewModel.getCategorywiseProductCnt().observe(this, new Observer<List<ProductAndInventory>>() {
//            @Override
//            public void onChanged(@Nullable List<ProductAndInventory> brandsList) {
//                ArrayList<ProductAndInventory> brandArrayList = new ArrayList<>();
//                if (brandArrayList == null)
//                    brandArrayList = new ArrayList<>();
//            }});

    }

    private void initUI() {
        mRecyclerViewBrand = findViewById(R.id.recyclerViewBrand);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerViewBrand.setLayoutManager(mLayoutManager);
        mRecyclerViewBrand.setItemAnimator(new DefaultItemAnimator());
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
        mSearchAutoComplete.setDropDownBackgroundResource(R.drawable.search_bg);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);

    }

    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isBrandscreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .build()
                    .show();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isBrandscreenIntroShown", true);
            editor.commit();
        }
    }


}
