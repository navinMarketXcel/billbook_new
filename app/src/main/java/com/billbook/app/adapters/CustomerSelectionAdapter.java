package com.billbook.app.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.Customer;

import java.util.ArrayList;

public class CustomerSelectionAdapter extends RecyclerView.Adapter<CustomerSelectionAdapter.MyViewHolder> {
    private ArrayList<Customer> customers = new ArrayList<>();
    private Context context;

    public CustomerSelectionAdapter(ArrayList<Customer> customers, Context context) {
        this.customers = customers;
        this.context = context;
    }

    @NonNull
    @Override
    public CustomerSelectionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categories_item_layout, parent, false);
        return new CustomerSelectionAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Customer customer = customers.get(position);
        holder.tvCategoriesName.setText(customer.getCustomerNameame() + "\n" + customer.getMobileNo());
        holder.selected.setVisibility(View.VISIBLE);
        boolean flag = customers.get(position).isSelected();
//        Log.v(TAG," status::"+flag);
        holder.selected.setChecked(customers.get(position).isSelected());
    }

    @Override
    public int getItemCount() {
        return customers.size();
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
                    customers.get(adapterPosition).setSelected(b);//=b;
                }
            });
//            this.setIsRecyclable(false);
        }
    }
}
