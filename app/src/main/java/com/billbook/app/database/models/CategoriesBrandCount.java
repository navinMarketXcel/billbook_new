package com.billbook.app.database.models;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class CategoriesBrandCount {

    @Embedded
    private Category category;

    @Relation(parentColumn = "id", entityColumn = "category", entity = Brand.class)
    private List<Brand> brandList;

    @Override
    public String toString() {
        return "CategoriesBrandCount{" +
                "category=" + category +
                ", brandList=" + brandList +
                '}';
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Brand> getBrandList() {
        return brandList;
    }

    public void setBrandList(List<Brand> brandList) {
        this.brandList = brandList;
    }
}
