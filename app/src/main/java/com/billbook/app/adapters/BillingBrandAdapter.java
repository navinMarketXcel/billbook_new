package com.billbook.app.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.activities.BillingProductListActivity;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.Brand;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class BillingBrandAdapter extends RecyclerView.Adapter<BillingBrandAdapter.MyViewHolder> implements
        Filterable {

    private static final String TAG = "BillingBrandAdapter";
    private ArrayList<Brand> brandArrayList;
    private ArrayList<Brand> brandArrayListFiltered;
    private Context context;
    private boolean isHeaderShown = false;

    public BillingBrandAdapter(Context context, ArrayList<Brand> brandArrayList) {
        this.context = context;
        this.brandArrayList = brandArrayList;
        this.brandArrayListFiltered = brandArrayList;
    }

    @Override
    public BillingBrandAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categories_item_layout, parent, false);
        return new BillingBrandAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BillingBrandAdapter.MyViewHolder holder, int position) {
        final Brand brand = brandArrayListFiltered.get(position);
        holder.tvCategoriesName.setText(brand.getName());
        if (position == 0 && brand.getIsBestSeller() == true) {
            holder.header.setVisibility(View.VISIBLE);
            holder.header.setText("Top Selling");
        } else if (brand.getIsBestSeller() == false && (position > 0 &&
                brandArrayListFiltered.get(position - 1).getIsBestSeller() == true)) {
            holder.header.setVisibility(View.VISIBLE);
            holder.header.setText("Other Selling");
            isHeaderShown = true;
        } else
            holder.header.setVisibility(View.GONE);


        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (brand.getName().equalsIgnoreCase("Other")) {
                    showOtherBrandDialog(context, brand.getCategory());
                } else {
                    Intent intent = new Intent(context, BillingProductListActivity.class);
                    intent.putExtra("CategoryID", brand.getCategory());
                    intent.putExtra("BrandID", brand.getId());
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return brandArrayListFiltered.size();
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
                    isHeaderShown = false;
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

    public void showOtherBrandDialog(final Context context, final int categoriId) {
        final Dialog otherDialog = new Dialog(context);
        otherDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = otherDialog.getLayoutInflater().inflate(R.layout.dialog_other_layout, null);
        otherDialog.setContentView(view);
        otherDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        otherDialog.show();

        final EditText edtOther = view.findViewById(R.id.edtOther);
        final TextView tvForOther = view.findViewById(R.id.tvForOther);
        final Button btnSubmit = view.findViewById(R.id.btnSubmit);
        tvForOther.setText(R.string.please_enter_brand);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtOther.getText().toString().trim().isEmpty()) {

                    Toast.makeText(context, context.getString(R.string.please_enter_brand), Toast.LENGTH_SHORT).show();
                } else {

                    final Brand brand = new Brand();
                    brand.setName(edtOther.getText().toString().trim());
                    brand.setCategory(categoriId);
                    brand.setIs_active(true);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            int local_brand_id = (int) MyApplication.getDatabase().brandDao().inserBrand(brand);
                            Log.v(TAG, "local_brand_id::" + local_brand_id);
                            brand.setLocal_id(local_brand_id);
                            if (Util.isNetworkAvailable(context)) {
//                                AppRepository.getInstance().postOtherBrandAPI(brand);
                            }
                        }
                    });
                    otherDialog.dismiss();
                    Toast.makeText(context, context.getString(R.string.brand_added_successfuly), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCategoriesName, header;
        private CardView card_view;

        public MyViewHolder(View view) {
            super(view);
            card_view = view.findViewById(R.id.card_view);
            header = view.findViewById(R.id.titleOfBrand);
            tvCategoriesName = (TextView) view.findViewById(R.id.tvCategoriesName);
        }
    }

}