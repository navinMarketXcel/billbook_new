package com.billbook.app.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.adapter_callback.ExpenseCallBack;
import com.billbook.app.adapters.ExpenseAdapter;
import com.billbook.app.networkcommunication.NetworkType;
import com.billbook.app.utils.Util;
import com.billbook.app.viewmodel.ExpenseViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.billbook.app.R;
import com.billbook.app.adapters.ExpenseListAdapter;
import com.billbook.app.database.models.Expense;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseActivity extends AppCompatActivity implements ExpenseCallBack {
    private static final String TAG = "ExpenseActivity" ;
    private SearchView.SearchAutoComplete mSearchAutoComplete;
    private JSONObject profile;
    private int userid;
    private Date invoiceDate;
    private String invoiceDateStr, networkType;
    private EditText selectDate, expenseAmount, expenseName;
    private RecyclerView expensesRV;
    private EditText edtSearchExpense;
    private ExpenseListAdapter expenseListAdapter;
    private ExpenseViewModel expenseViewModel;
    private Button sortExpense, cancelExpense;
    private ArrayList<Expense> expenseArrayList;
    private ExpenseAdapter  expenseAdapter;
    private Expense expense;
    private boolean isEdit=false;
    public static ArrayList<Expense> expenses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        setTitle("Expenses");
        sortExpense = findViewById(R.id.sortExpense);
        NetworkType nt = new NetworkType();
        networkType = nt.getNetworkClass(this);
        initUI();
        setonClick();
        setOnTextChangeListener();
    }
    private void initUI(){
        edtSearchExpense = findViewById(R.id.edtSearchExpense);
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
        expenseListAdapter = new ExpenseListAdapter(this,expenses,ExpenseActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        expensesRV.setLayoutManager(mLayoutManager);
        expensesRV.setItemAnimator(new DefaultItemAnimator());
        expensesRV.setAdapter(expenseListAdapter);
    }

    @Override
    protected void onResume() {
        
        super.onResume();
        Util.dailyLogout(ExpenseActivity.this);
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
        mSearchAutoComplete =
                searchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setDropDownBackgroundResource(R.drawable.search_bg);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);
    }

    public void gotoAddExpense(View v){
        BottomSheetDialog addExpenseDialog = new BottomSheetDialog(ExpenseActivity.this,R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_add_expens,findViewById(R.id.addExpenseLayout));
        selectDate = view.findViewById(R.id.selectDate);
        expenseName= view.findViewById(R.id.expenseName);
        expenseAmount = view.findViewById(R.id.expenseAmount);
        String date;
        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        date = formatter.format(new Date());
        selectDate.setText(date);
        Button add = view.findViewById(R.id.addNewExpense);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Expense expense = new Expense();
                expense.setAmount(Integer.parseInt(expenseAmount.getText().toString()));
                expense.setName(expenseName.getText().toString());
                expense.setDate(selectDate.getText().toString());
                expense.setUserid(userid);
                saveExpenseToServer(expense);
                addExpenseDialog.dismiss();
            }
        });
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        addExpenseDialog.setContentView(view);
        addExpenseDialog.show();
        addExpenseDialog.findViewById(R.id.cancelExpense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpenseDialog.dismiss();
            }
        });
    }
    public void gotoEditExpense(Expense data){
        BottomSheetDialog addExpenseDialog = new BottomSheetDialog(ExpenseActivity.this,R.style.BottomSheetDialogTheme);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_add_expens,findViewById(R.id.addExpenseLayout));
        selectDate = view.findViewById(R.id.selectDate);
        expenseName= view.findViewById(R.id.expenseName);
        expenseAmount = view.findViewById(R.id.expenseAmount);
        expenseName.setText(data.getName());
        expenseAmount.setText(""+data.getAmount());
        selectDate.setText(data.getDate());
        TextView txtLabel=view.findViewById(R.id.txtLabel);
        txtLabel.setText("Edit Expenses");
        String date;
        isEdit = true;
        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        date = formatter.format(new Date());
        selectDate.setText(date);
        Button save = view.findViewById(R.id.addNewExpense);
        save.setText("Save");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Expense updateExpense = new Expense();
                updateExpense.setAmount(Integer.parseInt(expenseAmount.getText().toString()));
                updateExpense.setName(expenseName.getText().toString());
                updateExpense.setDate(selectDate.getText().toString());
                updateExpense.setUserid(userid);
                updateExpense.setId((long) data.getId());
                saveExpenseToServer(updateExpense);
                addExpenseDialog.dismiss();
            }
        });
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        addExpenseDialog.setContentView(view);
        addExpenseDialog.show();
        addExpenseDialog.findViewById(R.id.cancelExpense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpenseDialog.dismiss();
            }
        });
    }

    public void gotoDeleteExpense(Expense data){
        DialogUtils.showAlertDialog(ExpenseActivity.this, getResources().getString(R.string.yes), getResources().getString(R.string.no), getResources().getString(R.string.expense_delete_button), new DialogUtils.DialogClickListener() {
            @Override
            public void positiveButtonClick() {
                Call<Object> call = null;
                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("Content-Type", "application/json");
                if(Util.isNetworkAvailable(ExpenseActivity.this))
                {
                    if(networkType.equals("2G") || networkType.equals("?")){

                    } else {
                        ApiInterface apiService = ApiClient.getClient(ExpenseActivity.this).create(ApiInterface.class);
                        long id = data.getId();
                        call = apiService.deleteExpenses(headerMap,id);
                        call.enqueue(new Callback<Object>() {
                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                try {
                                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                                    if(body.getBoolean("status")){
                                        DialogUtils.showToast(ExpenseActivity.this, "Expense deleted successfully");
                                        getExpenses();
                                    } else {
                                        DialogUtils.showToast(ExpenseActivity.this, "Please try again");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(Call<Object> call, Throwable t) {
                                DialogUtils.showToast(ExpenseActivity.this, "Failed to get expenses");
                                Util.logErrorApi("/v1/expense/"+id, new JSONObject(), Arrays.toString(t.getStackTrace()), t.toString() , null, ExpenseActivity.this);

                            }
                        });
                    }

                } else {
                    long id = data.getId();
                }
            }

            @Override
            public void negativeButtonClick() {

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void getExpenses(){
        if(Util.isNetworkAvailable(this)) {
            if(networkType.equals("2G") || networkType.equals("?")){
                setOfflineExpenses();
            } else {
                DialogUtils.startProgressDialog(this, "");
                ApiInterface apiService =
                        ApiClient.getClient(this).create(ApiInterface.class);
                Map<String, String> headerMap = new HashMap<>();

                headerMap.put("Content-Type", "application/json");
                JSONObject getExpenseObject = new JSONObject();
                try {
                    getExpenseObject.put("userid", userid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


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
                                expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,expenses,ExpenseActivity.this);
                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                expensesRV.setLayoutManager(mLayoutManager);
                                expensesRV.setItemAnimator(new DefaultItemAnimator());
                                expensesRV.setAdapter(expenseListAdapter);
                                expenseListAdapter.notifyDataSetChanged();
                                if(!myModelList.isEmpty()){
                                    TextView textView = findViewById(R.id.tvExpenseNotFound);
                                    textView.setVisibility(View.GONE);
                                }
                                Log.v("RESP", body.toString());

                            } else {
                                expenses.clear();
                                expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,expenses,ExpenseActivity.this);
                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                expensesRV.setLayoutManager(mLayoutManager);
                                expensesRV.setItemAnimator(new DefaultItemAnimator());
                                expensesRV.setAdapter(expenseListAdapter);
                                expenseListAdapter.notifyDataSetChanged();
                                TextView textView = findViewById(R.id.tvExpenseNotFound);
                                textView.setVisibility(View.VISIBLE);

                            }
                        } catch (JSONException e) {
                            Util.logErrorApi("expense/" + userid, getExpenseObject, Arrays.toString(e.getStackTrace()), e.toString() , null,ExpenseActivity.this);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        Util.logErrorApi("expense/" + userid, getExpenseObject, Arrays.toString(t.getStackTrace()), t.toString() , null,ExpenseActivity.this);
                        DialogUtils.stopProgressDialog();
                        DialogUtils.showToast(ExpenseActivity.this, "Failed to get expenses");
                    }
                });
            }
        }else{
            setOfflineExpenses();
            DialogUtils.showToast(this, getString(R.string.no_internet));
        }
    }

    public void setOfflineExpenses(){
        if (MyApplication.getUnSyncedExpenses().length() > 0) {
            Type listType = new TypeToken<List<Expense>>() {
            }.getType();
            List<Expense> myModelList = new Gson().fromJson(MyApplication.getUnSyncedExpenses(),
                    listType);
            expenses.clear();
            expenses.addAll(myModelList);
            expenseListAdapter.notifyDataSetChanged();
            TextView textView = findViewById(R.id.tvExpenseNotFound);
            textView.setVisibility(View.GONE);
        }
    }

    public void setonClick(){

        ImageView iv = findViewById(R.id.ivToolBarBack);
        LinearLayout help = findViewById(R.id.lnHelp);
        LinearLayout youTube = findViewById(R.id.lnYouTube);
        iv.setOnClickListener(v -> {
            finish();
        });
        help.setOnClickListener(v -> {
            Util. startHelpActivity(ExpenseActivity.this);
        });
        youTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(ExpenseActivity.this);
        });


    }
    public void setOnTextChangeListener(){
        edtSearchExpense.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                expenseListAdapter.getFilter().filter(s);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }




    public void clickExpenseSort(View v)
    {
                BottomSheetDialog sortExpenseSheet = new BottomSheetDialog(ExpenseActivity.this,R.style.BottomSheetDialogTheme);
                View sortBottomSheet = LayoutInflater.from(getApplicationContext()).inflate(R.layout.expense_sort_layout,(LinearLayout)findViewById(R.id.sortExpenseLayout));
                sortBottomSheet.findViewById(R.id.btnCanelExpense).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortExpenseSheet.dismiss();
                    }
                });
                sortBottomSheet.findViewById(R.id.edate).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        TextView btn_Date = sortBottomSheet.findViewById(R.id.edate);
