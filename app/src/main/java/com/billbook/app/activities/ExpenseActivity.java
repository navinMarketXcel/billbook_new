package com.billbook.app.activities;

import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.utils.Util;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseActivity extends AppCompatActivity {
    private static final String TAG = "ExpenseActivity" ;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private JSONObject profile;
    private int userid;
    private RecyclerView expensesRV;
    private ExpenseListAdapter expenseListAdapter;
    private Button sortExpense;
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
        sortExpense = findViewById(R.id.sortExpense);
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
        expenses.clear();
        expenseListAdapter.notifyDataSetChanged();
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
                searchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(R.drawable.search_bg);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);
    }

    public void gotoAddExpense(View v){
        BottomSheetDialog addExpenseDialog = new BottomSheetDialog(ExpenseActivity.this,R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_add_expens,findViewById(R.id.addExpenseLayout));
//        addExpenseDialog.findViewById(R.id.addNewExpense).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        addExpenseDialog.setContentView(view);
        addExpenseDialog.show();
        addExpenseDialog.findViewById(R.id.cancelExpense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpenseDialog.dismiss();
            }
        });
//        Util.postEvents("Add New Expense","Add New Expense",this.getApplicationContext());
//        Intent intent = new Intent(this, AddExpenseActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void getExpenses(){
        if(Util.isNetworkAvailable(this)) {
            DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient(this).create(ApiInterface.class);
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
                            expenses.addAll(myModelList);
                            expenseListAdapter.notifyDataSetChanged();
                            if(!myModelList.isEmpty()){
                                TextView textView = findViewById(R.id.tvExpenseNotFound);
                                textView.setVisibility(View.GONE);
                            }
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

    public void clickExpenseSort(View v)
    {
        sortExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView btn_Date;
                BottomSheetDialog sortExpenseSheet = new BottomSheetDialog(ExpenseActivity.this,R.style.BottomSheetDialogTheme);
                View sortBottomSheet = LayoutInflater.from(getApplicationContext()).inflate(R.layout.expense_sort_layout,(LinearLayout)findViewById(R.id.sortExpenseLayout));
                btn_Date = sortBottomSheet.findViewById(R.id.edate);
                btn_Date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btn_Date.setBackground(ContextCompat.getDrawable(ExpenseActivity.this, R.drawable.sort_screen));
                    }
                });
                sortBottomSheet.findViewById(R.id.btnExpenseSort).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expenseListAdapter.notifyDataSetChanged();
                        sortExpenseSheet.dismiss();
                    }
                });
                sortBottomSheet.findViewById(R.id.btnCanelExpense).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortExpenseSheet.dismiss();
                    }
                });
                sortBottomSheet.findViewById(R.id.edate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView eDate = findViewById(R.id.edate);
                        ArrayList<Expense> sortDateExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getDate)).collect(Collectors.toList());
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortDateExpense);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        //bno.setBackgroundColor;
                    }
                });
                sortBottomSheet.findViewById(R.id.eName).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView eName = findViewById(R.id.eName);
                        ArrayList<Expense> sortNameExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getName)).collect(Collectors.toList());
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortNameExpense);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        //bno.setBackgroundColor;
                    }
                });
                sortBottomSheet.findViewById(R.id.eLtoH).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView eLtoH = findViewById(R.id.eLtoH);
                        ArrayList<Expense> sortLtoHExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getAmount)).collect(Collectors.toList());
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortLtoHExpense);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        //bno.setBackgroundColor;
                    }
                });
                sortBottomSheet.findViewById(R.id.eHtoL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView eHtoL = findViewById(R.id.eHtoL);
                        ArrayList<Expense> sortHtoLExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getAmount)).collect(Collectors.toList());
                        Collections.reverse(sortHtoLExpense);
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortHtoLExpense);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        //bno.setBackgroundColor;
                    }
                });
                sortExpenseSheet.setContentView(sortBottomSheet);
                sortExpenseSheet.show();
            }

        });

    }
}
