package com.billbook.app.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.billbook.app.R;
import com.billbook.app.adapters.PurchaseListAdapterNew;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CancelInvoiceActivityNew extends AppCompatActivity implements PurchaseListAdapterNew.TotalAmount {
    private RecyclerView productRv;
    private PurchaseListAdapterNew purchaseListAdapterNew;
    private JSONObject invoice;
    private JSONArray items;
    private float totalAmount =0;
    private TextView totalAmountTV;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_invoice);
        productRv = findViewById(R.id.productList);
        totalAmountTV = findViewById(R.id.TotalAmount);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        productRv.setLayoutManager(mLayoutManager);
        try {
            invoice = new JSONObject(getIntent().getStringExtra("invoice"));
            items = invoice.getJSONArray("masterItems");
            totalAmount = invoice.getInt("totalAmount");
            totalAmountTV.setText("Total Bill Amount - "+invoice.getInt("totalAmount"));
            purchaseListAdapterNew = new PurchaseListAdapterNew(this,items);
            productRv.setAdapter(purchaseListAdapterNew);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void cancelInvoice(View v) {
        if (Util.isNetworkAvailable(CancelInvoiceActivityNew.this)) {
            progressDialog = DialogUtils.startProgressDialog(CancelInvoiceActivityNew.this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            String token = MyApplication.getUserToken();

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Authorization", token);
            Call<Object> call = null;
            try {
                JsonObject  jsonObject = new JsonParser().parse(invoice.toString()).getAsJsonObject();
                call = apiService.updateInvoice(headerMap,invoice.getInt("id"),jsonObject);
                call.enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        final JSONObject body;
                        try {
                            body = new JSONObject(new Gson().toJson(response.body()));

                            if (body.getBoolean("status")) {

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();


                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        progressDialog.dismiss();
                    }
                });
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }

        } else {
            DialogUtils.showToast(CancelInvoiceActivityNew.this, getString(R.string.no_internet));
        }
    }

    @Override
    public void calculateTotalAmount(int position, JSONObject item) {
            try {
                float totalAmount = 0;
               for(int i=0;i<items.length();i++){
                   totalAmount=totalAmount+items.getJSONObject(i).getInt("totalAmount");
               }
                    totalAmountTV.setText("Total Bill Amount - " + Util.formatDecimalValue(totalAmount));
               invoice.put("totalAmount",totalAmount);
               if(totalAmount == 0)
                   invoice.put("is_active",false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

    }
}
