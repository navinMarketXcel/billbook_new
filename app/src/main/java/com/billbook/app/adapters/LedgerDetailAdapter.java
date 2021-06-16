package com.billbook.app.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billbook.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LedgerDetailAdapter extends RecyclerView.Adapter<LedgerDetailAdapter.MyViewHolder> {

    private static final String TAG = "SellingDetailAdapter";
    private boolean isProduct;
    private JSONArray productArrayList;

    public LedgerDetailAdapter(JSONArray productArrayList) {
        this.productArrayList = productArrayList;
    }

    @Override
    public LedgerDetailAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selling_item_layout, parent, false);
        return new LedgerDetailAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LedgerDetailAdapter.MyViewHolder holder, int position) {
        JSONObject productAndCount = null;
        try {
            productAndCount = productArrayList.getJSONObject(position);
            if (productAndCount != null) {
                holder.tvProductName.setText(productAndCount.getString("name"));
                holder.tvProductCount.setText("" + productAndCount.getInt("qty"));
                holder.tvProductPrice.setVisibility(View.VISIBLE);
                holder.tvProductPrice.setText(String.format("%.2f", productAndCount.getDouble("totalPrice")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {

        return productArrayList.length();
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
