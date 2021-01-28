package com.billbook.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.Purchase;

import java.util.ArrayList;

public class PurchaseListAdapter extends RecyclerView.Adapter<PurchaseListAdapter.MyViewHolder> {

    private static final String TAG = "BrandAdapter";
    private ArrayList<Purchase> purchases;
    private Context context;

    public PurchaseListAdapter(Context context, ArrayList<Purchase> brandArrayList) {
        this.context = context;
        this.purchases = brandArrayList;
    }

    @Override
    public PurchaseListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cancel_inv_item, parent, false);
        return new PurchaseListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PurchaseListAdapter.MyViewHolder holder, int position) {
        final Purchase purchase = purchases.get(position);
        holder.productName.setText(purchase.getCategory_name() + " - " + purchase.getBrand_name() + " - " + purchase.getProduct_name());
        holder.productQty.setSelection(purchase.getQuantity());
        holder.productQty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > purchase.getQuantity()) {
                    holder.productQty.setSelection(purchase.getQuantity());
                } else {
                    if (position == 0) {
                        purchase.setQuantity(0);
                        purchase.setIs_active(false);
                    } else {
                        float taxPerItem = purchase.getTaxAmount() / purchase.getQuantity();
                        purchase.setTaxAmount(taxPerItem * position);
                        purchase.setQuantity(position);
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return purchases.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView productName;
        public Spinner productQty;

        public MyViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.productName);
            productQty = (Spinner) view.findViewById(R.id.productQty);
        }
    }

}