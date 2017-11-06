package com.movideo.whitelabel.database;

public class DatabaseInfo {

	public static final int HLSDL_STATUS_NONE = 0;
	public static final int HLSDL_STATUS_FULLY_LOCAL = 1;
	public static final int HLSDL_STATUS_DOWNLOADING = 2;
	public static final int HLSDL_STATUS_PAUSED = 3;
	
	public String associatedSubtitleFile;
	public String associatedTimedTextTrack;
	public int associatedTimedTextType;

	public int lastPlaybackPosInMs;
	public int mediaDurationInMs;

	public int hlsDlStatus = HLSDL_STATUS_NONE;

	// when 'hlsDlStatus' = HLSDL_STATUS_NONE, the following three values are irrelevant
	// and might not be accurate
	public int hlsDLBitrate = -1;
	public int hlsDLProgress = -1;
	public String hlsDlLocalServerUrl = null;


	public String toString() {
		return new String("associated sub: " + associatedSubtitleFile + "\n" +
				"sub track: " + associatedTimedTextTrack + "\n" +
				"sub track type: " + associatedTimedTextType + "\n" +
						"duration: " + mediaDurationInMs + "\n" +
								"resume time: " + lastPlaybackPosInMs);
	}
}
