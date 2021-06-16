package com.billbook.app.activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.adapters.PurchaseListAdapter;
import com.billbook.app.database.models.Inventory;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.PrintInvoice;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CancelInvoiceActivity extends AppCompatActivity {
    private RecyclerView productRv;
    private PurchaseListAdapter purchaseListAdapter;
    private ArrayList<Purchase> previousPurchases = new ArrayList();
    private HashMap<Integer, Integer> mapOfPurchases = new HashMap<>();

    private PrintInvoice printInvoice;
    private ArrayList<Inventory> inventories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_invoice);
        productRv = findViewById(R.id.productList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        productRv.setLayoutManager(mLayoutManager);
        printInvoice = (PrintInvoice) getIntent().getSerializableExtra("mPrintInvoiceObj");
        for (int i = 0; i < printInvoice.getPurchaseArrayList().size(); i++) {
            previousPurchases.add(printInvoice.getPurchaseArrayList().get(i));
            mapOfPurchases.put(printInvoice.getPurchaseArrayList().get(i).getId(),
                    printInvoice.getPurchaseArrayList().get(i).getQuantity());
        }
        Log.e("MAP", mapOfPurchases.toString());
//        previousPurchases.addAll((Collection<? extends Purchase>) printInvoice.getPurchaseArrayList().clone());
        purchaseListAdapter = new PurchaseListAdapter(this, printInvoice.getPurchaseArrayList());
        productRv.setItemAnimator(new DefaultItemAnimator());
        productRv.setAdapter(purchaseListAdapter);
    }

    public void cancelInvoice(View v) {
        printInvoice.getPurchaseArrayList();
        RequestInvoice requestInvoice = new RequestInvoice();
        Invoice invoice = printInvoice.getInvoice();
        requestInvoice.setId(printInvoice.getInvoice().getId());
        requestInvoice.setInvoice_no(printInvoice.getStrInvoiceNo());

        requestInvoice.setPurchase(printInvoice.getPurchaseArrayList());
        float total = 0;
        for (Purchase purchase : printInvoice.getPurchaseArrayList()) {
            if (purchase.isIs_active())
                total = total + (purchase.getPrice() * purchase.getQuantity());
        }
        requestInvoice.setTotal_amount(total);
        if (total == 0)
            requestInvoice.setIs_active(false);
        if (total <= invoice.getPaymentMethod1Amount()) {
            requestInvoice.setPaymentMethod1Amount(total);
            requestInvoice.setPaymentMethod2Amount(0);
        } else {
            requestInvoice.setPaymentMethod1Amount(invoice.getPaymentMethod1Amount());
            requestInvoice.setPaymentMethod2Amount(total - invoice.getPaymentMethod1Amount());
        }
        Log.e("Invoice", new Gson().toJson(requestInvoice));
        if (Util.isNetworkAvailable(this)) {
            final ProgressDialog progressDialog = DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            String token = MyApplication.getUserToken();
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", token);
            headerMap.put("Content-Type", "application/json");

            Call<RequestInvoice> call = apiService.patchInvoice(headerMap, requestInvoice.getId(), requestInvoice);
            call.enqueue(new Callback<RequestInvoice>() {
                @Override
                public void onResponse(Call<RequestInvoice> call, Response<RequestInvoice> response) {
                    Toast.makeText(CancelInvoiceActivity.this, "Invoice canceled Successfuly",
                            Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    insertInventory();
//                    CancelInvoiceActivity.this.finish();
                }

                @Override
                public void onFailure(Call<RequestInvoice> call, Throwable t) {
                    Toast.makeText(CancelInvoiceActivity.this, "Failed to update Invoice",
                            Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    private void insertInventory() {
        inventories.clear();
        for (int i = 0; i < mapOfPurchases.size(); i++) {
            for (int j = 0; j < printInvoice.getPurchaseArrayList().size(); j++) {
                Log.v("Inventory::", "" + printInvoice.getPurchaseArrayList().get(j).getQuantity() + "," +
                        mapOfPurchases.get(printInvoice.getPurchaseArrayList().get(j).getId()));

                if (mapOfPurchases.containsKey(printInvoice.getPurchaseArrayList().get(j).getId()) &&
                        printInvoice.getPurchaseArrayList().get(j).getQuantity() != mapOfPurchases.get(printInvoice.getPurchaseArrayList().get(j).getId())
                ) {
                    int cnt = mapOfPurchases.get(printInvoice.getPurchaseArrayList().get(j).getId()) -
                            printInvoice.getPurchaseArrayList().get(j).getQuantity();
                    Purchase purchase = previousPurchases.get(i);
                    for (int k = 0; k < cnt; k++) {
                        Inventory inventory = new Inventory();
                        inventory.setId(0);
                        inventory.setProduct_id(purchase.getProduct_id());
                        inventory.setProduct_name(purchase.getProduct_name());
                        inventory.setSerial_no(purchase.getSerial_no());
                        inventory.setCategory(purchase.getCategory());
                        inventory.setCategory_name(purchase.getCategory_name());
                        inventory.setBrand(purchase.getBrand());
                        inventory.setBrand_name(purchase.getCategory_name());
                        inventory.setUser(MyApplication.getUserID());
                        inventory.setQuantity(1);
                        inventory.setPrice(purchase.getPrice());
                        inventory.setTax(purchase.getTax());
                        inventory.setSelling_price(purchase.getPrice());
                        inventories.add(inventory);
                    }
                }
            }
        }
        Log.v("Inventory::", "" + inventories.size());
        if (inventories.size() > 0) {
            if (Util.isNetworkAvailable(this)) {
//                MyApplication.getDatabase().inventoryDao().insertAll(inventories);
                ApiInterface apiService =
                        ApiClient.getClient().create(ApiInterface.class);

                String token = MyApplication.getUserToken();

                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("Authorization", token);
                headerMap.put("Content-Type", "application/json");

                Call<List<Inventory>> call = apiService.postBulkInventory(headerMap, inventories);
                call.enqueue(new Callback<List<Inventory>>() {
                    @Override
                    public void onResponse(Call<List<Inventory>> call, Response<List<Inventory>> response) {
                        Log.v("CancelInv", "response::" + response);
                        final List<Inventory> inventories = response.body();
                        if (inventories != null && inventories.size() > 0) {
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    MyApplication.getDatabase().inventoryDao().insertAll(inventories);
                                    Log.v("CancelInv", "inventory is updated list::" + MyApplication.getDatabase().inventoryDao().getInventoryToUpdate());
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Inventory>> call, Throwable t) {
                        Toast.makeText(CancelInvoiceActivity.this, "Failed to insert inventories", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        } else {
            Toast.makeText(CancelInvoiceActivity.this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();

        }
    }

}
