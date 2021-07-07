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
    private EditText userNameEdt, shopNameEdt, firstName, lastName, shopAddr, mobileNo,pinCodeEdt;

    private TextView agreeTermsAndConditionText;
    private ArrayList<String> stateList = new ArrayList<>();
    private ArrayList<String> citiesList = new ArrayList<>();
    private ArrayAdapter<String> cityAdpater;
    private ArrayAdapter<String> stateAdpater;
    private JSONArray citiesJSONArray = null;
    private Handler handler;
    private String mobileNoText,OTP;
    private CheckBox termsCondition;
    private AutoCompleteTextView states,city;
    String text = "<font color='#1d4388'>Terms of use</font>";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setTitle("Registration");
        initUI();
        initAdapter();


    }

    public void goToPrivacyPolicy(View v){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://thebillbook.com/privacypolicy.html"));
        startActivity(i);
    }
    public void gotoToTermsAndConsitions(View v){
        Intent intent = new Intent(this,TermsAndConditionActivity.class);
        startActivityForResult(intent,01);
    }
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent intent) {
        if (requestCode == 01 &&
                resultCode == RESULT_OK) {
            termsCondition.setChecked(true);
        }
    }

    private void initUI() {
        mobileNoText = getIntent().getExtras().getString("mobileNo");
        OTP = getIntent().getExtras().getString("otp");
        userNameEdt = findViewById(R.id.userNameEdt);
        handler = new Handler();
        shopNameEdt = findViewById(R.id.shopNameEdt);
        shopAddr = findViewById(R.id.shopAddressEdt);
        mobileNo = findViewById(R.id.mobileNoEdt);
        agreeTermsAndConditionText = findViewById(R.id.agreeTermsAndConditionText);
        mobileNo.setText(mobileNoText);
        states = findViewById(R.id.state);
        pinCodeEdt = findViewById(R.id.pinCodeEdt);
//        termsCondition = findViewById(R.id.agreeTermsAndCondition);
        city = findViewById(R.id.city);
        agreeTermsAndConditionText.setText(Html.fromHtml(text), CheckBox.BufferType.SPANNABLE);

       /* city.setTitle("Select City");
        states.setTitle("Select State");
        stateList.addAll( Arrays.asList(getResources().getStringArray(R.array.states)));
        states.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              if(position ==0){
                      citiesList.clear();
                      citiesList.add("Select City");
                      cityAdpater.notifyDataSetChanged();
              } else{
                  loadCities();
              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

    }

    private void initAdapter() {
        citiesList.add("Select City");
        cityAdpater =  new ArrayAdapter<String>(
                RegistrationActivity.this,
                R.layout.spinner_item, citiesList);
        city.setAdapter(cityAdpater);


        stateAdpater =  new ArrayAdapter<String>(
                RegistrationActivity.this,
                R.layout.spinner_item, stateList);
        states.setAdapter(stateAdpater);

    }

    private boolean validateData() {
       if (shopNameEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Shop name can not be empty.");
            return false;
        }  else if (shopAddr.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Shop address can not be empty.");
            return false;
        } else if (mobileNo.getText().toString().isEmpty() || mobileNo.getText().toString().length() < 10) {
            DialogUtils.showToast(this, "Please enter valid mobile number");
            return false;
        }else if (states.getText().toString().isEmpty()) {
           DialogUtils.showToast(this, "Please select State");
           return false;
       }else if (city.getText().toString().isEmpty()) {
           DialogUtils.showToast(this, "Please select city");
           return false;
       } /*else if (stateList.get(states.getSelectedItemPosition()).isEmpty()) {
            DialogUtils.showToast(this, "Please select State");
            return false;
        }else if (citiesList.get(city.getSelectedItemPosition()).isEmpty()) {
           DialogUtils.showToast(this, "Please select city");
           return false;
       }*/ else if(pinCodeEdt.getText().toString().length()<6){
           DialogUtils.showToast(this, "Please enter valid pin code");
           return false;
       }else if(!termsCondition.isChecked()){
           DialogUtils.showToast(this, "Please accept Terms & Conditions");
           return false;
       }
        else return true;
    }

    public void registerUser(View view) {
        if(validateData()){
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("shopName",shopNameEdt.getText().toString());
//            headerMap.put("shopAddr",shopAddr.getText().toString() +" "+ stateList.get(states.getSelectedItemPosition())
//                    + ", "+citiesList.get(city.getSelectedItemPosition())+" - "
//                    +pinCodeEdt.getText().toString());
            headerMap.put("shopAddr",shopAddr.getText().toString());
            headerMap.put("mobileNo",mobileNo.getText().toString());
            headerMap.put("state",states.getText().toString());
            headerMap.put("city",city.getText().toString());
            headerMap.put("pincode",pinCodeEdt.getText().toString());
            headerMap.put("otp",OTP);
            register(headerMap);
        }
    }
    private void startHomeActivity(){
        Intent intent = new Intent(this,HomeActivity.class);
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
                    citiesList.add(0,"Select City");
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


    private void register(Map<String, String> req){
        Util.postEvents("Registration","Registration ",this.getApplicationContext());
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
                    Log.v("RESP",body.toString());
                    Headers headerList = response.headers();
                    String token = headerList.get("authorization");
                    body.put("token",token);
                    Log.v("token::",token);
                        ((MyApplication)getApplication()).saveUserDetails(body.getJSONObject("data").toString());
                    ((MyApplication)getApplication()).saveUserToken(token);

                    if(body.getJSONObject("data").has("isGST"))
                        ((MyApplication) getApplication()).setShowGstPopup((Integer) body.getJSONObject("data").get("isGST"));

                    startHomeActivity();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(RegistrationActivity.this,"Failed to get OTP");
            }
        });

    }
}
