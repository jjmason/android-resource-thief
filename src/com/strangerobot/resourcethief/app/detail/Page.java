package com.strangerobot.resourcethief.app.detail;

import android.app.FragmentManager;
import android.content.Context;

public interface Page {
	CharSequence getTitle(Context context);
	ListFragmentBase createFragment(Context context, FragmentManager fm);
}
