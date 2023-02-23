package com.billbook.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.billbook.app.databinding.ActivityRegistrationBinding;
import com.billbook.app.utils.Util;
import com.google.android.gms.common.api.Api;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {
    private final String TAG = "RegistrationActivity";
    private final ArrayList<String> stateList = new ArrayList<>();
    private final ArrayList<String> citiesList = new ArrayList<>();
    private ArrayAdapter<String> cityAdpater;
    private ArrayAdapter<String> stateAdpater;
    private JSONArray citiesJSONArray = null;
    private String mobileNoText,OTP, state, city, referrer_link;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private ActivityRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setTitle("Registration");
        initUI();
        initAdapter();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void goToPrivacyPolicy(View v) {
        try{
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.thebillbook.com/privacypolicy.html"));
            startActivity(i);
        }
        catch(Exception e){
            DialogUtils.showToast(RegistrationActivity.this,"Browser not installed");
            e.printStackTrace();

        }
    }

    public void gotoToTermsAndConsitions(View v) {
        Intent intent = new Intent(this, TermsAndConditionActivity.class);
        startActivity(intent);
    }

    private void initUI() {
        mobileNoText = getIntent().getExtras().getString("mobileNo");
        OTP = getIntent().getExtras().getString("otp");
        referrer_link = getIntent().getExtras().getString("referrer_link");
        binding.mobileNoEdt.setText(mobileNoText);
//        binding.pinCodeEdt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                binding.llFetchCity.setVisibility(View.VISIBLE);
//                binding.displayCity.setText("Fetching City");
//                binding.pincodeProgressBar.setVisibility(View.VISIBLE);
//                state=null;
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                try {
//                    if (s.length() == 6) {
//                        binding.llFetchCity.setVisibility(View.VISIBLE);
//                        binding.displayCity.setText("Fetching City");
//                        ApiInterface apiService = ApiClient.getClient(RegistrationActivity.this).create(ApiInterface.class);
//                        Map<String, String> req = new HashMap<>();
//                        req.put("pincode", s.toString());
//                        Call<Object> call = apiService.pincode((HashMap<String, String>) req);
//                        call.enqueue(new Callback<Object>() {
//
//                            @Override
//                            public void onResponse(Call<Object> call, Response<Object> response) {
//                                try {
//                                    if (response.body() == null) {
//                                        binding.pinCodeEdt.setError("Invalid PIN Code");
//                                        binding.llFetchCity.setVisibility(View.GONE);
//                                    } else {
//                                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
//                                        JSONObject data = body.getJSONObject("data");
//                                        state = data.getString("state");
//                                        city = data.getString("city");
//                                        binding.pincodeProgressBar.setVisibility(View.GONE);
//                                        binding.displayCity.setText(new StringBuilder().append(city).append(", ").append(state).toString());
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<Object> call, Throwable t) {
//                                binding.pinCodeEdt.setError("Please Check your Internet Connection!");
//                                binding.llFetchCity.setVisibility(View.GONE);
//                            }
//                        });
//                    }
//                    else if(s.length()>6){
//                        binding.pinCodeEdt.setError("Invalid PIN Code");
//                        binding.llFetchCity.setVisibility(View.GONE);
//                    }
//                    else{
//                        binding.llFetchCity.setVisibility(View.GONE);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private void initAdapter() {
        citiesList.add("Select City");
        cityAdpater = new ArrayAdapter<String>(
                RegistrationActivity.this,
                R.layout.spinner_item, citiesList);
//        binding.city.setAdapter(cityAdpater);


        stateAdpater = new ArrayAdapter<String>(
                RegistrationActivity.this,
                R.layout.spinner_item, stateList);
//        binding.state.setAdapter(stateAdpater);

    }

    private boolean validateData() {
        if (binding.shopNameEdt.getText().toString().isEmpty() || binding.shopNameEdt.getText().equals("")) {
            DialogUtils.showToast(this, "Shop name can not be empty.");
            return false;
        } else if (binding.shopAddressEdt.getText().toString().isEmpty() || binding.shopAddressEdt.getText().equals("")) {
            DialogUtils.showToast(this, "Shop address can not be empty.");
            return false;
        } else if (binding.mobileNoEdt.getText().toString().isEmpty() || binding.mobileNoEdt.getText().toString().length() < 10) {
            DialogUtils.showToast(this, "Please enter valid mobile number");
            return false;
        } else if (binding.pinCodeEdt.getText().toString().length() < 6 || binding.pinCodeEdt.getError() != null) {
            DialogUtils.showToast(this, "Invalid Pincode");
            return false;
        } else if (binding.state.getText().toString().isEmpty() || binding.city.getText().toString().isEmpty()) {

            DialogUtils.showToast(this, "State or city cannot be empty");
            return false;
        }
        else {
            return true;
        }
    }

    public void registerUser(View view) {
        if (validateData()) {
            MyApplication.cleverTapAPI.pushEvent("Clicked Register");
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("shopName",binding.shopNameEdt.getText().toString());
            headerMap.put("shopAddr",binding.shopAddressEdt.getText().toString());
            headerMap.put("mobileNo",binding.mobileNoEdt.getText().toString());
            headerMap.put("state",binding.state.getText().toString());
            headerMap.put("city",binding.city.getText().toString());
            headerMap.put("pincode",binding.pinCodeEdt.getText().toString());
            headerMap.put("otp",OTP);
            headerMap.put("referrer_link", referrer_link);
            register(headerMap);

        }
    }

    private void startHomeActivity() {
        Log.i(TAG, "startHomeActivity:");
        MyApplication.cleverTapAPI.pushEvent("Registration successful");
        Intent intent = new Intent(this, BottomNavigationActivity.class);
        startActivity(intent);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        String currentDate = sdf.format(c.getTime());
        MyApplication.setLogoutDaily(false);
        MyApplication.saveScheduleLogOutDate(currentDate);


    }


//    private void loadCities() {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                DialogUtils.startProgressDialog(RegistrationActivity.this, "");
//            }
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                try {
//                    citiesList.clear();
//                    if (citiesJSONArray == null)
//                        citiesJSONArray = new JSONArray(readJSONFromAsset());
//                    for (int i = 0; i < citiesJSONArray.length(); i++) {
//                        if (binding.state.getText().toString().equalsIgnoreCase(
//                                citiesJSONArray.getJSONObject(i).getString("state"))) {
//                            citiesList.add(citiesJSONArray.getJSONObject(i).getString("name"));
//                        }
//                        Collections.sort(citiesList, String.CASE_INSENSITIVE_ORDER);
//
//                    }
//                    citiesList.add(0, "Select City");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                cityAdpater.notifyDataSetChanged();
//                DialogUtils.stopProgressDialog();
//            }
//
//
//        }.execute();
//    }

    public String readJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
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
                ApiClient.getClient(this).create(ApiInterface.class);
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
