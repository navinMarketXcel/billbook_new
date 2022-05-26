package com.billbook.app.activities;

import android.app.DatePickerDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.adapters.ExpenseAdapter;
import com.billbook.app.database.models.Expense;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.viewmodel.ExpenseViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddExpenseActivity extends AppCompatActivity {
    private String invoiceDateStr;
    private Date invoiceDate;
    private EditText selectDate,expenseAmount;
    private EditText expenseName;
    private String TAG = "Expense";
    private int userid;
    private Expense expense;
    private boolean isEdit=false;
    private ExpenseViewModel expenseViewModel;
    private ArrayList<Expense> expenseArrayList;
    private ExpenseAdapter  expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expens);
        setTitle("Expenses");
        initUI();
    }
    private void initUI(){
        selectDate = findViewById(R.id.selectDate);
        expenseName=findViewById(R.id.expenseName);
        expenseAmount =findViewById(R.id.expenseAmount);
        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        invoiceDateStr = formatter.format(new Date());
        selectDate.setText(invoiceDateStr);
        try {
            userid = new JSONObject(MyApplication.getUserDetails()).getInt("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(getIntent().hasExtra("expense")) {
            isEdit = true;
            expense = (Expense) getIntent().getSerializableExtra("expense");
            expenseName.setText(expense.getName());
            expenseAmount.setText(""+expense.getAmount());
            selectDate.setText(expense.getDate());
            SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                invoiceDate = myFormat.parse(expense.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        getExpenseData();
    }
    public void saveExpense(View v){
        if(expenseName.getText().toString().isEmpty()){
            DialogUtils.showToast(this,"Expense name can not be empty");
            return;
        }
        else if(expenseAmount.getText().toString().isEmpty() || Float.parseFloat(expenseAmount.getText().toString())==0){
            DialogUtils.showToast(this,"Expense amount can not be 0 or empty");
            return;
        }
        else if(selectDate.getText().toString().isEmpty()){
            DialogUtils.showToast(this,"Expense date can not be empty");
            return;
        }
        Expense expense = new Expense();
        expense.setAmount(Integer.parseInt(expenseAmount.getText().toString()));
        expense.setName(expenseName.getText().toString());
        expense.setDate(selectDate.getText().toString());
        expense.setUserid(userid);
        if(isEdit)
            expense.setId(this.expense.getId());
        saveExpenseToServer(expense);
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
                            AddExpenseActivity.this.finish();
                        } else if (isEdit && body.getBoolean("status")){
                            DialogUtils.showToast(getApplicationContext(),"Expense successfully updated");
                            AddExpenseActivity.this.finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(AddExpenseActivity.this, "Failed to save expense data");
                }
            });
        }else{
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
                    AddExpenseActivity.this.finish();
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
                        AddExpenseActivity.this.finish();
                    }
                },1000);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        }
    }

    private void getExpenseData(){
        expenseViewModel = ViewModelProviders.of(this).get(ExpenseViewModel.class);
        expenseViewModel.getModels().observe(this, new Observer<List<Expense>>() {
            @Override
            public void onChanged(@Nullable List<Expense> modelList) {
                expenseArrayList = (ArrayList<Expense>) modelList;
                if (modelList == null) {
                    expenseArrayList = new ArrayList<>();
                }
                expenseAdapter = new ExpenseAdapter(AddExpenseActivity.this,R.layout.spinner_textview_layout,expenseArrayList);
                Log.v(TAG, "models::" + expenseArrayList);
            }
        });
    }
}
