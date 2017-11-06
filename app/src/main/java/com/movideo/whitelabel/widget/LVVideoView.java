package com.movideo.whitelabel.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lifevibes.LVSurfaceView;
import com.lifevibes.lvmediaplayer.LVAudioTrack;
import com.lifevibes.lvmediaplayer.LVMediaPlayer;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnBufferingUpdateListener;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnCompletionListener;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnDownloadUpdateListener;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnErrorListener;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnInfoListener;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnPreparedListener;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnSeekCompleteListener;
import com.lifevibes.lvmediaplayer.LVMediaPlayer.OnVideoSizeChangedListener;
import com.lifevibes.lvmediaplayer.LVSubtitle;
import com.lifevibes.lvmediaplayer.LVSubtitleTrack;
import com.movideo.whitelabel.Constants;
import com.movideo.whitelabel.widget.LVMediaController.LVMediaPlayerControl;
import com.movideo.whitelabel.widget.LVMediaControllerInterface.ButtonMode;
import com.movideo.whitelabel.widget.LVMediaControllerInterface.PlayPauseBtnState;
import com.movideo.whitelabel.widgetutils.LVClosedCaptionFormatter;
import com.movideo.whitelabel.widgetutils.LVID3MetadataParser;
import com.movideo.whitelabel.widgetutils.LVSubtitleFormatter;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;
import com.movideo.whitelabel.widgetutils.WidgetsUtils.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;


public final class LVVideoView extends RelativeLayout implements LVMediaPlayerControl, SurfaceHolder.Callback {

    /**
     * Interface to implement to be notified of a double-tap event occurring on the video surface. <br/>
     * Use {@link #setOnDoubleTapListener(OnDoubleTapListener)} to give the reference of listener to notify.
     */
    public interface OnDoubleTapListener {
        /**
         * Will be called whenever a double tap event occurs on the video surface
         * @param x position of the double-tap event relative to the LVVideoView's position
         * @param y position of the double-tap event relative to the LVVideoView's position
         * @return true if the event was handled, false otherwise
         */
        public abstract boolean onDoubleTap(LVVideoView videoView, float x, float y);
    }

    /** ---------------------------------------------------------------------------- */

