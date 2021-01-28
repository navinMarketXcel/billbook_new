package com.billbook.app.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.ProductAndCount;

import java.util.ArrayList;

public class SellingDetailAdapter extends RecyclerView.Adapter<SellingDetailAdapter.MyViewHolder> {

    private static final String TAG = "SellingDetailAdapter";
    boolean isProduct;
    private ArrayList<ProductAndCount> productArrayList;
    private ArrayList<ProductAndCount> productAndInventoryListFiltered;


    public SellingDetailAdapter(ArrayList<ProductAndCount> productArrayList, boolean isProduct) {
        this.productArrayList = productArrayList;
        this.productAndInventoryListFiltered = productArrayList;
        this.isProduct = isProduct;
    }

    @Override
    public SellingDetailAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selling_item_layout, parent, false);
        return new SellingDetailAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SellingDetailAdapter.MyViewHolder holder, int position) {
        ProductAndCount productAndCount = productAndInventoryListFiltered.get(position);
        if (productAndCount != null) {
            holder.tvProductName.setText(productAndCount.getProduct_name());
            holder.tvProductCount.setText("" + productAndCount.getTotal());
            if (isProduct) {
                holder.tvProductPrice.setVisibility(View.VISIBLE);
                holder.tvProductPrice.setText(String.format("%,.2f", productAndCount.getPrice()));
            } else {

                holder.tvProductPrice.setVisibility(View.GONE);

            }
        }


    }

    @Override
    public int getItemCount() {
        if (productAndInventoryListFiltered != null) {

            return productAndInventoryListFiltered.size();
        } else
            return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProductName, tvProductCount, tvProductPrice;


        public MyViewHolder(View view) {
            super(view);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvProductCount = view.findViewById(R.id.tvProductCount);
            tvProductPrice = view.findViewById(R.id.tvProductPrice);
        }
    }

}
