package net.qlun.celllogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;

public class Station {

	private static final String TAG = "STATION";

	private Map<CharSequence, Integer> _stations = new LinkedHashMap<CharSequence, Integer>();

	private static Station _instance = null;
	private final Context ctx;

	private Station(Context ctx) {
		this.ctx = ctx;
	}

	public static synchronized Station getInstance(Context ctx) {
		if (_instance == null) {
			_instance = new Station(ctx);
			_instance.init();
		}
		return _instance;
	}

	private void init() {
		Log.i(TAG, "init");

		try {
			InputStream is = ctx.getAssets().open("station.json");

			int size = is.available();

			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			String text = new String(buffer);

			try {
				JSONObject r = (JSONObject) new JSONTokener(text).nextValue();
				JSONArray lines = r.getJSONArray("stations");

				for (int i = 0; i < lines.length(); i++) {
					JSONObject line = lines.getJSONObject(i);
					String lineName = line.getString("line");
					JSONArray stations = line.getJSONArray("station");
					for (int j = 0; j < stations.length(); j++) {
						JSONObject station = stations.getJSONObject(j);
						String stationName = station.getString("name");
						int id = station.getInt("id");

						String key = lineName + "-" + stationName;

						this._stations.put(key, id);
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			// Should never happen!
			throw new RuntimeException(e);
		}
	}

	public CharSequence[] getAllItems() {
		return _stations.keySet().toArray(new CharSequence[] {});
	}

	public CharSequence[] getItems(String line) {
		String prefix = line + "-";

		List<CharSequence> ret = new ArrayList<CharSequence>();

		for (CharSequence item : _stations.keySet()) {
			if (item.toString().startsWith(prefix)) {
				ret.add(item);
			}
		}

		return ret.toArray(new CharSequence[] {});
	}

	public int getId(String xStation) {
		return _stations.get(xStation);
	}

	public String getName(int id) {

		for (Entry<CharSequence, Integer> x : _stations.entrySet()) {
			if (x.getValue() == id) {
				return x.getKey().toString();
			}
		}

		return "---";
	}
}
