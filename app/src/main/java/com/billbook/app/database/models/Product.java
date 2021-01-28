package com.billbook.app.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(indices = {@Index(value = "id", unique = true)})

public class Product implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @SerializedName("local_id")
    @ColumnInfo(name = "local_id")
    private int local_id;

    @NonNull
    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("name")
    @ColumnInfo(name = "name")
    private String name;

    @SerializedName("barcode")
    @ColumnInfo(name = "barcode")
    private String barcode;

    @SerializedName("description")
    @ColumnInfo(name = "description")
    private String description;

    @SerializedName("category")
    @ColumnInfo(name = "category")
    private int category;

    @SerializedName("brand")
    @ColumnInfo(name = "brand")
    private int brand;

    @SerializedName("price")
    @ColumnInfo(name = "price")
    private int price;

    @SerializedName("code")
    @ColumnInfo(name = "code")
    private String code;

    @SerializedName("item_id")
    @ColumnInfo(name = "item_id")
    private String item_id;

   /* @SerializedName("specification")
    @ColumnInfo(name = "specification")
    private String specification;*/

    @SerializedName("is_active")
    @ColumnInfo(name = "is_active")
    private boolean is_active;

    @SerializedName("createdAt")
    @ColumnInfo(name = "createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    @ColumnInfo(name = "updatedAt")
    private String updatedAt;

    @SerializedName("IsBestSeller")
    @ColumnInfo(name = "IsBestSeller")
    private boolean isBestSeller;


    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getBrand() {
        return brand;
    }

    public void setBrand(int brand) {
        this.brand = brand;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

   /* public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }*/

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean getIsBestSeller() {
        return isBestSeller;
    }

    public void setIsBestSeller(boolean isBestSeller) {
        this.isBestSeller = isBestSeller;
    }

    @Override
    public String toString() {
        return name;
    }


}
