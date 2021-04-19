package com.billbook.app.utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.billbook.app.activities.MyApplication;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    public static void sendWhatsAppMessageasPDF(String number, Context context,  File file) {
        Intent sendIntent = new Intent("android.intent.action.MAIN");
        context.startActivity(Intent.createChooser(sendIntent, "Share Invoice By"));
        sendIntent.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
        sendIntent.putExtra("jid", "91"+number + "@s.whatsapp.net");
        sendIntent.putExtra("test", "test whatsapp.net");//phone number without "+" prefix
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setType("application/pdf");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "test");
        Uri uri =
                FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider",
                        file);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(sendIntent);



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
                    new SimpleDateFormat("dd MMM yyyy");
            dateToday = formatter.format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            Date date = Calendar.getInstance().getTime();
            // Display a date in day, month, year format
            @SuppressLint("SimpleDateFormat") DateFormat formatter =
                    new SimpleDateFormat("dd MMM yyyy");
            dateToday = formatter.format(date);

        }
        return dateToday;
    }

    public static String formatDecimalValue(float val){
        String pattern = "##,##,###.##";
        java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat(pattern);
        return decimalFormat.format(val);
    }

    public static void postEvents (String eventName, String value,Context context){
        Bundle params = new Bundle();
        params.putString(eventName.replaceAll(" ", "_"), value);
        MyApplication.getFirebaseAnalytics(context).logEvent(eventName.replaceAll(" ", "_"),params);
    }
}
