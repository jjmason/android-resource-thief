package jm.rt.activity;

import android.widget.ListAdapter;

public class FragmentParams { 
	CharSequence mEmptyText;
	ListAdapter mListAdapter;  
	
	FragmentParams(CharSequence emptyText, ListAdapter listAdapter) { 
		mEmptyText = emptyText;
		mListAdapter = listAdapter; 
	}
	public CharSequence getEmptyText(){
		return mEmptyText;
	}
	public ListAdapter getListAdapter(){
		return mListAdapter;
	} 
}
