package com.billbook.app.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.InvoiceModel;

@Dao
public interface NewInvoiceDao {

    @Insert
    void Insert(InvoiceModel invoiceModel);

    @Update
    void Update(InvoiceModel invoiceModel);

    @Delete
    void Delete(InvoiceModel invoiceModel);

    @Query("SELECT * from invoice_table")
    InvoiceModel getAllModel();

    @Query("SELECT * from invoice_table WHERE id =:id ")
    InvoiceModel getInvoiceById(int id);
}
