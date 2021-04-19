package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.billbook.app.database.models.CategoriesBrandCount;
import com.billbook.app.database.models.Category;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;


@Dao
public interface CategoriesDao {

    @Query("SELECT * FROM Category WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM Category WHERE id =:categoryId")
    Category loadCategoryByCategoryId(int categoryId);

    @Query("SELECT * FROM Category WHERE is_active = 1")
    List<CategoriesBrandCount> getAllEmptyCategories();

    @Insert(onConflict = REPLACE)
    long insertcategory(Category category);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Category> category);


    @Query("DELETE FROM Category")
    void deleteAll();

}
