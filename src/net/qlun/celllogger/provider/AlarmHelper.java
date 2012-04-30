package net.qlun.celllogger.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class AlarmHelper extends SQLiteOpenHelper {

	private static final String TAG = "AlarmHelper";

	public AlarmHelper(Context context, String name,
			CursorFactory factory, int version) {

		super(context, name, factory, version);

		Log.v(TAG, this.getClass().getName() + " construct. ver="
				+ Alarm.DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(TAG, this.getClass().getName() + " onCreate");

		db.execSQL(Alarm.TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.v(TAG, this.getClass().getName() + " onUpgrade.");

		// db.execSQL("DROP TABLE IF EXISTS " + CellLocationLog.TABLE_NAME);
		// db.execSQL(CellLocationLog.TABLE_CREATE);
	}

}