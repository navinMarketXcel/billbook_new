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

import com.billbook.app.activities.SearchInvoiceActivity;

import com.billbook.app.adapter_bill_callback.BillCallback;

import com.billbook.app.database.models.Invoice;

import com.billbook.app.model.InvoicesData;

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

import java.util.ArrayList;

import java.util.Calendar;

import java.util.Date;

import java.util.HashMap;

import java.util.Map;

import java.util.TimeZone;


import retrofit2.Call;

import retrofit2.Callback;

import retrofit2.Response;


public class SearchInvoiceListAdapterNew extends RecyclerView.Adapter<SearchInvoiceListAdapterNew.MyViewHolder> {


    private ArrayList<InvoicesData> requestInvoiceArrayList;

    private Context context;

    private SearchInvoiceItemClickListener invoiceItemClickListener;

    private Boolean ischeck;
    private Boolean checkSelect;

    public BillCallback billCallback;

    public SearchInvoiceListAdapterNew(Context context, ArrayList<InvoicesData> categoryArrayList, SearchInvoiceItemClickListener invoiceItemClickListener, Boolean ischeck,BillCallback billCallback,boolean checkSelect) {

        this.context = context;

        this.ischeck = ischeck;

        this.requestInvoiceArrayList = categoryArrayList;

        this.billCallback = billCallback;

        this.invoiceItemClickListener = invoiceItemClickListener;
        this.checkSelect = checkSelect;
        Log.v("checkSelect", String.valueOf(checkSelect));

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

    public void onBindViewHolder(SearchInvoiceListAdapterNew.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        InvoicesData data=requestInvoiceArrayList.get(position);

        if(ischeck)

        {

            holder.download.setVisibility(View.VISIBLE);

        }

        else

        {

            holder.download.setVisibility(View.GONE);

        }
        if(checkSelect)

        {


            holder.buttonLay.setVisibility(View.VISIBLE);


        }

        else

        {

            holder.buttonLay.setVisibility(View.GONE);



        }




        try {

            holder.tvInvoiceCustNameValue.setText(data.getCustomer().getCustomerNameame());

            if(data.getDiscount()!=0 && data.getTotalAfterDiscount()!=0)

                holder.tvTotalAmtValue.setText("₹"+Util.formatDecimalValue((float)data.getTotalAfterDiscount().floatValue()));

            else

                holder.tvTotalAmtValue.setText("₹"+Util.formatDecimalValue((float)data.getTotalAmount().floatValue()));

        } catch (Exception e) {

            e.printStackTrace();

        }


        // holder.tvTotalAmtValue.setText(Util.formatDecimalValue((float)data.getTotalAfterDiscount().floatValue()));

        Log.v("formatted date",data.getInvoiceDate());

        String formatedDate = getFormatedDate(data.getInvoiceDate());

        Log.v("formatted date",formatedDate);



        boolean isGST = data.getGstType().isEmpty()?false:true;

        holder.tvInvoiceValue.setText(""+(isGST?data.getGstBillNo():data.getNonGstBillNo() ));

/*

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



            }*/



        holder.download.setChecked(data.isSelected());

        holder.download.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                if(holder.download.isChecked())

                {

                    billCallback.callback("Selected",data,position);

                }

                else

                {

                    billCallback.callback("unSelected",data,position);

                }

            }

        });




//        holder.saveInv.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//
//            public void onClick(View v) {
//
//                Util.postEvents("Save","Save",context.getApplicationContext());
//                Util.pushEvent("Clicked on Save Invoice in Search Invoices");
//
//
//                Intent i = new Intent(Intent.ACTION_VIEW);
//
//                if( data.getPdfLink()!=null && !data.getPdfLink().isEmpty()) {
//
//                    String pdfLink = data.getPdfLink();
//
//
//                    if(pdfLink.contains("http://")){
//
//                        pdfLink = pdfLink.replace("http://", "https://");
//
//                    }
//
//
//                    i.setData(Uri.parse(pdfLink));
//
//                    try{
//
//                        context.startActivity(i);
//
//                    }catch(Exception e){
//
//                        DialogUtils.showToast(context, "Browser not installed");
//
//                        e.printStackTrace();
//
//                    }
//
//                }
//
//                else{
//
////                                Util.postEvents("Edit","Edit",context.getApplicationContext());
//
////                                Intent intent = new Intent(context, BillingNewActivity.class);
//
////                                intent.putExtra("edit",true);
//
////                                intent.putExtra("invoice",requestInvoice.toString());
//
////                                context.startActivity(intent);
//
//                }
//
//
//            }
//
//        });


        if (data.getIsActive()) {
            if(checkSelect)
            {
                holder.cancelInvBItn.setVisibility(View.VISIBLE);
            }
            else {

                holder.cancelInvBItn.setVisibility(View.GONE);
            }



            Util.postEvents("Cancel","Cancel",context.getApplicationContext());


            holder.cancelInvBItn.setOnClickListener(new View.OnClickListener() {

                @Override

                public void onClick(View v) {
                    Util.pushEvent("Clicked on Delete Invoice in Search Invoices");

                    billCallback.callback("delete",data,position);


                }

            });

            holder.searchItem.setOnClickListener(new View.OnClickListener() {

                @Override

                public void onClick(View view) {

                    billCallback.callback("edit",requestInvoiceArrayList.get(position),position);

                }

            });
            holder.itemLyaout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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

            //holder.edit.setVisibility(View.GONE);



        }

        holder.tvInvoiceDateValue.setText(Util.formatDate(formatedDate));

    }


    @Override

    public int getItemCount() {

        if (requestInvoiceArrayList == null) {

            return 0;

        }

        return requestInvoiceArrayList.size();

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

        private CardView card_view;

        private LinearLayout llMain,itemLyaout,buttonLay;
        private Button searchItem;

        private SearchInvoiceItemClickListener searchInvoiceItemClickListener;



        public MyViewHolder(View view, SearchInvoiceItemClickListener searchInvoiceItemClickListener) {

            super(view);

            tvInvoiceValue = view.findViewById(R.id.tvInvoiceValue);
            buttonLay = view.findViewById(R.id.buttonLay);

            tvInvoiceCustNameValue = view.findViewById(R.id.tvInvoiceCustNameValue);

            tvTotalAmtValue = view.findViewById(R.id.tvTotalAmtValue);

            llMain = view.findViewById(R.id.llMain);

            tvInvoiceDateValue = view.findViewById(R.id.tvInvoiceDateValue);

            saveInv = view.findViewById(R.id.saveInv);

            cancelInvBItn = view.findViewById(R.id.cancelInvBItn);

            searchItem = view.findViewById(R.id.searchItem);
            itemLyaout = view.findViewById(R.id.itemLayout);



            this.searchInvoiceItemClickListener = searchInvoiceItemClickListener;

            download =view.findViewById(R.id.download);

            //saveInv.setOnClickListener(this);

        }


        @Override

        public void onClick(View v) {

            searchInvoiceItemClickListener.onSaveButtonClick(getAdapterPosition());

        }

    }


}
