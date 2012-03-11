package net.qlun.celllogger.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.TitleProvider;

public class TabFragmentAdapter extends FragmentPagerAdapter implements
		TitleProvider {

	protected static final String[] TITLES = new String[] { "History",
			"Record", "Upload" };

	protected static final Class<?>[] CLASSES = new Class<?>[] {
			HistoryFragment.class, RecordFragment.class, UploadFragment.class, };

	private int mCount = TITLES.length;

	public TabFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		Class<?> cls = CLASSES[position % TITLES.length];
		Fragment fm = null;
		try {
			fm = (Fragment) cls.newInstance();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fm;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public String getTitle(int position) {
		return TabFragmentAdapter.TITLES[position % TITLES.length];
	}
}