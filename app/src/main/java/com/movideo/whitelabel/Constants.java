package com.movideo.whitelabel;

import android.os.Environment;


/**
 *  Application's global constants
 */
public class Constants {
    
    public static final String PREF_LICENSE_ACCEPTED = "license_accepted";

    public static final String PREF_PROXY_TYPE = "proxyType";
    public static final String PREF_PROXY_URL = "proxyURL";
    public static final String PREF_PROXY_PORT = "proxyPort";
    
    public static final String PREF_TYPICAL_BITRATE = "typicalBitrate";
      
    public static final String PREF_LOOP_MODE = "autoLoop";

    public static final String PREF_USE_CONTENT_LIST = "useContentList";
    public static final String PREF_CONTENT_LIST_URI = "contentListUri";

    public static final String PREF_HLSDL_START_DELAY = "hlsDlStartDelay";
    
    public static final String PREF_ENABLE_LIVE_SEEKING = "enableLiveSeeking";
    
    public static final String PREF_ENABLE_FAAS_OFFLINE = "enableFaasOffline";

    /**
     * Intent keys recognized in incoming intents for player Activity
     */
    public static final String VCAS_BOOT_ADDRESS_INTENT_SKEY = "vmxBootAddress";
    public static final String VCAS_COMPANY_NAME_INTENT_SKEY = "vmxCompanyName";
    public static final String DRM_CUSTOM_RIGHTS_SKEY        = "drmCustomRights";
    public static final String DB_FAAS_OFFLINE_SQUADEO_SKEY  = "dbFaasOfflineSquadeo";
    public static final String MARLIN_PROTECTED_SKEY        = "MarlinProtected";

    // Authentec login
    public static final String AUTH_USER_NAME_INTENT_SKEY = "HttpAuthUsername";
    public static final String AUTH_PASSWORD_INTENT_SKEY  = "HttpAuthPassword";

    //SSL
    public static final String SSL_CA_CERT_FILE_PATH_INTENT_SKEY = "SSLCACertFilePath";
    public static final String SSL_CA_CERT_FILE_TYPE_INTENT_SKEY = "SSLCACertFileType";
    public static final String SSL_CLIENT_CERT_FILE_PATH_INTENT_SKEY = "SSLClientCertFilePath";
    public static final String SSL_CLIENT_CERT_FILE_TYPE_INTENT_SKEY = "SSLClientCertFileType";
    public static final String SSL_CLIENT_KEY_FILE_PATH_INTENT_SKEY = "SSLClientKeyFilePath";
    public static final String SSL_CLIENT_KEY_FILE_TYPE_INTENT_SKEY = "SSLClientKeyFileType";
    
    // Custom header Http
    public static final String HTTP_CUSTOM_HEADER_INTENT_SKEY = "HttpCustomHeader";

    public static final String SUBTITLE_FILE_INTENT_SKEY = "subtitleFile";
    public static final String SUBTITLE_TRACK_INTENT_SKEY = "subtitleTrack";
    public static final String SUBTITLE_TRACKTYPE_INTENT_SKEY = "subtitleTrackType";

    public static final String RESUME_TIME_INTENT_IKEY = "resumeTime";

    public static final String LOCAL_SERVER_INTENT_ZKEY = "localServer";

    public static final String AUDIO_LANGUAGE_INTENT_SKEY = "audio_lng";
    
    public static final String HLSDL_ORG_URI_INTENT_SKEY = "hlsdlOriginalUri";
    
    public static final String HLS_CUSTOM_BITRATES_ZERO_LAG_AIKEY = "hls_zerolag_bitrates";


    /** General database name for the whole app */
    public static final String APP_DATABASE_NAME = "quickplayer_db";
    public static final int APP_DATABASE_VERSION = 1;
    
    
    public static final int LOCAL_HTTP_SERVER_PORT = 8889;
    public static final String LOCAL_HTTP_SERVER_WWWROOT =
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/qp_cache";
                
    /** Certificate types for https streams */
    public static final int SSL_TYPE_UNKNOWN = -1;
    public static final int SSL_TYPE_PEM = 0;
    public static final int SSL_TYPE_DER = 1;
    public static final int SSL_TYPE_ENG = 2;

}

