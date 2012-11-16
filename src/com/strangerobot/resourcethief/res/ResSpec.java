package com.strangerobot.resourcethief.res;

import com.google.common.base.Objects;
import com.strangerobot.resourcethief.util.Hex;


public class ResSpec implements Comparable<ResSpec>{
	private ResTable mTable;
	private int mId;
	private ResType mType;
	private String mName;
	
	ResSpec(ResTable table, ResType type, int id){
		mTable = table;
		mType  = type;
		mId    = id;
	}
	
	public ResType getType(){
		return mType;
	}
	
	public ResTable getTable(){
		return mTable;
	}
	
	public String getName(){
		if(mName == null){
			return mTable.getEntryName(mId);
		}
		return mName;
	}
	
	public int getId(){
		return mId;
	}
	
	public int hashCode(){
		return mId;
	}
	
	public int compareTo(ResSpec other){
		return getName().compareToIgnoreCase(other.getName());
	}
	
	public String toString(){
		return Objects.toStringHelper(this)
				.add("id", Hex.hex((short)mId))
				.add("name", getName())
				.add("type", getType())
				.toString();
	}
	
	public boolean equals(Object other){
		if(other == null)
			return false;
		if(other.getClass() != ResSpec.class)
			return false;
		return mId == ((ResSpec)other).mId;
	}
}
