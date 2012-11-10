package jm.rt.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jm.rt.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class ApplicationsActivity extends SherlockListActivity {

	private List<PackageInfo> mPackages = new ArrayList<PackageInfo>();
	private Handler mHandler = new Handler();
	
	 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.app_list); 
		loadApps();  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SearchView searchView = new SearchView(getSupportActionBar()
				.getThemedContext());

		searchView.setOnQueryTextListener(new QueryTextListener());

		menu.add("Search").setIcon(R.drawable.abs__ic_search_api_holo_light)
				.setActionView(searchView)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		PackageInfo info = mPackages.get(position);
		Intent intent = new Intent(this, ResourcesActivity.class);
		intent.putExtra(ResourcesActivity.EXTRA_PACKAGE_INFO, info);
		startActivity(intent);
	} 

	public void loadApps() {
		setListAdapter(null);
		final ProgressDialog progressDialog = 
				new ProgressDialog(this);
		progressDialog.setTitle("Loading applications...");
		progressDialog.setIndeterminate(true);
		AsyncTask<Void, Void, List<PackageInfo>> task = new AsyncTask<Void, Void, List<PackageInfo>> (){
			@Override
			protected List<PackageInfo> doInBackground(Void... params) {
				return  getPackageManager()
						.getInstalledPackages(
								PackageManager.GET_ACTIVITIES
								| PackageManager.GET_INTENT_FILTERS
								| PackageManager.GET_RECEIVERS
								| PackageManager.GET_PROVIDERS
								| PackageManager.GET_META_DATA
								| PackageManager.GET_PERMISSIONS
								| PackageManager.GET_SERVICES
								| PackageManager.GET_SIGNATURES);
			}
			@Override
			protected void onPreExecute() {
				progressDialog.show();
			}
			@Override
			protected void onPostExecute(List<PackageInfo> result) {
				progressDialog.dismiss();
				mPackages = result;
				setListAdapter(new AppListAdapter(mPackages));
			}
		};
		task.execute();
	}

	private HashMap<String, Drawable> mBadDrawables = new HashMap<String, Drawable>();

	/*
	 * So. This *should* be easy, but at least one app (System UI) has an icon
	 * that is HUGE. So we scale it down here.
	 */
	private Drawable getIconDrawable(ApplicationInfo appInfo) {
		if (mBadDrawables.containsKey(appInfo.packageName)) {
			return mBadDrawables.get(appInfo.packageName);
		}

		Drawable drawable = appInfo.loadIcon(getPackageManager());
		
		if(drawable == null){
			drawable = getResources().getDrawable(R.drawable.ic_launcher);
			return drawable;
		}
		
		if ((drawable instanceof BitmapDrawable)
				&& (drawable.getMinimumHeight() > 96 || drawable
						.getMinimumWidth() > 96)) {
			Bitmap scaled = Bitmap.createScaledBitmap(
					((BitmapDrawable)drawable).getBitmap(), 96, 96, true);
			drawable = new BitmapDrawable(getResources(), scaled);
			mBadDrawables.put(appInfo.packageName, drawable);
		}
		return drawable;
	}

	private class AppListAdapter extends ArrayAdapter<PackageInfo> {

		public AppListAdapter(List<PackageInfo> packageInfos){
			super(ApplicationsActivity.this, 0, packageInfos);
		}
		  
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.app_list_item, parent, false);
			}
			PackageManager pm = getPackageManager();
			PackageInfo info =  getItem(position);
			CharSequence label = info.applicationInfo.loadLabel(pm);
			CharSequence pkg = info.packageName;
			Drawable icon = info.applicationInfo.loadIcon(pm);
			if (icon == null) {
				icon = getResources().getDrawable(R.drawable.ic_launcher);
			}
			((TextView) convertView.findViewById(R.id.label)).setText(label);
			((TextView) convertView.findViewById(R.id.packageName))
					.setText(pkg);
			((ImageView) convertView.findViewById(R.id.icon))
					.setImageDrawable(getIconDrawable(info.applicationInfo));
			return convertView;
		}

	} 

	private class QueryTextListener implements SearchView.OnQueryTextListener {

		@Override
		public boolean onQueryTextSubmit(String query) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
