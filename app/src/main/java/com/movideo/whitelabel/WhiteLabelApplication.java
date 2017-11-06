package com.movideo.whitelabel;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.enums.LicenseType;

/**
 * Application class.
 */
public class WhiteLabelApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static WhiteLabelApplication singleton;
    private User user;

    private boolean isUserLoggedIn;
    private LicenseType licenseType = LicenseType.AVOD;

    public static WhiteLabelApplication getInstance() {
        return singleton;
    }
    private int count = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        user = new User();
        singleton = this;
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        registerActivityLifecycleCallbacks(this);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    /**
     * Returns true if user logged in.
     *
     * @return true/false
     */
    public boolean isUserLoggedIn() {
        return isUserLoggedIn;
    }

    /**
     * sets true if user logged in.
     */
    public void setIsUserLoggedIn(boolean isUserLoggedIn) {
        this.isUserLoggedIn = isUserLoggedIn;
    }

    /**
     * Returns user's full name.
     *
     * @return Full name.
     */
    public String getUserFullName() {
        return user.getGivenName() + " " + user.getFamilyName();
    }
    /**
     * Sets user's full name.
     *
     */
    /**
     * Returns license type.
     *
     * @return {@link LicenseType}
     */
    public LicenseType getLicenseType() {
        return licenseType;
    }

    /**
     * Sets license type.
     *
     * @param licenseType {@link LicenseType}
     */
    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
    }

    /**
     * Returns access token of the user.
     *
     * @return Access token
     */
    public String getAccessToken() {
        if (user != null)
            return user.getAccessToken();
        else
            return null;
    }

    /**
     * Sets access token of the user.
     *
     * @param accessToken Access token.
     */
    public void setAccessToken(String accessToken) {
        if (user != null)
            user.setAccessToken(accessToken);
    }

    public void onActivityStarted(Activity activity) {
        count++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        count--;
    }

    /**
     * Use this method in your Activities to test if the activity was
     * transitioned to from outside the application.
     *
     * If you call this method in Activity.onResume(), then count should be
     * compared to 0. If you call this method in Activity.onStart() but
     * *before* calling super.onStart(), then count should be compared to 0.
     *
     * However, if you call this method after super.onStart(), then count
     * should be compared to 1.
     */
    public boolean cameFromOutsideApplication() {
        return count == 0;
    }

    //Don't need to use the rest of the activity lifecycle callbacks
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }
    public void onActivityDestroyed(Activity activity) {
    }
    public void onActivityPaused(Activity activity) {
    }
    public void onActivityResumed(Activity activity) {
    }
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

}
