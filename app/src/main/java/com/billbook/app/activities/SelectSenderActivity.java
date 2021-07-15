package com.billbook.app.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.billbook.app.R;
import com.billbook.app.adapters.CustomerSelectionAdapter;
import com.billbook.app.database.models.Customer;

import java.util.ArrayList;
import java.util.Objects;

public class SelectSenderActivity extends AppCompatActivity {
    private RecyclerView customerRV;
    private ArrayList<Customer> customers = new ArrayList<>();
    private CustomerSelectionAdapter customerSelectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sender);
        customerRV = findViewById(R.id.customerRV);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Send Customers");
        getCustomerData();
    }

    private void getCustomerData() {
        for (int i = 0; i < 10; i++) {
            Customer customer = new Customer("Dummy User", "+91909660356" + i, false);
            customers.add(customer);
        }
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, true);
        customerRV.setLayoutManager(mLayoutManager);
        customerSelectionAdapter = new CustomerSelectionAdapter(customers, this);
        customerRV.setAdapter(customerSelectionAdapter);
    }
}
