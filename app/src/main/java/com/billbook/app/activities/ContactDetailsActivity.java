package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.billbook.app.R;
import com.billbook.app.fragment.HelpFragment;
import com.billbook.app.fragment.HomeFragment;
import com.billbook.app.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ContactDetailsActivity extends AppCompatActivity {
ImageView ivToolBarBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_details);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);

        setonClick();
    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
    }

}