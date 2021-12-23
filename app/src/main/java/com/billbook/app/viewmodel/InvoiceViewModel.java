package com.billbook.app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.repository.AppRepository;

public class InvoiceViewModel extends AndroidViewModel {

    public InvoiceViewModel(Application application){
        super(application);
    }

    public void insert(InvoiceModelV2 invoiceModelV2){
        AppRepository.getInstance().insert(invoiceModelV2);
    }

    public LiveData<InvoiceModelV2> getCurrentInvoice(long localInvoiceId){
        return AppRepository.getInstance().getCurrentInvoice(localInvoiceId);
    }

    public void updateIsSync(long localInvoiceId){
        AppRepository.getInstance().updateIsSync(localInvoiceId);
    }

    public void updateInvoiceId(long localInvoiceId,long invoiceId){
        AppRepository.getInstance().updateInvoiceId(localInvoiceId,invoiceId);
    }

    public void updatePdfPath(long localInvoiceId, String pdfPath){
        AppRepository.getInstance().updatePdfPath(localInvoiceId, pdfPath);
    }

}
