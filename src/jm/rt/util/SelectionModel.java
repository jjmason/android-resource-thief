package jm.rt.util;

public interface SelectionModel<T> {
	public boolean isSelected(T item);
	public boolean setSelected(T item, boolean selected);
}
