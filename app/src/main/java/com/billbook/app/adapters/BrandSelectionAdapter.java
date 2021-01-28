package com.billbook.app.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.billbook.app.activities.ManageProductsActivity;
import com.billbook.app.database.models.Brand;

import java.util.ArrayList;
import java.util.List;

public class BrandSelectionAdapter extends RecyclerView.Adapter<BrandSelectionAdapter.MyViewHolder> implements
        Filterable {

    private static final String TAG = "BilliADP";
    private ArrayList<Brand> brandArrayList;
    private ArrayList<Brand> brandArrayListFiltered;
    private Context context;

    public BrandSelectionAdapter(Context context, ArrayList<Brand> brandArrayList) {
        this.context = context;
        this.brandArrayList = brandArrayList;
        this.brandArrayListFiltered = brandArrayList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    brandArrayListFiltered = brandArrayList;
                } else {
                    List<Brand> filteredList = new ArrayList<>();
                    for (Brand brand : brandArrayList) {
                        if (brand.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(brand);
                        }
                    }

                    brandArrayListFiltered = (ArrayList<Brand>) filteredList;
                    Log.v(TAG, " performFiltering brandArrayListFiltered::" + brandArrayListFiltered);
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = brandArrayListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                brandArrayListFiltered = (ArrayList<Brand>) filterResults.values;
                Log.v(TAG, " publishResults brandArrayListFiltered::" + brandArrayListFiltered);
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
        final Brand brand = brandArrayListFiltered.get(position);
        holder.tvCategoriesName.setText(brand.getName());
        holder.selected.setVisibility(View.VISIBLE);
        boolean flag = brandArrayListFiltered.get(position).isSelected();
//        Log.v(TAG," status::"+flag);
        holder.selected.setChecked(brandArrayListFiltered.get(position).isSelected());

    }

    @Override
    public int getItemCount() {
        return brandArrayListFiltered.size();
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
                    brandArrayListFiltered.get(adapterPosition).setSelected(b);//=b;
                }
            });
            tvCategoriesName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ManageProductsActivity.class);
                    int adapterPosition = getAdapterPosition();
                    intent.putExtra("category", brandArrayListFiltered.get(adapterPosition).getCategory());
                    intent.putExtra("brand", brandArrayListFiltered.get(adapterPosition).getId());
                    context.startActivity(intent);
                }
            });
//            this.setIsRecyclable(false);
        }
    }
}