package com.billbook.app.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.billbook.app.database.models.Expense;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ExpenseDao {

    @Query("SELECT * FROM Expense ORDER BY name ASC")
    LiveData<List<Expense>> getAllExpenses();

    @Insert(onConflict = REPLACE)
    long insertExpense(Expense expense);

    @Insert(onConflict = REPLACE)
    void insertAll(List<Expense> expenses);

    @Query("DELETE FROM Expense")
    void deleteAll();

}
