package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;

import com.billbook.app.database.models.ProductAndInventory;

import java.util.List;

@Dao
public interface ProductAndInventoryDao {


    @Query("SELECT * FROM Product WHERE category =:categoryId AND brand=:brandId ORDER BY name ASC")
    LiveData<List<ProductAndInventory>> loadProductByCategoryIdAndBrandNew(int categoryId, int brandId);

    @Delete
    void delete(ProductAndInventory Product);
}