    /**
     * Internal GestureListener to capture simple and double-tap events on the video surface.<br/>
     * Double-tap events are forwarded to an external listener (if any) to be used by the parent Activity.<br/>
     * Use {@link #setOnDoubleTapListener(OnDoubleTapListener)} to give the reference of listener to notify.
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (null != onDoubleTapListener) {
                Log.d(TAG, "doubleTapListener" + onDoubleTapListener);
                return onDoubleTapListener.get().onDoubleTap(LVVideoView.this, e.getX(), e.getY());
            }
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            Log.d(TAG, "onSingleTapConfirmed");
            if(null != mediaControllerInterface) {
                Log.d(TAG, "toggleControlsVisibility on mediaController " + mediaControllerInterface);
                mediaControllerInterface.toggleControlsVisibility();
            }
            return super.onSingleTapConfirmed(e);
        }
    }


    /**
     * Internal listener class for all mediaplayer related events.
     * All events are forwarded to external listeners if any.
     */
    private class InternalMediaPlayerListener implements
                    LVMediaPlayer.OnInfoListener,
                    LVMediaPlayer.OnPreparedListener,
                    LVMediaPlayer.OnErrorListener,
                    LVMediaPlayer.OnCompletionListener,
                    LVMediaPlayer.OnVideoSizeChangedListener,
                    LVMediaPlayer.OnSeekCompleteListener,
                    LVMediaPlayer.OnBufferingUpdateListener,
                    LVMediaPlayer.OnDownloadUpdateListener {

        //private static final String TAG = "LVVideoView$InternalMediaPlayerListener";

        @Override
        public void onDownloadUpdate(MediaPlayer mp, int percent) {
            internalOnDownloadUpdate(percent);
            if(null != downloadUpdateListener) {
                downloadUpdateListener.get().onDownloadUpdate(mp, percent);
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            internalOnBufferingUpdate(percent);
            if(null != bufferingupdateListener) {
                bufferingupdateListener.get().onBufferingUpdate(mp, percent);
            }
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            internalOnSeekComplete();
            if(null != seekCompleteListener) {
                seekCompleteListener.get().onSeekComplete(mp);
            }
        }

        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            if(null != videoSizeChangedListener) {
                videoSizeChangedListener.get().onVideoSizeChanged(mp, width, height);
            }
            updateClosedCaptionTextHeight();
            updateClosedCaptionViewPadding();
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            internalOnCompletion(mp);
            if(null != completionListener) {
                completionListener.get().onCompletion(mp);
            }
        }

        @Override
        public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
            isInErrorState = true;
            Log.e(TAG, "onError " + arg1 + " " + arg2);
            if(null != errorListener) {
                // forward event to external listener if any
                return errorListener.get().onError(arg0, arg1, arg2);
            }
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            internalOnPrepared();
            if(null != preparedListener) {
                preparedListener.get().onPrepared(mp);
            }
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            internalOnInfo(what, extra);
            // forward event to external listener if any
            if(null != infoListener) {
                return infoListener.get().onInfo(mp, what, extra);
            }
            return true;
        }
    }

    /**
     * Progress bar update count down class.
     * This timer is used to regularly update the playback progression.
     */
    private class ProgressUpdateCDT extends CountDownTimer {

        public ProgressUpdateCDT(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() { }

        @Override
        public void onTick(long millisUntilFinished) {
            internalUpdateProgress();
        }
    }

    private final Handler closedCaptionUpdatesHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "closedCaptionUpdatesHandler - handleMessage");
            handleSubtitleEvent(msg.arg1);
        }
    };

    private final Handler id3UpdatesHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "id3UpdatesHandler - handleMessage");
            handleID3Metadata();
        }
    };

    private final Handler videoExpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "videoExpHandler - handleMessage");
            if(null != mediaplayer) {
                try {
                    // Use automatic settings
                    mediaplayer.setVideoExperienceConfig(videoExperienceEffectMode, videoExperienceEffectValue);

                    WidgetsUtils.Log.v(TAG, "LVVideoView - setVideoExperienceConfig: + " + videoExperienceEffectMode + ", value="+videoExperienceEffectValue);

                    //Use automatic settings
                    //mediaplayer.setVideoExperienceMode(videoExperienceEffectMode);
                }
                catch(UnsupportedOperationException e) {}
            }
        }
    };


    /** Period for playback progression update in ms */
    private static final int PROGRESS_UPDATE_TIMER_PERIOD = 500;


    /** Default values used for HLS variant choice */
    public static final int DEFAULT_MIN_HTTP_STREAMING_BITRATE = 0;
    public static final int TYPICAL_HTTP_STREAMING_BITRATE_WIFI = 2048; // in kbps
    public static final int TYPICAL_HTTP_STREAMING_BITRATE_3G = 256; // in kbps

    public static final int LIVE_PLAYBACK_POSITION = -1;

    public static final String DEFAULT_NO_DATA_CONNECTION_MSG = "No connexion available";

    /**
     * Possible ways of reacting when encountering an "Event" HLS playlist
     * the mode must be set using the {@link #() } with key "eventStartMode".
     *
     */
    public static enum EventStartMode {
        BEGIN,  /** playback will start directly from beginning*/
        LIVE, /** playback will start directly from live */
        ASK, /** playback will not start directly. The app is responsible for starting the playback
        at the desired position when receiving the onPrepared() callback. Event detection is available
        using method {@link #isInEventMode()}*/
    }

    public String TAG = "LVVideoView";


    private LVSurfaceView videoView = null;
    private TextView subtitleView = null;
    private TextView id3TextView = null;
    private ImageView id3ImageView = null;
    private LinearLayout id3Layout = null;
    private TextView closedCaptionView = null;
    private TextView smpteSubtitleView = null;
    private WebView id3WebView = null;

    private LVMediaPlayer mediaplayer;

    // application contexts and state
    private boolean mbAutoStart = true;

    // internal events listener
    private InternalMediaPlayerListener mediaplayerListener;
    private ProgressUpdateCDT progressUpdateTimer = null;
    private GestureDetector gestureDetector = null;

    private Context context;
    private Uri mediaUri;
    private boolean videoUriChanged;

    private String subtitleFilePath = null;
    private String subtitleTrackName = null;
    private int subtitleTrackType = LVSubtitle.Type_Unknown;
    private String currentAudioTrackName = null;
    private LVAudioTrack[] audioTracksList = null;


    private int audioOnlyDrawableId;
    private Drawable audioOnlyDrawable;

    private LVMediaControllerInterface mediaControllerInterface;

    private boolean canSeek = false;
    private boolean surfaceCreated = false;
    private boolean prepareMediaPlayerOnSurfaceCreated = false;

    private boolean mbMarlinProtected = false;
    private String DRMCustomRights;
    private String securedHLSParam1;
    private String securedHLSParam2;
    private String dbFaasOfflineSquadeo;
    private String httpAuthUsername = null;
    private String httpAuthPassword = null;
    private String SSLCACertFilePath = null;
    private int    SSLCACertFileType = Constants.SSL_TYPE_UNKNOWN;
    private String SSLClientCertFilePath = null;
    private int    SSLClientCertFileType = Constants.SSL_TYPE_UNKNOWN;
    private String SSLClientKeyFilePath = null;
    private int    SSLClientKeyFileType = Constants.SSL_TYPE_UNKNOWN;
    private String httpCustomHeader = null;

    private String proxyUrl;
    private int proxyPort;
    private int proxyType;
    private boolean loopingPlayback = false;

    private int customNetworkMode = 0;

    private int minHLSBitrate = -1;
    private int maxHLSBitrate = -1;
    private int typicalHLSBitrate = -1;

    private int currentDisplayMode;
    private int currentBuffering;

    private int[] liveSeekingWindow = null;
    private boolean liveSeekingEnabled = false;

    private boolean faasOfflineEnabled = false;

    private double resumePlaybackPosition = 0;

    private boolean isInErrorState = false;

    private int videoExperienceEffectValue = 50;
    private int videoExperienceEffectMode = 0;

    protected boolean inEventMode = false;
    private int mDownloadedMediaDuration = -1;


    // by default LVVideoView will start at "live" on event HLS playlists
    protected EventStartMode eventStartMode = EventStartMode.LIVE;

    private boolean secondaryProgressFromOutside = false;

    // references on potential external listeners for mediaplayer events
    private WeakReference<OnErrorListener> errorListener = null;
    private WeakReference<OnInfoListener> infoListener = null;
    private WeakReference<OnCompletionListener> completionListener = null;
    private WeakReference<OnBufferingUpdateListener> bufferingupdateListener = null;
    private WeakReference<OnDownloadUpdateListener> downloadUpdateListener = null;
    private WeakReference<OnVideoSizeChangedListener> videoSizeChangedListener = null;
    private WeakReference<OnPreparedListener> preparedListener = null;
    private WeakReference<OnSeekCompleteListener> seekCompleteListener = null;

    // reference on potential external listener for double tap events
    private WeakReference<OnDoubleTapListener> onDoubleTapListener = null;

    /** ---------------------------------------------------------------------------- */


    public LVVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        WidgetsUtils.Log.v(TAG, "LVVideoView(Context context, AttributeSet attrs)");
        internalConstruct(context, attrs);
    }

    public LVVideoView(Context context) {
        super(context);
        WidgetsUtils.Log.v(TAG, "LVVideoView(Context context)");
        internalConstruct(context, null);
    }

    public LVVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        WidgetsUtils.Log.v(TAG, "LVVideoView(Context context, AttributeSet attrs, int defStyle)");
        internalConstruct(context, attrs);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        WidgetsUtils.Log.v(TAG, "surfaceChanged " + holder);
        updateClosedCaptionTextHeight();
        updateClosedCaptionViewPadding();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        WidgetsUtils.Log.d(TAG, "surfaceCreated" + holder);
        surfaceCreated = true;

        if(null == videoView) {
            videoView = (LVSurfaceView) getChildAt(0);
            Log.d(TAG, "surfaceCreated - got videoView: " + videoView);
        }
        if(null != mediaplayer) {

            /** update the video surface here in case of change */
            mediaplayer.setVideoView(videoView);

            if (prepareMediaPlayerOnSurfaceCreated){
                /** prepare player for playback because the surface is available now */
                preparePlayerForPlayback();
                prepareMediaPlayerOnSurfaceCreated = false;
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        WidgetsUtils.Log.d(TAG, "surfaceDestroyed " + holder);
        surfaceCreated = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void finalize() throws Throwable {
        Log.d(TAG, "finalize");
        internalCleanUp();
        super.finalize();
    }


    /** ---------------------------------------------------------------------------- */


    private void internalConstruct(Context context, AttributeSet attrs) {

        WidgetsUtils.Log.v(TAG, "internalConstruct START");
        loadResources(context);

        if(!isInEditMode()) {
            LVSurfaceView videoView = new LVSurfaceView(context, attrs);
            videoView.getHolder().addCallback(this);
            if (LVMediaPlayer.getSdkVersion(context).contains("demo_pro_hevc_vmx_quickplayerStr_")){
                videoView.setSecure(false); //enable screenshot in demo mode
            }

            LayoutParams params = (LayoutParams) this.generateDefaultLayoutParams();
            params.width = LayoutParams.MATCH_PARENT;
            params.height = LayoutParams.MATCH_PARENT;
            videoView.setLayoutParams(params);
            this.addView(videoView, 0);
        }

        //construct ID3 views
        id3TextView = new TextView(context);
        id3TextView.setTextColor(Color.WHITE);
        id3TextView.setBackgroundColor(Color.TRANSPARENT);
        id3TextView.setShadowLayer((float)3.0, (float)5.0, (float)5.0, Color.BLACK);
        id3TextView.setPadding(1, 1, 1, 5);
        id3TextView.setVisibility(View.GONE);

        id3ImageView = new ImageView(context);
        id3ImageView.setBackgroundColor(Color.TRANSPARENT);
        id3ImageView.setVisibility(View.GONE);

        id3Layout = new LinearLayout(context);
        id3Layout.setOrientation(LinearLayout.VERTICAL);
        id3Layout.addView(id3TextView);
        id3Layout.addView(id3ImageView);
        RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) this.generateDefaultLayoutParams();
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, TRUE);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, TRUE);
        relativeParams.topMargin = 10;
        this.addView(id3Layout, relativeParams);
        id3Layout.setVisibility(View.GONE);

        //construct subtitle views
        subtitleView = new TextView(context);
        subtitleView.setTextColor(Color.WHITE);
        subtitleView.setBackgroundColor(Color.TRANSPARENT);
        subtitleView.setShadowLayer((float)3.0, (float)5.0, (float)5.0, Color.BLACK);
        subtitleView.setPadding(1, 1, 1, 5);
        relativeParams = (RelativeLayout.LayoutParams) this.generateDefaultLayoutParams();
        relativeParams.addRule(RelativeLayout.CENTER_HORIZONTAL, TRUE);
        relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, TRUE);
        relativeParams.bottomMargin = 10;
        subtitleView.setVisibility(View.GONE);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 10);
        this.addView(subtitleView, relativeParams);

        closedCaptionView = new TextView(context);
        closedCaptionView.setVisibility(View.GONE);
        this.addView(closedCaptionView);

        smpteSubtitleView = new TextView(context);
        smpteSubtitleView.setVisibility(View.GONE);
        smpteSubtitleView.setGravity(Gravity.LEFT);
        this.addView(smpteSubtitleView);

        id3WebView = new WebView(context);
        RelativeLayout.LayoutParams id3WebViewParams = (RelativeLayout.LayoutParams) this.generateDefaultLayoutParams();
        id3WebViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, TRUE);
        id3WebViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, TRUE);
        id3WebViewParams.width = LayoutParams.MATCH_PARENT;
        id3WebViewParams.height = LayoutParams.MATCH_PARENT;

        this.addView(id3WebView, id3WebViewParams);
        id3WebView.setClickable(false);
        id3WebView.setFocusable(false);
        id3WebView.setEnabled(false);
        id3WebView.clearFocus();
        id3WebView.setVisibility(GONE);

        gestureDetector = new GestureDetector(context, new GestureListener());
        this.setClickable(true);

        WidgetsUtils.Log.v(TAG, "internalConstruct END");
    }

    /**
     *  ----------------------------------------------------------------------------
     * Implementation of LVMediaController$LVMediaPlayerControl interface
     */

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeek() {
        return canSeek;
    }

    @Override
    public int getBufferPercentage() {
        return currentBuffering;
    }

    @Override
    public int getCurrentPosition() {
        if(null != mediaplayer) {
            return mediaplayer.getCurrentPosition();
        }
        return -1;
    }

    @Override
    public int getDuration() {
        if (0 <= mDownloadedMediaDuration){
            return mDownloadedMediaDuration;
        }else if(null != mediaplayer){
            return mediaplayer.getDuration();
        }
        return -1;
    }

    @Override
    public boolean isPlaying() {
        if(null != mediaplayer) {
            return mediaplayer.isPlaying();
        }
        return false;
    }

    @Override
    public void pause() {
        internalPause();
    }

    @Override
    public void seekTo(int pos, boolean lastStep) {
        resetID3Views();
        Log.d(TAG, "seekTo " + pos + ", " + lastStep);


        if(null != mediaplayer && canSeek) {
            if(!lastStep) {

                    WidgetsUtils.Log.v(TAG, "seekTo");
                    if(null != liveSeekingWindow && liveSeekingEnabled) {
                        if(LVMediaControllerInterface.JUMP_TO_LIVE_TIME_CMD == pos ) {
                            /* jump to live button pushed - seek to the live position */
                            mediaplayer.seekTo(liveSeekingWindow[1]);
                        } else {
                            mediaplayer.seekTo(liveSeekingWindow[0] + pos);
                        }
                    } else {
                        mediaplayer.seekTo(pos);
                    }
            }
            else
            {
                WidgetsUtils.Log.v(TAG, "seekTo");
                if(null != liveSeekingWindow && liveSeekingEnabled) {
                    if(LVMediaControllerInterface.JUMP_TO_LIVE_TIME_CMD == pos ) {
                        /* jump to live button pushed - seek to the live position */
                        mediaplayer.seekTo(liveSeekingWindow[1]);
                    } else {
                        mediaplayer.seekTo(liveSeekingWindow[0] + pos);
                    }
                } else {
                    mediaplayer.seekTo(pos);
                }
            }
        }
    }

    @Override
    public void start() {

        if(null == this.context) {
            WidgetsUtils.Log.w(TAG, "start - null context !");
            return;
        }
        if(null == mediaUri) {
            WidgetsUtils.Log.w(TAG, "start - null uri !");
            return;
        }

        /** if the media uri has changed since last call to start, we need to reset the player to take it
         * into account */
        if(videoUriChanged) {
            WidgetsUtils.Log.v(TAG, "start - media Uri has changed");
            if(null != mediaplayer) {
                WidgetsUtils.Log.v(TAG, "start - mediaplayer instance found => reset it");

                if(isInErrorState) {
                    Log.w(TAG, "start - player is is error state, we need to create a new one");
                    mediaplayer.release();
                    mediaplayer = null;
                }
                else {
                    mediaplayer.reset();
                }
            }
            createMediaplayer(this.context);

            /** Reset audio track list as it does not correspond to the media anymore */
            audioTracksList = null;  /** It will be updated in the onPrepared callback */

            if(null == mediaplayer) {
                WidgetsUtils.Log.e(TAG, "start - Impossible to create mediaplayer. Phone is probably rooted");
                return;
            }

            videoUriChanged = false;
            preparePlayerForPlayback();
        }
        else {
            WidgetsUtils.Log.v(TAG, "start - media Uri has not changed");
            if(null != mediaplayer) {
                internalStart();
            }
            else {
                WidgetsUtils.Log.w(TAG, "Warning, start called with no MediaPlayer instance !");
            }
        }
    }

    private boolean preparePlayerForPlayback() {
        boolean prepareOk = true;
        try {
            if(!surfaceCreated) {
                prepareMediaPlayerOnSurfaceCreated = true;
                return false;
            }

            try {
                String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//                //http://search.spotxchange.com/vast/2.0/85394?VPI=mp4&player_width=800&player_height=640&app[name]=danet&app[domain]=danet.vn&app[bundle]=com.movideo.whitelabel&device[ifa]=9817239871298379812
                //String vastUrl = "http://search.spotxchange.com/vast/2.0/163250?VPI=mp4&pla yer_width=800&player_height=640&app[name]=danet&app[domain]=danet.vn&app[bundle]=com.movideo.whitelabel&device[ifa]=" + deviceId;
                //mediaplayer.vastSetUrl(vastUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Set proxy parameters
            mediaplayer.setProxyType(this.proxyType);
            if(proxyType > 0) {
                mediaplayer.setProxyPort(this.proxyPort);
                mediaplayer.setProxyUrl(this.proxyUrl);
            }

            mediaplayer.setDRMCertificatePath("file:///android_asset/");
            if (null != DRMCustomRights){
                mediaplayer.setDRMCustomRights(DRMCustomRights);
            }

            setMarlinDecryptorEnabled(mbMarlinProtected);

            if(null != securedHLSParam2) {
                mediaplayer.setVerimatrixBootAddress(securedHLSParam2);
            }
            if(null != securedHLSParam1) {
                mediaplayer.setVerimatrixCompanyName(securedHLSParam1);
            }

            if(null != httpAuthUsername && null != httpAuthPassword) {
                mediaplayer.setHttpAuthentication(httpAuthUsername,httpAuthPassword);
            }
            if(null != SSLCACertFilePath && Constants.SSL_TYPE_UNKNOWN != SSLCACertFileType) {
                mediaplayer.setSSLCACert(SSLCACertFilePath,SSLCACertFileType);
            }
            if(null != SSLClientCertFilePath && Constants.SSL_TYPE_UNKNOWN!= SSLClientCertFileType &&
               null != SSLClientKeyFilePath && Constants.SSL_TYPE_UNKNOWN != SSLClientKeyFileType) {
                mediaplayer.setSSLClientCert(this.SSLClientCertFilePath,this.SSLClientCertFileType,this.SSLClientKeyFilePath,this.SSLClientKeyFileType);
            }
            if(null != httpCustomHeader) {
                mediaplayer.setHttpCustomHeader(httpCustomHeader);
            }

            try {
                if(null != currentAudioTrackName) {
                    Log.d(TAG, "preparePlayerForPlayback - audio track: " + currentAudioTrackName);
                    mediaplayer.setAudioTrack(currentAudioTrackName);
                }
                if(LVSubtitle.Type_WEBVTT == subtitleTrackType && null != subtitleTrackName) {
                    mediaplayer.setSubtitleTrack(subtitleTrackName, subtitleTrackType);
                }

                if(liveSeekingEnabled) {
                    WidgetsUtils.Log.d(TAG,  "preparePlayerForPlayback - LiveSeeking enabled");
                    mediaplayer.setHttpStreamingLiveSeekingEnabled(true);
                }
//                else {
//                    mediaplayer.setHttpStreamingLiveSeekingEnabled(false);
//                }
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }

            mediaplayer.setVideoExperienceBenchmarkEnabled(true);
            mediaplayer.setDataSource(context, mediaUri);
            mediaplayer.setVideoView(videoView);



            if(null != mediaControllerInterface) {
                //TODO: use resource instead of fixed String
                mediaControllerInterface.setNotification(true, "Chuẩn bị phát");
            }
            mediaplayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            prepareOk = false;
        } catch (IllegalStateException e) {
            prepareOk = false;
        } catch (IOException e) {
            prepareOk = false;
        }
        return prepareOk;
    }


    @Override
    public void startAt(double time) {
        resumePlaybackPosition = time;
        start();
    }


    @Override
    public void setDownloadedMediaDuration(int duration){
        mDownloadedMediaDuration = duration;
    }

    private void internalStart() {
        WidgetsUtils.Log.v(TAG, "internalStart START");
        if(null != mediaplayer) {

            if(resumePlaybackPosition > 0) {
                mediaplayer.startAt(resumePlaybackPosition);
                resumePlaybackPosition = 0;
            }
            else {
                mediaplayer.start();
            }
            if(videoExperienceEffectMode > 0) {
                Message msg = Message.obtain(null, 0, 0, 0);
                videoExpHandler.sendMessageDelayed(msg, 50);
            }
            canSeek = true;
            if(null != mediaControllerInterface) {
                mediaControllerInterface.setPlayPauseBtnState(PlayPauseBtnState.PAUSE);
                mediaControllerInterface.setNotification(false, null);
                mediaControllerInterface.startAutoHideTimer();
                int mediaDuration = getDuration();
                if(0 == mediaDuration) {
                    liveSeekingWindow = mediaplayer.getHttpStreamingLiveSeekingWindow();
                    if(liveSeekingEnabled && (null != liveSeekingWindow)) {
                        mediaControllerInterface.setMaxPlaybackCursorProgressValue(liveSeekingWindow[1] - liveSeekingWindow[0]);
                        mediaControllerInterface.setPlaybackCursorPosition(liveSeekingWindow[1]);
                        mediaControllerInterface.setSeekBarVisibility(true);
                        mediaControllerInterface.setLiveSeekingMode(true);
                    } else {
                        mediaControllerInterface.setSeekBarVisibility(false);
                        mediaControllerInterface.setMediaDuration(0);
                    }
                } else {
                    mediaControllerInterface.setSeekBarVisibility(true);
                    mediaControllerInterface.setMediaDuration(mediaDuration);
                    mediaControllerInterface.setMaxPlaybackCursorProgressValue(mediaDuration);
                }
                if(!secondaryProgressFromOutside) {
                    mediaControllerInterface.setSecondaryProgress(0);
                }
            }
            startProgressTimer();
        }
        WidgetsUtils.Log.v(TAG, "internalStart END");
    }

    @Override
    public void stop() {
        // nothing to do (stop is not functionnal in HLS
    }


    /**
     * Sets the video stream Uri
     * @param uri Uri of the video stream to play
     * @param ctx context of the application
     */
    public final void setVideoURI(Uri uri, Context ctx) {

        if(uri.equals(mediaUri)) {
            WidgetsUtils.Log.w(TAG, "setVideoURI - same uri as current");
            return;
        }
        videoUriChanged = true;
        this.mediaUri = uri;
        this.context = ctx;
        this.securedHLSParam1 = null;
        this.securedHLSParam2 = null;
        videoExperienceEffectMode = 0;
        currentDisplayMode = LVMediaPlayer.DISPLAY_MODE_FIT;

        subtitleFilePath = null;
        subtitleTrackName = null;
        subtitleTrackType = LVSubtitle.Type_Unknown;
    }

    /**
     * @return the Uri of the currently handled video stream (if any)
     */
    public final Uri getVideoUri() {
        return this.mediaUri;
    }

    /**
     * Sets the path of the file to use to read subtitles.<br/>
     * The file will be used directly is a media is already playing back;
     * otherwise, the path will be memorized and used for next playback.<br/>
     * To stop reading from a subtitle file, just call the method with a null parameter.
     * @param filePath path of the file containing subtitles
     */
    public final void setSubtitleFile(String filePath) {
        subtitleFilePath = filePath;
        subtitleTrackName = null;
        subtitleTrackType = LVSubtitle.Type_SUB_FILE;
        Log.d(TAG, "setSubtitleFile " + subtitleFilePath);
        if(null != mediaplayer) {
            internalSetSubtitle();
        }
    }

    public final void setSubtitleTrack(String trackName, int trackType) {
        if (subtitleTrackName != trackName){
            subtitleTrackName = trackName;
            subtitleTrackType = trackType;
            if(null != mediaplayer) {
                internalSetSubtitleTrack();
            }
        }
    }

    private final void internalSetSubtitleTrack() {
        if(null != mediaplayer && null != this.subtitleTrackName) {
            boolean wasPlaying = false;
            if(mediaplayer.isPlaying()) {
                wasPlaying = true;
                mediaplayer.pause();
                //FIXME: trick to by-pass non-synchronous pause()
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            try {
                mediaplayer.setSubtitleTrack(this.subtitleTrackName, this.subtitleTrackType);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
            if(wasPlaying) {
                mediaplayer.start();
            }
            mediaplayer.setSubtitleVisibility(true);
            this.subtitleView.setText(null);
        }
    }


    /**
     * Sets the bitrates limits for HTTP Live Streaming use cases
     * @param minBitrate minimum bitrate limit to consider for alternate switching in HLS (in kbps)
     * @param maxBitrate maximum bitrate limit to consider for alternate switching in HLS (in kbps)
     * Note: Theses limits are used in best-effort mode.
     * Their effect during playback can be delayed to up to 30 seconds due to internal buffering.
     */
    public final void setHttpBitratesLimits(int minBitrate, int maxBitrate) {
        this.minHLSBitrate = minBitrate;
        this.maxHLSBitrate = maxBitrate;
        if(null != mediaplayer) {
            try {
                mediaplayer.setHttpStreamingMinimumBitrate(this.minHLSBitrate * 1000);
                mediaplayer.setHttpStreamingMaximumBitrate(this.maxHLSBitrate * 1000);
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Indicates what typical bitrate the mediaplayer can expect to find on the data connection when starting the playback.<br/>
     * This indication is useful at startup to choose which alternate to start with more wisely.<br/>
     * This value is used only at start-up time.
     * @param bitrate typical bitrate expected on the data connection (in kbps)
     */
    public final void setTypicalHttpBitrate(int bitrate) {
        this.typicalHLSBitrate = bitrate;
        if(null != mediaplayer) {
            mediaplayer.setHttpStreamingTypicalBitrate(this.typicalHLSBitrate);
        }
    }

    /**
     * Sets the parameters to use for secured HLS authentication (to be called before starting playback)
     * @param param1 first parameter to use to connect to protected server
     * @param param2 second parameter to use to connect to protected server
     * @param param3 third parameter to use handle right
     */
    public final void setSecuredHLSParameters(String param1, String param2, String param3)
    {
        this.securedHLSParam1 = param1;
        this.securedHLSParam2 = param2;
        this.DRMCustomRights  = param3;
        if(null != mediaplayer) {
            if (null != this.securedHLSParam1) {
                mediaplayer.setVerimatrixCompanyName(this.securedHLSParam1);
            }
            if (null != this.securedHLSParam2) {
                mediaplayer.setVerimatrixBootAddress(this.securedHLSParam2);
            }
            if (null != this.DRMCustomRights) {
                mediaplayer.setDRMCustomRights(this.DRMCustomRights);
            }
        }
    }

    public final void setMarlinDecryptorEnabled(boolean bEnabled) {
    	mbMarlinProtected = bEnabled;
        if (null != mediaplayer){
            try {
                mediaplayer.setMarlinDecryptorEnabled(bEnabled, mediaUri.toString().contains("ms3-gen"));
            } catch (UnsupportedOperationException e) {
                Log.v(TAG, "setSecuredHLSParameters - Marlin not supported");
            }
        }
    }

    /**
     * Sets the parameters to use db offline path(to be called before starting playback)
     * @param dbFaasOfflineSquadeo db path
     */
    public final void setDbFaasOfflineSquadeo(String dbFaasOfflineSquadeo)
    {
        this.dbFaasOfflineSquadeo = dbFaasOfflineSquadeo;
        if(null != mediaplayer) {
            HashMap<String, Object> customization = new HashMap<String, Object>();
            customization.put(LVMediaPlayer.CustomMode.FAAS_OFFLINE_MODE, (Object)(dbFaasOfflineSquadeo));
            mediaplayer.setCustomMode(customization);
        }
    }

    /**
     * Sets the parameters to use Authentec authentication (to be called before starting playback)
     * @param httpAuthUsername user name
     * @param httpAuthPassword password
     */
    public final void setHttpAuthParams(String httpAuthUsername, String httpAuthPassword)
    {
        this.httpAuthUsername = httpAuthUsername;
        this.httpAuthPassword = httpAuthPassword;
        if(null != mediaplayer) {
            mediaplayer.setHttpAuthentication(this.httpAuthUsername, this.httpAuthPassword);
        }
    }

    /**
     * Sets the parameters SSL CA Certificate (to be called before starting playback)
     * @param SSLCACertFilePath SSL CA Certificate file path
     * @param SSLCACertFileType SSL CA Certificate file type
     */
    public final void setSSLCACertParams(String SSLCACertFilePath, int SSLCACertFileType)
    {
        this.SSLCACertFilePath = SSLCACertFilePath;
        this.SSLCACertFileType = SSLCACertFileType;
        if(null != mediaplayer) {
            mediaplayer.setSSLCACert(this.SSLCACertFilePath, this.SSLCACertFileType);
        }
    }

    /**
     * Sets the parameters SSL Client Certificate (to be called before starting playback)
     * @param SSLClientCertFilePath SSL Client Certificate file path
     * @param SSLClientCertFileType SSL Client Certificate file type
     * @param SSLClientKeyFilePath SSL Client Certificate key file path
     * @param SSLClientKeyFileType SSL Client Certificate key file type
     */
    public final void setSSLClientCertParams(String SSLClientCertFilePath, int SSLClientCertFileType, String SSLClientKeyFilePath, int SSLClientKeyFileType)
    {
        this.SSLClientCertFilePath = SSLClientCertFilePath;
        this.SSLClientCertFileType = SSLClientCertFileType;
        this.SSLClientKeyFilePath = SSLClientKeyFilePath;
        this.SSLClientKeyFileType = SSLClientKeyFileType;
        if(null != mediaplayer) {
            mediaplayer.setSSLClientCert(this.SSLClientCertFilePath, this.SSLClientCertFileType, this.SSLClientKeyFilePath, this.SSLClientKeyFileType);
        }
    }

    /**
     * Sets the parameter Http Custom Header (to be called before starting playback)
     * @param httpCustomHeader SSL Client Certificate file path
     */
    public final void setHttpCustomHeader(String httpCustomHeader)
    {
        this.httpCustomHeader = httpCustomHeader;
        if(null != mediaplayer) {
            mediaplayer.setHttpCustomHeader(this.httpCustomHeader);
        }
    }

    /**
     * Switches to the next video display mode (looping in this order: fit->stretched->cropped)
     */
    public final void nextDisplayMode() {
        int nextDisplayMode = 0;
        switch(currentDisplayMode) {
        case LVMediaPlayer.DISPLAY_MODE_CROP:
            nextDisplayMode = LVMediaPlayer.DISPLAY_MODE_FIT;
            break;
        case LVMediaPlayer.DISPLAY_MODE_FIT:
            nextDisplayMode = LVMediaPlayer.DISPLAY_MODE_STRETCH;
            break;
        case LVMediaPlayer.DISPLAY_MODE_STRETCH:
            nextDisplayMode = LVMediaPlayer.DISPLAY_MODE_CROP;
            break;
        }
        setDisplayMode(nextDisplayMode);
    }

    /**
     * Sets the video display mode (fit, stretched, cropped)
     * @param displayMode video display mode to use
     */
    public final void setDisplayMode(int displayMode) {
        if(null != mediaplayer) {
            mediaplayer.changeDisplayMode(displayMode);
            currentDisplayMode = displayMode;

            updateClosedCaptionTextHeight();
            updateClosedCaptionViewPadding();
        }
    }

    /**
     * Returns the video display mode currently in use
     * @return video display mode currently in use
     */
    public final int getCurrentDisplayMode() {
        return currentDisplayMode;
    }


    private void internalSetSubtitle() {
        Log.d(TAG, "internalSetSubtitle - " + subtitleFilePath);

        if(null != mediaplayer) {
            Log.d(TAG, "internalSetSubtitle - mediaplayer exists");
            boolean wasPlaying = false;
            if(mediaplayer.isPlaying()) {
                wasPlaying = true;
                mediaplayer.pause();
                // FIXME ugly trick to by-pass non-synchronous pause()
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                }
            }
            mediaplayer.closeSubtitleFile();

            if(null != subtitleFilePath) {
                try {
                    Log.d(TAG, "internalSetSubtitle - open file");
                    try {
                        mediaplayer.openSubtitleFile(subtitleFilePath);
                    } catch (UnsupportedOperationException e) {
                        e.printStackTrace();
                    }
                    mediaplayer.setSubtitleVisibility(true);
                    this.subtitleView.setText(null);
                    //this.subtitleView.setVisibility(VISIBLE);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    WidgetsUtils.Log.w(TAG, "internalSetSubtitle - incorrect subtitle file");
                }
            }else {
                mediaplayer.setSubtitleVisibility(false);
                this.subtitleView.setVisibility(GONE);
                this.closedCaptionView.setVisibility(GONE);
                this.smpteSubtitleView.setVisibility(GONE);
            }
            if(wasPlaying) {
                mediaplayer.start();
            }
        }
    }

    /**
     * Internal clean-up method; stops and release the mediaplayer; free all taken resources.
     */
    private final void internalCleanUp() {

        if(faasOfflineEnabled || null != dbFaasOfflineSquadeo) {
            SharedPreferences mySPrefs = context.getSharedPreferences("quickplayerBenchmarkPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove("lastCodecExceptionType");
            editor.apply();
        }

        if(null != mediaplayer) {
            if(mediaplayer.isPlaying()) {
                mediaplayer.stop();
            }
            mediaplayer.release();
            mediaplayer = null;
        }
        if (null != audioOnlyDrawable) {
            audioOnlyDrawable = null;
        }
    }

    /**
     * Internal method: updates the UI components of the attached LVMediaController with the latest status of the player
     */
    private void updateMediaControllerInterface() {
        if(null != mediaControllerInterface) {
            if(null != mediaplayer) {
                if(mediaplayer.isPlaying()) {
                    mediaControllerInterface.setPlayPauseBtnState(PlayPauseBtnState.PAUSE);
                }
                else {
                    mediaControllerInterface.setPlayPauseBtnState(PlayPauseBtnState.PLAY);
                }

                int mediaDuration = getDuration();
                int currentPosition = mediaplayer.getCurrentPosition();


                if(0 == mediaDuration) {
                    if(liveSeekingEnabled) {
                        liveSeekingWindow = mediaplayer.getHttpStreamingLiveSeekingWindow();
                        mediaControllerInterface.setMaxPlaybackCursorProgressValue(liveSeekingWindow[1] - liveSeekingWindow[0]);
                        int cursorPosition = currentPosition - liveSeekingWindow[0];
                        int liveSeekingProgress = 0;
                        if(liveSeekingWindow[1] - currentPosition > 0) {
                            /* set negative time */
                            liveSeekingProgress = currentPosition - liveSeekingWindow[1];
                        }
                        mediaControllerInterface.setPlaybackCursorPosition(cursorPosition);
                        mediaControllerInterface.setPlaybackTime(liveSeekingProgress);
                        mediaControllerInterface.setSeekBarVisibility(true);
                    }
                    else {
                        mediaControllerInterface.setSeekBarVisibility(false);
                    }
                }
                else {
                    mediaControllerInterface.setMediaDuration(mediaDuration);
                    mediaControllerInterface.setMaxPlaybackCursorProgressValue(mediaDuration);
                    mediaControllerInterface.setPlaybackCursorPosition(currentPosition);
                    mediaControllerInterface.setPlaybackTime(currentPosition);
                    mediaControllerInterface.setSeekBarVisibility(true);
                }
                mediaControllerInterface.updateBufferingIndication(currentBuffering);
            }
            else {
                mediaControllerInterface.setSeekBarVisibility(false);
                mediaControllerInterface.setPlayPauseBtnState(PlayPauseBtnState.DISABLE);
                mediaControllerInterface.setMediaDuration(0);
                mediaControllerInterface.setPlaybackCursorPosition(0);
                if(!secondaryProgressFromOutside) {
                    mediaControllerInterface.setSecondaryProgress(0);
                }
            }
        }
    }
private int triedTime = 0;
    /**
     * Internal method: starts the playback and update the associated controller interface if any
     */
    private void internalOnPrepared() {
        boolean error = false;
        try{
            triedTime ++;
            WidgetsUtils.Log.v(TAG, "internalonPrepared START");
            if(null != mediaplayer) {
                if(null != videoView) {
                    videoView.setBackgroundColor(Color.TRANSPARENT);
                }
                setDisplayMode(currentDisplayMode);

                mediaplayer.setLooping(this.loopingPlayback);

                setSubtitlesOnPrepared();

                audioTracksList = mediaplayer.getAudioTracks();

                if(inEventMode) {
                    Log.i(TAG, "Event start mode = Ask > wait for external trigger");
                    return;
                }
                else if (mbAutoStart) {
                    internalStart();
                    if(videoExperienceEffectMode > 0) {
                        Message msg = Message.obtain(null, 0, 0, 0);
                        videoExpHandler.sendMessageDelayed(msg, 50);
                    }
                }
                else {
                    //to avoid opening notification after onResume
                    if(null != mediaControllerInterface) {
                        mediaControllerInterface.setNotification(false, null);
                    }
                }
            }
            WidgetsUtils.Log.v(TAG, "internalOnPrepared END");
        } catch (Exception ex){
            Log.e(TAG, ex.getMessage());
            error = true;
        }
        if (error && triedTime < 5){
            try {
                Thread.sleep(1000);
                internalOnPrepared();//try again after wait for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (triedTime > 5)
            Toast.makeText(context, "Có lỗi xảy ra, xin vui lòng tắt và bật lại ứng dụng, chúng tôi rất xin lỗi vì sự bất tiện này", Toast.LENGTH_LONG).show();

    }

    private void setSubtitlesOnPrepared() {
        Log.d(TAG, "setSubtitlesOnPrepared " + subtitleFilePath);
        if(null != subtitleFilePath) {
            try {
                mediaplayer.openSubtitleFile(subtitleFilePath);
                mediaplayer.setSubtitleVisibility(true);
                //call setsubtitletrack except for webvtt. For webvtt, the tracks must be selected before prepare().
                if(LVSubtitle.Type_WEBVTT != subtitleTrackType && null != this.subtitleTrackName) {
                    mediaplayer.setSubtitleTrack(subtitleTrackName, subtitleTrackType);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }

        }
        else {
            if(null != this.subtitleTrackName) {
                try {
                    mediaplayer.setSubtitleVisibility(true);
                    mediaplayer.setSubtitleTrack(subtitleTrackName, subtitleTrackType);
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Internal method: pauses the playback and update the associated controller interface if any
     */
    private void internalPause() {
        if(null != mediaplayer) {
            mediaplayer.pause();
            cancelProgressTimer();
            if(null != mediaControllerInterface) {
                mediaControllerInterface.setPlayPauseBtnState(PlayPauseBtnState.PLAY);
            }
        }
    }

    /**
     * Creates and sets up the internal LVMediaPlayer
     * @param context context of the application
     * @return true if the creation went well, false otherwise
     */
    private final boolean createMediaplayer(Context context) {

        if (null == mediaplayer) {
            WidgetsUtils.Log.v(TAG, "createMediaplayer - LVMediaPlayer is null => create it");

            try {
                mediaplayer = new LVMediaPlayer(context);
                mediaplayer.setVideoView(null);
                isInErrorState = false;
            } catch (IllegalStateException e) {
                Log.e(TAG, "Impossible to create LVMediaPlayer instance, device is probably rooted");
                return false;
            }
        }

        // sets the bitrates for HLS playback, only if explicitely set by integrator
        try {
            if(-1 != maxHLSBitrate) {
                mediaplayer.setHttpStreamingMaximumBitrate(maxHLSBitrate * 1000);
            }
            if(-1 != minHLSBitrate) {
                mediaplayer.setHttpStreamingMinimumBitrate(minHLSBitrate * 1000);
            }
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }

        computeHLSTypicalBitrate();
        mediaplayer.setHttpStreamingTypicalBitrate(typicalHLSBitrate * 1000);

        if(null == mediaplayerListener) {
            mediaplayerListener = new InternalMediaPlayerListener();
        }
        // set all listeners to the internal listener
        mediaplayer.setOnInfoListener(mediaplayerListener);
        mediaplayer.setOnErrorListener(mediaplayerListener);
        mediaplayer.setOnPreparedListener(mediaplayerListener);
        mediaplayer.setOnVideoSizeChangedListener(mediaplayerListener);
        mediaplayer.setOnSeekCompleteListener(mediaplayerListener);
        mediaplayer.setOnBufferingUpdateListener(mediaplayerListener);
        mediaplayer.setOnDownloadUpdateListener(mediaplayerListener);
        mediaplayer.setOnCompletionListener(mediaplayerListener);

        // screen will stay on while playing back
        mediaplayer.setScreenOnWhilePlaying(true);

        if(0!= customNetworkMode) {
            HashMap<String, Object> customization = new HashMap<String, Object>();
            customization.put("ZeroLagAlternateSwicthDemo", 1);
            mediaplayer.setCustomMode(customization);

            mediaplayer.setHttpStreamingTypicalBitrate((minHLSBitrate + maxHLSBitrate) * 500);
        }

        if(faasOfflineEnabled || null != dbFaasOfflineSquadeo) {
            String pathDataBase;
            WidgetsUtils.Log.d(TAG,  "preparePlayerForPlayback - FaasOffline enabled");
            HashMap<String, Object> customization = new HashMap<String, Object>();
            SharedPreferences mySPrefs = context.getSharedPreferences("quickplayerBenchmarkPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mySPrefs.edit();
            editor.remove("lastCodecExceptionType");
            editor.apply();
            if (null != dbFaasOfflineSquadeo) {
                pathDataBase = dbFaasOfflineSquadeo;
            } else {
                pathDataBase = "/sdcard/qp3_medias/dbFaasOfflineSquadeo.zip";
            }
            customization.put(LVMediaPlayer.CustomMode.FAAS_OFFLINE_MODE, (Object)(pathDataBase));
            mediaplayer.setCustomMode(customization);
        }

        return true;
    }

    /**
     * Computes the HLS typical bitrate based on the type of data network available
     */
    private final void computeHLSTypicalBitrate() {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (wifi != null && wifi.isAvailable()) {
            typicalHLSBitrate = Integer.parseInt(prefs.getString(Constants.PREF_TYPICAL_BITRATE, String.valueOf(TYPICAL_HTTP_STREAMING_BITRATE_WIFI)));
        }
        else if (mobile != null && mobile.isAvailable()) {
            typicalHLSBitrate = TYPICAL_HTTP_STREAMING_BITRATE_3G;
        }
        // by default, we assume the worst and use the 3G typical bitrate
        else {
            typicalHLSBitrate = TYPICAL_HTTP_STREAMING_BITRATE_3G;
        }
    }

    /**
     * Internal method: handles the info messages from the media player
     * @param what  info description
     * @param extra addition info description
     */
    private final void internalOnInfo(int what, int extra) {
        WidgetsUtils.Log.d(TAG, "internalOnInfo " + what + ", " + extra);
        Message msg;
        if(null != mediaplayer) {

            switch(what) {
            case LVMediaPlayer.MEDIA_INFO_HTTP_STREAMING:
                handleHTTPStreamingEvent(extra);
                break;
            case LVMediaPlayer.MEDIA_INFO_SUBTITLE:
                msg = Message.obtain(null, 0, extra, 0);
                //remove pending commands and post the new one
                closedCaptionUpdatesHandler.removeMessages(0);
                closedCaptionUpdatesHandler.sendMessage(msg);
                break;
            case LVMediaPlayer.MEDIA_INFO_BENCHMARK:
                handleBenchmarkInfoEvent(extra);
                break;
            case LVMediaPlayer.MEDIA_INFO_DRM:
                handleDRMInfoEvent(extra);
                break;

            default:
                WidgetsUtils.Log.v("internalOnInfo", "Event not treated: " + what + ", " + extra);
                break;
            }
        }
    }

    protected void handleBenchmarkInfoEvent(int extra) {
        if(LVMediaPlayer.MEDIA_INFO_BENCHMARK_STARTED == extra) {
            mediaControllerInterface.setNotification(true, "Đang kiểm tra");
        }
        else if(LVMediaPlayer.MEDIA_INFO_BENCHMARK_DONE == extra) {
            mediaControllerInterface.setNotification(true, "Chuẩn bị phát");
        }
        else {
            Log.w(TAG, "handleBenchmarkInfoEvent - unknown extra value: " + extra);
        }
    }

    protected void handleDRMInfoEvent(int extra) {
        if(LVMediaPlayer.MEDIA_INFO_DRM_RIGHTS_ACQUISITION_BEGIN == extra) {
            mediaControllerInterface.setNotification(true, "License acquisition ongoing...");
        }
        else if(LVMediaPlayer.MEDIA_INFO_DRM_RIGHTS_ACQUISITION_END == extra) {
            mediaControllerInterface.setNotification(true, "Chuẩn bị phát");
        }
        else {
            Log.w(TAG, "handleDRMInfoEvent - unknown extra value: " + extra);
        }
    }

    private final void handleHTTPStreamingEvent(int event) {
        if(LVMediaPlayer.MEDIA_INFO_ALTERNATE_CHANGE == event) {
            WidgetsUtils.Log.d(TAG, "Variant change");
            /*
            if(mediaplayer.isHttpStreamingAlternateAudioOnly()) {
                WidgetsUtils.Log.d(TAG, "Audio-only variant " + audioOnlyDrawable);
                if(null != audioOnlyDrawable) {
                    videoView.setBackgroundDrawable(this.audioOnlyDrawable);
                }
                else {
                    videoView.setBackgroundColor(Color.DKGRAY);
                }
            }
            else {
                videoView.setBackgroundColor(Color.TRANSPARENT);
                videoView.setBackgroundDrawable(null);
            }
            audioTracksList = mediaplayer.getAudioTracks();
            */
        }
        else if (LVMediaPlayer.MEDIA_INFO_EVENT_PLAYLIST_START == event) {
            inEventMode = true;
            if(null != mediaControllerInterface) {
                mediaControllerInterface.setLiveBtnMode(ButtonMode.ENABLED);
            }
            Toast.makeText(context, "Event playlist detected", Toast.LENGTH_SHORT).show();
        } else if (LVMediaPlayer.MEDIA_INFO_EVENT_PLAYLIST_STOP == event) {
            inEventMode = false;
            if(null != mediaControllerInterface) {
                mediaControllerInterface.setLiveBtnMode(ButtonMode.HIDDEN);
            }
            handleDurationChange();
            Toast.makeText(context, "End of event detected", Toast.LENGTH_SHORT).show();
        } else if (LVMediaPlayer.MEDIA_INFO_DURATION_CHANGED == event) {
            handleDurationChange();
        } else if (LVMediaPlayer.MEDIA_INFO_NEW_METADATA_ID3 == event) {
            Message msg = Message.obtain(null, 0, 0, 0);
            //remove pending commands and post the new one
            id3UpdatesHandler.removeMessages(0);
            id3UpdatesHandler.sendMessage(msg);
        }
    }

    private void handleDurationChange() {
        if(null != mediaControllerInterface) {

            liveSeekingWindow = mediaplayer.getHttpStreamingLiveSeekingWindow();
            if(liveSeekingEnabled && (null != liveSeekingWindow)) {
                mediaControllerInterface.setLiveSeekingMode(true);
                mediaControllerInterface.setMaxPlaybackCursorProgressValue(liveSeekingWindow[1] - liveSeekingWindow[0]);
                mediaControllerInterface.setMediaDuration(liveSeekingWindow[1] - liveSeekingWindow[0]);
            }
            else {
                int duration = getDuration();
                mediaControllerInterface.setMediaDuration(duration);
                mediaControllerInterface.setMaxPlaybackCursorProgressValue(duration);
            }
        }
    }

    private void handleID3Metadata() {
        String ConcatenatedText = null;
        LVID3MetadataParser id3Parser = new LVID3MetadataParser();

        boolean success = id3Parser.parse(mediaplayer.getHttpStreamingMetadataId3());
        if (false == success){
            //show an error message in RED if the parsing fails
            //ConcatenatedText = "PARSING ERROR";
            //id3TextView.setTextColor(Color.RED);
            Log.e(TAG, "handleID3Metadata parsing error!!");
            return;
        }else{
            ConcatenatedText = getID3ConcatenatedText(id3Parser);
            id3TextView.setTextColor(Color.WHITE);
        }

        if (null != ConcatenatedText){
            if (ConcatenatedText.startsWith("type:dvbs")) {
                /* Display nothing, keep last ID3 APIC frame displayed */
                id3TextView.setVisibility(View.GONE);
                id3WebView.setVisibility(GONE);
            } else {
                id3TextView.setText(ConcatenatedText);
                id3TextView.setVisibility(View.VISIBLE);
                id3WebView.setVisibility(GONE);
            }
        }else{
            id3TextView.setVisibility(View.GONE);
            id3WebView.setVisibility(GONE);

            id3ImageView.setImageBitmap(id3Parser.getEmbeddedPicture());

            if (null != id3Parser.getEmbeddedPicture()){
                id3ImageView.setVisibility(View.VISIBLE);
            }else{
                id3ImageView.setVisibility(View.GONE);
            }
        }
    }

    public String getID3ConcatenatedText(LVID3MetadataParser id3Parser)
    {
        String concatenatedText = null;
        String concatSeparator = "/";

        if (null != id3Parser.getArtist()){
            concatenatedText = (concatenatedText != null ? concatenatedText : "") + id3Parser.getArtist() + concatSeparator;
        }
        if (null != id3Parser.getTitle()){
            concatenatedText = (concatenatedText != null ? concatenatedText : "") + id3Parser.getTitle() + concatSeparator;
        }
        if (null != id3Parser.getTextInfo()){
            concatenatedText = (concatenatedText != null ? concatenatedText : "") + id3Parser.getTextInfo() + concatSeparator;
        }
        if (null != id3Parser.getPrivOwnerIdentifier()){
            concatenatedText = (concatenatedText != null ? concatenatedText : "") + id3Parser.getPrivOwnerIdentifier() + concatSeparator;
        }
        if (null != id3Parser.getGeobSubtitle()){
            concatenatedText = (concatenatedText != null ? concatenatedText : "") + id3Parser.getGeobSubtitle() + concatSeparator;
        }
        if (null != concatenatedText){
            //remove last separator
            concatenatedText = concatenatedText.substring(0, concatenatedText.length() - concatSeparator.length());
        }

        return concatenatedText;
    }

    private void resetID3Views(){
        id3TextView.setText(null);
        id3TextView.setVisibility(View.GONE);
        id3ImageView.setImageBitmap(null);
        id3ImageView.setVisibility(View.GONE);
    }

    /**
     * Handles events from the player about subtitling
     * @param subtitleInfo info
     */
    private final void handleSubtitleEvent(int subtitleInfo) {
        Log.d(TAG, "handleSubtitleEvent " + subtitleInfo);
        if(LVMediaPlayer.MEDIA_INFO_NEW_SUBTITLE == subtitleInfo) {

            LVSubtitle subtitle = null;
            if(null != mediaplayer) {
                try {
                    subtitle = mediaplayer.getSubtitle();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    subtitle = null;
                }
            }
            if (null != subtitle) {
                if(subtitle.getType() == LVSubtitle.Type_CC_CEA608) {
                    Log.d(TAG, "handleSubtitleEvent - CEA_608");
                    subtitleView.setVisibility(GONE);
                    smpteSubtitleView.setVisibility(GONE);
                    LVClosedCaptionFormatter.formatTextForCEA608Attributes(subtitle, closedCaptionView);
                    closedCaptionView.setVisibility(VISIBLE);
                }else if(subtitle.getType() == LVSubtitle.Type_SMPTE) {
                    Log.d(TAG, "handleSubtitleEvent - SMPTE");
                    closedCaptionView.setVisibility(GONE);
                    subtitleView.setVisibility(GONE);
                    LVSubtitleFormatter.formatSubtitleAttribute(videoView, subtitle, smpteSubtitleView);
                    smpteSubtitleView.setVisibility(VISIBLE);
                } else {
                    Log.d(TAG, "handleSubtitleEvent - Plain subtitle");
                    closedCaptionView.setVisibility(GONE);
                    smpteSubtitleView.setVisibility(GONE);
                    subtitleView.setText(Html.fromHtml(subtitle.getTextHtml()));
                    subtitleView.setVisibility(VISIBLE);
                }
                WidgetsUtils.Log.d(TAG, "updateSubtitleDisplay - new subtitle = " + subtitle.getTextHtml());
            }else {
                closedCaptionView.setVisibility(GONE);
                smpteSubtitleView.setVisibility(GONE);
                subtitleView.setVisibility(GONE);
                id3WebView.setVisibility(GONE);
            }
        }else if (LVMediaPlayer.MEDIA_INFO_NO_SUBTITLE == subtitleInfo) {
            closedCaptionView.setVisibility(GONE);
            smpteSubtitleView.setVisibility(GONE);
            subtitleView.setVisibility(GONE);
            id3WebView.setVisibility(GONE);
        }
    }

    /**
     * Internal method: update progression of the playback on the associated controller interface if any
     */
    private final void internalUpdateProgress() {
        if((null != mediaControllerInterface) && (null != mediaplayer)) {
            int currentPosition = mediaplayer.getCurrentPosition();
            if((0 == getDuration()) && liveSeekingEnabled) {
                liveSeekingWindow = mediaplayer.getHttpStreamingLiveSeekingWindow();
                if(null != liveSeekingWindow) {
                    mediaControllerInterface.setMaxPlaybackCursorProgressValue(liveSeekingWindow[1] - liveSeekingWindow[0]);
                    mediaControllerInterface.setPlaybackCursorPosition(currentPosition - liveSeekingWindow[0]);
                    int liveSeekingProgress = 0;
                    if(liveSeekingWindow[1] - currentPosition > 0) {
                        /* set negative time */
                        liveSeekingProgress = currentPosition - liveSeekingWindow[1];
                    }
                    currentPosition = liveSeekingProgress;
                    mediaControllerInterface.setLiveBtnMode(ButtonMode.ENABLED);
                }
                mediaControllerInterface.setPlaybackTime(currentPosition);
            }
            else if (0 != getDuration()){
                mediaControllerInterface.setPlaybackCursorPosition(currentPosition);
                mediaControllerInterface.setPlaybackTime(currentPosition);
            }
        }
    }

    /**
     * Internal method: update download (in progressive download use case)
     * @param percentage percentage of file already downloaded
     */
    private final void internalOnDownloadUpdate(int percentage) {
        if((null != mediaControllerInterface) &&
                (null != mediaplayer) &&
                !secondaryProgressFromOutside) {
            mediaControllerInterface.setSecondaryProgress(percentage * getDuration() / 100);
        }
    }

    /**
     * Internal method: Update the associated controller (if any) about the re-buffering
     * @param percent new buffering percentage value (guaranteed to reach 100%)
     */
    private final void internalOnBufferingUpdate(int percent) {
        Log.d(TAG, "internalOnBufferingUpdate " + percent);
        currentBuffering = percent;
        if(null != mediaControllerInterface) {
            mediaControllerInterface.updateBufferingIndication(percent);
        }
    }

    /**
     * Internal method: Manage the end of the playback and update the associated controller interface if any
     * @param mp reference on the mediaplayer
     */
    private final void internalOnCompletion(MediaPlayer mp) {
        cancelProgressTimer();
        resetID3Views();
        if(null != mediaControllerInterface) {
            mediaControllerInterface.setPlaybackCursorPosition(0);
            mediaControllerInterface.setPlaybackTime(0);
            mediaControllerInterface.setPlayPauseBtnState(PlayPauseBtnState.PLAY);
        }
    }

    /**
     * Internal method: manage seek action completion
     */
    private final void internalOnSeekComplete() {
        WidgetsUtils.Log.d(TAG, "internalOnSeekComplete");
        if(null != mediaplayer) {
            if(mediaplayer.isHttpStreamingAlternateAudioOnly()) {
                WidgetsUtils.Log.d(TAG, "audio only");
                if(null != audioOnlyDrawable) {
                    this.setBackgroundDrawable(this.audioOnlyDrawable);
                }
            }
            else {
                WidgetsUtils.Log.d(TAG, "audio video");
                this.setBackgroundDrawable(null);
            }
        }
    }

    /**
     * Cancels the progress update timer
     */
    private final void cancelProgressTimer() {
        if(null != progressUpdateTimer) {
            progressUpdateTimer.cancel();
            progressUpdateTimer = null;
        }
    }

    /**
     * Starts then progress update timer
     */
    private void startProgressTimer() {
        progressUpdateTimer = new ProgressUpdateCDT(Long.MAX_VALUE, PROGRESS_UPDATE_TIMER_PERIOD);
        progressUpdateTimer.start();
    }

    /**
     * Loads the resources needed by the component (drawables, etc.)
     * @param context context of the application
     */
    private void loadResources(Context context) {

        audioOnlyDrawableId =  WidgetsUtils.findResourceIdInContext(context, "lvwidget_bg_audio_only", "drawable");
        if (audioOnlyDrawableId > 0) {
            audioOnlyDrawable = context.getResources().getDrawable(audioOnlyDrawableId);
        }
    }

    /**
     * Sets the drawable to use when playing an audio-only stream. <br/>
     * Set to null to use the default Drawable (or dark screen if not found).
     * @param d Drawable to be used as background (will be stretched to fill the LVVideoView complete surface)
     */
    public void setAudioOnlyDrawable(Drawable d) {
        this.audioOnlyDrawable = d;
        if((null == d) && (audioOnlyDrawableId > 0)) {
            audioOnlyDrawable = context.getResources().getDrawable(audioOnlyDrawableId);
        }
    }

    /**
     * Sets the listener to notify in case of playback errors. This listener comes in addition to the internal
     * one and does not modify the behavior of the LVVideoView itself.
     * @param listener listener to notify, use null to unregister a listener
     */
    public final void setOnErrorListener(LVMediaPlayer.OnErrorListener listener) {
        if(null != listener) {
            this.errorListener = new WeakReference<OnErrorListener>(listener);
        }
        else {
            this.errorListener = null;
        }
    }

    /**
     * Sets the listener to notify with all the information events from the player. This listener comes in
     * addition to the internal one and does not modify the behavior of the LVVideoView itself.
     * @param listener listener to notify, use null to unregister a listener
     */
    public final void setOnInfoListener(LVMediaPlayer.OnInfoListener listener) {
        if(null != listener) {
            this.infoListener = new WeakReference<OnInfoListener>(listener);
        }
        else {
            this.infoListener = null;
        }
    }

    /**
     * Sets the listener to notify when the playback is completed. This listener comes in
     * addition to the internal one and does not modify the behavior of the LVVideoView itself.
     * @param listener listener to notify, use null to unregister a listener
     */
    public final void setOnCompletionListener(LVMediaPlayer.OnCompletionListener listener) {
        if(null != listener) {
            this.completionListener = new WeakReference<OnCompletionListener>(listener);
        }
        else {
            this.completionListener = null;
        }
    }

    /**
     * Sets the listener to notify with buffering events. This listener comes in
     * addition to the internal one and does not modify the behavior of the LVVideoView itself.
     * @param bufferingupdateListener listener to notify, use null to unregister a listener
     */
    public final void setBufferingupdateListener(
            LVMediaPlayer.OnBufferingUpdateListener bufferingupdateListener) {
        if(null != bufferingupdateListener) {
            this.bufferingupdateListener = new WeakReference<OnBufferingUpdateListener>(bufferingupdateListener);
        }
        else {
            this.bufferingupdateListener = null;
        }
    }

    /**
     * Sets the listener to notify with downloads updates. This listener comes in
     * addition to the internal one and does not modify the behavior of the LVVideoView itself.
     * @param downloadUpdateListener listener to notify, use null to unregister a listener
     */
    public final void setDownloadUpdateListener(
            LVMediaPlayer.OnDownloadUpdateListener downloadUpdateListener) {
        if(null != downloadUpdateListener) {
            this.downloadUpdateListener = new WeakReference<OnDownloadUpdateListener>(downloadUpdateListener);
        }
        else {
            this.downloadUpdateListener = null;
        }
    }

    /**
     * Sets the listener to notify when the size of the video changes. This listener comes in
     * addition to the internal one and does not modify the behavior of the LVVideoView itself.
     * @param videoSizeChangedListener listener to notify, use null to unregister a listener
     */
    public final void setVideoSizeChangedListener(
            LVMediaPlayer.OnVideoSizeChangedListener videoSizeChangedListener) {
        if(null != videoSizeChangedListener) {
            this.videoSizeChangedListener = new WeakReference<OnVideoSizeChangedListener>(videoSizeChangedListener);
        }
        else {
            this.videoSizeChangedListener = null;
        }
    }

    /**
     * Sets the listener to notify when a media is prepared and ready to play. This listener comes in
     * addition to the internal one and does not modify the behavior of the LVVideoView itself.
     * @param preparedListener listener to notify, use null to unregister a listener
     */
    public final void setOnPreparedListener(
            LVMediaPlayer.OnPreparedListener preparedListener) {
        if(null != preparedListener) {
            this.preparedListener = new WeakReference<OnPreparedListener>(preparedListener);
        }
        else {
            this.preparedListener = null;
        }
    }


    public void setSeekCompleteListener(
            LVMediaPlayer.OnSeekCompleteListener seekCompleteListener) {
        if(null != seekCompleteListener) {
            this.seekCompleteListener = new WeakReference<OnSeekCompleteListener>(seekCompleteListener);
        }
        else {
            this.seekCompleteListener = null;
        }
    }

    public void setOnDoubleTapListener(OnDoubleTapListener doubleTapListener) {
        if(null != doubleTapListener) {
            this.onDoubleTapListener = new WeakReference<OnDoubleTapListener>(doubleTapListener);
        }
        else {
            this.onDoubleTapListener = null;
        }
    }

    /**
     * Sets the media controller interface that will be driven by the video view
     * @param mediaControllerInterface
     */
    public final void setLVMediaControllerInterface(LVMediaControllerInterface mediaControllerInterface) {
        Log.d(TAG, "setLVMediaControllerInterface - " + mediaControllerInterface);
        if(null != mediaControllerInterface) {
            this.mediaControllerInterface = mediaControllerInterface;
            updateMediaControllerInterface();
        }
        else {
            this.mediaControllerInterface = null;
        }
    }

    /**
     * WARNING, be EXTRA careful when using the player object returned. It should only be used to retrieve informations.
     * Avoid setting anything as much as possible (setting listeners is most definitely to avoid).
     * @return the internal LVMediaPlayer instance.
     */
    public final LVMediaPlayer getMediaPlayer() {
        return this.mediaplayer;
    }


    public final String[] getAvailableSubtitleTracks(int subtitleType) {
        LVSubtitleTrack[] allTracks = null;
        String[] subtitleTracks = null;
        if(null != mediaplayer && LVSubtitle.Type_Unknown != subtitleType) {
            allTracks = mediaplayer.getSubtitleTracks();
            if (null != allTracks){
                int nbSubFileTracks = 0;
                for (int i=0; i< allTracks.length; i++){
                    if (subtitleType == allTracks[i].getSubtitleType()){
                        nbSubFileTracks++;
                    }
                }
                if (0 < nbSubFileTracks){
                    int index = 0;
                    subtitleTracks = new String[nbSubFileTracks];
                    for (int i=0; i< allTracks.length; i++){
                        if (subtitleType == allTracks[i].getSubtitleType()){
                            subtitleTracks[index++] = allTracks[i].getName();
                        }
                    }
                }
            }
        }
        return subtitleTracks;
    }

    /**
     * @return path of the file used to read subtitles from
     */
    public String getSubtitleFilePath() {
        return subtitleFilePath;
    }

    public int getCurrentSubtitleTrackType() {
        return subtitleTrackType;
    }

    public void setAudioTrack(String trackName) {
        WidgetsUtils.Log.d(TAG, "setAudioTrack: " + trackName);
        if(trackName != currentAudioTrackName) {
            currentAudioTrackName = trackName;
            if(null != mediaplayer) {
                try {
                    mediaplayer.setAudioTrack(trackName);
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getCurrentAudioTrackName() {

        /** Retrieve the audio track list to be aligned with the media player */
        audioTracksList = mediaplayer.getAudioTracks();

        /** If the list is available check the flag indicating the selected track */
        if(null != audioTracksList)
        {
            for(int i = 0; i < audioTracksList.length; i++)
            {
                if(audioTracksList[i].IsSelected())
                {
                    this.currentAudioTrackName = audioTracksList[i].getName();
                }
            }
        }

        return this.currentAudioTrackName;
    }

    public LVAudioTrack[] getAudioTracks() {
        if(null != mediaplayer) {
            return mediaplayer.getAudioTracks();
        }
        else
            return null;
    }


    public boolean setVideoExperienceConfig(int mode, int value) throws UnsupportedOperationException {
        if(null != mediaplayer) {
                try {
                    //Automatic settings with a boost factor
                    WidgetsUtils.Log.v(TAG, "LVVideoView - setVideoExperienceConfig: + " + mode + ", value="+value);
                    mediaplayer.setVideoExperienceConfig(mode, value);
                }
                catch(UnsupportedOperationException e) {}
                if(false == mediaplayer.isPlaying())
                {
                    /** If we are in pause the GLSurfaceview renderer in mode RENDERMODE_WHEN_DIRTY (manual) */
                    /** So manually trig a call to the renderer Draw() function to see the effect of new config. */
                    if (null != videoView.getGLSurfaceView()){
                        videoView.getGLSurfaceView().requestRender();
                    }
                }
                videoExperienceEffectValue = value;
                videoExperienceEffectMode = mode;
                return true;
        }
        else {
            return false;
        }
    }

    public boolean canVideoExperienceBeEnabled(){
        if(null != mediaplayer) {
                try {
                    WidgetsUtils.Log.v(TAG, "LVVideoView - canVideoExperienceBeEnabled");
                    return mediaplayer.canVideoExperienceBeEnabled();
                }
                catch(UnsupportedOperationException e){
                    return false;
                }
                catch(IllegalStateException e){
                    return false;
                }
        }
        else {
            return false;
        }
    }

    public boolean setId3MetadataRetrieval(boolean enabled){
        if(null != mediaplayer) {
            mediaplayer.setHttpStreamingMetadataId3Enabled(enabled);
            if (true == enabled){
                id3Layout.setVisibility(View.VISIBLE);
            }else{
                id3Layout.setVisibility(View.GONE);
                id3TextView.setVisibility(View.GONE);
                id3ImageView.setVisibility(View.GONE);
                id3TextView.setText(null);
                id3ImageView.setImageBitmap(null);
                id3WebView.setVisibility(View.GONE);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public boolean isInEventMode() {
        return inEventMode;
    }

    public EventStartMode getEventStartMode() {
        return this.eventStartMode;
    }

    public final void setAutoStart(boolean bAutoStart) {
        WidgetsUtils.Log.v(TAG, "setAutoStart START");
        mbAutoStart = bAutoStart;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        internalCleanUp();
    }


    /**
     * Computes the height of the text for the Closed Caption TextView, given the surface size and the video size
     */
    private void updateClosedCaptionTextHeight() {

        int   mSurfaceHeight = 0; /** The surface height */
        int   mSurfaceWidth  = 0; /** The surface width */
        int   mDisplayHeight = 0; /** The video scaled height*/
        int   mDisplayWidth  = 0; /** The video scaled width*/
        int   mVideoHeight   = 0; /** The original video height*/
        int   mVideoWidth    = 0; /** The original video width*/
        float mTextHeight    = 0; /** The text height calculated */

        /** Retrieve the surface size attributes that will be used for scale ratio calculation */
        if ( null != videoView)
        {
            mSurfaceHeight = videoView.getHeight();
            mSurfaceWidth  = videoView.getWidth();
        }
        //DEBUG Log.d(TAG, ">>> \n - updateSubtitleTextHeight mSurfaceHeight=" + mSurfaceHeight + " \tmSurfaceWidth="  + mSurfaceWidth + " \tmSurfaceRatio= " + mSurfaceRatio);

        /** Retrieve the video size attributes that will be used for scale ratio calculation */
        if ( null != mediaplayer)
        {
            mVideoHeight = mediaplayer.getVideoHeight();
            mVideoWidth  = mediaplayer.getVideoWidth();
        }

        //DEBUG Log.d(TAG, "   updateSubtitleTextHeight mVideoHeight=" + mVideoHeight + " \tmVideoWidth="  + mVideoWidth + " \tmVideoRatio= " + mVideoRatio);

        if(( 0 != mVideoHeight ) && (0 != mVideoWidth))
        {
            /** Calculate both scale ratio to know how video will be scaled to screen */
            float ratioH = ((float)mSurfaceHeight/mVideoHeight);
            float ratioW = ((float)mSurfaceWidth/mVideoWidth);
            /** in case of "full screen fit" the real ratio will be the smaller one */

            /** Apply ratio to know the video display size */
            /** Smaller one is used otherwise video would be streched out of bounds */
            if(ratioH < ratioW)
            {
                //DEBUG Log.d(TAG, "   updateSubtitleTextHeight ratio1 < ratio2 CASE1 (ratio1= "+ratio1 + ")  ratio2= " + ratio2);
                mDisplayWidth   = (int)(mVideoWidth  * ratioH);
                mDisplayHeight  = (int)(mVideoHeight  * ratioH);
            }else{
                //DEBUG Log.d(TAG, "   updateSubtitleTextHeight ratio1 > ratio2 CASE2 ratio1= "+ratio1 + "  (ratio2= " + ratio2 + ")");
                mDisplayWidth   = (int)(mVideoWidth  * ratioW);
                mDisplayHeight  = (int)(mVideoHeight  * ratioW);
            }
        }

        mTextHeight = (float) ( ((float)mDisplayHeight / 15.0 /** linePerScreen */) * (0.85  /** Text use 85% of line line height */));
        closedCaptionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)mTextHeight );

        Log.d(TAG, "updateClosedCaptionTextHeight mVidDispHeight=" + mDisplayHeight + " \tmVidDispWidth=" + mDisplayWidth + " \tmTextHeight="  + mTextHeight);
    }


    /**
     * Computes the padding to apply to the Closed Caption TextView to correctly position it on top of the
     * video rendering surface
     */
    private void updateClosedCaptionViewPadding() {

        int   mSurfaceHeight        = 0; /** The surface height */
        int   mSurfaceWidth         = 0; /** The surface width */
        int   mDisplayHeight        = 0; /** The video scaled height*/
        int   mDisplayWidth         = 0; /** The video scaled width*/
        int   mVideoHeight          = 0; /** The original video height*/
        int   mVideoWidth           = 0; /** The original video width*/
        float mSurfaceRatio         = 0; /** The aspect ratio of the surface */
        float mVideoRatio           = 0; /** The aspect ratio of the video itself */
        int   mDisplayPaddingTopBottom = 0; /** The padding that will be applied to bottom (and top)*/
        int   mDisplayPaddingSides   = 0; /** The padding that will be applied to left (and right) */


        /** Retrieve the surface size attributes that will be used for scale ratio calculation */
        if ( null != videoView)
        {
            mSurfaceHeight = videoView.getHeight();
            mSurfaceWidth  = videoView.getWidth();

            /** Calculate surface aspect ratio for future padding position choice */
            mSurfaceRatio  = (float)mSurfaceHeight / (float)mSurfaceWidth;
        }

        /** Retrieve the video size attributes that will be used for scale ratio calculation */
        if ( null != mediaplayer)
        {
            mVideoHeight = mediaplayer.getVideoHeight();
            mVideoWidth  = mediaplayer.getVideoWidth();

            /** Calculate video aspect ratio for future padding position choice */
            mVideoRatio  = (float) mVideoHeight/(float)mVideoWidth;
        }

        if(( 0 != mVideoHeight ) && (0 != mVideoWidth))
        {
            /** Calculate both scale ratio to know how video will be scaled to screen */
            float ratioH = ((float)mSurfaceHeight/mVideoHeight);
            float ratioW = ((float)mSurfaceWidth/mVideoWidth);
            /** in case of "full screen fit" the real ratio will be the smaller one */

            /** Apply ratio to know the video display size */
            /** Smaller one is used otherwise video would be streched out of bounds */
            if(ratioH < ratioW)
            {
                mDisplayWidth   = (int)(mVideoWidth  * ratioH);
                mDisplayHeight  = (int)(mVideoHeight * ratioH);
            }else{
                mDisplayWidth   = (int)(mVideoWidth  * ratioW);
                mDisplayHeight  = (int)(mVideoHeight * ratioW);
            }
        }
        if(LVMediaPlayer.DISPLAY_MODE_FIT == currentDisplayMode ) {

            /** Compare ratio to know where padding will be */
            if(mVideoRatio < mSurfaceRatio) {
                /***************
                 *   ______   Example:
                 *   |    |   Surface ratio = 2
                 *   |XXXX|   Video ration = 1
                 *   |XXXX|
                 *   |____|
                 *
                 **************/
                /** Padding will be top and bottom */
                mDisplayPaddingTopBottom = (mSurfaceHeight - mDisplayHeight)/2;    // Distance from bottom of screen to bottom of video
            } else {
                /***************
                 *   __________   Example:
                 *   |  XXXX  |   Surface ratio = 0,5
                 *   |__XXXX__|   Video ration = 1
                 *
                 **************/
                /** Padding will be left and right */
                mDisplayPaddingSides  = (mSurfaceWidth - mDisplayWidth)/2;    // Distance from bottom of screen to bottom of video
            }
        } else {
            /** FULLSCREEN_CROPPED, FULLSCREEN_STRETCHED modes */
            mDisplayPaddingTopBottom = 0;    // Distance from bottom of screen to bottom of video
            mDisplayPaddingSides = 0;   // Distance from bottom of screen to bottom of video
        }
        closedCaptionView.setPadding(mDisplayPaddingSides , mDisplayPaddingTopBottom , mDisplayPaddingSides , mDisplayPaddingTopBottom);

        Log.d(TAG, "updateSubtitlePadding mSurfaceHeight=" + mSurfaceHeight + "\tmSurfaceWidth="  + mSurfaceWidth + "\tmSurfaceRatio= " + mSurfaceRatio);
        Log.d(TAG, "updateSubtitlePadding mVideoHeight=" + mVideoHeight + "\tmVideoWidth=" + mVideoWidth + "\tmVideoRatio= " + mVideoRatio);
        Log.d(TAG, "updateSubtitlePadding mVidDispHeight=" + mDisplayHeight + "\tmVidDispWidth=" + mDisplayWidth + "\tmDisplayPaddingBottom="  + mDisplayPaddingTopBottom);
    }


    /**
     * Set parameters that will be used for this VideoView. Parameters will be taken into account with
     * next new URI
     * @param paramSet
     */
    public void setParams(LVParamSet paramSet) {

        String proxyTypeParam = paramSet.getParam(LVParamSet.PROXY_TYPE_KEY);
        proxyType = 0;
        if(null != proxyTypeParam) {

            try {
                proxyType = Integer.parseInt(proxyTypeParam);
            }
            catch (NumberFormatException e) {}

            if(proxyType > 0) {
                proxyUrl = paramSet.getParam(LVParamSet.PROXY_HOST_KEY);
                String proxyPortP = paramSet.getParam(LVParamSet.PROXY_PORT_KEY);
                if(null != proxyPortP) {
                    proxyPort = Integer.parseInt(proxyPortP);
                }
            }
        }

        String eventStartMode = paramSet.getParam(LVParamSet.EVENT_START_MODE_KEY);
        if(null != eventStartMode) {

            if(eventStartMode.equalsIgnoreCase("ask")) {
                this.eventStartMode = EventStartMode.ASK;
            }
            else if(eventStartMode.equalsIgnoreCase("begin")) {
                this.eventStartMode = EventStartMode.BEGIN;
            }
            else {
                this.eventStartMode = EventStartMode.LIVE;
            }
        }

        String liveSeekingEnabledKey = paramSet.getParam(LVParamSet.LIVE_SEEKING_ENABLED_KEY);
        if(null != liveSeekingEnabledKey) {
            if(liveSeekingEnabledKey.equalsIgnoreCase("true")) {
                liveSeekingEnabled = true;
            }
            else {
                liveSeekingEnabled = false;
            }
        }

        String faasOfflineEnabledKey = paramSet.getParam(LVParamSet.FAAS_OFFLINE_ENABLED_KEY);
        if(null != faasOfflineEnabledKey) {
            if(faasOfflineEnabledKey.equalsIgnoreCase("true")) {
                faasOfflineEnabled = true;
            }
            else {
                faasOfflineEnabled = false;
            }
        }

        String loopingP = paramSet.getParam(LVParamSet.LOOPING_PLAYBACK_KEY);
        if(null != loopingP) {
            if(loopingP.equalsIgnoreCase("true")) {
                this.loopingPlayback = true;
            }
            else {
                this.loopingPlayback = false;
            }
        }

        String secondaryProgressP = paramSet.getParam("secondaryProgressFromOutside");
        if(null != secondaryProgressP) {
            if(secondaryProgressP.equalsIgnoreCase("true")) {
                this.secondaryProgressFromOutside = true;
            }
            else {
                this.secondaryProgressFromOutside = false;
            }
        }
        else {
            this.secondaryProgressFromOutside = false;
        }

        String customNetwork = paramSet.getParam(LVParamSet.CUSTOM_NETWORK_MODE);
        if(null != customNetwork) {
            customNetworkMode = Integer.parseInt(customNetwork);
        }
    }
    
    public void restoreVideoView() {
        if(null != mediaplayer && null != videoView) {
        	mediaplayer.setVideoView(videoView);
        }
    }


    public void hideSubtitle(){
        if (mediaplayer != null && this.subtitleView != null && this.closedCaptionView != null && this.smpteSubtitleView != null){
            mediaplayer.setSubtitleVisibility(false);
            this.subtitleView.setVisibility(GONE);
        }
    }

    public void showSubtitle(){
        if (mediaplayer != null && this.subtitleView != null){
            mediaplayer.setSubtitleVisibility(true);
            this.subtitleView.setVisibility(VISIBLE);
        }
    }

}
