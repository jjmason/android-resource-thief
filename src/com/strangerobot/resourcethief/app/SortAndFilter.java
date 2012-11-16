package com.strangerobot.resourcethief.app;


public abstract class SortAndFilter<E> {
	private static final int REQUEST_TOKEN = 0xD0D0F00D;
	private static final int FINISH_TOKEN = 0xDEADBEEF;
	
	private boolean mSortChanged;
	private boolean mFilterChanged;
	private boolean mListChanged;
	
	public void notifyListChanged(){
		mListChanged = true;
		// send msg to thread
	}
	
	public void setSortField(int field){
		
	}
	
	public void setSortDirection(boolean ascending){
		
	}
	
	public void setFilterConstaint(CharSequence constraint){
		
	}
	 
}
