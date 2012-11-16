package com.strangerobot.resourcethief.app.detail;

import com.google.common.base.Objects;
import com.strangerobot.resourcethief.res.ResSpec;

import android.graphics.drawable.Drawable;

public class ResourceListItem { 
	private Drawable mDrawable;
	private final ResSpec mSpec;
	private boolean mError;
	
	public ResourceListItem(ResSpec spec){
		this(spec, null);
	}
	
	public ResourceListItem(ResSpec spec, Drawable drawable){
		mSpec = spec;
		mDrawable = drawable;
	}
	
	public Drawable getDrawable() {
		return mDrawable;
	}
	
	public void setDrawable(Drawable drawable) {
		mDrawable = drawable;
	}
	
	public ResSpec getSpec() {
		return mSpec;
	}
	
	public boolean isError(){
		return mError;
	}
	
	public void setError(boolean error){
		mError = error;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("spec", mSpec)
				.add("drawable", mDrawable).toString();
	}
}
