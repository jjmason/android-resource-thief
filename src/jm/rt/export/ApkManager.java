package jm.rt.export;

import java.io.IOException;
import java.util.zip.ZipFile;

import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.LruCache;

public class ApkManager {
	private static ApkManager sApkManager;
	public synchronized static ApkManager getInstance(){
		if(sApkManager == null)
			sApkManager = new ApkManager();
		return sApkManager;
	}
	private ApkManager(){}
	private boolean mLoadingFramework;
	private ResPackage mFrameworkPackage;
	
	public ResPackage getFrameworkPackage() throws IOException {
		if(mLoadingFramework){
			return null;
		}
		if(mFrameworkPackage == null){
			mLoadingFramework = true;
			try{
				ZipFile zf = new ZipFile("/system/framework/framework-res.apk");
				ResTable tab = ApkFile.loadResTableFrom(zf.getInputStream(zf.getEntry("resources.arsc")));
				mFrameworkPackage = tab.getPackage(1);
			}finally {
				mLoadingFramework = false;
			}
		}
		return mFrameworkPackage;
	}
	
	public ApkFile load(String path) throws IOException {
		ApkFile apk = new ApkFile(this, path);
		apk.getResTable();
		return apk;
	}
	public ApkFile load(ApplicationInfo appInfo) throws IOException{
		return load(appInfo.sourceDir);
	}
	public ApkFile load(PackageInfo pi) throws IOException{
		return load(pi.applicationInfo);
	}
}
