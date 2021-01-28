package com.billbook.app.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndInventory;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM Product WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Product>> getAllProduct();

    @Query("SELECT * FROM Product WHERE id =:ProductId")
    Product getProduct(int ProductId);

    @Query("SELECT * FROM Product WHERE barcode =:barcode")
    Product getProductByBarcode(String barcode);

    @Query("SELECT * FROM Product WHERE name =:productName AND brand =:brand")
    Product getProductByNameBrand(String productName, int brand);

    @Query("SELECT * FROM Product WHERE category =:categoryId ORDER BY name ASC")
    LiveData<List<Product>> loadProductByCategoryId(int categoryId);

    @Query("SELECT * FROM Product WHERE category =:categoryId AND brand=:brandId ORDER BY name ASC")
    LiveData<List<Product>> loadProductByCategoryIdAndBrand(int categoryId, int brandId);

    @Query("SELECT * FROM Product WHERE name LIKE :dealText or brand LIKE :dealText or category LIKE:dealText ORDER BY name ASC")
    public LiveData<List<Product>> getSearchProductList(String dealText);

    /*@Query("SELECT * FROM Product WHERE barcode =:barcode")
    Product getProduct(String barcode);*/

    @Query("SELECT * FROM Product WHERE category =:categoryId AND brand=:brandId ORDER BY isBestSeller DESC, name ASC")
    LiveData<List<ProductAndInventory>> loadProductByCategoryIdAndBrandNew(int categoryId, int brandId);


    @Query("SELECT * FROM Product GROUP by category")
    LiveData<List<ProductAndInventory>> loadProductByCategory();


    @Query("SELECT * FROM Product WHERE category =:categoryId  GROUP BY brand")
    LiveData<List<ProductAndInventory>> loadProductByBrand(int categoryId);


    @Query("SELECT * FROM Product ORDER BY name ASC")
    LiveData<List<ProductAndInventory>> loadAllProducts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long inserProduct(Product Product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserAll(List<Product> Product);

    @Delete
    void delete(Product Product);

    @Query("DELETE FROM Product")
    void deleteAll();
}
