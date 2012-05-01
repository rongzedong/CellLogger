package net.qlun.celllogger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;

public class StationCell {

	private static final String TAG = "STATION_CELL";

	private static final String DATA_FILE = "CELL_DATA.json";

	private Map<String, Integer> _cell2station = new LinkedHashMap<String, Integer>();

	private static StationCell _instance = null;
	private final Context ctx;

	private StationCell(Context ctx) {
		this.ctx = ctx;
	}

	public static synchronized StationCell getInstance(Context ctx) {
		if (_instance == null) {
			_instance = new StationCell(ctx);
			_instance.read();
		}
		return _instance;
	}

	public void reload() {
		Log.v(TAG, "reload");
		_cell2station.clear();
		read();
	}

	private void read() {
		Log.i(TAG, "init");

		try {
			InputStream is = ctx.openFileInput(DATA_FILE);

			int size = is.available();

			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();

			String text = new String(buffer);

			try {
				JSONObject r = (JSONObject) new JSONTokener(text).nextValue();
				JSONArray stations = r.getJSONArray("stations");

				for (int i = 0; i < stations.length(); i++) {
					JSONObject station = stations.getJSONObject(i);
					String name = station.getString("name");
					int id = station.getInt("id");
					JSONArray cells = station.getJSONArray("cells");
					for (int j = 0; j < cells.length(); j++) {
						JSONObject cell = cells.getJSONObject(j);
						int lac = cell.getInt("lac");
						int cid = cell.getInt("cid");

						String key = getKey(lac, cid);

						this._cell2station.put(key, id);
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

	public void save(String data) {

		Log.v(TAG, "save");

		FileOutputStream fos = null;
		try {
			fos = ctx.openFileOutput(DATA_FILE, Context.MODE_PRIVATE);
			fos.write(data.getBytes());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public static String getKey(int lac, int cid) {
		return "" + lac + "," + cid;
	}

	public int getId(int lac, int cid) {
		String key = getKey(lac, cid);
		return _cell2station.get(key);
	}

}
