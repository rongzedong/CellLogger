package net.qlun.celllogger.app;

import net.qlun.celllogger.R;
import net.qlun.celllogger.util.AlarmWakeLock;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmAlertActivity extends Activity implements OnClickListener {

	protected static final String TAG = "CL-Alert";

	private CharSequence name;

	PhoneStateService mService = null;

	boolean mBound;

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.v(TAG, "service connected.");
			PhoneStateService.LocalBinder binder = (PhoneStateService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v(TAG, "service disconnected.");
			mBound = false;
		}

	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (PhoneStateService.DISMISS_ALERT_ACTION.equals(intent
					.getAction())) {
				CharSequence name = intent.getCharSequenceExtra("name");

				Log.v(TAG, "dismiss alert " + name);

				AlarmAlertActivity.this.finish();

			} else {
				Log.e(TAG, "unexpected action: " + intent.getAction());
			}

		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AlarmWakeLock.acquireCpuWakeLock(getApplicationContext());

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		setContentView(R.layout.alarm_alert);

		name = getIntent().getCharSequenceExtra("name");

		TextView tv_name = (TextView) findViewById(R.id.station_name);
		tv_name.setText(name);

		Button dismiss = (Button) findViewById(R.id.button_dismiss);
		dismiss.setOnClickListener(this);

	}

	@Override
	protected void onStart() {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PhoneStateService.DISMISS_ALERT_ACTION);
		registerReceiver(mReceiver, intentFilter);

		super.onStart();

		Intent intent = new Intent(this, PhoneStateService.class);
		getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onStop() {
		super.onStop();

		stopAlert();

		if (mBound) {
			try {
				getApplicationContext().unbindService(mConnection);
			} catch (IllegalArgumentException iae) {

			}
			mBound = false;
		}

		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		AlarmWakeLock.releaseCpuLock();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_dismiss: {
			Log.v(TAG, "dismiss");
			stopAlert();
		}
			break;
		}

	}

	private void stopAlert() {
		if (mBound) {
			Log.v(TAG, "bound stop");
			mService.stopHorn();
			finish(); // close me
		} else {
			Intent intent = new Intent();
			intent.setAction(PhoneStateService.STOP_ALARM_ACTION);
			intent.putExtra("name", name);
			sendBroadcast(intent);
		}
	}
}
