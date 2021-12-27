package com.billbook.app.database.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "invoiceItemTable")
public class InvoiceItems {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "local_id")
    private int localid;

    // @SerializedName("localInvoiceId")
    @ColumnInfo(name = "localinvoice_id")
    private long localInvoiceId;

    @SerializedName("measurementId")
    @ColumnInfo(name = "measurement_id")
    private int measurementId;

    @SerializedName("name")
    @ColumnInfo(name = "name")
    private String name;

    @SerializedName("quantity")
    @ColumnInfo(name = "quantity")
    private float quantity;

    @SerializedName("price")
    @ColumnInfo(name = "price")
    private float price;

    @SerializedName("gstType")
    @ColumnInfo(name = "gst_type")
    private String gstType;

    @SerializedName("gstAmount")
    @ColumnInfo(name = "gst_amount")
    private float gstAmount;

    @SerializedName("gst")
    @ColumnInfo(name = "gst")
    private float gst;

    @SerializedName("is_active")
    @ColumnInfo(name = "is_active")
    private boolean is_active = true;

    @SerializedName("user")
    @ColumnInfo(name = "user")
    private int user;

    @SerializedName("serial_no")
    @ColumnInfo(name = "serial_no")
    private String serial_no;

    @SerializedName("imei")
    @ColumnInfo(name = "imei")
    private String imei;

    @SerializedName("totalAmount")
    @ColumnInfo(name = "total_amount")
    private float totalAmount;

    @SerializedName("invoiceid")
    @ColumnInfo(name = "invoice_id")
    private int invoiceid;

    // @SerializedName("databaseid")
    @ColumnInfo(name = "database_id")
    private Integer databaseid;

    @SerializedName("isSync")
    @ColumnInfo(name="is_sync")
    private int isSync;

    public InvoiceItems(int measurementId, String name, float quantity, float price, String gstType, float gstAmount, float gst, boolean is_active, int user, String serial_no, String imei, float totalAmount, int invoiceid,int isSync, Integer databaseid,long localInvoiceId) {
        this.measurementId = measurementId+1;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.gstType = gstType;
        this.gstAmount = gstAmount;
        this.gst = gst;
        this.is_active = is_active;
        this.user = user;
        this.serial_no = serial_no;
        this.imei = imei;
        this.totalAmount = totalAmount;
        this.invoiceid = invoiceid;
        this.isSync = isSync;
        this.databaseid = databaseid;
        this.localInvoiceId = localInvoiceId;
    }

    public int getLocalid() {
        return localid;
    }

    public int getMeasurementId() {
        return measurementId-1;
    }

    public String getName() {
        return name;
    }

    public float getQuantity() {
        return quantity;
    }

    public float getPrice() {
        return price;
    }

    public String getGstType() {
        return gstType;
    }

    public float getGstAmount() {
        return gstAmount;
    }

    public float getGst() {
        return gst;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public int getUser() {
        return user;
    }

    public String getSerial_no() {
        return serial_no;
    }

    public String getImei() {
        return imei;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public int getInvoiceid() {
        return invoiceid;
    }

    public int getIsSync() {
        return isSync;
    }

    public Integer getDatabaseid() { return databaseid; }

    public long getLocalInvoiceId() {
        return localInvoiceId;
    }

    public void setLocalid(int localid) {
        this.localid = localid;
    }

    public void setMeasurementId(int measurementId) {
        this.measurementId = measurementId+1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setGstType(String gstType) {
        this.gstType = gstType;
    }

    public void setGstAmount(float gstAmount) {
        this.gstAmount = gstAmount;
    }

    public void setGst(float gst) {
        this.gst = gst;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public void setSerial_no(String serial_no) {
        this.serial_no = serial_no;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setInvoiceid(int invoiceid) {
        this.invoiceid = invoiceid;
    }

    public void setIsSync(int isSync) {
        this.isSync = isSync;
    }

    public void setDatabaseid(Integer databaseid) { this.databaseid = databaseid; }

    public void setLocalInvoiceId(long localInvoiceId) {
        this.localInvoiceId = localInvoiceId;
    }
}