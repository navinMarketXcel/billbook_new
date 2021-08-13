package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.InvoiceModel;

import java.util.List;

@Dao
public interface NewInvoiceDao {

    @Insert
    void Insert(InvoiceModel invoiceModel);

    @Update
    void Update(InvoiceModel invoiceModel);

    @Delete
    void Delete(InvoiceModel invoiceModel);

    // Check query againg List<InvoiceModel> or change query
    // @Query("SELECT * from invoice_table")
    // InvoiceModel getAllModel();

    @Query("SELECT * from invoice_table WHERE id =:id ")
    LiveData<InvoiceModel> getInvoiceById(long id);

    @Query("UPDATE invoice_table SET invoice_id =:invoiceId WHERE id =:localInvoiceId")
    void updateInvoiceId(long localInvoiceId,long invoiceId);

    @Query("SELECT * from invoice_table WHERE is_sync =0")
    List<InvoiceModel> getAllOffLineInvoice();

    @Query("UPDATE invoice_table SET is_sync=1 WHERE id=:id")
    void updateIsSync(long id);

}
