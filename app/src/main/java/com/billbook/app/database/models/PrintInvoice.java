package com.billbook.app.database.models;

import java.io.Serializable;
import java.util.ArrayList;

public class PrintInvoice implements Serializable {

    ArrayList<String> stringArrayList = null;
    private ArrayList<Purchase> purchaseArrayList = null;
    private String invoiceDate;
    private boolean isDiscountEnabled, isGSTEnabled;
    private String gstAmount, amountBeforeTax, discountAmount, total;
    private float igst, cgst, sgst;
    private String igstAmount, cgstAmount, sgstAmount;
    private String description;
    private String pdfFile;
    private Integer strInvoiceNo;
    private Invoice invoice = null;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getStringArrayList() {
        return stringArrayList;
    }

    public void setStringArrayList(ArrayList<String> stringArrayList) {
        this.stringArrayList = stringArrayList;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public ArrayList<Purchase> getPurchaseArrayList() {
        return purchaseArrayList;
    }

    public void setPurchaseArrayList(ArrayList<Purchase> purchaseArrayList) {
        this.purchaseArrayList = purchaseArrayList;
    }

    public boolean isDiscountEnabled() {
        return isDiscountEnabled;
    }

    public void setDiscountEnabled(boolean discountEnabled) {
        isDiscountEnabled = discountEnabled;
    }

    public boolean isGSTEnabled() {
        return isGSTEnabled;
    }

    public void setGSTEnabled(boolean GSTEnabled) {
        isGSTEnabled = GSTEnabled;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(String gstAmount) {
        this.gstAmount = gstAmount;
    }

    public String getAmountBeforeTax() {
        return amountBeforeTax;
    }

    public void setAmountBeforeTax(String amountBeforeTax) {
        this.amountBeforeTax = amountBeforeTax;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public Integer getStrInvoiceNo() {
        return strInvoiceNo;
    }

    public void setStrInvoiceNo(Integer strInvoiceNo) {
        this.strInvoiceNo = strInvoiceNo;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public float getIgst() {
        return igst;
    }

    public void setIgst(float igst) {
        this.igst = igst;
    }

    public float getCgst() {
        return cgst;
    }

    public void setCgst(float cgst) {
        this.cgst = cgst;
    }

    public float getSgst() {
        return sgst;
    }

    public void setSgst(float scst) {
        this.sgst = scst;
    }

    public String getIgstAmount() {
        return igstAmount;
    }

    public void setIgstAmount(String igstAmount) {
        this.igstAmount = igstAmount;
    }

    public String getCgstAmount() {
        return cgstAmount;
    }

    public void setCgstAmount(String cgstAmount) {
        this.cgstAmount = cgstAmount;
    }

    public String getSgstAmount() {
        return sgstAmount;
    }

    public void setSgstAmount(String scstAmount) {
        this.sgstAmount = scstAmount;
    }
}
