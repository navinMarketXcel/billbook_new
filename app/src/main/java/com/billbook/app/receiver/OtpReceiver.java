package com.billbook.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.widget.EditText;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class OtpReceiver extends BroadcastReceiver {

    public SmsBroadcastListener smsBroadcastListener;
    @Override
    public void onReceive(Context context, Intent intent) {
//        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
//
//        for(SmsMessage sms: messages)
//        {
//            String message = sms.getMessageBody();
//            String otp = message.split("is")[2];
//
//            editText.setText(otp.trim());
//        }
        if(intent.getAction()== SmsRetriever.SMS_RETRIEVED_ACTION)
        {
            Bundle extra = intent.getExtras();
            Status smsRetreiveStatus = (Status) extra.get(SmsRetriever.EXTRA_STATUS);

            switch (smsRetreiveStatus.getStatusCode())
            {
                case CommonStatusCodes
                        .SUCCESS:
                    Intent messagaeIntent = extra.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    smsBroadcastListener.onSuccess(messagaeIntent);
                    break;
                    case CommonStatusCodes.TIMEOUT:
                        smsBroadcastListener.onFailure();
                        break;
            }
        }

    }

    public interface SmsBroadcastListener
    {
        void onSuccess(Intent intent);

        void onFailure();

    }
}