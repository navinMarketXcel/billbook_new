package com.billbook.app.networkcommunication;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class GetProductInfoResponseFromSDK implements Serializable {

    @SerializedName("code")
    private String code;

    @SerializedName("total")
    private String total;

    @SerializedName("offset")
    private String offset;

    @SerializedName("items")
    private ArrayList<ProductSDK> items;

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(final String total) {
        this.total = total;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(final String offset) {
        this.offset = offset;
    }

    public ArrayList<ProductSDK> getItems() {
        return items;
    }

    public void setItems(
            final ArrayList<ProductSDK> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "GetProductInfoResponseFromSDK{" +
                "code='" + code + '\'' +
                ", total='" + total + '\'' +
                ", offset='" + offset + '\'' +
                ", items=" + items +
                '}';
    }
}
