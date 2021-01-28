package com.billbook.app.networkcommunication;

import com.google.gson.annotations.SerializedName;
import com.billbook.app.database.models.User;

import java.util.ArrayList;

public class UserReponse {
    @SerializedName("count")
    private String count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private ArrayList<User> results;

    public ArrayList<User> getResults() {
        return results;
    }

    public void setResults(ArrayList<User> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "CategoryResponse{" +
                "count='" + count + '\'' +
                ", next='" + next + '\'' +
                ", previous='" + previous + '\'' +
                ", results=" + results +
                '}';
    }
}
