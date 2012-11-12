package jm.rt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import jm.util.DefaultHashMap;
import jm.util.DefaultHashMap.Factory;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

public class ExportService extends Service {
	private static final String TAG = ExportService.class.getSimpleName();
	public static final String ACTION = "jm.rt.action.BindExportService";

	private static class IdGen {
		private int mNext;
		public synchronized int next(){
			return mNext ++;
		}
	}
	private final IdGen mIdGen = new IdGen(); 
	
	
	private final class WorkerThread extends Thread {
		public WorkerThread(){
			super("ExportService$WorkerThread");
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
		}
	}
	
	private final Handler mWorkerHandler = new Handler() {
		public void handleMessage(Message msg) {
			
		}
	};
	
	private final IExportService.Stub mEndpoint = new IExportService.Stub() {

		@Override
		public int export(ExportRequest request) throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public ExportStatus getStatus(int requestId) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addStatusListener(IStatusListener listener)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeStatusListener(IStatusListener listener)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancel(int requestId) throws RemoteException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeFinished(){
			
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		if (ACTION.equals(intent.getAction())) {
			Log.d(TAG, "bound to intent " + intent);
			return mEndpoint;
		} else {
			Log.w(TAG, "recieved invalid action " + intent);
			return null;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate(); 
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

}
