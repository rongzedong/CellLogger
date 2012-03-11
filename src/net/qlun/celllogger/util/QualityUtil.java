package net.qlun.celllogger.util;

import java.math.BigDecimal;

import android.util.Log;

public class QualityUtil {

	private static final String TAG = "util-quality";

	public static double getDbmQuality(double dbm) {

		double quality = 0;
		if (dbm <= -100) {
			quality = 0;
		} else if (dbm >= -50) {
			quality = 100;
		} else {
			quality = Math.floor(((float) (dbm + 100) / 50.0f) * 100);
		}

		// Log.v(TAG, "dbm " + dbm + ", q " + quality);
		return quality;
	}
	

}
