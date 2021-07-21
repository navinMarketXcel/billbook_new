package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.Expense;
import com.billbook.app.database.models.InvoiceItems;

import java.util.List;

@Dao
public interface InvoiceItemDao {

    @Insert
    void insert(InvoiceItems invoiceItem);

    @Update
    void update(InvoiceItems invoiceItem);

    @Delete
    void delete(InvoiceItems invoiceItem);

    @Query("DELETE from invoice_table")
    void deleteAll();

    @Query("SELECT * FROM invoice_table WHERE invoice_id = -1")
    LiveData<List<InvoiceItems>> getAllItems();

}
