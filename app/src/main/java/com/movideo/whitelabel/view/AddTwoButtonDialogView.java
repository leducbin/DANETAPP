package com.movideo.whitelabel.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.movideo.whitelabel.util.DialogEventListeners;

/**
 * Create a {@link android.app.Dialog} with given input params.
 */
public class AddTwoButtonDialogView implements DialogInterface.OnClickListener {

    private AlertDialog.Builder dialogBuilder;
    private DialogEventListeners listeners;
    private AlertDialog dialog;

    /**
     * Constructor with arguments.
     *
     * @param context             {@link Context}
     * @param message             Message to display.
     * @param positiveButtonLabel Label for positive button.
     * @param negativeButtonLabel Label for negative button.
     * @param listeners           {@link DialogEventListeners}
     */
    public AddTwoButtonDialogView(Context context, String title, String message, String positiveButtonLabel, String negativeButtonLabel, DialogEventListeners listeners) {
        this.listeners = listeners;

        dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(positiveButtonLabel, this);
        dialogBuilder.setNegativeButton(negativeButtonLabel, this);
        dialog = dialogBuilder.create();
    }

    public void show() {
        dialog.show();
    }

    public  void dismiss(){ dialog.dismiss(); }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                listeners.onPositiveButtonClick(dialog);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                listeners.onNegativeButtonClick(dialog);
                dialog.dismiss();
                break;
        }
    }
}
