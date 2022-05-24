package com.billbook.app.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.billbook.app.R;

public class BusinessDetailsActivity extends AppCompatActivity {
ImageView ivToolBarBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_details);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);

        setonClick();
    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
    }

}