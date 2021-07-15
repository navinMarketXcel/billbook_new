package com.billbook.app.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class User implements Serializable {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("username")
    @ColumnInfo(name = "username")
    private String username;

    @SerializedName("email")
    @ColumnInfo(name = "email")
    private String email;

    @SerializedName("password")
    @ColumnInfo(name = "password")
    private String password;

    @SerializedName("ledger_password")
    @ColumnInfo(name = "ledger_password")
    private String ledger_password;

    @SerializedName("first_name")
    @ColumnInfo(name = "first_name")
    private String first_name;

    @SerializedName("last_name")
    @ColumnInfo(name = "last_name")
    private String last_name;

    @SerializedName("address")
    @ColumnInfo(name = "address")
    private String address;

    @SerializedName("mobile_no")
    @ColumnInfo(name = "mobile_no")
    private String mobile_no;

    @SerializedName("shop_name")
    @ColumnInfo(name = "shop_name")
    private String shop_name;

    @SerializedName("gst_no")
    @ColumnInfo(name = "gst_no")
    private String gst_no;

    @SerializedName("code")
    @ColumnInfo(name = "code")
    private String code;

    @SerializedName("landline_no")
    @ColumnInfo(name = "landline_no")
    private String landline_no;

    @SerializedName("shop_type")
    @ColumnInfo(name = "shop_type")
    private String shop_type;

    @SerializedName("shop_kind")
    @ColumnInfo(name = "shop_kind")
    private String shop_kind;

    @SerializedName("payment_method")
    @ColumnInfo(name = "payment_method")
    private String payment_method;

    @SerializedName("is_finance_available")
    @ColumnInfo(name = "is_finance_available")
    private boolean is_finance_available;

    @SerializedName("is_panel_member")
    @ColumnInfo(name = "is_panel_member")
    private boolean is_panel_member;

    @SerializedName("image")
    @ColumnInfo(name = "image")
    private String image;

    @SerializedName("createdAt")
    @ColumnInfo(name = "createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    @ColumnInfo(name = "updatedAt")
    private String updatedAt;

    @SerializedName("askedForGST")
    @ColumnInfo(name = "askedForGST")
    private boolean askedForGST;

//    @SerializedName("category")
//    @ColumnInfo(name = "category")
//    private String category;

    @SerializedName("brand")
    @ColumnInfo(name = "brand")
    private String brand;

    @SerializedName("product")
    @ColumnInfo(name = "product")
    private String product;

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

//    public String getCategory() {
//        return category;
//    }
//
//    public void setCategory(String category) {
//        this.category = category;
//    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLedger_password() {
        return ledger_password;
    }

    public void setLedger_password(String ledger_password) {
        this.ledger_password = ledger_password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getGst_no() {
        return gst_no;
    }

    public void setGst_no(String gst_no) {
        this.gst_no = gst_no;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isAskedForGST() {
        return askedForGST;
    }

    public void setAskedForGST(boolean askedForGST) {
        this.askedForGST = askedForGST;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLandline_no() {
        return landline_no;
    }

    public void setLandline_no(String landline_no) {
        this.landline_no = landline_no;
    }

    public String getShop_type() {
        return shop_type;
    }

    public void setShop_type(String shop_type) {
        this.shop_type = shop_type;
    }

    public String getShop_kind() {
        return shop_kind;
    }

    public void setShop_kind(String shop_kind) {
        this.shop_kind = shop_kind;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public boolean isIs_finance_available() {
        return is_finance_available;
    }

    public void setIs_finance_available(boolean is_finance_available) {
        this.is_finance_available = is_finance_available;
    }

    public boolean isIs_panel_member() {
        return is_panel_member;
    }

    public void setIs_panel_member(boolean is_panel_member) {
        this.is_panel_member = is_panel_member;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", ledger_password='" + ledger_password + '\'' +
                ", address='" + address + '\'' +
                ", mobile_no='" + mobile_no + '\'' +
                ", shop_name='" + shop_name + '\'' +
                ", gst_no='" + gst_no + '\'' +
                ", image='" + image + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
