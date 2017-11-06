package com.movideo.whitelabel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetworkObserver extends BroadcastReceiver {

	private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();

		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
		{
			NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (info.getState().equals(NetworkInfo.State.CONNECTED))
			{
				//do whatever you want when wifi is active and connected to a hotspot
			}
		}
		else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			// do something
		}
	}

	public void startMonitoring(Context context) {
		this.context = context;
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		
		context.registerReceiver(this, filter);
	}

	public void stopMonitoring() {
		context.unregisterReceiver(this);
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
}
