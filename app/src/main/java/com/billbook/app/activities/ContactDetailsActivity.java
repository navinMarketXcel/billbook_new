package com.billbook.app.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.billbook.app.R;
import com.billbook.app.fragment.HelpFragment;
import com.billbook.app.fragment.HomeFragment;
import com.billbook.app.fragment.ProfileFragment;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactDetailsActivity extends AppCompatActivity {
ImageView ivToolBarBack;
TextView changePhone;
EditText etPhone,etEmail;
Button btnSave;
    private JSONObject profile;
    private long userid;
    LinearLayout lnHelp,lnYouTube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        changePhone = findViewById(R.id.changePhone);
        btnSave = findViewById(R.id.btnSave);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        lnHelp = findViewById(R.id.lnHelp);
        lnYouTube = findViewById(R.id.lnYouTube);
        try {
            profile = new JSONObject(((MyApplication) getApplication()).getUserDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("Profile ", profile.toString());
        setonClick();
        setUserData();
    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
        changePhone.setOnClickListener(v -> {
            Intent intent = new Intent(ContactDetailsActivity.this, EditMobileNoActivity.class);
            startActivity(intent);
        });
        btnSave.setOnClickListener(v -> {
            if(verifyData()) updateUserProfile();
        });
        lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(ContactDetailsActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(ContactDetailsActivity.this);
        });
    }
    private void setUserData() {

        try {
            etPhone.setText(profile.has("mobileNo") ? profile.getString("mobileNo") : "");
            etEmail.setText(profile.has("email") ? profile.getString("email") : "");
            userid = profile.getLong("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateUserProfile() {
        Util.postEvents("Update Profile", "Update Profile", ContactDetailsActivity.this.getApplicationContext());
        try {
            profile.put("email", etEmail.getText().toString());
            profile.put("mobileNo", etPhone.getText().toString());
            MyApplication.saveUserDetails(profile.toString());

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

        RequestBody email = RequestBody.create(etEmail.getText().toString(),MediaType.parse("multipart/form-data"));
        RequestBody mobileNo = RequestBody.create(etPhone.getText().toString(),MediaType.parse("multipart/form-data"));

        Map<String, RequestBody> map = new HashMap<>();
        map.put("email", email);
        map.put("mobileNo", mobileNo);


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
                       ContactDetailsActivity.this.finish();
                    } else {
                        DialogUtils.showToast(ContactDetailsActivity.this, "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(ContactDetailsActivity.this, "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(ContactDetailsActivity.this, "Failed update profile to server");
            }
        });
    }
    private boolean verifyData() {

        if (etPhone.getText().toString().isEmpty() || etPhone.getText().toString().length() < 10) {
            etPhone.setError( "Invalid phone number");
            return false;
        }
        else if (etEmail.getText().toString().isEmpty()) {
            etEmail.setError( "Please enter email id ");
            return false;
        }
        return true;
    }
}