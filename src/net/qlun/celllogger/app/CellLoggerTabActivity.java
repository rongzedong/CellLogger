package net.qlun.celllogger.app;

import net.qlun.celllogger.R;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class CellLoggerTabActivity extends TabActivity {

	private static final String TAG_RECORD = "record";
	private static final String TAG_HISTORY = "history";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_tab);

		{

			addTabSpec(RecordActivity.class, TAG_RECORD, "Record");
			addTabSpec(HistoryActivity.class, TAG_HISTORY, "History");

			setCurrentTab(TAG_RECORD);
		}
	}

	private void setCurrentTab(String tag) {
		TabHost tabHost = getTabHost();
		tabHost.setCurrentTabByTag(TAG_RECORD);
	}

	private void addTabSpec(Class<?> cls, String tag, String indicator) {

		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;

		Intent intent = new Intent().setClass(this, cls);
		spec = tabHost.newTabSpec(tag).setIndicator(indicator)
				.setContent(intent);
		tabHost.addTab(spec);
	}
}