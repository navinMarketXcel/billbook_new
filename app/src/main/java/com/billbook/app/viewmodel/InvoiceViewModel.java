package com.billbook.app.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;

import com.billbook.app.database.models.InvoiceModel;
import com.billbook.app.repository.AppRepository;

public class InvoiceViewModel extends AndroidViewModel {

    public InvoiceViewModel(Application application){
        super(application);
    }

    public void insert(InvoiceModel invoiceModel){
        AppRepository.getInstance().insert(invoiceModel);
    }

}
