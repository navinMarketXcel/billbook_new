package com.billbook.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.activities.CancelInvoiceActivity;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.activities.SendPDFActivity;
import com.billbook.app.database.models.Invoice;
import com.billbook.app.database.models.PrintInvoice;
import com.billbook.app.database.models.Purchase;
import com.billbook.app.database.models.RequestInvoice;
import com.billbook.app.utils.Util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SearchInvoiceListAdapter extends RecyclerView.Adapter<SearchInvoiceListAdapter.MyViewHolder> {

    private ArrayList<RequestInvoice> requestInvoiceArrayList;
    private Context context;

    public SearchInvoiceListAdapter(Context context, ArrayList<RequestInvoice> categoryArrayList) {
        this.context = context;
        this.requestInvoiceArrayList = categoryArrayList;
    }

    @Override
    public SearchInvoiceListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.searchinvoice_item_layout, parent, false);
        return new SearchInvoiceListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchInvoiceListAdapter.MyViewHolder holder, int position) {
        final RequestInvoice requestInvoice = requestInvoiceArrayList.get(position);
        if (requestInvoice != null) {
            holder.tvInvoiceValue.setText("INV-" + Integer.toString(requestInvoice.getInvoice_no()));
            holder.tvInvoiceCustNameValue.setText(requestInvoice.getCustomer_name());
            holder.tvQuantityValue.setText("" + (requestInvoice.getPurchase() == null ? "" : requestInvoice.getPurchase().size()));
            holder.tvTotalAmtValue.setText("" + requestInvoice.getTotal_amount());
            String sDate1 = requestInvoice.getCreatedAt();
            String formatedDate = getFormatedDate(sDate1);
            holder.tvInvoiceDateValue.setText(formatedDate);
            holder.cancelInv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrintInvoice printInvoice = generateInvoiceObj(requestInvoice);
                    Intent intentSendPDF = new Intent(context, CancelInvoiceActivity.class);
                    intentSendPDF.putExtra("mPrintInvoiceObj", printInvoice);
                    intentSendPDF.putExtra("isFormSearchBill", true);
                    context.startActivity(intentSendPDF);
                }
            });
            holder.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Invoice invoice = new Invoice();
                    invoice.setId(requestInvoice.getId());
                    invoice.setPaymentMethod1(requestInvoice.getPaymentMethod1());
                    invoice.setPaymentMethod2(requestInvoice.getPaymentMethod2());
                    invoice.setPaymentMethod1Amount(requestInvoice.getPaymentMethod1Amount());
                    invoice.setPaymentMethod2Amount(requestInvoice.getPaymentMethod2Amount());

                    invoice.setInvoice_no(requestInvoice.getInvoice_no());
                    invoice.setInvoiceName(requestInvoice.getCustomer_name());
                    invoice.setInvoiceAddress(requestInvoice.getCustomerAddress());
                    invoice.setInvoiceCity(requestInvoice.getCity());
                    invoice.setInvoiceState(requestInvoice.getInvoiceState());
                    invoice.setInvoiceMobileNo(requestInvoice.getCustomer_mobile_no());
                    String sDate1 = requestInvoice.getCreatedAt();
                    String formatedDate = getFormatedDate(sDate1);
                    invoice.setInvoiceDate(formatedDate);
                    invoice.setUser(MyApplication.getUserID());
                    PrintInvoice mPrintInvoiceObj = generateInvoiceObj(requestInvoice);

                    Intent intentSendPDF = new Intent(context, SendPDFActivity.class);
                    intentSendPDF.putExtra("mPrintInvoiceObj", mPrintInvoiceObj);
                    intentSendPDF.putExtra("isFormSearchBill", true);
                    intentSendPDF.putExtra("showGST", requestInvoice.getGst_type().equals("1") ? true : false);

                    context.startActivity(intentSendPDF);
