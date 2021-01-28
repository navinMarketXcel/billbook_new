package com.billbook.app.database.models;

public class LedgerDetails {
    String product_name;
    float price;
    int total;
    float priceTotal;

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getPriceTotal() {
        return priceTotal;
    }

    public void setPriceTotal(float priceTotal) {
        this.priceTotal = priceTotal;
    }

    @Override
    public String toString() {
        return "ProductAndCount{" +
                "product_name='" + product_name + '\'' +
                ", price=" + price +
                ", total=" + total +
                ", priceTotal=" + priceTotal +
                '}';
    }
}
