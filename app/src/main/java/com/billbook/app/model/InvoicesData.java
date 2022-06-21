package com.billbook.app.model;

import com.billbook.app.database.models.Customer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InvoicesData {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("invoiceDate")
    @Expose
    public String invoiceDate;
    @SerializedName("gstBillNo")
    @Expose
    public Integer gstBillNo;
    @SerializedName("nonGstBillNo")
    @Expose
    public Integer nonGstBillNo;
    @SerializedName("discount")
    @Expose
    public Integer discount=0;
    @SerializedName("totalAfterDiscount")
    @Expose
    public Integer totalAfterDiscount=0;
    @SerializedName("GSTNo")
    @Expose
    public String gSTNo;
    @SerializedName("totalAmount")
    @Expose
    public Integer totalAmount=0;
    @SerializedName("gstType")
    @Expose
    public String gstType;
    @SerializedName("pdfLink")
    @Expose
    public String pdfLink;
    @SerializedName("userid")
    @Expose
    public Integer userid;
    @SerializedName("customerid")
    @Expose
    public Integer customerid;
    @SerializedName("is_active")
    @Expose
    public Boolean isActive;
    @SerializedName("createdAt")
    @Expose
    public String createdAt;
    @SerializedName("updatedAt")
    @Expose
    public String updatedAt;
    @SerializedName("masterItems")
    @Expose
    public List<MasterItem> masterItems = null;

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    @SerializedName("customer")
    @Expose
    public Customer customer;
    public boolean isCheck= false;
    public boolean isSelected = false;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getGstBillNo() {
        return gstBillNo;
    }

    public void setGstBillNo(Integer gstBillNo) {
        this.gstBillNo = gstBillNo;
    }

    public Integer getNonGstBillNo() {
        return nonGstBillNo;
    }

    public void setNonGstBillNo(Integer nonGstBillNo) {
        this.nonGstBillNo = nonGstBillNo;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getTotalAfterDiscount() {
        return totalAfterDiscount;
    }

    public void setTotalAfterDiscount(Integer totalAfterDiscount) {
        this.totalAfterDiscount = totalAfterDiscount;
    }

    public String getGSTNo() {
        return gSTNo;
    }

    public void setGSTNo(String gSTNo) {
        this.gSTNo = gSTNo;
    }

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getGstType() {
        return gstType;
    }

    public void setGstType(String gstType) {
        this.gstType = gstType;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getCustomerid() {
        return customerid;
    }

    public void setCustomerid(Integer customerid) {
        this.customerid = customerid;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    public List<MasterItem> getMasterItems() {
        return masterItems;
    }

    public void setMasterItems(List<MasterItem> masterItems) {
        this.masterItems = masterItems;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
