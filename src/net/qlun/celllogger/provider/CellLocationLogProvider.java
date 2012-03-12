package net.qlun.celllogger.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CellLocationLogProvider extends ContentProvider {

	private static final UriMatcher uriMatcher;
	private static final String TAG = "LogProvider";
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(CellLocationLog.AUTHORITY, "log",
				CellLocationLog.MATCH_LOG);
		uriMatcher.addURI(CellLocationLog.AUTHORITY, "log/#",
				CellLocationLog.MATCH_LOG_ID);
	}

	private CellLocationLogHelper helper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {

		SQLiteDatabase db = helper.getWritableDatabase();
		int count;
		switch (uriMatcher.match(uri)) {

		case CellLocationLog.MATCH_LOG_ID:
			count = db.delete(CellLocationLog.TABLE_NAME, where, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {

		case CellLocationLog.MATCH_LOG:
			return "vnd.android.cursor.dir/vnd.qtester.cell.log ";

		case CellLocationLog.MATCH_LOG_ID:
			return "vnd.android.cursor.item/vnd.qtester.cell.log ";

		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);

		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		Log.v(TAG, CellLocationLogProvider.class.getName() + " insert: " + uri
				+ " / " + values);

		long rowID = helper.getWritableDatabase().insert(
				CellLocationLog.TABLE_NAME, "", values);

		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(CellLocationLog.CONTENT_URI,
					rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		Log.v(TAG, CellLocationLogProvider.class.getName() + " onCreate.");

		helper = new CellLocationLogHelper(getContext(),
				CellLocationLog.TABLE_NAME, null,
				CellLocationLog.DATABASE_VERSION);

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
		sqlBuilder.setTables(CellLocationLog.TABLE_NAME);

		if (uriMatcher.match(uri) == CellLocationLog.MATCH_LOG_ID)
			sqlBuilder.appendWhere(CellLocationLog._ID + " = "
					+ uri.getPathSegments().get(1));

		if (sortOrder == null || sortOrder == "")
			sortOrder = CellLocationLog.TIME + " DESC";

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

		case CellLocationLog.MATCH_LOG:
			count = helper.getWritableDatabase().update(
					CellLocationLog.TABLE_NAME, values, selection,
					selectionArgs);
			break;

		case CellLocationLog.MATCH_LOG_ID:
			count = helper.getWritableDatabase().update(
					CellLocationLog.TABLE_NAME,
					values,
					CellLocationLog._ID
							+ " = "
							+ uri.getPathSegments().get(1)
							+ (!TextUtils.isEmpty(selection) ? " AND ("
									+ selection + ')' : ""), selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);

		}

		getContext().getContentResolver().notifyChange(uri, null);

		return count;
	}

}