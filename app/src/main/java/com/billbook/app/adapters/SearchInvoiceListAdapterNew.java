package com.billbook.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.activities.BillingNewActivity;
import com.billbook.app.utils.OnDownloadClick;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.billbook.app.R;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchInvoiceListAdapterNew extends RecyclerView.Adapter<SearchInvoiceListAdapterNew.MyViewHolder> {

    private JSONArray requestInvoiceArrayList;
    private Context context;
    private SearchInvoiceItemClickListener invoiceItemClickListener;
    private Boolean ischeck;
    public SearchInvoiceListAdapterNew(Context context, JSONArray categoryArrayList, SearchInvoiceItemClickListener invoiceItemClickListener,Boolean ischeck) {
        this.context = context;
        this.ischeck = ischeck;
        this.requestInvoiceArrayList = categoryArrayList;
        this.invoiceItemClickListener = invoiceItemClickListener;
    }

    @Override
    public SearchInvoiceListAdapterNew.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.searchinvoice_item_layout, parent, false);
        return new SearchInvoiceListAdapterNew.MyViewHolder(itemView, invoiceItemClickListener);
    }

    public interface SearchInvoiceItemClickListener{
        void onSaveButtonClick(int invoicePosition);
    }
    @Override
    public void onBindViewHolder(SearchInvoiceListAdapterNew.MyViewHolder holder, final int position) {

        try {
            if(ischeck)
            {
                holder.checkbox.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.checkbox.setVisibility(View.GONE);
            }

            JSONObject requestInvoice = requestInvoiceArrayList.getJSONObject(position);
            boolean isGST = requestInvoice.getString("gstType").isEmpty()?false:true;
                    holder.tvInvoiceValue.setText(""+(isGST?requestInvoice.getInt("gstBillNo"):requestInvoice.getInt("nonGstBillNo") ));
            holder.tvInvoiceCustNameValue.setText(requestInvoice.getJSONObject("customer").getString("name"));
//            holder.tvQuantityValue.setText("" + requestInvoice.getInt("quantity"));
            if(requestInvoice.has("discount") && requestInvoice.has("totalAfterDiscount")&& requestInvoice.getDouble("discount")!=0 && requestInvoice.getDouble("totalAfterDiscount")!=0)
                holder.tvTotalAmtValue.setText(Util.formatDecimalValue((float)requestInvoice.getDouble("totalAfterDiscount")));
            else
                holder.tvTotalAmtValue.setText(Util.formatDecimalValue((float)requestInvoice.getDouble("totalAmount")));
//            String sDate1 = requestInvoice.getCreatedAt();
            String formatedDate = getFormatedDate(requestInvoice.getString("invoiceDate"));

            if(requestInvoice.has("download") && requestInvoice.getBoolean("download")){
                Log.v("INV ADPA", "position"+position + "download"+true);
                holder.download.setChecked(true);

            }else{
                //holder.download.setChecked(false);
                Log.v("INV ADPA", "position"+position + "download"+false);

            }
//            holder.download.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    CompoundButton btn = (CompoundButton) view ;
//                    Log.v("CHECK Changed",""+btn.isChecked()+"POS:"+position);
//                    try {
//                        requestInvoice.put("download",btn.isChecked());
//                        requestInvoiceArrayList.put(position,requestInvoice);
//                        ((OnDownloadClick) context).onclick(position,requestInvoice);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });



                holder.saveInv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Util.postEvents("Save","Save",context.getApplicationContext());

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        try {
                            if(requestInvoice.has("pdfLink") && requestInvoice.getString("pdfLink")!=null) {
                                String pdfLink = requestInvoice.getString("pdfLink");

                                if(pdfLink.contains("http://")){
                                    pdfLink = pdfLink.replace("http://", "https://");
                                }

                                i.setData(Uri.parse(pdfLink));
                                try{
                                    context.startActivity(i);
                                }catch(Exception e){
                                    DialogUtils.showToast(context, "Browser not installed");
                                    e.printStackTrace();
                                }
                            }
                            else{
                                Util.postEvents("Edit","Edit",context.getApplicationContext());
                                Intent intent = new Intent(context, BillingNewActivity.class);
                                intent.putExtra("edit",true);
                                intent.putExtra("invoice",requestInvoice.toString());
                                context.startActivity(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            if (requestInvoice.getBoolean("is_active")) {
                holder.cancelInvBItn.setVisibility(View.VISIBLE);
                Util.postEvents("Cancel","Cancel",context.getApplicationContext());

                holder.cancelInvBItn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DialogUtils.showAlertDialog((Activity) context, "Yes", "No", "Confirm if you want to cancel this bill", new DialogUtils.DialogClickListener() {
                            @Override
                            public void positiveButtonClick() {
                                if (Util.isNetworkAvailable(context)) {
                                    final ProgressDialog progressDialog = DialogUtils.startProgressDialog(context, "");
                                    ApiInterface apiService =
                                            ApiClient.getClient(context).create(ApiInterface.class);

                                    String token = MyApplication.getUserToken();

                                    Map<String, String> headerMap = new HashMap<>();
                                    headerMap.put("Authorization", token);
                                    Call<Object> call = null;
                                    try {
                                        JSONObject inv = requestInvoiceArrayList.getJSONObject(position);
                                        inv.remove("masterItems");
                                        inv.put("is_active", false);
                                        JsonObject jsonObject = new JsonParser().parse(inv.toString()).getAsJsonObject();
                                        call = apiService.updateInvoice(headerMap, inv.getInt("id"), jsonObject);
                                        call.enqueue(new Callback<Object>() {
                                            @Override
                                            public void onResponse(Call<Object> call, Response<Object> response) {
                                                final JSONObject body;
                                                try {
                                                    body = new JSONObject(new Gson().toJson(response.body()));

                                                    if (body.getBoolean("status")) {
                                                        requestInvoice.put("is_active",false);
                                                        notifyItemChanged(position);
                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                progressDialog.dismiss();


                                            }

                                            @Override
                                            public void onFailure(Call<Object> call, Throwable t) {
                                                progressDialog.dismiss();
                                            }
                                        });
                                    } catch (JSONException e) {
                                        progressDialog.dismiss();
                                        e.printStackTrace();
                                    }

                                } else {
                                    Toast.makeText(context, context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void negativeButtonClick() {

                            }
                        });


                    }
                });
                //holder.edit.setVisibility(View.VISIBLE);
                //holder.cancelledBill.setVisibility(View.GONE);
//                holder.edit.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Util.postEvents("Edit","Edit",context.getApplicationContext());
//                        Intent intent = new Intent(context, BillingNewActivity.class);
//                        intent.putExtra("edit",true);
//                        intent.putExtra("invoice",requestInvoice.toString());
//                        context.startActivity(intent);
//                    }
//                });
            }else{
                holder.cancelInvBItn.setVisibility(View.GONE);
                //holder.edit.setVisibility(View.GONE);
                holder.cancelledBill.setVisibility(View.VISIBLE);

            }
            holder.tvInvoiceDateValue.setText(formatedDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (requestInvoiceArrayList == null) {
            return 0;
        }
        return requestInvoiceArrayList.length();
    }

    public String getFormatedDate(String sDate1) {
        String dateToday;
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            Date date = dateFormatter.parse(sDate1);

            @SuppressLint("SimpleDateFormat") DateFormat formatter =
                    new SimpleDateFormat("yyyy-MM-dd");
//            dateToday = formatter.format(date.getTime()+TimeZone.getDefault().getRawOffset());
            dateToday = formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            Date date = Calendar.getInstance().getTime();
            // Display a date in day, month, year format
            @SuppressLint("SimpleDateFormat") DateFormat formatter =
                    new SimpleDateFormat("yyyy-MM-dd");
            dateToday = formatter.format(date);
        }
        return dateToday;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvInvoiceValue, tvInvoiceCustNameValue, tvQuantityValue, tvTotalAmtValue, tvInvoiceDateValue;
        public Button saveInv,cancelInvBItn,cancelledBill,edit;
        private CheckBox download;
        private CheckBox checkbox;
        private CardView card_view;
        private LinearLayout llMain;
        private SearchInvoiceItemClickListener searchInvoiceItemClickListener;

        public MyViewHolder(View view, SearchInvoiceItemClickListener searchInvoiceItemClickListener) {
            super(view);
            tvInvoiceValue = view.findViewById(R.id.tvInvoiceValue);
            tvInvoiceCustNameValue = view.findViewById(R.id.tvInvoiceCustNameValue);
            tvTotalAmtValue = view.findViewById(R.id.tvTotalAmtValue);
            llMain = view.findViewById(R.id.llMain);
            tvInvoiceDateValue = view.findViewById(R.id.tvInvoiceDateValue);
            saveInv = view.findViewById(R.id.saveInv);
            cancelInvBItn = view.findViewById(R.id.cancelInvBItn);
            checkbox = view.findViewById(R.id.checkbox);
            this.searchInvoiceItemClickListener = searchInvoiceItemClickListener;
            //download =view.findViewById(R.id.download);
            saveInv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            searchInvoiceItemClickListener.onSaveButtonClick(getAdapterPosition());
        }
    }

}



