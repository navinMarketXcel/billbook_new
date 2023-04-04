package com.billbook.app.activities;

import static com.billbook.app.utils.Util.getRequestBodyFormData;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TermsAndConditionsNewActivity extends AppCompatActivity {
    ImageView ivToolBarBack;

    EditText etTc;
    Button btnSaveTc;
    LinearLayout lnHelp,lnYouTube;
    private long userid;
    private JSONObject profile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions_new);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        btnSaveTc = findViewById(R.id.btnSaveTc);
        etTc = findViewById(R.id.etTc);
        lnHelp = findViewById(R.id.lnHelp);
        lnYouTube = findViewById(R.id.lnYouTube);
       // etTc.addTextChangedListener(tncWatcher);
        try {
            profile = new JSONObject(((MyApplication) getApplication()).getUserDetails());
            userid = profile.getLong("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setonClick();
        setUserData();
    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            Util.pushEvent("Clicked On Back Fom ToolBar in Terms And Conditions");
            finish();
        });

        btnSaveTc.setOnClickListener(v -> {
            updateUserProfile();


        });
        lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(TermsAndConditionsNewActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(TermsAndConditionsNewActivity.this);
        });
    }
    private TextWatcher tncWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(etTc.getText().toString().isEmpty())
            {
                btnSaveTc.setBackground(getDrawable(R.drawable.new_structure));
                btnSaveTc.setTextColor(getResources().getColor(R.color.Silver));
                btnSaveTc.setEnabled(false);
            }
            else {
                btnSaveTc.setBackground(getDrawable(R.drawable.button_click_structure));
                btnSaveTc.setTextColor(getResources().getColor(R.color.white));
                btnSaveTc.setEnabled(true);

            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void updateUserProfile() {
        Util.postEvents("Update Profile", "Update Profile", TermsAndConditionsNewActivity.this.getApplicationContext());
        try {
            profile.put("tnc", etTc.getText().toString());
            MyApplication.saveUserDetails(profile.toString());
            updateUserAPI();
        }
        catch (JSONException | IOException e) {

            e.printStackTrace();
        }
    }

    private void setUserData()
    {
        try {
            etTc.setText(profile.has("tnc") ? profile.getString("tnc") : "");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUserAPI() throws IOException {
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        Map<String, RequestBody> map = new HashMap<>();
        map.put("tnc", getRequestBodyFormData(etTc.getText().toString()));
        Log.v("map of tc",map.toString());



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
                        TermsAndConditionsNewActivity.this.finish();
                    } else {
                        DialogUtils.showToast(TermsAndConditionsNewActivity.this, "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(TermsAndConditionsNewActivity.this, "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(TermsAndConditionsNewActivity.this, "Failed update profile to server");
            }
        });
    }

}