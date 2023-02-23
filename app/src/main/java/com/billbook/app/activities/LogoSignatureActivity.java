package com.billbook.app.activities;

import static android.view.View.GONE;

import static com.billbook.app.utils.Util.getRequestBodyFormData;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoSignatureActivity extends AppCompatActivity {
    private final int RESULT_LOAD_IMAGE = 1;
    private final int RESULT_LOAD_IMAGE_CAMERA = 2;
    private int SCREEN_WIDTH = 120;
    private final double MAX_FILE_SIZE_LIMIT = 15.0;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    final private int REQUEST_CODE_ASK_CAMERA = 112;
    private String picturePath;
    private static final int PERMISSION_REQUEST_CODE = 200;

    ImageView ivToolBarBack,ivLogo,ivSetLogo,ivSetSign,ivDeleteSign,ivDeleteLogo;
    Button btnPickLogo,btnPickSign,btn_save;
    LinearLayout LnBrowseLogo,LnSetLogo,LnBrowseSign,LnSetSign,lnSave;
    private Uri companyImagePath = null;
    private Uri signatureImagePath = null;
    private int whereToShowImage = -1;
    private long userid;
    private JSONObject profile;
    LinearLayout lnHelp,lnYouTube;
    public String photoFileName = "photo.jpg";
    File photoFile;
    public final String APP_TAG = "BillBook";

    @RequiresApi(api = Build.VERSION_CODES.M)
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
        //checkPermission();
        checkAndRequestPermissions();
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
            boolean flag = checkAndRequestPermissions();
            Log.v("chcking permissiom", String.valueOf(flag));
           if(flag)
           {

               whereToShowImage=1;
               selectImage();

           }
           else
           {
               checkPermission();
           }

           // openGallery();

        });
        btnPickSign.setOnClickListener(v -> {
            boolean flag = checkAndRequestPermissions();
            if(flag)
            {
                whereToShowImage=0;
                selectImage();

            }
            else
            {
                checkPermission();
            }
        });
        ivDeleteLogo.setOnClickListener(v -> {
            DialogUtils.showAlertDialog(LogoSignatureActivity.this, "Yes", "No", "Are you sure you want to delete the Logo", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    LnBrowseLogo.setVisibility(View.VISIBLE);
                    LnSetLogo.setVisibility(GONE);
                    deletePic("isCompanyLogo");
                }

                @Override
                public void negativeButtonClick() {

                }
            });

        });
        ivDeleteSign.setOnClickListener(v -> {
            DialogUtils.showAlertDialog(LogoSignatureActivity.this, "Yes", "No", "Are you sure you want to delete the Signature?", new DialogUtils.DialogClickListener() {
                @Override
                public void positiveButtonClick() {
                    LnBrowseSign.setVisibility(View.VISIBLE);
                    LnSetSign.setVisibility(GONE);
                    deletePic("isSignatureImage");
                }

                @Override
                public void negativeButtonClick() {

                }
            });

        });
        btn_save.setOnClickListener(v -> {
            try {
                Util.pushEvent("Clicked on Save In Logo Signature");
                updateUserAPI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        lnHelp.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Whatsapp Help from Toolbar in LogoSignature");
            Util. startHelpActivity(LogoSignatureActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util.pushEvent("Clicked on Youtube Demo from Toolbar in Business Details");
            Util. startYoutubeActivity(LogoSignatureActivity.this);
        });
    }
   /* public void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg"};
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }*/
    private void selectImage() {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(LogoSignatureActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {


                        photoFileName = String.valueOf(System.currentTimeMillis());
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        photoFile = getPhotoFileUri(photoFileName);
                        Uri fileProvider = FileProvider.getUriForFile(LogoSignatureActivity.this, LogoSignatureActivity.this.getApplicationContext().getPackageName() + ".provider", photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            // Start the image capture intent to take photo
                            // startActivityForResult(intent, RESULT_LOAD_IMAGE_CAMERA);
                            cameraResultLauncher.launch(intent);

                    }

                }
                else if (options[item].equals("Choose from Gallery"))
                {


                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // startActivityForResult(intent, RESULT_LOAD_IMAGE);
                        galleryResultLauncher.launch(intent);

                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private boolean checkPermission() {
        int hasWriteStoragePermission =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                                REQUEST_CODE_ASK_PERMISSIONS);

                    }
                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_CAMERA);
            }
            return false;
        }
        return true;
    }
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if(photoFile!=null)
                    {
                        Uri selectedImage = Uri.fromFile(photoFile);
                        // Uri selectedImage = data.getData();
                        System.out.println("selectedImage"+selectedImage);
                        lnSave.setVisibility(View.VISIBLE);
                        setImage(selectedImage);
                    }
                }
            });
    ActivityResultLauncher<Intent> galleryResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                        Uri selectedImage = data.getData();
                        System.out.println("selectedImage gallery "+selectedImage);
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

                            if(path!=null)
                            {
                                File selectedImageFile = new File(path);
                                if (selectedImageFile.exists() && Util.checkFileSizeInMB(selectedImageFile, MAX_FILE_SIZE_LIMIT))
                                {
//                launchImageEditActivity(selectedImage);
                                    lnSave.setVisibility(View.VISIBLE);
                                    setImage(selectedImage);

                                }
                                else
                                {
                                    DialogUtils.showToast(LogoSignatureActivity.this, "Max file size limit " + (int) MAX_FILE_SIZE_LIMIT + "MB");
                                }
                            }

                        }

                }
            });


/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE_CAMERA && resultCode == RESULT_OK) {

           // File file = new File(Environment.getExternalStorageDirectory().getPath(), "photo.jpg");
            //Uri selectedImage = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", imgDir);
            if(photoFile!=null)
            {
                Uri selectedImage = Uri.fromFile(photoFile);
                // Uri selectedImage = data.getData();
                System.out.println("selectedImage"+selectedImage);
                lnSave.setVisibility(View.VISIBLE);
                setImage(selectedImage);
            }
        }
        else  if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {
            {
                Uri selectedImage = data.getData();
                System.out.println("selectedImage gallery "+selectedImage);
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

                    if(path!=null)
                    {
                        File selectedImageFile = new File(path);
                        if (selectedImageFile.exists() && Util.checkFileSizeInMB(selectedImageFile, MAX_FILE_SIZE_LIMIT))
                        {
//                launchImageEditActivity(selectedImage);
                            lnSave.setVisibility(View.VISIBLE);
                            setImage(selectedImage);

                        }
                        else
                        {
                            DialogUtils.showToast(LogoSignatureActivity.this, "Max file size limit " + (int) MAX_FILE_SIZE_LIMIT + "MB");
                        }
                    }

                }
            }

        }



}*/


    private void setImage(Uri uri) {
        if(whereToShowImage==1) {
            LnBrowseLogo.setVisibility(GONE);
            LnSetLogo.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(uri)
                    .into(ivSetLogo);
            companyImagePath = uri;
            Toast.makeText(this, "Image Uploaded Successfully", Toast.LENGTH_LONG).show();

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
                        DialogUtils.showToast(LogoSignatureActivity.this, "Upload logo & signature  successfully");
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS);
        int writeexternalpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int readexternalpermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        int camerapermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (writeexternalpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (readexternalpermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (camerapermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

}