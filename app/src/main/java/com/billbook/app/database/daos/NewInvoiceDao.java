package com.billbook.app.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.billbook.app.database.models.InvoiceModelV2;

import java.util.List;

@Dao
public interface NewInvoiceDao {

    @Insert
    void Insert(InvoiceModelV2 invoiceModelV2);

    @Update
    void Update(InvoiceModelV2 invoiceModelV2);

    @Delete
    void Delete(InvoiceModelV2 invoiceModelV2);

    // Check query againg List<InvoiceModel> or change query
    // @Query("SELECT * from invoice_table")
    // InvoiceModel getAllModel();

    @Query("SELECT * from invoiceTableV2 where id=:id AND local_id = (SELECT MAX(local_id) from invoiceTableV2)")
    LiveData<InvoiceModelV2> getInvoiceById(long id);

    @Query("UPDATE invoiceTableV2 SET invoice_id =:invoiceId WHERE id =:localInvoiceId")
    void updateInvoiceId(long localInvoiceId,long invoiceId);

    @Query("SELECT * from invoiceTableV2 WHERE is_sync =0")
    List<InvoiceModelV2> getAllOffLineInvoice();

    @Query("UPDATE invoiceTableV2 SET is_sync=1 WHERE id=:id")
    void updateIsSync(long id);

    @Query("UPDATE invoiceTableV2 SET pdf_path =:pdfPath WHERE id =:localInvoiceId")
    void updatePdfPath(long localInvoiceId,String pdfPath);


}
