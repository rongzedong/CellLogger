package net.qlun.celllogger.app;

import java.util.List;

import net.qlun.celllogger.R;
import net.qlun.celllogger.Station;
import net.qlun.celllogger.StationCell;
import net.qlun.celllogger.provider.Alarm;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.RingtoneManager;
import android.net.Uri;
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

	/** Play alarm up to 10 minutes before silencing */
	private static final int ALARM_TIMEOUT_SECONDS = 15;
	private boolean mPlaying = false;
	private MediaPlayer mMediaPlayer;
	private long mStartTime;
	// Volume suggested by media team for in-call alarms.
	private static final float IN_CALL_VOLUME = 0.125f;

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

		// try get station id from Station by lac,cid
		int station = -1;

		List<Integer> stations = StationCell.getInstance(context).getIdList(
				currentCell.lac, currentCell.cid);
		for (Integer station_id : stations) {
			int _station = station_id.intValue();
			Log.v(TAG, "check station , " + _station);

			// check if has alarm
			boolean en = Alarm.checkStationEnabled(this, _station);
			if (en) {
				station = _station;
				break;
			} else {
				Log.v(TAG, "skip station, " + _station);
			}

		}

		if (station == -1) {
			Log.v(TAG, "no alarm set on");
			return;
		}

		Log.v(TAG, "got station , " + station);

		Log.i(TAG, "alarm on");

		String name = Station.getInstance(this).getName(station);

		// play sound here
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

		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.e(TAG, "Error occurred while playing audio.");
				mp.stop();
				mp.release();
				mMediaPlayer = null;
				return true;
			}
		});

		try {
			// Check if we are in a call. If we are, use the in-call alarm
			// resource at a low volume to not disrupt the call.
			if (telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
				Log.v(TAG, "Using the in-call alarm");
				mMediaPlayer.setVolume(IN_CALL_VOLUME, IN_CALL_VOLUME);
				setDataSourceFromResource(getResources(), mMediaPlayer,
						R.raw.alarm);
			} else {
				mMediaPlayer.setDataSource(this, alert);
			}
			startAlarm(mMediaPlayer);

			mPlaying = true;

			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					Log.v(TAG, "stop horn timeout");
					stopHorn();
				}

			}, 1000 * ALARM_TIMEOUT_SECONDS);
		} catch (Exception ex) {
			Log.v(TAG, "cannot play ringtone");

		}
	}

	public void stopHorn() {
		Log.v(TAG, "stop horn");

		if (mPlaying) {
			mPlaying = false;

			// Stop audio playing
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
				mMediaPlayer = null;
			}

		}
	}

	// Do the common stuff when starting the alarm.
	private void startAlarm(MediaPlayer player) throws java.io.IOException,
			IllegalArgumentException, IllegalStateException {
		final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// do not play alarms if stream volume is 0
		// (typically because ringer mode is silent).
		if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
			player.setAudioStreamType(AudioManager.STREAM_ALARM);
			player.setLooping(true);
			player.prepare();
			player.start();
		}
	}

	private void setDataSourceFromResource(Resources resources,
			MediaPlayer player, int res) throws java.io.IOException {
		AssetFileDescriptor afd = resources.openRawResourceFd(res);
		if (afd != null) {
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
					afd.getLength());
			afd.close();
		}
	}
}
