package com.movideo.whitelabel.util.sharedPreference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.movideo.baracus.model.metadata.Content;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.ContentHandler;
import com.movideo.whitelabel.R;
import com.movideo.whitelabel.enums.LicenseType;

import java.lang.reflect.Type;
import java.util.List;

public class PreferenceHelper {

    private static final String PREFERENCES_NAME = "Android-Whitelabel-App-Movideo";
    private static Gson GSON = new Gson();

    public static void setDataInSharedPreference(Activity activity, String key,
                                                 Object object) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, GSON.toJson(object));
        editor.commit();
    }


    public static <T> T getSharedPrefData(Activity activity, String key, Class<T> a) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String gson = prefs.getString(key, null);
        if (gson == null) {
            return null;
        } else {
            try {
                return GSON.fromJson(gson, a);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object stored with key "
                        + key + " is instance of other class");
            }
        }
    }

    public static <T> T getSharedPrefData(Activity activity, String key, Type type) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String gson = prefs.getString(key, null);
        int maxLogSize = 100;
        for(int i = 0; i <= gson.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i+1) * maxLogSize;
            end = end > gson.length() ? gson.length() : end;
            Log.e("pref", gson.substring(start, end));
        }
        if (gson == null) {
            return null;
        } else {
            try {
                return GSON.fromJson(gson, type);
            } catch (Exception e) {
                throw new IllegalArgumentException("Object stored with key "
                        + key + " is instance of other class");
            }
        }
    }

    public static boolean getSharedPrefData(Activity activity, String key) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        boolean value = false;
        if (prefs != null && prefs.contains(key)) {
            value = prefs.getBoolean(key, false);
        }
        return value;
    }

    public static void setDataInSharedPreference(Activity activity, String key,
                                                 boolean value) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void deletePreferenceData(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    public static User getUser(Activity activity) {
        return PreferenceHelper.getSharedPrefData(activity, activity.getResources().getString(R.string.user_info), User.class);
    }

    public static List<Content> getContentList(Activity activity, LicenseType licenseType) {
        String key;
        switch (licenseType) {

            case AVOD:
                key = ContentHandler.KEY_AVOD_HOME_CONTENT;
                break;
            case SVOD:
                key = ContentHandler.KEY_SVOD_HOME_CONTENT;
                break;
            default:
                key = ContentHandler.KEY_TVOD_HOME_CONTENT;
                break;
        }
        List<Content> cont = getSharedPrefData(activity, key, new TypeToken<List<Content>>() {}.getType());
        return getSharedPrefData(activity, key, new TypeToken<List<Content>>() {}.getType());
    }
}
