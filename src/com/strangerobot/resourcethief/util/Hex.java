package com.strangerobot.resourcethief.util;

public final class Hex {
	private Hex(){}
	public static String hex(int i){
		return String.format("0x%08X", i);
	}
	public static String hex(short s){
		return String.format("0x%04X",s);
	}
	public static String hex(byte b){
		return String.format("0x%02X", b);
	}
}
