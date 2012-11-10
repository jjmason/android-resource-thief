package jm.rt.res;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import brut.androlib.ResException;
import brut.androlib.res.decoder.QuickAndDirtyARSCDecoder;
import jm.android.util.background.ProgressInputStream;
import jm.android.util.background.ProgressTarget;
import jm.util.Functional.Predicate;
import jm.util.ListUtil;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.Log;
import android.util.SparseArray;

public class AppResources {
	
	
	public static class ResourceLoadException extends IOException {
		private static final long serialVersionUID = 1L;
		public ResourceLoadException(){ super(); }
		public ResourceLoadException(String message){ super(message); }
		public ResourceLoadException(String message, Throwable cause){ super(message, cause); }
		public ResourceLoadException(Throwable cause){ super(cause); }
	}


	private static final String TAG = "AppResources";
	
	
	private final PackageInfo mPackageInfo;
	private final Context mContext;
 
	private List<Resource> mAllResources;
	private HashMap<Integer, Resource> mIdToResource = 
			new HashMap<Integer, Resource>();
	
	private SparseArray<List<Resource>> mMaskToResources = 
			new  SparseArray<List<Resource>>();  
	private Resources mPackageResources;
	
	
	private AppResources(Context context, PackageInfo packageInfo) {
		mPackageInfo = packageInfo;
		mContext = context; 
	}
	
	public static AppResources fromIdArray(Context context, 
			PackageInfo packageInfo, 
			int[] resIds) throws ResourceLoadException  {

		Resources packageResources;
		try{
			packageResources = context.getPackageManager()
				.getResourcesForApplication(packageInfo.applicationInfo);
		}catch(NameNotFoundException e){
			throw new ResourceLoadException("unable to get live resources", e);
		}

		AppResources result = new AppResources(context, packageInfo);
		
		LinkedHashMap<Integer, Resource> map = new LinkedHashMap<Integer, Resource>(3 * resIds.length);
		
		ArrayList<Resource> resources  = new ArrayList<Resource>(resIds.length);
		for(int resId : resIds){
			Resource res = new Resource(context, resId, packageResources);
			if(!res.isValid()){
				Log.w(TAG, String.format("skipping invalid resource 0x%08x", res.id));
			}else{
				resources.add(res);
				map.put(res.id, res);
			}
		}
		result.mIdToResource = map;
		result.mPackageResources = packageResources;
		result.mAllResources = Collections.unmodifiableList(resources);
		return result;
	}
	
	public static AppResources load(Context context, PackageInfo packageInfo, 
			ProgressTarget progressTarget) throws ResourceLoadException {

		if(progressTarget == null)
			progressTarget = ProgressTarget.Factory.createDummyTarget();
		
		ZipFile apk;
		try{
			apk = new ZipFile(packageInfo.applicationInfo.sourceDir);
		}catch(IOException e){
			throw new ResourceLoadException("unable to open apk file", e);
		}
		
		ZipEntry arsc = null;
		for(Enumeration<? extends ZipEntry> entries=apk.entries();entries.hasMoreElements();){
			ZipEntry entry = entries.nextElement();
			if(entry.getName().equals("resources.arsc")){
				arsc = entry; 
				break;
			}
		}
		if(arsc == null)
			throw new ResourceLoadException("no resources.arsc file found");
		
		InputStream in;
		try{
			in = new ProgressInputStream(apk.getInputStream(arsc),(int)arsc.getSize(), progressTarget);
		}catch(IOException e){
			throw new ResourceLoadException("error opening resources.arsc", e);
		}
		
		int[] resIds;
		try{
			resIds = QuickAndDirtyARSCDecoder.decode(in);
		}catch(ResException e){
			throw new ResourceLoadException(e);
		}finally{
			try{
				in.close();
			}catch(IOException e){
				throw new ResourceLoadException(e);
			}
		}

		return fromIdArray(context, packageInfo, resIds);
	}

	public List<Resource> listResources(){
		return mAllResources;
	}
	
	public List<Resource> listResources(Resource.Type...types){
		return listResources(Resource.Type.buildMask(types));
	}
	
	public List<Resource> listResources(final int typeMask){
		List<Resource> cached = mMaskToResources.get(typeMask);
		if(cached == null){
			mMaskToResources.put(typeMask,(cached = ListUtil.filter(mAllResources, 
					new Predicate<Resource>(){
						public boolean test(Resource res){
							return res.getType().match(typeMask);
						}
			})));
		}
		return cached;
	}
	
	public Resources getPackageResources(){
		if(mPackageResources == null){
			try{
				mPackageResources = mContext.getPackageManager().getResourcesForApplication(mPackageInfo.applicationInfo);
			}catch(NameNotFoundException e){
				throw new RuntimeException(e);
			}
		}
		return mPackageResources;
	}

	public Resource getResource(int id){
		return mIdToResource.get(id);
	}
	
	public int[] toIdArray(){
		int[] ids = new int[mAllResources.size()];
		for(int i=0;i<mAllResources.size();i++){
			ids[i] = mAllResources.get(i).id;
		} 
		return ids;
	}
}
