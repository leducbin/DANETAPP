package com.movideo.whitelabel.util;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DisplayResources {
    private Context context;

    public DisplayResources(Context context) {
        super();
        this.context = context;
    }

    public int getDisplayHeight() {
        if (Build.VERSION.SDK_INT >= 11) {
            Point size = new Point();
            try {
                ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
                return size.y;
            } catch (NoSuchMethodError e) {
                return ((Activity) context).getWindowManager().getDefaultDisplay().getHeight();
            }
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            return metrics.heightPixels;
        }
    }

    public int getDisplayWidth() {
        if (Build.VERSION.SDK_INT >= 11) {
            Point size = new Point();
            try {
                ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
                return size.x;
            } catch (NoSuchMethodError e) {
                return ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
            }
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            return metrics.widthPixels;
        }
    }

    public int getNavigationBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public int getStatusBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }


    public Drawable getDrawable(int id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(id, context.getTheme());
        } else {
            return context.getResources().getDrawable(id);
        }
    }
}
