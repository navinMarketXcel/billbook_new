package com.billbook.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.fragment.HelpFragment;
import com.billbook.app.fragment.HomeFragment;
import com.billbook.app.fragment.ProfileFragment;
import com.billbook.app.utils.Util;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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