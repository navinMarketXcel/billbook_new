package com.billbook.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.Expense;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends ArrayAdapter<Expense> {

    private Context context;
    private int resourceId;
    private List<Expense> items, tempItems, suggestions;
    public ExpenseAdapter(@NonNull Context context, int resourceId, ArrayList<Expense> items) {
        super(context, resourceId, items);
        this.items = items;
        this.context = context;
        this.resourceId = resourceId;
        tempItems = new ArrayList<>(items);
        suggestions = new ArrayList<>();
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        try {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                view = inflater.inflate(resourceId, parent, false);
            }
            Expense model = getItem(position);
            TextView name = (TextView) view.findViewById(R.id.modelName);
//            name.setTextColor(Color.BLACK);
            name.setText(model.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
    @Nullable
    @Override
    public Expense getItem(int position) {
        return items.get(position);
    }
    @Override
    public int getCount() {
        return items.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @NonNull
    @Override
    public Filter getFilter() {
        return fruitFilter;
    }
    private Filter fruitFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Expense fruit = (Expense) resultValue;
            return fruit.getName();
        }
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence != null) {
                suggestions.clear();
                for (Expense fruit: tempItems) {
                    if (fruit.getName().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {
                        suggestions.add(fruit);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            ArrayList<Expense> tempValues = (ArrayList<Expense>) filterResults.values;
            if (filterResults != null && filterResults.count > 0) {
                clear();
                for (Expense expense  : tempValues) {
                    add(expense);
                }
                notifyDataSetChanged();
            } else {
                clear();
                notifyDataSetChanged();
            }
        }
    };
}

