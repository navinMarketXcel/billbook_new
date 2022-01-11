package com.billbook.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.activities.BillingNewActivity;

import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ContentValues.TAG;

public class AutocompleteItemAdapter extends ArrayAdapter<String> {
    final String TAG = "AutoComplete";

    private ArrayList<String> itemList;
    private ArrayList<Integer> unitList;
    TextView itemSuggestion;
    Spinner measurementUnitSpinner;

//    public AutocompleteItemAdapter(ArrayList<String> itemList, ArrayList<Integer> unitList, Context context){
//
//        this.itemList = itemList;
//        this.context = context;
//        this.unitList = unitList;
//    }

    public AutocompleteItemAdapter(@NonNull Context context, int resource, ArrayList<String> itemList, ArrayList<Integer> unitList) {
        super(context, 0 ,itemList);
        this.itemList = itemList;
        this.unitList = unitList;
        Log.d(TAG, "AutocompleteItemAdapter: " + itemList + " " + unitList);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
         view = LayoutInflater.from(getContext()).inflate(R.layout.layout_autocomplete,parent,false);

        }
        View view1 = LayoutInflater.from(getContext()).inflate(R.layout.layout_item_bill,parent,false);
        String item = itemList.get(position);
        Integer unit = unitList.get(position);
        itemSuggestion = view.findViewById(R.id.itemSuggestion);
        measurementUnitSpinner = view1.findViewById(R.id.unit);
        itemSuggestion.setText(item);
        measurementUnitSpinner.setSelection(Integer.valueOf(unit));
        Log.d(TAG, "getView: item, unit " + item + " " + unit + " " + measurementUnitSpinner.getSelectedItemId());


        return view;
    }

    @Override
    public Filter getFilter() {
        Log.d(TAG, "getFilter: ");
        return itemFilter;
    }

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d(TAG, "performFiltering: ");
            FilterResults results = new FilterResults();
            ArrayList<String> suggestions = new ArrayList<>();

            suggestions.addAll(itemList);
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.d(TAG, "publishResults: ");
            clear();
            Log.d(TAG, "publishResults: results " + results.values);
            addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Log.d(TAG, "convertResultToString: " + resultValue);
            return ((String) resultValue);
        }
    };
//
//    @NonNull
//    @Override
//    public AutocompleteItemAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.id.itemNameET,parent,false);
//        return new AutocompleteItemAdapter.MyViewHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull AutocompleteItemAdapter.MyViewHolder holder, int position) {
//
//        String item = itemList.get(position);
//        Integer unit = unitList.get(position);
//        holder.itemNameET.setText(item);
//        holder.measurementUnitSpinner.setSelection(unit);
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//
//    public class MyViewHolder extends RecyclerView.ViewHolder {
//        public AutoCompleteTextView itemNameET;
//        public Spinner measurementUnitSpinner;
//        public MyViewHolder(View view) {
//            super(view);
//
//            itemNameET = view.findViewById(R.id.itemNameET);
//            measurementUnitSpinner = view.findViewById(R.id.unit);
//        }
//    }
}