//                 ((SearchInvoiceActivity)context).finish();
                }
            });


        }
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
                    new SimpleDateFormat("EEE, dd MMM yyyy");
            dateToday = formatter.format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            Date date = Calendar.getInstance().getTime();
            // Display a date in day, month, year format
            @SuppressLint("SimpleDateFormat") DateFormat formatter =
                    new SimpleDateFormat("EEE, dd MMM yyyy");
            dateToday = formatter.format(date);

        }
        return dateToday;
    }

    private PrintInvoice generateInvoiceObj(RequestInvoice requestInvoice) {
        Invoice invoice = new Invoice();
        invoice.setId(requestInvoice.getId());
        invoice.setPaymentMethod1(requestInvoice.getPaymentMethod1());
        invoice.setPaymentMethod2(requestInvoice.getPaymentMethod2());
        invoice.setPaymentMethod1Amount(requestInvoice.getPaymentMethod1Amount());
        invoice.setPaymentMethod2Amount(requestInvoice.getPaymentMethod2Amount());
        invoice.setInvoice_no(requestInvoice.getInvoice_no());
        invoice.setInvoiceName(requestInvoice.getCustomer_name());
        invoice.setInvoiceEmail(requestInvoice.getCustomer_email());
        invoice.setInvoiceAddress(requestInvoice.getCustomerAddress());
        invoice.setInvoiceCity(requestInvoice.getCity());
        invoice.setInvoiceState(requestInvoice.getInvoiceState());
        invoice.setInvoiceMobileNo(requestInvoice.getCustomer_mobile_no());
        String sDate1 = requestInvoice.getCreatedAt();
        String formatedDate = getFormatedDate(sDate1);
        invoice.setInvoiceDate(formatedDate);
        invoice.setUser(MyApplication.getUserID());
        PrintInvoice mPrintInvoiceObj = new PrintInvoice();
        mPrintInvoiceObj.setPurchaseArrayList((ArrayList<Purchase>) requestInvoice.getPurchase());
        mPrintInvoiceObj.setInvoice(invoice);
        float totalSum = 0;
        float gstAmt = 0;

        for (Purchase purchase : mPrintInvoiceObj.getPurchaseArrayList()) {
            totalSum = totalSum + (purchase.getPrice() * purchase.getQuantity());
            gstAmt = gstAmt + purchase.getTaxAmount();
        }

        mPrintInvoiceObj.setAmountBeforeTax(Util.formatDecimalValue( totalSum - gstAmt - requestInvoice.getDiscountAmount()));
//        gstAmt=((requestInvoice.getGstPercentage()/100)*totalSum);
        mPrintInvoiceObj.setGstAmount(Util.formatDecimalValue( gstAmt));
        mPrintInvoiceObj.setDiscountAmount(Util.formatDecimalValue( (float) requestInvoice.getDiscountAmount()));
        mPrintInvoiceObj.setTotal(Util.formatDecimalValue( requestInvoice.getTotal_amount()));
        mPrintInvoiceObj.setDiscountEnabled(true);
        mPrintInvoiceObj.setGSTEnabled(true);
        ArrayList<String> test = new ArrayList<>();
        for (Purchase p : requestInvoice.getPurchase()) {
            if (p.getSpecification() != null) {
                test.add(p.getSpecification().getValue());
            }
        }
        mPrintInvoiceObj.setStringArrayList(test);
        mPrintInvoiceObj.setDescription("" + "");
        mPrintInvoiceObj.setPdfFile(requestInvoice.getPdf());
        mPrintInvoiceObj.setStrInvoiceNo(requestInvoice.getInvoice_no());
        mPrintInvoiceObj.setDescription(requestInvoice.getDescription());
        return mPrintInvoiceObj;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvInvoiceValue, tvInvoiceCustNameValue, tvQuantityValue, tvTotalAmtValue, tvInvoiceDateValue;
        public Button cancelInv;
        private CardView card_view;
        private LinearLayout llMain;


        public MyViewHolder(View view) {
            super(view);
            tvInvoiceValue = view.findViewById(R.id.tvInvoiceValue);
            tvInvoiceCustNameValue = view.findViewById(R.id.tvInvoiceCustNameValue);
//            tvQuantityValue = view.findViewById(R.id.tvQuantityValue);
            tvTotalAmtValue = view.findViewById(R.id.tvTotalAmtValue);
            llMain = view.findViewById(R.id.llMain);
            tvInvoiceDateValue = view.findViewById(R.id.tvInvoiceDateValue);
            cancelInv = view.findViewById(R.id.cancelInvBItn);
        }
    }
}
