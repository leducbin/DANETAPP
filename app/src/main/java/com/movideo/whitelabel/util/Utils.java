package com.movideo.whitelabel.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Handles utility functions.
 */
public class Utils {

    /**
     * Execute the Async Task in multi threads.
     *
     * @param asyncTask {@link AsyncTask}
     */
    public static void executeInMultiThread(AsyncTask asyncTask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            asyncTask.execute();
        }
    }

    /**
     * Execute the Async Task in multi threads with input parameters.
     *
     * @param asyncTask  {@link AsyncTask}
     * @param params     Input parameter
     * @param <Params>   Input parameter type
     * @param <Progress> Progress parameter type
     * @param <Result>   Result parameter type
     */
    public static <Params, Progress, Result> void executeInMultiThread(AsyncTask<Params, Progress, Result> asyncTask, Params... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            asyncTask.execute(params);
        }
    }

    /**
     * Returns the real position for the given position from the circular view pager.
     *
     * @param position Position.
     * @param size     Total size of views in view pager including extra first and last.
     * @return Actual size.
     */
    public static int getRealPosition(int position, int size) {
        if (position == size - 1)
            return 0;
        if (position == 0)
            return size - 3;
        return position - 1;
    }


}
