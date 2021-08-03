package com.billbook.app.database.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "invoice_table")
public class InvoiceModel {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "local_id")
    private int local_id;

    //? Local Invoice id ? -> Should we save response ?
//    @ColumnInfo(name = "local_invoice_id")
//    private int local_invoice_id;

    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("customerName")
    @ColumnInfo(name = "customer_name")
    private String customerName;

    @SerializedName("customerMobileNo")
    @ColumnInfo(name = "customer_mobileNo")
    private Integer customerMobileNo;

    @SerializedName("customerAddress")
    @ColumnInfo(name = "customer_address")
    private String customerAddress;

    @SerializedName("GSTNo")
    @ColumnInfo(name = "gst_no")
    private String GSTNo;

    @SerializedName("totalAmount")
    @ColumnInfo(name = "total_amount")
    private Integer totalAmount;

    @SerializedName("userid")
    @ColumnInfo(name = "userid")
    private Integer userid;

    @SerializedName("invoiceDate")
    @ColumnInfo(name = "invoice_date")
    private String invoiceDate;

    @SerializedName("gstBillNo")
    @ColumnInfo(name = "gst_billNo")
    private int gstBillNo;

    @SerializedName("nonGstBillNo")
    @ColumnInfo(name = "nonGstBillNo")
    private int nonGstBillNo;

    @SerializedName("gstType")
    @ColumnInfo(name = "gst_type")
    private String gstType;

    @SerializedName("updatedAt")
    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    @SerializedName("createdAt")
    @ColumnInfo(name = "created_at")
    private String createdAt;

    @SerializedName("isSync")
    @ColumnInfo(name = "is_sync")
    private int isSync;

    public InvoiceModel(int id, String customerName, Integer customerMobileNo, String customerAddress, String GSTNo, Integer totalAmount, Integer userid, String invoiceDate, int gstBillNo, int nonGstBillNo, String gstType, String updatedAt, String createdAt, int isSync) {
        this.id = id;
        this.customerName = customerName;
        this.customerMobileNo = customerMobileNo;
        this.customerAddress = customerAddress;
        this.GSTNo = GSTNo;
        this.totalAmount = totalAmount;
        this.userid = userid;
        this.invoiceDate = invoiceDate;
        this.gstBillNo = gstBillNo;
        this.nonGstBillNo = nonGstBillNo;
        this.gstType = gstType;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.isSync = isSync;
    }

    public int getLocal_id() {
        return local_id;
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Integer getCustomerMobileNo() {
        return customerMobileNo;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getGSTNo() {
        return GSTNo;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public Integer getUserid() {
        return userid;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public int getGstBillNo() {
        return gstBillNo;
    }

    public int getNonGstBillNo() {
        return nonGstBillNo;
    }

    public String getGstType() {
        return gstType;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getIsSync() {
        return isSync;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerMobileNo(Integer customerMobileNo) {
        this.customerMobileNo = customerMobileNo;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public void setGSTNo(String GSTNo) {
        this.GSTNo = GSTNo;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public void setGstBillNo(int gstBillNo) {
        this.gstBillNo = gstBillNo;
    }

    public void setNonGstBillNo(int nonGstBillNo) {
        this.nonGstBillNo = nonGstBillNo;
    }

    public void setGstType(String gstType) {
        this.gstType = gstType;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setIsSync(int isSync) {
        this.isSync = isSync;
    }
}

//"is_active":true,
//        "id":23527,
//        "customerName":"amanbharti",
//        "customerMobileNo":"7250720774",
//        "customerAddress":"chanakya nagar ",
//        "GSTNo":"12321545",
//        "totalAmount":144,
//        "userid":"16",
//        "invoiceDate":"2021-07-31T19:10:50.000Z",
//        "gstBillNo":0,
//        "nonGstBillNo":34,
//        "gstType":"",
//        "updatedAt":"2021-07-31T08:10:50.638Z",
//        "createdAt":"2021-07-31T08:10:50.638Z"