package com.strangerobot.resourcethief.util;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultMap<K,V> implements Map<K, V> {
	public interface Factory<K,V> {
		public V create(K key);
	}
	
	private static class ClassFactory<K,V> implements Factory<K,V> {
		private final Constructor<? extends V> mConstructor;
		
		public ClassFactory(Class<?> klass){
			try{
				mConstructor = (Constructor<? extends V>) klass.getConstructor();
			}catch(NoSuchMethodException e){
				throw new IllegalArgumentException("class " + klass + 
						" does not have a no argument constructor");
			}
		}
		
		public V create(K key){
			try {
				return mConstructor.newInstance();
			} catch (Exception e){
				throw new InstantiationError("unable to create class with " + mConstructor);
			}
		}
	}
	
	private final Map<K, V> mMap;
	
	private final Factory<K, V> mFactory;
	
	public void clear() {
		mMap.clear();
	}

	public boolean containsKey(Object key) {
		return mMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return mMap.containsValue(value);
	}

	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return mMap.entrySet();
	}

	public boolean equals(Object object) {
		return mMap.equals(object);
	}

	@SuppressWarnings("unchecked")
	public V get(Object key) {
		V value = mMap.get(key);
		if(value == null){
			value = mFactory.create((K)key);
			put((K)key, value);
		}
		return value;
	}

	public int hashCode() {
		return mMap.hashCode();
	}

	public boolean isEmpty() {
		return mMap.isEmpty();
	}

	public Set<K> keySet() {
		return mMap.keySet();
	}

	public V put(K key, V value) {
		if(value == null)
			throw new NullPointerException("DefaultMap cannot contain null values");
		return mMap.put(key, value);
	}

	public void putAll(Map<? extends K, ? extends V> map) {
		if(map.containsValue(null)){
			throw new NullPointerException("DefaultMap cannot contain null values");
		}
		mMap.putAll(map);
	}

	public V remove(Object key) {
		return mMap.remove(key);
	}

	public int size() {
		return mMap.size();
	}

	public Collection<V> values() {
		return mMap.values();
	}

	public DefaultMap(Map<K, V> map, Factory<K, V> factory) {
		mMap = Args.notNull(map);
		mFactory = Args.notNull(factory);
	}
	
	public DefaultMap(Factory<K, V> factory){
		this(new HashMap<K, V>(), factory);
	}
	
	public DefaultMap(Class<?> klass){
		this(new ClassFactory<K, V>(klass));
	}
}
