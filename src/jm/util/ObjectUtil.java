package jm.util;

public final class ObjectUtil {
	private ObjectUtil(){}
	
	public static boolean equal(Object a, Object b){
		return a == b || 
				(a != null && a.equals(b));
	}
	
	public static boolean notEqual(Object a, Object b){
		return a != b && 
				(a == null || !a.equals(b));
	}
}
