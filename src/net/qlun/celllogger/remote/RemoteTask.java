package net.qlun.celllogger.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import net.qlun.celllogger.util.Installation;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class RemoteTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "RemoteTask";
	private static final String USER_AGENT = "CellLogger/1.0";
	private static final String INSTALLATION = "X-Installation";
	
	protected final Context context;

	public RemoteTask(Context c) {
		super();

		this.context = c;
	}

	@Override
	protected String doInBackground(String... params) {

		String result = null;
		String url = params[0];
		String json = params[1];

		StringEntity postEntity;
		try {
			postEntity = new StringEntity(json, "UTF-8");
			postEntity.setContentType("application/json");
		} catch (UnsupportedEncodingException e1) {
			Log.w(TAG, "Error body encoding send to " + url);
			return null;
		}

		String installationId = Installation.id(context);

		final AndroidHttpClient client = AndroidHttpClient
				.newInstance(USER_AGENT);

		final HttpPost postRequest = new HttpPost(url);
		postRequest.setHeader(INSTALLATION, installationId);
		postRequest.setEntity(postEntity);

		try {
			HttpResponse response = client.execute(postRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w(TAG, "Error " + statusCode + " while retrieving from "
						+ url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {

				long fileLength = entity.getContentLength();

				Log.i(TAG, url + " , size to download: " + fileLength);

				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();

					StringBuffer out = new StringBuffer();

					int n;
					long total = 0;
					byte buf[] = new byte[4096];

					for (; (n = inputStream.read(buf)) != -1;) {
						total += n;
						out.append(new String(buf, 0, n));

						// publishing the progress....
						if (fileLength > 0) {
							publishProgress((int) (total * 100 / fileLength));
						}
					}

					result = out.toString();

				} finally {
					if (inputStream != null) {
						inputStream.close();
					}

					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			postRequest.abort();
			Log.w(TAG, "I/O error while retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			postRequest.abort();
			Log.w(TAG, "Incorrect URL: " + url);
		} catch (Exception e) {
			postRequest.abort();
			Log.w(TAG, "Error while retrieving bitmap from " + url, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}

		return result;

	}

	@Override
	protected void onProgressUpdate(Integer... progress) {

	}

	@Override
	protected void onPostExecute(String result) {

	}

}
