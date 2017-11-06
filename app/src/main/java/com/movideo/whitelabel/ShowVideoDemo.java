package com.movideo.whitelabel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lifevibes.lvmediaplayer.LVMediaPlayer;
import com.lifevibes.lvmediaplayer.LVMemoryManager;
import com.lifevibes.lvmediaplayer.LVSubtitle;
import com.lifevibes.lvmediaplayer.LVSubtitleTrack;
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
import com.movideo.whitelabel.view.QPMediaController;
import com.movideo.whitelabel.widget.LVParamSet;
import com.movideo.whitelabel.widget.LVVideoView;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;

public class ShowVideoDemo
        extends AppCompatActivity
        implements View.OnClickListener,
        LVMediaPlayer.OnInfoListener,
        LVMediaPlayer.OnPreparedListener,
        LVMediaPlayer.OnBufferingUpdateListener,
        LVMediaPlayer.OnErrorListener,
        LVVideoView.OnDoubleTapListener {

    private static final String TAG = ShowVideoDemo.class.getSimpleName();
    private long progress = 0;
    private int heartbeat;
    private String streamCode;
    private String trailer;

    // Types of streams
    public enum StreamType {
        HTTPSTR_UNKNOWN,    // HTTP Streaming unknown mode
        HTTPSTR_VOD,        // HTTP Streaming in VOD mode
        HTTP_PGDL,          // HTTP Progressive Download mode
        UNKNOWN,
    }

    public static final String MEDIA_URI = "mediaUri";
    public static final String COMPANY_NAME = "companyName";
    public static final String BOOT_ADDRESS = "bootAddress";

    private Uri mediaUri;
    private StreamType mediaType = StreamType.UNKNOWN;

    private LVVideoView videoView;
    private QPMediaController mediaController;
    private ImageView danetLogo;

    private boolean loopingPlayback = false;
    private boolean enableLiveSeeking = false;

    private long resumePlaybackPosition = -1;
    private String drmCustomRights = null;
    private String vmxBootAddress = null;
    private String vmxCompanyName = null;

    private String productId = null;
    private String deviceId = null;
    private String variant = null;
    private final String deviceType = "android";
    private String episodeId = "";

    private boolean wasPlaying = false;

    private LVMemoryManager memoryManager = null;

    private int[] zeroLagBitrates = null;
    private int currentZeroLagBitrate = -1;

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
        loadAppPreferences();
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
                vmxCompanyName = savedInstanceState.getString(COMPANY_NAME);
                vmxBootAddress = savedInstanceState.getString(BOOT_ADDRESS);
                productId = savedInstanceState.getString(PRODUCT_ID);
                variant = savedInstanceState.getString(VARIANT);
                episodeId = savedInstanceState.getString(EPISODE_ID);
                trailer = savedInstanceState.getString(TRAILER);
                wasPlaying = savedInstanceState.getBoolean(WAS_PLAYING);
                heartbeat = savedInstanceState.getInt(HEARTBEAT);
            }
        }

        setContentView(R.layout.activity_show_video_demo);
        videoView = (LVVideoView) findViewById(R.id.videoView);
        mediaController = (QPMediaController) findViewById(R.id.videoController);
        danetLogo = (ImageView) findViewById(R.id.media_datnet_logo_img);


        videoView.setLVMediaControllerInterface(mediaController);
        mediaController.setLVMediaPlayerControl(videoView);

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

        videoView.setOnInfoListener(this);
        videoView.setOnPreparedListener(this);
        videoView.setBufferingupdateListener(this);
        videoView.setOnDoubleTapListener(this);
        videoView.setOnErrorListener(this);



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
        if (null != videoView) {
            videoView.setAutoStart(true);
        }

        videoView.restoreVideoView();

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
        if (null != videoView) {
            wasPlaying = videoView.isPlaying();
            videoView.stop();
            videoView.removeAllViewsInLayout();
        }
        Log.d(TAG,"ON BACK PRESS");
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
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
        if (null != videoView) {
            videoView.setAutoStart(false);
        }

        pauseAllPlayers();
        recordLastPositionIfNecessary();
