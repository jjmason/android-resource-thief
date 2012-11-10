package jm.rt.util;

import android.os.Bundle;
import android.util.SparseArray;

public final class Util {
	private Util() {
	}

	public static void saveSparseStringArray(SparseArray<String> array,
			String prefix, Bundle bundle) {
		String[] values = new String[array.size()];
		int[] keys = new int[array.size()];
		for (int i = 0; i < array.size(); i++) {
			values[i] = array.valueAt(i);
			keys[i] = array.keyAt(i);
		}
		bundle.putIntArray(prefix + "_keys", keys);
		bundle.putStringArray(prefix + "_values", values);
	}

	public static SparseArray<String> restoreSparseStringArray(String prefix,
			Bundle bundle) {
		int[] keys = bundle.getIntArray(prefix + "_keys");
		String[] values = bundle.getStringArray(prefix + "_values");
		SparseArray<String> result = new SparseArray<String>(2 * keys.length);
		for (int i = 0; i < keys.length; i++) {
			result.put(keys[i], values[i]);
		}
		return result;
	}
}
