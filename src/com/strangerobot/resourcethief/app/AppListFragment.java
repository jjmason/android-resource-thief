package com.strangerobot.resourcethief.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView.BufferType;

import com.strangerobot.resourcethief.R;
import com.strangerobot.resourcethief.util.ViewAssistant;

public class AppListFragment extends ListFragment {
	public interface Callbacks {
		void onListItemClick(ApplicationInfo appInfo);
	}
	
	private static class AppItem {
		public final ApplicationInfo info;
		public final String label;
		public final Drawable icon;

		public AppItem(ApplicationInfo info, Context context) {
			PackageManager pm = context.getPackageManager();
			this.info = info;
			label = info.loadLabel(pm).toString();
			Drawable d = info.loadIcon(pm);
			if (d == null) {
				d = context.getResources().getDrawable(
						R.drawable.app_icon_missing);
			}
			icon = d;
		}

	}

	private AppListLoader mLoader;
	
	private class AppListLoader extends AsyncTask<Void, Void, List<AppItem>> {
		private ProgressDialog mProgressDialog; 
		@Override
		protected List<AppItem> doInBackground(Void... params) {

			List<ApplicationInfo> infos = getActivity().getPackageManager()
					.getInstalledApplications(0);
			List<AppItem> items = createAppItems(infos, getActivity());
			return items;
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(getActivity());
			mProgressDialog.setMessage(getText(R.string.loading_apps));
			mProgressDialog.show();
		}

