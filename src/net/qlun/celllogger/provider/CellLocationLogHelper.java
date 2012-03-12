package net.qlun.celllogger.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class CellLocationLogHelper extends SQLiteOpenHelper {

	private static final String TAG = "LogHelper";

	public CellLocationLogHelper(Context context, String name,
			CursorFactory factory, int version) {

		super(context, name, factory, version);

		Log.v(TAG, CellLocationLogHelper.class.getName() + " construct. ver="
				+ CellLocationLog.DATABASE_VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(TAG, CellLocationLogHelper.class.getName() + " onCreate");

		db.execSQL(CellLocationLog.TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		Log.v(TAG, CellLocationLogHelper.class.getName() + " onUpgrade.");

		db.execSQL("DROP TABLE IF EXISTS " + CellLocationLog.TABLE_NAME);

		db.execSQL(CellLocationLog.TABLE_CREATE);
	}

}