//                        btn_Date.setBackground(ContextCompat.getDrawable(ExpenseActivity.this, R.drawable.sort_screen));
                        ArrayList<Expense> sortDateExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getDate)).collect(Collectors.toList());
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortDateExpense,ExpenseActivity.this);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        sortExpenseSheet.dismiss();
                        //bno.setBackgroundColor;
                    }
                });
                sortBottomSheet.findViewById(R.id.eName).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        TextView btn_Name = sortBottomSheet.findViewById(R.id.eName);
//                        btn_Name.setBackground(ContextCompat.getDrawable(ExpenseActivity.this, R.drawable.sort_screen));
                        ArrayList<Expense> sortNameExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getName)).collect(Collectors.toList());
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortNameExpense,ExpenseActivity.this);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        sortExpenseSheet.dismiss();
                        //bno.setBackgroundColor;
                    }
                });
                sortBottomSheet.findViewById(R.id.eLtoH).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        TextView btn_LtoH = sortBottomSheet.findViewById(R.id.eName);
//                        btn_LtoH.setBackground(ContextCompat.getDrawable(ExpenseActivity.this, R.drawable.sort_screen));
                        ArrayList<Expense> sortLtoHExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getAmount)).collect(Collectors.toList());
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortLtoHExpense,ExpenseActivity.this);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        sortExpenseSheet.dismiss();
                        //bno.setBackgroundColor;
                    }
                });
                sortBottomSheet.findViewById(R.id.eHtoL).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        TextView btn_HtoL = sortBottomSheet.findViewById(R.id.eName);
