package com.billbook.app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.billbook.app.R;
import com.billbook.app.adapters.ProductSelectionAdapter;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.viewmodel.ProductViewModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageProductsActivity extends AppCompatActivity {
    private final String TAG = "categoriesSElect";
    private ArrayList<Product> products = new ArrayList<>();
    private ProductSelectionAdapter productSelectionAdapter;
    private RecyclerView productSelectionRV;
    private CheckBox selectAllCategories;
    private ProductViewModel mproductViewModel;
    private User user;
    private Handler handler = new Handler();
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

            productSelectionAdapter.getFilter().filter(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Log.v(TAG, "newText::" + newText);
            productSelectionAdapter.getFilter().filter(newText);

            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_categories);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Manage Products");

        initUI();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);
                int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
                user = MyApplication.getDatabase().userDao().getUser(userId);
                Log.v(TAG, "user::" + user);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                });
            }
        }).start();
    }

    private void loadData() {
        mproductViewModel = ViewModelProviders.of(this).get(ProductViewModel.class);
        mproductViewModel.getProductListByCategoryIDAndBrandID(getIntent().getIntExtra("category", 0),
                getIntent().getIntExtra("brand", 0)).observe(this, new Observer<List<Product>>() {
            @Override
            public void onChanged(@Nullable List<Product> brandsList) {
                products = (ArrayList<Product>) brandsList;
                if (products == null)
                    products = new ArrayList<>();
                Log.v(TAG, "brandLiust::" + products.size());
                try {
                    JSONArray selectedBrands = new JSONArray(user.getProduct() == null ? "" : user.getProduct());
                    if (selectedBrands.length() == 0)
                        selectAllCategories.setChecked(true);
                    else {
                        for (int i = 0; i < selectedBrands.length(); i++) {
                            for (Product product : products) {
                                if (product.getId() == selectedBrands.getInt(i))
                                    product.setSelected(true);
                            }
                        }
                    }
                    if (products != null) {
                        Log.v(TAG, "categoryArrayList::" + products.size());
                        productSelectionAdapter = new ProductSelectionAdapter(ManageProductsActivity.this, products);
                        productSelectionRV.setAdapter(productSelectionAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        startSpotLight(mRecyclerViewBrand.getChildAt(0),"Brand","Click here to see products inside brand.");
//                    }
//                },500);
            }
        });

    }

    private void initUI() {
        productSelectionRV = findViewById(R.id.categorySelectionRV);
        selectAllCategories = findViewById(R.id.selectAllCategories);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        productSelectionRV.setLayoutManager(mLayoutManager);
        productSelectionRV.setItemAnimator(new DefaultItemAnimator());
        if (products != null) {
            Log.v(TAG, "categoryArrayList::" + products.size());
            productSelectionAdapter = new ProductSelectionAdapter(ManageProductsActivity.this, products);
            productSelectionRV.setAdapter(productSelectionAdapter);
        }
        selectAllCategories.setText("Select All Products");
        selectAllCategories.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (Product product : products) {
                    product.setSelected(isChecked);
                }
                productSelectionAdapter.notifyDataSetChanged();

            }
        });
    }

    public void submit(View v) {
        ArrayList<Integer> jsonArray = new ArrayList<>();
        JSONArray jsonArray1;
        try {
            jsonArray1 = new JSONArray(user.getProduct());
            for (int i = 0; i < jsonArray1.length(); i++)
                jsonArray.add(jsonArray1.getInt(i));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < products.size(); i++) {
            if (jsonArray.contains(products.get(i).getId()) && !products.get(i).isSelected()) {
                jsonArray.remove(new Integer(products.get(i).getId()));
            } else if (products.get(i).isSelected())
                jsonArray.add(products.get(i).getId());
        }
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
        final ProgressDialog progressDialog = DialogUtils.startProgressDialog(this, "");
        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Map<String, ArrayList<Integer>> req = new HashMap<>();
        req.put("product", jsonArray);
        user.setProduct(jsonArray.toString());
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Call<User> call = apiService.updateCategories(headerMap, userId, req);
        call.enqueue(new Callback<User>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User user = response.body();
                if (user != null) {

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.saveLoginUserID(user.getId());
                            MyApplication.getDatabase().userDao().inserUser(user);
                            MyApplication.setUser(user);
                        }
                    });
                    DialogUtils.showToast(ManageProductsActivity.this, "Products updated successfully");
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            MyApplication.saveLoginUserID(user.getId());
                            MyApplication.getDatabase().userDao().inserUser(user);
                            MyApplication.setUser(user);
                            return null;

                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            ManageProductsActivity.this.finish();
                        }
                    }.execute();
                } else {
                    DialogUtils.showToast(ManageProductsActivity.this, "Failed to update Brands");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
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
}
