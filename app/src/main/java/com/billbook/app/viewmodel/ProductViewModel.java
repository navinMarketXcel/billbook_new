package com.billbook.app.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.repository.AppRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private LiveData<List<Product>> productList;
    private LiveData<List<ProductAndInventory>> productListNew;

    public ProductViewModel(Application application) {
        super(application);
    }

    public LiveData<List<Product>> getProductListByCategoryIDAndBrandID(int categoriesID, int brandID) {
        productList = AppRepository.getInstance().getAllProductListByCategoryIDAndBrandID(categoriesID, brandID);
        return productList;
    }

    public LiveData<List<ProductAndInventory>> getProductListByCategoryIDAndBrandIDNew(int categoriesID, int brandID) {
        productListNew = AppRepository.getInstance().getAllProductListByCategoryIDAndBrandIDNew(categoriesID, brandID);
        return productListNew;
    }

    public LiveData<List<ProductAndInventory>> getCat(int categoriesID) {
        productListNew = AppRepository.getInstance().getBrandWiseProductCnt(categoriesID);
        return productListNew;
    }

    public LiveData<List<ProductAndInventory>> getCategorywiseProductCnt() {
        productListNew = AppRepository.getInstance().getCategoryWiseProductCnt();
        return productListNew;
    }


}
