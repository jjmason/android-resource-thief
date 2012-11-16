package com.strangerobot.resourcethief.res;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.strangerobot.resourcethief.res.ResTableParser.ResTableParserException;
import com.strangerobot.resourcethief.util.HiddenApis.AssetManagerH;


public class ResTable { 
	private Map<Integer, ResType> mTypes = Maps.newHashMap();
	private List<ResType> mTypesList;
	private Resources mResources;
	
	public static ResTable load(String apkPath) throws ResTableParserException{
		final ResTable table = new ResTable();
		table.mResources = createResources(apkPath);
		ResTableParser.DefaultHandler h = new ResTableParser.DefaultHandler() {
			@Override
			public void resource(int id, int configOffset) {
				table.addSpec(id);
			}
		};
		ResTableParser p = new ResTableParser(h);
		p.parse(apkPath);
		return table;
	}
	
	public static Resources createResources(String apkPath){
		// do things ourselves so we get all resources (public and private)
		AssetManagerH amh = new AssetManagerH();
		amh.addAssetPath(apkPath);
		return new Resources(amh.assetManager, null, null);
	}
	
	private ResTable(){
		
	}
	
	private void addSpec(int id){
		int typeId = getTypeId(id);
		ResType type = mTypes.get(typeId);
		if(type == null){
			type = new ResType(this, typeId, getTypeName(id));
			mTypes.put(typeId, type);
			mTypesList = null;
		}
		type.addSpec(id);
	}
	
	public Resources getResources(){
		return mResources;
	}
	
	public List<ResType> getTypes(){
		if(mTypesList == null){
			mTypesList = Lists.newArrayList(mTypes.values());
			Collections.sort(mTypesList);
		}
		return mTypesList;
	}
	
	public String getTypeName(int resId){
		return mResources.getResourceTypeName(resId);
	}
	
	public String getEntryName(int id) {
		return mResources.getResourceEntryName(id);
	}
	
	public static int getTypeId(int resId){
		return ((resId >> 16) & 0xFF);
	}
}
 