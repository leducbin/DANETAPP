package com.movideo.whitelabel.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lifevibes.lvmediaplayer.LVSubtitle;
import com.movideo.whitelabel.Constants;

public class MediaMetaInfoDB {

	private static final String TAG = "MediaMetaInfoDB";

	// Columns ids for database :
	// Primary key, mandatory in SQLite database (used in all tables)
	private static final String KEY_MAIN_ID			= "_id";

	// Generic columns, common for all medias, used in generic table
	private static final String KEY_MEDIA_URI			= "media_uri";
	private static final String KEY_LAST_STOP_POS		= "last_stop";
	private static final String KEY_SUBTITLE_FILE		= "associated_sub_file";
	private static final String KEY_SUBTITLE_TRACK		= "associated_sub_trackname";
	private static final String KEY_SUBTITLE_TRACKTYPE  = "associated_sub_type";
	private static final String KEY_MEDIA_DURATION		= "duration";

	private static final String KEY_HLSDL_BITRATE		= "hlsdl_bitrate";
	private static final String KEY_HLSDL_PROGRESS		= "hlsdl_progress";
	private static final String KEY_HLSDL_LOCALURL		= "hlsdl_localUrl";
	private static final String KEY_HLSDL_STATUS		= "hlsdl_fullylocal";
	
	

	// Database base name
	private static final String LV_MEDIA_META_INFO_TABLE = "media_meta_info_table";

	private static final String LV_CREATE_MEDIA_META_INFO_TABLE_OP =
		"CREATE TABLE "+
		LV_MEDIA_META_INFO_TABLE + " (" + 
		KEY_MAIN_ID 			+ " integer PRIMARY KEY AUTOINCREMENT, " +
		KEY_MEDIA_URI 			+ " text NOT NULL, " +
		KEY_LAST_STOP_POS		+ " integer NOT NULL DEFAULT -1, " +
		KEY_MEDIA_DURATION		+ " integer NOT NULL DEFAULT 0, " +
		KEY_SUBTITLE_FILE 		+ " text, " +
		KEY_SUBTITLE_TRACK 	+ " text, " +
		KEY_SUBTITLE_TRACKTYPE  + " integer NOT NULL DEFAULT 0, " +
		
		KEY_HLSDL_BITRATE + " integer NOT NULL DEFAULT -1, " +
		KEY_HLSDL_PROGRESS + " integer NOT NULL DEFAULT -1, " +
		KEY_HLSDL_LOCALURL + " text, " +
		KEY_HLSDL_STATUS + " integer NOT NULL DEFAULT 0" +
		");";

	/**
	 * Mutex lock for database access protection
	 */
	@SuppressLint("UseValueOf")
	private static final Integer databaseMutex = new Integer(0);

