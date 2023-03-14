package com.billbook.app.activities;

import static com.billbook.app.utils.Util.getRequestBodyFormData;

import android.content.ContentResolver;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusinessDetailsActivity extends AppCompatActivity {
ImageView ivToolBarBack;
    EditText etBusiness,etPinCode,etShopAdd,etCity,etState,etGstNum;
    private JSONObject profile;
    private long userid;
    Button btnSave;
    LinearLayout lnHelp,lnYouTube;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        btnSave = findViewById(R.id.btnSave);
        etBusiness = findViewById(R.id.etBusiness);
        etShopAdd = findViewById(R.id.etShopAdd);
        etPinCode = findViewById(R.id.etPinCode);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etGstNum = findViewById(R.id.etGstNum);
        lnHelp = findViewById(R.id.lnHelp);
        lnYouTube = findViewById(R.id.lnYouTube);
        try {
            profile = new JSONObject(((MyApplication) getApplication()).getUserDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setonClick();
        setUserData();
    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            Util.pushEvent("Clicked on back from Toolbar in Business Details");
            finish();
        });
        btnSave.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Save in Business Details");
            if(verifyData())updateUserProfile();
            Toast.makeText(BusinessDetailsActivity.this, "Business Details Uploaded Successfully", Toast.LENGTH_SHORT).show();
        });
        lnHelp.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Whatsapp Help from Toolbar in Business Details");
           Util. startHelpActivity(BusinessDetailsActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Youtube Demo from Toolbar in Business Details");
           Util. startYoutubeActivity(BusinessDetailsActivity.this);
        });
    }
    private void setUserData() {

        try {
            etBusiness.setText(profile.has("shopName") ? profile.getString("shopName") : "");
            etShopAdd.setText(profile.has("shopAddr") ? profile.getString("shopAddr") : "");
            etPinCode.setText(profile.has("pincode") ? profile.getString("pincode") : "");
            etCity.setText(profile.has("city") ? profile.getString("city") : "");
            etState.setText(profile.has("state") ? profile.getString("state") : "");
            etGstNum.setText(profile.has("gstNo") ? profile.getString("gstNo") : "");
            userid = profile.getLong("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    private void updateUserProfile() {
        Util.postEvents("Update Profile", "Update Profile", BusinessDetailsActivity.this.getApplicationContext());
        try {
            profile.put("shopAddr", etShopAdd.getText().toString());
            profile.put("shopName", etBusiness.getText().toString());
            profile.put("gstNo", etGstNum.getText().toString());
            profile.put("state", etState.getText().toString());
            profile.put("city", etCity.getText().toString());
            MyApplication.saveUserDetails(profile.toString());
            if(etGstNum.getText().toString().isEmpty())MyApplication.setIsGst(false);

            updateUserAPI();

        } catch (JSONException | IOException e) {

            e.printStackTrace();
        }
    }
    private void updateUserAPI() throws IOException {
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        Map<String, RequestBody> map = new HashMap<>();
        map.put("shopAddr", getRequestBodyFormData(etShopAdd.getText().toString()));
        map.put("shopName", getRequestBodyFormData(etBusiness.getText().toString()));
        map.put("gstNo", getRequestBodyFormData(etGstNum.getText().toString()));
        map.put("pincode", getRequestBodyFormData(etPinCode.getText().toString()));
        map.put("state", getRequestBodyFormData(etState.getText().toString()));
        map.put("city", getRequestBodyFormData(etCity.getText().toString()));

        File profileFile = null, companyFile = null, signatureFile = null;
        MultipartBody.Part profileFilePart = null, companyFilePart = null, signatureFilePart = null;

        ContentResolver cR = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        Call<Object> call = null;

        call = apiService.updateUser(headerMap, userid, profileFilePart, map, companyFilePart, signatureFilePart);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    if (body.getBoolean("status")) {
                        MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                        MyApplication.saveUserToken(body.getJSONObject("data").getString("userToken"));
                        BusinessDetailsActivity.this.finish();
                    } else {
                        DialogUtils.showToast(BusinessDetailsActivity.this, "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(BusinessDetailsActivity.this, "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(BusinessDetailsActivity.this, "Failed update profile to server");
            }
        });
    }
    private boolean verifyData() {

        if (etBusiness.getText().toString().isEmpty()) {
            etBusiness.setError( "Shop Name can not be empty");
            return false;
        } else if (etShopAdd.getText().toString().isEmpty()) {
            etShopAdd.setError( "Shop Address can not be empty");
            return false;
        }else if (etPinCode.getText().toString().isEmpty() || etPinCode.getText().length() < 6) {
            etPinCode.setError( "Please enter a valid Pincode");
            return false;
        }else if (etCity.getText().toString().isEmpty()) {
            etCity.setError( "City can not be empty");
            return false;
        }else if (etState.getText().toString().isEmpty()) {
            etState.setError( "State can not be empty");
            return false;
        }else if (etGstNum.getText().toString().length()<15 &&etGstNum.getText().toString().length()!=0 ) {
            etGstNum.setError( "GST number can not be less than 15 characters");
            return false;
        }
        return true;
    }
}