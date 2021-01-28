package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class AllProductViewModel extends AndroidViewModel {
    private LiveData<List<ProductAndInventory>> productListNew;

    public AllProductViewModel(Application application) {
        super(application);
    }


    public LiveData<List<ProductAndInventory>> getAllProductList() {
        productListNew = AppRepository.getInstance().getAllProductList();
        return productListNew;
    }


}
