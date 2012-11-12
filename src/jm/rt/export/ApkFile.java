package jm.rt.export;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import android.util.Log;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.decoder.ARSCDecoder;
import brut.androlib.res.decoder.ARSCDecoder.ARSCData;

public class ApkFile {
	private final ZipFile mZipFile;
	private ResTable mResTable;
	private ApkManager mManager;
	
	ApkFile(ApkManager manager, String path) throws IOException {
		mZipFile = new ZipFile(path);
		mManager = manager;
	}
	
	public ResTable getResTable(){
		if(mResTable == null){
			try {
				mResTable = loadResTable();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return mResTable;
	}
	
	private ResTable loadResTable() throws IOException{
		Log.d("apk", "loadResTable - " + mZipFile.getName());
		mResTable = loadResTableFrom(open("resources.arsc"));
		ResPackage fmk = mManager.getFrameworkPackage();
		if(fmk!=null&&!mResTable.hasPackage(fmk.getId())){
			mResTable.addPackage(fmk, false);
		}
		return mResTable;
	}
	
	public InputStream open(String path) throws IOException{
		return mZipFile.getInputStream(mZipFile.getEntry(path));
	}
	
	static ResTable loadResTableFrom(InputStream in) throws IOException{
		Log.d("apk", "loading it - " + in.available());
		ARSCData data= ARSCDecoder.decode(in, false, false);
		ResTable tab = data.getResTable();
		for(ResPackage p:data.getPackages()){
			if(!tab.hasPackage(p.getId())){
				tab.addPackage(p, true);
			}
		}
		return tab;
	}
}