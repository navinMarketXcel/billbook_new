package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.billbook.app.R;

public class TermsAndConditionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_condition);
    }

    public void agree(View v) {
        MyApplication.setTermsAndConditions(true);
        Intent intent = new Intent();
        intent.putExtra
                ("agrreed", true);
        setResult(RESULT_OK, intent);
        finish();
    }
}
