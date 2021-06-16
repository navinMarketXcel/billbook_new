package com.billbook.app.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.billbook.app.R;
import com.billbook.app.adapters.CategoriesSelectionAdapter;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Constants;
import com.billbook.app.viewmodel.CategoriesViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageCategoriesActivity extends AppCompatActivity {
    private final String TAG = "categoriesSElect";
    private ArrayList<Category> categories = new ArrayList<>();
    private CategoriesSelectionAdapter categoriesSelectionAdapter;
    private RecyclerView categorySelectionRV;
    private CheckBox selectAllCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_categories);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Manage Categories");
        getCategoriesAPI(0, 0);
        selectAllCategories = findViewById(R.id.selectAllCategories);
        selectAllCategories.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (Category category : categories) {
                    category.setSelected(isChecked);
                }
                categoriesSelectionAdapter.notifyDataSetChanged();
            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
//                        getString(R.string.preference_file_key), MODE_PRIVATE);
//                int userId=sharedPref.getInt(getString(R.string.login_user_id),0);
//                User user=MyApplication.getDatabase().userDao().getUser(userId);
//                Log.v(TAG,"user::"+user);
//            }
//        }).start();
    }

    private void initUI() {

        categorySelectionRV = findViewById(R.id.categorySelectionRV);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        categorySelectionRV.setLayoutManager(mLayoutManager);
        categorySelectionRV.setItemAnimator(new DefaultItemAnimator());
        if (categories != null) {
            Log.v(TAG, "categoryArrayList::" + categories.size());
            categoriesSelectionAdapter = new CategoriesSelectionAdapter(ManageCategoriesActivity.this, categories);
            categorySelectionRV.setAdapter(categoriesSelectionAdapter);
        }

    }

    public void submit(View v) {
        updateCategories();
    }

    public void getCategoriesAPI(int page, final long syncTime) {
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Map<String, String> headerMap = new HashMap<>();
        String token = MyApplication.getUserToken();
        if (token != null)
            headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<ArrayList<Category>> call = apiService.getCategoriesWithoutHeader(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<ArrayList<Category>>() {
            @Override
            public void onResponse(Call<ArrayList<Category>> call, Response<ArrayList<Category>> response) {
                categories = response.body();
                CategoriesViewModel mCategoriesViewModel = ViewModelProviders.of(ManageCategoriesActivity.this).get(CategoriesViewModel.class);
                mCategoriesViewModel.getCategoriesList().observe(ManageCategoriesActivity.this, new Observer<List<Category>>() {
                    @Override
                    public void onChanged(@Nullable List<Category> categoriesList) {
                        ArrayList<Category> categoryArrayList = (ArrayList<Category>) categoriesList;
                        if (categoriesList != null) {
                            Log.v(TAG, "categoryArrayList::" + categoryArrayList);
                            for (Category category : categoryArrayList) {
                                for (int i = 0; i < categories.size(); i++) {
                                    if (categories.get(i).getId() == category.getId()) {
                                        categories.get(i).setSelected(true);
                                        break;
                                    }
                                }
                            }
                        }
                        initUI();
                    }
                });
                DialogUtils.stopProgressDialog();
            }

            @Override
            public void onFailure(Call<ArrayList<Category>> call, Throwable t) {
                Log.v(TAG, " getCategoriesAPI onFailure productResponse::");
                t.printStackTrace();
                DialogUtils.stopProgressDialog();
                MyApplication.saveGetCategoriesLAST_SYNC_TIMESTAMP(syncTime);
            }
        });
    }

    public void updateCategories() {
        ArrayList<Integer> jsonArray = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).isSelected())
                jsonArray.add(categories.get(i).getId());
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
        req.put("category", jsonArray);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
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
                        }
                    });
                    DialogUtils.showToast(ManageCategoriesActivity.this, "Categories updated successfully");
                    new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            MyApplication.saveGetCategoriesLAST_SYNC_TIMESTAMP(0);
                            MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(0);
                            MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(0);
                            MyApplication.saveGetInventoryLAST_SYNC_TIMESTAMP(0);
                            MyApplication.saveGetInvoiceLAST_SYNC_TIMESTAMP(0);
//                            MyApplication.getDatabase().inventoryDao().deleteAll();
                            MyApplication.getDatabase().productDao().deleteAll();
                            MyApplication.getDatabase().brandDao().deleteAll();
                            MyApplication.getDatabase().categoriesDao().deleteAll();
                            return null;

                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent(ManageCategoriesActivity.this, SyncActivity.class);
                            intent.putExtra("killSelf", true);
                            startActivity(intent);
                            ManageCategoriesActivity.this.finish();
                        }
                    }.execute();


                } else {
                    DialogUtils.showToast(ManageCategoriesActivity.this, "Failed to update Categories");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }
}
