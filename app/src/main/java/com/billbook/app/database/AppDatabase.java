package com.billbook.app.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.billbook.app.database.daos.BrandDao;
import com.billbook.app.database.daos.CategoriesDao;
import com.billbook.app.database.daos.DistributorDao;
import com.billbook.app.database.daos.ExpenseDao;
import com.billbook.app.database.daos.InventoryDao;
import com.billbook.app.database.daos.InvoiceDao;
import com.billbook.app.database.daos.InvoiceItemDao;
import com.billbook.app.database.daos.ModelDao;
import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.daos.ProductDao;
import com.billbook.app.database.daos.PurchaseDao;
import com.billbook.app.database.daos.UserDao;
import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Distributor;
import com.billbook.app.database.models.Expense;
import com.billbook.app.database.models.Inventory;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.database.models.Model;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.database.models.User;

@Database(entities = {
        User.class, Category.class, Brand.class, Product.class, Inventory.class, Invoice.class,
        Purchase.class, Distributor.class, Model.class, Expense.class,InvoiceItems.class, InvoiceModelV2.class
}, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    public abstract CategoriesDao categoriesDao();

    public abstract BrandDao brandDao();

    public abstract ProductDao productDao();

    public abstract InventoryDao inventoryDao();

    public abstract InvoiceDao invoiceDao();

    public abstract PurchaseDao purchaseDao();

    public abstract DistributorDao distributorDao();

    public abstract ModelDao getModelDao();

    public abstract ExpenseDao getExpModelDao();

    public abstract InvoiceItemDao invoiceItemDao();

    public abstract NewInvoiceDao newInvoiceDao();

}