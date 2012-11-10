package jm.rt.export;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class Exporter {
	private final Context mContext;
	private final File mOutDirectory;
	private final ApplicationInfo mAppInfo;
	private final Collection<Integer> mResIds;
	private final Collection<String> mAssets;
 

	public Exporter(Context context, ApplicationInfo appInfo, File outDirectory,
			Collection<Integer> resIds, Collection<String> assets) {
		mContext = context;
		mAppInfo = appInfo;
		mOutDirectory = outDirectory;
		mResIds = new HashSet<Integer>(resIds);
		mAssets = new HashSet<String>(assets);
	}

	public void executeOnCurrentThread() throws Exception {
 
	}
	 
}
