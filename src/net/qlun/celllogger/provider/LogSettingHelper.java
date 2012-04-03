package net.qlun.celllogger.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class LogSettingHelper extends SQLiteOpenHelper {

	private static final String TAG = "SettingHelper";

	public LogSettingHelper(Context context, String name,
			CursorFactory factory, int version) {

		super(context, name, factory, version);

		Log.v(TAG, LogSettingHelper.class.getName() + " construct. ver="
				+ LogSetting.DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(TAG, LogSettingHelper.class.getName() + " onCreate");

		db.execSQL(LogSetting.TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v(TAG, CellLocationLogHelper.class.getName() + " onUpgrade.");

		if (oldVersion == 1 && newVersion > oldVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + LogSetting.TABLE_NAME);
			db.execSQL(LogSetting.TABLE_CREATE);
		}
	}

}