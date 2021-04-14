package com.billbook.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

public class EditProfileActivity extends AppCompatActivity {
    private final String TAG = "EditProfile";
    private Handler handler = new Handler();
    private EditText emailEdt, addressEdt, phoneNoEdt, pincode, firstNameOfDealerEdt, lastNameOfDealerEdt,
            ledgerPasswordEdt, shopNameEdt, gstNoEdt, ledgerNewPasswordEdt, ledgerConfirmPassword;
    private JSONObject profile;
    private ImageView profileImg;
//    private SearchableSpinner states,city;
    private EditText states, cityEdt;
    private String picturePath;
    private ArrayList<String> stateList = new ArrayList<>();
    private ArrayList<String> citiesList = new ArrayList<>();
    private ArrayAdapter<String> cityAdapater;
    private ArrayAdapter<String> stateAdapater;
    private JSONArray citiesJSONArray = null;
    private final int RESULT_LOAD_IMAGE=1;
    private final int OTP_VERIFIED=2;

    private long userid;
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
            profile= new JSONObject (((MyApplication)getApplication()).getUserDetails());
            Log.v("Profile ",profile.toString());
            Log.v("token ",((MyApplication)getApplication()).getUserToken());

        } catch (JSONException e) {
            e.printStackTrace();
        }
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


        profileImg = findViewById(R.id.profileImage);

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
            emailEdt.setText(profile.has("email")?profile.getString("email"):"");
            addressEdt.setText(profile.has("shopAddr")?profile.getString("shopAddr"):"");
            shopNameEdt.setText(profile.has("shopName")?profile.getString("shopName"):"");
            phoneNoEdt.setText(profile.has("mobileNo")?profile.getString("mobileNo"):"");
            gstNoEdt.setText(profile.has("gstNo")?profile.getString("gstNo"):"");
            pincode.setText(profile.has("pincode")?profile.getString("pincode"):"");
            states.setText(profile.getString("state"));
            cityEdt.setText(profile.getString("city"));
            userid = profile.getLong("userid");
            if(profile.has("profileImage") && profile.getString("profileImage") != null)
            Picasso.get()
                    .load(profile.getString("profileImage"))
                    .placeholder(R.drawable.man_new)
                    .error(R.drawable.man_new)
                    .resize(70, 70)
                    .centerCrop()
                    .into(profileImg);
