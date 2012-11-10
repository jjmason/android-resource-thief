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

	private ExportHistoryDb mDb;

	private Set<IExportListener> mListeners = new HashSet<IExportListener>();

	private Map<String, Map<Integer, Integer>> mPackageToIdToStatus = new LinkedHashMap<String, Map<Integer, Integer>>();

	private WorkerHandler mWorkerHandler;
	
	private final CountDownLatch mWorkerStartedLatch = 
			new CountDownLatch(1);
	
	private final IExportService.Stub mEndpoint = new IExportService.Stub() {

		@Override
		public void removeExportListener(IExportListener exportListener)
				throws RemoteException {
			ExportService.this.addExportListener(exportListener);
		}

		@Override
		public ExportStatus[] getExportStatus(String packageName)
				throws RemoteException {
			return ExportService.this.getExportStatus(packageName);
		}

		@Override
		public void exportResource(String packageName, int resourceId)
				throws RemoteException {
			ExportService.this.exportResource(packageName, resourceId);
		}

		@Override
		public void addExportListener(IExportListener exportListener)
				throws RemoteException {
			ExportService.this.removeExportListener(exportListener);
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
		mDb = new ExportHistoryDb(this);

		loadHistory();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// TODO commit export history changes to disk
	}

	// ///////////////////////
	// Service method impls
	// ///////////////////////
	private void addExportListener(IExportListener listener) {
		if (listener == null)
			throw new NullPointerException("listener may not be null");
		synchronized (mListeners) {
			mListeners.add(listener);
		}
	}

	private void removeExportListener(IExportListener listener) {
		if (listener == null)
			return;
		synchronized (mListeners) {
			mListeners.remove(listener);
		}
	}

	private ExportStatus[] getExportStatus(String packageName) {
		Map<Integer, Integer> idToStatus = null;
		synchronized (mPackageToIdToStatus) {
			idToStatus = mPackageToIdToStatus.get(packageName);
		}
		ArrayList<ExportStatus> result = new ArrayList<ExportStatus>();
		if (idToStatus != null) {
			synchronized (idToStatus) {
				for (Map.Entry<Integer, Integer> e : idToStatus.entrySet()) {
					result.add(new ExportStatus(packageName, e.getKey(), e
							.getValue()));
				}
			}
		}
		return result.toArray(new ExportStatus[result.size()]);
	}

	private void exportResource(String packageName, int resourceId) {

	}

	private void notifyListeners(String packageName, int resourceId, int status) {
		synchronized (mListeners) {
			for (IExportListener listener : mListeners) {
				try {
					listener.onExportStatusChanged(packageName, resourceId,
							status);
				} catch (RemoteException e) {
					Log.e(TAG, "error notifying listener", e);
				}
			}
		}
	}

	private void loadHistory() {
		List<ExportStatus> history = mDb.load();
		DefaultHashMap<String, Map<Integer, Integer>> map = new DefaultHashMap<String, Map<Integer, Integer>>(
				new Factory<Map<Integer, Integer>>() {
					@Override
					public Map<Integer, Integer> create(Object key) {
						return new HashMap<Integer, Integer>();
					}
				});
		for(ExportStatus es : history){
			map.get(es.packageName).put(es.resourceId, es.status);
		}
		mPackageToIdToStatus = new LinkedHashMap<String, Map<Integer,Integer>>(map);
	}
	
	private final class WorkerThread extends Thread {
		public WorkerThread() {
			super("ExportService$WorkerThread");
		}
		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			Looper.prepare();
			mWorkerHandler = new WorkerHandler();
			mWorkerStartedLatch.countDown();
			Looper.loop();
		}
	}
	
	private static final class MessageCounts {
		public int pending;
		public int complete;
		public int failed;
		public int ok; 
		
	}
	
	private final class WorkerHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
		
		}
	}
}
