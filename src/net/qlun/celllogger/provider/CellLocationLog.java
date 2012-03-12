package net.qlun.celllogger.provider;

import android.net.Uri;

public class CellLocationLog {

	public static final String AUTHORITY = CellLocationLog.class.getName();

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/log");

	public static final String _ID = "_id";
	public static final String NETWORK_TYPE = "network_type";
	public static final String CID = "cid";
	public static final String LAC = "lac";
	public static final String STATION_ID = "station_id";
	public static final String SIGNAL_STRENGTH = "signal_strength";
	public static final String TIME = "time";

	public static final int DATABASE_VERSION = 8;

	public static final String TABLE_NAME = "cell_location_log";
	public static final String TABLE_CREATE = "CREATE TABLE " 
			+ TABLE_NAME
			+ " (" + " _id integer primary key autoincrement, "
			+ " network_type integer, " 
			+ " cid integer, "
			+ " lac integer, "
			+ " station_id text, "
			+ " signal_strength integer, "
			+ " time integer " 
			+ ");";

	public static final int MATCH_LOG = 1;
	public static final int MATCH_LOG_ID = 2;

}