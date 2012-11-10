package jm.rt.util;

import java.util.Collection;

public class Preconditions {
	public static <T> T checkNotNull(T obj){
		if(obj == null)
			throw new NullPointerException();
		return obj;
	}
	public static <T extends CharSequence> T checkNotBlank(T s){
		if(s == null)
			throw new NullPointerException();
		if(s.length() == 0)
			throw new IllegalArgumentException();
		return s;
	}
	public static <T> T checkArgument(boolean p, T arg){
		if(!p)
			throw new IllegalArgumentException();
		return arg;
	}
	public static <T extends Collection<?>> T checkNotEmpty(T collection){
		if(collection == null)
			throw new NullPointerException();
		if(collection.isEmpty())
			throw new IllegalArgumentException();
		return collection;
	}
}
