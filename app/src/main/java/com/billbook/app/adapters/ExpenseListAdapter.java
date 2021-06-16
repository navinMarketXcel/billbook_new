package com.billbook.app.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.billbook.app.R;
import com.billbook.app.activities.AddExpenseActivity;
import com.billbook.app.database.models.Expense;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ExpenseListAdapter extends RecyclerView.Adapter<ExpenseListAdapter.MyViewHolder> implements Filterable {

    private ArrayList<Expense> expenseArrayList;
    private ArrayList<Expense> expenseArrayListFiltered;
    private Context context;

    public ExpenseListAdapter(Context context, ArrayList<Expense> expenseArrayList) {
        this.context = context;
        this.expenseArrayList = expenseArrayList;
        expenseArrayListFiltered = expenseArrayList;
    }

    @Override
    public ExpenseListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ExpenseListAdapter.MyViewHolder holder, final int position) {
        holder.expenseAmountTV.setText(Util.formatDecimalValue( (float) expenseArrayListFiltered.get(position).getAmount()));
        holder.expenseDateTV.setText(""+expenseArrayListFiltered.get(position).getDate());
        holder.expenseNameTV.setText(""+expenseArrayListFiltered.get(position).getName());
        holder.expenseAmountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddExpenseActivity.class);
                intent.putExtra("expense",expenseArrayListFiltered.get(position));
                intent.putExtra("id",expenseArrayListFiltered.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseArrayListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    expenseArrayListFiltered = expenseArrayList;
                } else {
                    List<Expense> filteredList = new ArrayList<>();
                    for (Expense expense : expenseArrayList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (expense.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(expense);
                        }
                    }

                    expenseArrayListFiltered = (ArrayList<Expense>) filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = expenseArrayListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                expenseArrayListFiltered = (ArrayList<Expense>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView expenseDateTV,expenseNameTV,expenseAmountTV;

        public MyViewHolder(View view) {
            super(view);
            expenseDateTV = (TextView) view.findViewById(R.id.expenseDateTV);
            expenseNameTV = (TextView) view.findViewById(R.id.expenseNameTV);
            expenseAmountTV = (TextView) view.findViewById(R.id.expenseAmountTV);

        }
    }
}