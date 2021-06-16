package com.billbook.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.services.SyncService;
import com.billbook.app.utils.Constants;
import com.billbook.app.utils.Util;

public class SyncActivity extends AppCompatActivity {
    private static final String TAG = "SyncActivity";
    private ProgressBar pBar;
    private int apiCountMaxProgress = 6;
    private int synchAPICount;
    private SynchReceiver broadCastReceiver;
    private TextView progressInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        initUI();
        startService(new Intent(SyncActivity.this, SyncService.class));
    }

    private void initUI() {
        pBar = findViewById(R.id.pBar);
        pBar.setMax(apiCountMaxProgress);
        progressInt = findViewById(R.id.progressInt);
        broadCastReceiver = new SynchReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.SYNC_DATA_ACTION);
        this.registerReceiver(broadCastReceiver, intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadCastReceiver);
    }

    private void gotoHome() {
        if (getIntent().hasExtra("killSelf")) {
            this.finish();
        } else
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intentHome = new Intent(SyncActivity.this, HomeActivity.class);
                    startActivity(intentHome);
                    finish();
                }
            }, 500);
    }

    class SynchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() == Constants.SYNC_DATA_ACTION) {
                synchAPICount++;
                Log.v(TAG, " SynchReceiver synchAPICount::" + synchAPICount);
                Log.v(TAG, "SynchReceiver API_NAME::" + intent.getStringExtra(Constants.API_NAME));
                pBar.setProgress(synchAPICount);
                progressInt.setText((int) (synchAPICount * 16.66) + "%");
                if (synchAPICount == apiCountMaxProgress) {
                    progressInt.setText("100%");
                    long lastTimestamp = Util.getCurrentLongDate();
                    MyApplication.saveLAST_SYNC_TIMESTAMP(lastTimestamp);

                    gotoHome();
                }
            }

        }
    }
}
