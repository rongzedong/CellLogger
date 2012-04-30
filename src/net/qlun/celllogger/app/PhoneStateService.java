package net.qlun.celllogger.app;

import net.qlun.celllogger.Station;
import net.qlun.celllogger.provider.Alarm;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class PhoneStateService extends Service {

	public static final String CELL_UPDATE_ACTION = PhoneStateService.class
			.getName() + "-cell_update";
	protected static final String LOCATION_UPDATE_ACTION = PhoneStateService.class
			.getName() + "-loc_update";

	/**
	 * send stop alert to service
	 */
	public static final String STOP_ALARM_ACTION = PhoneStateService.class
			.getName() + "-stop_alarm";

	/**
	 * send dismiss to alert activity
	 */
	public static final String DISMISS_ALERT_ACTION = PhoneStateService.class
			.getName() + "-dismiss_alert";

	private static final String TAG = "cl-svr";

	private Context context;
	private TelephonyManager telephonyManager;
	private LocationManager locationManager;

	public class CurrentCellInfo implements Cloneable {
		public int cid;
		public int lac;
		public int signalStrength;
		public int networkType;

		@Override
		public int hashCode() {
			return cid + lac;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || obj.getClass() != this.getClass()) {
				return false;
			}
			CurrentCellInfo check = (CurrentCellInfo) obj;

			return (check.cid == cid && check.lac == lac);
		}

		@Override
		public Object clone() {
			try {
				CurrentCellInfo cloned = (CurrentCellInfo) super.clone();

				return cloned;
			} catch (CloneNotSupportedException e) {
				System.out.println(e);
				return null;
			}
		}
	}

	private CurrentCellInfo currentCell = new CurrentCellInfo();

	public class CurrentLocationInfo implements Cloneable {
		public float accuracy;
		public double latitude;
		public double longitude;
		public float speed;
		public long time;
	}

	private CurrentLocationInfo currentLocation = new CurrentLocationInfo();

	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		public PhoneStateService getService() {
			return PhoneStateService.this;
		}
	}

	private Handler mHandler = new Handler();

	/**
	 * send data frequently
	 */
	private Runnable mTickTask = new Runnable() {
		public void run() {

			// sendCells();

			mHandler.postDelayed(this, 5000);
		}
	};

	private PhoneStateListener mPhoneListener = new PhoneStateListener() {

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			Log.v(TAG, "signal strength: " + signalStrength.toString());

			if (signalStrength.isGsm()) {
				// GSM
				int asu = signalStrength.getGsmSignalStrength();
				currentCell.signalStrength = -113 + 2 * asu;
			} else {
				// fixme
				currentCell.signalStrength = signalStrength.getCdmaDbm();
			}

		}

		@Override
		public void onCellLocationChanged(CellLocation location) {
			Log.v(TAG, "cell location: " + location.toString());

			int networkType = telephonyManager.getNetworkType();
			Log.i(TAG, "network type: " + networkType);

			if (location instanceof GsmCellLocation) {
				GsmCellLocation cl = (GsmCellLocation) location;
				Log.i(TAG, "gsm cell id: " + cl.getCid());

				currentCell.cid = cl.getCid();
				currentCell.lac = cl.getLac();
			} else if (location instanceof CdmaCellLocation) {
				CdmaCellLocation cl = (CdmaCellLocation) location;
				Log.i(TAG, "cdma cell id: " + cl.getBaseStationId());

				currentCell.cid = cl.getBaseStationId();
				currentCell.lac = cl.getNetworkId();
			} else {
				Log.w(TAG, "invalid cell location.");

			}

			currentCell.networkType = networkType;

			cellLocationChanged();

		}

	};

	public CurrentCellInfo getCurrentCellInfo() {

		return currentCell;
	}

	public CurrentLocationInfo getCurrentLocationInfo() {
		return currentLocation;
	}

	private LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {

			currentLocation.accuracy = location.getAccuracy();
			currentLocation.latitude = location.getLatitude();
			currentLocation.longitude = location.getLongitude();
			currentLocation.speed = location.getSpeed();
			currentLocation.time = location.getTime();

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (STOP_ALARM_ACTION.equals(intent.getAction())) {
				CharSequence name = intent.getCharSequenceExtra("name");

				Log.v(TAG, "stop alarm " + name);

				// stop alarm and dismiss alert
				stopHorn();

				
				Intent i = new Intent();
				i.setAction(PhoneStateService.DISMISS_ALERT_ACTION);
				i.putExtra("name", name);
				sendBroadcast(i);
				
				
			} else {
				Log.e(TAG, "unexpected action: " + intent.getAction());
			}

		}

	};

	@Override
	public void onCreate() {

		context = this.getApplicationContext();
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// telephonyManager.listen(mPhoneListener,
		// PhoneStateListener.LISTEN_CALL_STATE
		// | PhoneStateListener.LISTEN_CELL_LOCATION
		// | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		telephonyManager.listen(mPhoneListener,
				PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 5000, 10, mLocationListener);

		// mHandler.postDelayed(mTickTask, 5000);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "start service.");

		// new LogCatTask().execute();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(STOP_ALARM_ACTION);
		registerReceiver(mReceiver, intentFilter);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {

		Log.v(TAG, "bind service.");
		/*
		 * telephonyManager = (TelephonyManager)
		 * getSystemService(Context.TELEPHONY_SERVICE);
		 * 
		 * telephonyManager.listen(mPhoneListener,
		 * PhoneStateListener.LISTEN_CELL_LOCATION |
		 * PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		 */
		return mBinder;
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "destroy service.");

		telephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
		locationManager.removeUpdates(mLocationListener);

		super.onDestroy();
	}

	private void cellLocationChanged() {
		Log.v(TAG, "cell changed, " + currentCell.lac + ", " + currentCell.cid);

		// try get station id from Station
		int station = 1310;

		Log.v(TAG, "got station , " + station);

		// check if has alarm

		boolean en = Alarm.checkStationEnabled(this, station);
		if (!en) {
			Log.v(TAG, "alarm not enabled.");
			return;
		}

		Log.i(TAG, "alarm on");

		String name = Station.getInstance(this).getName(station);

		// play sound here?
		startHorn();

		// ,and open intent
		Intent intent = new Intent(this, AlarmAlertActivity.class);
		intent.putExtra("name", name);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		startActivity(intent);

	}

	public void startHorn() {
		Log.v(TAG, "start horn");
	}

	public void stopHorn() {
		Log.v(TAG, "stop horn");
	}
}
