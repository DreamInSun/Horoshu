package ctop.v3.msb.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class HashSetMap<K, V> extends HashMap<K, Set<V>> {
	private static final long serialVersionUID = 1L;

	/*========== Constructor ==========*/

	/*========== Interface Map ==========*/
	public Set<V> putValue(K key, V value) {
		Set<V> values = super.get(key);
		if (values == null) {
			values = new HashSet<V>();
			super.put(key, values);
		}
		values.add(value);
		return values;
	}

	/* ========== ==========*/
	public Set<V> remove(K key, V value) {
		Set<V> values = super.get(key);
		if (values != null) {
			Iterator<V> it = values.iterator();
			while (it.hasNext()) {
				if (value.equals(it.next())) {
					it.remove();
				}
			}
		}
		return values;
	}
}
