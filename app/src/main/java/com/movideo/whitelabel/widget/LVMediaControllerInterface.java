package com.movideo.whitelabel.widget;

/**
 * Interface a component must implement to get feedback from a LVVideoView component
 * (media informations and playback state feedback)
 */
public interface LVMediaControllerInterface {

    /**
     * possible states for the play/pause button
     */
    public static enum PlayPauseBtnState {
        PLAY,
        PAUSE,
        DISABLE
    }

    public static enum ButtonMode {
        ENABLED,
        HIDDEN,
        DISABLED,
    }

    public static final int MEDIA_DURATION_LIVE = 0;

    public static final int JUMP_TO_LIVE_TIME_CMD = -1;

    /**
     * Will be called with the duration of the current media
     * @param duration duration in ms (can be 0)
     */
    public abstract void setMediaDuration(int duration);

    /**
     * Will be called with updates on the buffering status of the player
     * @param percent buffering percentage, guaranteed to reach 100% at the end of buffering
     */
    public abstract void updateBufferingIndication(int percent);

    /**
     * Will be called to update the notification area
     * @param visible true if the notification area should be made visible, false otherwise
     * @param text text to display in the notification area
     */
    public abstract void setNotification(boolean visible, String text);

    /**
     * Will be called with updates on the playback progression
     * @param position new playback position in ms
     */
    public abstract void setPlaybackCursorPosition(int position);

    /**
     * Sets the absolute playback time playback. This time can be different from the 'seek' cursor position (eg in HLS LiveSeeking mode).
     * @param time time in ms
     */
    public abstract void setPlaybackTime(int time);

    /**
     * Will be called to update the secondary progression in the Seekbar
     * @param position new secondary position (in ms)
     */
    public abstract void setSecondaryProgress(int position);

    /**
     * Will be called with the state of the play/pause button(s) to set to match the player's state
     * @param state new state to apply
     */
    public abstract void setPlayPauseBtnState(PlayPauseBtnState state);

    /**
     * Will be called to toggle the controls visibility
     */
    public abstract void toggleControlsVisibility();

    /**
     * Will be called to initiate the hiding of all controls
     */
    public abstract void startAutoHideTimer();

    public abstract void setLiveBtnMode(ButtonMode mode);

    /**
     * Sets the boundaries to allow/forbid jump in the see bar
     * @param start start time of the allowed range in ms
     * @param end end time of the allowed range in ms
     */
    public abstract void setJumpRestrictionBoundaries(int start, int end);

    /**
     * Sets the maximum possible value for the playback cursor seek bar
     * @param maxCursorValue
     */
    public abstract void setMaxPlaybackCursorProgressValue(int maxCursorValue);

    public abstract void setSeekBarVisibility(boolean visible);

    public abstract void setLiveSeekingMode(boolean liveSeeking);
}
