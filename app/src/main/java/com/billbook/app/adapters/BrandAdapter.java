package com.billbook.app.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
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
import com.billbook.app.activities.MyApplication;
import com.billbook.app.activities.ProductListActivity;
import com.billbook.app.database.models.Brand;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.MyViewHolder> implements Filterable {

    private static final String TAG = "BrandAdapter";
    private ArrayList<Brand> brandArrayList;
    private ArrayList<Brand> brandArrayListFiltered;
    private Context context;

    public BrandAdapter(Context context, ArrayList<Brand> brandArrayList) {
        this.context = context;
        this.brandArrayList = brandArrayList;
        this.brandArrayListFiltered = brandArrayList;
    }

    public static void showOtherBrandDialog(final Context context, final int categoriId) {
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
                                AppRepository.getInstance().postOtherBrandAPI(brand,context);
                            }
                        }
                    });

                    otherDialog.dismiss();
                    Toast.makeText(context, context.getString(R.string.brand_added_successfuly), Toast.LENGTH_SHORT).show();


                }
            }
        });

    }

    @Override
    public BrandAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categories_item_layout, parent, false);
        return new BrandAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BrandAdapter.MyViewHolder holder, int position) {
        final Brand brand = brandArrayListFiltered.get(position);
        holder.tvCategoriesName.setText(brand.getName());
        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.tvCategoriesName.getText().toString().equalsIgnoreCase(context.getString(R.string.other))) {
                    showOtherBrandDialog(context, brand.getCategory());
                } else {
                    Intent intent = new Intent(context, ProductListActivity.class);
                    intent.putExtra("CategoryID", brand.getCategory());
                    intent.putExtra("BrandID", brand.getId());
                    context.startActivity(intent);
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

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCategoriesName;
        private CardView card_view;

        public MyViewHolder(View view) {
            super(view);
            card_view = view.findViewById(R.id.card_view);
            tvCategoriesName = (TextView) view.findViewById(R.id.tvCategoriesName);
        }
    }

}