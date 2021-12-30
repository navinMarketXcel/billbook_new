package com.billbook.app.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.models.InvoiceModelV2;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.billbook.app.services.SyncService;
import com.billbook.app.utils.Util;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.receivers.AlarmReceiver;
import com.billbook.app.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jaiman.nitin.com.customclickableemailphonetextview.ClickPattern;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "HomeActivity";

    LinearLayout btnSellingDetails, btnBilling, btnManageInventory, btnGetSalesReport, btnSearchInvoice;
    Toolbar mToolbar;
    private NavigationView navigationView;
    private TextView tvName;
    private TextView tvEmail;
    private ImageView iv;
    private JSONObject userProfile;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TextView syncText;
    private JSONObject profile;
    private JSONArray exp = null;
    private String expString;
    private InMobiBanner mBannerAd1=null, mBannerAd2=null;
    // Change placementId values to actual placementId for InMobi BannerAd
    private long placementId1=0, placementId2=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        initUI();
        Util.setMeasurementUnits();
        try {
            userProfile= new JSONObject (((MyApplication)getApplication()).getUserDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject consentObject = new JSONObject();

        try{
        consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE,true);
        consentObject.put("gdpr","0");
        consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB,"<< CONSENT in IAB Format");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        InMobiSdk.init(this, "", consentObject, new SdkInitializationListener() {
            @Override
            public void onInitializationComplete(@Nullable Error error) {
                if(null != error){
                    Log.e(TAG,"InMobi Init Failed " + error.getMessage());
                }
                else{
                    Log.d(TAG, "onInitializationComplete: Complete InMobi");
                    if(null==mBannerAd1 && null == mBannerAd2){
                        Log.d(TAG, "onInitializationComplete: mbannerad1 null");
                        createBannerAd();
                    }
                    else if(mBannerAd2==null){
                        mBannerAd1.load();
                    }
                    else if(mBannerAd1==null){
                        mBannerAd2.load();
                    }
                    else{
                        mBannerAd1.load();
                        mBannerAd2.load();
                    }

                }
            }
        });
        

        //    Room.databaseBuilder(getApplicationContext(), AppDatabase.class,"database-name.db").addMigrations(addInvoiceNumber).build();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Called when a drawer's position changes.
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //Called when a drawer has settled in a completely open state.
                //The drawer is interactive at this point.
                // If you have 2 drawers (left and right) you can distinguish
                // them by using id of the drawerView. int id = drawerView.getId();
                // id will be your layout's id: for example R.id.left_drawer

                navigationView.setCheckedItem(R.id.nav_home);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Called when a drawer has settled in a completely closed state.
                navigationView.setCheckedItem(R.id.nav_home);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
            }
        });
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setToolbar();

//        isGSTVerified();
        updateGST();
