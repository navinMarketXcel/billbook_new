package com.billbook.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.InvoiceItems;
import com.billbook.app.database.models.NewInvoiceModels;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewBillingAdapter extends RecyclerView.Adapter<NewBillingAdapter.MyViewHolder>  {
    private static final String TAG = "BilliADP";
    private ArrayList<InvoiceItems> newInvoiceModels;
    private Context context;
    private onItemClick onItemClick;
    private boolean isGSTAvailable;
    private onItemClick mOnItemClick;
    private TextView additemTv;
    private List<String> measurementUnitTypeList;
    public NewBillingAdapter(ArrayList<InvoiceItems> newInvoiceModels, Context context, boolean isGSTAvailable, onItemClick mOnItemClick) {
        this.newInvoiceModels = newInvoiceModels;
        this.context = context;
        onItemClick = (NewBillingAdapter.onItemClick) context;
        this.isGSTAvailable=isGSTAvailable;
        this.mOnItemClick= mOnItemClick;

        getMeasurementUnit();
        if(measurementUnitTypeList==null)
            this.measurementUnitTypeList=  Arrays.asList (context.getResources().getStringArray(R.array.measurementUnit));
    }

    @NonNull
    @Override
    public NewBillingAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_bill_item, parent, false);
        return new NewBillingAdapter.MyViewHolder(itemView,mOnItemClick);
    }

    @Override
    public void onBindViewHolder(@NonNull NewBillingAdapter.MyViewHolder myViewHolder, @SuppressLint("RecyclerView") final int position) {
        InvoiceItems newInvoiceModel = newInvoiceModels.get(position);
        //myViewHolder.tvSrNoTv.setText(""+(position+1));
        myViewHolder.itemNameHdTv.setText(newInvoiceModel.getName());
        String quantity = newInvoiceModel.getQuantity() + "";
        if(newInvoiceModel.getMeasurementId() > -1){
            quantity += " " + measurementUnitTypeList.get(newInvoiceModel.getMeasurementId());
        }
        myViewHolder.quantityHdTv.setText(quantity);
        //myViewHolder.totalHdTv.setText(Util.formatDecimalValue(newInvoiceModel.getTotalAmount()));
        myViewHolder.priceHdTv.setText(Util.formatDecimalValue(newInvoiceModel.getPrice()));
        if(isGSTAvailable){
            myViewHolder.gstTV.setVisibility(View.VISIBLE);
            myViewHolder.gstTV.setText((int)newInvoiceModel.getGst()+("%"));
        }else{
            myViewHolder.gstTV.setVisibility(View.GONE);
        }
//        myViewHolder.editLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onItemClick.itemClick(position,true);
//            }
//        });
//        myViewHolder.deleteLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onItemClick.itemClick(position,false);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return newInvoiceModels.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvSrNoTv,itemNameHdTv,quantityHdTv,priceHdTv,totalHdTv,gstTV;
        public TextView edit,delete;
        public TableRow deleteLayout,editLayout;
        onItemClick onitemclick;

        public MyViewHolder(View view,onItemClick onitemclick) {
            super(view);
            this.onitemclick = onitemclick;

            //tvSrNoTv = view.findViewById(R.id.tvSrNoTv);
            itemNameHdTv = view.findViewById(R.id.itemNameHdTv);
            quantityHdTv = view.findViewById(R.id.quantityHdTv);
            priceHdTv = view.findViewById(R.id.priceHdTv);
            //totalHdTv = view.findViewById(R.id.totalHdTv);
            // edit = view.findViewById(R.id.edit);
            //delete = view.findViewById(R.id.delete);
            gstTV = view.findViewById(R.id.gstTV);
            //deleteLayout = view.findViewById(R.id.deleteLayout);
            //editLayout= view.findViewById(R.id.editLayout);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onitemclick.itemClick(getAdapterPosition(),true);


        }
    }
    public interface  onItemClick {
        public void itemClick(int position,boolean isEdit);
    }

    public void getMeasurementUnit(){
        try{
            List<String>onlineMeasurementUnit = MyApplication.getMeasurementUnits();
            if(onlineMeasurementUnit!=null)measurementUnitTypeList = onlineMeasurementUnit;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
