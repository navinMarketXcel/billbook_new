package com.billbook.app.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.activities.ManageBrandsActivity;
import com.billbook.app.database.models.Category;

import java.util.ArrayList;

public class CategoriesSelectionAdapter extends RecyclerView.Adapter<CategoriesSelectionAdapter.MyViewHolder> {

    private static final String TAG = "BilliADP";
    private ArrayList<Category> categoryArrayList;
    private ArrayList<Category> categoryArrayListFiltered;
    private Context context;

    public CategoriesSelectionAdapter(Context context, ArrayList<Category> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.categoryArrayListFiltered = categoryArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categories_item_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final Category category = categoryArrayListFiltered.get(position);
        holder.tvCategoriesName.setText(category.getName());
        holder.selected.setVisibility(View.VISIBLE);
        boolean flag = categoryArrayListFiltered.get(position).isSelected();
//        Log.v(TAG," status::"+flag);
        holder.selected.setChecked(categoryArrayListFiltered.get(position).isSelected());
    }

    @Override
    public int getItemCount() {
        return categoryArrayListFiltered.size();
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
                    categoryArrayList.get(adapterPosition).setSelected(b);//=b;
                }
            });
            tvCategoriesName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ManageBrandsActivity.class);
                    int adapterPosition = getAdapterPosition();
                    intent.putExtra("category", categoryArrayList.get(adapterPosition).getId());
                    context.startActivity(intent);
                }
            });
//            this.setIsRecyclable(false);
        }
    }
}