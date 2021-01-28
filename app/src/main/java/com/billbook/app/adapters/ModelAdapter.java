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
import com.billbook.app.database.models.Model;

import java.util.ArrayList;
import java.util.List;

public class ModelAdapter extends ArrayAdapter<Model> {

    private Context context;
    private int resourceId;
    private List<Model> items, tempItems, suggestions;
    public ModelAdapter(@NonNull Context context, int resourceId, ArrayList<Model> items) {
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
            Model model = getItem(position);
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
    public Model getItem(int position) {
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
            Model fruit = (Model) resultValue;
            return fruit.getName();
        }
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence != null) {
                suggestions.clear();
                for (Model fruit: tempItems) {
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
            ArrayList<Model> tempValues = (ArrayList<Model>) filterResults.values;
            if (filterResults != null && filterResults.count > 0) {
                clear();
                for (Model fruitObj : tempValues) {
                    add(fruitObj);
                }
                notifyDataSetChanged();
            } else {
                clear();
                notifyDataSetChanged();
            }
        }
    };
}

