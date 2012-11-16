package com.strangerobot.resourcethief.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.res.AssetManager;

public class HiddenApis {
	static <T> Constructor<T> getConstructor(Class<T> klass, Class<?>...parameterTypes){
		try {
			Constructor<T> ctor = klass.getConstructor(parameterTypes);
			ctor.setAccessible(true);
			return ctor;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	static Method getMethod(Class<?> klass, String name, Class<?> parameterTypes){
		try {
			Method m = klass.getMethod(name, parameterTypes);
			m.setAccessible(true);
			return m;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	static Object invoke(Method method, Object receiver, Object...args){
		try {
			return method.invoke(receiver, args);
		}catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	static <T> T construct(Constructor<T> ctor, Object...args){
		try {
			return ctor.newInstance(args);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static final class AssetManagerH {
		private static final Constructor<AssetManager> sCtor =
				getConstructor(AssetManager.class);
		private static final Method sAddAssetPath = 
				getMethod(AssetManager.class, "addAssetPath", String.class);
		
		public final AssetManager assetManager;
		public AssetManagerH(){
			assetManager = construct(sCtor);
		}
		public int addAssetPath(String path){
			return ((Integer) invoke(sAddAssetPath, assetManager, path)).intValue();
		}
	}
} 
