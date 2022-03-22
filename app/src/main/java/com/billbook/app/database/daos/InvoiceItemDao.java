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

    // @Update(onConflict = REPLACE)
    // void update(InvoiceItems invoiceItem);

    @Delete
    void delete(InvoiceItems invoiceItem);

    @Query("DELETE from invoiceItemTable WHERE localinvoice_id =:localInvoiceId")
    void deleteAll(long localInvoiceId);

    @Query("SELECT * FROM invoiceItemTable WHERE localinvoice_id =:localInvoiceId")
    LiveData<List<InvoiceItems>> getAllItems(long localInvoiceId);

    @Query("UPDATE invoiceItemTable SET measurement_id =:measurementId,name =:name ,quantity =:quantity, price=:price, gst_type=:gstType, gst_amount=:gstAmount,gst=:gst,is_active=:isActive,user=:user,serial_no=:serialNo,imei =:imei,total_amount=:totalAmount,invoice_id=:invoiceId,is_sync=:isSync WHERE local_id=:id")
    void updateByLocalId(int measurementId, String name, float quantity, float price, String gstType, float gstAmount, float gst, boolean isActive, int user, String serialNo, String imei, float totalAmount, int invoiceId,int isSync,int id);

    // @Query("UPDATE invoiceItemTable SET invoice_id =:id WHERE local_id=:curId")
    // void updateId(int id,int curId);

    @Query("SELECT * FROM invoiceItemTable WHERE localinvoice_id=:id")
    List<InvoiceItems>getCurrentItems(long id);
}
