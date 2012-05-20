package net.qlun.celllogger.util;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and released in the
 * AlarmAlert activity
 */
public class AlarmWakeLock {

	private static final String TAG = "CL-WakeLock";
	private static PowerManager.WakeLock sCpuWakeLock;

	public static void acquireCpuWakeLock(Context context) {
		Log.v(TAG, "Acquiring cpu wake lock");
		if (sCpuWakeLock != null) {
			return;
		}

		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);

		sCpuWakeLock = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, TAG);
		sCpuWakeLock.acquire();
	}

	public static void releaseCpuLock() {
		Log.v(TAG, "Releasing cpu wake lock");
		if (sCpuWakeLock != null) {
			sCpuWakeLock.release();
			sCpuWakeLock = null;
		}
	}
}