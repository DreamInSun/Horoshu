package cyan.data.amoeba.schema;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 
 * @author DreamInSun
 *
 */
public class SchemaManager {
	/*========== Properties ==========*/
	private static Map<String, Schema> g_SchemaCache = new WeakHashMap<String, Schema>();

	/*==========  ==========*/
	public static Schema getSchema(String objName) {
		Schema tmpRet = null;
		tmpRet = g_SchemaCache.get(objName);
		if (tmpRet == null) {
			/* Parse Schema File and Format */

			/* Add to Cache */
			g_SchemaCache.put(objName, tmpRet);
		}
		return tmpRet;
	}
}
