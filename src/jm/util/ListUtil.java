package jm.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jm.util.Functional.Predicate;

public final class ListUtil {
	private ListUtil(){}
	
	public static <T> List<T> filter(List<T> list, Predicate<T> predicate){
		ArrayList<T> filtered = new ArrayList<T>();
		for(T t : filter((Iterable<T>)list, predicate)){
			filtered.add(t);
		}
		return filtered;
	}
	
	public static <T> Iterable<T> filter(Iterable<T> iterable, Predicate<T> predicate){
		return new FilteredIterable<T>(iterable, predicate);
	}
	
	public static <T> Iterator<T> filter(Iterator<T> iterator, Predicate<T> test){
		return new FilteredIterator<T>(iterator, test);
	}
	
	public static <T> List<T> copy(List<T> from){
		if(from == null)
			return null;
		ArrayList<T> list = new ArrayList<T>(from);
		return list;
	}
	
	private static class FilteredIterator<T> implements Iterator<T> {
		private final Iterator<T> mSource;
		private final Predicate<T> mFilter;
		
		private boolean mAtNext;
		private T mNext;
		
		public FilteredIterator(Iterator<T> source, Predicate<T> filter){
			mSource = source;
			mFilter = filter;
		}
		
		private boolean findNext(){
			if(mAtNext)
				return true;
			T next;
			while(mSource.hasNext()){
				next = mSource.next();
				if(mFilter.test(next)){
					mAtNext = true;
					mNext = next;
					break;
				}
			}
			return mAtNext;
		}
		
		@Override
		public boolean hasNext() {
			return findNext();
		}

		@Override
		public T next() {
			if(findNext()){
				mAtNext = false;
				return mNext;
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static class FilteredIterable<T> implements Iterable<T> {
		private final Iterable<T> mBase;
		private final Predicate<T> mFilter;
		public FilteredIterable(Iterable<T> base, Predicate<T> filter){
			mBase = base;
			mFilter = filter;
		}
		
		@Override
		public Iterator<T> iterator() {
			return new FilteredIterator<T>(mBase.iterator(), mFilter);
		}
	}
	
	
}
