package jm.rt.service;

import jm.rt.res.Resource;
import jm.util.Args;
import android.os.Parcel;
import android.os.Parcelable;

public class ResourceGID implements Parcelable {
	private final String mPackageName;
	private final int mResourceId;
	
	public static ResourceGID of(Resource resource){
		return new ResourceGID(resource.getPackageName(), resource.id);
	}
	
	public ResourceGID(String packageName, int resourceId){
		mPackageName = Args.notNull(packageName);
		mResourceId = Args.notZero(resourceId);
	}
	
	public String getPackageName(){
		return mPackageName;
	}
	
	public int getResourceId(){
		return mResourceId;
	}
	
	public static final Parcelable.Creator<ResourceGID> CREATOR = 
			new Parcelable.Creator<ResourceGID>() {
				@Override
				public ResourceGID createFromParcel(Parcel source) {
					String packageName = source.readString();
					int resourceId = source.readInt();
					return new ResourceGID(packageName, resourceId);
				}
				
				@Override
				public ResourceGID[] newArray(int size) {
					return new ResourceGID[size];
				}
			};
			
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPackageName);
		dest.writeInt(mResourceId);
	}

}
