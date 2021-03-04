package com.billbook.app.database.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity
public class NewInvoiceModels implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @SerializedName("localId")
    @ColumnInfo(name = "localId")
    private int localId;

    @SerializedName("id")
    @ColumnInfo(name = "id")
    private int id;

    @SerializedName("measurementId")
    @ColumnInfo(name = "measurementId")
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
    @ColumnInfo(name = "gstType")
    private String gstType;

    @SerializedName("gstAmount")
    @ColumnInfo(name = "gstAmount")
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
    @ColumnInfo(name = "totalAmount")
    private float totalAmount;

    @SerializedName("invoiceid")
    @ColumnInfo(name = "invoiceid")
    private int invoiceid;


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

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
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


    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }


    public float getGst() {
        return gst;
    }

    public void setGst(float gst) {
        this.gst = gst;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGstType() {
        return gstType;
    }

    public void setGstType(String gstType) {
        this.gstType = gstType;
    }

    public float getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(float gstAmount) {
        this.gstAmount = gstAmount;
    }

    public float getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(float totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "localId=" + localId +
                ", id=" + id +
                ", quantity=" + quantity +
                ", price=" + price +
                ", tax=" + gst +
                ", is_active=" + is_active +
                ", user=" + user +
                ", serial_no='" + serial_no + '\'' +
                ", measurement_id=" + measurementId +
                '}';
    }

    public int getMeasurementId() {
        return measurementId;
    }

    public void setMeasurementId(int measurementId) {
        this.measurementId = measurementId;
    }
}
