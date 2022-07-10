package com.billbook.app.networkcommunication;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.billbook.app.database.models.Brand;
import com.billbook.app.database.models.Category;
import com.billbook.app.database.models.Distributor;
import com.billbook.app.database.models.Expense;
import com.billbook.app.database.models.Inventory;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.database.models.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiInterface {


    @POST("login/")
    Call<LoginResponse> doLogin(@Body LoginRequest loginRequest);

    @POST("vendor/")
    Call<Object> registerVendor(@HeaderMap Map<String, String> headers, @Body RequestBody user);

    @GET("user/")
    Call<UserReponse> getUser(@HeaderMap Map<String, String> headers);

    @GET("category/")
    Call<CategoryResponse> getCategories(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);

    @GET("category_list/")
    Call<ArrayList<Category>> getCategoriesWithoutHeader(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);


    @GET("brand/")
    Call<BrandResponse> getBrands(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);

    @GET("brand/")
    Call<BrandResponse> getBrandsOfCategory(@HeaderMap Map<String, String> headers, @Query("category") int category, @Query("page") int page);

    @GET("product/")
    Call<ProductResponse> getProduct(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);

    @PUT("product/1/")
    Call<ProductResponse> putProduct(@HeaderMap Map<String, String> headers, @Body Product product);

    @GET("product/")
    Call<ProductResponse> getProductofBrand(@HeaderMap Map<String, String> headers, @Query("brand") int brand, @Query("page") int page, @Query("limit") int limit);


    @GET("purchase/")
    Call<PurchaseAPIResponse> getPurchase(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);


    @GET("invoice/")
    Call<InvoiceApiResponse> getInvoice(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);

    @GET("invoice/{invoiceID}")
    Call<RequestInvoice> getInvoiceForInvoiceIDAndDate(@HeaderMap Map<String, String> headers, @Path("invoiceID") int invoiceID);


    @POST("invoice/")
    Call<RequestInvoice> postInvoice(@HeaderMap Map<String, String> headers, @Body RequestInvoice invoice);

    @PATCH("invoice/{invoiceID}/")
    Call<RequestInvoice> patchInvoice(@HeaderMap Map<String, String> headers, @Path("invoiceID") int invoiceID, @Body RequestInvoice invoice);

    @POST("inventory/")
    Call<Inventory> postInventory(@HeaderMap Map<String, String> headers, @Body Inventory inventory);

    @POST("bulk_inventory/")
    Call<List<Inventory>> postBulkInventory(@HeaderMap Map<String, String> headers, @Body List<Inventory> inventoryList);

    @GET("inventory/")
    Call<InventoryAPIResponse> getInventory(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);

    @GET("distributor/")
    Call<DistributorAPIResponse> getDistributor(@HeaderMap Map<String, String> headers, @Query("page") int page, @Query("limit") int limit, @Query("date") long date);


    @GET("prod/trial/lookup")
    Call<GetProductInfoResponseFromSDK> getProductInfoFromSDK(@Query("upc") String upc);


    @PATCH("user/{id}/")
    Call<User> patchUser(@HeaderMap Map<String, String> headers, @Path("id") int userId, @Body RequestBody password);

    @GET("export/{invoice}/")
    Call<Object> sendReport(@HeaderMap Map<String, String> headers, @Path("invoice") String invoice, @Query("to") long to, @Query("from") long from, @Query("send") int send);


    @Multipart
    @PATCH("invoice/{invoiceID}/")
    Call<RequestInvoice> uploadInvoicePDf(@HeaderMap Map<String, String> headers, @Path("invoiceID") int invoiceID, @Part MultipartBody.Part pdf);

    @GET("invoice/")
    Call<SearchInvoiceAPIResponse> searchInvoice(@HeaderMap Map<String, String> headers, @Query("customer_mobile_no") String customer_mobile_no);

    @POST("category/")
    Call<Category> postOtherCategory(@HeaderMap Map<String, String> headers, @Body Category category);

    @POST("brand/")
    Call<Brand> postOtherBrand(@HeaderMap Map<String, String> headers, @Body Brand brand);

    @POST("product/")
    Call<Product> postOtherProduct(@HeaderMap Map<String, String> headers, @Body Product product);

    @POST("mail_password/")
    Call<Object> forgotPasswordLedger(@HeaderMap Map<String, String> headers, @Body String body);

    @PATCH("user/{id}/")
    Call<User> updateUser(@HeaderMap Map<String, String> headers, @Path("id") int userId, @Body Map<String, String> gst_no);

    @PUT ("users/{id}")
    Call<Object> updateUserGstStatus(@Path("id") long userId,@Body Map<String, String> gst_no);

    @GET("send_report/")
    Call<Object> getLedgerDetails(@HeaderMap Map<String, String> headers, @Query("from") long from, @Query("to") long to, @Query("send") int send, @Query("category") long category, @Query("brand") long brand);

    @GET("refVersion/")
    Call<Object> getVersionUpdate(@HeaderMap Map<String, String> headers);

    @POST("getLastInvoiceNumber")
    Call<Object> getLatestInvoice(@HeaderMap Map<String, String> headers,@Body Map<String, String> body);

    @GET("user/state/")
    Call<ArrayList<String>> getStates(@HeaderMap Map<String, String> headers);

    @PATCH("user/{id}/")
    Call<User> updateCategories(@HeaderMap Map<String, String> headers, @Path("id") int userId, @Body Map<String, ArrayList<Integer>>
            category);

    @GET("mail_password/")
    Call<Object> sendOTP(@HeaderMap Map<String, String> headers, @Body String body);

    @GET("user/verify/")
    Call<ArrayList<JSONObject>> verifyUserName(@QueryMap(encoded = true) Map<String, String> filters);

    @POST("distributor/")
    Call<Distributor> addDistributor(@HeaderMap Map<String, String> headers, @Body Distributor
            distributor);

    @POST ("getOTP")
    Call<Object> getOTP(@Body HashMap<String, String> body);

    @POST ("verifyOTP")
    Call<Object> verifyOTP(@Body HashMap<String, String> body);

    @POST ("users")
    Call<Object> users(@Body HashMap<String, String> body);

    @POST ("invoice")
    Call<Object> invoice(@Body JsonObject body);

    @GET ("invoices/{userid}/{page}")
    Call<Object> invoices(@HeaderMap Map<String, String> headers, @Path("userid") int userid,@Path("page") int page);

    @GET ("expense/{userid}")
    Call<Object> expenses(@HeaderMap Map<String, String> headers, @Path("userid") int userid);

    @POST ("expense")
    Call<Object> expenses(@HeaderMap Map<String, String> headers, @Body Expense expense);

    @POST ("expenseInBulk")
    Call<Object> expensesInBulk(@HeaderMap Map<String, String> headers, @Body JsonArray req);

    @PUT ("expense/{id}")
    Call<Object> updateExpenses(@HeaderMap Map<String, String> headers,@Path("id") int id, @Body Expense expense);

    @DELETE("expense/{id}")
    Call<Object> deleteExpenses(@HeaderMap Map<String, String> headers,@Path("id") long id);

    @POST("invoices/deleteInBulk")
    Call<Object> deleteSearchBills(@HeaderMap Map<String, String> headers,@Body JsonObject object);

    @Multipart
    @PUT ("updateInvoicePdf/{id}")
    Call<Object> updateInvoicePdf(@HeaderMap Map<String, String> headers, @Path("id") int id, @Part MultipartBody.Part pdf);

    @PUT ("updateInvoice/{id}")
    Call<Object> updateInvoice(@HeaderMap Map<String, String> headers, @Path("id") long id,@Body JsonObject object);

    @GET("getInvoiceUrl/{id}")
    Call<Object>getCutlyUrl(@HeaderMap Map<String, String> headers,@Path("id") int id);

    @PUT ("daybook/{userid}")
    Call<Object> getDayBook(@HeaderMap Map<String, String> headers, @Path("userid") int userid,@Body JsonObject object);


    @PUT ("exportDayBook/{userid}")
    Call<Object> exportDayBook(@HeaderMap Map<String, String> headers, @Path("userid") int userid,@Body JsonObject object);
    @Multipart
    @PUT ("users/{userid}")
    Call<Object> updateUser(@HeaderMap Map<String, String> headers, @Path("userid") long userid, @Part MultipartBody.Part pdf, @PartMap Map<String,RequestBody> params);


    @Multipart
    @PUT ("users/{userid}")
    Call<Object> updateUser(@HeaderMap Map<String, String> headers, @Path("userid") long userid, @Part MultipartBody.Part pdf, @PartMap Map<String,RequestBody> params, @Part MultipartBody.Part signaturePdf, @Part MultipartBody.Part companyImagePdf);

    @POST ("searchInvoice")
    Call<Object> searchInvoice(@HeaderMap Map<String, String> headers, @Body Map<String, String> body);

    @POST ("loggerAPI")
    Call<Object> loggerAPI(@Body JSONObject body);

    @GET("measurements")
    Call<Object>measuremntUnit(@HeaderMap Map<String,String> headers);

    @POST("getPincode")
    Call<Object>pincode(@Body HashMap<String,String> body);

    @POST("searchItems")
    Call<Object>searchItem(@Body HashMap<String,String> body);

    @POST("searchMeasurementId")
    Call<Object>fetchMeasurementIdForItem(@Body HashMap<String,String> body);

    @GET("getSignupUrl/")
    Call<Object> getSignupUrl();

    @POST("getUserDetails/")
    Call<Object> getUserDetails(@Body HashMap<String,String> body);

    @POST("findCustomer")
    Call<Object>findCustomer(@Body HashMap<String ,String> body);

}
