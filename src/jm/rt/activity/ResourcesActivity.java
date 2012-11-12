package jm.rt.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jm.android.util.background.ProgressTarget;
import jm.rt.R;
import jm.rt.export.ApkFile;
import jm.rt.export.ApkManager;
import jm.rt.export.Exporter;
import jm.rt.fragment.ResourceListFragment;
import jm.rt.fragment.ResourceListFragment.ListFragmentListener;
import jm.rt.res.AppResources;
import jm.rt.res.AppResources.ResourceLoadException;
import jm.rt.res.Resource;
import jm.rt.res.Resource.Type;
import jm.util.Functional.Predicate;
import jm.util.ListUtil;
import jm.util.ViewAssistant;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.SwipeyTabButton;
import com.astuetz.viewpager.extensions.SwipeyTabsView;
import com.astuetz.viewpager.extensions.TabsAdapter;

public class ResourcesActivity extends SherlockFragmentActivity implements
		ListFragmentListener, FragmentParamsProvider {
	public static final String EXTRA_PACKAGE_INFO = "packageInfo";
	private static final String TAG = "AppDetail";
	private static final String STATE_SELECTED_ITEMS = "selectedItems";
	private static final String STATE_RESOURCE_IDS = "resourceIds";

	private Map<String, String> sExtensionToDescription = new HashMap<String, String>();
	static {
		// TODO initilize sExtensionToDescription
	}

	private PackageInfo mPackageInfo;
	private List<Page> mPages = new ArrayList<ResourcesActivity.Page>();
	private ViewPager mPager;
	private SwipeyTabsView mTabs;
	private ActionMode mActionMode;
	private Set<Integer> mSelectedItems = new LinkedHashSet<Integer>();
	private AppResources mAppResources;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resources_activity);

		mPackageInfo = getIntent().getParcelableExtra(EXTRA_PACKAGE_INFO);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_SELECTED_ITEMS)) {
				int[] sel = savedInstanceState
						.getIntArray(STATE_SELECTED_ITEMS);
				for (int id : sel) {
					mSelectedItems.add(id);
				}
			}
			if (savedInstanceState.containsKey(STATE_RESOURCE_IDS)) {
				int[] ids = savedInstanceState.getIntArray(STATE_RESOURCE_IDS);
				try {
					mAppResources = AppResources.fromIdArray(this,
							mPackageInfo, ids);
				} catch (ResourceLoadException e) {
					throw new RuntimeException(e);
				}
			}
		}

		addPage("R.drawable", "No drawble resources found", Type.DRAWABLE);
		addPage("R.color", "No color resources found", Type.COLOR);
		addPage("R.anim", "No animation resources found", Type.ANIM);
		addPage("R.layout", "No layout resources found", Type.LAYOUT);
		addPage("R.menu", "No menu resources found", Type.MENU);
		addPage("R.xml", "No xml resources found", Type.XML);
		addPage("R.style", "No style resources found", Type.STYLE);
		addPage("R.raw", "No raw resources found", Type.RAW);

		mPager = (ViewPager) findViewById(R.id.pager);
		mTabs = (SwipeyTabsView) findViewById(R.id.tabs);
		mPager.setAdapter(new MyPagerAdapter());
		mTabs.setAdapter(new MyTabsAdapter());
		mTabs.setViewPager(mPager);

		if (mAppResources == null) {
			new ResourceLoaderTask().execute();
		}else{
			addResourcesToLists();
		}

	}

	private void addResourcesToLists() {
		if (mAppResources == null)
			throw new IllegalStateException(
					"wtf app resources shouldn't be null here");
		for (Page p : mPages) {
			if (p.fragmentType != Page.FRAGMENT_TYPE_RESOURCE_LIST)
				continue;
			if (p.types < 0 && p.filter != null) {
				p.listAdapter.mItems = ListUtil.filter(
						mAppResources.listResources(), p.filter);
			} else {
				p.listAdapter.mItems = mAppResources.listResources(p.types);
			}
			p.listAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mAppResources != null) {
			outState.putIntArray(STATE_RESOURCE_IDS, mAppResources.toIdArray());
		}
		if (mSelectedItems != null && !mSelectedItems.isEmpty()) {
			outState.putIntArray(STATE_SELECTED_ITEMS,
					setToArray(mSelectedItems));
		}
	}

	private static int[] setToArray(Set<Integer> set) {
		int[] ia = new int[set.size()];
		int idx = 0;
		for (int i : set) {
			ia[idx++] = i;
		}
		return ia;
	}

	private ResourceListAdapter newAdapter() {
		return new ResourceListAdapter();
	}

	private void addPage(CharSequence title, CharSequence empty, Type... types) {
		addPage(title, empty, Type.buildMask(types));
	}

	private void addPage(CharSequence title, CharSequence empty, int types) {
		mPages.add(Page.resourceListPage(newAdapter(), title, empty, types));
	}

	@Override
	public FragmentParams getFragmentParams(int fragmentIndex) {
		Page page = mPages.get(fragmentIndex);
		return new FragmentParams(page.emptyText, page.listAdapter);
	}

	@Override
	public void onListItemClick(int index, int position) {
		Toast.makeText(this, "clicked " + index + " => " + position,
				Toast.LENGTH_LONG).show();
	}

	private class ResourceLoaderTask extends AsyncTask<Void, Integer, Object> {
		private ProgressDialog mProgressDialog;

		@Override
		protected Object doInBackground(Void... params) {
			try {
				return AppResources.load(ResourcesActivity.this, mPackageInfo,
						ProgressTarget.Factory.createTarget(mProgressDialog));
			} catch (Exception e) {
				Log.e(TAG, "error loading resources", e);
				return e;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			if (result instanceof AppResources) {
				mAppResources = (AppResources) result;
				addResourcesToLists();
				// do this AFTER setting the items
				dismissDialog();
			} else {
				dismissDialog();
				AlertDialog error = new AlertDialog.Builder(
						ResourcesActivity.this)
						.setTitle("FAIL!")
						.setMessage(
								"An error occured while loading resources: "
										+ result)
						.setIcon(android.R.drawable.ic_dialog_alert).create();
				error.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
					}
				});
			}
		}

		private void dismissDialog() {
			// dismiss() throws an exception if the dialog
			// is no longer attached to the window, which
			// happens when the activity is killed while
			// the task is running.
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
			}
		}

		@TargetApi(11)
		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(ResourcesActivity.this);
			mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMessage(mPackageInfo.packageName);
			mProgressDialog.setTitle("Loading Resources");
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mProgressDialog.setProgressNumberFormat(null);
			}
			mProgressDialog.show();
		}
	}

	private static class Page {
		public static final int FRAGMENT_TYPE_RESOURCE_LIST = 0,
				FRAGMENT_TYPE_ASSETS = 1, FRAGMENT_TYPE_MANIFEST = 2;
		public int fragmentType;
		public ResourceListAdapter listAdapter;
		public CharSequence title;
		public CharSequence emptyText;
		public int types = -1;
		public Predicate<Resource> filter;

		public static Page genericPage(CharSequence title, int fragmentType) {
			Page p = new Page();
			p.title = title;
			p.fragmentType = fragmentType;
			return p;
		}

		public static Page resourceListPage(ResourceListAdapter adapter,
				CharSequence title, CharSequence emptyText, int types) {
			Page p = genericPage(title, FRAGMENT_TYPE_RESOURCE_LIST);
			p.emptyText = emptyText;
			p.types = types;
			p.listAdapter = adapter;
			return p;
		}
	}

	private static class MaskFilter implements Predicate<Resource> {
		public final int mask;

		public MaskFilter(int mask) {
			this.mask = mask;
		}

		public MaskFilter(Type... types) {
			mask = Type.buildMask(types);
		}

		public boolean test(Resource res) {
			return res.getType().match(mask);
		}
	}

	private class MyTabsAdapter implements TabsAdapter {
		@Override
		public View getView(int position) {
			SwipeyTabButton tab = (SwipeyTabButton) getLayoutInflater()
					.inflate(R.layout.tab, null);
			if (position < mPages.size()) {
				tab.setText(mPages.get(position).title);
			}
			return tab;
		}
	}

	private class MyPagerAdapter extends FragmentPagerAdapter {

		public MyPagerAdapter() {
			super(getSupportFragmentManager());
		}

		@Override
		public int getCount() {
			return mPages.size();
		}

		@Override
		public Fragment getItem(int position) {
			ResourceListFragment fragment = new ResourceListFragment();
			Bundle args = new Bundle();
			args.putInt(ResourceListFragment.INDEX_KEY, position);
			fragment.setArguments(args);
			return fragment;
		}

	}

	private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			mSelectedItems.clear();
			refreshListAdapters();
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			getSupportMenuInflater().inflate(R.menu.resource_action_mode, menu);
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menuExportLater:
				Toast.makeText(ResourcesActivity.this,
						"Export later not implemented", Toast.LENGTH_SHORT)
						.show();
				return true;
			case R.id.menuExportNow:
				exportSelectedItems();
				return true;
			default:
				return false;
			}
		}
	};

	private void exportSelectedItems() {
		File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
		if (!dir.exists()) {
			Log.e(TAG, "can't write to " + dir.getAbsolutePath());
			return;
		}
		ApkFile apk = null;
		try{
			apk = ApkManager.getInstance().load(mPackageInfo);
		}catch(IOException e){
			Log.e(TAG, "couldn't create apk file in " + mPackageInfo.applicationInfo.sourceDir,e);
			return;
		}
		
		Exporter exporter = new Exporter(this, apk, dir.getAbsolutePath(), mSelectedItems, null);
		Log.d(TAG, "exporting...");
		try{
			exporter.export();
		}catch(Exception e){
			Log.e(TAG, "FAIL", e);
			return;
		}
		Log.d(TAG,"done!");
	}

	public boolean isItemSelected(int id) {
		return mSelectedItems.contains(id);
	}

	public void setItemSelected(int id, boolean selected,
			boolean refreshListAdapters) {
		boolean changed = selected ? mSelectedItems.add(id) : mSelectedItems
				.remove(id);
		if (changed) {
			if (refreshListAdapters) {
				refreshListAdapters();
			}
			boolean empty = mSelectedItems.isEmpty();
			if (empty) {
				if (mActionMode != null) {
					mActionMode.finish();
				}
			} else {
				if (mActionMode == null) {
					mActionMode = startActionMode(mActionModeCallback);
				}
			}
		}
	}

	private void refreshListAdapters() {
		for (Page p : mPages) {
			if (p.listAdapter != null) {
				p.listAdapter.notifyDataSetChanged();
			}
		}
	}

	private class ResourceListAdapter extends BaseAdapter {
		private List<Resource> mItems;

		@Override
		public int getCount() {
			return mItems == null ? 0 : mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mItems.get(position).id;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			if (v == null) {
				v = getLayoutInflater().inflate(R.layout.resource_list_item,
						parent, false);
			}
			ViewAssistant va = new ViewAssistant(ResourcesActivity.this, v);

			final Resource r = mItems.get(position);

			va.setText(R.id.primaryText, r.getName());

			if (r.getType().match(Type.DRAWABLE, Type.COLOR, Type.RAW)) {
				va.show(R.id.iconContainer);
				Drawable d = r.getDrawable();
				if (d == null) {
					d = getResources().getDrawable(
							R.drawable.error_loading_drawable);
				}
				va.setImageDrawable(R.id.image, d);
			} else {
				va.hide(R.id.iconContainer, R.id.divider1);
			}

			va.setTextOrHide(R.id.secondaryText, getSecondaryText(r));

			final CheckBox cb = (CheckBox) va.find(R.id.checkbox);
			// don't fire an existing listener!
			cb.setOnCheckedChangeListener(null);
			cb.setChecked(isItemSelected(r.id));
			// need this so that the list items remain clickable
			cb.setFocusable(false);
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					setItemSelected(r.id, isChecked, false);
				}
			});
			return v;
		}

		private CharSequence getSecondaryText(Resource r) {
			if (r.getErrorMessage() != null)
				return r.getErrorMessage();
			switch (r.getType()) {
			case DRAWABLE:
				if (r.getDrawable() == null) {
					return r.getErrorMessage();
				}
				return r.getDrawable().getClass().getSimpleName();
			case COLOR:
				if (r.hasColor()) {
					return String.format("#%08X", r.getColor());
				}
			case RAW:
				String fn = r.getFileName();
				if (fn != null) {
					return describeFileName(fn);
				}
			default:
				return null;
			}
		}

		private CharSequence describeFileName(String fileName) {
			String[] parts = fileName.split("\\.");
			String ext = parts[parts.length - 1];
			String desc = sExtensionToDescription.get(ext);
			if (desc == null) {
				desc = String.format(".%s file", ext);
			}
			return desc;
		}
	}
}