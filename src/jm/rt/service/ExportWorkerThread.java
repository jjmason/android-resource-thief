package jm.rt.service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import jm.rt.export.ApkFile;
import jm.rt.export.ApkManager;
import jm.util.Args;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.data.ResValuesFile;
import brut.androlib.res.data.value.ResFileValue;

class ExportWorkerThread extends ServiceThread {

	/**
	 * Data object passed with {@link ExportWorkerThread#EXPORT} messages.
	 */
	public static class ExportSpec {
		private String mApplicationName;
		private String mExportPath;
		private int[] mResourceIds;

		public static ExportSpec obtain() {
			return new ExportSpec();
		}

		public static ExportSpec obtain(String applicationName,
				String exportPath, int[] resourceIds) {
			ExportSpec es = obtain();
			es.mApplicationName = applicationName;
			es.mExportPath = exportPath;
			es.mResourceIds = resourceIds;
			return es;
		}

		public String getApplicationName() {
			return mApplicationName;
		}

		public String getExportPath() {
			return mExportPath;
		}

		public int[] getResourceIds() {
			return mResourceIds;
		}

		public void setApplicationName(String applicationName) {
			mApplicationName = Args.notNull(applicationName);
		}

		public void setExportPath(String exportPath) {
			mExportPath = Args.notNull(exportPath);
		}

		public void setResourceIds(int[] resourceIds) {
			mResourceIds = Args.notNull(resourceIds);
		}

		public void setResourcesIds(Collection<Integer> resourceIds) {
			mResourceIds = new int[Args.notNull(resourceIds).size()];
			Iterator<Integer> it = resourceIds.iterator();
			for (int i = 0; i < resourceIds.size(); i++) {
				mResourceIds[i] = it.next();
			}
		}
	}

	private class ApkCache extends LruCache<String, ApkFile> {
		public ApkCache(int maxSize) {
			super(maxSize);
		}

		@Override
		protected ApkFile create(String applicationName) {
			return loadApk(applicationName);
		}
		

	}

	private static final String TAG = "ExportWorkerThread";

	// message types
	/**
	 * Tell the worker to export some resources. msg.obj is an ExportSpec
	 * object.
	 */
	public static final int EXPORT = 1;

	/**
	 * Tell the worker to quit.
	 */
	public static final int QUIT = 2;

	/**
	 * Tell the worker to adjust the size of it's apk cache. msg.arg1 is the new
	 * cache size.
	 */
	public static final int SET_APK_CACHE_SIZE = 3;

	/**
	 * Tell the worker to clear it's apk cache.
	 */
	public static final int CLEAR_APK_CACHE = 4;

	/**
	 * Tell the worker that it should probably start loading the apk specified
	 * by the String in msg.obj.
	 * 
	 * Uses {@link PackageManager#getApplicationInfo(String, int)} to look up
	 * the application info and apk path.
	 * <p>
	 */
	public static final int PRELOAD_APK = 5;

	private static final int DEFAULT_CACHE_SIZE = 10;

	private final StatusWatcherThread mWatcher;
	private final Context mContext;
	private final ApkCache mApkCache;
	
	public ExportWorkerThread(Context context, StatusWatcherThread watcher) {
		super("ExportWorkerThread");
		mWatcher = watcher;
		mContext = context;
		mApkCache = new ApkCache(DEFAULT_CACHE_SIZE);
	}

	@Override
	protected void handleMessage(Message msg) {
		try {
			handleMessageWrapped(msg);
		} catch (Exception e) {
			Log.e(TAG, "ExportWorkerThread message handler threw an exception",
					e);
		}
	}

	public void sendExport(ExportSpec spec){
		Message m = obtainMessage(EXPORT);
		m.obj = spec;
		sendMessage(m);
	}
	
	public void sendQuit(){
		sendMessage(obtainMessage(QUIT));
	}
	
	public void sendSetApkCacheSize(int newSize){
		Message m = obtainMessage(SET_APK_CACHE_SIZE);
		m.arg1 = newSize;
		sendMessage(m);
	}
	
	public void sendClearApkCache(){
		sendMessage(obtainMessage(CLEAR_APK_CACHE));
	}
	
	public void sendPreloadApk(String applicationName){
		Message m = obtainMessage(PRELOAD_APK);
		m.obj = applicationName;
		sendMessage(m);
	}
	
	private void handleMessageWrapped(Message msg) throws Exception {
		switch (msg.what) {
		case EXPORT:
			handleExport((ExportSpec) msg.obj);
			break;
		case QUIT:
			handleQuit();
			break;
		case SET_APK_CACHE_SIZE:
			handleSetApkCacheSize(msg.arg1);
			break;
		case CLEAR_APK_CACHE:
			handleClearApkCache();
			break;
		case PRELOAD_APK:
			handlePreloadApk((String) msg.obj);
			break;
		default:
			Log.w(TAG, String.format("ExportWorkerThread received "
					+ "an unknown message type: 0x%02X", msg.what));
		}
	}

	private void handleExport(ExportSpec exportSpec) throws Exception {
		// notify watcher of start...
		try{
			// export it
			
			// notify watcher of complete
		}catch(Exception e){
			// notify watcher of error
		}
	}

	private void handleQuit() throws Exception {
		Log.i(TAG, "ExportWorkerThread is QUITING!");
		Looper.myLooper().quit();
	}

	private void handleSetApkCacheSize(int newSize) throws Exception {
		mApkCache.resize(newSize);
	}

	private void handleClearApkCache() throws Exception {
		mApkCache.evictAll();
	}

	private void handlePreloadApk(String applicationName) throws Exception {
		// tell watcher what we're up to
		mApkCache.get(applicationName);
	}

	private ApkFile loadApk(String applicationName) {
		PackageManager pm = mContext.getPackageManager();
		ApplicationInfo appInfo;
		ApkFile apk;
		try {
			appInfo = pm.getApplicationInfo(applicationName,
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e1) {
			throw new RuntimeException("package " + applicationName
					+ " not found", e1);
		}
		String apkPath = appInfo.sourceDir;

		try {
			apk = ApkManager.getInstance().load(apkPath);
		} catch (IOException e) {
			throw new RuntimeException("error loading resources for "
					+ applicationName, e);
		}
		return apk;
	}
	
	private void export(ExportSpec exportSpec){

	}
}
