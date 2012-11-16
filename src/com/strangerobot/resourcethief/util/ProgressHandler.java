package com.strangerobot.resourcethief.util;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

@SuppressLint("HandlerLeak")
public abstract class ProgressHandler {
	private final Object mLock = new Object();
	private UpdateHandler mHandler;
	// synchronized on mLock
	private boolean mUpdateSent;
	private String mStatus;
	private boolean mIndeterminate;
	private int mProgress;
	private int mMax;

	private class UpdateHandler extends Handler {
		public UpdateHandler(){
			super();
		}
		public UpdateHandler(Looper looper){
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			String status = null;
			boolean indeterminate = true;
			int progress = 0;
			int max = 0;
			synchronized (mLock){
				mUpdateSent = false;
				status = mStatus;
				indeterminate = mIndeterminate;
				progress = mProgress;
				max = mMax;
			}
			onProgressUpdate(status, indeterminate, progress, max);
		}
	}

	public ProgressHandler(){
		if(Looper.myLooper() != Looper.getMainLooper()){
			Log.w("ProgressHandler", "creating a progress handler from a background thread is wierd!");
		}
		mHandler = new UpdateHandler();
	}
	
	@SuppressLint("HandlerLeak")
	public ProgressHandler(Looper looper){
		mHandler = new UpdateHandler(looper);
	}
	
	public void setStatus(String status){
		mStatus = status;
		sendUpdate();
	}
	
	public void setIndeterminate(boolean indeterminate){
		mIndeterminate = indeterminate;
		sendUpdate();
	}
	
	public void setProgress(int progress){
		if(progress < 0){
			progress = 0;
		}
		mProgress = progress;
		sendUpdate();
	}
	
	public void setMax(int max){
		if(max<0)
			max = 1;
		mMax = max;
		sendUpdate();
	}
	
	private void sendUpdate(){
		synchronized(mLock){
			if(!mUpdateSent){
				mUpdateSent = true;
				mHandler.sendEmptyMessage(0);
			}
		}
	}
	
	protected abstract void onProgressUpdate(String status, boolean indeterminate,
			int progress, int max);
	
	public static ProgressHandler createFromDialog(final ProgressDialog dlg){
		return new ProgressHandler() {
			
			@Override
			protected void onProgressUpdate(String status, boolean indeterminate,
					int progress, int max) {
				dlg.setMessage(status);
				dlg.setIndeterminate(indeterminate);
				if(!indeterminate){
					dlg.setProgress(progress);
					dlg.setMax(max);
				}
			}
		};
	}
}
