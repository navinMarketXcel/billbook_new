
package com.billbook.app.model.appmetadata;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class UserData {

    @SerializedName("Demo")
    @Expose
    private String demo;
    @SerializedName("userid")
    @Expose
    private String userid;

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

}
