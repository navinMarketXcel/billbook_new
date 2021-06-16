package com.billbook.app.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.billbook.app.R;
import com.billbook.app.database.models.User;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    Button btn_submit;
    TextInputLayout input_layout_new_password, input_layout_confirm_password,
            input_layout_old_password;
    EditText input_password;
    String str_old_password, str_new_password, str_confirm_password;
    String TAG = "ChangePasswordActivity";

    private void init() {

        input_layout_old_password = findViewById(R.id.input_layout_old_password);
        input_layout_new_password = findViewById(R.id.input_layout_new_password);
        input_layout_confirm_password = findViewById(R.id.input_layout_confirm_password);
        input_password = findViewById(R.id.input_password);
        btn_submit = findViewById(R.id.btn_submit);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        init();

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputInformation()) {
                    doChangePassword(str_confirm_password);
                } else {

                }
            }
        });

        input_password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if (validateInputInformation()) {
                        doChangePassword(str_new_password);
                    } else {

                    }
                    return true;
                }
                return false;
            }
        });

//    Util.sendWhatsAppMessage("9096603564",ChangePasswordActivity.this,null);


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    // tool bar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void doChangePassword(String password) {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        int userId = sharedPref.getInt(getString(R.string.login_user_id), 0);
        progressDialog = DialogUtils.startProgressDialog(ChangePasswordActivity.this, "");
        String token = MyApplication.getUserToken();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", token);
        headerMap.put("Content-Type", "application/json");

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);
        Call<User> call = null;
        JSONObject req = new JSONObject();
        try {
            req.put("password", password);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), req.toString());
            call = apiService.patchUser(headerMap, userId, body);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    DialogUtils.showToast(ChangePasswordActivity.this, "Password updated successfully");
                } else {
                    DialogUtils.showToast(ChangePasswordActivity.this, "Failed to update the password");
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    private boolean validateInputInformation() {

        str_old_password = input_layout_old_password.getEditText().getText().toString();
        str_new_password = input_layout_new_password.getEditText().getText().toString();
        str_confirm_password = input_layout_confirm_password.getEditText().getText().toString();

        boolean result = true;

        if (str_old_password.equals("")) {
            input_layout_old_password.setError(getString(R.string.error_blank_old_password));
            result = false;
        } else if (!Util.passwordValidator(str_old_password)) {
            input_layout_old_password.setError(getResources().getString(R.string.pswd_error));
            result = false;
        } else {
            input_layout_old_password.setErrorEnabled(false);
        }

        if (str_new_password.equals("")) {
            input_layout_new_password.setError(getString(R.string.error_blank_new_password));
            result = false;
        } else if (!Util.passwordValidator(str_new_password)) {
            input_layout_new_password.setError(getResources().getString(R.string.pswd_error));
            result = false;
        } else {
            input_layout_new_password.setErrorEnabled(false);
        }

        if (str_confirm_password.equals("")) {
            input_layout_confirm_password.setError(getString(R.string.error_blank_confirm_password));
            result = false;
        } else if (!Util.passwordMatch(str_new_password, str_confirm_password)) {
            input_layout_confirm_password.setError(getString(R.string.error_match_password));
            result = false;
        } else {
            input_layout_confirm_password.setErrorEnabled(false);
        }
        return result;
    }


}
