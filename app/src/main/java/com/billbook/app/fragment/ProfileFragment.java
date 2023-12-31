package com.billbook.app.fragment;

import static android.view.View.GONE;

import static com.billbook.app.utils.Util.getRequestBodyFormData;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;
import com.billbook.app.activities.BusinessDetailsActivity;
import com.billbook.app.activities.ContactDetailsActivity;
import com.billbook.app.activities.LanguageChooseActivity;
import com.billbook.app.activities.LoginActivity;
import com.billbook.app.activities.LogoSignatureActivity;
import com.billbook.app.activities.MyApplication;
import com.billbook.app.activities.SplashActivity;
import com.billbook.app.activities.TermsAndConditionActivity;
import com.billbook.app.activities.TermsAndConditionsNewActivity;
import com.billbook.app.activities.loginPick_activity;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private JSONObject profile;
    int isGst;
    private long userid;
    public ProfileFragment() {
        // Required empty public constructor
    }
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    RelativeLayout rlContact,rlBusiness,rlLogoSign,rlChangeLanguage,rlLogout,tcrl;
    TextView txtProfileName,txtProfileAdd;
    de.hdodenhof.circleimageview.CircleImageView ivUserProfile;
    Switch switchGst;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tcrl = view.findViewById(R.id.tcrl);
        rlContact =  view.findViewById(R.id.rlContact);
        rlBusiness =  view.findViewById(R.id.rlBusiness);
        rlLogoSign =  view.findViewById(R.id.rlLogoSign);
        rlLogout =  view.findViewById(R.id.rlLogout);
        rlChangeLanguage =  view.findViewById(R.id.rlChangeLanguage);
        txtProfileName = view.findViewById(R.id.txtProfileName);
        txtProfileAdd = view.findViewById(R.id.txtProfileAdd);
        ivUserProfile = view.findViewById(R.id.ivUserProfile);
        switchGst = view.findViewById(R.id.switchGst);
        switchGst.setChecked(false);
//        try {
//            profile = new JSONObject(((MyApplication) getActivity().getApplication()).getUserDetails());
//            userid = profile.getLong("userid");
//            Log.v("Profile ", profile.toString());
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        setonClick();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.dailyLogout((AppCompatActivity) getActivity());

        try {
            profile = new JSONObject(((MyApplication) getActivity().getApplication()).getUserDetails());
            userid = profile.getLong("userid");
            Log.v("Profile ", profile.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        setUserData();
    }

    public void setonClick(){
        tcrl.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Terms and Conditions");
            Intent intent = new Intent(getActivity(), TermsAndConditionsNewActivity.class);
            startActivity(intent);
        });
        rlContact.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Contact Details");
            Intent intent = new Intent(getActivity(), ContactDetailsActivity.class);
            startActivity(intent);
        });
        rlBusiness.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Business Details");
            Intent intent = new Intent(getActivity(), BusinessDetailsActivity.class);
            startActivity(intent);
        });
        rlLogoSign.setOnClickListener(v -> {
            Util.pushEvent("Clicked on LogoSignature");
            Intent intent = new Intent(getActivity(), LogoSignatureActivity.class);
            startActivity(intent);
        });
        rlChangeLanguage.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Language");
            Intent intent = new Intent(getActivity(), LanguageChooseActivity.class);
            startActivity(intent);
        });
        rlLogout.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Logout");
            DialogUtils.showAlertDialog(getActivity(), "YES", "NO", "Are you sure you want to Logout?", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    Util.pushEvent("Clicked on YES for logout");
                    Intent intent = new Intent(getActivity(), loginPick_activity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Util.clearAllTables(getActivity());
                    getActivity().finish();
                    MyApplication.saveUserDetails("");
                    MyApplication.setHaveGst(true);
                    ApiInterface apiService =
                            ApiClient.getClient(getActivity()).create(ApiInterface.class);
                    Map<String, String> headerMap = new HashMap<>();
                    Call<Object> call = null;
                    call = apiService.logoutUser(headerMap, new JsonObject());
//                    call.enqueue(new Callback<Object>() {
//                        @Override
//                        public void onResponse(Call<Object> call, Response<Object> response) {
//                            try {
//                                JSONObject body = new JSONObject(new Gson().toJson(response.body()));
//                                String res = body.getString("msg");
//                                Log.v("res of logout user",res);
//                                Toast.makeText(getContext(),res,Toast.LENGTH_SHORT).show();
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<Object> call, Throwable t) {
//
//                        }
//                    });
                }
                @Override
                public void negativeButtonClick() {
                    Util.pushEvent("Clicked on No for logout");
                }
            });
//            Intent intent = new Intent(getActivity(), loginPick_activity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//            getActivity().finish();
//            MyApplication.saveUserDetails("");
        });


        switchGst.setOnClickListener(v -> {
            Util.pushEvent("Clicked on GST Toggle");

            if (profile.has("gstNo")) {
                String GstNo = null;
                try {
                    GstNo = profile.getString("gstNo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (GstNo.isEmpty()) {
                    switchGst.setChecked(false);
                    MyApplication.setIsGst(false);
                    Toast.makeText(getContext(), "Please Update Gst No", Toast.LENGTH_SHORT).show();

                    return;
                }
            }
            else
            {
                switchGst.setChecked(false);
                MyApplication.setIsGst(false);
                Toast.makeText(getContext(), "Please Update Gst No", Toast.LENGTH_SHORT).show();

                return;
            }

            if(switchGst.isChecked()){
                try {
                    updateUserAPI("1",true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    updateUserAPI("0",false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setUserData() {

        try {
            txtProfileName.setText(profile.has("shopName") ? profile.getString("shopName") : "");
            txtProfileAdd.setText(profile.has("shopAddr") ? profile.getString("shopAddr") : "");

            if (profile.has("companyLogo")) {
                String companyLogoPath = profile.getString("companyLogo");
                companyLogoPath = companyLogoPath.replaceAll("\\/", "/");


                Picasso.get()
                        .load(companyLogoPath)
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .into(ivUserProfile);
            }
            else
            {
                ivUserProfile.setBackground(getResources().getDrawable(R.drawable.store));
            }
            if (profile.has("gstNo")) {
                String GstNo=profile.getString("gstNo");
                if(!GstNo.isEmpty() && MyApplication.getIsGst()){
                    switchGst.setChecked(true);
                }else{
                    switchGst.setChecked(false);
                }
            }else{
                switchGst.setChecked(false);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateUserAPI(String isGst,boolean checkGst) throws IOException {
        DialogUtils.startProgressDialog(getActivity(), "");
        ApiInterface apiService =
                ApiClient.getClient(getActivity()).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Map<String, RequestBody> map = new HashMap<>();
        map.put("isGST", getRequestBodyFormData(isGst));
        MultipartBody.Part profileFilePart = null, companyFilePart = null, signatureFilePart = null;
        Call<Object> call = null;

        call = apiService.updateUser(headerMap, userid, profileFilePart, map, companyFilePart, signatureFilePart);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    if (body.getBoolean("status")) {
                        MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                        MyApplication.saveUserToken(body.getJSONObject("data").getString("userToken"));
                        MyApplication.setIsGst(checkGst);

                    } else {
                        DialogUtils.showToast(getActivity(), "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(getActivity(), "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(getActivity(), "Failed update profile to server");
            }
        });
    }
}
