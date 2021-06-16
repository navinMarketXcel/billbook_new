package com.billbook.app.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.billbook.app.R;

import java.util.ArrayList;

public class ExtraInputAdapter extends RecyclerView.Adapter<ExtraInputAdapter.MyViewHolder> {

    private static final String TAG = "BrandAdapter";
    private ArrayList<String> extraInputArraylist;
    private Context context;

    public ExtraInputAdapter(Context context, ArrayList<String> brandArrayList) {
        this.context = context;
        this.extraInputArraylist = brandArrayList;
    }

    @Override
    public ExtraInputAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.extra_input_item_layout, parent, false);
        return new ExtraInputAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ExtraInputAdapter.MyViewHolder holder, int position) {
        final String input = extraInputArraylist.get(position);
        holder.tvCategoriesName.setText(input);
    }

    @Override
    public int getItemCount() {
        return extraInputArraylist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCategoriesName;

        public MyViewHolder(View view) {
            super(view);

            tvCategoriesName = (TextView) view.findViewById(R.id.tvCategoriesName);
        }
    }


}