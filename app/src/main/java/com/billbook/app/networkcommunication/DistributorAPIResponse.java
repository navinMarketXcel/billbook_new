package com.billbook.app.networkcommunication;

import com.google.gson.annotations.SerializedName;
import com.billbook.app.database.models.Distributor;

import java.util.ArrayList;

public class DistributorAPIResponse {
    @SerializedName("count")
    private String count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private ArrayList<Distributor> results;

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

    public ArrayList<Distributor> getResults() {
        return results;
    }

    public void setResults(ArrayList<Distributor> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "InventoryAPIResponse{" +
                "count='" + count + '\'' +
                ", next='" + next + '\'' +
                ", previous='" + previous + '\'' +
                ", results=" + results +
                '}';
    }
}