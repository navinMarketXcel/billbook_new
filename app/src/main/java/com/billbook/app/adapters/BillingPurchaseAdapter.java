package com.billbook.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.Purchase;

import java.util.List;


public class BillingPurchaseAdapter extends RecyclerView.Adapter<BillingPurchaseAdapter.MyViewHolder> {

    public List<Purchase> listItems;
    private Context mContext;
    private BillingPurchaseAdapter.OnItemClickListener mItemClickListener;

    public BillingPurchaseAdapter(Context context, List<Purchase> listItems) {
        this.listItems = listItems;
        this.mContext = context;
    }

    public void setOnItemClickListener(final BillingPurchaseAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public BillingPurchaseAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_billing_products, parent, false);

        return new BillingPurchaseAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BillingPurchaseAdapter.MyViewHolder holder, int position) {

        if (listItems.get(position).getSerial_no() != null && !listItems.get(position)
                .getSerial_no()
                .equals("null")) {
            holder.tvSrNo.setText(listItems.get(position).getSerial_no());
        }

        if (listItems.get(position).getProduct_name() != null && !listItems.get(position)
                .getProduct_name()
                .equals("null")) {
            holder.tvProductName.setText(listItems.get(position).getProduct_name());
        }
        if (listItems.get(position).getBrand_name() != null && !listItems.get(position)
                .getBrand_name()
                .equals("null")) {
            holder.tvBrand.setText(listItems.get(position).getBrand_name());
        }

        //if (listItems.get(position).getmHSNCode() != null && !listItems.get(position)
        //    .getmHSNCode()
        //    .equals("null")) {
        //  holder.tvHSNCode.setText(listItems.get(position).getmHSNCode());
        //}
        if (true) {
            holder.tvQTY.setText(String.valueOf(listItems.get(position).getQuantity()));
        }
        if (true) {
            holder.tvRate.setText(String.valueOf(listItems.get(position).getPrice()));
        }
        if (true) {
            holder.tvAmount.setText(String.valueOf(listItems.get(position).getPrice() * listItems.get(position).getQuantity()));
        }


        //holder.mTxtSubTitle.setSelected(true);


    }


    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvSrNo, tvProductName, tvBrand, tvQTY, tvRate, tvAmount;


        public MyViewHolder(View itemView) {
            super(itemView);
            tvSrNo = (TextView) itemView.findViewById(R.id.tvSrNo);
            tvQTY = (TextView) itemView.findViewById(R.id.tvQTY);
            tvRate = (TextView) itemView.findViewById(R.id.tvRate);
            tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
            tvProductName = (TextView) itemView.findViewById(R.id.tvProductName);
            tvBrand = (TextView) itemView.findViewById(R.id.tvBrand);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, getPosition());
            }
        }
    }

    /*
    method to set listener to the adapter ViewHolder item
     */
}
