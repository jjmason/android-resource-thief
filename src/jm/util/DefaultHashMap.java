package jm.util;

import java.util.HashMap;

public class DefaultHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 1L;
	
	public interface Factory<V> {
		V create(Object key);
	}
	
	private final Factory<V> mFactory;
	
	public DefaultHashMap(Factory<V> factory){
		mFactory = Args.notNull(factory);
	}
	
	@SuppressWarnings("unchecked")
	public V get(Object key, boolean create){
		V v = get(key);
		if(v == null && create){
			v = mFactory.create(key);
			put((K)key, v);
		}
		return v;
	}
	
	@Override
	public V get(Object key) {
		return get(key, true);
	}
}
