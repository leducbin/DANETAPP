package com.movideo.whitelabel.util;

import android.content.DialogInterface;
import android.view.View;

/**
 * Define the event listener methods for dialog view.
 */
public interface DialogEventListeners {
    /**
     * This method will be invoked when the positive button in the {@link android.app.AlertDialog} is clicked.
     *
     * @param dialog {@link DialogInterface}
     */
    void onPositiveButtonClick(DialogInterface dialog);

    /**
     * This method will be invoked when the negative button in the {@link android.app.AlertDialog} is clicked.
     * This will execute before dialog dismiss is called.
     *
     * @param dialog {@link DialogInterface}
     */
    void onNegativeButtonClick(DialogInterface dialog);
}
