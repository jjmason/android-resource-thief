package jm.util;

public final class StringUtil {
	private StringUtil(){}
	
	public static boolean isUpperCase(CharSequence string){
		for(int i=0;i<string.length();i++){
			if(!Character.isUpperCase(string.charAt(i)))
				return false;
		}
		return true;
	}
}
