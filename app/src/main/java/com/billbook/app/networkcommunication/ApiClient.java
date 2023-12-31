package com.billbook.app.networkcommunication;

import android.content.Context;
import android.util.Log;

import com.billbook.app.BuildConfig;
import com.billbook.app.activities.MyApplication;
import com.readystatesoftware.chuck.ChuckInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

//    public static final String BASE_URL = "http://192.168.1.102:3033/v1/";
//    public static final String BASE_URL = "https://api.thebillbook.com/v1/";
//    public static final String BASE_URL = "https://devapi.thebillbook.com/v1/";
//    public static final String BASE_URL = "https://6a853f947a99.ngrok.io/v1/";
    public static final String BASE_URL = BuildConfig.BASE_URL;

    private static Retrofit retrofit = null;


    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder client = new OkHttpClient.Builder();

            client.addInterceptor(chain -> {
                Request request = chain.request().newBuilder().addHeader("x-auth-token", MyApplication.getUserToken()).build();
                return chain.proceed(request);
            });

           // client.addInterceptor(new ChuckInterceptor(context));
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.addInterceptor(loggingInterceptor);
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
//                    .client(getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client.writeTimeout(30000, TimeUnit.MILLISECONDS).readTimeout(30000,TimeUnit.MILLISECONDS).build())
                    .build();
        }
        return retrofit;
    }

}
