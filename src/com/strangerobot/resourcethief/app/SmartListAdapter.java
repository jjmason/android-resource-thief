package com.strangerobot.resourcethief.app;

import java.util.Collections;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.BaseAdapter;

import com.google.common.collect.Lists;

public abstract class SmartListAdapter<ElemType, ConstraintType, OrderType>
		extends BaseAdapter {
	static final int UPDATE_TOKEN = 0xD0D0F00D;
	static final int FINISH_TOKEN = 0xDEADBEEF;

	private static final String TAG = "SmartListAdapter";

	private final Object mLock = new Object();
	private List<ElemType> mOriginalItems;
	private List<ElemType> mItems;
	private ConstraintType mConstraint;
	private OrderType mOrder;
	private UpdateHandler mUpdateHandler;
	private ResultHandler mResultHandler;

	public void setItems(Iterable<ElemType> items) {
		items = items == null ? Collections.<ElemType> emptyList() : items;
		mOriginalItems = Lists.newArrayList(items);
		if (mOrder != null || mConstraint != null) {
			sendUpdateRequest();
		} else {
			mItems = performUpdate();
			notifyDataSetChanged();
		}
	}
	
	public void sort(OrderType order){
		mOrder = order;
		sendUpdateRequest();
	}
	
	public void filter(ConstraintType constraint){
		mConstraint = constraint;
		sendUpdateRequest();
	}

	protected List<ElemType> getOriginalItems() {
		if (mOriginalItems == null) {
			mOriginalItems = Collections.emptyList();
		}
		return mOriginalItems;
	}

	@Override
	public int getCount() {
		return mItems == null ? 0 : mItems.size();
	}

	@Override
	public ElemType getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	protected abstract List<ElemType> performSort(List<ElemType> unsorted,
			OrderType order);

	protected abstract List<ElemType> performFilter(List<ElemType> unfiltered,
			ConstraintType constraint);

	private void sendUpdateRequest() {
		synchronized(mLock){
			if(mUpdateHandler == null){
				HandlerThread thread = new HandlerThread("SmartListAdapterWorker", 
						Process.THREAD_PRIORITY_BACKGROUND);
				thread.start();
				mUpdateHandler = new UpdateHandler(thread.getLooper());
			}
			mUpdateHandler.removeMessages(FINISH_TOKEN);
			mUpdateHandler.removeMessages(UPDATE_TOKEN);
			mUpdateHandler.sendEmptyMessage(UPDATE_TOKEN);
		}
	}

	private List<ElemType> performUpdate() {
		List<ElemType> items;
		OrderType order;
		ConstraintType constraint;
		synchronized(mLock){
			items = Lists.newArrayList(getOriginalItems());
			order = mOrder;
			constraint = mConstraint;
		}
		if (order != null) {
			items = performSort(items, order);
		}
		if (constraint != null) {
			items = performFilter(items, constraint);
		}
		return items;
	}

	private class ResultHandler extends Handler {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE_TOKEN) { 
				synchronized(mLock){
					if(msg.obj != null){
						mItems = (List<ElemType>) msg.obj;
					}
				}
				notifyDataSetChanged();
			} else {
				Log.w(TAG, "invalid message type " + msg.what);
			}
		}
	}

	private class UpdateHandler extends Handler {
		public UpdateHandler(Looper looper){
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_TOKEN:
				msg.obj = null;
				try{
					msg.obj = performUpdate();
				}catch(Exception e){
					Log.e(TAG, "performUpdate threw an exception", e);
				}
				mResultHandler.sendMessage(msg);
				
				synchronized(mLock){
					if(mUpdateHandler != null){
						mUpdateHandler.sendEmptyMessageDelayed(FINISH_TOKEN, 3000);
					}
				}
				break;
			case FINISH_TOKEN:
				synchronized (mLock) {
					if (mUpdateHandler != null) {
						mUpdateHandler.getLooper().quit();
						mUpdateHandler = null;
					}
				} 
				break;
			default:
				Log.w(TAG, "ThreadHandler got invalid message type " + msg.what);
			}
		}
	} 

}
