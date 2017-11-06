package com.movideo.whitelabel;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.movideo.baracus.model.metadata.Content;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.communication.ContentRequestListener;
import com.movideo.whitelabel.communication.GetContentTask;
import com.movideo.whitelabel.communication.GetGenresTask;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.ProgressView;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int TOTAL = 4;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    private String productId;

    private boolean waitOver;
    private int count;
    private TextView versionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Uri data = getIntent().getData();
        if (data != null){
            List<String> params = data.getPathSegments();
            if (params != null && params.size() > 0 ){
                productId = params.get(params.size() - 1);
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        versionTextView = (TextView) findViewById(R.id.versionTextView);
        /*Get user information from SharedPreferences*/
        User user = PreferenceHelper.getUser(this);
        boolean userLoggedIn = PreferenceHelper.getSharedPrefData(this, getResources().getString(R.string.user_is_logged_in));
        WhiteLabelApplication whiteLabelApplication = WhiteLabelApplication.getInstance();
        whiteLabelApplication.setLicenseType(LicenseType.AVOD);
        if (userLoggedIn && user != null) {

            whiteLabelApplication.setIsUserLoggedIn(userLoggedIn);
            whiteLabelApplication.setUser(user);
            whiteLabelApplication.setAccessToken(user.getAccessToken());
        } else if (user != null) {
            whiteLabelApplication.setIsUserLoggedIn(false);
            whiteLabelApplication.setUser(user);
            whiteLabelApplication.setAccessToken(null);
        } else {
            whiteLabelApplication.setIsUserLoggedIn(false);
            whiteLabelApplication.setUser(null);
        }

        waitOver = false;
        count = 0;

        GetContentTask getContentTaskAVOD = new GetContentTask(getAVODContentListener(ContentHandler.KEY_AVOD_HOME_CONTENT));
        GetContentTask getContentTaskSVOD = new GetContentTask(getAVODContentListener(ContentHandler.KEY_SVOD_HOME_CONTENT));
        GetContentTask getContentTaskTVOD = new GetContentTask(getAVODContentListener(ContentHandler.KEY_TVOD_HOME_CONTENT));

        List<String> types = new ArrayList<>();
        types.add("movie");
        types.add("series");

        GetGenresTask getGenresTask = new GetGenresTask(getGenresContentListener(), types);

//        Utils.executeInMultiThread(getContentTaskAVOD, getString(R.string.label_menu_header_avod_title).toLowerCase());
//        Utils.executeInMultiThread(getContentTaskSVOD, getString(R.string.label_menu_header_svod_title).toLowerCase());
//        Utils.executeInMultiThread(getContentTaskTVOD, getString(R.string.label_menu_header_tvod_title).toLowerCase());

        Utils.executeInMultiThread(getContentTaskAVOD, "go");
        Utils.executeInMultiThread(getContentTaskSVOD, "buffet");
        Utils.executeInMultiThread(getContentTaskTVOD, "cineplex");


        Utils.executeInMultiThread(getGenresTask);
        setVersion();
        startCounter();
    }

    private ContentRequestListener<List<Content>> getAVODContentListener(final String key) {

        return new ContentRequestListener<List<Content>>() {

            @Override
            public void onRequestCompleted(List<Content> content) {
                if (content != null) {
                    PreferenceHelper.setDataInSharedPreference(SplashScreenActivity.this, key, content);
                    count++;
                    loadHomePageActivity();
                }
            }

            @Override
            public void onRequestFail(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
                forceLoadHomePageActivity();
            }
        };
    }

    private ContentRequestListener<List<String>> getGenresContentListener() {
        return new ContentRequestListener<List<String>>() {

            @Override
            public void onRequestCompleted(List<String> strings) {
                PreferenceHelper.setDataInSharedPreference(SplashScreenActivity.this, ContentHandler.KEY_GENRES_LIST, strings);
                count++;
                loadHomePageActivity();
            }

            @Override
            public void onRequestFail(Throwable throwable) {
                Log.e(TAG, throwable.getMessage(), throwable);
            }
        };
    }

    private void goToNextScreen(){
        if (productId == null){
            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else {
            new AsyncTask<Void, Void, Product>() {
                private String error = null;
                private ProgressView progressView;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected Product doInBackground(Void... params) {
                    Product product = null;
                    try {
                        String accessToken = WhiteLabelApplication.getInstance().getAccessToken();
                        product = ContentHandler.getInstance().getProductById(accessToken, productId);
                    } catch (Exception e) {
                        Log.e(this.getClass().getName(), e.getMessage());
                        error = e.getMessage();
                    }
                    return product;
                }

                @Override
                protected void onPostExecute(Product product) {
                    try {
                        if (error == null && product != null) {
                            Intent intent = new Intent(SplashScreenActivity.this, MovieDetailsActivity.class);
                            intent.putExtra(MovieDetailsActivity.KEY_PRODUCT, product);
                            startActivity(intent);
                        }
                    } catch (Exception e){
                        Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
                    }
                }
            }.execute();
        }
    }

    private void loadHomePageActivity() {
        if (TOTAL == count && waitOver) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    finish();
                    goToNextScreen();
                }
            });
        }
    }

    private void forceLoadHomePageActivity() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                finish();
                goToNextScreen();
            }
        });

    }

    private void startCounter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    waitOver = true;
                    loadHomePageActivity();
                } catch (InterruptedException e) {
                    Log.d(TAG, e.getMessage(), e);
                }
            }
        }).start();
    }

    private void setVersion(){
        PackageManager manager = this.getPackageManager();
        String version;
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionName;
        }
        catch (PackageManager.NameNotFoundException e){
            version = "Couldn't find version name";
        }
        versionTextView.setText("Version " + version);
        versionTextView.setAlpha(0.65f);
    }
}
