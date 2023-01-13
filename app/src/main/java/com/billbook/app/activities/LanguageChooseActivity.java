package com.billbook.app.activities;

import static android.view.View.GONE;
import static com.billbook.app.utils.Util.getRequestBodyFormData;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LanguageChooseActivity extends AppCompatActivity {

    ImageView ivToolBarBack;

    private long userid;
    private JSONObject profile;
    LinearLayout lnHelp,lnYouTube;
    RelativeLayout layoutEnglish,layoutHindi,layoutMarathi,layoutGujrati;
    ImageView imgEnglish,imgHindi,imgMarathi,imgGujrati;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        lnHelp = findViewById(R.id.lnHelp);
        lnYouTube = findViewById(R.id.lnYouTube);
        layoutEnglish = findViewById(R.id.layoutEnglish);
        layoutHindi = findViewById(R.id.layoutHindi);
        imgEnglish = findViewById(R.id.imgEnglish);
        imgHindi = findViewById(R.id.imgHindi);
        layoutMarathi = findViewById(R.id.layoutMarathi);
        layoutGujrati = findViewById(R.id.layoutGujarati);
        imgMarathi = findViewById(R.id.imgMarathi);
        imgGujrati = findViewById(R.id.imgGujrati);

        try {
            profile = new JSONObject(((MyApplication) getApplication()).getUserDetails());
            userid = profile.getLong("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setonClick();
        setUserData();
    }
    private void setUserData() {

    if(MyApplication.getLanguage().equals("hi")){
        imgEnglish.setImageResource(R.drawable.ic_language_unselected);
        imgHindi.setImageResource(R.drawable.ic_language_selected);
        imgGujrati.setImageResource(R.drawable.ic_language_unselected);
        imgMarathi.setImageResource(R.drawable.ic_language_unselected);
    }
    else if(MyApplication.getLanguage().equals("ma"))
    {
        imgEnglish.setImageResource(R.drawable.ic_language_unselected);
        imgHindi.setImageResource(R.drawable.ic_language_unselected);
        imgGujrati.setImageResource(R.drawable.ic_language_unselected);
        imgMarathi.setImageResource(R.drawable.ic_language_selected);
        }
    else if(MyApplication.getLanguage().equals("gu"))
    {
        imgEnglish.setImageResource(R.drawable.ic_language_unselected);
        imgHindi.setImageResource(R.drawable.ic_language_unselected);
        imgGujrati.setImageResource(R.drawable.ic_language_selected);
        imgMarathi.setImageResource(R.drawable.ic_language_unselected);
    }
    else{
        imgEnglish.setImageResource(R.drawable.ic_language_selected);
        imgHindi.setImageResource(R.drawable.ic_language_unselected);
        imgGujrati.setImageResource(R.drawable.ic_language_unselected);
        imgMarathi.setImageResource(R.drawable.ic_language_unselected);
    }

    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });

        lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(LanguageChooseActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(LanguageChooseActivity.this);
        });
        layoutEnglish.setOnClickListener(v -> {
            imgEnglish.setImageResource(R.drawable.ic_language_selected);
            imgHindi.setImageResource(R.drawable.ic_language_unselected);
            updateLanguage("en");
            finish();

        });
        layoutHindi.setOnClickListener(v -> {
            imgEnglish.setImageResource(R.drawable.ic_language_unselected);
            imgHindi.setImageResource(R.drawable.ic_language_selected);
            updateLanguage("hi");
            finish();

        });
        layoutMarathi.setOnClickListener(view -> {
            imgEnglish.setImageResource(R.drawable.ic_language_selected);
            imgHindi.setImageResource(R.drawable.ic_language_unselected);
            updateLanguage("ma-rIn");
            finish();
        });
        layoutGujrati.setOnClickListener(view -> {
            imgEnglish.setImageResource(R.drawable.ic_language_selected);
            imgHindi.setImageResource(R.drawable.ic_language_unselected);
            updateLanguage("gu-rIN");
            finish();
        });

    }

    public void updateLanguage(String lng) {
        Locale locale = new Locale(lng);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        MyApplication.saveLanguage(lng);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        Intent intent = new Intent(LanguageChooseActivity.this, BottomNavigationActivity.class);
        startActivity(intent);
        finish();
    }

}