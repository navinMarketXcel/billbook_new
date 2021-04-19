package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.billbook.app.database.models.Inventory;

import java.util.List;

@Dao
public interface InventoryDao {

    @Query("SELECT * FROM Inventory ORDER BY product_name ASC")
    List<Inventory> getAllInventory();

    @Query("SELECT * FROM Inventory WHERE id =:inventoryId")
    LiveData<Inventory> getInventory(int inventoryId);

    @Query("SELECT * FROM Inventory WHERE isUpadted =0")
    List<Inventory> getInventoryToUpdate();

    @Query("SELECT * FROM Inventory WHERE product =:id")
    Inventory getInventoryByProductId(int id);


    @Query("SELECT * FROM Inventory WHERE serial_no =:id")
    Inventory getInventoryBySerialNo(String id);

    @Query("SELECT * FROM Inventory WHERE serial_no =:id")
    List<Inventory> getListInventoryBySerialNo(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserInventory(Inventory inventory);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Inventory> inventories);

    @Delete
    void delete(Inventory inventory);


    @Query("DELETE FROM Inventory")
    void deleteAll();
}
