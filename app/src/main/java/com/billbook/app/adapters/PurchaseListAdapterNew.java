package com.billbook.app.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.billbook.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PurchaseListAdapterNew extends RecyclerView.Adapter<PurchaseListAdapterNew.MyViewHolder> {

    private static final String TAG = "BrandAdapter";
    private JSONArray msterItems;
    private Context context;
    private TotalAmount totalAmount;
    public PurchaseListAdapterNew(Context context, JSONArray msterItems) {
        this.context = context;
        this.msterItems = msterItems;
        this.totalAmount= (TotalAmount) context;
    }

    @Override
    public PurchaseListAdapterNew.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cancel_inv_item, parent, false);
        return new PurchaseListAdapterNew.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PurchaseListAdapterNew.MyViewHolder holder, final int position) {
        final JSONObject item;
        try {
            item = msterItems.getJSONObject(position);
            holder.productName.setText("Item - "+ item.getString("name"));
            holder.productQty.setSelection(item.getInt("quantity"));
            holder.itemTotalAmt.setText("Price - "+item.getInt("price"));
            holder.productQty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int sposition, long id) {
                    try {
                        if (sposition > item.getInt("quantity") ) {
                            holder.productQty.setSelection(item.getInt("quantity"));
                        } else {
                            if (sposition == 0) {
                                item.put("quantity",0);
                                item.put("totalAmount",0);
                                item.put("gstAmount",0);
                                item.put("is_active",false);
                            } else {
                                item.put("quantity",sposition);
                                float totalAmount = item.getInt("price")*item.getInt("quantity");
                                float totalGSTAmount =totalAmount;
                                if(item.getInt("gst")>0)
                                totalGSTAmount = (totalAmount *100)/(100+item.getInt("gst"));
                                item.put("totalAmount",totalAmount);
                                item.put("gstAmount",totalGSTAmount);
                                item.put("is_active",true);
                            }

                            PurchaseListAdapterNew.this.totalAmount.calculateTotalAmount(position,item);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return msterItems.length();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView productName, itemTotalAmt;
        public Spinner productQty;

        public MyViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.productName);
            productQty = (Spinner) view.findViewById(R.id.productQty);
            itemTotalAmt = view.findViewById(R.id.itemTotalAmt);
        }
    }
    public interface  TotalAmount {
        public void calculateTotalAmount(int positon, JSONObject item);
    }

}