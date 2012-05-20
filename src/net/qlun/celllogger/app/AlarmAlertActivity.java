package net.qlun.celllogger.app;

import net.qlun.celllogger.R;
import net.qlun.celllogger.util.AlarmWakeLock;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
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

	private KeyguardManager keyguardManager;
	private KeyguardLock keyLock;
	
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

		Log.v(TAG, "create");



		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


		keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
		keyLock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
		
		
		setContentView(R.layout.alarm_alert);

		name = getIntent().getCharSequenceExtra("name");

		TextView tv_name = (TextView) findViewById(R.id.station_name);
		tv_name.setText(name);

		Button dismiss = (Button) findViewById(R.id.button_dismiss);
		dismiss.setOnClickListener(this);

	}

	@Override
	protected void onStart() {
		super.onStart();

		Log.v(TAG, "start");
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(PhoneStateService.DISMISS_ALERT_ACTION);
		registerReceiver(mReceiver, intentFilter);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "resume");
		
		AlarmWakeLock.acquireCpuWakeLock(this);
		
		keyLock.disableKeyguard();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.v(TAG, "pause");
		
		keyLock.reenableKeyguard();
		
		AlarmWakeLock.releaseCpuLock();
	}
	
	@Override
	protected void onStop() {
		super.onStop();

		Log.v(TAG, "stop");
		
		stopAlert();

		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "destroy");
		

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

		Intent intent = new Intent();
		intent.setAction(PhoneStateService.STOP_ALARM_ACTION);
		intent.putExtra("name", name);
		sendBroadcast(intent);

	}
}
