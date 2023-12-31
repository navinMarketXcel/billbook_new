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
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.NewInvoiceModels;
import com.billbook.app.utils.Util;

import java.util.Arrays;
import java.util.List;


public class NewInvoicePurchaseAdapter extends RecyclerView.Adapter<NewInvoicePurchaseAdapter.MyViewHolder> {

    private Context mContext;
    private NewInvoicePurchaseAdapter.OnItemClickListener mItemClickListener;
    private List<NewInvoiceModels> listItems;
    private List<InvoiceItems> curItems;
    private boolean isGSTAvailable;
    private List<String> measurementUnitList;
    private String GSTType="";
    float totalpretaxvalue=0;
    public NewInvoicePurchaseAdapter(Context context, List<InvoiceItems> curItems,boolean isGSTAvailable,String GSTType) {
        this.curItems = curItems;
        this.mContext = context;
        this.isGSTAvailable=isGSTAvailable;
        this.GSTType = GSTType;
        getMeasurementUnit();
        if(this.measurementUnitList==null){
            this.measurementUnitList = Arrays.asList(context.getResources().getStringArray(R.array.measurementUnit));
        }
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

       /* holder.tvProductName.setText(curItems.get(position).getName() +
                (curItems.get(position).getSerial_no().length()>0?" HSN - "+curItems.get(position).getSerial_no():"")+
                (curItems.get(position).getImei().length()>0?" ,IMEI/Serial Number - "+curItems.get(position).getImei():""));*/

        holder.tvProductName.setText(curItems.get(position).getName());
       holder.tvProductNumber.setText(String.valueOf(position+1));
       holder.imeinoOnBill.setText( curItems.get(position).getImei());
       holder.hsnOnBill.setText(curItems.get(position).getSerial_no());

        String qtyString = String.valueOf(curItems.get(position).getQuantity());
        if(curItems.get(position).getMeasurementId() > -1){
            qtyString += " " + measurementUnitList.get(curItems.get(position).getMeasurementId());
        }
        holder.tvQTY.setText(qtyString);
        holder.tvAmount.setText(Util.formatDecimalValue( curItems.get(position).getTotalAmount()));

       // holder.llForHeader.setWeightSum(isGSTAvailable ? (float)10.5 : 9);

        if (curItems.get(position).getGst()>0 || isGSTAvailable) {
            holder.preTaxValue.setVisibility(View.VISIBLE);
            float gstVlaue = curItems.get(position).getTotalAmount() - curItems.get(position).getGstAmount();
            float gst = 1 + (curItems.get(position).getGst() / 100);
            float preTaxSingleValue =  curItems.get(position).getPrice() / gst;
           // holder.tvRate.setText(Util.formatDecimalValue(preTaxSingleValue));
            holder.tvRate.setText(Util.formatDecimalValue( curItems.get(position).getPrice()));
            holder.preTaxValue.setText(Util.formatDecimalValue(preTaxSingleValue * curItems.get(position).getQuantity()));
             totalpretaxvalue=totalpretaxvalue+preTaxSingleValue * curItems.get(position).getQuantity();
            if(this.isGSTAvailable){
                holder.hsnOnBill.setVisibility(View.VISIBLE);
//                if(curItems.get(position).getGstType() != null && curItems.get(position).getGstType().equals("IGST (Central/outstation customer)")) {
                if(GSTType.equals("IGST (Central/outstation customer)")){
                    holder.CGSTValue.setVisibility(View.GONE);
                    holder.SGSTValue.setVisibility(View.GONE);
//                    holder.SGSTTaxLabel.setVisibility(View.GONE);
//                    holder.CGSTTaxLabel.setVisibility(View.GONE);
                    holder.IGSTValue.setVisibility(View.VISIBLE);
                    holder.IGSTValue.setText(Util.formatDecimalValue(gstVlaue)+"\n"+(float)curItems.get(position).getGst()+"%");
                    holder.llForHeader.setWeightSum(10f);
                    /* holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 4.5f));*/

                }else
//                    if(curItems.get(position).getGstType() != null && curItems.get(position).getGstType().equals("CGST/SGST (Local customer)")){
                        if(GSTType.equals("CGST/SGST (Local customer)")){
                    /*holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 3.5f));*/
                    holder.CGSTValue.setVisibility(View.VISIBLE);
//                    holder.CGSTTaxLabel.setText((((float)curItems.get(position).getGst()))/2+"%");
//                    holder.SGSTTaxLabel.setText((((float)curItems.get(position).getGst()))/2+"%");
                    holder.SGSTValue.setVisibility(View.VISIBLE);
                    holder.IGSTValue.setVisibility(View.GONE);
                    holder.CGSTValue.setText(Util.formatDecimalValue( gstVlaue / 2)+"\n"+(float)curItems.get(position).getGst()/2+"%");
                    holder.SGSTValue.setText(Util.formatDecimalValue( gstVlaue / 2)+"\n"+(float)curItems.get(position).getGst()/2+"%");

                }else {
                   /* holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));*/
                    holder.CGSTValue.setVisibility(View.GONE);
                    holder.IGSTValue.setVisibility(View.GONE);
                    holder.SGSTValue.setVisibility(View.GONE);
//                            holder.SGSTTaxLabel.setVisibility(View.GONE);
//                            holder.CGSTTaxLabel.setVisibility(View.GONE);
                            holder.llForHeader.setWeightSum(8.5f);

                        }
            }
        }
        else {
//            holder.SGSTTaxLabel.setVisibility(View.GONE);
//            holder.CGSTTaxLabel.setVisibility(View.GONE);
            holder.hsnOnBill.setVisibility(View.GONE);
            holder.tvRate.setText(Util.formatDecimalValue( curItems.get(position).getPrice()));
            holder.CGSTValue.setVisibility(View.GONE);
            holder.IGSTValue.setVisibility(View.GONE);
            holder.SGSTValue.setVisibility(View.GONE);
            holder.llForHeader.setWeightSum(7.0f);
           /* holder.tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 5.5f));*/
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
        TextView tvProductNumber,tvProductName, SGSTValue, CGSTValue, IGSTValue, tvQTY, tvRate, tvAmount, preTaxValue,imeinoOnBill,hsnOnBill,SGSTTaxLabel,CGSTTaxLabel;
        LinearLayout llForHeader,getLlForHeaderItem;

        public MyViewHolder(View itemView) {
            super(itemView);
            llForHeader = itemView.findViewById(R.id.llForHeader);
//            SGSTTaxLabel = itemView.findViewById(R.id.SGSTTaxLabel);
//            CGSTTaxLabel = itemView.findViewById(R.id.CGSTTaxLabel);
            tvProductName = (TextView) itemView.findViewById(R.id.tvProductName);
            tvQTY = (TextView) itemView.findViewById(R.id.tvQTY);
            preTaxValue = itemView.findViewById(R.id.tv_preTaxValue);
            tvRate = (TextView) itemView.findViewById(R.id.tvRate);
           tvProductNumber = (TextView) itemView.findViewById(R.id.tvProductNumber);
//            tvDiscount = (TextView) itemView.findViewById(R.id.tvItemDiscount);
            SGSTValue = (TextView) itemView.findViewById(R.id.SGSTValue);
            IGSTValue = (TextView) itemView.findViewById(R.id.IGSTValue);
            CGSTValue = (TextView) itemView.findViewById(R.id.CGSTValue);
            tvAmount = (TextView) itemView.findViewById(R.id.tvAmount);
            imeinoOnBill = (TextView) itemView.findViewById(R.id.imeiNoOnBill);
            hsnOnBill = (TextView) itemView.findViewById(R.id.HsnNoOnBill);
//            if (showGst)
           /* tvProductName.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 3f));*/
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
