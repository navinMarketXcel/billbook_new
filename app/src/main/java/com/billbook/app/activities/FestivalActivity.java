package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.billbook.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FestivalActivity extends AppCompatActivity {
    private static final int SPLASH_SCREEN_TIMEOUT = 2000;
    private static final String TAG = "SplashActivity";
    private  String validity="05/01/2020";
    SimpleDateFormat formatter=new SimpleDateFormat("dd/MM/yyyy");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if(formatter.parse(validity).getTime()>new Date().getTime()){
                setContentView(R.layout.festival_activity_layout);
                showFestival();
            }else{
                gotoSplashScreen();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        showFestival();
    }
    private void gotoSplashScreen(){
        Intent intent = new Intent(this,SplashActivity.class);
        startActivity(intent);
        this.finish();;
    }
    private void showFestival() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoSplashScreen();
            }
        }, SPLASH_SCREEN_TIMEOUT);
    }
}
