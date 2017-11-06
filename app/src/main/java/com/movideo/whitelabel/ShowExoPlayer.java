package com.movideo.whitelabel;

/**
 * Created by BHD on 10/2/2017.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.ima.ImaAdsMediaSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.lifevibes.lvmediaplayer.LVMediaPlayer;
import com.movideo.baracus.model.media.ProductStream;
import com.movideo.baracus.model.media.VariantStream;
import com.movideo.baracus.model.product.Product;
import com.movideo.baracus.model.user.User;
import com.movideo.whitelabel.communication.AuthenticationParser;
import com.movideo.whitelabel.communication.AuthenticationUserResultReceived;
import com.movideo.whitelabel.database.DatabaseInfo;
import com.movideo.whitelabel.database.MediaMetaInfoDB;
import com.movideo.whitelabel.util.DialogEventListeners;
import com.movideo.whitelabel.util.Utils;
import com.movideo.whitelabel.util.ViewUtils;
import com.movideo.whitelabel.util.sharedPreference.PreferenceHelper;
import com.movideo.whitelabel.view.AddTwoButtonDialogView;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;

public class ShowExoPlayer extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ShowExoPlayer.class.getSimpleName();
    private long progress = 0;
    private int heartbeat;
    private String streamCode;
    private String trailer;

    public static final String MEDIA_URI = "mediaUri";
    public static final String COMPANY_NAME = "companyName";
    public static final String BOOT_ADDRESS = "bootAddress";

    private Uri mediaUri;

    /**
     * Exo player
     */
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private MediaSource mMediaSource;
    private ImageView danetLogo;
    private ImaAdsLoader imaAdsLoader;

    private DefaultBandwidthMeter bandwidthMeterA;
    private DefaultDataSourceFactory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private long resumePlaybackPosition = -1;

    private String productId = null;
    private String deviceId = null;
    private String variant = null;
    private final String deviceType = "android";
    private String episodeId = "";

    private boolean wasPlaying = false;

    private long currentPlaybackPosition = -1;

    private long lastReportedPosition = 0;

    MainActivity mActivity= new MainActivity();

    public static final String
            PRODUCT_ID = "PRODUCT_ID",
            VARIANT = "VARIANT",
            API_BASE_URL = "API_BASE_URL",
            EPISODE_ID = "EPISODE_ID",
            TRAILER = "TRAILER",
            WAS_PLAYING = "WAS_PLAYING",
            HEARTBEAT = "HEARTBEAT";

    private Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"on create");
        super.onCreate(savedInstanceState);
        toast = Toast.makeText(getBaseContext(), "", Toast.LENGTH_SHORT);

        Window w = this.getWindow();
        if (null != w) {
            w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Log.w(TAG, "onCreate - getWindow returns null");
        }
        //mActivity.checkForPhoneStatePermission();
        //deviceId = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        Intent intent = this.getIntent();
        if (intent != null && savedInstanceState == null) {
            Bundle eventBus = intent.getExtras();
            if (eventBus != null) {
                productId = eventBus.getString(PRODUCT_ID);
                variant = eventBus.getString(VARIANT, "HD");
                if (eventBus.containsKey(EPISODE_ID))
                    episodeId = eventBus.getString(EPISODE_ID);
                if (eventBus.containsKey(TRAILER))
                    trailer = eventBus.getString(TRAILER);
            }



        }
        else{
            if (null != savedInstanceState){
                String mediaUriString = savedInstanceState.getString(MEDIA_URI);
                if (mediaUriString != null)
                    mediaUri = Uri.parse(mediaUriString);
                productId = savedInstanceState.getString(PRODUCT_ID);
                variant = savedInstanceState.getString(VARIANT);
                episodeId = savedInstanceState.getString(EPISODE_ID);
                trailer = savedInstanceState.getString(TRAILER);
                wasPlaying = savedInstanceState.getBoolean(WAS_PLAYING);
                heartbeat = savedInstanceState.getInt(HEARTBEAT);
            }
        }

        setContentView(R.layout.activity_show_exo_player);

        /**
         * Set up Exo Player
         */
        // 1. Create a default TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();


        // 3. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.videoView);

        //Set media controller
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();

        danetLogo = (ImageView) findViewById(R.id.media_datnet_logo_img);

        // Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);
        String adTagUrl = "https://pubads.g.doubleclick.net/gampad/live/ads?sz=640x480&iu=%2F21635228936%2FDANET-Web-PreRoll-Unilever-Glee&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
        adTagUrl = adTagUrl.replace("%%CACHEBUSTER%%", String.valueOf(System.currentTimeMillis()));
        imaAdsLoader = new ImaAdsLoader(this,
                Uri.parse(adTagUrl));

        //Measures bandwidth during playback. Can be null if not required.
        bandwidthMeterA = new DefaultBandwidthMeter();

        //Produces DataSource instances through which media data is loaded.
        dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "DANET"), bandwidthMeterA);

        //Produces Extractor instances for parsing the media data.
        extractorsFactory = new DefaultExtractorsFactory();
        setListener();


        /**
         * Get product stream
         */
        try {
            Product product = ContentHandler.getInstance().getProductById(WhiteLabelApplication.getInstance().getAccessToken(), productId);
            //mediaController.setMediaName(product.getTitle());

            if (product != null
                    && product.getOfferings() != null
                    && product.getOfferings().size() > 0
                    && product.getOfferings().get(0).getType().equals("AVOD")){
                danetLogo.setVisibility(View.GONE);
            }
            else {
                danetLogo.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (trailer == null){
            ProductStreamTask productStreamTask = new ProductStreamTask();
            Utils.executeInMultiThread(productStreamTask);
        }
        else {
            mediaUri = Uri.parse(trailer);
            startPlayback();
        }
    }


    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        //registerReceiver(this.batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // Re-enable autoStart mode
        if (null != player) {
            player.setPlayWhenReady(false);
//            videoView.setAutoStart(true);
        }

//        videoView.restoreVideoView();

        if (mediaUri != null){
            resumePlayback();
        }
        else {
            startPlayback();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (null != player) {
            wasPlaying = true;
            player.release();
            //videoView.stop();
            //videoView.removeAllViewsInLayout();
        }
        Log.d(TAG,"ON BACK PRESS");
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
        int currentProductId;
        if (productId != null)
        {
            try{
                currentProductId = Integer.parseInt(productId);
                ContentHandler.getInstance().reportPause(currentProductId, currentPlaybackPosition, streamCode);
                //update last reported position
                lastReportedPosition = currentPlaybackPosition;
            }
            catch (Exception ex)
            {
                Log.e(TAG, "report playback position error");
                ex.printStackTrace();
            }
        }

        // Disable autoStart mode
        if(null != player)
            player.setPlayWhenReady(false);

        pauseAllPlayers();
        recordLastPositionIfNecessary();
    }

    private void setListener(){
        player.addListener(new ExoPlayer.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {
                Log.v(TAG, "Listener-onTimelineChanged...");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "Listener-onTracksChanged...");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.v(TAG, "Listener-onLoadingChanged...isLoading:"+isLoading);

                Log.e(TAG, "ad is playing : " + player.isPlayingAd());
                if(player.isPlayingAd())
                    danetLogo.setVisibility(View.INVISIBLE);
                else
                    danetLogo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState);
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.v(TAG, "Listener-onRepeatModeChanged...");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if(error.type == ExoPlaybackException.TYPE_SOURCE)
                    showToast("File not found on server, trying to recover Url...");
                else
                    showToast("Error playback, trying to recover Url...");
                player.stop();
                player.prepare(mMediaSource);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onPositionDiscontinuity() {
                Log.v(TAG, "Listener-onPositionDiscontinuity...");
                if(player.isPlayingAd())
                    danetLogo.setVisibility(View.INVISIBLE);
                else
                    danetLogo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "Listener-onPlaybackParametersChanged...");
            }
        });
    }

    private void preparePlayback() {
        Log.d(TAG,"prepare playback");
        String mediaScheme = mediaUri.getScheme();
        if (null != mediaScheme) {
            if (mediaScheme.startsWith("http")) {
                boolean dataConnectionAvailable = WidgetsUtils.isDataConnectionAvailable(getApplicationContext());
                if (!dataConnectionAvailable) {
                    WidgetsUtils.Log.w(TAG, "start - No data connection available !!");
                    showToast("Warning, no data connection available.");

                }
            }
        }
    }
    private void startPlayback() {
        Log.e(TAG,"start playback");
        if (null != this.mediaUri) {
            Log.d("Okhttp stream uri", this.mediaUri.toString());
            preparePlayback();
            Log.e(TAG, "Progress : " + progress);
            if (progress > 0 ){
                AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(ShowExoPlayer.this, "Xem tiếp", "Quí khách có muốn xem tiếp từ phút thứ " + (progress/60 + 1) + " ?", "Xem tiếp", "Xem từ đầu", new DialogEventListeners() {
                    @Override
                    public void onPositiveButtonClick(DialogInterface dialog) {
                        play(progress*1000);
                    }

                    @Override
                    public void onNegativeButtonClick(DialogInterface dialog) {
                        play(0);
                    }
                });
                dialogView.show();
            }
            else {
                play(0);
            }
        } else {
            showToast("Waiting for media...");
        }
    }


    private void play(long startAt) {
        Log.d(TAG,"play at " + startAt);

        MediaSource videoSource = new HlsMediaSource(mediaUri, dataSourceFactory, 1, null, null);
        if(imaAdsLoader != null && !wasPlaying)
            mMediaSource = new ImaAdsMediaSource(
                    videoSource,
                    dataSourceFactory,
                    imaAdsLoader,
                    simpleExoPlayerView.getOverlayFrameLayout());
        else
            mMediaSource= videoSource;



        player.prepare(mMediaSource);
        if (startAt == 0)
        {
            //videoView.start();
        }
        else {
            player.seekTo(startAt);
            //videoView.startAt(startAt);
        }
        player.setPlayWhenReady(true);

        int currentProductId;
        if (productId != null){
            currentProductId = Integer.parseInt(productId);
            try {
                ContentHandler.getInstance().reportPlaying(currentProductId, currentPlaybackPosition, streamCode);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void resumePlayback() {
        Log.d(TAG,"resume playback");
        if (null != player && wasPlaying) {
            preparePlayback();
            getResumePlaybackPositionIfExisted();
            play(resumePlaybackPosition);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        Log.d(TAG,"on save instance state");
        super.onSaveInstanceState(outState);

        if (mediaUri != null) {
            outState.putString(MEDIA_URI, mediaUri.toString());
        }
        outState.putString(PRODUCT_ID, productId);
        outState.putString(VARIANT, variant);
        outState.putString(EPISODE_ID, episodeId);
        outState.putString(TRAILER, trailer);
        outState.putBoolean(WAS_PLAYING, wasPlaying);
        outState.putInt(HEARTBEAT, heartbeat);


    }

    private void getResumePlaybackPositionIfExisted() {
        Log.d(TAG,"get resume playback position if existed");
        MediaMetaInfoDB database = new MediaMetaInfoDB();
        database.openDatabase(getApplicationContext());
        DatabaseInfo info = database.getAssociatedDataForMedia(mediaUri.toString());
        if (null != info){
            resumePlaybackPosition = info.lastPlaybackPosInMs;
        }
        database.closeDatabase();
    }

    private void recordLastPositionIfNecessary() {
        writeCurrentPlaybackPositionInDb();
    }

    private void writeCurrentPlaybackPositionInDb() {
        if (mediaUri != null && player != null)
        {
            MediaMetaInfoDB database = new MediaMetaInfoDB();

            database.openDatabase(getApplicationContext());
            int playbackPosition = (int) player.getCurrentPosition();
            database.setResumePositionForMedia(mediaUri.toString(), playbackPosition, (int) player.getDuration());

            database.closeDatabase();
        }

    }

    private void pauseAllPlayers() {
        Log.d(TAG,"pause all players");
        if (null != player) {
            wasPlaying = true;
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"on click");
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.d(TAG,"on Info");
        Log.d(TAG,String.valueOf(what));
        Log.d(TAG,String.valueOf(extra));
        int currentProductId = 0;
        switch (what) {
            case LVMediaPlayer.MEDIA_INFO_EVENT_PLAYLIST_START:
                if (productId != null)
                {
                    try{
                        currentProductId = Integer.parseInt(productId);
                    }
                    catch (Exception ex)
                    {
                        showToast("Mã số phim không đúng: " + productId);
                    }
                }

                //report heartbeat
                try {

                    ContentHandler.getInstance().reportPlaying(currentProductId, currentPlaybackPosition, streamCode);

                } catch (Exception e) {
                    Log.e(TAG, "report playback position error");
                    e.printStackTrace();
                }

                //update last reported position
                lastReportedPosition = currentPlaybackPosition;
                break;
            case LVMediaPlayer.MEDIA_INFO_HTTP_STREAMING:
                currentPlaybackPosition = mp.getCurrentPosition();
                if (heartbeat > 0)
                {
                    if ( (currentPlaybackPosition - lastReportedPosition) > (heartbeat*1000 - 1000) ){
                        if (productId != null)
                        {
                            try
                            {
                                currentProductId = Integer.parseInt(productId);
                            }
                            catch (Exception ex)
                            {
                                showToast("Mã số phim không đúng: " + productId);
                            }
                        }

                        //report heartbeat
                        try {

                            ContentHandler.getInstance().reportPlaying(currentProductId, currentPlaybackPosition, streamCode);

                        } catch (Exception e) {
                            Log.e(TAG, "report playback position error");
                            e.printStackTrace();
                        }

                        //update last reported position
                        lastReportedPosition = currentPlaybackPosition;
                    }
                }

                break;
            case LVMediaPlayer.MEDIA_INFO_NETWORK:
                if (LVMediaPlayer.MEDIA_INFO_NETWORK_DOWN == extra) {
                    showToast("Can not connect to server, trying to recover Url...");
                } else if (LVMediaPlayer.MEDIA_INFO_NETWORK_NO_FILE == extra) {
                    showToast("File not found on server, trying to recover Url...");
                } else if (LVMediaPlayer.MEDIA_INFO_NETWORK_UP == extra) {
                    showToast("Url recovered !");
                }
                break;
            default:
                // we don't use other information from the player
                break;
        }
        ;


        return true;
    }

    private void showToast(String message){
        toast.setText(message);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy");
        player.release();
        super.onDestroy();
    }

    /*Call webservice for load season products for this product*/
    public class ProductStreamTask extends AsyncTask<Object, String, ProductStream> {

        private String error = null;
        @Override
        protected void onPreExecute() {
            Log.d(TAG,"on pre execute product stream");
            super.onPreExecute();
            showToast("Xin vui lòng chờ trong giây lát");
        }
        @Override
        protected ProductStream doInBackground(Object... params) {
            Log.d(TAG,"do in background producstream");
            ContentHandler contentHandler = ContentHandler.getInstance();
            ProductStream productStream = null;

            try {
//                Log.d(TAG,"product ID : " + productId + "\n"
//                            + "variant : " + variant + "\n"
//                            + "device type : " + deviceType + "\n"
//                            + "device ID : " + deviceId + "\n"
//                            + "episode ID : " + episodeId + "\n"
//                            + "accessToken : " + WhiteLabelApplication.getInstance().getAccessToken() + "\n");
                productStream = contentHandler.getProductStream(productId, variant, deviceType, deviceId, episodeId);
            } catch (Exception e) {
                error = e.getMessage();
            }
            return productStream;
        }

        @Override
        protected void onPostExecute(ProductStream productStream) {
            Log.d(TAG,"on Post Execute productstream");
            if(error!= null) Log.d("ERROR stream" ,error);
            super.onPostExecute(productStream);
            try {
                if (error == null && productStream != null) {
                    Log.e(TAG, productStream.toString());
                    VariantStream variantStream = productStream.getHd() != null ? productStream.getHd() : productStream.getSd();
                    mediaUri = Uri.parse(variantStream.getSrc());
                    progress = variantStream.getProgress(); //progress in second
                    heartbeat = variantStream.getHeartbeat();
                    streamCode = variantStream.getStream_code();
                    if (wasPlaying)
                    {
                        resumePlayback();
                    }
                    else {
                        startPlayback();

                    }
                } else {
                    if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)) && PreferenceHelper.getSharedPrefData(ShowExoPlayer.this, getResources().getString(R.string.user_is_logged_in))) {
                        User userInfo = PreferenceHelper.getSharedPrefData(ShowExoPlayer.this, getResources().getString(R.string.user_info), User.class);
                        String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                        AuthenticationParser authenticationParser = new AuthenticationParser(ShowExoPlayer.this, new AuthenticationUserResultReceived() {
                            @Override
                            public void onResult(String error, User result) {
                                if (error == null && result != null) {
                                    //showToast(error);
                                } else {
                                    showToast("Thanh toán không thành công, vui lòng thử lại");
                                }
                            }
                        });
                        ViewUtils.execute(authenticationParser, params);
                    } else {
                        Toast.makeText(ShowExoPlayer.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
}
