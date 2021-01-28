package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.billbook.app.database.models.Brand;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class BrandViewModel extends AndroidViewModel {
    private LiveData<List<Brand>> brandList;

    public BrandViewModel(Application application) {
        super(application);

    }

    public LiveData<List<Brand>> getBrandListByCategoryID(int catId) {
        brandList = AppRepository.getInstance().getBrandListByCategoryID(catId);
        return brandList;
    }

}
