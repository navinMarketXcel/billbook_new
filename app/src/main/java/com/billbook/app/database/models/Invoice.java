package com.billbook.app.database.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.billbook.app.utils.TimestampConverter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Kedar Labde @ Benchmark IT Solutions LCC on 20-09-2018.
 */

@Entity
public class Invoice implements Serializable {


    @PrimaryKey(autoGenerate = true)
    @SerializedName("local_id")
    @ColumnInfo(name = "local_id")
    private int local_id;

    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("invoiceDate")
    @ColumnInfo(name = "invoiceDate")
    private String invoiceDate;

    @SerializedName("invoiceName")
    @ColumnInfo(name = "invoiceName")
    private String invoiceName;

    @SerializedName("invoiceEmail")
    @ColumnInfo(name = "invoiceEmail")
    private String invoiceEmail;

    @SerializedName("invoiceAddress")
    @ColumnInfo(name = "invoiceAddress")
    private String invoiceAddress;

    @SerializedName("invoiceCity")
    @ColumnInfo(name = "invoiceCity")
    private String invoiceCity;

    @SerializedName("invoiceState")
    @ColumnInfo(name = "invoiceState")
    private String invoiceState;

    @SerializedName("invoiceMobileNo")
    @ColumnInfo(name = "invoiceMobileNo")
    private String invoiceMobileNo;

    @SerializedName("is_active")
    @ColumnInfo(name = "is_active")
    private Boolean is_active = true;

//  @SerializedName("payment_method")
//  @ColumnInfo(name = "payment_method")
//  private String paymentmethod;

    @SerializedName("createdAt")
    @ColumnInfo(name = "createdAt")
    @TypeConverters({TimestampConverter.class})
    private Date createdAt;

    @SerializedName("payment_method1")
    @ColumnInfo(name = "payment_method1")
    private String paymentMethod1;

    @SerializedName("payment_method2")
    @ColumnInfo(name = "payment_method2")
    private String paymentMethod2;

    @SerializedName("payment_method1_amount")
    @ColumnInfo(name = "payment_method1_amount")
    private float paymentMethod1Amount;

    @SerializedName("payment_method2_amount")
    @ColumnInfo(name = "payment_method2_amount")
    private float paymentMethod2Amount;

    @SerializedName("updatedAt")
    @ColumnInfo(name = "updatedAt")
    private String updatedAt;

    @SerializedName("invoice_no")
    @ColumnInfo(name = "invoice_no")
    private int invoice_no;

    @SerializedName("user")
    @ColumnInfo(name = "user")
    private int user;

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }

    public int getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(int invoice_no) {
        this.invoice_no = invoice_no;
    }

/*  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }*/

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    @NonNull
    public int getId() {
        return id;
    }

    public void setId(@NonNull int id) {
        this.id = id;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceName() {
        return invoiceName;
    }

    public void setInvoiceName(String invoiceName) {
        this.invoiceName = invoiceName;
    }

    public String getInvoiceAddress() {
        return invoiceAddress;
    }

    public void setInvoiceAddress(String invoiceAddress) {
        this.invoiceAddress = invoiceAddress;
    }

    public String getInvoiceCity() {
        return invoiceCity;
    }

    public void setInvoiceCity(String invoiceCity) {
        this.invoiceCity = invoiceCity;
    }

    public String getInvoiceState() {
        return invoiceState;
    }

    public void setInvoiceState(String invoiceState) {
        this.invoiceState = invoiceState;
    }

    public String getInvoiceMobileNo() {
        return invoiceMobileNo;
    }

    public void setInvoiceMobileNo(String invoiceMobileNo) {
        this.invoiceMobileNo = invoiceMobileNo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    public String getPaymentMethod1() {
        return paymentMethod1;
    }

    public void setPaymentMethod1(String paymentMethod1) {
        this.paymentMethod1 = paymentMethod1;
    }

    public String getPaymentMethod2() {
        return paymentMethod2;
    }

    public void setPaymentMethod2(String paymentMethod2) {
        this.paymentMethod2 = paymentMethod2;
    }

    public float getPaymentMethod1Amount() {
        return paymentMethod1Amount;
    }

    public void setPaymentMethod1Amount(float paymentMethod1Amount) {
        this.paymentMethod1Amount = paymentMethod1Amount;
    }

    public float getPaymentMethod2Amount() {
        return paymentMethod2Amount;
    }

    public void setPaymentMethod2Amount(float paymentMethod2Amount) {
        this.paymentMethod2Amount = paymentMethod2Amount;
    }

    public String getInvoiceEmail() {
        return invoiceEmail;
    }

    public void setInvoiceEmail(String invoiceEmail) {
        this.invoiceEmail = invoiceEmail;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "local_id=" + local_id +
                ", id=" + id +
                ", invoiceDate='" + invoiceDate + '\'' +
                ", invoiceName='" + invoiceName + '\'' +
                ", invoiceAddress='" + invoiceAddress + '\'' +
                ", invoiceState='" + invoiceState + '\'' +
                ", invoiceMobileNo='" + invoiceMobileNo + '\'' +
                ", is_active=" + is_active +
                ", createdAt=" + createdAt +
                ", updatedAt='" + updatedAt + '\'' +
                ", user=" + user +
                '}';
    }
}
