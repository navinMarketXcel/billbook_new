package com.billbook.app.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Distributor;
import com.billbook.app.database.models.Product;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class AddProductViewModel extends AndroidViewModel {

    private final LiveData<List<Category>> categoryList;
    private LiveData<List<Product>> productList;
    private LiveData<List<Brand>> brandList;
    private LiveData<List<Distributor>> distributorList;

    public AddProductViewModel(Application application) {
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

    public LiveData<List<Distributor>> getDistributors() {
        distributorList = AppRepository.getInstance().getDistributors();
        return distributorList;
    }


    public LiveData<List<Product>> getProductListByCategoryIDAndBrand(int categoriesId, int brandId) {
        productList = AppRepository.getInstance().getProductListByCategoryIDAndBrand(categoriesId, brandId);
        return productList;
    }


}
