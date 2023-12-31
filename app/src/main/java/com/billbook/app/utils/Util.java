package com.billbook.app.utils;

import static android.content.Context.MODE_PRIVATE;

import static androidx.core.content.ContextCompat.startActivity;

import static com.billbook.app.activities.MyApplication.context;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.billbook.app.R;
import com.billbook.app.activities.BillingNewActivity;
import com.billbook.app.activities.BottomNavigationActivity;
import com.billbook.app.activities.LoginActivity;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.activities.PDFActivity;
import com.billbook.app.activities.loginPick_activity;
import com.billbook.app.database.models.Model;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Util {

    private static final String TAG = "Util";

    //Check the network Availability
    public static boolean isNetworkAvailable(Context contexts) {
        ConnectivityManager connectivityManager = (ConnectivityManager) contexts
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return true;
        else
            return false;
    }
    public static void dailyLogout(AppCompatActivity sourceActivity)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        String currentDate = sdf.format(c.getTime());
        System.out.println("currentDate"+currentDate);

        if(currentDate.equals(MyApplication.getScheduleDate())){
            MyApplication.setLogoutDaily(false);
        }else{
            MyApplication.setLogoutDaily(true);
        }

        int mHours = c.get(Calendar.HOUR_OF_DAY);
        System.out.println("currentDate mHours"+mHours);
        if(mHours >= 3 && MyApplication.getLogoutDaily()) {
            SharedPreferences sharedPref = sourceActivity.getApplicationContext().getSharedPreferences(sourceActivity.getString(R.string.preference_file_key), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();
            Intent intentObj = new Intent(sourceActivity, loginPick_activity.class);
            sourceActivity.startActivity(intentObj);
            sourceActivity.finish();
            MyApplication.saveScheduleLogOutDate(currentDate);
            clearAllTables(sourceActivity);
        }
    }

    public static void clearAllTables(Context context) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                MyApplication.getDatabase().clearAllTables();
            }
        };
        thread.start();
    }


    public static String getRandamNo() {
        double x = Math.random();
        String randamNo = String.valueOf(x);
        return randamNo;
    }

    public static float calculateGSTAmount(float percent, float amount) {

        float gstAmount = (percent / 100) * amount;
        System.out.println("gst amount = " + gstAmount);

        return gstAmount;
    }


    public static float calculateDiscountAmount(float percent, float amount) {

        float gstAmount = (percent / 100) * amount;
        System.out.println("gst amount = " + gstAmount);

        return gstAmount;
    }
    public static void pushEvent(String message)
    {
        MyApplication.cleverTapAPI.pushEvent(message);
    }

    public static float calculateDiscountPercentFromAmt(float discountAmt, float total){

        float discountPercent=0;
        if(total>0) {
            discountPercent = ((discountAmt / total) * 100);
        }
        return discountPercent;
    }

    public static float calculateDiscountAmtFromPercent(float discountPercent, float total){

        float discountAmt = 0;
        discountAmt = ((discountPercent*total)/100);
        return discountAmt;
    }

    public static String getTodaysdate() {
        //2018-09-24T11:13:23.744709Z
        String dateStr;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        date.setTime(date.getTime());
        dateStr = formatter.format(date);
        return dateStr;
    }

    public static String getToDateBack(int daysCount) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date date = new Date();
        String todate = dateFormat.format(date);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -daysCount);
        Date todate1 = cal.getTime();
        String fromdate = dateFormat.format(todate1);
        return fromdate;
    }


    public static String getStringDate(Date date) {
        String dateStr;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        dateStr = formatter.format(date);
        return dateStr;

    }

    public static int getDiffereneBetweenDates(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        try {
            Date dateStr = formatter.parse(date);
            return (int) TimeUnit.DAYS.convert((new Date().getTime() - dateStr.getTime()),
                    TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }


    public static long getCurrentLongDate() {
        //2018-09-24T11:13:23.744709Z
        long currentLongDate = 0;

        Date date = new Date();
        currentLongDate = date.getTime();
        return currentLongDate;
    }

    public static boolean isValidEmail(CharSequence inputStr) {
        String expression =
                "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static boolean isPhoneNumber(CharSequence inputStr) {

        String expression = "^[0-9]{10}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }
    public static RequestBody getRequestBodyFormData(String str) {
       return RequestBody.create(str, MediaType.parse("multipart/form-data"));
    }

    public static boolean passwordValidator(String password) {
        /*String PASSWORD_PATTERN = "^(?!.* ).$";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();*/
        if (password.length() > 5) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean passwordMatch(String password, String conformPassword) {
        return password.equals(conformPassword);
    }

    public static void sendWhatsAppMessage(String number, Context context, String message) {
//        Intent sendIntent = new Intent("android.intent.action.MAIN");

//        context.startActivity(Intent.createChooser(sendIntent, "Share Invoice By"));


//        sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
//        sendIntent.putExtra("jid", "91"+number + "@s.whatsapp.net");
//        sendIntent.putExtra("test", "test whatsapp.net");//phone number without "+" prefix
////        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////        sendIntent.setType("application/pdf");
//        sendIntent.putExtra(Intent.EXTRA_TEXT, "test");
////        Uri uri =
////                FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider",
////                        pdfFile);
////        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
//        context.startActivity(sendIntent);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + "91" + number + "&text=" + message + ""));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void sendWhatsAppMessageasPDF(String number, Context context, File file) {

        try {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            context.startActivity(Intent.createChooser(sendIntent, "Share Invoice By"));
            sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
            sendIntent.putExtra("jid", "91" + number + "@s.whatsapp.net");
            sendIntent.putExtra("test", "test whatsapp.net");//phone number without "+" prefix
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.setType("application/pdf");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "test");
            Uri uri =
                    FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider",
                            file);
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String floatToStringNoDecimal(float d) {
        DecimalFormat f = new DecimalFormat("#,##,##0.00");
        System.out.println(f.format(1234567));
        return f.format(1234567);
    }

    public static String getFormatedDate(String sDate1) {
        String dateToday;
        try {

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); //this format changeable
            dateFormatter.setTimeZone(TimeZone.getDefault());
            Date date = dateFormatter.parse(sDate1);
            @SuppressLint("SimpleDateFormat") DateFormat formatter =
                    new SimpleDateFormat("yyyy-MM-dd");
            dateToday = formatter.format(date.getTime());
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
    public static String formatDate(String string)
    {
        String outputDateStr="";
        try{
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
            String inputDateStr=string;
            Date date = inputFormat.parse(inputDateStr);
            outputDateStr = outputFormat.format(date);
        }
        catch (Exception e)
        {

        }
        return outputDateStr;
    }

    public static String formatDecimalValue(float val) {
        String pattern = "##,##,###.##";
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat(pattern);
        return decimalFormat.format(val);
    }

    public static void postEvents(String eventName, String value, Context context) {
        Bundle params = new Bundle();
        params.putString(eventName.replaceAll(" ", "_"), value);
        MyApplication.getFirebaseAnalytics(context).logEvent(eventName.replaceAll(" ", "_"), params);
    }

    public static boolean checkFileSizeInMB(File fileToCheck, Double upperLimit) {
        double sizeOfFile = fileToCheck.length() / (1024 * 1024) * 1.0;
        return sizeOfFile < upperLimit;
    }

    public static void logErrorApi(String api, JSONObject frontendPayload, String frontendError, String catchError, JsonObject backendResponse, Context context) {
        try {
            ApiInterface apiService =
                    ApiClient.getClient(context).create(ApiInterface.class);

            JSONObject requestObj = new JSONObject();

            requestObj.put("api", api);
            requestObj.put("frontendPayload", frontendPayload);
            requestObj.put("frontendError", frontendError);
            requestObj.put("catchError", catchError);
            requestObj.put("backendResponse", backendResponse);


            Call<Object> call = null;
            call = apiService.loggerAPI(requestObj);

            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP", body.toString());
                        if (body.getBoolean("status")) {
                            Log.i(TAG, "instance initializer: Success Error reported");
                        } else {
                            Log.i(TAG, "instance initializer: Fail Error reported");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Log.e(TAG, "onFailure : error reporting Fail");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMeasurementUnits(Context context){
        try{
            ApiInterface apiService =
                    ApiClient.getClient(context).create(ApiInterface.class);

            Map<String, String> map = new HashMap<>();
            Call<Object> call = apiService.measuremntUnit(map);

            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        if (body.getBoolean("status")) {
                            JSONObject data = body.getJSONObject("data");
                            JSONArray invoices = data.getJSONArray("invoices");
                            List<String> measurementUnits = new ArrayList<String>();
                            for(int i= 0;i<invoices.length();i++){
                                measurementUnits.add(invoices.getJSONObject(i).getString("measurementAbreviation"));
                            }
                            MyApplication.measurementUnits = measurementUnits;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    //DialogUtils.stopProgressDialog();
                    //DialogUtils.showToast(HomeActivity.this, "Failed update profile to server");
                }
            });

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void startHelpActivity(Context context){
        boolean installed = appInstallOrNot("com.whatsapp",context);
        String mobNo = "9289252155" ;
        if(installed)
        {
            Intent intent= new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+"+91"+mobNo));
            context.startActivity(intent);
        }
        else
        {
            Toast.makeText(context,"Please Install Whatsapp", Toast.LENGTH_SHORT).show();
        }

    }
    private static boolean appInstallOrNot(String url,Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        boolean app;
        try {

            packageManager.getPackageInfo(url,PackageManager.GET_ACTIVITIES);
            app=true;
        }
        catch(PackageManager.NameNotFoundException e)
        {
            app=false;
        }
        return app;

    }
    protected static boolean isAppInstalled(String packageName,Context context) {
        Intent mIntent = context.getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }

//    public static void startYoutubeActivity(Context context) {
//        String packageName = "com.google.android.youtube";
//        boolean isYoutubeInstalled = isAppInstalled(packageName,context);
//        if(isYoutubeInstalled)
//        {
//            Log.v("installed home", String.valueOf(isYoutubeInstalled));
//            Util.postEvents("Watch Demo", "Watch Demo", context.getApplicationContext());
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse("https://www.youtube.com/playlist?list=PLFuhsI7LfH3VFoH8oTfozpUlITI6fy7U8"));
//            intent.setPackage("com.google.android.youtube");
//            context.startActivity(intent);
//        }
//        else
//        {
//            Log.v("installed home", String.valueOf(isYoutubeInstalled));
//            Toast.makeText(context.getApplicationContext(), "Please Install Youtube", Toast.LENGTH_SHORT).show();
//        }
//
//    }

    public static void startYoutubeActivity(Context context){
        boolean installed = appInstallOrNot("com.google.android.youtube",context);
        Log.v("installed", String.valueOf(installed));
        if(!installed)
        {
            Util.postEvents("Watch Demo", "Watch Demo", context.getApplicationContext());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.youtube.com/playlist?list=PLFuhsI7LfH3VFoH8oTfozpUlITI6fy7U8"));
            intent.setPackage("com.google.android.youtube");
            context.startActivity(intent);
        }
        else
        {
            Toast.makeText(context.getApplicationContext(), "Please Install Youtube", Toast.LENGTH_SHORT).show();
        }
    }


}
