package net.qlun.celllogger.app;

import net.qlun.celllogger.R;
import net.qlun.celllogger.util.QualityUtil;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RecordActivity extends Activity {

	PhoneStateService mService = null;

	boolean mBound;

	private static final int MAX_SAMPLES = 60;
	protected static final long CHART_UPDATE_INTERVAL = 1000;

	protected static final String TAG = "CL";
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mCurrentSeries;
	private XYSeriesRenderer mCurrentRenderer;

	private GraphicalView mChartView;

	private Handler mHandler = new Handler();

	private int index = 0;

	private PhoneStateService.CurrentCellInfo previousCell = null;
	private PhoneStateService.CurrentCellInfo currentCell = null;

	private long time_switch = -1;

	private Runnable mChartUpdateTask = new Runnable() {
		public void run() {

			if (mBound) {

				PhoneStateService.CurrentCellInfo ci = mService
						.getCurrentCellInfo();

				signalQualityPercent = QualityUtil
						.getDbmQuality(ci.signalStrength);

				if (!ci.equals(currentCell)) {
					// switch cell
					previousCell = null;
					previousCell = currentCell;
					currentCell = ci;
					time_switch = System.currentTimeMillis();
				}

			} else {
				signalQualityPercent = 0;
			}

			drawQuality();

			showStateTexts();

			mHandler.postDelayed(this, CHART_UPDATE_INTERVAL);

		}
	};

	protected double signalQualityPercent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);

		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
		mRenderer.setAxisTitleTextSize(16);
		mRenderer.setChartTitleTextSize(20);
		mRenderer.setLabelsTextSize(15);
		mRenderer.setLegendTextSize(15);
		mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
		mRenderer.setZoomButtonsVisible(true);
		// mRenderer.setPointSize(2);

		mRenderer.setXAxisMax(MAX_SAMPLES);

		mRenderer.setYAxisMin(0);
		mRenderer.setYAxisMax(100);
		mRenderer.setShowGrid(true);
		{
			XYSeries series = new XYSeries("test1");
			mDataset.addSeries(series);
			mCurrentSeries = series;
			XYSeriesRenderer renderer = new XYSeriesRenderer();
			mRenderer.addSeriesRenderer(renderer);

			renderer.setFillPoints(true);
			// renderer.setFillBelowLine(true); // achartengine3 bug
			renderer.setLineWidth(2.0f);

			mCurrentRenderer = renderer;
		}

		mHandler.postDelayed(mChartUpdateTask, CHART_UPDATE_INTERVAL);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "start");
		Intent intent = new Intent(this, PhoneStateService.class);
		System.out.println(mConnection);
		getApplicationContext().bindService(intent, mConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(TAG, "stop");
		if (mBound) {
			try {
				getApplicationContext().unbindService(mConnection);
			} catch (IllegalArgumentException iae) {

			}
			mBound = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.chart);

			mChartView = ChartFactory.getCubeLineChartView(this, mDataset,
					mRenderer, 0.33f);

			mRenderer.setClickEnabled(false);
			mRenderer.setSelectableBuffer(100);
			mRenderer.setPanEnabled(false, false);
			mRenderer.setZoomEnabled(false, false);
			mRenderer.setZoomButtonsVisible(false);
			mRenderer.setShowLegend(false);
			// mRenderer.setShowLabels(false);
			mRenderer.setXLabels(0);

			layout.addView(mChartView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		} else {
			mChartView.repaint();
		}
	}

	private void drawQuality() {

		int total = mCurrentSeries.getItemCount();
		if (total >= MAX_SAMPLES) {

			mCurrentSeries.remove(0);
			mRenderer.setXAxisMax(index);
		}

		double x = index;
		double y = signalQualityPercent;
		mCurrentSeries.add(x, y);
		if (mChartView != null) {
			mChartView.repaint();
		}

		index++;

	}

	private void showStateTexts() {

		if (currentCell != null) {
			{
				TextView ti = (TextView) findViewById(R.id.current_cid);
				ti.setText("" + currentCell.cid);
			}
			{
				TextView ti = (TextView) findViewById(R.id.current_lac);
				ti.setText("" + currentCell.lac);
			}
		}

		if (previousCell != null) {
			{
				TextView ti = (TextView) findViewById(R.id.previous_cid);
				ti.setText("" + previousCell.cid);
			}
			{
				TextView ti = (TextView) findViewById(R.id.previous_lac);
				ti.setText("" + previousCell.lac);
			}
		}

		if (time_switch > 0) {

			long tc = (System.currentTimeMillis() - time_switch) / 1000;
			long hours = tc / 3600;
			long minutes = (tc % 3600) / 60;
			long seconds = tc % 60;

			String timeString = String.format("%02d:%02d:%02d", hours, minutes,
					seconds);

			TextView ti = (TextView) findViewById(R.id.time_change);
			ti.setText(timeString);
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.v(TAG, "service connected.");
			PhoneStateService.LocalBinder binder = (PhoneStateService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v(TAG, "service disconnected.");
			mBound = false;
		}
	};

}
