package com.movideo.whitelabel.util;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkAvailability {
    public static boolean chkStatus(Context context) {
        // TODO Auto-generated method stub
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr.getActiveNetworkInfo() != null
                && connMgr.getActiveNetworkInfo().isAvailable()
                && connMgr.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public NetworkAvailability() {
        // TODO Auto-generated constructor stub
        super();
    }
}
