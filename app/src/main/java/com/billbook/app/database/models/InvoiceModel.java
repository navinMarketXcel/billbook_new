package com.billbook.app.database.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "invoice_table")
public class InvoiceModel {

    //local id
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "local_id")
    private int local_id;

    //? Local Invoice id ? -> Should we save response ?
    //    @ColumnInfo(name = "local_invoice_id")
    //    private int local_invoice_id;

    //invoice id = backend se id
    @SerializedName("invoiceId")
    @ColumnInfo(name = "invoice_id")
    private long invoiceId;

    //myapplication wala localInvoiceId
    @SerializedName("id")
    @ColumnInfo(name = "id")
    private long id;


    @SerializedName("customerName")
    @ColumnInfo(name = "customer_name")
    private String customerName;

    @SerializedName("customerMobileNo")
    @ColumnInfo(name = "customer_mobileNo")
    private String customerMobileNo;

    @SerializedName("customerAddress")
    @ColumnInfo(name = "customer_address")
    private String customerAddress;

    @SerializedName("GSTNo")
    @ColumnInfo(name = "gst_no")
    private String GSTNo;

    @SerializedName("totalAmount")
    @ColumnInfo(name = "total_amount")
    private float totalAmount;

    // discount  - represents discount Percent
    @SerializedName("discount")
    @ColumnInfo(name = "discount")
    private float discount;

    // discountAmt = totalAmount - discountedAmout i.e amount left after deducting discount
    @SerializedName("discountAmt")
    @ColumnInfo(name = "discountAmt")
    private float discountAmt;

    //userid from backend
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


    @SerializedName("totalAmountBeforeGST")
    @ColumnInfo(name="totalAmountBeforeGST")
    private long totalAmountBeforeGST;

    @SerializedName("updatedAt")
    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    @SerializedName("createdAt")
    @ColumnInfo(name = "created_at")
    private String createdAt;

    //0 for notsync 1 for sync
    @SerializedName("isSync")
    @ColumnInfo(name = "is_sync")
    private int isSync;

//    @SerializedName("pdfPath")
    @ColumnInfo(name = "pdf_path")
    private String pdfPath="";

    public InvoiceModel(long id,long invoiceId, String customerName, String customerMobileNo, String customerAddress, String GSTNo, float totalAmount, Integer userid, String invoiceDate,long totalAmountBeforeGST, int gstBillNo, int nonGstBillNo, String gstType, String updatedAt, String createdAt, int isSync, String pdfPath, float discount, float discountAmt) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.customerName = customerName;
        this.customerMobileNo = customerMobileNo;
        this.customerAddress = customerAddress;
        this.GSTNo = GSTNo;
        this.totalAmount = totalAmount;
        this.userid = userid;
        this.invoiceDate = invoiceDate;
        this.totalAmountBeforeGST = totalAmountBeforeGST;
        this.gstBillNo = gstBillNo;
        this.nonGstBillNo = nonGstBillNo;
        this.gstType = gstType;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.isSync = isSync;
        this.pdfPath=pdfPath;
        this.discount = discount;
        this.discountAmt = discountAmt;
    }

    public int getLocal_id() {
        return local_id;
    }

    public long getId() {
        return id;
    }

    public long getInvoiceId() {
        return invoiceId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerMobileNo() {
        return customerMobileNo;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getGSTNo() {
        return GSTNo;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public Integer getUserid() {
        return userid;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public long getTotalAmountBeforeGST() {
        return totalAmountBeforeGST;
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

    public String getPdfPath() { return pdfPath; }

    public float getDiscount() {
        return discount;
    }

    public float getDiscountAmt() {
        return discountAmt;
    }

    public void setDiscount(float discount) { this.discount = discount; }

    public void setDiscountAmt(float discountAmt) { this.discountAmt = discountAmt; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerMobileNo(String customerMobileNo) {
        this.customerMobileNo = customerMobileNo;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public void setGSTNo(String GSTNo) {
        this.GSTNo = GSTNo;
    }

    public void setTotalAmount(Float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public void setTotalAmountBeforeGST(long totalAmountBeforeGST) {
        this.totalAmountBeforeGST = totalAmountBeforeGST;
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