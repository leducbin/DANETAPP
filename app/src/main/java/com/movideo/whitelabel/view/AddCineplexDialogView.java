package com.movideo.whitelabel.view;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.movideo.whitelabel.R;

public class AddCineplexDialogView implements View.OnClickListener {

    private Context context;
    private Dialog dialog;

    public static String WEBSITE_URL = "http://movideo.com";

    public AddCineplexDialogView(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add_cineplex_credits);

        ((TextView) dialog.findViewById(R.id.goToWebsiteTextView)).setOnClickListener(this);
        ((TextView) dialog.findViewById(R.id.cancelTextView)).setOnClickListener(this);
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goToWebsiteTextView:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
                context.startActivity(intent);
                dismiss();
                break;
            case R.id.cancelTextView:
                dismiss();
                break;
            default:
                break;
        }
    }
}
