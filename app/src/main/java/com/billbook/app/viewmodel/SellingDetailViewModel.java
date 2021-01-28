package com.billbook.app.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Product;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class SellingDetailViewModel extends AndroidViewModel {
    private LiveData<List<Category>> categoryList = null;
    private LiveData<List<Product>> productList = null;
    private LiveData<List<Brand>> brandList = null;

    public SellingDetailViewModel(Application application) {
        super(application);
        categoryList = AppRepository.getInstance().getAllCategories();
    }


    public LiveData<List<Category>> getCategoriesList() {
        return categoryList;
    }

    public LiveData<List<Brand>> getBrandListByCategoryID(int categoriesID) {
        brandList = AppRepository.getInstance().getBrandListByCategoryID(categoriesID);
        return brandList;
    }


    public LiveData<List<Product>> getProductListByCategoryIDAndBrand(int categoriesId, int brandId) {
        productList = AppRepository.getInstance().getProductListByCategoryIDAndBrand(categoriesId, brandId);
        return productList;
    }

}
