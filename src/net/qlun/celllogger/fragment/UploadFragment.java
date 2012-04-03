package net.qlun.celllogger.fragment;

import java.util.ArrayList;
import java.util.List;

import net.qlun.celllogger.R;
import net.qlun.celllogger.provider.CellLocationLog;
import net.qlun.celllogger.provider.CellLocationLogItem;
import net.qlun.celllogger.provider.LogSetting;
import net.qlun.celllogger.remote.RemoteTask;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class UploadFragment extends Fragment implements OnClickListener {

	private static final String TAG = "CL-Upload";

	protected static final String KEY_OFFSET = "upload_offset";

	private class UploadTask extends RemoteTask {

		public UploadTask(Context c) {
			super(c);
		}

		protected void onPostExecute(String result) {

			toggleButtons(true);

			updateStatus(SyncStatus.SUCCESS);
		}

	};

	private class SyncStatus {
		public static final int IDLE = 0;
		public static final int UPLOADING = 1;
		public static final int SUCCESS = 2;
		public static final int FAILED = 3;
		public static final int PREPARE_DATA = 4;
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

		View view = inflater.inflate(R.layout.upload, container, false);

		{
			Button btn = (Button) view.findViewById(R.id.button_upload);
			btn.setOnClickListener(this);
		}
		{
			Button btn = (Button) view.findViewById(R.id.button_upload_all);
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
		case R.id.button_upload:
			Log.v(TAG, "upload button click.");
			{

				upload(false);
			}
			break;
		case R.id.button_upload_all:
			Log.v(TAG, "upload_all button click.");
			{
				upload(true);
			}
			break;
		}

	}

	private void toggleButtons(boolean enabled) {
		{
			Button btn = (Button) getActivity()
					.findViewById(R.id.button_upload);
			btn.setEnabled(enabled);
		}
		{
			Button btn = (Button) getActivity().findViewById(
					R.id.button_upload_all);
			btn.setEnabled(enabled);
		}
	}

	private void upload(boolean all) {
		toggleButtons(false);

		updateStatus(SyncStatus.PREPARE_DATA);
		
		new AsyncTask<Boolean, Void, List<CellLocationLogItem>>() {

			@Override
			protected List<CellLocationLogItem> doInBackground(
					Boolean... params) {

				boolean all = params[0];

				List<CellLocationLogItem> items = new ArrayList<CellLocationLogItem>();

				int offset = 0;
				if (!all) {
					String s_offset = LogSetting.get(
							UploadFragment.this.getActivity(), KEY_OFFSET);
					if (s_offset != null) {
						offset = Integer.parseInt(s_offset, 10);
					}
				}

				Log.v(TAG, "offset: " + offset);

				String sortOrder = CellLocationLog._ID + " ASC";

				Cursor c = UploadFragment.this
						.getActivity()
						.getContentResolver()
						.query(CellLocationLog.CONTENT_URI, null,
								CellLocationLog._ID + " > ? ",
								new String[] { String.valueOf(offset) },
								sortOrder);

				while (c.moveToNext()) {
					CellLocationLogItem item = new CellLocationLogItem();
					item.id = c.getInt(c.getColumnIndex(CellLocationLog._ID));
					item.network_type = c.getInt(c
							.getColumnIndex(CellLocationLog.NETWORK_TYPE));
					item.cid = c.getInt(c.getColumnIndex(CellLocationLog.CID));
					item.lac = c.getInt(c.getColumnIndex(CellLocationLog.LAC));
					item.station_id = c.getString(c
							.getColumnIndex(CellLocationLog.STATION_ID));
					item.signal_strength = c.getInt(c
							.getColumnIndex(CellLocationLog.SIGNAL_STRENGTH));
					item.time = c.getLong(c
							.getColumnIndex(CellLocationLog.TIME));

					items.add(item);

				}

				c.close();

				return items;
			}

			@Override
			protected void onProgressUpdate(Void... progress) {

			}

			@Override
			protected void onPostExecute(List<CellLocationLogItem> items) {
				Log.v(TAG, "" + items.size());

				updateStatus(SyncStatus.UPLOADING);
				
				String json = "";

				String remote_endpoint = UploadFragment.this
						.getString(R.string.remote_endpoint);

				new UploadTask(UploadFragment.this.getActivity()).execute(
						remote_endpoint, json);

			}

		}.execute(all);

		
	}

	private void updateStatus(int status) {
		String[] msgs = new String[] { "Idle", "Uploading...", "Success",
				"Failed", "Preparing..."

		};

		String msg;
		try {
			msg = msgs[status];
		} catch (ArrayIndexOutOfBoundsException e) {
			msg = "unknown";
		}

		TextView tv = (TextView) getActivity().findViewById(
				R.id.text_sync_status);
		tv.setText("Sync Status: " + msg);
	}

}