//        if(null != batteryInfoReceiver) {
//            this.unregisterReceiver(batteryInfoReceiver);
//        }
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

        LVParamSet paramSet = new LVParamSet();
        paramSet.setParam(LVParamSet.EVENT_START_MODE_KEY, "ask");

        if (this.loopingPlayback) {
            paramSet.setParam(LVParamSet.LOOPING_PLAYBACK_KEY, "true");
        }

        if (this.enableLiveSeeking) {
            paramSet.setParam(LVParamSet.LIVE_SEEKING_ENABLED_KEY, "true");
        }

        if (null != this.zeroLagBitrates) {
            paramSet.setParam(LVParamSet.CUSTOM_NETWORK_MODE, "2");
            videoView.setHttpBitratesLimits(zeroLagBitrates[currentZeroLagBitrate] - 50, zeroLagBitrates[currentZeroLagBitrate] + 50);
        }

        videoView.setParams(paramSet);
        videoView.setVideoURI(mediaUri, getApplicationContext());

        Log.d(TAG, "vmxBootAddress : " + vmxBootAddress + "\n" + "vmxCompanyName : " + vmxCompanyName + "\n" + "drmCustomRights : " + drmCustomRights);
        if (null != vmxBootAddress && null != vmxCompanyName) {
            videoView.setSecuredHLSParameters(vmxCompanyName, vmxBootAddress, drmCustomRights);

            // mediaController.setDRMType(QPMediaController.SECURED_HLS_FLAG);

        } else if (null != drmCustomRights) {
            videoView.setSecuredHLSParameters(vmxCompanyName, vmxBootAddress, drmCustomRights);
        }




    }
    private void startPlayback() {
        Log.d(TAG,"start playback");
        if (null != this.mediaUri) {
            Log.d("Okhttp stream uri", this.mediaUri.toString());
            preparePlayback();
            if (progress > 0 ){
                AddTwoButtonDialogView dialogView = new AddTwoButtonDialogView(ShowVideoDemo.this, "Xem tiếp", "Quí khách có muốn xem tiếp từ phút thứ " + (progress/60 + 1) + " ?", "Xem tiếp", "Xem từ đầu", new DialogEventListeners() {
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
        if (startAt == 0)
        {
            videoView.start();
        }
        else {
            videoView.startAt(startAt);
        }

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
        if (null != videoView && wasPlaying) {
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
        outState.putString(COMPANY_NAME, vmxCompanyName);
        outState.putString(BOOT_ADDRESS, vmxBootAddress);
        outState.putString(PRODUCT_ID, productId);
        outState.putString(VARIANT, variant);
        outState.putString(EPISODE_ID, episodeId);
        outState.putString(TRAILER, trailer);
        outState.putBoolean(WAS_PLAYING, wasPlaying);
        outState.putInt(HEARTBEAT, heartbeat);


    }

    private void loadAppPreferences() {
        Log.d(TAG,"load app preferences");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        loopingPlayback = prefs.getBoolean(Constants.PREF_LOOP_MODE, false);
        enableLiveSeeking = prefs.getBoolean(Constants.PREF_ENABLE_LIVE_SEEKING, false);
    }

    private void getResumePlaybackPositionIfExisted() {
        Log.d(TAG,"gte resume playback position if existed");
        MediaMetaInfoDB database = new MediaMetaInfoDB();
        database.openDatabase(getApplicationContext());
        DatabaseInfo info = database.getAssociatedDataForMedia(mediaUri.toString());
        if (null != info){
            resumePlaybackPosition = info.lastPlaybackPosInMs;
        }
        database.closeDatabase();
    }

    private void recordLastPositionIfNecessary() {
        Log.d(TAG,"record last position if necessary");
        if (StreamType.HTTPSTR_UNKNOWN != this.mediaType) {
            writeCurrentPlaybackPositionInDb();
        }
    }

    private void writeCurrentPlaybackPositionInDb() {
        Log.d(TAG,"write current playback position in db");
        if (mediaUri != null && videoView != null)
        {
            MediaMetaInfoDB database = new MediaMetaInfoDB();

            database.openDatabase(getApplicationContext());
            int playbackPosition = videoView.getCurrentPosition();
            Log.d(TAG, "writeCurrentPlaybackPositionInDb: " + playbackPosition);
            database.setResumePositionForMedia(mediaUri.toString(), playbackPosition, videoView.getDuration());

            database.closeDatabase();
        }

    }

    private void pauseAllPlayers() {
        Log.d(TAG,"pause all players");
        if (null != videoView) {
            wasPlaying = videoView.isPlaying();
            videoView.pause();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer player, int bufferPercentage) {
        Log.d(TAG,"on Buffering Update");
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG,"on click");
        if (v.getId() == R.id.lvwidget_mediacontroller_subtitle_btn){
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(ShowVideoDemo.this);
            builderSingle.setIcon(R.drawable.cc_icon);
            builderSingle.setTitle("Vui lòng chọn phụ đề");

            builderSingle.setNegativeButton(
                    "Bỏ qua",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builderSingle.setAdapter(
                    ccAdapter,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String ccName = ccAdapter.getItem(which);
                            if (ccName != "Tắt"){
                                videoView.getMediaPlayer().setSubtitleTrack(ccName, LVSubtitle.Type_WEBVTT);
                                videoView.showSubtitle();
                            }
                            else{
                                videoView.hideSubtitle();

                            }

                        }
                    });
            builderSingle.show();
        }

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int error, int extra) {
        Log.e(TAG, "On error");
        showToast("Playback Error!");
        return true;
    }

    @Override
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

    private  ArrayAdapter<String> ccAdapter;

    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d(TAG,"on prepared");
        LVMediaPlayer lvplayer = (LVMediaPlayer) player;
        int streamDuration = lvplayer.getDuration();
        Log.d(TAG, "onPrepared - streamDuration: " + streamDuration);



        if (StreamType.HTTPSTR_UNKNOWN == mediaType) {
            if (streamDuration > 0) {
                mediaType = StreamType.HTTPSTR_VOD;
            }
        }


        ccAdapter = new ArrayAdapter<String>(
                ShowVideoDemo.this,
                android.R.layout.select_dialog_singlechoice);

        LVSubtitleTrack[] subtitleTracks =  lvplayer.getSubtitleTracks();
        if (subtitleTracks != null)
        {
            for(int i = 0; i < subtitleTracks.length; i++)
            {
                if (subtitleTracks[i].getSubtitleType() == LVSubtitle.Type_WEBVTT){
                    String trackName = subtitleTracks[i].getName();
                    ccAdapter.add(trackName);
                    if (trackName.toLowerCase().contains("vie") || trackName.toLowerCase().contains("vie") ){
                        lvplayer.setSubtitleTrack(trackName, subtitleTracks[i].getSubtitleType());
                        lvplayer.setSubtitleVisibility(true);
                    }
                }
            }
        }



        ImageButton ccButton =  mediaController.getSubtitleButton();
        if (ccAdapter.getCount() == 0){
            ccButton.setVisibility(View.GONE);
        }
        else {
            ccButton.setVisibility(View.VISIBLE);
            ccAdapter.add("Tắt");
        }

        ccButton.setOnClickListener(this);

        //mediaController.setUseHWCodec(LVMediaPlayer.CODECS_TYPE_SW != lvplayer.getCodecsType());


    }
    @Override
    public boolean onDoubleTap(LVVideoView videoView, float x, float y) {
        Log.d(TAG,"on double tap");
        return false;
    }

//    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context arg0, Intent intent) {
//            int batteryLevel = intent.getIntExtra("level", 0);
//
//            if(null != mediaController) {
//                mediaController.setBatteryText(batteryLevel + "%");
//            }
//        }
//    };

    @Override
    protected void onDestroy() {
        Log.w(TAG, "onDestroy");
        if (null != memoryManager) {
            Log.v(TAG, "memory manager dump & destroy");
            if (-1 != memoryManager.getCurrentAlloc()) {
                // Cause a GC before memory leak tracking
                System.gc();
                // Dump the memory leaks (only if the library is not stripped), and free the memory manager
                memoryManager.dump();
            }
            memoryManager.cleanup();
        }
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
            Log.e(TAG ,productStream.getHd().getSrc());
            super.onPostExecute(productStream);
            try {
                if (error == null && productStream != null) {
                    showToast("Đang chuẩn bị phát");
                    VariantStream variantStream = productStream.getHd() != null ? productStream.getHd() : productStream.getSd();
                    mediaUri = Uri.parse(variantStream.getSrc());
                    if (variantStream.getDrm() != null){
                        vmxBootAddress = variantStream.getDrm().getBoot_url();
                        vmxCompanyName = variantStream.getDrm().getCompany_name();
                    }

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
                    if (error != null && error.equals(getResources().getString(R.string.un_authorised_exception_code)) && PreferenceHelper.getSharedPrefData(ShowVideoDemo.this, getResources().getString(R.string.user_is_logged_in))) {
                        User userInfo = PreferenceHelper.getSharedPrefData(ShowVideoDemo.this, getResources().getString(R.string.user_info), User.class);
                        String[] params = {userInfo.getProvider(), userInfo.getIdentifier(), userInfo.getPassword()};
                        AuthenticationParser authenticationParser = new AuthenticationParser(ShowVideoDemo.this, new AuthenticationUserResultReceived() {
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
                        Toast.makeText(ShowVideoDemo.this, error, Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }
}
