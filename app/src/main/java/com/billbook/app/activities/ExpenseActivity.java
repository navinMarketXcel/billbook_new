package com.billbook.app.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.billbook.app.R;
import com.billbook.app.adapters.ExpenseListAdapter;
import com.billbook.app.database.models.Expense;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseActivity extends AppCompatActivity {
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private JSONObject profile;
    private int userid;
    private RecyclerView expensesRV;
    private ExpenseListAdapter expenseListAdapter;
    public static ArrayList<Expense> expenses = new ArrayList<>();
    private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    expenseListAdapter.getFilter().filter(query);
                    return false;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    expenseListAdapter.getFilter().filter(newText);
                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        setTitle("Expenses");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initUI();
    }
    private void initUI(){
        expensesRV = findViewById(R.id.expensesRV);
        try {
            profile = new JSONObject(MyApplication.getUserDetails());
            userid= profile.getInt("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView syncText = findViewById(R.id.syncText);
        if(MyApplication.getUnSyncedExpenses().isEmpty()){
            syncText.setVisibility(View.INVISIBLE);
        }else {
            syncText.setVisibility(View.VISIBLE);

        }
        expenseListAdapter = new ExpenseListAdapter(this,expenses);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        expensesRV.setLayoutManager(mLayoutManager);
        expensesRV.setItemAnimator(new DefaultItemAnimator());
        expensesRV.setAdapter(expenseListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getExpenses();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

    public void gotoAddExpense(View v){
        Util.postEvents("Add New Expense","Add New Expense",this.getApplicationContext());
        Intent intent = new Intent(this, AddExpenseActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void getExpenses(){
        if(Util.isNetworkAvailable(this)) {
            DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            Map<String, String> headerMap = new HashMap<>();

            headerMap.put("Content-Type", "application/json");

            Call<Object> call = apiService.expenses(headerMap, userid);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        if (body.getJSONObject("data").has("rows") && body.getJSONObject("data").getInt("count") > 0) {
                            Type listType = new TypeToken<List<Expense>>() {
                            }.getType();
                            List<Expense> myModelList = new Gson().fromJson(body.getJSONObject("data").getJSONArray("rows").toString(),
                                    listType);
                            expenses.clear();
                            expenses.addAll(myModelList);
                            expenseListAdapter.notifyDataSetChanged();
                            Log.v("RESP", body.toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(ExpenseActivity.this, "Failed to get expenses");
                }
            });
        }else{
            DialogUtils.showToast(this, getString(R.string.no_internet));
        }
    }
}
