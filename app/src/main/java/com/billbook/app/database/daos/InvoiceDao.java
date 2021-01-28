package com.billbook.app.database.daos;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.ProductAndCount;

import java.util.List;

@Dao
public interface InvoiceDao {

    @Query("SELECT * FROM Invoice WHERE is_active = 1 ORDER BY invoiceName ASC")
    List<Invoice> getAllInvoice();

    @Query("SELECT * FROM Invoice WHERE id=:id")
    LiveData<Invoice> getInvoice(int id);

    @Query("SELECT * FROM Invoice WHERE local_id=:local_id")
    Invoice getInvoiceLocal(int local_id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertInvoice(Invoice invoice);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserAll(List<Invoice> invoiceList);

    @Query("select * from Invoice where createdAt=Date(:from)")
    LiveData<List<Invoice>> fetchInvoiceBetweenDate(String from);

    @Query("select product_name,price,sum(price) as priceTotal,sum(quantity) as total from Purchase where invoice in (select id from Invoice where Date(updatedAt)>= Date (:fromDate) AND Date(updatedAt) <= Date (:toDate)) group by product")
    LiveData<List<ProductAndCount>> fetchInvoiceBetweenDateNew(String fromDate, String toDate);

    @Query("select product_name,sum(price*quantity) as price,sum(price) as priceTotal,sum(quantity) as total from Purchase where brand=:brandId and invoice in (select id from Invoice where Date(updatedAt)>= Date (:fromDate) AND Date(updatedAt) <= Date (:toDate)) group by product")
    LiveData<List<ProductAndCount>> fetchInvoiceBetweenDateNewForBrand(String fromDate, String toDate, int brandId);

    @Query("select brand_name as product_name,sum(price*quantity) as price,sum(price) as priceTotal,sum(quantity) as total from Purchase where category=:catId and invoice in (select id from Invoice where Date(updatedAt)>= Date (:fromDate) AND Date(updatedAt) <= Date (:toDate)) group by brand")
    LiveData<List<ProductAndCount>> fetchInvoiceBetweenDateNewForCategory(String fromDate, String toDate, int catId);

    @Query("select product_name,sum(price*quantity) as price,sum(price) as priceTotal,sum(quantity) as total from Purchase where product=:prodId and invoice in (select id from Invoice where Date(updatedAt)>= Date (:fromDate) AND Date(updatedAt) <= Date (:toDate)) group by product")
    LiveData<List<ProductAndCount>> fetchInvoiceBetweenDateNewForProduct(String fromDate, String toDate, int prodId);


    @Delete
    void delete(Invoice invoice);

    @Query("DELETE FROM Invoice")
    void deleteAll();
}
