package com.billbook.app.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

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
