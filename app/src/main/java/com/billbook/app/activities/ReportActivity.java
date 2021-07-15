package com.billbook.app.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import androidx.lifecycle.Observer;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.utils.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ReportActivity";
    private TextView tvToDate, tvFromDate;
    private String selctedFromDate, selctedToDate;
    private long selectedFromDateLong, selectedToDateLong;
    private Button btnSave;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initUI();
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.getsalesreport));
        startSpotLight(tvFromDate, "Date", "Enter from date.");
    }

    private void initUI() {

        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        btnSave = findViewById(R.id.btnSave);

        tvFromDate.setOnClickListener(this);
        tvToDate.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        tvFromDate.setText(Util.getToDateBack(1));
        tvToDate.setText(Util.getTodaysdate());
        selctedToDate = Util.getTodaysdate();
        selctedFromDate = Util.getToDateBack(1);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tvFromDate:
                showDatePickerDialog(1);
                break;
            case R.id.tvToDate:
                showDatePickerDialog(2);
                break;
            case R.id.btnSave:

                if (!validateDate(selctedFromDate, selctedToDate)) {
                    Toast.makeText(ReportActivity.this, "From Date should be less than To Date", Toast.LENGTH_LONG).show();
                } else {
                    if (Util.isNetworkAvailable(ReportActivity.this)) {
                        sendReportApiCall();
                    } else {
                        Toast.makeText(ReportActivity.this, getString(R.string.no_internet), Toast.LENGTH_LONG).show();

                    }
                }

                break;


        }
    }

    private void showDatePickerDialog(final int forDate) {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ReportActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {


                String dateToUse = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                if (dayOfMonth < 10 && (monthOfYear + 1) < 10) {
                    dateToUse = year + "-0" + (monthOfYear + 1) + "-0" + dayOfMonth;
                } else if (dayOfMonth < 10) {
                    dateToUse = year + "-" + (monthOfYear + 1) + "-0" + dayOfMonth;
                } else if ((monthOfYear + 1) < 10) {
                    dateToUse = year + "-0" + (monthOfYear + 1) + "-" + dayOfMonth;
                }


                if (forDate == 1) {
                    tvFromDate.setText(dateToUse);
                    selctedFromDate = dateToUse;
                } else {
                    selctedToDate = dateToUse;
                    tvToDate.setText(dateToUse);


                }
                Log.v(TAG, "selctedFromDate::" + selctedFromDate + " selctedToDate::" + selctedToDate);

                if (!validateDate(selctedFromDate, selctedToDate)) {
                    Toast.makeText(ReportActivity.this, "From Date should be less than To Date", Toast.LENGTH_LONG).show();
                }


            }
        }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean validateDate(String selctedFromDate, String selctedToDate) {
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        boolean isValidate = true;
        try {
            Date selFrDate = myFormat.parse(selctedFromDate);
            selFrDate.setHours(0);
            selFrDate.setMinutes(0);
            selFrDate.setSeconds(0);
            Date selToDate = myFormat.parse(selctedToDate);
            selToDate.setHours(23);
            selToDate.setMinutes(59);
            selToDate.setSeconds(59);
            Log.v(TAG, "selFrDate::" + selFrDate.getTime());
            Log.v(TAG, "selToDate::" + selToDate.getTime());

            selectedFromDateLong = selFrDate.getTime() / 1000;
            selectedToDateLong = selToDate.getTime() / 1000;

            if (selFrDate.getTime() > selToDate.getTime()) {
                isValidate = false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            isValidate = false;
        }
        Log.v(TAG, "isValidate::" + isValidate);
        return isValidate;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void sendReportApiCall() {
        progressDialog = DialogUtils.startProgressDialog(ReportActivity.this, "");
        AppRepository.getInstance().SendReportAPI(selectedFromDateLong, selectedToDateLong).observe(ReportActivity.this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                Log.v(TAG, "integer::" + integer.intValue());
                if (integer.intValue() == 200) {
                    Toast.makeText(ReportActivity.this, "Report send successfully on registered email id.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReportActivity.this, "Report sending failed.", Toast.LENGTH_SHORT).show();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        finish();
                    }
                });
            }
        });
    }

    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isReportscreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.btnSave) {
                                SharedPreferences sharedPref =
                                        ReportActivity.this.getSharedPreferences(ReportActivity.this.getString(R.string.preference_file_key),
                                                ReportActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isReportscreenIntroShown", true);
                                editor.commit();
                            } else {
                                if (view.getId() == R.id.tvFromDate) {
                                    startSpotLight(tvToDate, "Date", "Enter to date.");
                                } else if (view.getId() == R.id.tvToDate) {
                                    startSpotLight(findViewById(R.id.btnSave), "Send Report", "This will be used to send report to registered email id.");
                                }
                            }
                        }
                    })
                    .build()
                    .show();
        }
    }
}
