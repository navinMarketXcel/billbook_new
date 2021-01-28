package com.billbook.app.networkcommunication;

import com.google.gson.annotations.SerializedName;
import com.billbook.app.database.models.Customer;

import java.util.ArrayList;

public class CustomerResponse {

    @SerializedName("results")
    private ArrayList<Customer> results;
}
