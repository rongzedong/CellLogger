package net.qlun.celllogger.fragment;

import java.text.SimpleDateFormat;

import net.qlun.celllogger.R;
import net.qlun.celllogger.Station;
import net.qlun.celllogger.provider.Alarm;
import net.qlun.celllogger.util.ToastMaster;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmFragment extends ListFragment implements OnClickListener {

	private static final String TAG = "CL-Alarm";

	class AlarmCursorAdapter extends CursorAdapter {
		private Cursor mCursor;
		private Context mContext;
		private final LayoutInflater mInflater;

		private final SimpleDateFormat sdfDate = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		public AlarmCursorAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = LayoutInflater.from(context);
			mContext = context;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			final int id = cursor.getInt(cursor.getColumnIndex(Alarm._ID));

			TextView station_name = (TextView) view
					.findViewById(R.id.station_name);

			int station = cursor
					.getInt(cursor.getColumnIndex(Alarm.STATION_ID));
			final String name = Station.getInstance(getActivity()).getName(
					station);
			station_name.setText(name);

			CheckBox enbox = (CheckBox) view.findViewById(R.id.check_box);
			int enabled = cursor.getInt(cursor.getColumnIndex(Alarm.ENABLED));
			if (enabled == Alarm.Enabled.YES) {
				enbox.setChecked(true);
			} else {
				enbox.setChecked(false);
			}
			enbox.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					boolean isChecked = ((CheckBox) v).isChecked();

					enableAlarm(id, isChecked);

					Toast toast = Toast.makeText(getActivity(),
							(isChecked ? "Enable" : "Disable") + " " + name,
							Toast.LENGTH_SHORT);
					ToastMaster.setToast(toast);
					toast.show();
				}
			});

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.alarm_item, parent,
					false);
			return view;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "create");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.v(TAG, "createView");

		Cursor cursor = getActivity().getContentResolver().query(
				Alarm.CONTENT_URI,
				new String[] { Alarm._ID, Alarm.STATION_ID, Alarm.ENABLED },
				null, null, null);

		getActivity().startManagingCursor(cursor);

		AlarmCursorAdapter mAdapter = new AlarmCursorAdapter(getActivity(),
				cursor);

		this.setListAdapter(mAdapter);

		View view = inflater.inflate(R.layout.alarm, container, false);

		{
			Button btn = (Button) view.findViewById(R.id.add_alarm);
			btn.setOnClickListener(this);
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v(TAG, "activity created");

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.v(TAG, "start");

	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(TAG, "stop");

	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.add_alarm:
			Log.v(TAG, "add alarm button click.");
			{

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle("Pick a station");
				final CharSequence[] items = Station.getInstance(getActivity())
						.getAllItems();
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						String station_xid = items[item].toString();

						int station = Station.getInstance(getActivity()).getId(
								station_xid);

						addAlarm(station);

					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
			break;
		}

	}

	private void enableAlarm(int id, boolean enabled) {

		Log.v(TAG, "Enable " + id + " " + enabled);

		ContentValues values = new ContentValues();
		values.put(Alarm.ENABLED, enabled ? Alarm.Enabled.YES
				: Alarm.Enabled.NO);

		getActivity().getContentResolver().update(
				ContentUris.withAppendedId(Alarm.CONTENT_URI, id), values,
				null, null);

	}

	private void addAlarm(int station) {

		Log.v(TAG, "add " + station);

		ContentValues values = new ContentValues();
		values.put(Alarm.STATION_ID, station);
		values.put(Alarm.ENABLED, Alarm.Enabled.YES);
		getActivity().getContentResolver().insert(Alarm.CONTENT_URI, values);

	}
}
