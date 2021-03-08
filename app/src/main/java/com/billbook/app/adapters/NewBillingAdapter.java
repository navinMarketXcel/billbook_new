package com.billbook.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.NewInvoiceModels;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewBillingAdapter extends RecyclerView.Adapter<NewBillingAdapter.MyViewHolder>  {
    private static final String TAG = "BilliADP";
    private ArrayList<NewInvoiceModels> newInvoiceModels;
    private Context context;
    private onItemClick onItemClick;
    private boolean isGSTAvailable;
    private List<String> measurementUnitTypeList;
    public NewBillingAdapter(ArrayList<NewInvoiceModels> newInvoiceModels, Context context, boolean isGSTAvailable) {
        this.newInvoiceModels = newInvoiceModels;
        this.context = context;
        onItemClick = (NewBillingAdapter.onItemClick) context;
        this.isGSTAvailable=isGSTAvailable;

        this.measurementUnitTypeList=  Arrays.asList (context.getResources().getStringArray(R.array.measurementUnit));
    }

    @NonNull
    @Override
    public NewBillingAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_bill_item, parent, false);
        return new NewBillingAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewBillingAdapter.MyViewHolder myViewHolder, @SuppressLint("RecyclerView") final int position) {
        NewInvoiceModels newInvoiceModel = newInvoiceModels.get(position);
        myViewHolder.tvSrNoTv.setText(""+(position+1));
        myViewHolder.itemNameHdTv.setText(newInvoiceModel.getName());
        String quantity = newInvoiceModel.getQuantity() + "";
        if(measurementUnitTypeList.get(newInvoiceModel.getMeasurementId()) != null){
            quantity += " " + measurementUnitTypeList.get(newInvoiceModel.getMeasurementId());
        }
        myViewHolder.quantityHdTv.setText(quantity);
        myViewHolder.totalHdTv.setText(Util.formatDecimalValue(newInvoiceModel.getTotalAmount()));
        myViewHolder.priceHdTv.setText(Util.formatDecimalValue(newInvoiceModel.getPrice()));
        if(isGSTAvailable){
            myViewHolder.gstTV.setVisibility(View.VISIBLE);
            myViewHolder.gstTV.setText((int)newInvoiceModel.getGst()+("%"));
        }else{
            myViewHolder.gstTV.setVisibility(View.GONE);
        }
        myViewHolder.editLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.itemClick(position,true);
            }
        });
        myViewHolder.deleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.itemClick(position,false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newInvoiceModels.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSrNoTv,itemNameHdTv,quantityHdTv,priceHdTv,totalHdTv,gstTV;
        public TextView edit,delete;
        public TableRow deleteLayout,editLayout;

        public MyViewHolder(View view) {
            super(view);
            tvSrNoTv = view.findViewById(R.id.tvSrNoTv);
            itemNameHdTv = view.findViewById(R.id.itemNameHdTv);
            quantityHdTv = view.findViewById(R.id.quantityHdTv);
            priceHdTv = view.findViewById(R.id.priceHdTv);
            totalHdTv = view.findViewById(R.id.totalHdTv);
            edit = view.findViewById(R.id.edit);
            delete = view.findViewById(R.id.delete);
            gstTV = view.findViewById(R.id.gstTV);
            deleteLayout = view.findViewById(R.id.deleteLayout);
            editLayout= view.findViewById(R.id.editLayout);
        }
    }
    public interface  onItemClick {
        public void itemClick(int position,boolean isEdit);
    }
}
