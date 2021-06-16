package com.billbook.app.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

@Entity(indices = {@Index(value = "id", unique = true)})
public class Category {
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

    @SerializedName("is_active")
    @ColumnInfo(name = "is_active")
    private boolean is_active;

    @SerializedName("createdAt")
    @ColumnInfo(name = "createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    @ColumnInfo(name = "updatedAt")
    private String updatedAt;

    // this flag is added for catgory Slection activity
    private boolean isSelected = false;

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Category)) return false;

        Category other = (Category) o;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }
}
