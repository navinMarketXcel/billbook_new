package com.billbook.app.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.Product;

import java.util.ArrayList;

public class SearchProductItemAdapter extends ArrayAdapter<Product> {

    private static final String TAG = "SearchProductItemAdapte";
    private ArrayList<Product> productArrayList;
    private Context context;

    public SearchProductItemAdapter(Context context, ArrayList<Product> productArrayList) {
        super(context, R.layout.search_product_item_layout, productArrayList);
        this.productArrayList = productArrayList;
        this.context = context;
        Log.v(TAG, "inside SearchProductItemAdapter");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MyViewHolder viewHolder;
        Log.v(TAG, "inside getView");
        if (convertView == null) {

            viewHolder = new MyViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.search_product_item_layout, parent, false);
            viewHolder.tvProductName = convertView.findViewById(R.id.tvProductName);
            viewHolder.tvBrand = convertView.findViewById(R.id.tvBrand);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (MyViewHolder) convertView.getTag();

        }

        Product product = productArrayList.get(position);
        viewHolder.tvProductName.setText(product.getName());
        viewHolder.tvBrand.setText("" + product.getBrand());
        viewHolder.tvCategory.setText("" + product.getCategory());

        return convertView;
    }

    public class MyViewHolder {
        public TextView tvProductName, tvBrand, tvCategory;


    }


}