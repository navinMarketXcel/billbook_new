package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.billbook.app.database.models.Purchase;

import java.util.List;

@Dao
public interface PurchaseDao {

    @Query("SELECT * FROM Purchase")
    LiveData<List<Purchase>> getAllPurchase();

    @Query("SELECT * FROM Purchase WHERE id IN (:id)")
    LiveData<List<Purchase>> loadAllByIds(int[] id);

    @Query("SELECT * FROM Purchase WHERE id =:id")
    LiveData<Purchase> getPurchase(int id);

//  @Query("SELECT * FROM Purchase WHERE invoiceId IN (:invoiceId)") LiveData<List<Purchase>> getPurchaseListByInvoiceId(int invoiceId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPurchase(Purchase purchase);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserAll(List<Purchase> purchasesList);

    @Delete
    void delete(Purchase purchase);

    @Query("DELETE FROM Purchase")
    void deleteAll();
}
