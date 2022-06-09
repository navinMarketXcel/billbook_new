package com.billbook.app.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.billbook.app.BuildConfig;
import com.billbook.app.R;
import com.billbook.app.activities.BillingNewActivity;
import com.billbook.app.activities.DayBookActivity;
import com.billbook.app.activities.EditProfileActivity;
import com.billbook.app.activities.ExpenseActivity;
import com.billbook.app.activities.HelpActivity;
import com.billbook.app.activities.LedgerLoginActivity;
import com.billbook.app.activities.LoginActivity;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.activities.SearchInvoiceActivity;
import com.billbook.app.database.daos.NewInvoiceDao;
import com.billbook.app.database.models.InvoiceModelV2;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.receivers.AlarmReceiver;
import com.billbook.app.services.SyncService;
import com.billbook.app.utils.Constants;
import com.billbook.app.utils.Util;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.inmobi.ads.AdMetaInfo;
import com.inmobi.ads.InMobiAdRequestStatus;
import com.inmobi.ads.InMobiBanner;
import com.inmobi.ads.listeners.BannerAdEventListener;
import com.inmobi.sdk.InMobiSdk;
import com.inmobi.sdk.SdkInitializationListener;
import com.vungle.warren.AdConfig;
import com.vungle.warren.BannerAdConfig;
import com.vungle.warren.Banners;
import com.vungle.warren.InitCallback;
import com.vungle.warren.LoadAdCallback;
import com.vungle.warren.Vungle;
import com.vungle.warren.VungleBanner;
import com.vungle.warren.error.VungleException;

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

