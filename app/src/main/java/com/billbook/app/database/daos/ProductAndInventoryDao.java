package com.billbook.app.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Query;

import com.billbook.app.database.models.ProductAndInventory;

import java.util.List;

@Dao
public interface ProductAndInventoryDao {


    @Query("SELECT * FROM Product WHERE category =:categoryId AND brand=:brandId ORDER BY name ASC")
    LiveData<List<ProductAndInventory>> loadProductByCategoryIdAndBrandNew(int categoryId, int brandId);

    @Delete
    void delete(ProductAndInventory Product);
}
