package com.billbook.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.DialogUtils;

import java.util.Objects;

public class SendMarketingSMSActivity extends AppCompatActivity {
    private EditText messageToBeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_marketing_sms);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Send Message");
        messageToBeSent = findViewById(R.id.messageToBeSent);
    }

    public void Next(View v) {
        if (messageToBeSent.getText().toString().length() > 10)
            startActivity(new Intent(this, SelectSenderActivity.class));
        else
            DialogUtils.showToast(this, "Message should more than 10 characters");
    }
}
