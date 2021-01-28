package com.billbook.app.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.billbook.app.database.models.Distributor;

import java.util.List;

@Dao
public interface DistributorDao {
    /*@Query("SELECT * FROM user WHERE id=")
    List<User> getUser();*/

    @Query("SELECT * FROM Distributor")
    LiveData<List<Distributor>> getAll();

    @Query("SELECT * FROM Distributor WHERE id IN (:ids)")
    List<Distributor> loadAllByIds(int[] ids);

    @Query("SELECT * FROM Distributor WHERE id =:id")
    Distributor getUser(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserDistributor(Distributor distributor);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Distributor> inventories);

    @Delete
    void delete(Distributor distributor);

    @Query("DELETE FROM Distributor")
    void deleteAll();
}
