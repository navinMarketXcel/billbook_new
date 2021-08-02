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
        modelList = AppRepository.getInstance().getItems(-1);
    }

    public List<InvoiceItems> getCurrentItems(int invoiceId){
        return AppRepository.getInstance().getCurrentItems(invoiceId);
    }


    public void insert(InvoiceItems invoiceItems) {
        AppRepository.getInstance().insert(invoiceItems);
    }

    public void updateByLocalId(InvoiceItems invoiceItems){
        AppRepository.getInstance().updateByLocalId(invoiceItems);
    }

    public void delete(InvoiceItems invoiceItems){
        AppRepository.getInstance().delete(invoiceItems);
    }

    public void deleteAll(long localInvoiceId){
        AppRepository.getInstance().deleteAllItems(localInvoiceId);
    }

    public void updateId(int id,int curId){
        AppRepository.getInstance().updateID(id,curId);
    }

    public LiveData<List<InvoiceItems>> getInvoices(long localInvoiceId)
    {
        return AppRepository.getInstance().getItems(localInvoiceId);
    }

}
