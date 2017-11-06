package com.movideo.whitelabel.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.movideo.whitelabel.util.DialogEventListeners;

/**
 * Create a message {@link android.app.Dialog} with given input params.
 */
public class AddMessageDialogView  implements DialogInterface.OnClickListener {

    private AlertDialog.Builder dialogBuilder;
    private DialogEventListeners mListener;
    private AlertDialog dialog;
    /**
     * Constructor with arguments.
     *
     * @param context             {@link Context}
     * @param message             Message to display.
     * @param positiveButtonLabel Label for positive button.
     */
    public AddMessageDialogView(Context context, String title, String message, String positiveButtonLabel) {

        dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(positiveButtonLabel, this);
        dialog = dialogBuilder.create();
    }

    public AddMessageDialogView(Context context, String title, String message, String positiveButtonLabel, DialogEventListeners listener) {
        this(context, title, message, positiveButtonLabel);
        mListener = listener;

    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(mListener != null){
            mListener.onPositiveButtonClick(dialog);
        }
        dialog.dismiss();
    }

    public void dismiss(){
        dialog.dismiss();
    }
}
