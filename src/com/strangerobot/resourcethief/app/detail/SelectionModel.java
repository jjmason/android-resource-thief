package com.strangerobot.resourcethief.app.detail;

public interface SelectionModel<E> { 
	boolean isItemSelected(E item);
	void setItemSelected(E item, boolean selected);
}
