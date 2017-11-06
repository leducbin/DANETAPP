package com.movideo.whitelabel.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.lifevibes.LVUtils;
import com.movideo.whitelabel.widgetutils.WidgetsUtils;
import com.movideo.whitelabel.widgetutils.WidgetsUtils.Log;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

/**
 * LifeVibes QuickPlayer equivalent for Android's MediaController widget.<br/>
 * This widget provides pre-packaged controls to drive a mediaplayer implementing the LVMediaPlayerControl interface.<br/>
 * It implements the LVMediaControllerInterface interface to get feedback on media info and playback state.<br/>
 * The visual implementation of the widget is available for customization (lvmediacontroller_widget_layout.xml)
 */
public class LVMediaController extends FrameLayout implements LVMediaControllerInterface {

	/**
	 * Interface a player object must implement to be driven by LVMediaController widget
	 */
	public interface LVMediaPlayerControl {
		/**
		 * @return true if the player authorizes pausing, false otherwise
		 */
		public abstract boolean 	canPause();
		/**
		 * @return true if the player authorizes seeking, false otherwise
		 */
		public abstract boolean 	canSeek();
		/**
		 * @return the percentage of buffer currently available (meaningful only during re-buffering phases)
		 */
		public abstract int 		getBufferPercentage();
		/**
		 * @return the current playback position (in ms) or -1 if irrelevant (not initialized)
		 */
		public abstract int 		getCurrentPosition();
		/**
		 * @return the duration (in ms) of the current stream or -1 if irrelevant (live stream or not initialized)
		 */
		public abstract int 		getDuration();
		/**
		 * @return true if the player is currently playing, false otherwise
		 */
		public abstract boolean 	isPlaying();
		/**
		 * Pauses the current playback
		 */
		public abstract void 		pause();
		/**
		 * Jumps to the desired position in the stream
		 * @param pos time position (in ms) to jump to
		 * @param finalStep set to true if this is the last step of a serie of seek commands (scrubbing use case)
		 */
		public abstract void 		seekTo(int pos, boolean finalStep);
		/**
		 * Starts / resume the playback
		 */
		public abstract void 		start();

		public abstract void startAt(double time);

		/**
		 * Stops playback
		 */
		public abstract void		stop();

		/**
		 * Switches to the next display mode looping in this order: fit > stretched > cropped
		 */
		public abstract void		nextDisplayMode();

		/**
		* Set downloaded media duration
		*/
		public abstract void setDownloadedMediaDuration(int duration);
	}

	/**
	 * Internal listener for events from the widget controls (buttons and seekbar) and animations (controls fade-in and fade-out)
	 */
	private class InternalListener implements OnClickListener, OnSeekBarChangeListener, AnimationListener {

		//private final static String TAG = "LVMediaController$InternalListener";

