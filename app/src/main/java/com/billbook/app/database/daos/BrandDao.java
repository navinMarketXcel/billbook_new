package com.billbook.app.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.BrandProductCount;

import java.util.List;

@Dao
public interface BrandDao {

    @Query("SELECT * FROM Brand WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Brand>> getAllBrand();

    @Query("SELECT * FROM Brand WHERE id IN (:brandIds) ORDER BY name ASC")
    LiveData<List<Brand>> loadAllByIds(int[] brandIds);

    @Query("SELECT * FROM Brand WHERE id =:brandId")
    LiveData<Brand> getBrand(int brandId);

    @Query("SELECT * FROM Brand WHERE name =:brandName")
    Brand getBrandByName(String brandName);

    @Query("SELECT * FROM Brand WHERE id =:brandId")
    Brand getBrandByProduct(int brandId);

    @Query("SELECT * FROM Brand WHERE category =:categoryId ORDER BY isBestSeller DESC, name ASC")
    LiveData<List<Brand>> loadBrandByCategoryId(int categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long inserBrand(Brand brand);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserAll(List<Brand> brandList);

    @Query("SELECT * FROM Brand")
    List<BrandProductCount> getAllEmptyBrand();

    @Delete
    void delete(Brand brand);

    @Query("DELETE FROM Brand")
    void deleteAll();
}
