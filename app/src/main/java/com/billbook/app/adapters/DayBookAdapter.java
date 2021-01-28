package com.billbook.app.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.DayBook;
import com.billbook.app.utils.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DayBookAdapter extends RecyclerView.Adapter<DayBookAdapter.MyViewHolder>  {

    private static final String TAG = "BrandAdapter";
    private ArrayList<DayBook> dayBookArrayList;
    private Context context;
    SimpleDateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    public DayBookAdapter(Context context, ArrayList<DayBook> dayBookArrayList) {
        this.context = context;
        this.dayBookArrayList = dayBookArrayList;
    }


    @Override
    public DayBookAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daybook_item, parent, false);
        return new DayBookAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DayBookAdapter.MyViewHolder holder, int position) {
         DayBook dayBook = dayBookArrayList.get(position);
        holder.daybookname.setText(dayBook.getName());
        if(dayBook.isExpense()){
            holder.sale_expense.setText("-"+ Util.formatDecimalValue((float)dayBook.getAmount()));

        }else{
            holder.sale_income.setText(Util.formatDecimalValue((float)dayBook.getAmount()));

        }
        holder.date.setText(myFormat1.format(dayBook.getDate()));


    }

    @Override
    public int getItemCount() {
        return dayBookArrayList.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView daybookname,sale_expense,date,sale_income;

        public MyViewHolder(View view) {
            super(view);
            daybookname = (TextView) view.findViewById(R.id.daybookname);
            sale_expense = (TextView) view.findViewById(R.id.sale_expense);
            date = (TextView) view.findViewById(R.id.date);
            sale_income = view.findViewById(R.id.sale_income);

        }
    }

}