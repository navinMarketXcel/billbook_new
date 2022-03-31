package com.billbook.app.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.billbook.app.R;
import com.billbook.app.database.models.Distributor;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDistributorActivity extends AppCompatActivity {
    private EditText nameEdt, addressEdt, gstEdt, mobileEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_distributor);
        initUI();
    }

    private void initUI() {
        nameEdt = findViewById(R.id.userNameEdt);
        addressEdt = findViewById(R.id.distaddressEdt);
        gstEdt = findViewById(R.id.distGSTEdt);
        mobileEdt = findViewById(R.id.distContactEdt);
    }

    public void submit(View v) {
        if (validateInput()) {
            DialogUtils.startProgressDialog(this, "");
            final Distributor distributor = new Distributor();
            distributor.setName(nameEdt.getText().toString().trim());
            distributor.setIs_active(true);
            distributor.setAddress(addressEdt.getText().toString());
            distributor.setGst_no(gstEdt.getText().toString());
            distributor.setMobileNo(mobileEdt.getText().toString());
            ApiInterface apiService =
                    ApiClient.getClient(this).create(ApiInterface.class);

            String token = MyApplication.getUserToken();

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", token);
            headerMap.put("Content-Type", "application/json");
            Call<Distributor> call = apiService.addDistributor(headerMap, distributor);
            call.enqueue(new Callback<Distributor>() {
                @Override
                public void onResponse(Call<Distributor> call, final Response<Distributor> response) {
                    Log.v("Distributor", "response::" + response.body());
                    if (response.body() != null) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                MyApplication.getDatabase().distributorDao().inserDistributor(response.body());
                                AddDistributorActivity.this.finish();
                            }
                        });
                    }
                    DialogUtils.stopProgressDialog();
                }

                @Override
                public void onFailure(Call<Distributor> call, Throwable t) {
                    DialogUtils.showToast(AddDistributorActivity.this, "Failed to add distributor");
                    DialogUtils.stopProgressDialog();
                }
            });


        }

    }

    private boolean validateInput() {
        if (nameEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Please enter name");
            return false;

        } else if (addressEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Please enter address");

            return false;

        } else if (gstEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Please enter gstno");
            return false;

        } else if (mobileEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Please enter mobileno");

            return false;

        } else
            return true;
    }
}
