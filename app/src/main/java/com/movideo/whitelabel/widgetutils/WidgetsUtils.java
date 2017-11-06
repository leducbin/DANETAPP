package com.movideo.whitelabel.widgetutils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WidgetsUtils {
	
	public static class Log {
		
		// verbose: 0; debug: 1; info: 2; warning: 3; error: 4; none: 5 
		private final static int LOG_LEVEL = 0;
		
		public static void e(String tag, String msg) {
			if(LOG_LEVEL <= 4) {
				android.util.Log.e(tag, msg);
			}
		}
		
		public static void w(String tag, String msg) {
			if(LOG_LEVEL <= 3) {
				android.util.Log.w(tag, msg);
			}
		}
		
		public static void i(String tag, String msg) {
			if(LOG_LEVEL <= 2) {
				android.util.Log.i(tag, msg);
			}
		}
		
		public static void d(String tag, String msg) {
			if(LOG_LEVEL <= 1) {
				android.util.Log.d(tag, msg);
			}
		}
		
		public static void v(String tag, String msg) {
			if(LOG_LEVEL <= 0) {
				android.util.Log.v(tag, msg);
			}
		}
	}

	/**
	 * Checks is there is a data connection available, whatever its type
	 * @param ctx Context of the calling application
	 * @return	true if a data connection is available, false otherwise
	 */
	public static boolean isDataConnectionAvailable(Context ctx) {
		ConnectivityManager connectivityMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo[] networks = connectivityMgr.getAllNetworkInfo();
		if(null != networks) {
			for(NetworkInfo network : networks) {
				if(NetworkInfo.State.CONNECTED == network.getState()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns the UDID of an application's resource
	 * @param context context of the application
	 * @param resourceName name of the resource
	 * @param resourceType type of the resource
	 * @return UDID of the resource or -1 if not found
	 */
	public final static int findResourceIdInContext(Context context, String resourceName, String resourceType) {
		String packageName = context.getPackageName();
		return context.getResources().getIdentifier(resourceName, resourceType, packageName);
	}
}
