package jm.rt.service;

import java.util.ArrayList;
import java.util.List;

import jm.util.Args;

import android.os.Message;

/**
 * Responds to updates in export status by showing notifications and notifying
 * listeners.
 */
class StatusWatcherThread extends ServiceThread {
	
	public static class MsgBase {
		public String applicationName;
		public String exportPath;
		public int queueSize;
		public int resourceCount; 
		public boolean isPreloadApk;
	}
 
	public static class MsgProgress extends MsgBase {
		public int progress;
		public int maxProgress;
	}
	
	public static class MsgText extends MsgBase {
		public String text;
	}
	
	
	public static final int QUEUED = 3;  
	public static final int STARTED = 4;
	public static final int PROGRESS = 5; 
	public static final int MESSAGE = 6;
	public static final int COMPLETE = 7;
	public static final int ERROR = 8;
	public static final int CANCELLED = 9;
	
	
	
	
	
	public StatusWatcherThread() {
		super("StatusWatcherThread");
	}

	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub

	}

}
