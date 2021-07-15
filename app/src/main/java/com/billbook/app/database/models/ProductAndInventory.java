package com.billbook.app.database.models;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

public class ProductAndInventory implements Serializable {

    @Relation(parentColumn = "id", entityColumn = "product", entity = Inventory.class)
    public List<Inventory> inventoryList;
    @Embedded
    Product product;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Inventory> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    @Override
    public String toString() {
        return "ProductAndInventory{" +
                "product=" + product +
                ", inventoryList=" + inventoryList +
                '}';
    }
}
