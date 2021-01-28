package com.billbook.app.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity implements DialogUtils.DialogClickListener {
    private static final String TAG = "UserProfileActivity";
    private ImageView imgProfileImage;
    private TextView tvUserName, tvStoreAddress, tvAddress, tvPhone, tvEmailId, gstNo;
    private User user;
    private EditText edtGST;
    private Handler handler = new Handler();

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initUI();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                        getString(R.string.preference_file_key), MODE_PRIVATE);
                int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
                user = MyApplication.getDatabase().userDao().getUser(userId);
                Log.v(TAG, "user::" + user);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setUserData(user);
                    }
                });
            }
        }).start();

    }

    private void initUI() {
        imgProfileImage = findViewById(R.id.imgProfileImage);
        tvUserName = findViewById(R.id.tvUserName);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        tvStoreAddress = findViewById(R.id.tvStoreAddress);
        tvEmailId = findViewById(R.id.tvEmailId);
        gstNo = findViewById(R.id.gstNo);
        edtGST = findViewById(R.id.edtGST);
        edtGST.setOnEditorActionListener(new EditText.OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    //do your work here
                    user.setGst_no(edtGST.getText().toString());
                    new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.getDatabase().userDao().inserUser(user);
                        }
                    };
                    updateGST(edtGST.getText().toString());
                    return true;

                }

                return false;
            }
        });
    }

    private void setUserData(User user) {
        if (user != null) {
            if (user.getUsername() != null) {
                tvUserName.setText(user.getUsername());
            }
            if (user.getEmail() != null) {
                tvEmailId.setText(user.getEmail());
            }

            if (user.getMobile_no() != null) {
                tvPhone.setText(user.getMobile_no());
            }
            if (user.getAddress() != null) {
                tvAddress.setText(user.getAddress());
            }
            if (user.getShop_name() != null) {
                tvStoreAddress.setText(user.getShop_name());
            }
            if (user.getGst_no() == null && !user.isAskedForGST()) {

                DialogUtils.showAlertDialog(this, "Yes", "No", "Do You have GST number ?", this);
            } else {
                gstNo.setText(user.getGst_no());
            }
        }
    }

    @Override
    public void positiveButtonClick() {
        edtGST.setVisibility(View.VISIBLE);
        user.setAskedForGST(true);
        new Runnable() {
            @Override
            public void run() {
                MyApplication.getDatabase().userDao().inserUser(user);
            }
        };

        edtGST.setVisibility(View.VISIBLE);
        edtGST.setFocusable(true);
    }

    @Override
    public void negativeButtonClick() {
        user.setAskedForGST(true);
        new Runnable() {
            @Override
            public void run() {
                MyApplication.getDatabase().userDao().inserUser(user);
            }
        };
    }

    public void updateGST(final String gst) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
        final ProgressDialog progressDialog = DialogUtils.startProgressDialog(this, "");
        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");
        Map<String, String> req = new HashMap<>();
        req.put("gst_no", gst);
        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<User> call = apiService.updateUser(headerMap, userId, req);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User user = response.body();
                if (user != null) {

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.saveLoginUserID(user.getId());
                            MyApplication.getDatabase().userDao().inserUser(user);
                        }
                    });
                    DialogUtils.showToast(UserProfileActivity.this, "GST no updated successfully");
                    edtGST.setVisibility(View.GONE);
                    gstNo.setText(gst);
                } else {
                    DialogUtils.showToast(UserProfileActivity.this, "Failed to update the GST");
                }

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    public void startEditProfileActivity(View v) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        startActivity(intent);
    }
}
