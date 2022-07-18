package com.billbook.app.database.models;

import com.google.gson.annotations.SerializedName;

public class Customer {
    @SerializedName("customerName")
    private String customerNameame;
    @SerializedName("mobileNo")
    private String mobileNo;
    @SerializedName("isSelected")
    private boolean isSelected;
    @SerializedName("customerAddress")
    private String customerAddress;

    public Customer(String customerNameame, String mobileNo, boolean isSelected,String customerAddress) {
        this.customerNameame = customerNameame;
        this.mobileNo = mobileNo;
        this.isSelected = isSelected;
        this.customerAddress = customerAddress;
    }

    public String getCustomerNameame() {
        return customerNameame;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public void setCustomerNameame(String customerNameame) {
        this.customerNameame = customerNameame;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
