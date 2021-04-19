package com.billbook.app.database.models;

import androidx.room.ColumnInfo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Specification implements Serializable {


    @SerializedName("key")
    @ColumnInfo(name = "key")
    private String key;


//
//    @PrimaryKey(autoGenerate = true)
//    @SerializedName("id")
//    @ColumnInfo(name = "id")
//    private int id;
    @SerializedName("value")
    @ColumnInfo(name = "value")
    private String value;

    public Specification() {
    }
    //
    //@SerializedName("Segment")
    //@ColumnInfo(name = "Segment")
    //private String Segment;
    //
    //@SerializedName("SubCategory")
    //@ColumnInfo(name = "SubCategory")
    //private String SubCategory;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
}
