package com.billbook.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {
    private final String TAG = "RegistrationActivity";
    private EditText userNameEdt, shopNameEdt, firstName, lastName, shopAddr, mobileNo, pinCodeEdt;

    private ArrayList<String> stateList = new ArrayList<>();
    private ArrayList<String> citiesList = new ArrayList<>();
    private ArrayAdapter<String> cityAdpater;
    private ArrayAdapter<String> stateAdpater;
    private JSONArray citiesJSONArray = null;
    private String mobileNoText, OTP;
    private AutoCompleteTextView states, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setTitle("Registration");
        initUI();
        initAdapter();
    }

    public void goToPrivacyPolicy(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://thebillbook.com/privacypolicy.html"));
        startActivity(i);
    }

    public void gotoToTermsAndConsitions(View v) {
        Intent intent = new Intent(this, TermsAndConditionActivity.class);
        startActivity(intent);
    }

    private void initUI() {
        mobileNoText = getIntent().getExtras().getString("mobileNo");
        OTP = getIntent().getExtras().getString("otp");
        userNameEdt = findViewById(R.id.userNameEdt);
        shopNameEdt = findViewById(R.id.shopNameEdt);
        shopAddr = findViewById(R.id.shopAddressEdt);
        mobileNo = findViewById(R.id.mobileNoEdt);
        mobileNo.setText(mobileNoText);
        states = findViewById(R.id.state);
        pinCodeEdt = findViewById(R.id.pinCodeEdt);
        city = findViewById(R.id.city);
    }

    private void initAdapter() {
        citiesList.add("Select City");
        cityAdpater = new ArrayAdapter<String>(
                RegistrationActivity.this,
                R.layout.spinner_item, citiesList);
        city.setAdapter(cityAdpater);


        stateAdpater = new ArrayAdapter<String>(
                RegistrationActivity.this,
                R.layout.spinner_item, stateList);
        states.setAdapter(stateAdpater);

    }

    private boolean validateData() {
        if (shopNameEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Shop name can not be empty.");
            return false;
        } else if (shopAddr.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Shop address can not be empty.");
            return false;
        } else if (mobileNo.getText().toString().isEmpty() || mobileNo.getText().toString().length() < 10) {
            DialogUtils.showToast(this, "Please enter valid mobile number");
            return false;
        } else if (states.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Please select State");
            return false;
        } else if (city.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Please select city");
            return false;
        } else if (pinCodeEdt.getText().toString().length() < 6) {
            DialogUtils.showToast(this, "Please enter valid pin code");
            return false;
        } else return true;
    }

    public void registerUser(View view) {
        if (validateData()) {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("shopName", shopNameEdt.getText().toString());
            headerMap.put("shopAddr", shopAddr.getText().toString());
            headerMap.put("mobileNo", mobileNo.getText().toString());
            headerMap.put("state", states.getText().toString());
            headerMap.put("city", city.getText().toString());
            headerMap.put("pincode", pinCodeEdt.getText().toString());
            headerMap.put("otp", OTP);
            register(headerMap);
        }
    }

    private void startHomeActivity() {
        Log.i(TAG, "startHomeActivity:");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }


    private void loadCities() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DialogUtils.startProgressDialog(RegistrationActivity.this, "");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    citiesList.clear();
                    if (citiesJSONArray == null)
                        citiesJSONArray = new JSONArray(readJSONFromAsset());
                    for (int i = 0; i < citiesJSONArray.length(); i++) {
                        if (states.getText().toString().equalsIgnoreCase(
                                citiesJSONArray.getJSONObject(i).getString("state"))) {
                            citiesList.add(citiesJSONArray.getJSONObject(i).getString("name"));
                        }
                        Collections.sort(citiesList, String.CASE_INSENSITIVE_ORDER);

                    }
                    citiesList.add(0, "Select City");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                cityAdpater.notifyDataSetChanged();
                DialogUtils.stopProgressDialog();
            }


        }.execute();
    }

    public String readJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            Log.v(TAG, json);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }


    private void register(Map<String, String> req) {
        Util.postEvents("Registration", "Registration ", this.getApplicationContext());
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("Content-Type", "application/json");

        Call<Object> call = apiService.users((HashMap<String, String>) req);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP", body.toString());
                    // UserToken now comes in body object
//                    Headers headerList = response.headers();
//                    String token = headerList.get("authorization");
//                    body.put("token",token);
                    JSONObject data = body.getJSONObject("data");
                    String userToken = data.getString("userToken");
                    MyApplication.saveUserDetails(data.toString());
                    MyApplication.saveUserToken(userToken);

                    if (body.getJSONObject("data").has("isGST"))
                        MyApplication.setShowGstPopup((Integer) data.get("isGST"));

                    startHomeActivity();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(RegistrationActivity.this, "Failed to get OTP");
            }
        });

    }
}
