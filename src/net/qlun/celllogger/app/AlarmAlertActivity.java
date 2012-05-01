package net.qlun.celllogger.app;

import net.qlun.celllogger.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
	}

	@Override
	protected void onStop() {

		super.onStop();

		unregisterReceiver(mReceiver);

		stopAlert();
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
