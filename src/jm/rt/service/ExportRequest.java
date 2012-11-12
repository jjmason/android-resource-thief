package jm.rt.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import jm.util.Collect;
import android.os.Parcel;
import android.os.Parcelable;

public class ExportRequest implements Parcelable {
	public final String apkPath;
	public final String outPath;
	public final Set<Integer> resIds = new LinkedHashSet<Integer>();
	
	public ExportRequest(String apkPath, String outPath){
		this(apkPath, outPath, null);
	}
	
	public ExportRequest(String apkPath, String outPath, Collection<Integer> resIds){
		this.apkPath = apkPath;
		this.outPath = outPath;
		if(resIds != null)
			this.resIds.addAll(resIds);
	}
	
	@Override
	public int describeContents() { 
		return 0;
	}

	private ExportRequest(Parcel p){
		apkPath = p.readString();
		outPath = p.readString();
		int[] ia = p.createIntArray();
		for(int i : ia)
			resIds.add(i);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(apkPath);
		dest.writeString(outPath); 
		dest.writeIntArray(Collect.toIntArray(resIds));
	} 
	
	public static final Parcelable.Creator<ExportRequest> CREATOR = 
			new Parcelable.Creator<ExportRequest>() {
				public ExportRequest createFromParcel(Parcel source) {
					return new ExportRequest(source);
				}
				public ExportRequest[] newArray(int size) {
					return new ExportRequest[size];
				}
			};

}