//                        btn_HtoL.setBackground(ContextCompat.getDrawable(ExpenseActivity.this, R.drawable.sort_screen));
                        ArrayList<Expense> sortHtoLExpense = (ArrayList<Expense>) expenses.stream().sorted(Comparator.comparing(Expense::getAmount)).collect(Collectors.toList());
                        Collections.reverse(sortHtoLExpense);
                        expenseListAdapter = new ExpenseListAdapter(ExpenseActivity.this,sortHtoLExpense,ExpenseActivity.this);
                        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                        expensesRV.setLayoutManager(mLayoutManager);
                        expensesRV.setItemAnimator(new DefaultItemAnimator());
                        expensesRV.setAdapter(expenseListAdapter);
                        sortExpenseSheet.dismiss();
                        //bno.setBackgroundColor;
                    }
                });
                sortExpenseSheet.setContentView(sortBottomSheet);
                sortExpenseSheet.show();
    }

    public void showDatePickerDialog(View v) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                String dateToUse = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                if (dayOfMonth < 10) {
                    dateToUse = year + "-" + (monthOfYear + 1) + "-0" + dayOfMonth;
                } else if ((monthOfYear + 1) < 10) {
                    dateToUse = year + "-0" + (monthOfYear + 1) + "-" + dayOfMonth;
                } else if (dayOfMonth < 10 && (monthOfYear + 1) < 10) {
                    dateToUse = year + "-0" + (monthOfYear + 1) + "-0" + dayOfMonth;
                }
                SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                try {
                    dateToUse = dateToUse + "T00:00:00.001Z";
                    invoiceDate = myFormat.parse(dateToUse);
                    DateFormat formatter =
                            new SimpleDateFormat("dd-MMM-yyyy");
                    invoiceDateStr = formatter.format(invoiceDate);
                    selectDate.setText(invoiceDateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.v(TAG, "selctedFromDate::" + invoiceDate);

            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveExpenseToServer(Expense expense){
        if(Util.isNetworkAvailable(this)) {
            if(networkType.equals("2G") || networkType.equals("?")){
                saveExpenseOffline(expense);
                DialogUtils.showToast(this, getString(R.string.no_internet));
            }else {
                DialogUtils.startProgressDialog(this, "");
                ApiInterface apiService =
                        ApiClient.getClient(this).create(ApiInterface.class);
                Map<String, String> headerMap = new HashMap<>();

                headerMap.put("Content-Type", "application/json");

                Call<Object> call = null;
                if (!isEdit) {
                    Util.postEvents("Add Expense","Add Expense",this.getApplicationContext());
                    call = apiService.expenses(headerMap, expense);
                }
                else {
                    Util.postEvents("Update Expense","Update Expense",this.getApplicationContext());

                    call = apiService.updateExpenses(headerMap, (int) expense.getId(), expense);
                }
                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        DialogUtils.stopProgressDialog();
                        try {
                            JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                            if (!isEdit && body.getBoolean("status") && !body.isNull("data")) {
                                Log.v("RESP", body.toString());
                                final Expense expense1 = new Gson().fromJson(body.getJSONObject("data").toString(), Expense.class);
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        MyApplication.getDatabase().getExpModelDao().insertExpense(expense1);
                                    }
                                });
                                DialogUtils.showToast(getApplicationContext(),"Expense successfully created");
                                getExpenses();
                            } else if (isEdit && body.getBoolean("status")){
                                DialogUtils.showToast(getApplicationContext(),"Expense successfully updated");
                                getExpenses();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        DialogUtils.stopProgressDialog();
                        DialogUtils.showToast(ExpenseActivity.this, "Failed to save expense data");
                    }
                });
            }
        }else{
            saveExpenseOffline(expense);
        }

    }

    public void saveExpenseOffline(Expense expense){
        String expenses = MyApplication.getUnSyncedExpenses();
        if(expenses.isEmpty()){
            JSONArray exp= null;
            try {
                exp = new JSONArray().put(new JSONObject(new Gson().toJson(expense)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            MyApplication.saveUnSyncedExpenses(exp.toString());
            DialogUtils.showToast(getApplicationContext(),"Expense saved in offline");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                        ExpenseActivity.this.finish();
                }
            },1000);
        }else{
            try {
                JSONArray jsonArray =  new JSONArray(expenses);
                jsonArray.put(new JSONObject(new Gson().toJson(expense)));
                MyApplication.saveUnSyncedExpenses(jsonArray.toString());
                DialogUtils.showToast(getApplicationContext(),"Expense saved in offline");
                Log.v("EXET",MyApplication.getUnSyncedExpenses());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                            ExpenseActivity.this.finish();
                    }
                },1000);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getExpenses();
    }

    private void getExpenseData(){
        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseViewModel.getModels().observe(this, new Observer<List<Expense>>() {
            @Override
            public void onChanged(@Nullable List<Expense> modelList) {
                expenseArrayList = (ArrayList<Expense>) modelList;
                if (modelList == null) {
                    expenseArrayList = new ArrayList<>();
                }
                expenseAdapter = new ExpenseAdapter(ExpenseActivity.this,R.layout.spinner_textview_layout,expenseArrayList);
                Log.v(TAG, "models::" + expenseArrayList);
            }
        });
    }

    @Override
    public void callback(String action, Expense data, Integer position) {
        if(action.equals("edit")){
            gotoEditExpense(data);
        }
        if(action.equals("delete")){
            gotoDeleteExpense(data);
        }

    }
}
