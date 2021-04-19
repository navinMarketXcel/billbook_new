package com.billbook.app.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.Purchase;

import java.util.List;


public class InvoicePurchaseAdapter extends RecyclerView.Adapter<InvoicePurchaseAdapter.MyViewHolder> {

    private Context mContext;
    private InvoicePurchaseAdapter.OnItemClickListener mItemClickListener;
    private List<Purchase> listItems;
    private boolean showGst;

    public InvoicePurchaseAdapter(Context context, List<Purchase> listItems, boolean showGst) {
        this.listItems = listItems;
        this.mContext = context;
        this.showGst = showGst;
    }

    public void setOnItemClickListener(final InvoicePurchaseAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public InvoicePurchaseAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice_item_layout, parent, false);

        return new InvoicePurchaseAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(InvoicePurchaseAdapter.MyViewHolder holder, int position) {


        if (listItems.get(position).getProduct_name() != null && !listItems.get(position)
                .getProduct_name()
                .equals("null")) {
            holder.tvProductName.setText(listItems.get(position).getCategory_name() + " - " + listItems.get(position).getBrand_name() + " - " + listItems.get(position).getProduct_name());

        }

        holder.tvQTY.setText(String.valueOf(listItems.get(position).getQuantity()));
        holder.tvRate.setText(String.format("%,.2f", (listItems.get(position).getPrice() - listItems.get(position).getTaxAmount())));
        float totalAmount = listItems.get(position).getPrice() * listItems.get(position).getQuantity();
        holder.tvAmount.setText(String.format("%,.2f", totalAmount));
        if (showGst) {
            holder.CGSTValue.setVisibility(View.VISIBLE);
            holder.IGSTValue.setVisibility(View.VISIBLE);
            holder.SGSTValue.setVisibility(View.VISIBLE);
            float Igst = totalAmount - (totalAmount / (1 + ((listItems.get(position).getIgst()) / 100)));
            float gst = totalAmount - (totalAmount / (1 + ((listItems.get(position).getGst()) / 100)));
            holder.CGSTValue.setText(String.format("%,.2f", gst / 2));
            holder.SGSTValue.setText(String.format("%,.2f", gst / 2));
            holder.IGSTValue.setText(String.format("%,.2f", Igst));

        } else {
            holder.CGSTValue.setVisibility(View.GONE);
            holder.IGSTValue.setVisibility(View.GONE);
            holder.SGSTValue.setVisibility(View.GONE);


        }


    }


    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvProductName, SGSTValue, CGSTValue, IGSTValue, tvQTY, tvRate, tvAmount;


        public MyViewHolder(View itemView) {
            super(itemView);
            tvProductName = (TextView) itemView.findViewById(R.id.tvProductName);
            tvQTY = (TextView) itemView.findViewById(R.id.tvQTY);
            tvRate = (TextView) itemView.findViewById(R.id.tvRate);
//            tvDiscount = (TextView) itemView.findViewById(R.id.tvItemDiscount);
            SGSTValue = (TextView) itemView.findViewById(R.id.SGSTValue);
            IGSTValue = (TextView) itemView.findViewById(R.id.IGSTValue);
            CGSTValue = (TextView) itemView.findViewById(R.id.CGSTValue);
            tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
            if (showGst)
                tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 3f));
            else
                tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 6f));
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
