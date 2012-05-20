package net.qlun.celllogger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;

public class StationCell {

	private static final String TAG = "STATION_CELL";

	private static final String DATA_FILE = "CELL_DATA.json";

	private class Cell {
		int lac;
		int cid;

		@Override
		public int hashCode() {
			return lac + cid;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Cell) {
				Cell c = (Cell) o;
				return (c.lac == lac && c.cid == cid);
			}
			return false;
		}
	}

	private HashMap<Integer, HashSet<Cell>> _stations = new HashMap<Integer, HashSet<Cell>>();

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
		_stations.clear();
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

						Cell c = new Cell();
						c.lac = lac;
						c.cid = cid;

						// fill stations
						HashSet<Cell> st = _stations.get(id);

						if (st == null) {
							st = new HashSet<Cell>();
							_stations.put(id, st);
						}

						st.add(c);

					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			// Should never happen!
			e.printStackTrace();
		}
	}

	public void save(String data) {

		Log.v(TAG, "save");

		FileOutputStream fos = null;
		try {
			fos = ctx.openFileOutput(DATA_FILE, Context.MODE_PRIVATE);
			fos.write(data.getBytes());
			Log.v(TAG, "save to " + DATA_FILE);
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

	public static String getKey(Cell c) {
		return "" + c.lac + "," + c.cid;
	}

	public static String getKey(int lac, int cid) {
		return "" + lac + "," + cid;
	}

	public List<Integer> getIdList(int lac, int cid) {

		ArrayList<Integer> idList = new ArrayList<Integer>();

		STATION_LOOP: for (Entry<Integer, HashSet<Cell>> row : _stations
				.entrySet()) {
			int station_id = row.getKey();
			HashSet<Cell> cells = row.getValue();

			// if(cells == null ){
			// continue;
			// }
			for (Cell c : cells) {
				// Log.v(TAG, "ccc " + c.lac + "," + c.cid);
				if (c.lac == lac && c.cid == cid) {
					idList.add(station_id);
					continue STATION_LOOP;
				}
			}
		}

		return idList;
	}

	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++) {
			ret[i] = iterator.next().intValue();
		}
		return ret;
	}
}