//        CustomPartialyClickableTextview customPartialyClickableTextview = (CustomPartialyClickableTextview) findViewById(R.id.txtWhatsAppNumber);

        /**
         * Create Objects For Click Patterns
         */
        //ClickPattern email=new ClickPattern();
        ClickPattern phone = new ClickPattern();


        /**
         * set Functionality for what will happen on click of that pattern
         * In this example pattern is phone
         */
        phone.setOnClickListener(new ClickPattern.OnClickListener() {
            @Override
            public void onClick() {
                //Toast.makeText(HomeActivity.this, "phone clicked", Toast.LENGTH_LONG).show();

                PackageManager packageManager = getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);

                try {
                    String url = "https://api.whatsapp.com/send?phone=" + "919319595452" + "&text=" + URLEncoder.encode("HelpLine Message : ", "UTF-8");
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        startActivity(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        phone.setRegex("[1-9][0-9]{9,14}"); // regex for phone number
//
//        customPartialyClickableTextview.addClickPattern("phone", phone);
//    customPartialyClickableTextview.addClickPattern("weblink",weblink);
    }

    private void createBannerAd(){
        mBannerAd1 = new InMobiBanner(this, placementId1);
        mBannerAd2 = new InMobiBanner(this, placementId2);
        RelativeLayout adContainer = (RelativeLayout) findViewById(R.id.parent);
        int width = toPixelUnits(320);
        int height = toPixelUnits(50);

        // mBannerAd1
        RelativeLayout.LayoutParams bannerLayoutParams1 = new RelativeLayout.LayoutParams(width, height);
        bannerLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bannerLayoutParams1.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mBannerAd1.setAnimationType(InMobiBanner.AnimationType.ROTATE_HORIZONTAL_AXIS);
        mBannerAd1.setLayoutParams(bannerLayoutParams1);
        mBannerAd1.setRefreshInterval(20);
        adContainer.addView(mBannerAd1);
        Log.d(TAG, "createBannerAd: completed 1");
        mBannerAd1.load();

        // mBannerAd2
        RelativeLayout.LayoutParams bannerLayoutParams2 = new RelativeLayout.LayoutParams(width, height);
        bannerLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bannerLayoutParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bannerLayoutParams2.setMargins(0,0,0,toPixelUnits(55));
        mBannerAd2.setAnimationType(InMobiBanner.AnimationType.ROTATE_HORIZONTAL_AXIS);
        mBannerAd2.setLayoutParams(bannerLayoutParams2);
        mBannerAd2.setRefreshInterval(40);
        adContainer.addView(mBannerAd2);

        mBannerAd2.load();
        setupBannerAd(mBannerAd2);
        setupBannerAd(mBannerAd1);

        // to dynamically make space for ad
//        ViewGroup.LayoutParams main2Lp = ((ViewGroup) main2).getLayoutParams();
//
//        if(main2Lp instanceof ViewGroup.MarginLayoutParams){
//            ((ViewGroup.MarginLayoutParams) main2Lp).bottomMargin = toPixelUnits(50);
//        }
//        else{
//            Log.d(TAG, "setBannerLayoutParams: Not able to set bottom margin");
//        }

    }

    private void setupBannerAd(InMobiBanner mBannerAd){

        mBannerAd.setListener(new BannerAdEventListener() {

            @Override
            public void onAdLoadSucceeded(@NonNull InMobiBanner inMobiBanner, @NonNull AdMetaInfo adMetaInfo) {
                super.onAdLoadSucceeded(inMobiBanner, adMetaInfo);
                Log.d(TAG, "onAdLoadSucceeded: with bid " + adMetaInfo.getBid());
            }

            @Override
            public void onAdLoadFailed(@NonNull InMobiBanner inMobiBanner, @NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                super.onAdLoadFailed(inMobiBanner, inMobiAdRequestStatus);
                Log.d(TAG, "onAdLoadFailed: Banner ad failed to load with error: " + inMobiAdRequestStatus.getMessage());
            }

            @Override
            public void onAdClicked(@NonNull InMobiBanner inMobiBanner, Map<Object, Object> map) {
                super.onAdClicked(inMobiBanner, map);
                Log.d(TAG, "onAdClicked: ");
            }
            @Override
            public void onAdDisplayed(@NonNull InMobiBanner inMobiBanner) {
                super.onAdDisplayed(inMobiBanner);
                Log.d(TAG, "onAdDisplayed: ");
            }

            @Override
            public void onAdDismissed(@NonNull InMobiBanner inMobiBanner) {
                super.onAdDismissed(inMobiBanner);
                Log.d(TAG, "onAdDismissed: ");
            }

            @Override
            public void onUserLeftApplication(@NonNull InMobiBanner inMobiBanner) {
                super.onUserLeftApplication(inMobiBanner);
                Log.d(TAG, "onUserLeftApplication: ");
            }

            @Override
            public void onRewardsUnlocked(@NonNull InMobiBanner inMobiBanner, Map<Object, Object> map) {
                super.onRewardsUnlocked(inMobiBanner, map);
                Log.d(TAG, "onRewardsUnlocked: ");
            }

        });
        mBannerAd.load();
    }

    private int toPixelUnits(int dipUnit) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dipUnit * density);
    }

    private void initUI() {

        try {
            profile= new JSONObject (((MyApplication)getApplication()).getUserDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        btnManageInventory = findViewById(R.id.btnManageInventory);
        btnBilling = findViewById(R.id.btnBilling);
        btnSellingDetails = findViewById(R.id.btnSellingDetails);
        btnGetSalesReport = findViewById(R.id.btnGetSalesReport);
        btnSearchInvoice = findViewById(R.id.btnSearchInvoice);
        syncText = findViewById(R.id.syncText);
        mToolbar = findViewById(R.id.toolbar);


        setSupportActionBar(mToolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        btnBilling.setOnClickListener(this);
        btnManageInventory.setOnClickListener(this);

        btnSellingDetails.setOnClickListener(this);
        btnGetSalesReport.setOnClickListener(this);
        btnSearchInvoice.setOnClickListener(this);
        setAlarm();
        // for displaying sync message on HomeActivity
        syncOffLineInvoiceFromDatabase();
        try {
             exp = null;
             expString = MyApplication.getUnSyncedExpenses();
            if (expString.length() > 0)
                exp = new JSONArray(expString);

            if ((exp != null && exp.length() > 0))
                syncText.setVisibility(View.VISIBLE);
            else
                syncText.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public  void syncOffLineInvoiceFromDatabase(){
        new HomeActivity.FetchInvoice(MyApplication.getDatabase().newInvoiceDao()).execute();
    }


    private class FetchInvoice extends AsyncTask<Void,Void, List<InvoiceModelV2>> {
        NewInvoiceDao newInvoiceDao;
        private FetchInvoice(NewInvoiceDao newInvoiceDao){
            this.newInvoiceDao =newInvoiceDao;
        }
        @Override
        protected List<InvoiceModelV2> doInBackground(Void... voids) {
            return newInvoiceDao.getAllOffLineInvoice();
        }

        @Override
        protected void onPostExecute(List<InvoiceModelV2> invoiceModelV2List) {
            super.onPostExecute(invoiceModelV2List);

            if (invoiceModelV2List.size()>0 || ((exp != null && exp.length() > 0)))
                syncText.setVisibility(View.VISIBLE);
            else
                syncText.setVisibility(View.INVISIBLE);

        }
    }


    private void setToolbar() {

        tvName = navigationView.getHeaderView(0).findViewById(R.id.tvName);
        tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvEmail);
        iv = navigationView.getHeaderView(0).findViewById(R.id.imageViewxx);
        if(userProfile != null) {
            try {
                updateDrawerProfileImg();
                tvName.setText(userProfile.getString("shopName"));
                tvEmail.setText(userProfile.getString("shopAddr"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                final User user = MyApplication.getDatabase().userDao().getUser(MyApplication.getUserID());
//                MyApplication.setUser(user);
//                Log.v(TAG, "user::" + user);
//                if (user != null) {
//                    Objects.requireNonNull(getSupportActionBar()).setTitle(user.getShop_name());
//
//                    if (user.getUsername() != null) {
//                        tvName.setText(user.getUsername());
//                    }
//                    if (user.getEmail() != null) {
//                        tvEmail.setText(user.getEmail());
//                    }
//                    SharedPreferences sharedPref =
//                            getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
//                                    getApplicationContext().MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPref.edit();
//                    editor.putBoolean(getString(R.string.showGST), (user.getGst_no() == null || user.getGst_no().isEmpty()) ? false : true);
//                    editor.commit();
//                }
//            }
//        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnManageInventory:
                Util.postEvents("Expense","Expense",this.getApplicationContext());
                Intent intent = new Intent(HomeActivity.this, ExpenseActivity.class);
                startActivity(intent);

                break;
            case R.id.btnBilling:
                Util.postEvents("Billing","Billing",this.getApplicationContext());
                intent = new Intent(HomeActivity.this, BillingNewActivity.class);
                startActivity(intent);
                break;
            case R.id.btnSellingDetails:
                intent = new Intent(HomeActivity.this, LedgerLoginActivity.class);
                intent.putExtra("startReportActivity", false);
                startActivity(intent);
                break;
            case R.id.btnSearchInvoice:
                Util.postEvents("Search Bills","Search Bills",this.getApplicationContext());

                intent = new Intent(HomeActivity.this, SearchInvoiceActivity.class);
                startActivity(intent);
                break;
            case R.id.btnGetSalesReport:
                Util.postEvents("Day Book","Day Book",this.getApplicationContext());
                intent = new Intent(HomeActivity.this, DayBookActivity.class);
                intent.putExtra("startReportActivity", true);
                startActivity(intent);
                break;

        }
    }

    private void showAlert() {
        SweetAlertDialog sweetAlertDialog =
                new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        sweetAlertDialog.setTitleText(getString(R.string.do_u_want_to_logout));
        sweetAlertDialog.setConfirmText("Yes");
        sweetAlertDialog.setCancelText("No");
        sweetAlertDialog.showCancelButton(true);
        sweetAlertDialog.setCancelClickListener(null);
        sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override

            public void onClick(SweetAlertDialog sDialog) {
                sDialog.dismiss();
                logoutToken();
            }
        });
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
    }

    private void logoutToken() {

        SharedPreferences sharedPref =
                getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
                        MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.user_login_token), " ");
        editor.commit();
//    MyApplication.saveGetCategoriesLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetInventoryLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetInvoiceLAST_SYNC_TIMESTAMP(0);


        Intent intentObj = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intentObj);
        finish();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_my_profile) {
            Util.postEvents("My Profile","My Profile",HomeActivity.this.getApplicationContext());
            Intent intent1 = new Intent(HomeActivity.this, EditProfileActivity.class);
            intent1.putExtra("editProfile", true);
            startActivity(intent1);
        }  else if (id == R.id.nav_help) {
            Util.postEvents("Help","Help",HomeActivity.this.getApplicationContext());
            Intent intent1= new Intent(HomeActivity.this, HelpActivity.class);
            intent1.putExtra("isFromHelpTab",true);
            startActivity(intent1);
        } else if (id == R.id.nav_logout) {
            Util.postEvents("Logout","Logout",HomeActivity.this.getApplicationContext());
            showAlert();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void setAlarm() {
        startService(new Intent(this, SyncService.class));
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add 5 minutes to the calendar object
        cal.add(Calendar.SECOND, 10);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(Constants.ALARM_RECEIVER_ACTION);
        intent.putExtra("alarm_message", "O'Doyle Rules!");

        // In reality, you would want to have a static variable for the request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(this, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
// Get the AlarmManager service
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    /*
    // am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
    long currentTime = System.currentTimeMillis();
    long oneMinute = 30 * 1000;
    am.setRepeating(AlarmManager.RTC_WAKEUP, currentTime+oneMinute,1000 * 60 * 60 * 24 , sender);
*/

        // every day at scheduled time
        Calendar calendar = Calendar.getInstance();
        // if it's after or equal 6 am schedule for next day
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 6) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

//        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+3000,3000 , sender);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            userProfile= new JSONObject (MyApplication.getUserDetails());
            updateDrawerProfileImg();
            getLatestInvoice(userProfile.getString("userid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startSpotLight(btnBilling, "Billing", "This will be used for billing.");
        try{
            if(null==mBannerAd1){
                setupBannerAd(mBannerAd1);
            }
            else{
                mBannerAd1.load();
            }
            if(null==mBannerAd2){
                setupBannerAd(mBannerAd2);
            }
            else{
                mBannerAd2.load();
            }
        }
        catch(Exception e){
         e.printStackTrace();
        }
    }

    public void updateDrawerProfileImg(){
        try{
            if (userProfile.has("companyLogo") && userProfile.getString("companyLogo") != null) {
                String companyLogoPath = userProfile.getString("companyLogo");
                companyLogoPath = companyLogoPath.replaceAll("\\/", "/");

                Picasso.get()
                        .load(companyLogoPath)
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .resize(70, 70)
                        .centerCrop()
                        .into(iv);
            }
        }
        catch (JSONException e){
            e.fillInStackTrace();
        }
    }


    private void getLatestInvoice(String userid) {
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        String token = MyApplication.getUserToken();

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        Map<String, String> body = new HashMap<>();
        body.put("userid",userid);
        Call<Object> call = apiService.getLatestInvoice(headerMap,body);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                final JSONObject body;
                try {
                    body = new JSONObject(new Gson().toJson(response.body()));
                    Log.d(TAG, "Invoice Body::" + body);
                    if (body.getJSONObject("data").has("nonGstBillNo")) {
                        MyApplication.setInvoiceNumber(body.getJSONObject("data").getInt("gstBillNo")+1);
                        MyApplication.setInvoiceNumberForNonGst(body.getJSONObject("data").getInt("nonGstBillNo")+1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
            }
        });

    }


    private void startSpotLight(View view, String title, String description) {
        final SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), this.MODE_PRIVATE);
        boolean showInfo = !sharedPref.getBoolean("isHomescreenIntroShown", false);
        if (showInfo) {
            new GuideView.Builder(this)
                    .setTitle(title)
                    .setContentText(description)
                    .setTargetView(view)
                    .setDismissType(DismissType.outside)
                    .setGuideListener(new GuideListener() {
                        @Override
                        public void onDismiss(View view) {
                            if (view.getId() == R.id.helpLine) {
                                SharedPreferences sharedPref =
                                        HomeActivity.this.getSharedPreferences(HomeActivity.this.getString(R.string.preference_file_key),
                                                HomeActivity.this.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean("isHomescreenIntroShown", true);
                                editor.commit();
                            } else {
                                if (view.getId() == R.id.btnBilling) {
                                    startSpotLight(btnManageInventory, "Manage Expenses",
                                            "This is used for the managing your expenses.");
                                } else if (view.getId() == R.id.btnManageInventory) {
                                    startSpotLight(btnSearchInvoice, "Search Invoice", "Search, edit and cancel bills.");
                                } else if (view.getId() == R.id.btnSearchInvoice) {
                                    startSpotLight(btnGetSalesReport, "Day Book", "Check profit and download your data.");
                                } else if(view.getId() == R.id.btnGetSalesReport) {
                                    startSpotLight(findViewById(R.id.wathcDemo), "Watch Demo", "Videos on how to use app.");
                                }else{
                                    startSpotLight(findViewById(R.id.helpLine), "Helpline", "Customer support details.");
                                }
                            }
                        }
                    })
                    .build()
                    .show();
        }
    }
    private void updateGST() {
//    boolean test = MyApplication.getIsGSTVeeditProfilerifies();
        if(MyApplication.showGstPopup() == -1 ){
            DialogUtils.showAlertDialog(this, "Yes", "No",
                    "Do you have a GST number?", new DialogUtils.DialogClickListener() {
                        @Override
                        public void positiveButtonClick() {
                            MyApplication.setShowGstPopup(1);
                            sendGstUpdateStatus(1);
                            MyApplication.setGSTFilled();
                            Intent intent = new Intent(HomeActivity.this, EditProfileActivity.class);
                            startActivity(intent);

                        }

                        @Override
                        public void negativeButtonClick() {
                            sendGstUpdateStatus(0);
                            MyApplication.setShowGstPopup(0);
                            MyApplication.setGSTFilled();
                        }
                    });
        }
    }

    private void sendGstUpdateStatus(int gstStatus) {
        try {
            DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            Map<String, Integer> map = new HashMap<>();
            map.put("isGST", gstStatus);
            long userid = profile.getLong("userid");
            Call<Object> call = apiService.updateUserGstStatus(userid, map);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP", body.toString());
                        if (body.getBoolean("status")) {
                            MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                        } else {
                            DialogUtils.showToast(HomeActivity.this, "Failed update GST");
                        }

                    } catch (JSONException e) {
                        DialogUtils.showToast(HomeActivity.this, "Failed update GST");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(HomeActivity.this, "Failed update profile to server");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void isGSTVerified() {
//    boolean test = MyApplication.getIsGSTVeeditProfilerifies();
        if (!MyApplication.getIsGSTVerifies()) {
            DialogUtils.showAlertDialog(this, "Yes", "No",
                    "Please verify GST no. Press yes to proceed ?", new DialogUtils.DialogClickListener() {
                        @Override
                        public void positiveButtonClick() {
                            Intent intent = new Intent(HomeActivity.this, LedgerLoginActivity.class);
                            intent.putExtra("verifyGST", true);
                            startActivity(intent);
                        }

                        @Override
                        public void negativeButtonClick() {

                        }
                    });
        }
    }
    public void startHelpActivity(View v){
        Util.postEvents("HelpLine","HelpLine",HomeActivity.this.getApplicationContext());

        Intent intent = new Intent(this,HelpActivity.class);
        intent.putExtra("isFromHelpTab",false);
        startActivity(intent);
    }
    public void startYoutubeActivity(View v){
        Util.postEvents("Watch Demo","Watch Demo",this.getApplicationContext());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://www.youtube.com/playlist?list=PLFuhsI7LfH3VFoH8oTfozpUlITI6fy7U8"));
        intent.setPackage("com.google.android.youtube");
        startActivity(intent);
    }

    private void sendEvent(){
        if(mFirebaseAnalytics!=null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "More Than 5 Invoices ");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
    }
}
