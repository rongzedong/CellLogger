package net.qlun.celllogger.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class LogSettingProvider extends ContentProvider {

	private static final UriMatcher uriMatcher;
	private static final String TAG = "SettingProvider";
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(LogSetting.AUTHORITY, "setting",
				LogSetting.MATCH_SETTING);
		uriMatcher.addURI(LogSetting.AUTHORITY, "setting/*",
				LogSetting.MATCH_SETTING_ITEM);
	}

	private LogSettingHelper helper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {

		case LogSetting.MATCH_SETTING:
			return "vnd.android.cursor.dir/vnd.net.qlun.celllogger.setting";

		case LogSetting.MATCH_SETTING_ITEM:
			return "vnd.android.cursor.dir/vnd.net.qlun.celllogger.setting-item";

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);

		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		Log.v(TAG, LogSettingProvider.class.getName() + " insert: " + uri
				+ ", " + values);

		long rowID = helper.getWritableDatabase().insert(LogSetting.TABLE_NAME,
				"", values);

		if (rowID > 0) {
			Uri _uri = Uri.withAppendedPath(LogSetting.CONTENT_URI,
					values.getAsString(LogSetting.KEY));
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		Log.v(TAG, LogSetting.class.getName() + " onCreate.");

		helper = new LogSettingHelper(getContext(), LogSetting.TABLE_NAME,
				null, LogSetting.DATABASE_VERSION);

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(LogSetting.TABLE_NAME);

		switch (uriMatcher.match(uri)) {

		case LogSetting.MATCH_SETTING_ITEM:

			selection = LogSetting.KEY + " = ? ";
			selectionArgs = new String[] { uri.getPathSegments().get(1) };
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		Cursor c = sqlBuilder.query(helper.getReadableDatabase(), projection,
				selection, selectionArgs, null, null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int count = 0;

		switch (uriMatcher.match(uri)) {

		case LogSetting.MATCH_SETTING_ITEM:
			count = helper.getWritableDatabase().update(LogSetting.TABLE_NAME,
					values, LogSetting.KEY + " = ? ",
					new String[] { uri.getPathSegments().get(1) });
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

}