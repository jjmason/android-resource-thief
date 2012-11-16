package com.strangerobot.resourcethief.app.detail;

public interface DetailFragmentParent {
	public interface OnSortChangedListener {
		void onSortChanged(int field, boolean ascending);
	}
	public interface OnFilterChangedListener {
		void onFilterChanged(String query, boolean[] filters);
	}
	
	void setShowFilterControls(boolean showFilterControls);
	void setFilterItems(String[] labels, boolean[] values);
}
