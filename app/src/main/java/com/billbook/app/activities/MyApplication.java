package com.billbook.app.activities;

import android.app.Application;

import androidx.multidex.MultiDex;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.room.Room;
import androidx.room.migration.Migration;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.billbook.app.R;
import com.billbook.app.database.AppDatabase;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Constants;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplication extends Application {
    public static CleverTapAPI cleverTapAPI;
    static final Migration MIGRATION_2_3 = new Migration(2, 3)
    {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Invoice "
                    + " ADD COLUMN invoiceEmail TEXT");
        }
    };
    public static FirebaseAnalytics firebaseAnalytics;
    public static AppDatabase db = null;
    public static Context context;
    public static JSONObject userProfile;
    public static User user;
    static List<String> stateList = new ArrayList<>();
    public static long localInvoiceId = 0;
    public static List<String> measurementUnits;

    public static List<String> getStateList() {
        return stateList;
    }

    public static AppDatabase getDatabase() {
        return db;
    }

    public static void setLocalInvoiceId(long localInvoiceId){
        SharedPreferences sharedPref = context.getSharedPreferences("MarketExcelAppPref",
                context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("localInvoiceId", localInvoiceId);
        editor.commit();
    }

    public static long getLocalInvoiceId(){
        SharedPreferences sharedPref = context.getSharedPreferences("MarketExcelAppPref",
                context.MODE_PRIVATE);
        long localInvoiceId = sharedPref.getLong("localInvoiceId",1);
        return localInvoiceId;
    }

    public static List<String> getMeasurementUnits(){
        return measurementUnits;
    }

    public static String getUserToken() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        String token = sharedPref.getString(context.getString(R.string.user_login_token), "");
        return token;

    }

    public static void saveUserToken(String token) {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.user_login_token), token);
        editor.commit();
    }
    public static Boolean getLogout() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        Boolean userLogoutToken = sharedPref.getBoolean("userLogoutToken", true);
        return userLogoutToken;

    }
    public static void setLogout(Boolean bool) {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("userLogoutToken", bool);
        editor.commit();
    }

    public static void saveScheduleLogOutDate(String scheduleDate) {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.schedule_date), scheduleDate);
        editor.commit();
    }

    public static String getScheduleDate() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        String scheduleDate = sharedPref.getString(context.getString(R.string.schedule_date), "");
        return scheduleDate;

    }

    public static void setLogoutDaily(Boolean bool) {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("userLogoutTokenDaily", bool);
        editor.commit();
    }
    public static Boolean getLogoutDaily() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        Boolean userLogoutToken = sharedPref.getBoolean("userLogoutTokenDaily", true);
        return userLogoutToken;

    }
    public static void setuserMetaDataFlag(Boolean bool) {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("userMetaDataFlag", bool);
        editor.commit();
    }
    public static Boolean getUserMetaDataFlag() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        Boolean userLogoutToken = sharedPref.getBoolean("userMetaDataFlag", true);
        return userLogoutToken;

    }




    public static boolean showGST() {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        boolean showGST = sharedPref.getBoolean(context.getString(R.string.showGST), false);
        return showGST;

    }
    public static void setGSTFilled( ) {

        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.gst), true);
        editor.commit();

    }

    public static void setShowGstPopup(int value) {
        // -1 : show, 1 & 0 : don't show
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.showGstPopup), value);
        editor.commit();
    }

    public static int showGstPopup() {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        int showGstPopupValue = sharedPref.getInt(context.getString(R.string.showGstPopup), -1);
        return showGstPopupValue;
    }

    public static boolean getGSTFilled() {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        boolean showGST = sharedPref.getBoolean(context.getString(R.string.gst), false);
        return showGST;

    }

    public static int getInVoiceNumber() {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        int InVoiceNumber = sharedPref.getInt(context.getString(R.string.InVoiceNumber), 0);
        return InVoiceNumber;

    }

    public static void setInvoiceNumber(int invNo) {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.InVoiceNumber), invNo);
        editor.commit();
    }

    public static int getInVoiceNumberForNonGst() {

        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        int InVoiceNumber = sharedPref.getInt(context.getString(R.string.InVoiceNumberForNonGST), 0);
        return InVoiceNumber;

    }

    public static void setInvoiceNumberForNonGst(int invNo) {
        SharedPreferences sharedPref =
                context.getSharedPreferences(context.getString(R.string.preference_file_key),
                        context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.InVoiceNumberForNonGST), invNo);
        editor.commit();
    }

    public static void saveLoginUserID(int userId) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(context.getString(R.string.login_user_id), userId);
        editor.commit();
    }

    public static void saveUserDetails(String user) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.userDetails), user);
        editor.commit();
    }
    public static String getUserDetails() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        String userId = sharedPref.getString(context.getString(R.string.userDetails), "");
        return userId;
    }
    public static void isFirstBill(Boolean firstBill)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("firstBill", firstBill);
        editor.commit();

    }
    public static boolean getIsFirstBill()
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        Boolean FirstBill = sharedPref.getBoolean("firstBill", false);
        return FirstBill;

    }

    public static int getUserID() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        int userId = sharedPref.getInt(context.getString(R.string.login_user_id), 0);
        return userId;

    }

    public static void saveLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveGetCategoriesLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.GET_CATEGORIES_LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getGetCategoriesLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.GET_CATEGORIES_LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveGetBrandLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.GET_BRAND_LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getGetBrandLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.GET_BRAND_LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveGetProductLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.GET_PRODUCT_LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getGetProductLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.GET_PRODUCT_LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveGetInvoiceLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.GET_INVOICE_LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getGetInvoiceLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.GET_INVOICE_LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveGetPurchaseLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.GET_PURCHASE_LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getGetPurchaseLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.GET_PURCHASE_LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveGetInventoryLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.GET_INVENTORY_LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getGetInventoryLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.GET_INVENTORY_LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveGetDistributorLAST_SYNC_TIMESTAMP(long timeStamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.GET_DISTRIBUTOR_LAST_SYNC_TIMESTAMP, timeStamp);
        editor.commit();
    }

    public static long getGetDistributorLAST_SYNC_TIMESTAMP() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        long timeStamp = sharedPref.getLong(Constants.GET_DISTRIBUTOR_LAST_SYNC_TIMESTAMP, 0);
        return timeStamp;
    }

    public static void saveInVoiceOffline(JSONArray jsonArray) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.invoicesOffline), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Invoices", jsonArray.toString());
        editor.commit();
    }

    public static String getInVoiceOffline() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.invoicesOffline), context.MODE_PRIVATE);
        String invoices = sharedPref.getString("Invoices", new JSONArray().toString());
        return invoices;
    }

    public static boolean getTermsAndConditions() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        boolean agree = sharedPref.getBoolean(Constants.TERMS_AND_CONDITION, false);
        return agree;
    }

    public static void setTermsAndConditions(boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Constants.TERMS_AND_CONDITION, value);
        editor.commit();
    }

    public static int getMigrationStatus() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        int agree = sharedPref.getInt(Constants.MIGRATION_DONE, 0);
        return agree;
    }

    public static void setMigrationStatus(int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Constants.MIGRATION_DONE, value);
        editor.commit();
    }

    public static void saveSelectedCategories(String cats) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.selected_categories), cats);
        editor.commit();
    }

    public static String getSelectedCategories() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        String userId = sharedPref.getString(context.getString(R.string.select_category), "");
        return userId;

    }

    public static void saveIsGSTVerifies(boolean bool) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.isGSTVerified), bool);
        editor.commit();
    }

    public static boolean getIsGSTVerifies() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        boolean bool = sharedPref.getBoolean(context.getString(R.string.isGSTVerified), false);
        return bool;
    }
    public static void saveLanguage(String lng) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.LANGUAGE, lng);
        editor.commit();
    }
    public static String getLanguage() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        String lng = sharedPref.getString(Constants.LANGUAGE, "en");
        return lng;

    }
    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        MyApplication.user = user;
    }

    public static FirebaseAnalytics getFirebaseAnalytics(Context context){
        if(firebaseAnalytics==null)
            firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        return firebaseAnalytics;
    }
    @Override
    public void onCreate() {
        ActivityLifecycleCallback.register(this);
        cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.INFO);
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG);
        CleverTapAPI.createNotificationChannel(getApplicationContext(),"test","test","Your Channel Description", NotificationManager.IMPORTANCE_MAX,true);

        super.onCreate();
        if(firebaseAnalytics==null)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        db.
