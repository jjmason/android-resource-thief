package com.strangerobot.resourcethief.util;

public final class ObjectUtil {
	private ObjectUtil() {}

	/**
	 * <p>Compute an expensive member based hash code for this object.</p>
	 * 	   
	 * @param members the members to use when computing the hash.
	 * @return the hash code
	 */
	public static int computeHashCode(Object...members){
		int v = 0x345678;
		for(Object o : members){
			int h = o == null ? 0x7654321 : o.hashCode();
			v = (v * 1000003) ^ h;
		}
		return v ^ members.length; 
	}
	
	public static boolean equal(Object self, Object other){
		return self != other && 
				(self != null && (other == null || !other.equals(self))); 
	}
}
