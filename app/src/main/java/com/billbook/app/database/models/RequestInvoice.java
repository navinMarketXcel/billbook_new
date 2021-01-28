package com.billbook.app.database.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestInvoice {

    @SerializedName("id")
    @Expose
    private Integer id;

    @SerializedName("local_id")
    @Expose
    private Integer local_id;

    @SerializedName("invoiceDate")
    @Expose
    private String invoiceDate;

    @SerializedName("is_active")
    @Expose
    private Boolean is_active;
    @SerializedName("createdAt")
    @Expose
    private String createdAt;
    @SerializedName("updatedAt")
    @Expose
    private String updatedAt;
    @SerializedName("user")
    @Expose
    private Integer user;
    @SerializedName("customer_name")
    @Expose
    private String customer_name;

    @SerializedName("customer_email")
    @Expose
    private String customer_email;
    @SerializedName("customer_mobile_no")
    @Expose
    private String customer_mobile_no;
    @SerializedName("purchase")
    @Expose
    private List<Purchase> purchase = null;

    @SerializedName("payment_method1")
    private String paymentMethod1;

    @SerializedName("payment_method2")
    private String paymentMethod2;

    @SerializedName("payment_method1_amount")
    private float paymentMethod1Amount;

    @SerializedName("payment_method2_amount")
    private float paymentMethod2Amount;

    @SerializedName("invoice_no")
    private int invoice_no;

    @SerializedName("gst_type")
    @Expose
    private Integer gst_type;

    @SerializedName("discount_amount")
    @Expose
    private Integer discountAmount;

    @SerializedName("gst_percentage")
    @Expose
    private Float gstPercentage;
    @SerializedName("total_amount")
    @Expose
    private Float total_amount;
    @SerializedName("pdf")
    @Expose
    private String pdf;
    @SerializedName("customer_address")
    private String customerAddress;
    @SerializedName("city")
    private String city;
    @SerializedName("state")
    private String invoiceState;
    @SerializedName("description")
    private String description;

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInvoiceState() {
        return invoiceState;
    }

    public void setInvoiceState(String invoiceState) {
        this.invoiceState = invoiceState;
    }

    public Integer getGst_type() {
        return gst_type;
    }

    public void setGst_type(Integer gst_type) {
        this.gst_type = gst_type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
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

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_mobile_no() {
        return customer_mobile_no;
    }

    public void setCustomer_mobile_no(String customer_mobile_no) {
        this.customer_mobile_no = customer_mobile_no;
    }

    public Float getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(Float total_amount) {
        this.total_amount = total_amount;
    }

    public List<Purchase> getPurchase() {
        return purchase;
    }

    public void setPurchase(List<Purchase> purchase) {
        this.purchase = purchase;
    }

    public Integer getLocal_id() {
        return local_id;
    }

    public void setLocal_id(Integer local_id) {
        this.local_id = local_id;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Float getGstPercentage() {
        return gstPercentage;
    }

    public void setGstPercentage(Float gstPercentage) {
        this.gstPercentage = gstPercentage;
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

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public int getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(int invoice_no) {
        this.invoice_no = invoice_no;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "RequestInvoice{" +
                "id=" + id +
                ", local_id=" + local_id +
                ", is_active=" + is_active +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", user=" + user +
                ", customer_name='" + customer_name + '\'' +
                ", customer_mobile_no=" + customer_mobile_no +
                ", purchase=" + purchase +
                ", gst_type=" + gst_type +
                ", discountAmount=" + discountAmount +
                ", gstPercentage=" + gstPercentage +
                ", total_amount=" + total_amount +
                ", pdf='" + pdf + '\'' +
                ", customerAddress='" + customerAddress + '\'' +
                ", city='" + city + '\'' +
                ", invoiceState='" + invoiceState + '\'' +
                '}';
    }
}