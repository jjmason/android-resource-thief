package jm.rt.activity;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;

public class SelectionModel implements Iterable<Integer>, Parcelable {
	public interface Observer {
		void onSelectionChanged(int id, boolean isSelected);
	}

	private final Set<Observer> mObservers = new LinkedHashSet<SelectionModel.Observer>();

	private final Set<Integer> mSelection = new LinkedHashSet<Integer>();

	public void addObserver(Observer observer) {
		mObservers.add(observer);
	}

	public void removeObserver(Observer observer) {
		mObservers.remove(observer);
	}

	public boolean setSelected(int id, boolean selected) {
		return setSelected(id, selected, true);
	}

	public boolean setSelected(int id, boolean selected,
			boolean notifyObservers) {
		boolean changed;
		if (selected) {
			changed = mSelection.add(id);
		} else {
			changed = mSelection.remove(id);
		}
		if (changed && notifyObservers) {
			for (Observer obs : mObservers) {
				obs.onSelectionChanged(id, selected);
			}
		}
		return changed;
	}
	
	public boolean setSelected(int id, boolean selected, Observer sourceObserver){
		boolean changed;
		if (selected) {
			changed = mSelection.add(id);
		} else {
			changed = mSelection.remove(id);
		}
		if (changed) {
			for (Observer obs : mObservers) {
				if(!obs.equals(sourceObserver)){
					obs.onSelectionChanged(id, selected);
				}
			}
		}
		return changed;
	}

	public boolean isSelected(int id) {
		return mSelection.contains(id);
	}

	public int count() {
		return mSelection.size();
	}

	public boolean isEmpty() {
		return count() != 0;
	}

	public Collection<Integer> getSelection() {
		return Collections.unmodifiableSet(mSelection);
	}

	public Iterator<Integer> iterator() {
		return getSelection().iterator();
	}

	public int[] toIntArray(){
		int[] r = new int[count()];
		int i = 0;
		for(int id : mSelection)
			r[i++] = id;
		return r;
	}
	
	@Override
	public int describeContents() { 
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeIntArray(toIntArray());
	}
	
	public static Parcelable.Creator<SelectionModel> CREATOR = new Parcelable.Creator<SelectionModel>() {
		public SelectionModel createFromParcel(Parcel source) {
			SelectionModel sm = new SelectionModel();
			int[] ids = source.createIntArray();
			for(int id : ids){
				sm.setSelected(id, true, false);
			}
			return sm;
		}
		public SelectionModel[] newArray(int size) {
			return new SelectionModel[size];
		}
	};
}
