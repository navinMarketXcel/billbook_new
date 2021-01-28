package com.billbook.app.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductSelectionAdapter extends RecyclerView.Adapter<ProductSelectionAdapter.MyViewHolder> implements Filterable {

    private static final String TAG = "BilliADP";
    private ArrayList<Product> productrrayList;
    private ArrayList<Product> productArrayListFiltered;
    private Context context;

    public ProductSelectionAdapter(Context context, ArrayList<Product> brandArrayList) {
        this.context = context;
        this.productrrayList = brandArrayList;
        this.productArrayListFiltered = brandArrayList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    productArrayListFiltered = productrrayList;
                } else {
                    List<Product> filteredList = new ArrayList<>();
                    for (Product product : productrrayList) {
                        if (product.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(product);
                        }
                    }

                    productArrayListFiltered = (ArrayList<Product>) filteredList;
                    Log.v(TAG, " performFiltering brandArrayListFiltered::" + productArrayListFiltered);
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = productArrayListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                productArrayListFiltered = (ArrayList<Product>) filterResults.values;
                Log.v(TAG, " publishResults brandArrayListFiltered::" + productArrayListFiltered);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categories_item_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Product product = productArrayListFiltered.get(position);
        holder.tvCategoriesName.setText(product.getName());
        holder.selected.setVisibility(View.VISIBLE);
        boolean flag = productArrayListFiltered.get(position).isSelected();
//        Log.v(TAG," status::"+flag);
        holder.selected.setChecked(productArrayListFiltered.get(position).isSelected());
    }

    @Override
    public int getItemCount() {
        return productArrayListFiltered.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCategoriesName;
        public CheckBox selected;
        private CardView card_view;

        public MyViewHolder(View view) {
            super(view);
            card_view = view.findViewById(R.id.card_view);
            tvCategoriesName = (TextView) view.findViewById(R.id.tvCategoriesName);
            selected = view.findViewById(R.id.selected);
            selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    int adapterPosition = getAdapterPosition();
                    productArrayListFiltered.get(adapterPosition).setSelected(b);//=b;
                }
            });
//            this.setIsRecyclable(false);
        }
    }
}