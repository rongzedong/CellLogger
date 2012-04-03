package net.qlun.celllogger.provider;

import org.json.JSONException;
import org.json.JSONObject;

public class CellLocationLogItem {

	public int id;
	public int network_type;
	public int cid;
	public int lac;
	public int station_id;
	public int signal_strength;
	public long time;

	public Object toJSONObject() {
		JSONObject jo = new JSONObject();

		try {

			jo.put("id", id);
			jo.put("network_type", network_type);
			jo.put("lac", lac);
			jo.put("cid", cid);
			jo.put("station", station_id);
			jo.put("signal", signal_strength);
			jo.put("time", time);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jo;
	}
}
