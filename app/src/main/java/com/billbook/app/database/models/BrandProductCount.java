package com.billbook.app.database.models;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class BrandProductCount {

    @Embedded
    private Brand brand;

    @Relation(parentColumn = "id", entityColumn = "brand", entity = Product.class)
    private List<Product> productList;

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    @Override
    public String toString() {
        return "BrandProductCount{" +
                "brand=" + brand +
                ", productList=" + productList +
                '}';
    }
}
