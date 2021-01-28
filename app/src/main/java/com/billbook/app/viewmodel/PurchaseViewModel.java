package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.billbook.app.database.models.Purchase;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class PurchaseViewModel extends AndroidViewModel {
    private LiveData<List<Purchase>> purchaseList;

    public PurchaseViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Purchase>> getPurchaseListByInvoiceId(int id) {
        purchaseList = AppRepository.getInstance().getAllPurchase();
        return purchaseList;
    }
}
