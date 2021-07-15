package com.billbook.app.adapters;

import android.annotation.SuppressLint;
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

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.billbook.app.R;
import com.billbook.app.activities.BillingAddProductActivity;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.database.models.Product;
import com.billbook.app.database.models.ProductAndInventory;
import com.billbook.app.repository.AppRepository;
import com.billbook.app.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class BillingProductListAdapter
        extends RecyclerView.Adapter<BillingProductListAdapter.MyViewHolder> implements Filterable {

    private static final String TAG = "BillingProdListAdapter";
    private ArrayList<ProductAndInventory> productArrayList;
    private ArrayList<ProductAndInventory> productAndInventoryListFiltered;
    private Context mContext;
    private BottomDialog bottomDialog;
    private View customView = null;
    private int userId;

    public BillingProductListAdapter(Context context,
                                     ArrayList<ProductAndInventory> productArrayList) {
        this.productArrayList = productArrayList;
        this.productAndInventoryListFiltered = productArrayList;
        this.mContext = context;
    }

    @Override
    public BillingProductListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.billing_productlist_item_layout, parent, false);
        return new BillingProductListAdapter.MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(BillingProductListAdapter.MyViewHolder holder, int position) {
        final ProductAndInventory product = productAndInventoryListFiltered.get(position);
        holder.tvProductName.setText(product.getProduct().getName());
        if (position == 0 && product.getProduct().getIsBestSeller() == true) {
            holder.header.setVisibility(View.VISIBLE);
            holder.header.setText("Top Selling");
        } else if (product.getProduct().getIsBestSeller() == false && (position > 0 &&
                productAndInventoryListFiltered.get(position - 1).getProduct().getIsBestSeller() == true)) {
            holder.header.setVisibility(View.VISIBLE);
            holder.header.setText("Other Selling");
        } else
            holder.header.setVisibility(View.GONE);

        if (product.getInventoryList() != null) {
            holder.tvProductCount.setText("" + product.getInventoryList().size());
        } else {
            holder.tvProductCount.setText("0");
        }

        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePurchase(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productAndInventoryListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    productAndInventoryListFiltered = productArrayList;
                } else {
                    List<ProductAndInventory> filteredList = new ArrayList<>();
                    for (ProductAndInventory productAndInventory : productArrayList) {

                        if (productAndInventory.getProduct()
                                .getName()
                                .toLowerCase()
                                .contains(charString.toLowerCase())) {
                            filteredList.add(productAndInventory);
                        }
                    }

                    productAndInventoryListFiltered = (ArrayList<ProductAndInventory>) filteredList;
                    Log.v(TAG, " performFiltering productAndInventoryListFiltered::"
                            + productAndInventoryListFiltered);
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = productAndInventoryListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                productAndInventoryListFiltered = (ArrayList<ProductAndInventory>) filterResults.values;
                Log.v(TAG,
                        " publishResults productAndInventoryListFiltered::" + productAndInventoryListFiltered);
                notifyDataSetChanged();
            }
        };
    }

    private void handlePurchase(final ProductAndInventory productAndInventory) {
        if (productAndInventory.getProduct().getName().equalsIgnoreCase("Other")) {
            showOtherProductDialog(mContext, productAndInventory.getProduct().getCategory(), productAndInventory.getProduct().getBrand());
        } else {
            mContext.startActivity(new Intent(mContext, BillingAddProductActivity.class)
                    .putExtra("productAndInventory", productAndInventory));
            ((Activity) mContext).finish();
        }
    }

    public void showOtherProductDialog(final Context context, final int catId, final int brandId) {
        final Dialog otherDialog = new Dialog(context);
        otherDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = otherDialog.getLayoutInflater().inflate(R.layout.dialog_other_layout, null);
        otherDialog.setContentView(view);
        otherDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        otherDialog.show();

        final EditText edtOther = view.findViewById(R.id.edtOther);
        final TextView tvForOther = view.findViewById(R.id.tvForOther);
        final Button btnSubmit = view.findViewById(R.id.btnSubmit);
        tvForOther.setText(R.string.please_enter_product);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtOther.getText().toString().trim().isEmpty()) {

                    Toast.makeText(context, context.getString(R.string.please_enter_product), Toast.LENGTH_SHORT).show();


                } else {
                    final Product product = new Product();
                    product.setName(edtOther.getText().toString().trim());
                    product.setIs_active(true);
                    product.setCategory(catId);
                    product.setBrand(brandId);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            int localid = (int) MyApplication.getDatabase().productDao().inserProduct(product);
                            Log.v(TAG, "inserted cate localid::" + localid);
                            product.setLocal_id(localid);
                            if (Util.isNetworkAvailable(context)) {
                                AppRepository.getInstance().postOtherProductAPI(product);
                            }

                        }
                    });

                    otherDialog.dismiss();
                    Toast.makeText(context, context.getString(R.string.product_added_successfuly), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvProductName, tvProductCount, header;
        private CardView card_view;

        public MyViewHolder(View view) {
            super(view);
            tvProductName = view.findViewById(R.id.tvProductName);
            tvProductCount = view.findViewById(R.id.tvProductCount);
            header = view.findViewById(R.id.titleOfBrand);
            card_view = view.findViewById(R.id.card_view);
        }
    }

}