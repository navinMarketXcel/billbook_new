package com.billbook.app.activities;

import android.app.DatePickerDialog;
import android.arch.lifecycle.Observer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.utils.Util;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class InvoiceDetailsActivity extends AppCompatActivity {
    private static final String TAG = "InvoiceDetailsActivity";
    private EditText edtName, edtInvoiceNo;
    private Button btnSearch;
    private TextView txtInvoiceNo, txtInvoiceDate;
    private EditText edtDate;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_details);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();

    }

    private void initUI() {
        edtName = findViewById(R.id.edtName);
        edtInvoiceNo = findViewById(R.id.edtInvoiceNo);
        edtDate = findViewById(R.id.edtDate);

        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    int invoiceID = Integer.parseInt(edtInvoiceNo.getText().toString());
                    Log.v(TAG, "input invoiceID::" + invoiceID);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            Log.v(TAG, "all invoice::" + MyApplication.getDatabase().invoiceDao().getAllInvoice());

                        }
                    });
                    MyApplication.getDatabase().invoiceDao().getInvoice(invoiceID).observe(InvoiceDetailsActivity.this, new Observer<Invoice>() {
                        @Override
                        public void onChanged(@Nullable Invoice invoice) {
                            Log.v(TAG, "database response of invoice::" + invoice);
                            if (invoice == null) {
                                // fetch from server
                                getInvoiceDetailsFromAPI();
                            } else {
                                //display local invoice
                                Toast.makeText(InvoiceDetailsActivity.this, "Customer Invoice details available", Toast.LENGTH_SHORT);

                            }
                        }
                    });
                }
            }
        });

        edtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }


    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(InvoiceDetailsActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                edtDate.setText(dateToUse);


            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


    private void getInvoiceDetailsFromAPI() {
        if (Util.isNetworkAvailable(getApplicationContext())) {
            int invoiceID = Integer.parseInt(edtInvoiceNo.getText().toString());
            long dateTime = new Date().getTime();
            AppRepository.getInstance().getInvoiceAPIForInvoiceIDAndDate(invoiceID, dateTime).observe(InvoiceDetailsActivity.this, new Observer<RequestInvoice>() {
                @Override
                public void onChanged(@Nullable RequestInvoice requestInvoice) {
                    Log.v(TAG, "RequestInvoice::" + requestInvoice);
                    if (requestInvoice == null) {
                        Toast.makeText(InvoiceDetailsActivity.this, "Customer Invoice details not available", Toast.LENGTH_SHORT);

                    } else {
                        Toast.makeText(InvoiceDetailsActivity.this, "Customer Invoice details available", Toast.LENGTH_SHORT);

                    }
                }
            });
        } else {
            Toast.makeText(InvoiceDetailsActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        boolean isvalidate = true;
        if (edtName.getText().toString().isEmpty()) {
            isvalidate = false;
            Toast.makeText(InvoiceDetailsActivity.this, "Please enter name", Toast.LENGTH_SHORT).show();
        } else if (edtInvoiceNo.getText().toString().isEmpty()) {
            isvalidate = false;
            Toast.makeText(InvoiceDetailsActivity.this, "Please enter invoice no", Toast.LENGTH_SHORT).show();

        } else if (edtDate.getText().toString().isEmpty()) {
            isvalidate = false;
            Toast.makeText(InvoiceDetailsActivity.this, "Please select date", Toast.LENGTH_SHORT).show();

        }
        Log.v(TAG, "isvalidate::" + isvalidate);
        return isvalidate;
    }
}
