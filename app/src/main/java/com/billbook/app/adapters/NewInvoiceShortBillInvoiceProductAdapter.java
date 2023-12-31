package com.billbook.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.billbook.app.R;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.NewInvoiceModels;
import com.billbook.app.utils.Util;

import java.util.Arrays;
import java.util.List;

public class NewInvoiceShortBillInvoiceProductAdapter extends RecyclerView.Adapter<NewInvoiceShortBillInvoiceProductAdapter.MyViewHolder> {
    private Context mContext;
    private NewInvoiceShortBillInvoiceProductAdapter.OnItemClickListener mItemClickListener;
    private List<NewInvoiceModels> listItems;
    private List<InvoiceItems> curItems;
    private boolean isGSTAvailable;
    private List<String> measurementUnitList;
    private String GSTType="";
    public NewInvoiceShortBillInvoiceProductAdapter(Context context, List<InvoiceItems> curItems,boolean isGSTAvailable,String GSTType) {
        this.curItems = curItems;
        this.mContext = context;
        this.isGSTAvailable=isGSTAvailable;
        this.GSTType = GSTType;
        getMeasurementUnit();
        if(this.measurementUnitList==null){
            this.measurementUnitList = Arrays.asList(context.getResources().getStringArray(R.array.measurementUnit));
        }
    }

    public void setOnItemClickListener(final NewInvoiceShortBillInvoiceProductAdapter.OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public NewInvoiceShortBillInvoiceProductAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.short_bill_item_layout, parent, false);

        return new NewInvoiceShortBillInvoiceProductAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NewInvoiceShortBillInvoiceProductAdapter.MyViewHolder holder, int position) {

       /* holder.tvProductName.setText(curItems.get(position).getName() +
                (curItems.get(position).getSerial_no().length()>0?" HSN - "+curItems.get(position).getSerial_no():"")+
                (curItems.get(position).getImei().length()>0?" ,IMEI/Serial Number - "+curItems.get(position).getImei():""));*/

        holder.productDescription.setText(curItems.get(position).getName());
        String qtyString = String.valueOf(curItems.get(position).getQuantity());
        if(curItems.get(position).getMeasurementId() > -1){
            qtyString += " " + measurementUnitList.get(curItems.get(position).getMeasurementId());
        }
        //holder.invoiceItemsQty.setText(String.valueOf(curItems.get(position).getQuantity()));
        holder.productLabel.setText(qtyString);
       // holder.invoiceItemsQty.setText(qtyString);
        //holder.productMRP.setText(String.valueOf(Util.formatDecimalValue(curItems.get(position).getPrice())));
        float gst = 1 + (curItems.get(position).getGst() / 100);
        float preTaxSingleValue =  curItems.get(position).getPrice() / gst;
        holder.productAmnt.setText(Util.formatDecimalValue(preTaxSingleValue * curItems.get(position).getQuantity()));

        holder.rate.setText(String.valueOf(Util.formatDecimalValue(curItems.get(position).getPrice())));
        holder.imei.setText(curItems.get(position).getImei());

        if(this.isGSTAvailable){
            holder.productTax.setText(String.valueOf((int)curItems.get(position).getGst())+"%");
            holder.hsnItemLaySf.setText(curItems.get(position).getSerial_no());
        } else {
            holder.llForHeader.setWeightSum(3.0f);
            holder.productTax.setVisibility(View.GONE);
            holder.hsnItemLaySf.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return curItems.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView productDescription, productLabel, productMRP, rate, productAmnt, productTax,invoiceItemsQty,imei,hsnItemLaySf;
        LinearLayout llForHeader,editLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            imei = itemView.findViewById(R.id.imei);
            hsnItemLaySf = itemView.findViewById(R.id.hsnItemLaySf);
            invoiceItemsQty = itemView.findViewById(R.id.invoiceItemsQty);
            llForHeader = itemView.findViewById(R.id.llForHeader);
            productDescription = itemView.findViewById(R.id.productDescription);
            productLabel = itemView.findViewById(R.id.productLabel);
            rate = itemView.findViewById(R.id.rate);
            productAmnt = itemView.findViewById(R.id.productAmnt);
            productTax = itemView.findViewById(R.id.productTax);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(view, getPosition());
            }
        }
    }

    public void getMeasurementUnit(){
        try{
            List<String>onlineMeasurementUnit = MyApplication.getMeasurementUnits();
            if(onlineMeasurementUnit!=null)this.measurementUnitList = onlineMeasurementUnit;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    method to set listener to the adapter ViewHolder item
     */
}