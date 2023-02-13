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
import android.os.Build;
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
import com.billbook.app.model.appmetadata.AppMetaData;
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
private String version_name,deviceVersionName;
private String mobNo= " ",userId = " ";
private int versionCode;
private JSONObject userProfile;
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
        assert info != null;
        version_name = info.versionName;
        versionCode = info.versionCode;
        Log.v("ver no",String.valueOf(info.versionCode));
        try {
            userProfile= new JSONObject (MyApplication.getUserDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
             mobNo = userProfile.getString("mobileNo");
             userId = userProfile.getString("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        deviceVersionName = android.os.Build.MANUFACTURER+android.os.Build.MODEL;
        System.out.println("mob no: " +mobNo+ " user id" + userId + "Phone name" +deviceVersionName);
        if(MyApplication.getUserMetaDataFlag())
        {
            getUserMetaData(mobNo);
        }




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
    private void saveUserMetaData(String mobno,String versionName,int versionNo,String deviceVersionName,String userid,String demo) {
        JSONObject userDataJson = new JSONObject();
        JSONObject userInfo = new JSONObject();
        try {
            userDataJson.put("mobileNo", mobno);

            userDataJson.put("versionNo",versionNo);
            userDataJson.put("versionName",versionName);
            userDataJson.put("deviceVersionName",deviceVersionName);
            userInfo.put("userid",userid);
            userInfo.put("Demo",demo);
            userDataJson.put("userData",userInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonParser jsonParser = new JsonParser();
        JsonObject gsonObject = (JsonObject)jsonParser.parse(userDataJson.toString());

        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        Call<AppMetaData> call = apiService.updateUserMetaData(headerMap,gsonObject);
        call.enqueue(new Callback<AppMetaData>() {
            @Override
            public void onResponse(Call<AppMetaData> call, Response<AppMetaData> response) {
            System.out.println("saveUserMetaData"+ response.body().getStatus());
            MyApplication.setuserMetaDataFlag(false);
            }

            @Override
            public void onFailure(Call<AppMetaData> call, Throwable t) {
                // Log error here since request failed

            }
        });
    }
    private void getUserMetaData(String mobNo) {
        JSONObject userDataJson = new JSONObject();
        try {
            userDataJson.put("mobileNo",mobNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonParser jsonParser = new JsonParser();
        JsonObject gsonObject = (JsonObject)jsonParser.parse(userDataJson.toString());

        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        Call<AppMetaData> call = apiService.getUserMetaData(headerMap,gsonObject);
        call.enqueue(new Callback<AppMetaData>() {
            @Override
            public void onResponse(Call<AppMetaData> call, Response<AppMetaData> response) {

               // System.out.println("user Meta data obj getUserMetadata :"+response.body().getData().getMobileNo());
                if(response.body().getStatus())
                {
                    Log.v("response of meta",response.body().toString());
                    System.out.println("ver name"+version_name + "ver no"+versionCode);
                    if(version_name.equals(response.body().getData().getVersionName()) && versionCode == response.body().getData().getVersionNo())
                    {
                        return;

                    }
                    else
                    {
                        saveUserMetaData(mobNo,version_name,versionCode,deviceVersionName,userId ,"Testing");
                    }
                }
                else
                {
                    saveUserMetaData(mobNo,version_name,versionCode,deviceVersionName,userId ,"Testing");
                }

            }

            @Override
            public void onFailure(Call<AppMetaData> call, Throwable t) {
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
        Util.startYoutubeActivity(this);

    }
}