package com.strangerobot.resourcethief.app.detail;

import android.app.ListFragment;
import android.view.LayoutInflater;

public abstract class ListFragmentBase extends ListFragment {
	public LayoutInflater getLayoutInflater(){
		return getActivity().getLayoutInflater();
	}
}
