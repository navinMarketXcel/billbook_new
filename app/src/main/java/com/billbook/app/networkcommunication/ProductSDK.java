package com.billbook.app.networkcommunication;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProductSDK implements Serializable {
    @SerializedName("ean")
    private String ean;

    @SerializedName("title")
    private String title;

    @SerializedName("upc")
    private String upc;
    @SerializedName("gtin")
    private String gtin;
    @SerializedName("asin")
    private String asin;
    @SerializedName("description")
    private String description;
    @SerializedName("brand")
    private String brand;
    @SerializedName("model")
    private String model;
    @SerializedName("dimension")
    private String dimension;

    @SerializedName("weight")
    private String weight;

    @SerializedName("currency")
    private String currency;
    @SerializedName("lowest_recorded_price")
    private String lowest_recorded_price;
    @SerializedName("highest_recorded_price")
    private String highest_recorded_price;

    public String getEan() {
        return ean;
    }

    public void setEan(final String ean) {
        this.ean = ean;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getUpc() {
        return upc;
    }

    public void setUpc(final String upc) {
        this.upc = upc;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(final String gtin) {
        this.gtin = gtin;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(final String asin) {
        this.asin = asin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(final String dimension) {
        this.dimension = dimension;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(final String weight) {
        this.weight = weight;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getLowest_recorded_price() {
        return lowest_recorded_price;
    }

    public void setLowest_recorded_price(final String lowest_recorded_price) {
        this.lowest_recorded_price = lowest_recorded_price;
    }

    public String getHighest_recorded_price() {
        return highest_recorded_price;
    }

    public void setHighest_recorded_price(final String highest_recorded_price) {
        this.highest_recorded_price = highest_recorded_price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "ean='" + ean + '\'' +
                ", title='" + title + '\'' +
                ", upc='" + upc + '\'' +
                ", gtin='" + gtin + '\'' +
                ", asin='" + asin + '\'' +
                ", description='" + description + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", dimension='" + dimension + '\'' +
                ", weight='" + weight + '\'' +
                ", currency='" + currency + '\'' +
                ", lowest_recorded_price='" + lowest_recorded_price + '\'' +
                ", highest_recorded_price='" + highest_recorded_price + '\'' +
                '}';
    }
}
