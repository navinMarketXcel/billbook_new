package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.billbook.app.database.models.Expense;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

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
