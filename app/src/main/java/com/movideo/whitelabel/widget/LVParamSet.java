package com.movideo.whitelabel.widget;

import java.util.HashMap;
import java.util.Set;

public class LVParamSet {
	
	public static final String PROXY_PORT_KEY = "proxyPort";
	public static final String PROXY_HOST_KEY = "proxyHost";
	public static final String PROXY_TYPE_KEY = "proxyType";

	/**
	 * Way of reacting when encountering an HLS Event playlist
	 * recognized values are:
	 * <ul>
	 * <li>ask: playback will not start directly</li>
	 * <li>begin: playback will start directly from begin of event</li>
	 * <li>live: playback will start directly from live of event</li>
	 * </ul>
	 */
	public static final String EVENT_START_MODE_KEY = "eventStartMode";
	
	public static final String LOOPING_PLAYBACK_KEY = "looping";
	
	public static final String LIVE_SEEKING_ENABLED_KEY = "liveSeekingEnabled";
	
	public static final String FAAS_OFFLINE_ENABLED_KEY = "faasOfflineEnabled";
	
	public static final String CUSTOM_NETWORK_MODE = "customNetworkMode";

	private String name = null;
	private HashMap<String, String> params;

	public LVParamSet() {
		params = new HashMap<String, String>();
	}

	public LVParamSet(String name) {
		params = new HashMap<String, String>();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setParam(String key, String value) {
		params.put(key, value);
	}

	public String getParam(String key) {
		return params.get(key);
	}

	public Set<String> keys() {
		return params.keySet();
	}

	public void clearParam(String key) {
		params.remove(key);
	}
}