//            states.setSelection(stateList.indexOf(profile.getString("state")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateUserProfile(View v) {
        if(verifyData()) {
            try {
                if(profile.getString("mobileNo").equals(phoneNoEdt.getText().toString())){
                    updateUserProfile();
                }else{
                    startOTPActivity();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
    private void startOTPActivity(){
        try {
        Intent intent = new Intent(this,OTPActivity.class);
        intent.putExtra("fromEditProfile",true);
        intent.putExtra("mobileNo",profile.getString("mobileNo"));
        intent.putExtra("OTP","");
        startActivityForResult(intent,OTP_VERIFIED);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void updateUserProfile(){
        Util.postEvents("Update Profile","Update Profile",EditProfileActivity.this.getApplicationContext());
        try {
            profile.put("email",emailEdt.getText().toString());
            profile.put("shopAddr",addressEdt.getText().toString());
            profile.put("shopName",shopNameEdt.getText().toString());
            profile.put("mobileNo",phoneNoEdt.getText().toString());
            profile.put("gstNo",gstNoEdt.getText().toString());

            if (!gstNoEdt.getText().toString().isEmpty()) {
                sendGstUpdateStatus(2);
                MyApplication.setShowGstPopup(2);
            }else{
                int k = MyApplication.showGstPopup();
                // k = 2 User was GST user but now what ?
                // is he still gst user or wants to be gst user and not enter gst status
                sendGstUpdateStatus(k == 2 ? 1 : k);
                MyApplication.setShowGstPopup(k == 2 ? 1 : k);
            }

            profile.put("state",states.getText().toString());
            profile.put("city", cityEdt.getText().toString());
            MyApplication.saveUserDetails(profile.toString());
            if(picturePath==null)
                updateUserAPI();
            else{
                compressImage();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private boolean verifyData(){
        if(shopNameEdt.getText().toString().isEmpty()){
           DialogUtils.showToast(this,"Shop Name can not be empty");
            return false;
        }
        else if(addressEdt.getText().toString().isEmpty()){
            DialogUtils.showToast(this,"Shop Address can not be empty");
            return false;
        }
        else if(phoneNoEdt.getText().toString().isEmpty() || phoneNoEdt.getText().toString().length()<10){
            DialogUtils.showToast(this,"Invalid phone number");
            return false;
        }
        else if(states.getText().toString().isEmpty()){
            DialogUtils.showToast(this,"State can not be empty");
            return false;
        }
        else if(cityEdt.getText().toString().isEmpty()){
            DialogUtils.showToast(this,"City can not be empty");
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

    public void openGallery(View v){
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor == null || cursor.getCount() < 1) {
                return; // no cursor or no record. DO YOUR ERROR HANDLING
            }

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            if(columnIndex < 0) // no column index
                return; // DO YOUR ERROR HANDLING

             picturePath = cursor.getString(columnIndex);

            cursor.close(); // close cursor

            Log.v(TAG,"Path ->"+picturePath);

            Picasso.get()
                    .load(selectedImage)
                    .resize(70, 70)
                    .centerCrop()
                    .into(profileImg);
        }else if(requestCode == OTP_VERIFIED && resultCode == RESULT_OK
                && null != data){
            if(data.getBooleanExtra("OTP_VERIFIED",false))
            updateUserProfile();
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


    private void updateUserAPI(){
        if(picturePath != null){
            DialogUtils.startProgressDialog(this, "");
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            Map<String, String> headerMap = new HashMap<>();
            File file= new File(picturePath);
            RequestBody requestBody =RequestBody.create(MediaType.parse("*/*"), file);
            RequestBody email = RequestBody.create(MediaType.parse("multipart/form-data"), emailEdt.getText().toString());
            RequestBody shopAddr = RequestBody.create(MediaType.parse("multipart/form-data"), addressEdt.getText().toString());
            RequestBody shopName = RequestBody.create(MediaType.parse("multipart/form-data"), shopNameEdt.getText().toString());
            RequestBody mobileNo = RequestBody.create(MediaType.parse("multipart/form-data"), phoneNoEdt.getText().toString());
            RequestBody gstNo = RequestBody.create(MediaType.parse("multipart/form-data"), gstNoEdt.getText().toString());
            RequestBody state = RequestBody.create(MediaType.parse("multipart/form-data"), states.getText().toString());
            RequestBody city = RequestBody.create(MediaType.parse("multipart/form-data"), cityEdt.getText().toString());

            Map<String, RequestBody> map = new HashMap<>();
            map.put("email",email);
            map.put("shopAddr",shopAddr);
            map.put("shopName",shopName);
            map.put("mobileNo",mobileNo);
            map.put("gstNo",gstNo);
            map.put("state",state);
            map.put("city",city);

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(),
                    requestBody);
            Call<Object> call = apiService.updateUser(headerMap, userid,filePart,map);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP",body.toString());
                        if(body.getBoolean("status")){
                            MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                            EditProfileActivity.this.finish();
                        }else{
                            DialogUtils.showToast(EditProfileActivity.this,"Failed update profile to server");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        DialogUtils.showToast(EditProfileActivity.this,"Failed update profile to server");
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(EditProfileActivity.this,"Failed update profile to server");
                }
            });
        }else{
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


            Map<String, RequestBody> map = new HashMap<>();
            map.put("email",email);
            map.put("shopAddr",shopAddr);
            map.put("shopName",shopName);
            map.put("mobileNo",mobileNo);
            map.put("gstNo",gstNo);
            map.put("state",state);
            map.put("city",city);


            Call<Object> call = apiService.updateUser(headerMap, userid,null,map);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    DialogUtils.stopProgressDialog();
                    try {
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        Log.v("RESP",body.toString());
                        if(body.getBoolean("status")){
                            MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                            EditProfileActivity.this.finish();
                        }else{
                            DialogUtils.showToast(EditProfileActivity.this,"Failed update profile to server");
                        }

                    } catch (JSONException e) {
                        DialogUtils.showToast(EditProfileActivity.this,"Failed update profile to server");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    DialogUtils.stopProgressDialog();
                    DialogUtils.showToast(EditProfileActivity.this,"Failed update profile to server");
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults != null && grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                   DialogUtils.showToast(this,"Permission granted");
                } else {
                    // Permission Denied
                    DialogUtils.showToast(this, "WRITE_EXTERNAL Permission Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void checkPermission(){
        int hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }

    private void compressImage(){
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


}
