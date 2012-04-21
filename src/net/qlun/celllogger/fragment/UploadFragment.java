package net.qlun.celllogger.fragment;

import java.util.ArrayList;
import java.util.List;

import net.qlun.celllogger.R;
import net.qlun.celllogger.Station;
import net.qlun.celllogger.provider.CellLocationLog;
import net.qlun.celllogger.provider.CellLocationLogItem;
import net.qlun.celllogger.provider.LogSetting;
import net.qlun.celllogger.remote.RemoteTask;
import net.qlun.celllogger.util.Installation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

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

	protected static final String KEY_OFFSET = "upload_offset2";

	private class UploadTask extends RemoteTask {

		private final int newOffset;

		public UploadTask(Context c, int newOffset) {
			super(c);
			this.newOffset = newOffset;
		}

		protected void onPostExecute(String result) {

			Log.v(TAG, "Upload Response: " + result);

			int status = -1;
			String reason = "";
			try {
				JSONObject r = (JSONObject) new JSONTokener(result).nextValue();

				status = r.getInt("error");
				JSONObject payload = r.getJSONObject("payload");
				if (payload.has("description")) {
					reason = payload.getString("description");
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Log.i(TAG, status + ", " + reason);

			if (status == 0) {
				updateStatus(SyncStatus.SUCCESS);

				if (newOffset > 0) {
					LogSetting.set(UploadFragment.this.getActivity(),
							KEY_OFFSET, "" + newOffset);
				}
			} else {
				updateStatus(SyncStatus.FAILED);
			}

			toggleButtons(true);
		}

	};

	private class SyncStatus {
		public static final int IDLE = 0;
		public static final int UPLOADING = 1;
		public static final int SUCCESS = 2;
		public static final int FAILED = 3;
		public static final int PREPARE_DATA = 4;
		protected static final int NO_NEW = 5;
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

			private int newOffset = -1;

			@Override
			protected List<CellLocationLogItem> doInBackground(
					Boolean... params) {

				boolean all = params[0];

				List<CellLocationLogItem> items = new ArrayList<CellLocationLogItem>();

				int offset = 0;
				if (!all) {
					String s_offset = LogSetting.get(
							UploadFragment.this.getActivity(), KEY_OFFSET);
					Log.v(TAG, "s_offset: " + s_offset);
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

					// string station to integer id
					String xStation = c.getString(c
							.getColumnIndex(CellLocationLog.STATION_ID));
					item.station_id = Station.getInstance(getActivity()).getId(
							xStation);

					item.signal_strength = c.getInt(c
							.getColumnIndex(CellLocationLog.SIGNAL_STRENGTH));

					// micro-seconds to seconds
					item.time = c.getLong(c
							.getColumnIndex(CellLocationLog.TIME)) / 1000;

					items.add(item);

					if (item.id > newOffset) {
						newOffset = item.id;
					}

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

				if (items.size() > 0) {
					updateStatus(SyncStatus.UPLOADING);

					String json = getRequestJson(items);

					String remote_endpoint = UploadFragment.this
							.getString(R.string.remote_endpoint);

					new UploadTask(UploadFragment.this.getActivity(), newOffset)
							.execute(remote_endpoint, json);
				} else {
					updateStatus(SyncStatus.NO_NEW);
					toggleButtons(true);                      
				}
			}

		}.execute(all);

	}

	private void updateStatus(int status) {
		String[] msgs = new String[] { "Idle", "Uploading...", "Success",
				"Failed", "Preparing...", "Up to date"

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

	private String getRequestJson(List<CellLocationLogItem> items) {

		JSONObject root = new JSONObject();
		try {
			root.put("client", Installation.id(getActivity()));

			JSONArray records = new JSONArray();
			for (CellLocationLogItem item : items) {
				records.put(item.toJSONObject());
			}
			root.put("records", records);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return root.toString();
	}

}
