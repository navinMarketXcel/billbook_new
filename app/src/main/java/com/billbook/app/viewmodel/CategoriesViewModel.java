package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.billbook.app.database.models.Category;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class CategoriesViewModel extends AndroidViewModel {
    private final LiveData<List<Category>> categoryList;

    public CategoriesViewModel(Application application) {
        super(application);
        categoryList = AppRepository.getInstance().getAllCategories();
    }

    public LiveData<List<Category>> getCategoriesList() {
        return categoryList;
    }

}
