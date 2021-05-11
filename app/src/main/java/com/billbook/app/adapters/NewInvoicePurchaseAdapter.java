package com.billbook.app.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.NewInvoiceModels;
import com.billbook.app.utils.Util;

import java.util.Arrays;
import java.util.List;


public class NewInvoicePurchaseAdapter extends RecyclerView.Adapter<NewInvoicePurchaseAdapter.MyViewHolder> {

    private Context mContext;
    private NewInvoicePurchaseAdapter.OnItemClickListener mItemClickListener;
    private List<NewInvoiceModels> listItems;
    private boolean isGSTAvailable;
    private List<String> measurementUnitList;
    public NewInvoicePurchaseAdapter(Context context, List<NewInvoiceModels> listItems,boolean isGSTAvailable) {
        this.listItems = listItems;
        this.mContext = context;
        this.isGSTAvailable=isGSTAvailable;
        this.measurementUnitList = Arrays.asList(context.getResources().getStringArray(R.array.measurementUnit));
    }

    public void setOnItemClickListener(final NewInvoicePurchaseAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public NewInvoicePurchaseAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice_item_layout, parent, false);

        return new NewInvoicePurchaseAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewInvoicePurchaseAdapter.MyViewHolder holder, int position) {

        holder.tvProductName.setText(listItems.get(position).getName() +
                (listItems.get(position).getSerial_no().length()>0?" HSN - "+listItems.get(position).getSerial_no():"")+
                (listItems.get(position).getImei().length()>0?" ,IMEI/Serial Number - "+listItems.get(position).getImei():""));
        String qtyString = String.valueOf(listItems.get(position).getQuantity());
        if(listItems.get(position).getMeasurementId() > -1){
            qtyString += " " + measurementUnitList.get(listItems.get(position).getMeasurementId());
        }
        holder.tvQTY.setText(qtyString);
//        holder.tvRate.setText(Util.formatDecimalValue( listItems.get(position).getPrice()));
        holder.tvAmount.setText(Util.formatDecimalValue( listItems.get(position).getTotalAmount()));
        if (listItems.get(position).getGst()>0 || isGSTAvailable) {
            holder.preTaxValue.setVisibility(View.VISIBLE);
            float gstVlaue = listItems.get(position).getTotalAmount() - listItems.get(position).getGstAmount();
            float gst = 1 + (listItems.get(position).getGst() / 100);
            float preTaxSingleValue =  listItems.get(position).getPrice() / gst;
            holder.tvRate.setText(Util.formatDecimalValue(preTaxSingleValue));
            holder.preTaxValue.setText(Util.formatDecimalValue(preTaxSingleValue * listItems.get(position).getQuantity()));

            if(this.isGSTAvailable){
                if(listItems.get(position).getGstType() != null && listItems.get(position).getGstType().equals("IGST (Central/outstation customer)")) {
                    holder.CGSTValue.setVisibility(View.GONE);
                    holder.SGSTValue.setVisibility(View.GONE);
                    holder.IGSTValue.setVisibility(View.VISIBLE);
                    holder.IGSTValue.setText(Util.formatDecimalValue(gstVlaue));
                    holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f));
                }else if(listItems.get(position).getGstType() != null && listItems.get(position).getGstType().equals("CGST/SGST (Local customer)")){
                    holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 3.5f));
                    holder.CGSTValue.setVisibility(View.VISIBLE);
                    holder.SGSTValue.setVisibility(View.VISIBLE);
                    holder.IGSTValue.setVisibility(View.GONE);
                    holder.CGSTValue.setText(Util.formatDecimalValue( gstVlaue / 2));
                    holder.SGSTValue.setText(Util.formatDecimalValue( gstVlaue / 2));
                }else {
                    holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));
                    holder.CGSTValue.setVisibility(View.GONE);
                    holder.IGSTValue.setVisibility(View.GONE);
                    holder.SGSTValue.setVisibility(View.GONE);
                }
            }
        }
        else {
            holder.tvRate.setText(Util.formatDecimalValue( listItems.get(position).getPrice()));
            holder.CGSTValue.setVisibility(View.GONE);
            holder.IGSTValue.setVisibility(View.GONE);
            holder.SGSTValue.setVisibility(View.GONE);
            holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));
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
        TextView tvProductName, SGSTValue, CGSTValue, IGSTValue, tvQTY, tvRate, tvAmount, preTaxValue;


        public MyViewHolder(View itemView) {
            super(itemView);
            tvProductName = (TextView) itemView.findViewById(R.id.tvProductName);
            tvQTY = (TextView) itemView.findViewById(R.id.tvQTY);
            preTaxValue = itemView.findViewById(R.id.tv_preTaxValue);
            tvRate = (TextView) itemView.findViewById(R.id.tvRate);
//            tvDiscount = (TextView) itemView.findViewById(R.id.tvItemDiscount);
            SGSTValue = (TextView) itemView.findViewById(R.id.SGSTValue);
            IGSTValue = (TextView) itemView.findViewById(R.id.IGSTValue);
            CGSTValue = (TextView) itemView.findViewById(R.id.CGSTValue);
            tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
//            if (showGst)
            tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 3f));
//            else
//                tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
//                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 6f));
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
