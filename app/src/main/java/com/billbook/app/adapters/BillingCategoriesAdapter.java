package com.billbook.app.adapters;

import android.app.Activity;
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
import com.billbook.app.activities.BillingBrandActivity;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.Category;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class BillingCategoriesAdapter extends RecyclerView.Adapter<BillingCategoriesAdapter.MyViewHolder> implements
        Filterable {

    private static final String TAG = "BilliADP";
    private ArrayList<Category> categoryArrayList;
    private ArrayList<Category> categoryArrayListFiltered;
    private Context context;

    public BillingCategoriesAdapter(Context context, ArrayList<Category> categoryArrayList) {
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
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Category category = categoryArrayListFiltered.get(position);
        holder.tvCategoriesName.setText(category.getName());
        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (category.getName().equalsIgnoreCase("Other")) {
                    showOtherCategoryDialog(context);
                } else {
                    Intent intent = new Intent(context, BillingBrandActivity.class);
                    intent.putExtra("CategoryID", category.getId());
                    intent.putExtra("CategoryName", category.getName());
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    categoryArrayListFiltered = categoryArrayList;
                } else {
                    List<Category> filteredList = new ArrayList<>();
                    for (Category category : categoryArrayList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (category.getName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(category);
                        }
                    }

                    categoryArrayListFiltered = (ArrayList<Category>) filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = categoryArrayListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                categoryArrayListFiltered = (ArrayList<Category>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void showOtherCategoryDialog(final Context context) {
        final Dialog otherDialog = new Dialog(context);
        otherDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = otherDialog.getLayoutInflater().inflate(R.layout.dialog_other_layout, null);
        otherDialog.setContentView(view);
        otherDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        otherDialog.show();

        final EditText edtOther = view.findViewById(R.id.edtOther);
        final TextView tvForOther = view.findViewById(R.id.tvForOther);
        final Button btnSubmit = view.findViewById(R.id.btnSubmit);
        tvForOther.setText(R.string.please_enter_category);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtOther.getText().toString().trim().isEmpty()) {

                    Toast.makeText(context, context.getString(R.string.please_enter_category), Toast.LENGTH_SHORT).show();
                } else {
                    final Category category = new Category();
                    category.setName(edtOther.getText().toString().trim());
                    category.setIs_active(true);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            if (Util.isNetworkAvailable(context)) {
                                AppRepository.getInstance().postOtherCategoryAPI(category,context);
                            } else {
                                int localid = (int) MyApplication.getDatabase().categoriesDao().insertcategory(category);
                                Log.v(TAG, "inserted cate localid::" + localid);
                                category.setLocal_id(localid);
                            }
                        }
                    });

                    otherDialog.dismiss();
                    Toast.makeText(context, context.getString(R.string.category_added_successfuly), Toast.LENGTH_SHORT).show();

                }
            }
        });

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