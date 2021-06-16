package com.billbook.app.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.utils.Util;

import java.util.Objects;

public class HelpActivity extends AppCompatActivity {
    private boolean isFromHelpTab;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help2);
        TextView helpText = findViewById(R.id.helpText);
        isFromHelpTab = getIntent().getBooleanExtra("isFromHelpTab",false);
        helpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFromHelpTab)
                Util.postEvents("HelpTab"," help.billbook@gmail.com from help tab",HelpActivity.this.getApplicationContext());
                else
                Util.postEvents("HelpLineTab tab"," help.billbook@gmail.com from help tab",HelpActivity.this.getApplicationContext());

            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