	private DatabaseHelper databaseHelper;
	private SQLiteDatabase currentDatabase;


	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context)
		{
			super(context, Constants.APP_DATABASE_NAME, null, Constants.APP_DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			try {
				Log.v(TAG, "Creating media list table: \n" + LV_CREATE_MEDIA_META_INFO_TABLE_OP);
				db.execSQL(LV_CREATE_MEDIA_META_INFO_TABLE_OP);
			}
			catch(SQLException sqle) {
				Log.e(TAG, "Error chen creating database tables");
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion
					+ " to "
					+ newVersion);
		}
	}


	/**
	 * Creates a database accessor.
	 */
	public MediaMetaInfoDB() {
		synchronized(databaseMutex) {
			currentDatabase = null;
		}
	}

	/**
	 * Opens the database to make it available for usage.
	 * A call to @see closeDatabase() should be done after use.
	 * @param ctx the activity context from which the database will be accessed.
	 */
	public void openDatabase(Context ctx) {

		if(null != currentDatabase) {
			Log.w(TAG, "Database already opened !");
			return;
		}
		if(null == databaseHelper) {
			databaseHelper = new DatabaseHelper(ctx);
		}
		synchronized(databaseMutex) {
			currentDatabase = databaseHelper.getWritableDatabase();
		}
	}

	/**
	 * Closes the access to the database.
	 */
	public void closeDatabase() {
		if(databaseHelper != null) {
			databaseHelper.close();
			databaseHelper = null;
		}
		synchronized(databaseMutex) {
			currentDatabase = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		closeDatabase();
		super.finalize();
	}


	/**
	 * Creates an entry in the database for the given mediaUri
	 * @param mediaUri	the Uri of the media for the new entry
	 * @return			true if the entry creation was successful, false otherwise 
	 */
	private boolean createNewMediaEntry(String mediaUri) {

		String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);

		ContentValues mediaEntry = new ContentValues();
		mediaEntry.put(KEY_MEDIA_URI, escapedUri);

		try {
			synchronized(databaseMutex) {
				currentDatabase.insertOrThrow(LV_MEDIA_META_INFO_TABLE, null, mediaEntry);
			}
		}
		catch(SQLException sqle) {
			Log.e(TAG, "createNewMediaEntry - Error when adding new media entry");
			sqle.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Creates an entry in the database for the given mediaUri if it does not exists yet.
	 * @param mediaUri	the Uri of the media for the new entry
	 * @return			true if the entry is created or already exists, false otherwise
	 * 					(the entry is not present and could not be created)
	 */
	private boolean createMediaEntryIfNotExisting(String mediaUri) {

		int entryCount;

		String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);

		// Checks for media entry existence in database
		synchronized(databaseMutex) {
			Cursor queryInfos = currentDatabase.query(LV_MEDIA_META_INFO_TABLE,
					new String[]{KEY_MAIN_ID},
					KEY_MEDIA_URI + "=?", new String[]{escapedUri},
					null, null, null);

			entryCount = queryInfos.getCount();
			queryInfos.close();
		}

		if(entryCount > 1) {
			Log.e(TAG, "createMediaEntryIfNotExisting - database corruption");
			return false;
		}
		if(entryCount == 0) {
			return createNewMediaEntry(mediaUri);
		}
		return true;
	}


	/**
	 * Stores the path of the subtitle file associated to the given media.
	 * <br>
	 * <b>Note:&nbsp;</b>
	 * The database entry is automatically created is the media was not previously known.
	 * 
	 * @param mediaUri	Uri of the media
	 * @param subtitlePath	path of the subtitle file to associate
	 * @return			true if the storage went OK, false otherwise (database not opened, data corruption)
	 */
	public synchronized boolean setAssociateSubtitleFileForMedia(String mediaUri, String subtitlePath) {

		if(null == currentDatabase) {
			Log.w(TAG, "setAssociateSubtitleFileForMedia - database not opened !");
			return false;
		}
		synchronized(databaseMutex) {

			int nbRows = 0;
			if(false == createMediaEntryIfNotExisting(mediaUri)) {
				return false;
			}

			ContentValues associatedTimedTextValues = new ContentValues();
			if(null != subtitlePath) {
				associatedTimedTextValues.put(KEY_SUBTITLE_FILE,  subtitlePath);
			}
			else {
				associatedTimedTextValues.putNull(KEY_SUBTITLE_FILE);
			}
			associatedTimedTextValues.putNull(KEY_SUBTITLE_TRACK);
			associatedTimedTextValues.put(KEY_SUBTITLE_TRACKTYPE, LVSubtitle.Type_SUB_FILE);

			String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);

			try {
				nbRows = currentDatabase.update(LV_MEDIA_META_INFO_TABLE, associatedTimedTextValues,
						KEY_MEDIA_URI + "=?", new String[] {escapedUri});
			}
			catch(SQLException sqle) {
				Log.e(TAG, "setAssociateSubtitleFileForMedia - Error when set media information");
				sqle.printStackTrace();
				return false;
			}
			if(nbRows == 1) {
				return true;
			}
		}
		return false;
	}

	public synchronized DatabaseInfo getAssociatedDataForMedia(String mediaUri) {

		if(null == currentDatabase) {
			Log.w(TAG, "getAssociatedDataForMedia - database not opened !");
			return null;
		}

		String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);
		DatabaseInfo info = new DatabaseInfo();

		synchronized(databaseMutex) {

			Cursor queryInfos = currentDatabase.query(LV_MEDIA_META_INFO_TABLE,
					new String[]{KEY_SUBTITLE_FILE, KEY_SUBTITLE_TRACK, KEY_SUBTITLE_TRACKTYPE, KEY_LAST_STOP_POS, KEY_MEDIA_DURATION,
					KEY_HLSDL_BITRATE, KEY_HLSDL_PROGRESS, KEY_HLSDL_LOCALURL, KEY_HLSDL_STATUS
			},
					KEY_MEDIA_URI + "=?", new String[]{escapedUri},
					null, null, null);

			if(queryInfos.getCount() > 1) {
				Log.e(TAG, "getAssociatedDataForMedia - database corruption");
				queryInfos.close();
				return null;
			}

			if(queryInfos.getCount() == 1) {
				queryInfos.moveToFirst();
				info.associatedSubtitleFile = queryInfos.getString(queryInfos.getColumnIndex(KEY_SUBTITLE_FILE));
				info.associatedTimedTextTrack = queryInfos.getString(queryInfos.getColumnIndex(KEY_SUBTITLE_TRACK));
				info.associatedTimedTextType = queryInfos.getInt(queryInfos.getColumnIndex(KEY_SUBTITLE_TRACKTYPE));
				info.lastPlaybackPosInMs = queryInfos.getInt(queryInfos.getColumnIndex(KEY_LAST_STOP_POS));
				info.mediaDurationInMs = queryInfos.getInt(queryInfos.getColumnIndex(KEY_MEDIA_DURATION));
				
				info.hlsDlStatus = queryInfos.getInt(queryInfos.getColumnIndex(KEY_HLSDL_STATUS));
				
				if(info.hlsDlStatus > 0) {

					info.hlsDLBitrate = queryInfos.getInt(queryInfos.getColumnIndex(KEY_HLSDL_BITRATE));
					info.hlsDLProgress = queryInfos.getInt(queryInfos.getColumnIndex(KEY_HLSDL_PROGRESS));
					info.hlsDlLocalServerUrl = queryInfos.getString(queryInfos.getColumnIndex(KEY_HLSDL_LOCALURL));
				}
			}
			queryInfos.close();
		}
		return info;
	}


	/**
	 * Stores the language of the close caption file associated to the given media.
	 * <br>
	 * <b>Note:&nbsp;</b>
	 * The database entry is automatically created is the media was not previously known.
	 * 
	 * @param mediaUri	Uri of the media
	 * @param subTrackName name of the subtitle track to associate
	 * @param subType	   type of the subtitle track to associate
	 * @return			   true if the storage went OK, false otherwise (database not opened, data corruption)
	 */
	public synchronized boolean setAssociatedSubtitleTrackForMedia(String mediaUri, String subTrackName, int subTrackType) {

		if(null == currentDatabase) {
			Log.w(TAG, "setAssociatedSubtitleTrackForMedia - database not opened !");
			return false;
		}
		synchronized(databaseMutex) {

			int nbRows = 0;
			if(false == createMediaEntryIfNotExisting(mediaUri)) {
				return false;
			}
			
			ContentValues associatedCCLangValue = new ContentValues();
			
			associatedCCLangValue.put(KEY_SUBTITLE_TRACKTYPE, subTrackType);

			if(null != subTrackName) {
				associatedCCLangValue.put(KEY_SUBTITLE_TRACK,  subTrackName);
			}
			else {
				associatedCCLangValue.putNull(KEY_SUBTITLE_TRACK);
			}

			String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);

			try {
				nbRows = currentDatabase.update(LV_MEDIA_META_INFO_TABLE, associatedCCLangValue,
						KEY_MEDIA_URI + "=?", new String[] {escapedUri});
			}
			catch(SQLException sqle) {
				Log.e(TAG, "setAssociatedSubtitleTrackForMedia - Error when set media information");
				sqle.printStackTrace();
				return false;
			}
			if(nbRows == 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Updates the database information regarding HLS Download for a given uri 
	 * @param uri URI for which HLS Download info must be updated
	 * @param hlsDlStatus	HLS Download status (0: NONE, 1: LOCAL, 2: DOWNLOADING, 3: PAUSED)
	 * @param duration complete stream duration (in ms)
	 * @param bitrate selected bitrate for HLS Download
	 * @param downloaded already download duration (in ms)
	 * @param localUri local URI after download
	 * @return true if the update went well, false otherwise
	 */
	public synchronized boolean setHLSDlInfoForUri(String uri, int hlsDlStatus, int duration, int bitrate, int downloaded, String localUri) {

		if(null == currentDatabase) {
			Log.w(TAG, "setDbInfoForMedia - database not opened !");
			return false;
		}
		synchronized(databaseMutex) {

			int nbRows = 0;
			if(! createMediaEntryIfNotExisting(uri)) {
				return false;
			}
			ContentValues dbValues = new ContentValues();

			dbValues.put(KEY_HLSDL_STATUS, hlsDlStatus);
			if(hlsDlStatus > 0) {
				dbValues.put(KEY_MEDIA_DURATION, duration);
				dbValues.put(KEY_HLSDL_BITRATE, bitrate);
				dbValues.put(KEY_HLSDL_PROGRESS, downloaded);
				if(null != localUri) {
					dbValues.put(KEY_HLSDL_LOCALURL, DatabaseUtils.sqlEscapeString(localUri));
				}
				else {
					dbValues.putNull(KEY_HLSDL_LOCALURL);
				}
			}
			String escapedUri = DatabaseUtils.sqlEscapeString(uri);
			try {
				nbRows = currentDatabase.update(LV_MEDIA_META_INFO_TABLE, dbValues,
						KEY_MEDIA_URI + "=?", new String[] {escapedUri});
			}
			catch(SQLException sqle) {
				Log.e(TAG, "setHLSDlInfoForUri - Error when set media information");
				sqle.printStackTrace();
				return false;
			}
			if(1 == nbRows) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean setDbInfoForMedia(String mediaUri, DatabaseInfo dbInfo) {

		if(null == currentDatabase) {
			Log.w(TAG, "setDbInfoForMedia - database not opened !");
			return false;
		}
		synchronized(databaseMutex) {

			int nbRows = 0;
			if(! createMediaEntryIfNotExisting(mediaUri)) {
				return false;
			}
			ContentValues dbValues = new ContentValues();

			dbValues.put(KEY_LAST_STOP_POS,  dbInfo.lastPlaybackPosInMs);
			dbValues.put(KEY_MEDIA_DURATION,  dbInfo.mediaDurationInMs);
			
			dbValues.put(KEY_SUBTITLE_FILE,  dbInfo.associatedSubtitleFile);
			dbValues.put(KEY_SUBTITLE_TRACK,  dbInfo.associatedTimedTextTrack);
			dbValues.put(KEY_SUBTITLE_TRACKTYPE, dbInfo.associatedTimedTextType);
			
			dbValues.put(KEY_HLSDL_STATUS, dbInfo.hlsDlStatus);
			if(dbInfo.hlsDlStatus > 0) {
				dbValues.put(KEY_HLSDL_BITRATE, dbInfo.hlsDLBitrate);
				dbValues.put(KEY_HLSDL_PROGRESS, dbInfo.hlsDLProgress);
				if(null != dbInfo.hlsDlLocalServerUrl) {
					dbValues.put(KEY_HLSDL_LOCALURL, DatabaseUtils.sqlEscapeString(dbInfo.hlsDlLocalServerUrl));
				}
				else {
					dbValues.putNull(KEY_HLSDL_LOCALURL);
				}
			}
			String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);
			try {
				nbRows = currentDatabase.update(LV_MEDIA_META_INFO_TABLE, dbValues,
						KEY_MEDIA_URI + "=?", new String[] {escapedUri});
			}
			catch(SQLException sqle) {
				Log.e(TAG, "setAssociateSubtitleFileForMedia - Error when set media information");
				sqle.printStackTrace();
				return false;
			}
			if(1 == nbRows) {
				return true;
			}
		}
		return false;
	}

	

	/**
	 * Stores the resume position for a given media.
	 * <br>
	 * <b>Note:</b>
	 * <br>The database entry is automatically created is the media was not previously known.
	 * @param mediaUri	Uri of the media
	 * @param position	last playback position for the media in ms
	 * @return			true if the storage went OK, false otherwise
	 */
	public synchronized boolean setResumePositionForMedia(String mediaUri, int position, int duration) {

		if(null == currentDatabase) {
			Log.w(TAG, "setResumePositionForMedia - database not opened !");
			return false;
		}
		synchronized(databaseMutex) {

			int nbRows = 0;
			if(false == createMediaEntryIfNotExisting(mediaUri)) {
				return false;
			}

			String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);

			ContentValues values = new ContentValues();
			values.put(KEY_LAST_STOP_POS, position);
			values.put(KEY_MEDIA_DURATION, duration);

			try {
				nbRows = currentDatabase.update(LV_MEDIA_META_INFO_TABLE, values,
						KEY_MEDIA_URI + "=?", new String[] {escapedUri});
			}
			catch(SQLException sqle) {
				Log.e(TAG, "setResumePositionForMedia - Error when set media information");
				sqle.printStackTrace();
				return false;
			}
			if(nbRows == 1) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Removes all stored informations for a given media (removes media entry from database).
	 * @param mediaUri	Uri of the media
	 * @return			true if the removal went OK, false otherwise
	 */
	public synchronized boolean removeDbEntryForUri(String mediaUri) {
		if(null == currentDatabase) {
			Log.w(TAG, "removeDbEntryForUri - database not opened !");
			return false;
		}
		int id = 0;
		synchronized(databaseMutex) {
			String escapedUri = DatabaseUtils.sqlEscapeString(mediaUri);

			Cursor queryInfos = currentDatabase.query(LV_MEDIA_META_INFO_TABLE,
					new String[]{KEY_MAIN_ID},
					KEY_MEDIA_URI + "=?", new String[]{escapedUri},
					null, null, null);

			if(queryInfos.getCount() > 1) {
				Log.e(TAG, "removeDbEntryForUri - database corruption");
				queryInfos.close();
				return false;
			}

			if(queryInfos.getCount() == 1) {
				queryInfos.moveToFirst();
				id = queryInfos.getInt(queryInfos.getColumnIndex(KEY_MAIN_ID));
			}
			queryInfos.close();
			
			currentDatabase.beginTransaction();
			
			int nbDelete = currentDatabase.delete(LV_MEDIA_META_INFO_TABLE, KEY_MAIN_ID + "=" + id, null);
			if(nbDelete > 0) {
				currentDatabase.setTransactionSuccessful();
				currentDatabase.endTransaction();
				return true;
			}
			else {
				currentDatabase.endTransaction();
				return false;
			}
		}
	}
}