//            if(getMigrationStatus()!=3) {
//                db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
//                        .addMigrations(MIGRATION_2_3).build();
//                setMigrationStatus(3);
//            }
//            else
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .build();
        context = getApplicationContext();
        formStateList();
        MultiDex.install(context);

        //CleverTapAPI.createNotificationChannel(getApplicationContext(),"test","test","Your Channel Description", NotificationManager.IMPORTANCE_MAX,true);
    }

    private void formStateList() {
        String stateListArray[] = {"Select State", "Andhra Pradesh",

                "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh"
                , "Jammu & Kashmir", "Jharkhand", "Karnataka",
                "Kerala",
                "Madhya Pradesh",
                "Maharashtra",
                "Manipur",
                "Meghalaya",
                "Mizoram",
                "Nagaland",
                "Odisha",
                "Punjab",
                "Rajasthan",
                "Sikkim",
                "Tamil Nadu",
                "Telangana",
                "Tripura",
                "Uttarakhand",
                "Uttar Pradesh",
                "West Bengal"};

        stateList = Arrays.asList(stateListArray);

    }

    public static JSONObject getUserProfile() {
        return userProfile;
    }

    public static void setUserProfile(JSONObject userProfile) {
        MyApplication.userProfile = userProfile;
    }


    public static String getUnSyncedExpenses() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        String expenses = sharedPref.getString(context.getString(R.string.expenses), "");
        return expenses;

    }

    public static void saveUnSyncedExpenses(String cats) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.expenses), cats);
        editor.commit();
    }

    public static String getUnSyncedInvoice() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        String expenses = sharedPref.getString(context.getString(R.string.inv_pref), "");
        return expenses;

    }
    public static void saveUnSyncedInvoices(String inv) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.inv_pref), inv);
        editor.commit();
    }

    public static boolean getIsGst()
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        boolean CheckIsGst = sharedPref.getBoolean("isGst",false);
        return CheckIsGst;
    }
    public static void setIsGst(boolean check)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isGst",check);
        editor.commit();
    }

    public static boolean getHaveGst()
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        boolean CheckIsGst = sharedPref.getBoolean("haveGst",true);
        return CheckIsGst;
    }
    public static void setHaveGst(boolean check)
    {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("haveGst",check);
        editor.commit();
    }
}
