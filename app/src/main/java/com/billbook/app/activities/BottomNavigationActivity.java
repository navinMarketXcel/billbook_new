package com.billbook.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.fragment.HelpFragment;
import com.billbook.app.fragment.HomeFragment;
import com.billbook.app.fragment.ProfileFragment;
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

    public void addFragment( Fragment frag) {
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
}