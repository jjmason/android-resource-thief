package com.strangerobot.resourcethief.res;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.strangerobot.resourcethief.util.Hex;

public class ResType implements Comparable<ResType>, Iterable<ResSpec>{
	private ResTable mTable;
	private SortedSet<ResSpec> mSpecs = Sets.newTreeSet(); 
	private List<ResSpec> mSpecsList;
	private String mName;
	private int mId;
	
	ResType(ResTable table, int id, String name) {
		mTable = table;
		mId = id;
		mName = name;
	}
	
	ResSpec addSpec(int id){
		ResSpec spec = new ResSpec(mTable, this, id);
		if(mSpecs.add(spec)){
			mSpecsList = null;
			return spec;
		}
		return null;
	}
	
	public ResTable getTable(){
		return mTable;
	}
	
	public int getId(){
		return mId;
	}
	
	public String getName(){
		return mName;
	}
	
	public Iterator<ResSpec> iterator(){
		return Iterators.unmodifiableIterator(mSpecs.iterator());
	}
	
	public List<ResSpec> getSpecsList(){
		if(mSpecsList == null){
			mSpecsList = Lists.newArrayList(this);
		}
		return mSpecsList;
	}
	
	public Collection<ResSpec> getSpecs(){
		return Collections.unmodifiableSet(mSpecs);
	}
	
	@Override
	public int hashCode() {
		return mId;
	}
	
	public boolean equals(Object o) {
		return o != null && o.getClass() == ResType.class &&
				((ResType)o).mId == mId;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", mName)
			.add("id", Hex.hex((byte)mId))
			.add("specs", mSpecs.size())
			.toString();
	}
	
	@Override
	public int compareTo(ResType another) {
		return getName().compareToIgnoreCase(another.getName());
	}
}
