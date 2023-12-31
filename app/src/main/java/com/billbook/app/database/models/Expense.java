package com.billbook.app.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(indices = {@Index(value = "name", unique = true) })
public class Expense implements Serializable {
    @PrimaryKey()
    @SerializedName("name")
    @ColumnInfo(name = "name" )
    @NonNull
    private String name;
    @SerializedName("amount")
    @ColumnInfo(name = "amount")
    private float amount;
    @SerializedName("expenseDate")
    @ColumnInfo(name = "expenseDate")
    private String expenseDate;

    @SerializedName("userid")
    @ColumnInfo(name = "userid")
    private int userid;

    @SerializedName("id")
    @ColumnInfo(name = "id")
    private long id;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDate() {
        return expenseDate;
    }

    public void setDate(String date) {
        this.expenseDate = date;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

}
