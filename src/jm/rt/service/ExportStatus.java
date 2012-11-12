package jm.rt.service;

import jm.util.Args;
import android.os.Parcel;
import android.os.Parcelable;

public class ExportStatus implements Parcelable {
	public static enum Status {
		QUEUED(1),
		RUNNING(2),
		COMPLETE(3),
		ERROR(4);
		private final int code;
		private Status(int code){
			this.code = code;
		}
		public final int code(){ return code;}
		public static final Status fromCode(int code){
			for(Status st : values()){
				if(st.code == code){
					return st;
				}
			}
			throw new IllegalArgumentException("bad code: " + code);
		}
	}
	
	public int id;
	public Status status;
	public int progress;
	public int maxProgress;
	public String message;
	
	public ExportStatus(int id,Status status){
		this(id,status, -1, -1, null);
	}
	
	public ExportStatus(int id, Status status, String message){
		this(id,status, -1,  -1, message);
	}
	
	public ExportStatus(int id, Status status, int progress, int maxProgress){
		this(id,status, progress, maxProgress, null);
	}
	
	public ExportStatus(int id, Status status, int progress, int maxProgress, String message){
		this.id = id;
		this.status = Args.notNull(status);
		this.progress = progress;
		this.maxProgress = maxProgress;
		this.message = message == null ? getMessageFromStatus(status) : message;
	}
	
	private static String getMessageFromStatus(Status status){
		switch(status){
		case QUEUED:
			return "Queued";
		case RUNNING:
			return "Running";
		case COMPLETE:
			return "Done";
		case ERROR: 
			return "Failed";
		}
		return null; /* unreachable */
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(status.code());
		dest.writeInt(progress);
		dest.writeInt(maxProgress);
		dest.writeString(message);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	private ExportStatus(Parcel p){
		id = p.readInt();
		status = Status.fromCode(p.readInt());
		progress = p.readInt();
		maxProgress = p.readInt();
		message = p.readString();
	}
	
	public static final Parcelable.Creator<ExportStatus> CREATOR = 
			new Parcelable.Creator<ExportStatus>() {
				@Override
				public ExportStatus createFromParcel(Parcel source) {
					return new ExportStatus(source);
				}
				public ExportStatus[] newArray(int size) {
					return new ExportStatus[size];
				}
			};
}