		@Override
		protected void onPostExecute(List<AppItem> result) {
			mListAdapter.setItems(result);
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
			}
			if(mLoader == this){
				mLoader = null;
			}
		}
	}

	private static final String STATE_APPS = "apps";

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onListItemClick(ApplicationInfo appInfo) {
		}
	};
	
	private AppListAdapter mListAdapter;
	
	private Callbacks mCallbacks = sDummyCallbacks;
	
	
	
	private class AppListAdapter extends BaseAdapter {
		private List<AppItem> mItems;
		private List<AppItem> mFilteredItems;
		private List<Pair<Integer, Integer>> mMatchedSpans;
		private Filter mFilter;

		public Filter getFilter() {
			if (mFilter == null)
				mFilter = new AppListFilter();
			return mFilter;
		}

		public List<AppItem> getItems() {
			return mItems;
		}

		public void setFilteredItems(List<AppItem> filteredItems,
				List<Pair<Integer, Integer>> matchedSpans) {
			mFilteredItems = filteredItems;
			mMatchedSpans = matchedSpans;
			notifyDataSetChanged();
		}

		public void restoreUnfilteredItems() {
			mFilteredItems = null;
			mMatchedSpans = null;
			notifyDataSetChanged();
		}

		public void setItems(List<AppItem> items) {
			mItems = items;
			mFilteredItems = null;
			mMatchedSpans = null;
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.app_list_item, null);
			}
			AppItem item = getItem(position);
			CharSequence label = item.label;
			Pair<Integer, Integer> span = getMatchedSpan(position);
			BufferType bt = BufferType.NORMAL;
			if (span != null) {
				SpannableString ss = new SpannableString(label);
				ss.setSpan(new BackgroundColorSpan(getActivity().getResources()
						.getColor(android.R.color.holo_red_light)), span.first,
						span.second, 0);
				ss.setSpan(new ForegroundColorSpan(Color.WHITE), span.first,
						span.second, 0);
				label = ss;
				bt = BufferType.SPANNABLE;
			}
			new ViewAssistant(getActivity(), convertView)
					.setText(R.id.label, label, bt)
					.setText(R.id.packageName, item.info.packageName)
					.setImageDrawable(R.id.icon, item.icon);
			return convertView;
		}

		public int getCount() {
			if (mFilteredItems != null) {
				return mFilteredItems.size();
			}
			if (mItems != null) {
				return mItems.size();
			}
			return 0;
		}

		public AppItem getItem(int position) {
			if (mFilteredItems != null) {
				return mFilteredItems.get(position);
			}
			return mItems.get(position);
		}

		public Pair<Integer, Integer> getMatchedSpan(int position) {
			if (mMatchedSpans != null && position < mMatchedSpans.size()) {
				return mMatchedSpans.get(position);
			}
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

	}

	private class AppListFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			List<AppItem> current = mListAdapter.getItems();
			List<AppItem> tmp;
			synchronized (current) {
				tmp = new ArrayList<AppItem>(current);
			}

			FilterResults results = new FilterResults();
			results.count = tmp.size();
			results.values = null;

			// remove the filter completely if it's null
			if (constraint == null) {
				return results;
			}

			String c = constraint.toString().trim().toLowerCase();

			// or if it's empty
			if (c.isEmpty()) {
				return results;
			}

			// but it isn't, so we'll actually filter it.
			List<AppItem> filtered = new ArrayList<AppItem>();
			List<Pair<Integer, Integer>> spans = new ArrayList<Pair<Integer, Integer>>();
			results.values = new Pair<List<AppItem>, List<Pair<Integer, Integer>>>(
					filtered, spans);

			for (AppItem item : tmp) {
				String ls = item.label.trim().toLowerCase();
				int i = ls.indexOf(c);
				if (i >= 0) {
					int end = c.length() + i;
					spans.add(new Pair<Integer, Integer>(i, end));
					filtered.add(item);
				}
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			if (results.values == null) {
				mListAdapter.restoreUnfilteredItems();
			} else {
				@SuppressWarnings("unchecked")
				Pair<List<AppItem>, List<Pair<Integer, Integer>>> values = (Pair) results.values;
				mListAdapter.setFilteredItems(values.first, values.second);
			}
		}

	};

	private final OnQueryTextListener mOnQueryTextListener = new OnQueryTextListener() {

		public boolean onQueryTextSubmit(String query) {
			// don't care
			return false;
		}

		public boolean onQueryTextChange(String newText) {
			if (mListAdapter != null) {
				mListAdapter.getFilter().filter(newText);
			}
			return false;
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(!(activity instanceof Callbacks))
			throw new IllegalStateException("activity must implement Callbacks");
		mCallbacks = (Callbacks) activity;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallbacks.onListItemClick(mListAdapter.getItem(position).info);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(mListAdapter = new AppListAdapter());

		if (savedInstanceState != null) {
			ApplicationInfo[] infos = (ApplicationInfo[]) savedInstanceState
					.getParcelableArray(STATE_APPS);
			mListAdapter.setItems(createAppItems(infos));
		} else {
			new AppListLoader().execute();
		}
		setHasOptionsMenu(true);
		Activity activity = getActivity();
		if(activity != null){
			
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		synchronized(this){
			if(mLoader != null){
				try{
					mLoader.cancel(true);
				}finally {
					mLoader = null;
				}
			}
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.app_list, menu);
		SearchView searchView = (SearchView) menu.findItem(R.id.searchView)
				.getActionView();
		searchView.setQueryHint(getText(R.string.app_search_hint));
		searchView.setOnQueryTextListener(mOnQueryTextListener);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mListAdapter != null) {
			ApplicationInfo[] infos = new ApplicationInfo[mListAdapter
					.getCount()];
			for (int i = 0; i < mListAdapter.getCount(); i++) {
				infos[i] = mListAdapter.getItem(i).info;
			}
			outState.putSerializable(STATE_APPS, infos);
		}
	}

	private List<AppItem> createAppItems(ApplicationInfo[] infos) {
		return createAppItems(Arrays.asList(infos));
	}

	private List<AppItem> createAppItems(List<ApplicationInfo> infos){
		return createAppItems(infos, getActivity());
	}

	private List<AppItem> createAppItems(List<ApplicationInfo> infos, Context context) {
		ArrayList<AppItem> items = new ArrayList<AppItem>(infos.size());
		for (ApplicationInfo info : infos) {
			items.add(new AppItem(info, context));
		}
		Collections.sort(items, new Comparator<AppItem>() {
			@Override
			public int compare(AppItem lhs, AppItem rhs) {
				return lhs.label.compareToIgnoreCase(rhs.label);
			}
		});
		return items;
	}

}
