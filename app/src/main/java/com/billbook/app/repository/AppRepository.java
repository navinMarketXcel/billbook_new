package com.billbook.app.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.billbook.app.database.daos.InvoiceItemDao;
import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.InvoiceModel;
import com.google.gson.Gson;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.BrandProductCount;
import com.billbook.app.database.models.CategoriesBrandCount;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Distributor;
import com.billbook.app.database.models.Expense;
import com.billbook.app.database.models.Inventory;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.Model;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.BrandResponse;
import com.billbook.app.networkcommunication.CategoryResponse;
import com.billbook.app.networkcommunication.DistributorAPIResponse;
import com.billbook.app.networkcommunication.GetProductInfoResponseFromSDK;
import com.billbook.app.networkcommunication.InventoryAPIResponse;
import com.billbook.app.networkcommunication.InvoiceApiResponse;
import com.billbook.app.networkcommunication.ProductResponse;
import com.billbook.app.networkcommunication.PurchaseAPIResponse;
import com.billbook.app.networkcommunication.SearchInvoiceAPIResponse;
import com.billbook.app.networkcommunication.UserReponse;
import com.billbook.app.networkcommunication.WebserviceResponseHandler;
import com.billbook.app.utils.Constants;
import com.billbook.app.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppRepository {
    private static final String TAG = "AppRepository";
    private static AppRepository appRepository;

    private AppRepository() {
    }

    public static AppRepository getInstance() {
        if (appRepository == null) {
            appRepository = new AppRepository();
        }
        return appRepository;
    }




    public LiveData<List<Brand>> getAllBrand() {

        return MyApplication.getDatabase().brandDao().getAllBrand();
    }


    public LiveData<List<Category>> getAllCategories() {

        return MyApplication.getDatabase().categoriesDao().getAllCategories();
    }

    public LiveData<List<Purchase>> getAllPurchase() {

        return MyApplication.getDatabase().purchaseDao().getAllPurchase();
    }

    public LiveData<List<Product>> getSearchProductList(String searchText) {

        return MyApplication.getDatabase().productDao().getSearchProductList(searchText);
    }

    public LiveData<List<Model>> getModels() {

        return MyApplication.getDatabase().getModelDao().getAllModels();
    }

    public LiveData<List<Expense>> getExpenses() {

        return MyApplication.getDatabase().getExpModelDao().getAllExpenses();
    }


    public LiveData<List<Model>> getModelsByName(String searchText) {

        return MyApplication.getDatabase().getModelDao().getAllModels();
    }


    public LiveData<List<Product>> getAllProductListByCategoryID(int categoryId) {

        return MyApplication.getDatabase().productDao().loadProductByCategoryId(categoryId);
    }

    public LiveData<List<Product>> getAllProductListByCategoryIDAndBrandID(int categoryId, int brandID) {

        return MyApplication.getDatabase().productDao().loadProductByCategoryIdAndBrand(categoryId, brandID);
    }

    public LiveData<List<ProductAndInventory>> getAllProductListByCategoryIDAndBrandIDNew(int categoryId, int brandID) {

        return MyApplication.getDatabase().productDao().loadProductByCategoryIdAndBrandNew(categoryId, brandID);
    }

    public LiveData<List<ProductAndInventory>> getBrandWiseProductCnt(int categoryId) {

        return MyApplication.getDatabase().productDao().loadProductByBrand(categoryId);
    }

    public LiveData<List<ProductAndInventory>> getCategoryWiseProductCnt() {

        return MyApplication.getDatabase().productDao().loadProductByCategory();
    }

    public LiveData<List<ProductAndInventory>> getAllProductList() {

        return MyApplication.getDatabase().productDao().loadAllProducts();
    }

    public LiveData<List<Brand>> getBrandListByCategoryID(int categoryId) {

        return MyApplication.getDatabase().brandDao().loadBrandByCategoryId(categoryId);
    }

    public LiveData<List<Distributor>> getDistributors() {

        return MyApplication.getDatabase().distributorDao().getAll();
    }

    public LiveData<List<Product>> getProductListByCategoryIDAndBrand(int categoryId, int brandID) {

        return MyApplication.getDatabase().productDao().loadProductByCategoryIdAndBrand(categoryId, brandID);
    }

    public void insertInventory(final Inventory inventoryVal) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<Inventory> inventories = new ArrayList<>();
                for (int i = 1; i <= inventoryVal.getQuantity(); i++) {
                    final Inventory inventory = new Inventory();
                    inventory.setId(0);
                    inventory.setProduct_id(inventoryVal.getProduct_id());
                    inventory.setProduct_name(inventoryVal.getProduct_name());
                    inventory.setBrand(inventoryVal.getBrand());
                    inventory.setBrand_name(inventoryVal.getBrand_name());
                    inventory.setCategory_name(inventoryVal.getCategory_name());
                    inventory.setCategory(inventoryVal.getCategory());
                    inventory.setQuantity(1);
                    inventory.setDistributor(inventoryVal.getDistributor());
                    inventory.setSpecification("[]");
                    inventory.setTax(0.0f);
                    inventory.setSelling_price(0.0f);
                    inventory.setPrice(inventoryVal.getPrice());
                    inventory.setSelling_price(inventoryVal.getSelling_price());
                    inventory.setIs_active(true);
                    inventory.setSerial_no(inventoryVal.getSerial_no());
                    inventory.setQuantity(1);
                    inventory.setIsUpadted(0);
                    inventory.setCreatedAt(inventory.getCreatedAt());
                    inventory.setUser(inventoryVal.getUser());
                    inventory.setSpecification("");
                    inventories.add(inventory);
                    //AppRepository.getInstance().putInventoryAPI(inventory);
                    Log.v(TAG, "inside for::" + inventory);
                }

                MyApplication.getDatabase().inventoryDao().insertAll(inventories);
                Log.v(TAG, "inventory is updated list::" + MyApplication.getDatabase().inventoryDao().getInventoryToUpdate());

                AppRepository.getInstance().postBulkInventoryAPI(MyApplication.getDatabase().inventoryDao().getInventoryToUpdate());
            }
        });

    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //invoice items dao


    public LiveData<List<InvoiceItems>> getItems(long localInvoiceId) {
        return MyApplication.getDatabase().invoiceItemDao().getAllItems(localInvoiceId);
    }

    public void insert(InvoiceItems invoiceItems) {
        new InsertInvoiceItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao()).execute(invoiceItems);
    }

    public void deleteAllItems(long localInvoiceId){
        new DeleteAllInvoiceItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao(),localInvoiceId).execute();
    }

    public void delete(InvoiceItems invoiceItems){
        new DeleteInvoiceItemsAsyncTask(MyApplication.getDatabase().invoiceItemDao()).execute(invoiceItems);
    }

    public void updateByLocalId(InvoiceItems invoiceItems){
        new UpdateInvoiceItemsByIdAsyncTask(MyApplication.getDatabase().invoiceItemDao()).execute(invoiceItems);
    }
    private static class DeleteAllInvoiceItemsAsyncTask extends AsyncTask<Void,Void,Void>{
        private InvoiceItemDao invoiceItemDao;
        private long localInvoiceId;
        private DeleteAllInvoiceItemsAsyncTask(InvoiceItemDao invoiceItemDao,long localInvoiceId){
            this.invoiceItemDao = invoiceItemDao;
            this.localInvoiceId = localInvoiceId;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            invoiceItemDao.deleteAll(localInvoiceId);
            return null;
        }
    }

    private static class DeleteInvoiceItemsAsyncTask extends AsyncTask<InvoiceItems,Void,Void>{
        private InvoiceItemDao invoiceItemDao;
        private DeleteInvoiceItemsAsyncTask(InvoiceItemDao invoiceItemDao){
            this.invoiceItemDao = invoiceItemDao;
        }

        @Override
        protected Void doInBackground(InvoiceItems... invoiceItems) {
            invoiceItemDao.delete(invoiceItems[0]);
            return null;
        }
    }

    private static class UpdateInvoiceItemsByIdAsyncTask extends AsyncTask<InvoiceItems,Void,Void>{
        private InvoiceItemDao invoiceItemDao;
        private UpdateInvoiceItemsByIdAsyncTask(InvoiceItemDao invoiceItemDao){
            this.invoiceItemDao = invoiceItemDao;
        }

        @Override
        protected Void doInBackground(InvoiceItems... invoiceItems) {
            // invoiceItems.update(invoiceItems[0]);
            invoiceItemDao.updateByLocalId(
                    invoiceItems[0].getMeasurementId(),
                    invoiceItems[0].getName(),
                    invoiceItems[0].getQuantity(),
                    invoiceItems[0].getPrice(),
                    invoiceItems[0].getGstType(),
                    invoiceItems[0].getGstAmount(),
                    invoiceItems[0].getGst(),
                    invoiceItems[0].isIs_active(),
                    invoiceItems[0].getUser(),
                    invoiceItems[0].getSerial_no(),
                    invoiceItems[0].getImei(),
                    invoiceItems[0].getTotalAmount(),
                    invoiceItems[0].getInvoiceid(),
                    invoiceItems[0].getIsSync(),
                    invoiceItems[0].getDatabaseid()
                    );
            return null;
        }
    }

    private static class InsertInvoiceItemsAsyncTask extends AsyncTask<InvoiceItems, Void, Void> {
        private InvoiceItemDao invoiceItemDao;
        private InsertInvoiceItemsAsyncTask(InvoiceItemDao invoiceItemDao) {
            this.invoiceItemDao = invoiceItemDao;
        }
        @Override
        protected Void doInBackground(InvoiceItems... invoiceItems) {
            invoiceItemDao.insert(invoiceItems[0]); //single invoiceitem
            return null;
        }
    }


    //////////////////////////Invoice activity
    public void insert(InvoiceModel invoiceModel) {
        new InsertInvoiceAsyncTask(MyApplication.getDatabase().newInvoiceDao()).execute(invoiceModel);
    }


    private static class InsertInvoiceAsyncTask extends AsyncTask<InvoiceModel, Void, Void> {
        private NewInvoiceDao newInvoiceDao;
        private InsertInvoiceAsyncTask(NewInvoiceDao newInvoiceDao) {
            this.newInvoiceDao  = newInvoiceDao;
        }
        @Override
        protected Void doInBackground(InvoiceModel... invoiceModel) {
            newInvoiceDao.Insert(invoiceModel[0]); //single invoiceItem
            return null;
        }
    }


    //----------------------API CAll-------------------------------------
    public void getCategoriesAPI(int page, final long syncTime) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<CategoryResponse> call = apiService.getCategories(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, final Response<CategoryResponse> response) {

                final CategoryResponse categoryResponse = response.body();
                Log.v(TAG, "categoryResponse::" + categoryResponse);


                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (categoryResponse != null && categoryResponse.getResults() != null && categoryResponse.getResults().size() > 0) {
                            MyApplication.getDatabase().categoriesDao().insertAll(categoryResponse.getResults());
                        }

                    }
                });


                if (categoryResponse.getNext() != null) {
                    String pageVal = categoryResponse.getNext().substring(categoryResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getCategoriesAPI(page, syncTime);
                } else {
                    sendSynchBrodCast(Constants.GET_CATEGORIES);
                    MyApplication.saveGetCategoriesLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate() / 1000);
                    checkForExtraCategories();
                }

            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Log.v(TAG, " getCategoriesAPI onFailure productResponse::");
                t.printStackTrace();

                MyApplication.saveGetCategoriesLAST_SYNC_TIMESTAMP(syncTime);
                sendSynchBrodCast(Constants.GET_CATEGORIES);


            }
        });
    }

    private void checkForExtraCategories() {
        MyApplication.getDatabase().categoriesDao().getAllCategories();
    }


    public void getBrandAPI(int page, final long syncTime) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<BrandResponse> call = apiService.getBrands(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<BrandResponse>() {
            @Override
            public void onResponse(Call<BrandResponse> call, final Response<BrandResponse> response) {
                final BrandResponse brandResponse = response.body();
                Log.v(TAG, "BrandResponse::" + brandResponse);
                if (brandResponse != null && brandResponse.getResults() != null && brandResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            MyApplication.getDatabase().brandDao().inserAll(response.body().getResults());

                        }
                    });
                }

                if (brandResponse.getNext() != null) {
                    String pageVal = brandResponse.getNext().substring(brandResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getBrandAPI(page, syncTime);
                } else {
                    sendSynchBrodCast(Constants.GET_BRAND);
                    MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate() / 1000);
                }
            }

            @Override
            public void onFailure(Call<BrandResponse> call, Throwable t) {
                Log.v(TAG, " getBrandAPI onFailure productResponse::");
                t.printStackTrace();
                sendSynchBrodCast(Constants.GET_BRAND);
                MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(syncTime);

            }
        });
    }

    public void getBrandAPIofCategory(final int categoryId, int page) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<BrandResponse> call = apiService.getBrandsOfCategory(headerMap, categoryId, page);
        call.enqueue(new Callback<BrandResponse>() {
            @Override
            public void onResponse(Call<BrandResponse> call, final Response<BrandResponse> response) {
                final BrandResponse brandResponse = response.body();
                Log.v(TAG, "getBrandAPIofCategory Response::" + brandResponse);
                if (brandResponse != null && brandResponse.getResults() != null && brandResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            MyApplication.getDatabase().brandDao().inserAll(response.body().getResults());

                        }
                    });
                }

                if (brandResponse.getNext() != null) {
                    String pageVal = brandResponse.getNext().substring(brandResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getBrandAPIofCategory(categoryId, page);
                } else {
                    //sendSynchBrodCast(Constants.GET_BRAND);
                    //MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate()/1000);
                }
            }

            @Override
            public void onFailure(Call<BrandResponse> call, Throwable t) {
                Log.v(TAG, " getBrandAPI onFailure productResponse::");
                t.printStackTrace();
                //sendSynchBrodCast(Constants.GET_BRAND);
                // MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(syncTime);

            }
        });
    }

    public void getProductAPI(int page, final long syncTime) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<ProductResponse> call = apiService.getProduct(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, final Response<ProductResponse> response) {
                final ProductResponse productResponse = response.body();
                Log.v(TAG, " onResponse productResponse::" + productResponse);
                if (productResponse != null && productResponse.getResults() != null && productResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            MyApplication.getDatabase().productDao().inserAll(productResponse.getResults());

                        }
                    });
                }


                if (productResponse.getNext() != null) {
                    String pageVal = productResponse.getNext().substring(productResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getProductAPI(page, syncTime);
                } else {
                    sendSynchBrodCast(Constants.GET_PRODUCT);
                    MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate() / 1000);

                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.v(TAG, "getProductAPI onFailure productResponse::");
                t.printStackTrace();
                sendSynchBrodCast(Constants.GET_PRODUCT);
                MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(syncTime);

            }
        });
    }

    public void getProductAPIOfBrand(final int brandId, int page) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<ProductResponse> call = apiService.getProductofBrand(headerMap, brandId, page, Constants.PAGE_LIMIT);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, final Response<ProductResponse> response) {
                final ProductResponse productResponse = response.body();
                Log.v(TAG, " onResponse productResponse::" + productResponse);
                if (productResponse != null && productResponse.getResults() != null && productResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            MyApplication.getDatabase().productDao().inserAll(productResponse.getResults());

                        }
                    });
                }

                if (productResponse.getNext() != null) {
                    String pageVal = productResponse.getNext().substring(productResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getProductAPIOfBrand(brandId, page);
                } else {
                    //sendSynchBrodCast(Constants.GET_PRODUCT);
                    //MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate()/1000);

                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.v(TAG, "getProductAPI onFailure productResponse::");
                t.printStackTrace();
                // sendSynchBrodCast(Constants.GET_PRODUCT);
                // MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(syncTime);

            }
        });
    }

    public void getUserInfoAPI() {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<UserReponse> call = apiService.getUser(headerMap);
        call.enqueue(new Callback<UserReponse>() {
            @Override
            public void onResponse(Call<UserReponse> call, Response<UserReponse> response) {

                UserReponse userReponse = response.body();
                final ArrayList<User> userArrayList = userReponse.getResults();
                if (userArrayList != null && userArrayList.size() > 0) {

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            User user = userArrayList.get(0);
                            MyApplication.saveLoginUserID(user.getId());
                            MyApplication.saveLoginUserID(user.getId());
                            MyApplication.getDatabase().userDao().inserUser(user);
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call<UserReponse> call, Throwable t) {
            }
        });
    }

    public void postBulkInventoryAPI(List<Inventory> inventoryList) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");

        Call<List<Inventory>> call = apiService.postBulkInventory(headerMap, inventoryList);
        call.enqueue(new Callback<List<Inventory>>() {
            @Override
            public void onResponse(Call<List<Inventory>> call, Response<List<Inventory>> response) {
                Log.v(TAG, "response::" + response);
                final List<Inventory> inventories = response.body();
                if (inventories != null && inventories.size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().inventoryDao().insertAll(inventories);
                            Log.v(TAG, "inventory is updated list::" + MyApplication.getDatabase().inventoryDao().getInventoryToUpdate());

                        }
                    });
                }

            }

            @Override
            public void onFailure(Call<List<Inventory>> call, Throwable t) {
            }
        });
    }

    /*public void putInventoryAPI(Inventory inventory) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");

        Call<Inventory> call=apiService.postInventory(headerMap,inventory);
        call.enqueue(new Callback<Inventory>() {
            @Override
            public void onResponse(Call<Inventory> call, Response<Inventory> response)
            {
                       Inventory inventory1=response.body();
                       Log.v(TAG,"inventory1::"+inventory1);
            }

            @Override
            public void onFailure(Call<Inventory> call, Throwable t) {

            }
        });
    }*/


    public void putProductAPI(final Product product) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");

        Call<ProductResponse> call = apiService.putProduct(headerMap, product);
        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {

                Log.d(TAG, "put product response Body::" + response.body());
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {


                    }
                });


                //handledResponse.onResponseSuccess(response.body());
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                // handledResponse.onResponseFailure();
            }
        });
    }

    public void putInvoiceAPI(final WebserviceResponseHandler handledResponse, final RequestInvoice invoice) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");

        Call<RequestInvoice> call = apiService.postInvoice(headerMap, invoice);
        call.enqueue(new Callback<RequestInvoice>() {
            @Override
            public void onResponse(Call<RequestInvoice> call, Response<RequestInvoice> response) {
                final RequestInvoice body = response.body();
                Log.d(TAG, "Invoice Body::" + body);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        Invoice invoice1 = MyApplication.getDatabase().invoiceDao().getInvoiceLocal(invoice.getLocal_id());
                        invoice1.setId(body.getId());
                        MyApplication.getDatabase().invoiceDao().insertInvoice(invoice1);
                    }
                });


                handledResponse.onResponseSuccess(response.body());
            }

            @Override
            public void onFailure(Call<RequestInvoice> call, Throwable t) {
                handledResponse.onResponseFailure();
            }
        });
    }

    public void getInventoryAPI(int page, final long syncTime) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<InventoryAPIResponse> call = apiService.getInventory(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<InventoryAPIResponse>() {
            @Override
            public void onResponse(Call<InventoryAPIResponse> call, Response<InventoryAPIResponse> response) {
                final InventoryAPIResponse inventoryAPIResponse = response.body();
                Log.v(TAG, " onResponse InventoryAPIResponse::" + inventoryAPIResponse);
                if (inventoryAPIResponse != null && inventoryAPIResponse.getResults() != null && inventoryAPIResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().inventoryDao().insertAll(inventoryAPIResponse.getResults());
                            Log.e("Inventory", "Size::" + MyApplication.getDatabase().inventoryDao().getAllInventory().size());
                        }
                    });

                }

                if (inventoryAPIResponse != null && inventoryAPIResponse.getNext() != null) {
                    String pageVal = inventoryAPIResponse.getNext().substring(inventoryAPIResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getInventoryAPI(page, syncTime);
                } else {
                    sendSynchBrodCast(Constants.GET_INVENTORY);
                    MyApplication.saveGetInventoryLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate() / 1000);
                }

            }

            @Override
            public void onFailure(Call<InventoryAPIResponse> call, Throwable t) {
                Log.v(TAG, " getInventoryAPI onFailure productResponse::" + t.getCause());
                sendSynchBrodCast(Constants.GET_INVENTORY);
                MyApplication.saveGetInventoryLAST_SYNC_TIMESTAMP(syncTime);

            }
        });
    }

    public void getDistributorAPI(int page, final long syncTime) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<DistributorAPIResponse> call = apiService.getDistributor(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<DistributorAPIResponse>() {
            @Override
            public void onResponse(Call<DistributorAPIResponse> call, Response<DistributorAPIResponse> response) {
                final DistributorAPIResponse distributorAPIResponse = response.body();
                Log.v(TAG, " onResponse InventoryAPIResponse::" + distributorAPIResponse);
                if (distributorAPIResponse != null && distributorAPIResponse.getResults() != null && distributorAPIResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().distributorDao().insertAll(distributorAPIResponse.getResults());
                        }
                    });

                }

                if (distributorAPIResponse != null && distributorAPIResponse.getNext() != null) {
                    String pageVal = distributorAPIResponse.getNext().substring(distributorAPIResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getDistributorAPI(page, syncTime);
                } else {
                    MyApplication.saveGetDistributorLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate() / 1000);
                }

            }

            @Override
            public void onFailure(Call<DistributorAPIResponse> call, Throwable t) {
                Log.v(TAG, " getInventoryAPI onFailure productResponse::" + t.getCause());
                sendSynchBrodCast(Constants.GET_INVENTORY);
                MyApplication.saveGetInventoryLAST_SYNC_TIMESTAMP(syncTime);

            }
        });
    }


    public void getPurchaseAPI(int page, final long syncTime) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<PurchaseAPIResponse> call = apiService.getPurchase(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<PurchaseAPIResponse>() {
            @Override
            public void onResponse(Call<PurchaseAPIResponse> call, Response<PurchaseAPIResponse> response) {

                final PurchaseAPIResponse purchaseAPIResponse = response.body();
                Log.v(TAG, " onResponse purchaseAPIResponse::" + purchaseAPIResponse);
                if (purchaseAPIResponse != null && purchaseAPIResponse.getResults() != null && purchaseAPIResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().purchaseDao().inserAll(purchaseAPIResponse.getResults());
                        }
                    });

                }

                if (purchaseAPIResponse != null && purchaseAPIResponse.getNext() != null) {
                    String pageVal = purchaseAPIResponse.getNext().substring(purchaseAPIResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getPurchaseAPI(page, syncTime);
                } else {
                    sendSynchBrodCast(Constants.GET_PURCHASE);
                    MyApplication.saveGetPurchaseLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate() / 1000);

                }
            }

            @Override
            public void onFailure(Call<PurchaseAPIResponse> call, Throwable t) {
                Log.v(TAG, " getPurchnageAPI onFailure productResponse::");
                sendSynchBrodCast(Constants.GET_PURCHASE);
                MyApplication.saveGetPurchaseLAST_SYNC_TIMESTAMP(syncTime);
            }
        });
    }

    private void sendSynchBrodCast(String apiName) {
        Intent intent = new Intent(Constants.SYNC_DATA_ACTION);
        intent.putExtra(Constants.API_NAME, apiName);
        MyApplication.context.sendBroadcast(intent);
    }

    public MutableLiveData<RequestInvoice> getInvoiceAPIForInvoiceIDAndDate(int invoiceID, final long dateTime) {
        final MutableLiveData<RequestInvoice> invoice = new MutableLiveData<>();
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<RequestInvoice> call = apiService.getInvoiceForInvoiceIDAndDate(headerMap, invoiceID);
        call.enqueue(new Callback<RequestInvoice>() {
            @Override
            public void onResponse(Call<RequestInvoice> call, Response<RequestInvoice> response) {
                Log.v(TAG, "getInvoiceAPIForInvoiceIDAndDate::" + response);
                if (response != null && response.body() != null) {
                    invoice.setValue(response.body());
                } else {

                }
            }

            @Override
            public void onFailure(Call<RequestInvoice> call, Throwable t) {

            }
        });
        return invoice;
    }

    public void getInvoiceAPI(int page, final long syncTime) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<InvoiceApiResponse> call = apiService.getInvoice(headerMap, page, Constants.PAGE_LIMIT, syncTime);
        call.enqueue(new Callback<InvoiceApiResponse>() {
            @Override
            public void onResponse(Call<InvoiceApiResponse> call, Response<InvoiceApiResponse> response) {

                final InvoiceApiResponse invoiceApiResponse = response.body();
                Log.v(TAG, " onResponse invoiceApiResponse::" + invoiceApiResponse);
                if (invoiceApiResponse != null && invoiceApiResponse.getResults() != null && invoiceApiResponse.getResults().size() > 0) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().invoiceDao().inserAll(invoiceApiResponse.getResults());
                        }
                    });

                }

                if (invoiceApiResponse != null && invoiceApiResponse.getNext() != null) {
                    String pageVal = invoiceApiResponse.getNext().substring(invoiceApiResponse.getNext().lastIndexOf("=") + 1);
                    Log.v(TAG, "pageVal::" + pageVal);
                    int page = Integer.parseInt(pageVal);
                    Log.v(TAG, "page::" + page);
                    getInvoiceAPI(page, syncTime);
                } else {
                    sendSynchBrodCast(Constants.GET_INVOICE);
                    MyApplication.saveGetInvoiceLAST_SYNC_TIMESTAMP(Util.getCurrentLongDate() / 1000);

                }
            }

            @Override
            public void onFailure(Call<InvoiceApiResponse> call, Throwable t) {
                Log.v(TAG, " getInvoiceAPI onFailure productResponse::");
                sendSynchBrodCast(Constants.GET_INVOICE);
                MyApplication.saveGetInvoiceLAST_SYNC_TIMESTAMP(syncTime);

            }
        });

    }

    public void insertAllInventories(ArrayList<Inventory> inventories) {
        MyApplication.getDatabase().inventoryDao().insertAll(inventories);
    }

    public void getCategoriesListOfZeroBrand() {
        List<Category> categoryList = new ArrayList<>();
        List<CategoriesBrandCount> categoriesBrandCountList = MyApplication.getDatabase().categoriesDao().getAllEmptyCategories();
        if (categoriesBrandCountList != null) {
            for (CategoriesBrandCount categoriesBrandCount : categoriesBrandCountList) {
                if (categoriesBrandCount.getBrandList().size() == 0) {
                    categoryList.add(categoriesBrandCount.getCategory());
                }
            }
            Log.v(TAG, "getCategoriesListOfZeroBrand::" + categoryList);

            if (categoryList.size() > 0) {
                for (Category category : categoryList) {
                    getBrandAPIofCategory(category.getId(), 1);
                }
            }
        }
    }

    public void getBrandListOfZeroProduct() {
        List<Brand> brandList = new ArrayList<>();
        List<BrandProductCount> brandProductCountList = MyApplication.getDatabase().brandDao().getAllEmptyBrand();
        if (brandProductCountList != null) {
            for (BrandProductCount brandProductCount : brandProductCountList) {
                if (brandProductCount.getProductList().size() == 0) {
                    brandList.add(brandProductCount.getBrand());
                }
            }

            Log.v(TAG, "getBrandListOfZeroProduct::" + brandList);
            if (brandList.size() > 0) {
                for (Brand brand : brandList) {
                    getProductAPIOfBrand(brand.getId(), 1);
                }
            }

        }
    }

    public MutableLiveData<GetProductInfoResponseFromSDK> getProductDetailsFromSDK(String scanText) {
        final MutableLiveData<GetProductInfoResponseFromSDK> getProductInfoResponseFromSDKMutableLiveData = new MutableLiveData<>();
        OkHttpClient.Builder client = new OkHttpClient.Builder();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        client.addInterceptor(loggingInterceptor);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.upcitemdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build();
        ApiInterface apiService = retrofit.create(ApiInterface.class);
        Call<GetProductInfoResponseFromSDK> call = apiService.getProductInfoFromSDK(scanText);
        call.enqueue(new Callback<GetProductInfoResponseFromSDK>() {
            @Override
            public void onResponse(Call<GetProductInfoResponseFromSDK> call, Response<GetProductInfoResponseFromSDK> response) {

                Log.v(TAG, "response.body()::" + response.body());
                getProductInfoResponseFromSDKMutableLiveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<GetProductInfoResponseFromSDK> call, Throwable t) {
                getProductInfoResponseFromSDKMutableLiveData.setValue(null);
            }
        });
        return getProductInfoResponseFromSDKMutableLiveData;
    }

    public MutableLiveData<Integer> SendReportAPI(final long fromTime, final long toTime) {
        final MutableLiveData<Integer> SendReportAPIMutableLiveData = new MutableLiveData<>();

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<Object> call = apiService.sendReport(headerMap, "invoice", toTime, fromTime, 1);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Log.v(TAG, "response.isSuccessful()::" + response.isSuccessful());
                Log.v(TAG, "response.code()::" + response.code());
                SendReportAPIMutableLiveData.setValue(response.code());
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.v(TAG, "onFailure");

                SendReportAPIMutableLiveData.setValue(400);

            }
        });
        return SendReportAPIMutableLiveData;
    }


    public MutableLiveData<ArrayList<RequestInvoice>> SearchInvoiceAPI(final String customer_mobile_no) {
        final MutableLiveData<ArrayList<RequestInvoice>> SendReportAPIMutableLiveData = new MutableLiveData<>();

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<SearchInvoiceAPIResponse> call = apiService.searchInvoice(headerMap, customer_mobile_no);
        call.enqueue(new Callback<SearchInvoiceAPIResponse>() {
            @Override
            public void onResponse(Call<SearchInvoiceAPIResponse> call, Response<SearchInvoiceAPIResponse> response) {
                if (response.body() != null) {
                    SendReportAPIMutableLiveData.setValue(response.body().getResults());
                } else {
                    SendReportAPIMutableLiveData.setValue(null);

                }
            }

            @Override
            public void onFailure(Call<SearchInvoiceAPIResponse> call, Throwable t) {
                SendReportAPIMutableLiveData.setValue(null);
            }
        });

        return SendReportAPIMutableLiveData;
    }

    public void postOtherCategoryAPI(Category category) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<Category> call = apiService.postOtherCategory(headerMap, category);
        call.enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, final Response<Category> response) {
                if (response.body() != null) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.v(TAG, "post cate response.body()::" + response.body());
                            MyApplication.getDatabase().categoriesDao().insertcategory(response.body());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable t) {

            }
        });


    }

    public void postOtherBrandAPI(Brand brand) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<Brand> call = apiService.postOtherBrand(headerMap, brand);
        call.enqueue(new Callback<Brand>() {
            @Override
            public void onResponse(Call<Brand> call, final Response<Brand> response) {
                Log.v(TAG, "response::" + response.body());
                if (response.body() != null) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().brandDao().inserBrand(response.body());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Brand> call, Throwable t) {

            }
        });


    }

    public void postOtherProductAPI(Product product) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Call<Product> call = apiService.postOtherProduct(headerMap, product);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, final Response<Product> response) {
                Log.v(TAG, "response::" + response.body());
                if (response.body() != null) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().productDao().inserProduct(response.body());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {

            }
        });
    }

    public void getLedgerDetails(final WebserviceResponseHandler handledResponse, final long fromTime, final long toTime, int categoryId, int models, int send) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");

        Call<Object> call = apiService.getLedgerDetails(headerMap, fromTime, toTime, 0, categoryId, models);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                final JSONObject body;
                try {
                    body = new JSONObject(new Gson().toJson(response.body()));
                    Log.d(TAG, "Invoice Body::" + body);
                    handledResponse.onResponseSuccess(body);
                } catch (JSONException e) {
                    e.printStackTrace();
                    handledResponse.onResponseFailure();
                }

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                handledResponse.onResponseFailure();
            }
        });
    }

}
