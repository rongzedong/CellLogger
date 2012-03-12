package net.qlun.celllogger.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.qlun.celllogger.R;
import net.qlun.celllogger.provider.CellLocationLog;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryFragment extends ListFragment {

	private static final String TAG = "h-f";

	class LogsCursorAdapter extends CursorAdapter {
		private Cursor mCursor;
		private Context mContext;
		private final LayoutInflater mInflater;

		private final SimpleDateFormat sdfDate = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		public LogsCursorAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = LayoutInflater.from(context);
			mContext = context;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			TextView cellIdView = (TextView) view
					.findViewById(R.id.cell_row_cell_id1);

			int cellId = cursor.getInt(cursor
					.getColumnIndex(CellLocationLog.CID));
			cellIdView.setText("cid: " + cellId);

			TextView cellTypeView = (TextView) view
					.findViewById(R.id.cell_row_type1);

			String station = cursor.getString(cursor
					.getColumnIndex(CellLocationLog.STATION_ID));

			cellTypeView.setText(station);
			
			
			TextView timeView = (TextView) view
					.findViewById(R.id.cell_row_time1);
			long timestamp = cursor.getLong(cursor
					.getColumnIndex(CellLocationLog.TIME));
			Date now = new Date(timestamp);
			timeView.setText(sdfDate.format(now));

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.show_cell_log_row,
					parent, false);
			return view;
		}

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(TAG, "history createView");

		Cursor cursor = getActivity().getContentResolver().query(
				CellLocationLog.CONTENT_URI,
				new String[] { CellLocationLog._ID, CellLocationLog.CID,
						CellLocationLog.STATION_ID, CellLocationLog.TIME },
				null, null, null);
		
		getActivity().startManagingCursor(cursor);

		LogsCursorAdapter mAdapter = new LogsCursorAdapter(getActivity(), cursor);

		this.setListAdapter(mAdapter);
		
		return inflater.inflate(R.layout.history, container, false);
	}
	
	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {

	}
}
