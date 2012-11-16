package com.strangerobot.resourcethief.res;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class IdMap {
	private final BiMap<Integer, Object> mMap = HashBiMap.create();
	public int getId(Object obj){
		return mMap.inverse().get(obj);
	}
	public Object getObject(int id){
		return mMap.get(id);
	}
}
