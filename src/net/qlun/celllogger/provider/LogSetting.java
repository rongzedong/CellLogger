package net.qlun.celllogger.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;

public class LogSetting {

	public static final String AUTHORITY = LogSetting.class.getName();

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/setting");

	public static final String _ID = "_id";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	public static final String TIME_CREATE = "time_create";
	public static final String TIME_UPDATE = "time_update";

	public static final int DATABASE_VERSION = 2;

	public static final String TABLE_NAME = "log_setting";
	public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME
			+ " (" + " _id integer primary key autoincrement, "
			+ " key text unique, " + " value text, " + " time_create integer, "
			+ " time_update integer" + ");";

	public static final int MATCH_SETTING = 1;
	public static final int MATCH_SETTING_ITEM = 2;

	public static String get(Context ctx, String key) {

		String value = null;

		Cursor c = ctx.getContentResolver().query(
				Uri.withAppendedPath(CONTENT_URI, key), null, null, null, null);

		if (c.moveToNext()) {
			value = c.getString(c.getColumnIndex(VALUE));
		}

		c.close();

		return value;
	}

	public static void set(Context ctx, String key, String value) {

		String previousValue = get(ctx, key);

		boolean update = false;
		if (previousValue == null) {
			try {
				ContentValues values = new ContentValues();
				values.put(KEY, key);
				values.put(VALUE, value);
				values.put(TIME_CREATE, System.currentTimeMillis());

				ctx.getContentResolver().insert(CONTENT_URI, values);

			} catch (SQLiteConstraintException ce) {
				update = true;
			}
		} else {
			update = true;
		}

		if (update) {
			// update
			ContentValues values = new ContentValues();
			values.put(VALUE, value);
			values.put(TIME_UPDATE, System.currentTimeMillis());

			ctx.getContentResolver().update(
					Uri.withAppendedPath(CONTENT_URI, key), values, null, null);
		}
	}
}