package com.billbook.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.fragment.HelpFragment;
import com.billbook.app.fragment.HomeFragment;
import com.billbook.app.fragment.ProfileFragment;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.networkcommunication.LoginRequest;
import com.billbook.app.networkcommunication.LoginResponse;
import com.billbook.app.utils.Util;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomNavigationActivity extends AppCompatActivity {
TextView txtToolBarTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
         txtToolBarTitle = findViewById(R.id.txtToolBarTitle);
        txtToolBarTitle.setText(R.string.home);
        addFragment(new HomeFragment());
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Toast.makeText(this,
                "PackageName = " + info.packageName + "\nVersionCode = "
                        + info.versionCode + "\nVersionName = "
                        + info.versionName + "\nPermissions = " + info.permissions, Toast.LENGTH_SHORT).show();
        Log.v("vesion name",info.versionName);
        Log.v("vesion code",String.valueOf(info.versionCode));
        sendUserMetaData("9999999999", info.versionName,info.versionCode,"one plus","123" ,"Testing");

        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.nav_home:
                        txtToolBarTitle.setText(R.string.home);
                        replaceFragment(new HomeFragment());
                        return true;
                    case R.id.nav_my_profile:
                        txtToolBarTitle.setText(R.string.profile);
                        replaceFragment(new ProfileFragment());
                        return true;
                    case R.id.nav_help:
                        txtToolBarTitle.setText(R.string.help);
                        replaceFragment(new HelpFragment());
                       // startActivity(new Intent(getApplicationContext(),HelpActivity.class));
                       // overridePendingTransition(0,0);
                        return true;

                }

                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    public void addFragment(Fragment frag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, frag);
        transaction.commit();
    } public void replaceFragment( Fragment frag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, frag);
        transaction.commit();
    }
    public void startHelpActivity(View v){
        boolean installed = appInstallOrNot("com.whatsapp");
        String mobNo = "9289252155" ;
        if(installed)
        {
            Intent intent= new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+"+91"+mobNo));
            startActivity(intent);
        }
        else
        {
            Toast.makeText(BottomNavigationActivity.this,"Please Install Whatsapp", Toast.LENGTH_SHORT).show();
        }

    }
    private void sendUserMetaData(String mobno,String versionName,int versionNo,String deviceVersionName,String userid,String demo) {
        JsonObject userDataJson = new JsonObject();
        JsonObject userInfo = new JsonObject();
        userDataJson.add("mobileNo", new JsonParser().parse(mobno));
        userDataJson.add("versionNo",new JsonParser().parse(String.valueOf(versionNo)));
        userDataJson.add("versionName",new JsonParser().parse(versionName));
        userDataJson.add("deviceVersionName",new JsonParser().parse("one plus"));
        userInfo.add("userid",new JsonParser().parse(userid));
        userInfo.add("Demo",new JsonParser().parse(demo));
        userDataJson.add("userData",userInfo);

        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        Call<Object> call = apiService.updateUserMetaData(headerMap,userDataJson);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

            System.out.println("user Meta data obj :"+response);

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                // Log error here since request failed

            }
        });
    }
    private boolean appInstallOrNot(String url)
    {
        PackageManager packageManager = getPackageManager();
        boolean app;
        try {
            packageManager.getPackageInfo(url,PackageManager.GET_ACTIVITIES);
            app=true;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            app=false;
        }

        return app;

    }
    public void startYoutubeActivity(View v){
        Util.postEvents("Watch Demo","Watch Demo",this.getApplicationContext());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.youtube.com/playlist?list=PLFuhsI7LfH3VFoH8oTfozpUlITI6fy7U8"));
        intent.setPackage("com.google.android.youtube");
        startActivity(intent);
    }
}