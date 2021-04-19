package com.billbook.app.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(indices = {@Index(value = "id", unique = true)})

public class Brand {

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

    @SerializedName("description")
    @ColumnInfo(name = "description")
    private String description;


    @SerializedName("category")
    @ColumnInfo(name = "category")
    private int category;


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

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
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
