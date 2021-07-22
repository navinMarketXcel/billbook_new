package com.billbook.app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.billbook.app.database.models.Expense;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;

public class InvoiceItemsViewModel extends AndroidViewModel {
    private final LiveData<List<InvoiceItems>> modelList;

    public InvoiceItemsViewModel(Application application){
        super(application);
        modelList = AppRepository.getInstance().getItems();
    }

    public void insert(InvoiceItems invoiceItems) {
        AppRepository.getInstance().insert(invoiceItems);
    }

    public void updateByLocalId(InvoiceItems invoiceItems){
        AppRepository.getInstance().updateByLocalId(invoiceItems);
    }


    public LiveData<List<InvoiceItems>> getInvoices() {
        return modelList;
    }

}
