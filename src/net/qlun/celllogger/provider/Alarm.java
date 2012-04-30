package net.qlun.celllogger.provider;

import android.net.Uri;

public class Alarm {

	public static final String AUTHORITY = Alarm.class.getName();
	public static final String XALARM = "station_alarm";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + XALARM);

	public static final String _ID = "_id";
	public static final String STATION_ID = "station_id";
	public static final String ENABLED = "enabled";

	public static class Enabled {
		public static final int YES = 1;
		public static final int NO = 0;
	}

	public static final int DATABASE_VERSION = 2;

	public static final String TABLE_NAME = "alarm";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ " (" + " _id integer primary key autoincrement, "
			+ " station_id integer, " + " enabled integer " + ");";

	public static final int MATCH_ALARM = 1;
	public static final int MATCH_ALARM_ID = 2;

}