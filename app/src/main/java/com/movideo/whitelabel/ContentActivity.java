package com.movideo.whitelabel;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.ProgressView;

/**
 * Created by BHD on 1/17/2017.
 */

public class ContentActivity extends AppCompatActivity implements View.OnClickListener {

    //@Bind(R.id.closeImageButton)
    ImageButton closeButton;

    private WebView webView;
    private final String SITE_URL = "http://www.danet.vn";
    private String url;
    ProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_content);
        progressView = new ProgressView(this);

        url = getIntent().getStringExtra("url");

        if (NetworkAvailability.chkStatus(this)) {
            //if (url.)
            webView = (WebView) findViewById(R.id.webViewExtra);
            webView.getSettings().setJavaScriptEnabled(true);
            //WebSettings webSettings = webView.getSettings();
            //webSettings.setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                    if(url.equals("http://www.danet.vn/account")){
                        finish();
                        return true;
                    }
                    return false;
                }

                @Override
                public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
                    progressView.show();
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(final WebView view, final String url) {
                    if (progressView!=null)
                        progressView.dismiss();
                    //progress.setVisibility(View.GONE);
                    super.onPageFinished(view, url);
                }
            });

            webView.loadUrl(url);

        } else {
            loadLoinErrorDialog(getString(R.string.dialog_msg_network_error_occurred));
        }

        closeButton = (ImageButton) findViewById(R.id.closeImageButton);
        closeButton.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        progressView = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.closeImageButton:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void loadLoinErrorDialog(String message) {
        AddMessageDialogView dialogView = new AddMessageDialogView(this, null, message, getString(R.string.label_ok));
        dialogView.show();
    }
}
