package com.strangerobot.resourcethief.app.detail;

import com.google.common.util.concurrent.ListenableFuture;
import com.strangerobot.resourcethief.res.ResSpec;
import com.strangerobot.resourcethief.res.ResTable;

public interface ResourceListParent {
	ListenableFuture<ResTable> getResTable();
	boolean isSpecSelected(ResSpec spec);
	void setSpecSelected(ResSpec spec, boolean selected);
	
}
