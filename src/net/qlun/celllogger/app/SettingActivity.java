package net.qlun.celllogger.app;

import net.qlun.celllogger.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SettingActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String EMPTY = "[empty]";
	private final String TAG = "CL-pref";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.setting);

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	protected void onStart() {
		super.onStart();

		PreferenceGroup group = getPreferenceScreen();
		initPreference(group);

	}

	private void initPreference(PreferenceGroup group) {
		int count = group.getPreferenceCount();
		for (int i = 0; i < count; i++) {
			Preference pref = group.getPreference(i);

			if (pref instanceof PreferenceGroup
					|| pref instanceof PreferenceScreen) {
				initPreference((PreferenceGroup) pref);
			} else {

				String key = pref.getKey();

				Log.v(TAG, "" + key);
				if (key == null) {
					continue;
				}

				if (key.equals("UploadEndpointUrl")) {

					String value = PreferenceManager
							.getDefaultSharedPreferences(this).getString(key,
									getString(R.string.remote_endpoint));
					pref.setSummary(value);
				} else if (key.equals("version")) {
					String versionName = "...";
					try {
						versionName = getPackageManager().getPackageInfo(
								getPackageName(), 0).versionName;
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					pref.setSummary(versionName);
				}
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		Log.v(TAG, "onSharedPreferenceChanged: " + key);

		Preference pref = getPreferenceScreen().findPreference(key);

		String value = sharedPreferences.getString(key, EMPTY);

		pref.setSummary(value);

	}
}