public class HomeFragment extends Fragment
        implements View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }
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
    InMobiBanner mBannerAd1;
    public static boolean isSheetShown = false;
    VungleBanner vungleBanner;
    private JSONArray gstList, nonGstList;
    // replace with actual placementId from InMobi
    private long placementId1= Long.parseLong(BuildConfig.PlacementId1);
    private long placementId2=Long.parseLong(BuildConfig.PlacementId2);
    private String placementId3=BuildConfig.VunglePlacement;
    private Button register;
    private Button wathcDemo,helpLine;
    RelativeLayout adContainer;

  /*  @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        register=view.findViewById(R.id.btnRegister);
        adContainer = (RelativeLayout) view.findViewById(R.id.parent);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        initUI(view);
        bottomSheetDialog(view);
        Util.setMeasurementUnits(getActivity());
        try {
            userProfile= new JSONObject (((MyApplication)getActivity().getApplication()).getUserDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InMobiInitialization();
        VungleInitialization();
        return view;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateGST();
        ClickPattern phone = new ClickPattern();


        /**
         * set Functionality for what will happen on click of that pattern
         * In this example pattern is phone
         */
        phone.setOnClickListener(new ClickPattern.OnClickListener() {
            @Override
            public void onClick() {
                //Toast.makeText(HomeActivity.this, "phone clicked", Toast.LENGTH_LONG).show();

                PackageManager packageManager = getActivity().getPackageManager();
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


    private void InMobiInitialization() {
        JSONObject consentObject = new JSONObject();

        try{
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE,true);
            consentObject.put("gdpr","0");
            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB,"<< CONSENT in IAB Format");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        try {
            // replace empty string with account Id of InMobi
            InMobiSdk.init(getActivity(), BuildConfig.AccountId, consentObject, new SdkInitializationListener() {
                @Override
                public void onInitializationComplete(@Nullable Error error) {
                    if (null != error) {
                    } else {
                        if (null == mBannerAd1) {
                            createBannerAd();
                            mBannerAd1.load();
                        }  else {
                            mBannerAd1.load();
                        }

                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void bottomSheetDialog(View view){

        if(isSheetShown)
        {
            BottomSheetDialog gstSheet = new BottomSheetDialog(getActivity(),R.style.BottomSheetDialogTheme);
            View bottomSheet = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.activity_home_addgst,(LinearLayout)view.findViewById(R.id.bottomSheetContainer));
            bottomSheet.findViewById(R.id.yesGSt).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gstSheet.dismiss();
                    BottomSheetDialog yesGst = new BottomSheetDialog(getActivity(),R.style.BottomSheetDialogTheme);
                    View yesGstSheet = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.activity_home_addgstyes,(LinearLayout)view.findViewById(R.id.editGSTyes));
//                    BottomSheetBehavior behavior = BottomSheetBehavior.from(yesGstSheet);
//                    behavior.setPeekHeight(250);
                    yesGstSheet.findViewById(R.id.btnUpdGst).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            BottomSheetDialog showGif =new BottomSheetDialog(getActivity(),R.style.BottomSheetDialogTheme);
                            View showgifView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.activity_home_gstyes,(LinearLayout)view.findViewById(R.id.GSTyes));
                            showGif.setContentView(showgifView);
                            showGif.show();
                            gstSheet.dismiss();
                            yesGst.dismiss();
                        }
                    });
                    yesGstSheet.findViewById(R.id.cancelGst).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            yesGst.dismiss();
                        }
                    });
                    yesGst.setContentView(yesGstSheet);
                    yesGst.show();

                }
            });
            bottomSheet.findViewById(R.id.noGSt).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gstSheet.dismiss();
                }
            });
            gstSheet.setContentView(bottomSheet);
            gstSheet.show();
        }

    }

    private void createBannerAd(){
        Log.v("Inmobi createBannerAd","In mobi banner createBannerAd");
        mBannerAd1 = new InMobiBanner(getActivity(), placementId1);

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
        mBannerAd1.load();
        setupBannerAd(mBannerAd1);

    }

    private void setupBannerAd(InMobiBanner mBannerAd){
        try{
            mBannerAd.setListener(new BannerAdEventListener() {

                @Override
                public void onAdLoadSucceeded(@NonNull InMobiBanner inMobiBanner, @NonNull AdMetaInfo adMetaInfo) {
                    super.onAdLoadSucceeded(inMobiBanner, adMetaInfo);
                }

                @Override
                public void onAdLoadFailed(@NonNull InMobiBanner inMobiBanner, @NonNull InMobiAdRequestStatus inMobiAdRequestStatus) {
                    super.onAdLoadFailed(inMobiBanner, inMobiAdRequestStatus);
                }

                @Override
                public void onAdClicked(@NonNull InMobiBanner inMobiBanner, Map<Object, Object> map) {
                    super.onAdClicked(inMobiBanner, map);
                }

                @Override
                public void onAdDisplayed(@NonNull InMobiBanner inMobiBanner) {
                    super.onAdDisplayed(inMobiBanner);
                }

                @Override
                public void onAdDismissed(@NonNull InMobiBanner inMobiBanner) {
                    super.onAdDismissed(inMobiBanner);
                }

                @Override
                public void onUserLeftApplication(@NonNull InMobiBanner inMobiBanner) {
                    super.onUserLeftApplication(inMobiBanner);
                }

                @Override
                public void onRewardsUnlocked(@NonNull InMobiBanner inMobiBanner, Map<Object, Object> map) {
                    super.onRewardsUnlocked(inMobiBanner, map);
                }

            });
            mBannerAd.load();
        }catch(Exception e) {
           e.printStackTrace();
        }
    }

    private int toPixelUnits(int dipUnit) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dipUnit * density);
    }


    private void VungleInitialization() {
        Vungle.init(BuildConfig.VungleId, getActivity().getApplicationContext(), new InitCallback() {
            @Override
            public void onSuccess() {
                final BannerAdConfig bannerAdConfig = new BannerAdConfig();
                bannerAdConfig.setAdSize(AdConfig.AdSize.BANNER);
                Banners.loadBanner(BuildConfig.VunglePlacement, bannerAdConfig, vungleLoadAdCallback);
            }

            @Override
            public void onError(VungleException e) {
                e.printStackTrace();
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {
                // Ad has become available to play for a cache optimized placement
            }
        });
    }

    private final LoadAdCallback vungleLoadAdCallback = new LoadAdCallback() {
        @Override
        public void onAdLoad(String id) {
            // Ad has been successfully loaded for the placement
            if(Banners.canPlayAd(BuildConfig.VunglePlacement, AdConfig.AdSize.BANNER)){
                Log.v("Dikha jayega", "Banner");
                int width = toPixelUnits(320);
                int height = toPixelUnits(50);

                RelativeLayout.LayoutParams bannerLayoutParams1 = new RelativeLayout.LayoutParams(width, height);
                bannerLayoutParams1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                bannerLayoutParams1.addRule(RelativeLayout.CENTER_HORIZONTAL);
                bannerLayoutParams1.setMargins(0,0,0,toPixelUnits(55));
                try{
                    final BannerAdConfig bannerAdConfig = new BannerAdConfig();
                    bannerAdConfig.setAdSize(AdConfig.AdSize.BANNER);
                    vungleBanner = Banners.getBanner(BuildConfig.VunglePlacement, bannerAdConfig, null);
                    vungleBanner.setLayoutParams(bannerLayoutParams1);
                    adContainer.addView(vungleBanner);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onError(String id, VungleException e) {
            // Ad has failed to load for the placement
            e.printStackTrace();
        }
    };



    private void initUI(View view) {

        try {
            profile= new JSONObject (((MyApplication)getActivity().getApplication()).getUserDetails());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        btnManageInventory = view.findViewById(R.id.btnManageInventory);
        btnBilling = view.findViewById(R.id.btnBilling);
        btnSellingDetails = view.findViewById(R.id.btnSellingDetails);
        btnGetSalesReport = view.findViewById(R.id.btnGetSalesReport);
        btnSearchInvoice = view.findViewById(R.id.btnSearchInvoice);
        syncText = view.findViewById(R.id.syncText);
        wathcDemo = view.findViewById(R.id.wathcDemo);
        helpLine = view.findViewById(R.id.helpLine);
        mToolbar = view.findViewById(R.id.toolbar);


      // getActivity() .setSupportActionBar(mToolbar);

        //Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);

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
        new HomeFragment.FetchInvoice(MyApplication.getDatabase().newInvoiceDao()).execute();
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


//    private void setToolbar() {
//
//        tvName = navigationView.getHeaderView(0).findViewById(R.id.tvName);
//        tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvEmail);
//        iv = navigationView.getHeaderView(0).findViewById(R.id.imageViewxx);
//        if(userProfile != null) {
//            try {
//                updateDrawerProfileImg();
//                tvName.setText(userProfile.getString("shopName"));
//                tvEmail.setText(userProfile.getString("shopAddr"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
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
    //}

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnManageInventory:
                Util.postEvents("Expense","Expense",getActivity().getApplicationContext());
                Intent intent = new Intent(getActivity(), ExpenseActivity.class);
                startActivity(intent);

                break;
            case R.id.btnBilling:
                Util.postEvents("Billing","Billing",getActivity().getApplicationContext());
                intent = new Intent(getActivity(), BillingNewActivity.class);
                intent.putExtra("gstBillNo", gstList.toString());
                intent.putExtra("nonGstBillNo",nonGstList.toString());
                startActivity(intent);
                break;
            case R.id.btnSellingDetails:
                intent = new Intent(getActivity(), LedgerLoginActivity.class);
                intent.putExtra("startReportActivity", false);
                startActivity(intent);
                break;
            case R.id.btnSearchInvoice:
                Util.postEvents("Search Bills","Search Bills",getActivity().getApplicationContext());

                intent = new Intent(getActivity(), SearchInvoiceActivity.class);
                startActivity(intent);
                break;
            case R.id.btnGetSalesReport:
                Util.postEvents("Day Book","Day Book",getActivity().getApplicationContext());
                intent = new Intent(getActivity(), DayBookActivity.class);
                intent.putExtra("startReportActivity", true);
                startActivity(intent);
                break;

        }
    }

    private void showAlert() {
        SweetAlertDialog sweetAlertDialog =
                new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE);
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
                getActivity().getApplicationContext().getSharedPreferences(getString(R.string.preference_file_key),
                        getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        getActivity().finish();

//        editor.putString(getString(R.string.user_login_token), " ");
//        editor.commit();
//    MyApplication.saveGetCategoriesLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetBrandLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetProductLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetInventoryLAST_SYNC_TIMESTAMP(0);
//    MyApplication.saveGetInvoiceLAST_SYNC_TIMESTAMP(0);


        Intent intentObj = new Intent(getActivity(), LoginActivity.class);
        startActivity(intentObj);
        getActivity(). finish();
    }



    void setAlarm() {
        getActivity().startService(new Intent(getActivity(), SyncService.class));
        // get a Calendar object with current time
        Calendar cal = Calendar.getInstance();
        // add 5 minutes to the calendar object
        cal.add(Calendar.SECOND, 10);

        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.setAction(Constants.ALARM_RECEIVER_ACTION);
        intent.putExtra("alarm_message", "O'Doyle Rules!");

        // In reality, you would want to have a static variable for the request code instead of 192837
        PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
// Get the AlarmManager service
        AlarmManager am = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
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
    public void onStart()
    {
        super.onStart();
        HomeFragment.isSheetShown = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //HomeFragment.isSheetShown = false;

        try {
            userProfile= new JSONObject (MyApplication.getUserDetails());
//            updateDrawerProfileImg();
            getLatestInvoice(userProfile.getString("userid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startSpotLight(btnBilling, "Billing", "This will be used for billing.");
        try{
            if(null==mBannerAd1){
                mBannerAd1.load();
            }
            else{
                setupBannerAd(mBannerAd1);
            }

            if(null == vungleBanner){
                final BannerAdConfig bannerAdConfig = new BannerAdConfig();
                bannerAdConfig.setAdSize(AdConfig.AdSize.BANNER);
                Banners.loadBanner(BuildConfig.VunglePlacement, bannerAdConfig, vungleLoadAdCallback);
            } else{
                VungleInitialization();
            }
        }
        catch(Exception e){
         e.printStackTrace();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        HomeFragment.isSheetShown= false;
    }

    @Override
    public void  onStop()
    {
        super.onStop();
        HomeFragment.isSheetShown = false;
    }


//    public void updateDrawerProfileImg(){
//        try{
//            if (userProfile.has("companyLogo") && userProfile.getString("companyLogo") != null) {
//                String companyLogoPath = userProfile.getString("companyLogo");
//                companyLogoPath = companyLogoPath.replaceAll("\\/", "/");
//
//                Picasso.get()
//                        .load(companyLogoPath)
//                        .placeholder(R.drawable.man_new)
//                        .error(R.drawable.man_new)
//                        .resize(70, 70)
//                        .centerCrop()
//                        .into(iv);
//            }
//            else{
//                iv.setImageResource(R.drawable.man_new);
//            }
//        }
//        catch (JSONException e){
//            e.fillInStackTrace();
//        }
//    }


    private void getLatestInvoice(String userid) {
        ApiInterface apiService =
                ApiClient.getClient(getActivity()).create(ApiInterface.class);

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
                        gstList = body.getJSONObject("data").getJSONArray("gstBillNo");
                        nonGstList = body.getJSONObject("data").getJSONArray("nonGstBillNo");
                        MyApplication.setInvoiceNumber(gstList.getInt(gstList.length() - 1)+1);
                        MyApplication.setInvoiceNumberForNonGst(nonGstList.getInt(nonGstList.length() - 1)+1);
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

        try {
            final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                    getString(R.string.preference_file_key), getActivity().MODE_PRIVATE);
            boolean showInfo = !sharedPref.getBoolean("isHomescreenIntroShown", false);
            if (showInfo) {
                new GuideView.Builder(getActivity())
                        .setTitle(title)
                        .setContentText(description)
                        .setTargetView(view)
                        .setDismissType(DismissType.outside)
                        .setGuideListener(new GuideListener() {
                            @Override
                            public void onDismiss(View view) {
                                if (view.getId() == R.id.helpLine) {
                                    SharedPreferences sharedPref =
                                            getActivity().getSharedPreferences(HomeFragment.this.getString(R.string.preference_file_key),
                                                    getActivity().MODE_PRIVATE);
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
                                        startSpotLight(wathcDemo, "Watch Demo", "Videos on how to use app.");
                                    }else{
                                        startSpotLight(helpLine, "Helpline", "Customer support details.");
                                    }
                                }
                            }
                        })
                        .build()
                        .show();
        }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void updateGST() {
//    boolean test = MyApplication.getIsGSTVeeditProfilerifies();
        if(MyApplication.showGstPopup() == -1 ){
            DialogUtils.showAlertDialog(getActivity(), "Yes", "No",
                    "Do you have a GST number?", new DialogUtils.DialogClickListener() {
                        @Override
                        public void positiveButtonClick() {
                            MyApplication.setShowGstPopup(1);
                            sendGstUpdateStatus(1);
                            MyApplication.setGSTFilled();
                            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
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
            DialogUtils.startProgressDialog(getActivity(), "");
            ApiInterface apiService =
                    ApiClient.getClient(getActivity()).create(ApiInterface.class);

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
                            DialogUtils.showToast(getActivity(), "Failed update GST");
                        }

                    } catch (JSONException e) {
                        DialogUtils.showToast(getActivity(), "Failed update GST");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(getActivity(), "Failed update profile to server");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void isGSTVerified() {
//    boolean test = MyApplication.getIsGSTVeeditProfilerifies();
        if (!MyApplication.getIsGSTVerifies()) {
            DialogUtils.showAlertDialog(getActivity(), "Yes", "No",
                    "Please verify GST no. Press yes to proceed ?", new DialogUtils.DialogClickListener() {
                        @Override
                        public void positiveButtonClick() {
                            Intent intent = new Intent(getActivity(), LedgerLoginActivity.class);
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
        boolean installed = appInstallOrNot("com.whatsapp");
        String mobNo = "9289252155" ;
        if(installed)
        {
            Intent intent= new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+"+91"+mobNo));
            startActivity(intent);
        }
        else
        {
            Toast.makeText(getActivity(),"Please Install Whatsapp", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean appInstallOrNot(String url)
    {
        PackageManager packageManager = getActivity().getPackageManager();
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

    public void startYoutubeActivity(View v){
        Util.postEvents("Watch Demo","Watch Demo",getActivity().getApplicationContext());
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
