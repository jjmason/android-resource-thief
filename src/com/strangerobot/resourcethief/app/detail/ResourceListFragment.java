package com.strangerobot.resourcethief.app.detail;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.strangerobot.resourcethief.res.ResTable;



public class ResourceListFragment extends ListFragmentBase {
	private static final String TAG = "ResourceListFragment";
	
	private ResourceListParent mResourceListParent;
	
	protected ResourceListParent getResourceListParent(){
		return mResourceListParent;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(!(activity instanceof ResourceListParent)){
			throw new IllegalStateException("activity does not implement ResourceListParent");
		}
		mResourceListParent = (ResourceListParent) activity;
		
		Futures.addCallback(mResourceListParent.getResTable(), mResTableCallback);
	} 
	
	private final FutureCallback<ResTable> mResTableCallback = new FutureCallback<ResTable>() {
		@Override
		public void onSuccess(ResTable result) {
			if(Looper.myLooper() == Looper.getMainLooper()){
				
			}else{
				
			}
		}
		
		public void onFailure(Throwable t) {
			Log.e(TAG, "getResTable future threw an error", t);
		}
	};
	
	private void createItems(){
		
	}
	
	
	
}
