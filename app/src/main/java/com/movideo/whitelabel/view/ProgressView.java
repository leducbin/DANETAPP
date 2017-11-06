package com.movideo.whitelabel.view;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Window;

import com.movideo.whitelabel.R;

public class ProgressView {

    private Context context;
    private Dialog progressDialog = null;

    public ProgressView(Context context) {
        this.context = context;
        //progressDialog = new ProgressDialog(context);
        progressDialog = new Dialog(context);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progressDialog.setContentView(R.layout.progress_view);
    }

    public void show() {
        if (progressDialog == null || progressDialog.isShowing()) {
            return;
        } else {
            //progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
            Runnable progressRunnable = new Runnable() {

                @Override
                public void run() {
                    progressDialog.cancel();
                }
            };

            Handler pdCanceller = new Handler();
            pdCanceller.postDelayed(progressRunnable, 10000);
        }
    }

    public void dismiss() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        } else {
            progressDialog.dismiss();
        }
    }
}
