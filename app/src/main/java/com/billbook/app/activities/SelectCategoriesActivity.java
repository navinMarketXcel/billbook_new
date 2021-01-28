package com.billbook.app.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.billbook.app.R;
import com.billbook.app.adapters.CategoriesSelectionAdapter;
import com.billbook.app.database.models.Category;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectCategoriesActivity extends AppCompatActivity {
    private final String TAG = "categoriesSElect";
    private ArrayList<Category> categories = new ArrayList<>();
    private CategoriesSelectionAdapter categoriesSelectionAdapter;
    private RecyclerView categorySelectionRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_categories);
//        getCategoriesAPI(0,0);
        setData();
        initUI();
    }

    private void setData() {
        Category category = new Category();
        category.setName("Telecom");
        categories.add(category);

        category = new Category();
        category.setName("Consumer Electronics (TV, Refrigerator, Washing machine)");
        categories.add(category);

        category = new Category();
        category.setName("Electrical Appliances");
        categories.add(category);

        category = new Category();
        category.setName("IT");
        categories.add(category);

        category = new Category();
        category.setName("Battery and Inverter");
        categories.add(category);

        category = new Category();
        category.setName("Tyre");
        categories.add(category);

        category = new Category();
        category.setName("Optics");
        categories.add(category);

        category = new Category();
        category.setName("Other");
        categories.add(category);

    }

    private void initUI() {
        categorySelectionRV = findViewById(R.id.categorySelectionRV);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        categorySelectionRV.setLayoutManager(mLayoutManager);
        categorySelectionRV.setItemAnimator(new DefaultItemAnimator());
        if (categories != null) {
            Log.v(TAG, "categoryArrayList::" + categories.size());
            categoriesSelectionAdapter = new CategoriesSelectionAdapter(SelectCategoriesActivity.this, categories);
            categorySelectionRV.setAdapter(categoriesSelectionAdapter);
        }
    }

    public void submit(View v) {
        try {
            JSONObject userObj = new JSONObject(getIntent().getStringExtra("userData"));
            StringBuffer stringBuffer = new StringBuffer();
            JSONArray categoriesJSON = new JSONArray();
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).isSelected()) {
                    stringBuffer.append(categories.get(i).getName());
                    stringBuffer.append(",");
                    JSONArray cat = setCategoryArray(i);
                    for (int j = 0; j < cat.length(); j++) {
                        categoriesJSON.put(cat.getInt(j));
                    }

                }
            }
            userObj.put("product_category", stringBuffer.toString());
            JSONArray array = new JSONArray(new ArrayList());
            userObj.put("category", categoriesJSON);
            Log.v(TAG, userObj.toString());
//            userObj.put("product_category",DealerCodeYes.isChecked());

            Intent intent = null;
            if (categories.get(categories.size() - 1).isSelected()) {
                intent = new Intent(this, SelectOtherCategoriesActivity.class);
            } else {
                intent = new Intent(this, OTPActivity.class);
            }
            intent.putExtra("userData", userObj.toString());
            intent.putExtra("other", false);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void getCategoriesAPI(int page, final long syncTime) {
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        Call<ArrayList<Category>> call = apiService.getCategoriesWithoutHeader(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<ArrayList<Category>>() {
            @Override
            public void onResponse(Call<ArrayList<Category>> call, Response<ArrayList<Category>> response) {
                categories = response.body();
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (categories != null) {
                            MyApplication.getDatabase().categoriesDao().insertAll(categories);
                        }
                    }
                });
                DialogUtils.stopProgressDialog();
                initUI();

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

    private JSONArray setCategoryArray(int catType) {
        JSONArray cat = null;
        try {
            switch (catType) {
                case 0:
                    cat = new JSONArray("[339,338,690,666,846,889,1006,952,983]");
                    break;
                case 1:
                    cat = new JSONArray("[325,330,984,329,335,340,332,324,328,323,321]");
                    break;
                case 2:
                    cat = new JSONArray("[329,969,685,328,485,342,334,323,322,327,337,984,982,326,333,336,341,340,331,324,893]");
                    break;
                case 3:
                    cat = new JSONArray("[540,981,952,345,344,346,353,347]");
                    break;
                case 4:
                    cat = new JSONArray("[685,349,352,351,350]");
                    break;
                case 5:
                    cat = new JSONArray("[348]");
                case 6:
                    cat = new JSONArray("[343,985]");
                    break;
                case 7:
                    cat = new JSONArray("[986,783,774,773]");
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cat;

    }
}
