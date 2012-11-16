package com.strangerobot.resourcethief.app;

import java.util.LinkedHashSet;
import java.util.Set;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;

import com.astuetz.viewpager.extensions.SwipeyTabsView;

public class AppDetailActivity extends Activity {
	private ViewPager mPager;
	private SwipeyTabsView mTabs;
	private ActionMode mActionMode;
	private Set<Integer> mSelectedItems = new LinkedHashSet<Integer>();
}
