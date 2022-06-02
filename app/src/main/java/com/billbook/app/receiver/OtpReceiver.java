package com.billbook.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.EditText;

public class OtpReceiver extends BroadcastReceiver {

    private static EditText editText;

    public void setEditText(EditText editText)
    {
        OtpReceiver.editText = editText;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

        for(SmsMessage sms: messages)
        {
            String message = sms.getMessageBody();
            String otp = message.split("is")[2];

            editText.setText(otp.trim());
        }

    }
}