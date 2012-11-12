package jm.util;

public final class Flags {
	private Flags(){}
	
	public static int or(int...masks){
		int r = 0;
		for(int mask : masks) r |= mask;
		return r;
	}
	
	public static boolean has(int mask, int flag){
		return 0 != (mask & flag);
	}
	
	public static int add(int mask, int addFlags){
		return mask | addFlags;
	}
	
	public static int del(int mask, int delFlags){
		return mask & ~delFlags;
	}
	
	public static int set(int mask, int changeFlags, boolean set){
		return set ? add(mask, changeFlags) : del(mask, changeFlags);
	}
}
