package com.strangerobot.resourcethief.app;

import java.io.IOException;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;

import com.strangerobot.resourcethief.R;
import com.strangerobot.resourcethief.res.ResTable;
import com.strangerobot.resourcethief.res.ResTableParser;

public class AppListActivity extends Activity implements AppListFragment.Callbacks{
	private static final String TAG = AppListActivity.class.getSimpleName();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_list_activity);  
	}  
	
	@Override
	public void onListItemClick(ApplicationInfo appInfo) {
		
	}
}
