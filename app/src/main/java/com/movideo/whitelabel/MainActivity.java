package com.movideo.whitelabel;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.enums.LicenseType;
import com.movideo.whitelabel.fragment.HomePageFragment;
import com.movideo.whitelabel.fragment.MovieTrailersFragment;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.OnProductItemClickListener;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManager;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnProductItemClickListener, MovieTrailersFragment.OnFilterClickListener {

    private static final int REQUEST_PHONE_STATE = 1;
    public static final int REQUEST_CODE_FILTER = 1;
    public static final int RESULT_CODE_FILTER_OK = 1;
    public static final int RESULT_CODE_FILTER_CANCEL = 2;

    private static final String APP_ID = "ce432eda52c346bd9fddd9b589a73726";
    private static final String TAG = MainActivity.class.getSimpleName();
    private AddTwoButtonDialogView dialogView;

    @Bind(R.id.buttonViewIcon)
    Button buttonViewIcon;
    @Bind(R.id.imageViewIcon)
    ImageView menuIcon;
    @Bind(R.id.left_drawer_list)
    ExpandableListView menuList;
    @Bind(R.id.drawerLayoutMain)
    DrawerLayout drawerLayout;
    @Bind(R.id.frame_layout_main)
    FrameLayout containerLayout;

    private MenuHandler menuHandler;
    private MovieTrailersFragment fragment;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if(WhiteLabelApplication.getInstance().getUser() == null || WhiteLabelApplication.getInstance().getAccessToken()==null){
            final User user = PreferenceHelper.getUser(this);
            if(user!=null) {
                WhiteLabelApplication.getInstance().setUser(user);
                WhiteLabelApplication.getInstance().setIsUserLoggedIn(true);
            }
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        menuHandler = new MenuHandler(this, drawerLayout, menuList, menuIcon, buttonViewIcon, containerLayout, getSupportFragmentManager());

        if (ViewUtils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (savedInstanceState == null) {
            HomePageFragment fragment = HomePageFragment.newInstance(LicenseType.AVOD);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frame_layout_main, fragment)
                    .commit();
        }
        checkForPhoneStatePermission();
        checkForUpdates();
        checkVersionAndUpdate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        onReloadMenuHandler();
    }

    public void onReloadMenuHandler() {
        menuHandler.onResume();
        AppEventsLogger.activateApp(this);
        Tracking.startUsage(this);
        checkForCrashes();

    }

    @Override
    protected void onPause() {
        try{
            if(dialogView!=null)
                dialogView.dismiss();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        AppEventsLogger.deactivateApp(this);
        Tracking.stopUsage(this);
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(dialogView!=null)
            dialogView.dismiss();
    }

    @Override
    protected void onStart() {
        if (WhiteLabelApplication.getInstance().cameFromOutsideApplication())
            checkVersionAndUpdate();
        super.onStart();
    }

    @Override
    public void onItemClick(Product product) {
        Intent intent = null;
        ContentHandler contentHandler = ContentHandler.getInstance();

        //Firebase analytics tracking
        String package_type = product.getPackageType();
        if(package_type == null)
            package_type= WhiteLabelApplication.getInstance().getLicenseType().name();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, product.getId().toString());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, product.getTitle());
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, package_type);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);

        //product = contentHandler.getProductById();
        if ("movie".equals(product.getType())) {
            intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
            intent.putExtra(MovieDetailsActivity.KEY_PRODUCT, product);
        } else if ("series".equals(product.getType())) {
            intent = new Intent(MainActivity.this, ShowDetailsActivity.class);
            intent.putExtra(ShowDetailsActivity.KEY_PRODUCT, product);
        } else if ("clip".equals(product.getType())) {
            intent = new Intent(MainActivity.this, ClipDetailsActivity.class);
            intent.putExtra(ClipDetailsActivity.KEY_PRODUCT, product);
        }
        if (intent != null) {
            startActivity(intent);
        }


    }

    @Override
    public void onFilterClick(MovieTrailersFragment fragment, List<String> types, List<String> genres, List<String> countries, String minYear, String maxYear, String sortBy) {
        this.fragment = fragment;
        if (types == null || types.isEmpty()) {
            types = new ArrayList<>();
            types.add("movies");
            types.add("series");
        }
        Intent intent = new Intent(MainActivity.this, FilterActivity.class);
        intent.putStringArrayListExtra(FilterActivity.KEY_TYPES, (ArrayList<String>) types);
        intent.putStringArrayListExtra(FilterActivity.KEY_GENRES, (ArrayList<String>) genres);
        intent.putStringArrayListExtra(FilterActivity.KEY_COUNTRIES, (ArrayList<String>) countries);
        intent.putExtra(FilterActivity.KEY_MIN_YEAR, minYear);
        intent.putExtra(FilterActivity.KEY_MAX_YEAR, maxYear);
        intent.putExtra(FilterActivity.KEY_SORT_BY, sortBy);

        startActivityForResult(intent, REQUEST_CODE_FILTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_FILTER) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> countries = data.getStringArrayListExtra(FilterActivity.KEY_COUNTRIES);
                ArrayList<String> genres = data.getStringArrayListExtra(FilterActivity.KEY_GENRES);
                String minYear = data.getStringExtra(FilterActivity.KEY_MIN_YEAR);
                String maxYear = data.getStringExtra(FilterActivity.KEY_MAX_YEAR);
                String sortBy = data.getStringExtra(FilterActivity.KEY_SORT_BY);

                if (fragment != null)
                    fragment.callWhenFilterActivated(genres, countries, minYear, maxYear, sortBy);
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();
        }

    }
    private void checkForCrashes() {
        CrashManager.register(this, APP_ID);
    }

    private void checkForUpdates() {
        // Remove this for store / production builds!
        UpdateManager.register(this, APP_ID);
    }

    /**
     * Returns menu handler.
     *
     * @return {@link MenuHandler}
     */
    public MenuHandler getMenuHandler() {
        return menuHandler;
    }

    public void checkForPhoneStatePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_PHONE_STATE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    showPermissionMessage();

                } else {

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            REQUEST_PHONE_STATE);
                }

            } else {
                //... Permission has already been granted, obtain the UUID
                //getDeviceUuId(MainActivity.this);
            }

        } else {
            //... No need to request permission, obtain the UUID
            //getDeviceUuId(MainActivity.this);
        }
    }


    private void showPermissionMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Play video")
                .setMessage("This app requires the permission to play video to continue")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_PHONE_STATE},
                                REQUEST_PHONE_STATE);
                    }
                }).create().show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PHONE_STATE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // .. Can now obtain the UUID
                    //getDeviceUuId(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Unable to continue without granting permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void checkVersionAndUpdate(){
        PackageManager manager = MainActivity.this.getPackageManager();
        String version;
        try {
            PackageInfo info = manager.getPackageInfo(
                    MainActivity.this.getPackageName(), 0);
            version = String.valueOf(info.versionCode).trim();
        }
        catch (PackageManager.NameNotFoundException e){
            version = "Couldn't find version name";
        }
        GetVersionTask getVersionTask = new GetVersionTask();
        Utils.executeInMultiThread(getVersionTask,version);


    }
    private class GetVersionTask extends AsyncTask<String, Void,Boolean > {
        @Override
        protected Boolean doInBackground(String... params) {

            try {
                URL tool_danet = new URL("http://tools.danet.vn/ott/version.php?device=android");
                HttpURLConnection conn = (HttpURLConnection) tool_danet.openConnection();
                try {
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");

                    OutputStream output = new BufferedOutputStream(conn.getOutputStream());
                    //output.write(body.getBytes("UTF-8"));
                    output.close();
                    conn.connect();
                    Log.d(TAG,conn.getResponseCode()+" "+conn.getResponseMessage());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    bufferedReader.close();
                    Log.i("HTTP Client", "Received String : " + sb.toString());
                    String version = sb.toString().trim();
                    return version.equalsIgnoreCase(params[0]);
                }
                finally{
                    conn.disconnect();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if(!b){
                 dialogView = new AddTwoButtonDialogView(MainActivity.this,
                        "Đã có phiên bản mới", "Vui lòng cập nhật phiên bản mới nhất để có trải nghiệm tốt hơn", "Cập nhật", "Để sau",
                        new DialogEventListeners() {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegativeButtonClick(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
                dialogView.show();
            }
        }
    }
}