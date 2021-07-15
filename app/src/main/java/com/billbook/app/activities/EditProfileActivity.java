package com.billbook.app.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class EditProfileActivity extends AppCompatActivity {
    private final String TAG = "EditProfile";
    private Handler handler = new Handler();
    private EditText etAdditionalDetails, emailEdt, addressEdt, phoneNoEdt, pincode, firstNameOfDealerEdt, lastNameOfDealerEdt,
            ledgerPasswordEdt, shopNameEdt, gstNoEdt, ledgerNewPasswordEdt, ledgerConfirmPassword;
    private JSONObject profile;
    private LinearLayout ll_signature, ll_company;
    private ImageView profileImg, signatureImg, companyLogoImg;
    //    private SearchableSpinner states,city;
    private Button btnAddCompanyLogo, btnAddSignature, btnDeleteCompanyLogo, btnDeleteSignatureImage;
    private EditText states, cityEdt;
    private String picturePath;
    private TextView tvCompanyTitle, tvSignatureTitle;
    private ArrayList<String> stateList = new ArrayList<>();
    private ArrayList<String> citiesList = new ArrayList<>();
    private ArrayAdapter<String> cityAdapater;
    private ArrayAdapter<String> stateAdapater;
    private JSONArray citiesJSONArray = null;
    private final int RESULT_LOAD_IMAGE = 1;
    private final int OTP_VERIFIED = 2;
    private int whereToShowImage = -1;
    private Uri companyImagePath = null;
    private Uri signatureImagePath = null;
    private long userid;
    private int SCREEN_WIDTH = 120;
    private final double MAX_FILE_SIZE_LIMIT = 15.0;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setTitle("Profile");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initUI();
        checkPermission();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initAdapter() {
        /*citiesList.add("Select City");
        cityAdapater =  new ArrayAdapter<String>(
                EditProfileActivity.this,
                R.layout.spinner_item, citiesList);
        city.setAdapter(cityAdapater);
        stateAdapater =  new ArrayAdapter<String>(
                EditProfileActivity.this,
                R.layout.spinner_item, stateList);
        states.setAdapter(stateAdapater);*/
    }

    private void initUI() {
        try {
            profile = new JSONObject(((MyApplication) getApplication()).getUserDetails());
            Log.v("Profile ", profile.toString());
            Log.v("token ", ((MyApplication) getApplication()).getUserToken());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        etAdditionalDetails = findViewById(R.id.etAdditionalDetails);
        btnAddCompanyLogo = findViewById(R.id.btn_addCompanyLogo);
        btnAddSignature = findViewById(R.id.btn_addSignature);
        signatureImg = findViewById(R.id.iv_signature);
        companyLogoImg = findViewById(R.id.iv_company);
        emailEdt = findViewById(R.id.emailId);
        addressEdt = findViewById(R.id.address);
        phoneNoEdt = findViewById(R.id.phoneno);
        firstNameOfDealerEdt = findViewById(R.id.firstName);
        lastNameOfDealerEdt = findViewById(R.id.lastName);
        ledgerPasswordEdt = findViewById(R.id.ledgerPassword);
        ledgerConfirmPassword = findViewById(R.id.ledgerConfirmPassword);
        ledgerNewPasswordEdt = findViewById(R.id.ledgerNewPassword);
        shopNameEdt = findViewById(R.id.shopName);
        gstNoEdt = findViewById(R.id.gstNo);
        pincode = findViewById(R.id.pincode);
        states = findViewById(R.id.state);
        cityEdt = findViewById(R.id.city);
        tvCompanyTitle = findViewById(R.id.tv_companyLogo);
        tvSignatureTitle = findViewById(R.id.tv_signature);

        profileImg = findViewById(R.id.profileImage);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        SCREEN_WIDTH = displayMetrics.widthPixels;

        ll_company = findViewById(R.id.ll_company);
        ll_signature = findViewById(R.id.ll_signature);
        btnDeleteCompanyLogo = findViewById(R.id.bt_company_delete);
        btnDeleteSignatureImage = findViewById(R.id.bt_signature_delete);
       /* city.setTitle("Select City");
        states.setTitle("Select State");
        stateList.addAll( Arrays.asList(getResources().getStringArray(R.array.states)));

        states.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position ==0){
                    citiesList.clear();
                    citiesList.add("Select City");
                    cityAdapater.notifyDataSetChanged();
                } else{
                    loadCities();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        initAdapter();*/
        setUserData();
    }

    private void loadCities() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DialogUtils.startProgressDialog(EditProfileActivity.this, "");
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    citiesList.clear();
                    citiesList.add("Select City");
                    if (citiesJSONArray == null)
                        citiesJSONArray = new JSONArray(readJSONFromAsset());
                    for (int i = 0; i < citiesJSONArray.length(); i++) {
//                        if (stateList.get(states.getSelectedItemPosition()).equalsIgnoreCase(
//                                citiesJSONArray.getJSONObject(i).getString("state"))) {
//                            citiesList.add(citiesJSONArray.getJSONObject(i).getString("name"));
//                        }
                        Collections.sort(citiesList, String.CASE_INSENSITIVE_ORDER);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                cityAdapater.notifyDataSetChanged();
                try {
                    cityEdt.setSelection(citiesList.indexOf(profile.getString("city")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                DialogUtils.stopProgressDialog();
            }


        }.execute();
    }

    private void setUserData() {

        try {
            emailEdt.setText(profile.has("email") ? profile.getString("email") : "");
            addressEdt.setText(profile.has("shopAddr") ? profile.getString("shopAddr") : "");
            shopNameEdt.setText(profile.has("shopName") ? profile.getString("shopName") : "");
            phoneNoEdt.setText(profile.has("mobileNo") ? profile.getString("mobileNo") : "");
            gstNoEdt.setText(profile.has("gstNo") ? profile.getString("gstNo") : "");
            pincode.setText(profile.has("pincode") ? profile.getString("pincode") : "");
            states.setText(profile.getString("state"));
            cityEdt.setText(profile.getString("city"));
            userid = profile.getLong("userid");
            etAdditionalDetails.setText(profile.has("additionalData") ? profile.getString("additionalData") : "");

            if (profile.has("profileImage") && profile.getString("profileImage") != null) {
                String profileImageLink = profile.getString("profileImage");
                profileImageLink = profileImageLink.replaceAll("\\/", "/");
                Picasso.get()
                        .load(profile.getString("profileImage").replace("\\/","/"))
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .resize(70, 70)
                        .centerCrop()
                        .into(profileImg);
            }

            if (profile.has("companyLogo")) {
                String companyLogoPath = profile.getString("companyLogo");
                companyLogoPath = companyLogoPath.replaceAll("\\/", "/");

                ll_company.setVisibility(View.VISIBLE);
                btnDeleteCompanyLogo.setVisibility(View.VISIBLE);
                btnAddCompanyLogo.setVisibility(GONE);
                tvCompanyTitle.setVisibility(View.VISIBLE);
                companyLogoImg.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load(companyLogoPath)
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .into(companyLogoImg);
            }

            if (profile.has("signatureImage")) {
                String profilePath = profile.getString("signatureImage");
                profilePath = profilePath.replaceAll("\\/", "/");

                btnDeleteSignatureImage.setVisibility(View.VISIBLE);
                btnAddSignature.setVisibility(GONE);
                tvSignatureTitle.setVisibility(View.VISIBLE);
                signatureImg.setVisibility(View.VISIBLE);
                ll_signature.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load(profilePath)
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .into(signatureImg);
            }
//            states.setSelection(stateList.indexOf(profile.getString("state")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void showImageOptions(View v) {
        whereToShowImage = v.getTag().toString().equals("company_logo") ? 0 : 1;
        openGallery();
    }

    public void launchImageEditActivity(Uri uri) {
        //Use library for image editing options ...
    }

    public void updateUserProfile(View v) {
        if (verifyData()) {
            try {
                if (profile.getString("mobileNo").equals(phoneNoEdt.getText().toString())) {
                    updateUserProfile();
                } else {
                    startOTPActivity();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void startOTPActivity() {
        try {
            Intent intent = new Intent(this, OTPActivity.class);
            intent.putExtra("fromEditProfile", true);
            intent.putExtra("mobileNo", profile.getString("mobileNo"));
            intent.putExtra("OTP", "");
            startActivityForResult(intent, OTP_VERIFIED);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUserProfile() {
        Util.postEvents("Update Profile", "Update Profile", EditProfileActivity.this.getApplicationContext());
        try {
            profile.put("email", emailEdt.getText().toString());
            profile.put("shopAddr", addressEdt.getText().toString());
            profile.put("shopName", shopNameEdt.getText().toString());
            profile.put("mobileNo", phoneNoEdt.getText().toString());
            profile.put("gstNo", gstNoEdt.getText().toString());
            if (!gstNoEdt.getText().toString().isEmpty()) {
                sendGstUpdateStatus(2);
                MyApplication.setShowGstPopup(2);
            } else {
                int k = MyApplication.showGstPopup();
                // k = 2 User was GST user but now what ?
                // is he still gst user or wants to be gst user and not enter gst status
                sendGstUpdateStatus(k == 2 ? 1 : k);
                MyApplication.setShowGstPopup(k == 2 ? 1 : k);
            }

            profile.put("state", states.getText().toString());
            profile.put("city", cityEdt.getText().toString());
            MyApplication.saveUserDetails(profile.toString());
            if (picturePath == null)
                updateUserAPI();
            else {
                compressImage();
            }
        } catch (JSONException | IOException e) {
//            Log.d(TAG, "updateUserProfile: saldfkjalsdkjflkj FIle not found ....");
            e.printStackTrace();
        }
    }

    private boolean verifyData() {
        if (shopNameEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Shop Name can not be empty");
            return false;
        } else if (addressEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "Shop Address can not be empty");
            return false;
        } else if (phoneNoEdt.getText().toString().isEmpty() || phoneNoEdt.getText().toString().length() < 10) {
            DialogUtils.showToast(this, "Invalid phone number");
            return false;
        } else if (states.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "State can not be empty");
            return false;
        } else if (cityEdt.getText().toString().isEmpty()) {
            DialogUtils.showToast(this, "City can not be empty");
            return false;
        }
        return true;
    }

    public String readJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            Log.v(TAG, json);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }

    public void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg"};
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor == null || cursor.getCount() < 1) {
                if (cursor != null) {
                    DialogUtils.showToast(EditProfileActivity.this, "Unable to select image");
//                    Log.d(TAG, "onActivityResult: NUll cursor....." + cursor.getCount());
                } else {
                    DialogUtils.showToast(EditProfileActivity.this, "Unable to select image");
//                    Log.d(TAG, "onActivityResult: Cursor is null");
                }
                return; // no cursor or no record. DO YOUR ERROR HANDLING
            }

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            if (columnIndex < 0) // no column index
            {
                DialogUtils.showToast(EditProfileActivity.this, "Not supported application");
//                Log.d(TAG, "onActivityResult: Column index going brr....");
                return; // DO YOUR ERROR HANDLING
            }
            else {
                String path = cursor.getString(columnIndex);

                cursor.close(); // close cursor

                File selectedImageFile = new File(path);

                if (selectedImageFile.exists() && Util.checkFileSizeInMB(selectedImageFile, MAX_FILE_SIZE_LIMIT)) {
//                launchImageEditActivity(selectedImage);
                    setImage(selectedImage);
                } else {
                    DialogUtils.showToast(EditProfileActivity.this, "Max file size limit " + (int) MAX_FILE_SIZE_LIMIT + "MB");
                }
//            Log.v(TAG,"Path ->"+picturePath);
//
//            Picasso.get()
//                    .load(selectedImage)
//                    .resize(70, 70)
//                    .centerCrop()
//                    .into(profileImg);
            }
        } else if (requestCode == OTP_VERIFIED && resultCode == RESULT_OK
                && null != data) {
            if (data.getBooleanExtra("OTP_VERIFIED", false))
                updateUserProfile();
        }
    }

    private void setImage(Uri uri) {
        ImageView loadImageHere = null;
        if (whereToShowImage == 0) {
            ll_company.setVisibility(View.VISIBLE);
            tvCompanyTitle.setVisibility(View.VISIBLE);
            btnDeleteCompanyLogo.setVisibility(GONE);
            btnAddCompanyLogo.setVisibility(GONE);
            loadImageHere = companyLogoImg;
            companyImagePath = uri;
        } else if (whereToShowImage == 1) {
            ll_signature.setVisibility(View.VISIBLE);
            tvSignatureTitle.setVisibility(View.VISIBLE);
            btnDeleteSignatureImage.setVisibility(GONE);
            btnAddSignature.setVisibility(GONE);
            loadImageHere = signatureImg;
            signatureImagePath = uri;
        }

        if(loadImageHere!=null)
        {
            loadImageHere.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(uri)
                    .into(loadImageHere);
        }
    }

    private void sendGstUpdateStatus(int gstStatus) {
        try {
//            DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            Map<String, Integer> map = new HashMap<>();
            map.put("isGST", gstStatus);
            long userid = profile.getLong("userid");
            Call<Object> call = apiService.updateUserGstStatus(userid, map);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
//                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP", body.toString());
                        if (body.getBoolean("status")) {
//                            MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                        } else {
                            DialogUtils.showToast(EditProfileActivity.this, "Failed update GST");
                        }

                    } catch (JSONException e) {
                        DialogUtils.showToast(EditProfileActivity.this, "Failed update GST");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
//                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(EditProfileActivity.this, "Failed update profile to server");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUserAPI() throws IOException {
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        RequestBody email = RequestBody.create(MediaType.parse("multipart/form-data"), emailEdt.getText().toString());
        RequestBody shopAddr = RequestBody.create(MediaType.parse("multipart/form-data"), addressEdt.getText().toString());
        RequestBody shopName = RequestBody.create(MediaType.parse("multipart/form-data"), shopNameEdt.getText().toString());
        RequestBody mobileNo = RequestBody.create(MediaType.parse("multipart/form-data"), phoneNoEdt.getText().toString());
        RequestBody gstNo = RequestBody.create(MediaType.parse("multipart/form-data"), gstNoEdt.getText().toString());
        RequestBody state = RequestBody.create(MediaType.parse("multipart/form-data"), states.getText().toString());
        RequestBody city = RequestBody.create(MediaType.parse("multipart/form-data"), cityEdt.getText().toString());
        RequestBody pincodeP = RequestBody.create(MediaType.parse("multipart/form-data"), pincode.getText().toString());
        RequestBody addData = RequestBody.create(MediaType.parse("multipart/form-data"), etAdditionalDetails.getText().toString());

        Map<String, RequestBody> map = new HashMap<>();
        map.put("email", email);
        map.put("shopAddr", shopAddr);
        map.put("shopName", shopName);
        map.put("mobileNo", mobileNo);
        map.put("gstNo", gstNo);
        map.put("pincode", pincodeP);
        map.put("state", state);
        map.put("city", city);
        map.put("additionalData", addData);

        File profileFile = null, companyFile = null, signatureFile = null;
        MultipartBody.Part profileFilePart = null, companyFilePart = null, signatureFilePart = null;

        ContentResolver cR = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        Call<Object> call = null;
        if (picturePath != null) {
            profileFile = new File(picturePath);
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), profileFile);

            profileFilePart = MultipartBody.Part.createFormData("profileImage", profileFile.getName(),
                    requestBody);
        }

        if (companyImagePath != null) {
            String type = mime.getExtensionFromMimeType(cR.getType(companyImagePath));
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(companyImagePath, "r", null);
            File newCompanyFile = new File(getCacheDir(), "companyLogo." + type);
            FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
            FileOutputStream fileOutputStream = new FileOutputStream(newCompanyFile);
            IOUtils.copyStream(fileInputStream, fileOutputStream);

//            companyFile = new File(companyImagePath);
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), newCompanyFile);

            companyFilePart = MultipartBody.Part.createFormData("companyLogo", newCompanyFile.getName(), requestBody);

        }
        if (signatureImagePath != null) {

            String type = mime.getExtensionFromMimeType(cR.getType(signatureImagePath));

            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(signatureImagePath, "r", null);
            File newSignatureFile = new File(getCacheDir(), "signature." + type);
            FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
            FileOutputStream fileOutputStream = new FileOutputStream(newSignatureFile);
            IOUtils.copyStream(fileInputStream, fileOutputStream);

//            signatureFile = new File(signatureImagePath);
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), newSignatureFile);
            signatureFilePart = MultipartBody.Part.createFormData("signatureImage", newSignatureFile.getName(),
                    requestBody);
        }


        RequestBody isImage= RequestBody.create(MediaType.parse("multipart/form-data"), "1");
        if(companyFilePart!=null)
        {
            map.put("isCompanyLogo", isImage);
        }
        if(signatureFilePart!=null)
        {
            map.put("isSignatureImage", isImage);
        }
        call = apiService.updateUser(headerMap, userid, profileFilePart, map, companyFilePart, signatureFilePart);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP", body.toString());
                    if (body.getBoolean("status")) {
                        MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                        EditProfileActivity.this.finish();
                    } else {
                        DialogUtils.showToast(EditProfileActivity.this, "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(EditProfileActivity.this, "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(EditProfileActivity.this, "Failed update profile to server");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    DialogUtils.showToast(this, "Permission granted");
                } else {
                    // Permission Denied
//                    DialogUtils.showToast(this, "WRITE_EXTERNAL Permission Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkPermission() {
        int hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }

    private void compressImage() {
//        DialogUtils.startProgressDialog(this,"compressing Image");
//        Tiny.getInstance().init(getApplication());
//        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
//        //options.height = xxx;//some compression configuration.
//        Tiny.getInstance().source(picturePath).asFile().withOptions(options).compress(new FileCallback() {
//            @Override
//            public void callback(boolean isSuccess, String outfile, Throwable t) {
//                //return the compressed file path
//                DialogUtils.stopProgressDialog();
//                picturePath=outfile;
//                updateUserAPI();
//            }
//        });
    }

    private void deletePic(String from) {

        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();

        RequestBody isPic = RequestBody.create(MediaType.parse("multipart/form-data"), "0");

        Map<String, RequestBody> map = new HashMap<>();
        map.put(from, isPic);

        Call<Object> call = null;

        call = apiService.updateUser(headerMap, userid, null, map, null, null);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                DialogUtils.stopProgressDialog();
                try {
                    JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                    Log.v("RESP", body.toString());
                    if (body.getBoolean("status")) {
                        MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                        recreate();
                    } else {
                        DialogUtils.showToast(EditProfileActivity.this, "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(EditProfileActivity.this, "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(EditProfileActivity.this, "Failed update profile to server");
            }
        });
    }

    public void onDelete(View v) {
        switch (v.getId()) {
            case R.id.bt_company_delete:
                deletePic("isCompanyLogo");
                break;
            case R.id.bt_signature_delete:
                deletePic("isSignatureImage");
                break;
            default:
                Log.i(TAG, "onDelete: HTAfslskj");

        }
    }
}
