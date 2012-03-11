package net.qlun.celllogger.fragment;

import net.qlun.celllogger.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HistoryFragment extends Fragment {

	private static final String TAG = "h-f";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(TAG, "history createView");

		return inflater.inflate(R.layout.history, container, false);
	}
}
