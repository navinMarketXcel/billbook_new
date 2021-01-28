package com.billbook.app.networkcommunication;

import com.google.gson.annotations.SerializedName;
import com.billbook.app.database.models.Invoice;

import java.util.ArrayList;

public class InvoiceApiResponse {
    @SerializedName("count")
    private String count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private ArrayList<Invoice> results;

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public ArrayList<Invoice> getResults() {
        return results;
    }

    public void setResults(ArrayList<Invoice> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "InvoiceApiResponse{" +
                "count='" + count + '\'' +
                ", next='" + next + '\'' +
                ", previous='" + previous + '\'' +
                ", results=" + results +
                '}';
    }
}
