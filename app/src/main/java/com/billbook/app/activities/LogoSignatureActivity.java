package com.billbook.app.activities;

import static android.view.View.GONE;

import static com.billbook.app.utils.Util.getRequestBodyFormData;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoSignatureActivity extends AppCompatActivity {
    private final int RESULT_LOAD_IMAGE = 1;
    private int SCREEN_WIDTH = 120;
    private final double MAX_FILE_SIZE_LIMIT = 15.0;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    private String picturePath;
    ImageView ivToolBarBack,ivLogo,ivSetLogo,ivSetSign,ivDeleteSign,ivDeleteLogo;
    Button btnPickLogo,btnPickSign,btn_save;
    LinearLayout LnBrowseLogo,LnSetLogo,LnBrowseSign,LnSetSign,lnSave;
    private Uri companyImagePath = null;
    private Uri signatureImagePath = null;
    private int whereToShowImage = -1;
    private long userid;
    private JSONObject profile;
    LinearLayout lnHelp,lnYouTube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_signature);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        btnPickLogo = findViewById(R.id.btnPickLogo);
        btnPickSign = findViewById(R.id.btnPickSign);
        ivLogo = findViewById(R.id.ivLogo);
        ivSetLogo = findViewById(R.id.ivSetLogo);
        ivSetSign = findViewById(R.id.ivSetSign);
        ivDeleteLogo = findViewById(R.id.ivDeleteLogo);
        ivDeleteSign = findViewById(R.id.ivDeleteSign);
        LnBrowseLogo = findViewById(R.id.LnBrowseLogo);
        LnSetLogo = findViewById(R.id.LnSetLogo);
        LnBrowseSign = findViewById(R.id.LnBrowseSign);
        LnSetSign = findViewById(R.id.LnSetSign);
        btn_save = findViewById(R.id.btn_save);
        lnSave = findViewById(R.id.lnSave);
        lnHelp = findViewById(R.id.lnHelp);
        lnYouTube = findViewById(R.id.lnYouTube);
        try {
            profile = new JSONObject(((MyApplication) getApplication()).getUserDetails());
            userid = profile.getLong("userid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        checkPermission();
        setonClick();
        setUserData();
    }
    private void setUserData() {

        try {
           /* if (profile.has("profileImage") && profile.getString("profileImage") != null) {
                String profileImageLink = profile.getString("profileImage");
                profileImageLink = profileImageLink.replaceAll("\\/", "/");
                Picasso.get()
                        .load(profile.getString("profileImage").replace("\\/","/"))
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .resize(70, 70)
                        .centerCrop()
                        .into(profileImg);
            }*/

            if (profile.has("companyLogo")) {
                String companyLogoPath = profile.getString("companyLogo");
                companyLogoPath = companyLogoPath.replaceAll("\\/", "/");

                LnBrowseLogo.setVisibility(GONE);
                LnSetLogo.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load(companyLogoPath)
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .into(ivSetLogo);
            }

            if (profile.has("signatureImage")) {
                String profilePath = profile.getString("signatureImage");
                profilePath = profilePath.replaceAll("\\/", "/");

                LnBrowseSign.setVisibility(GONE);
                LnSetSign.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load(profilePath)
                        .placeholder(R.drawable.man_new)
                        .error(R.drawable.man_new)
                        .into(ivSetSign);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
        btnPickLogo.setOnClickListener(v -> {
            whereToShowImage=1;
            openGallery();
        });
        btnPickSign.setOnClickListener(v -> {
            whereToShowImage=0;
            openGallery();
        });
        ivDeleteLogo.setOnClickListener(v -> {
            LnBrowseLogo.setVisibility(View.VISIBLE);
            LnSetLogo.setVisibility(GONE);
            deletePic("isCompanyLogo");
        });
        ivDeleteSign.setOnClickListener(v -> {
            LnBrowseSign.setVisibility(View.VISIBLE);
            LnSetSign.setVisibility(GONE);
            deletePic("isSignatureImage");
        });
        btn_save.setOnClickListener(v -> {
            try {
                updateUserAPI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(LogoSignatureActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(LogoSignatureActivity.this);
        });
    }
    public void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg"};
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
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
                    DialogUtils.showToast(LogoSignatureActivity.this, "Unable to select image");
//                    Log.d(TAG, "onActivityResult: NUll cursor....." + cursor.getCount());
                } else {
                    DialogUtils.showToast(LogoSignatureActivity.this, "Unable to select image");
//                    Log.d(TAG, "onActivityResult: Cursor is null");
                }
                return; // no cursor or no record. DO YOUR ERROR HANDLING
            }

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            if (columnIndex < 0) // no column index
            {
                DialogUtils.showToast(LogoSignatureActivity.this, "Not supported application");
//                Log.d(TAG, "onActivityResult: Column index going brr....");
                return; // DO YOUR ERROR HANDLING
            }
            else {
                String path = cursor.getString(columnIndex);

                cursor.close(); // close cursor

                File selectedImageFile = new File(path);

                if (selectedImageFile.exists() && Util.checkFileSizeInMB(selectedImageFile, MAX_FILE_SIZE_LIMIT)) {
//                launchImageEditActivity(selectedImage);
                    lnSave.setVisibility(View.VISIBLE);
                    setImage(selectedImage);
                } else {
                    DialogUtils.showToast(LogoSignatureActivity.this, "Max file size limit " + (int) MAX_FILE_SIZE_LIMIT + "MB");
                }

            }
        }
    }
    private void setImage(Uri uri) {
        if(whereToShowImage==1) {
            LnBrowseLogo.setVisibility(GONE);
            LnSetLogo.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(uri)
                    .into(ivSetLogo);
            companyImagePath = uri;

        }else{
            LnBrowseSign.setVisibility(GONE);
            LnSetSign.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(uri)
                    .into(ivSetSign);
            signatureImagePath=uri;
        }
    }
    private void updateUserAPI() throws IOException {
        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Map<String, RequestBody> map = new HashMap<>();

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
                    if (body.getBoolean("status")) {
                        MyApplication.saveUserDetails(body.getJSONObject("data").toString());
                        MyApplication.saveUserToken(body.getJSONObject("data").getString("userToken"));
                        LogoSignatureActivity.this.finish();
                    } else {
                        DialogUtils.showToast(LogoSignatureActivity.this, "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(LogoSignatureActivity.this, "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(LogoSignatureActivity.this, "Failed update profile to server");
            }
        });
    }
    private void deletePic(String from) {

        DialogUtils.startProgressDialog(this, "");
        ApiInterface apiService =
                ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Map<String, RequestBody> map = new HashMap<>();
        map.put(from, getRequestBodyFormData("0"));

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
                        DialogUtils.showToast(LogoSignatureActivity.this, "Failed update profile to server");
                    }

                } catch (JSONException e) {
                    DialogUtils.showToast(LogoSignatureActivity.this, "Failed to send");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                DialogUtils.stopProgressDialog();
                DialogUtils.showToast(LogoSignatureActivity.this, "Failed update profile to server");
            }
        });
    }
}