		@Override
		public void onClick(View v) {
			if (v == playPauseBtn) {
				internalPlayPause();
			} else if (v == displayBtn) {
				internalDisplayMode();
			}
			else if (v == jumpToLiveBtn) {
				if(null != associatedPlayerControl) {
					associatedPlayerControl.get().seekTo(LVMediaControllerInterface.JUMP_TO_LIVE_TIME_CMD, true);
				}
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			internalHandleSeekbarProgress(progress, fromUser);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			internalStartTracking();
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			internalStopTracking(seekBar.getProgress());
		}

		@Override
		public void onAnimationStart(Animation animation) {
			if(null != controlsLayout) {
				if(animation == fadeInAnimation) {
					controlsLayout.setVisibility(VISIBLE);
					controlsLayout.setTag(ControlsState.UI_SHOWING);
				}
				else if(animation == fadeOutAnimation) {
					controlsLayout.setTag(ControlsState.UI_HIDING);
				}
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {}

		@Override
		public void onAnimationEnd(Animation animation) {
			if(null != controlsLayout) {
				if(animation == fadeInAnimation) {
					controlsLayout.setTag(ControlsState.UI_READY_TO_HIDE);
				}
				else if(animation == fadeOutAnimation) {
					controlsLayout.setVisibility(INVISIBLE);
					controlsLayout.setTag(ControlsState.UI_READY_TO_SHOW);
				}
			}
		}
	}

	/**
	 * Runnable task helper for controls hiding.
	 * This makes sure the animations are triggered from the main UI thread.
	 */
	private class AutoHideRunnable implements Runnable {
		@Override
		public void run() {
			hideControls();
		}
	}

	/**
	 * Internal TimerTask to hide the controls
	 */
	private class AutoHideControlsTask extends TimerTask {
		@Override
		public void run() {
			Handler handler = LVMediaController.this.getHandler();
			if(null != handler) {
				handler.post(new AutoHideRunnable());
			}
		}
	}

	// UI visibility
	private static enum ControlsState {
		UI_READY_TO_SHOW,
		UI_READY_TO_HIDE,
		UI_SHOWING,
		UI_HIDING
	}

	private static final String TAG = "LVMediaController";

	// delay for the auto-hide of the controls (in ms)
	private static final int AUTO_HIDE_DELAY = 3000;
	private static final long ANIMATION_DURATION = 200;

	public static final String DEFAULT_BUFFERING_TEXT = "Đang tải";
	public static final String DEFAULT_BUFFERING_SYMBOL = "%";

	private int playBtnDrawableId = 0;
	private int pauseBtnDrawableId = 0;
	/**
	 * this string will be replaced by 'lvwidgets_buffering_str' from strings.xml if present
	 */
	private String bufferingTxt = DEFAULT_BUFFERING_TEXT;
	/**
	 * this string will be replaced by 'lvwidgets_buffering_symbol' from strings.xml if present
	 */
	private String bufferingSymb = DEFAULT_BUFFERING_SYMBOL;

	private Animation fadeInAnimation = null;
	private Animation fadeOutAnimation = null;

	private View controlsLayout;

	private ImageButton playPauseBtn = null;
	private ImageButton displayBtn = null;
	protected TextView elapsedTimeTxt = null;
	protected TextView durationTxt = null;
	protected SeekBar seekbar = null;
	protected FrameLayout notificationLayout = null;
	protected TextView notificationText = null;
	protected ProgressBar notificationWheel = null;
	private Button jumpToLiveBtn = null;

	private InternalListener listener = null;


	protected WeakReference<LVMediaPlayerControl> associatedPlayerControl = null;

	private PlayPauseBtnState playPauseState = PlayPauseBtnState.DISABLE;
	private Timer autoHideTimer = new Timer("autoHideTimer");
	private AutoHideControlsTask autoHideTask = null;

	private int jumpAllowedRangeStartTime = -1;
	private int jumpAllowedRangeEndTime = -1;

    private boolean liveSeekingDuration = false;
    private boolean mbBufferingIndicationEnabled = true;

	/**
	 * Constructor
	 * @param context context of the application
	 */
	public LVMediaController(Context context) {
		super(context);
		Log.d(TAG, "LVMediaController constructor 1");
		initializeControls(context, null);
		loadResources(context);
	}

	/**
	 * Constructor
	 * @param context context of the application
	 * @param attrs UI attributes set
	 */
	public LVMediaController(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(TAG, "LVMediaController constructor 2");
		initializeControls(context, null);
		loadResources(context);
	}

	/**
	 * Initializes all the controls of the widgets. Gets all the references on UI components and maps
	 * internal listeners.
	 * @param context context of the parent Activity
	 * @param attrs attributes sets of the View
	 */
	private void initializeControls(Context context, AttributeSet attrs) {
		Log.d(TAG, "addControls - start");
		int layoutId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_layout", "layout");
		if(0 == layoutId) {
			Log.e(TAG, "Impossible to find widget layout in app resources!");
			return;
		}
		int resId = 0;
		listener = new InternalListener();
		@SuppressWarnings("unused")
		View childView = inflate(context, layoutId, this);

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_playpause_btn", "id");
		if(resId > 0) {
			playPauseBtn = (ImageButton) findViewById(resId);
			playPauseBtn.setOnClickListener(listener);
			playPauseBtn.setEnabled(false);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_display_btn", "id");
		if(resId > 0) {
			displayBtn = (ImageButton) findViewById(resId);
			displayBtn.setOnClickListener(listener);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_elapsed_txt", "id");
		if(resId > 0) {
			elapsedTimeTxt = (TextView) findViewById(resId);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_duration_txt", "id");
		if(resId > 0) {
			durationTxt = (TextView) findViewById(resId);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_seekbar", "id");
		if(resId > 0) {
			seekbar = (SeekBar) findViewById(resId);
			seekbar.setOnSeekBarChangeListener(listener);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_notification_txt", "id");
		if(resId > 0) {
			notificationText = (TextView)findViewById(resId);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_notification_layout", "id");
		if(resId > 0) {
			notificationLayout = (FrameLayout)findViewById(resId);
			notificationLayout.setVisibility(View.GONE);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_notification_wheel", "id");
		if(resId > 0) {
			notificationWheel = (ProgressBar) findViewById(resId);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_controls_layout", "id");
		if(resId > 0 ) {
			controlsLayout = findViewById(resId);
			controlsLayout.setTag(ControlsState.UI_READY_TO_HIDE);
		}

		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_mediacontroller_jumpToLiveBtn", "id");
		if(resId > 0 ) {
			jumpToLiveBtn = (Button)findViewById(resId);
			jumpToLiveBtn.setVisibility(GONE);
			jumpToLiveBtn.setOnClickListener(listener);
		}

		initAdditionalControls(context);
		// add other additional controls here if needed
	}

	protected void initAdditionalControls(Context context) {};

	/**
	 * Loads the resources used by the widgets.
	 * @param context context of the application
	 */
	private void loadResources(Context context) {

		playBtnDrawableId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_play_btn", "drawable");
		pauseBtnDrawableId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_pause_btn", "drawable");

		int resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_buffering_str", "string");
		if(resId > 0) {
			bufferingTxt = context.getText(resId).toString();
		}
		resId = WidgetsUtils.findResourceIdInContext(context, "lvwidget_buffering_symbol", "string");
		if(resId > 0) {
			bufferingSymb = context.getText(resId).toString();
		}

		fadeInAnimation = new AlphaAnimation((float)0.0, (float)1.1);
		fadeInAnimation.setDuration(ANIMATION_DURATION);
		fadeInAnimation.setAnimationListener(listener);

		fadeOutAnimation = new AlphaAnimation((float)1.0, (float)0.0);
		fadeOutAnimation.setDuration(ANIMATION_DURATION);
		fadeOutAnimation.setAnimationListener(listener);

		//TODO: load additional resources here if needed
	}


	/* LVMediaControllerInterface methods */

    /**
     * Sets the duration to display
     * @param duration duration (in ms) that will displayed and used as progression maximum value
     */
    public final void setMediaDuration(int duration) {
        if(duration > 0) {
            if(null != seekbar) {
                seekbar.setMax(duration);
            }
            if(null != durationTxt) {
                durationTxt.setText(LVUtils.convertTimeToText(duration));
                durationTxt.setVisibility(View.VISIBLE);
            }
        }
        else {
            //We are in live
            if(null != durationTxt) {
                durationTxt.setVisibility(View.GONE);
            }
            if(null != elapsedTimeTxt) {
                elapsedTimeTxt.setText("");
            }
        }
    }

    /**
     * Sets the buffering value
     * @param percent buffering percentage
     */
    public final void updateBufferingIndication(int percent) {
        if (mbBufferingIndicationEnabled){
            if(null != notificationText) {
                notificationText.setText(bufferingTxt + " " + percent + " " + bufferingSymb);
            }
            if(null != notificationLayout) {
                if(percent < 100) {
                    notificationLayout.setVisibility(View.VISIBLE);
                }
                else {
                    notificationLayout.setVisibility(View.GONE);
                }
            }
        }
    }

	/**
	 * Sets the notification UI
	 * @param visible true to display the notification area, false otherwise
	 * @param text text to display in the notification area
	 */
	public final void setNotification(boolean visible, String text) {
		if(null != notificationLayout) {
			if(visible) {
				notificationLayout.setVisibility(View.VISIBLE);
			}
			else {
				notificationLayout.setVisibility(View.GONE);
			}
		}
		if(null != text && null != notificationText) {
			notificationText.setText(text);
		}
	}

	/**
	 * Sets the position of the secondary progress in the progression bar (if any)
	 * @param position
	 */
	public final void setSecondaryProgress(int position) {
		if(null != seekbar) {
			if(position <= seekbar.getMax()) {
				seekbar.setSecondaryProgress(position);
			}
		}
	}

	/**
	 * Sets the primary progress in the progression bar (if any)
	 * @param position
	 */
	public final void setPlaybackCursorPosition(int position) {
		if(null != seekbar) {
			if(position <= seekbar.getMax()) {
				seekbar.setProgress(position);
			}
		}
	}

	public final void setPlayPauseBtnState(PlayPauseBtnState state) {
		if(null == this.playPauseBtn) {
			Log.w(TAG, "wrong/no reference for play/pause button");
			return;
		}
		switch(state) {
		case PAUSE:
			this.playPauseBtn.setEnabled(true);
			this.playPauseBtn.setImageResource(pauseBtnDrawableId);
			break;
		case PLAY:
			this.playPauseBtn.setEnabled(true);
			this.playPauseBtn.setImageResource(playBtnDrawableId);
			break;
		case DISABLE:
			this.playPauseBtn.setEnabled(false);
			break;
		}
		playPauseBtn.invalidate();
		playPauseState = state;
	}

	public final void startAutoHideTimer() {
		if(null != autoHideTask) {
			autoHideTask.cancel();
			autoHideTask = null;
		}
		autoHideTimer.purge();
		autoHideTask = new AutoHideControlsTask();
		autoHideTimer.schedule(autoHideTask, AUTO_HIDE_DELAY);
	}


	public final void toggleControlsVisibility() {
		Log.d(TAG, "toggleControlsVisibility - " + controlsLayout );
		if(null != controlsLayout) {
			if(controlsLayout.getTag() == ControlsState.UI_READY_TO_SHOW) {
				Log.d(TAG, "showHideControls - start show");
				controlsLayout.startAnimation(fadeInAnimation);
			}
			else if(controlsLayout.getTag() == ControlsState.UI_READY_TO_HIDE) {
				Log.d(TAG, "showHideControls - start hide");
				controlsLayout.startAnimation(fadeOutAnimation);
			}
		}
	}

	/** --------------------------------------------------------------------------- */

	/**
	 * Sets the reference of the LVMediaPlayerControl that will be driven by the controls
	 * @param playerControl interface to be driven by the controller
	 */
	public final void setLVMediaPlayerControl(LVMediaPlayerControl playerControl) {
		if(null != playerControl) {
			associatedPlayerControl = new WeakReference<LVMediaPlayerControl>(playerControl);
		}
		else {
			associatedPlayerControl = null;
		}
	}

	/** --------------------------------------------------------------------------- */

	/** internal management methods */

	private void internalHandleSeekbarProgress(int progress, boolean fromUser) {
		//Log.d(TAG, "internalHandleSeekbarProgress - " + progress + " user: " + fromUser);
		if(fromUser) {
			if(null != elapsedTimeTxt) {
				if(liveSeekingDuration) {
					elapsedTimeTxt.setText("-"+ LVUtils.convertTimeToText(seekbar.getMax() - progress));
				}
				else {
					elapsedTimeTxt.setText(LVUtils.convertTimeToText(progress));
				}
			}
			if((jumpAllowedRangeStartTime > 0 && progress < jumpAllowedRangeStartTime) ||
					(jumpAllowedRangeEndTime > 0 && progress > jumpAllowedRangeEndTime)) {
				Log.w(TAG, "Trying to jump outside authorized range");
			}
			else {
				if(null != associatedPlayerControl) {
					associatedPlayerControl.get().seekTo(progress, false);
				}
			}
		}
	}

    /**
     * Internal method for playback progression tracking (user generated)
     */
    private void internalStartTracking() {
        Log.d(TAG, "internalStartTracking");
        mbBufferingIndicationEnabled = false;
        if(null != autoHideTask) {
            autoHideTask.cancel();
            autoHideTask = null;
        }
    }

    /**
     * Internal method for playback progression tracking (user generated)
     * @param progress position of progress bar at time of event
     */
    private void internalStopTracking(int progress) {
        Log.d(TAG, "internalStopTracking: " + progress);

        mbBufferingIndicationEnabled = true;

        startAutoHideTimer();

        if((jumpAllowedRangeStartTime > 0 && progress < jumpAllowedRangeStartTime) ||
                (jumpAllowedRangeEndTime > 0 && progress > jumpAllowedRangeEndTime)) {
            Log.w(TAG, "Trying to jump outside authorized range");
        }
        else {
            if(null != associatedPlayerControl) {
                associatedPlayerControl.get().seekTo(progress, true);
            }
        }
    }

	/**
	 * Internal method to handle click on Play/Pause button.
	 */
	private void internalPlayPause() {
		if(null != associatedPlayerControl) {
			if( PlayPauseBtnState.PAUSE == playPauseState) {
				associatedPlayerControl.get().pause();
			}
			else if(PlayPauseBtnState.PLAY == playPauseState) {
				associatedPlayerControl.get().start();
			}
		}
		startAutoHideTimer();
	}


	/**
	 * Internal method to hide controls layout.
	 */
	private final void hideControls() {
		Log.v(TAG, "hideControls");
		if(null != controlsLayout) {
			if(controlsLayout.getTag() == ControlsState.UI_READY_TO_HIDE) {
				Log.d(TAG, "showHideControls - start hide");
				controlsLayout.startAnimation(fadeOutAnimation);
			}
		}
		if(null != autoHideTask) {
			autoHideTask.cancel();
			autoHideTask = null;
		}
	}


	/**
	 * Internal method to handle click on 'Display mode' button
	 */
	private void internalDisplayMode() {
		if(null != associatedPlayerControl) {
			associatedPlayerControl.get().nextDisplayMode();
		}
		startAutoHideTimer();
	}

	@Override
	public void setLiveBtnMode(ButtonMode mode) {
		if(null != jumpToLiveBtn) {
			switch(mode) {
			case DISABLED:
				jumpToLiveBtn.setEnabled(false);
				jumpToLiveBtn.setVisibility(VISIBLE);
				break;
			case ENABLED:
				jumpToLiveBtn.setEnabled(true);
				jumpToLiveBtn.setVisibility(VISIBLE);
				break;
			case HIDDEN:
				jumpToLiveBtn.setVisibility(GONE);
				break;
			}
		}
	}


	@Override
	public void setJumpRestrictionBoundaries(int start, int end) {
		jumpAllowedRangeStartTime = start;
		jumpAllowedRangeEndTime = end;
		if(jumpAllowedRangeEndTime < jumpAllowedRangeStartTime) {
			Log.e(TAG, "setJumpRestrictionBoundaries, inverted boundaries!");
		}
		//TODO what if current position is outside of allowed range ?
	}

	@Override
	public void setMaxPlaybackCursorProgressValue(int maxCursorValue) {
		if(null != seekbar) {
			seekbar.setMax(maxCursorValue);
		}
	}

	@Override
	public void setPlaybackTime(int time) {
		if(null != elapsedTimeTxt) {
			if(time < 0) {
				elapsedTimeTxt.setText("-"+ LVUtils.convertTimeToText(-time));
			} else {
				elapsedTimeTxt.setText(LVUtils.convertTimeToText(time));
			}
		}
	}

	@Override
	public void setSeekBarVisibility(boolean visible) {
		if(null != seekbar) {
			if(visible) {
				seekbar.setVisibility(View.VISIBLE);
			} else {
				seekbar.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void setLiveSeekingMode(boolean liveSeeking) {
		liveSeekingDuration = liveSeeking;
	}
}
