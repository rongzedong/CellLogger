package net.qlun.celllogger.fragment;

import net.qlun.celllogger.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class AlarmFragment extends Fragment implements OnClickListener {

	private static final String TAG = "CL-Alarm";

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

			}
			break;
		case R.id.button_upload_all:
			Log.v(TAG, "upload_all button click.");
			{

			}
			break;
		}

	}

}
