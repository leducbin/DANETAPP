package com.movideo.whitelabel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.FacebookAuthenticationParser;
import com.movideo.whitelabel.communication.UpdateUserDetailsTask;
import com.movideo.whitelabel.fragment.UserDetailsFragment;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.NetworkAvailability;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddMessageDialogView;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.view.ProgressView;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;

import android.webkit.*;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ExtraActivity extends AppCompatActivity implements View.OnClickListener {

    //@Bind(R.id.closeImageButton)
    ImageButton closeButton;
    private String TAG = ExtraActivity.class.getSimpleName();
    private WebView webView;
    private final String SITE_URL = "http://www.danet.vn";
    private String url;
    ProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_screen);
        progressView = new ProgressView(this);

        url = getIntent().getStringExtra("url");

        if (NetworkAvailability.chkStatus(this)) {
            //if (url.)
            webView = (WebView) findViewById(R.id.webViewExtra);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setBackgroundColor(Color.BLACK);
            //WebSettings webSettings = webView.getSettings();
            //webSettings.setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                    if(url.equals("http://www.danet.vn/account")){
                        finish();
                        return true;
                    }
                    if(url.equals("http://www.danet.vn/delivery")){
                        WidgetsUtils.Log.d("EXTRA","DELIVERY");
                        if (NetworkAvailability.chkStatus(ExtraActivity.this)) {
                            Intent intent = new Intent(ExtraActivity.this, ContentActivity.class);
                            intent.putExtra("url", "http://www.danet.vn/content/delivery.html");
                            startActivity(intent);
                        } else {
                            Toast.makeText(ExtraActivity.this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
                        }
                        return true;
                    }
                    if(url.contains("napas")){
                        WidgetsUtils.Log.d("EXTRA","NAPAS");
                        //webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setLoadWithOverviewMode(true);
                        webView.getSettings().setUseWideViewPort(true);
                        webView.getSettings().setSupportZoom(true);
                        webView.getSettings().setBuiltInZoomControls(true);
                        webView.loadUrl(url);
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
            Log.d(TAG,url);

            webView.loadUrl(url);

        } else {
            loadLoinErrorDialog(getString(R.string.dialog_msg_network_error_occurred));
        }

        closeButton = (ImageButton) findViewById(R.id.closeImageButton);
        if(url.contains("payment.danet")){
            closeButton.setColorFilter(Color.parseColor("#000000"));
        }
        closeButton.setOnClickListener(this);
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
        AddMessageDialogView dialogView = new AddMessageDialogView(this, getString(R.string.label_sign_in_unable), message, getString(R.string.label_ok));
        dialogView.show();
    }
}
