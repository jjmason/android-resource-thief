package jm.rt.service;

import android.os.Parcel;
import android.os.Parcelable;

public class ExportStatus implements Parcelable {
	public static final int STATUS_UNKNOWN 	= 0x01;
	public static final int STATUS_RUNNING 	= 0x02;
	public static final int STATUS_COMPLETE = 0x04;
	public static final int STATUS_ERROR	= 0x08;
	
	public final String packageName;
	public  final int resourceId;
	public final int status;

	
	public ExportStatus(String packageName, int resourceId, int status){
		this.status = status;
		this.packageName = packageName;
		this.resourceId = resourceId;
	}
 
	
	
	@Override
	public int describeContents() { 
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageName);
		dest.writeInt(resourceId);
		dest.writeInt(status);
	}
	
	public static final Parcelable.Creator<ExportStatus> CREATOR = 
			new Parcelable.Creator<ExportStatus>() {
				public ExportStatus createFromParcel(Parcel source) {
					String packageName = source.readString();
					int resourceId = source.readInt();
					int status = source.readInt();
					return new ExportStatus(packageName, resourceId, status);
				}
				public ExportStatus[] newArray(int size) {
					return new ExportStatus[size];
				}
			};
	 

}
