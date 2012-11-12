package jm.util;

import java.util.Collection;
import java.util.Iterator;

public final class Collect {
	private Collect() {}

	public static int[] toIntArray(Collection<Integer> col){
		int[] ia = new int[col.size()];
		Iterator<Integer> it = col.iterator();
		for(int i=0;i<col.size();i++)
			ia[i] = it.next();
		return ia;
	}
}
