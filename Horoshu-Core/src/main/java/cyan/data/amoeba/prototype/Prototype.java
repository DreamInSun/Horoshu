package cyan.data.amoeba.prototype;

public class Prototype {

	/*========== Constant ==========*/
	/**  */
	static final int PROP_DATA_TYPE_NULL = -1;
	/** */
	static final int PROP_DATA_TYPE_OBJ = 0;
	/** Has Child */
	static final int PROP_DATA_TYPE_INT = 1;
	/** */
	static final int PROP_DATA_TYPE_BOOL = 2;
	/** */
	static final int PROP_DATA_TYPE_STRING = 3;
	/*========== Properties ==========*/
	public String schemaID;
	public String className;

	/*========== Inner Class ===========*/
	class Property {
		int data_type;
		String name;
		String key;
	}

}
