package com.billbook.app.networkcommunication;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.billbook.app.R;


/**
 * Created by priyanka on 12/2/16.
 */
public class DialogUtils {
    private static final String TAG = "DialogUtils";
    private static ProgressDialog _pd;
    private static ProgressDialog progressDialog;


    public static ProgressDialog startProgressDialog(Context context, String message) {
        Log.v(TAG, "DialogUtils _pd::" + _pd);
        _pd = null;
        _pd = ProgressDialog.show(context, null, null);
        _pd.setContentView(R.layout.layout_progress_dialog);
        _pd.setMessage(message);
        _pd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        _pd.setCancelable(false);
        _pd.show();
        _pd.findViewById(R.id.dialogLoadingText).setVisibility(View.GONE);


        return _pd;
    }

    public static void stopProgressDialog() {
        if (_pd != null) {
            _pd.dismiss();
            _pd = null;
        }
    }


    public static void showToast(Context context, String text) {
        if(context!=null)
        {
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.custom_toast);
//        view.setPadding(7,7,7,7);
                TextView textTV = (TextView) view.findViewById(android.R.id.message);
                textTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
//            Shadow of the Of the Text Color
//        textTV.setShadowLayer(0, 0, 0, Color.GRAY);
                textTV.setTextColor(Color.WHITE);
//        textTV.setTextSize(Integer.valueOf(context.getResources().getString(R.string.text_size)));
            }
            toast.show();
        }




    }

    public static void showLog(Context context, String tagName, String msg) {
        Log.d(tagName, msg);
    }

    public static void showAlertDialog(final Activity activity, String positiveText, String negativeText,
                                       String message, final DialogClickListener dialogClickListener) {

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirmation_layout);


        TextView messageTextView = (TextView) dialog.findViewById(R.id.textViewText);
        messageTextView.setText(message);
        dialog.setCancelable(true);
        final Button positiveButton = (Button) dialog.findViewById(R.id.positive_button);
        Button negativeButton = (Button) dialog.findViewById(R.id.negative_button);
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positiveButton.setEnabled(false);
                dialogClickListener.positiveButtonClick();
                dialog.dismiss();

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickListener.negativeButtonClick();
                dialog.dismiss();

            }
        });
        dialog.show();

    }

    public interface DialogClickListener {
        public void positiveButtonClick();

        public void negativeButtonClick();

    }

}
