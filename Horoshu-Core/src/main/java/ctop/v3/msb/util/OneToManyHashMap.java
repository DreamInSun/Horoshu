package ctop.v3.msb.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OneToManyHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = 1L;

	/*========== Constructor ==========*/

	/*========== Interface Map ==========*/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public V put(K key, V value) {
		if (containsKey(key)) {
			Set values = null;
			if (super.get(key) == null) {
				values = new HashSet();
				values.add(value);
			} else {
				if (super.get(key) instanceof Set) {
					/* If Has Many Values, Cast to Array */
					values = (Set) super.get(key);
					values.add(value);
				} else {
					values = new HashSet();
					/* If has one value, Cast to Array */
					values.add(super.get(key));
					values.add(value);
				}
			}
			return super.put(key, (V) values);
		} else {
			return super.put(key, value);
		}
	}

	/* ========== ==========*/
	@SuppressWarnings("rawtypes")
	public V remove(K key, V value) {
		if (containsKey(key)) {
			V valueSet = super.get(key);
			if (valueSet instanceof Set) {
				Set values = (Set) valueSet;
				Iterator it = values.iterator();
				while (it.hasNext()) {
					if (value.equals(it.next())) {
						it.remove();
					}
				}
			} else {
				super.remove(key);
			}

		}
		return super.remove(key);
	}
}
