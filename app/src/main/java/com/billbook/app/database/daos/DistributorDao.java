package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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
