package jm.util;

import java.util.Collection;
/**
 * Argument checking helpers.
 * 
 * @author jon mason
 */
public final class Args {
	private Args(){}
	
	public static int positive(int i){
		return positive(i, "argument");
	}
	
	public static int positive(int i, String name){
		return check(i > 0, i, "%s must be positive", name);
	}
	
	public static int nonNegative(int i){
		return nonNegative(i,"argument");
	}
	
	public static int nonNegative(int i, String name){
		return check(i >= 0, i, "%s must be non negative", name);
	}
	
	public static <T extends Collection<?>> T notEmpty(T collection){
		return notEmpty(collection, "argument");
	}
	
	public static <T extends Collection<?>> T notEmpty(T collection, String name){
		return check(notNull(collection, name).size() != 0, collection, "%s must not be empty", name);
	}
	
	public static <T extends CharSequence> T notEmpty(T string){
		return notEmpty(string, "argument");
	}
	
	public static <T extends CharSequence> T notEmpty(T string, String name){
		return check(notNull(string, name).length() != 0, string, "%s must not be empty", name);
	}
	
	public static <T> T notNull(T arg, String argname){
		return check(arg != null, arg, "%s must not be null", argname);
	}
	
	public static <T> T notNull(T arg){
		return notNull(arg, "argument");
	}
	
	public static <T> T check(boolean ok, T arg, String message, Object...messageArgs){
		if(!ok){
			throw new IllegalArgumentException(String.format(message, messageArgs));
		}
		return arg;
	}

	public static int notZero(int value) {
		if(value == 0)
			throw new IllegalArgumentException("argument must be non-zero");
		return value;
	}
}
