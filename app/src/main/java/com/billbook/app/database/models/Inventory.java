package com.billbook.app.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class Inventory implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("local_id")
    @ColumnInfo(name = "local_id")
    private int localId;

    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("invoiceNo")
    @ColumnInfo(name = "invoiceNo")
    private int invoiceNo;

    @SerializedName("quantity")
    @ColumnInfo(name = "quantity")
    private int quantity;


    @SerializedName("distributor")
    @ColumnInfo(name = "distributor")
    private int distributor;


    @SerializedName("price")
    @ColumnInfo(name = "price")
    private float price;

    @SerializedName("tax")
    @ColumnInfo(name = "tax")
    private float tax;

    @SerializedName("selling_price")
    @ColumnInfo(name = "selling_price")
    private float selling_price;

    @SerializedName("is_active")
    @ColumnInfo(name = "is_active")
    private boolean is_active;

    @SerializedName("product")
    @ColumnInfo(name = "product")
    private int product;

    @SerializedName("product_name")
    @ColumnInfo(name = "product_name")
    private String product_name;

    @SerializedName("category")
    @ColumnInfo(name = "category")
    private int category;

    @SerializedName("category_name")
    @ColumnInfo(name = "category_name")
    private String category_name;

    @SerializedName("brand")
    @ColumnInfo(name = "brand")
    private int brand;

    @SerializedName("brand_name")
    @ColumnInfo(name = "brand_name")
    private String brand_name;

    @SerializedName("user")
    @ColumnInfo(name = "user")
    private int user;

    @SerializedName("serial_no")
    @ColumnInfo(name = "serial_no")
    private String serial_no;

    @SerializedName("specification")
    @ColumnInfo(name = "specification")
    private String specification;

    @SerializedName("isUpdated")
    @ColumnInfo(name = "isUpadted")
    private int isUpadted = 1;

    @SerializedName("createdAt")
    @ColumnInfo(name = "createdAt")
    private String createdAt;

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(int invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    @NonNull
    public int getLocalId() {
        return localId;
    }

    public void setLocalId(@NonNull int localId) {
        this.localId = localId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public int getProduct_id() {
        return product;
    }

    public void setProduct_id(int product_id) {
        this.product = product_id;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public String getSerial_no() {
        return serial_no;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public int getProduct() {
        return product;
    }

    public void setProduct(int product) {
        this.product = product;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getBrand() {
        return brand;
    }

    public void setBrand(int brand) {
        this.brand = brand;
    }

    public String getBrand_name() {
        return brand_name;
    }

    public void setBrand_name(String brand_name) {
        this.brand_name = brand_name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getTax() {
        return tax;
    }


    public void setTax(float tax) {
        this.tax = tax;
    }

    public float getSelling_price() {
        return selling_price;
    }

    public void setSelling_price(float selling_price) {
        this.selling_price = selling_price;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public int getIsUpadted() {
        return isUpadted;
    }

    public void setIsUpadted(int isUpadted) {
        this.isUpadted = isUpadted;
    }

    public int getDistributor() {
        return distributor;
    }

    public void setDistributor(int distributor) {
        this.distributor = distributor;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "localId=" + localId +
                ", id=" + id +
                ", invoiceNo=" + invoiceNo +
                ", quantity=" + quantity +
                ", price=" + price +
                ", tax=" + tax +
                ", selling_price=" + selling_price +
                ", is_active=" + is_active +
                ", product=" + product +
                ", product_name='" + product_name + '\'' +
                ", category=" + category +
                ", category_name='" + category_name + '\'' +
                ", brand=" + brand +
                ", brand_name='" + brand_name + '\'' +
                ", user=" + user +
                ", serial_no='" + serial_no + '\'' +
                ", specification='" + specification + '\'' +
                ", isUpadted='" + isUpadted + '\'' +
                '}';
    }
}
