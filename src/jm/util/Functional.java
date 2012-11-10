package jm.util;

public final class Functional {
	private Functional(){}
	
	public interface Predicate<T> {
		boolean test(T t);
	}
	
	public interface Mapping<T,U> {
		T map(U u);
	}
	
	public static <T> Predicate<T> negate(final Predicate<T> predicate){
		return new Predicate<T>() {
			public boolean test(T t){
				return !predicate.test(t);
			}
		};
	}
}
