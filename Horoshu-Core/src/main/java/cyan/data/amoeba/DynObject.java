package cyan.data.amoeba;


import cyan.data.amoeba.prototype.Prototype;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

public class DynObject extends HashMap<String, Object> {
	private static final long serialVersionUID = 258462218348312988L;

	/*==========  ==========*/
	/** Class Template - Maybe JSON-scheme */
	public Prototype prototype;

	/*==========  ==========*/
	public void setBoolean(String key, Boolean value) {
		this.put(key, value);
	}

	public Boolean getBoolean(String key) {
		return (Boolean) this.get(key);
	}

	public void setByte(String key, Byte value) {
		this.put(key, value);
	}

	public Byte getByte(String key) {
		return (Byte) this.get(key);
	}

	public void setChar(String key, Character value) {
		this.put(key, value);
	}

	public Character getChar(String key) {
		return (Character) this.get(key);
	}

	public void setShort(String key, Short value) {
		this.put(key, value);
	}

	public Short getShort(String key) {
		return (Short) this.get(key);
	}

	public void setInteger(String key, Integer value) {
		this.put(key, value);
	}

	public Integer getInteger(String key) {
		return (Integer) this.get(key);
	}

	public void setLong(String key, Long value) {
		this.put(key, value);
	}

	public Long getLong(String key) {
		return (Long) this.get(key);
	}

	public void setFloat(String key, Float value) {
		this.put(key, value);
	}

	public Float getFloat(String key) {
		return (Float) this.get(key);
	}

	public void setDouble(String key, Double value) {
		this.put(key, value);
	}

	public Double getDouble(String key) {
		return (Double) this.get(key);
	}

	public void setString(String key, String value) {
		this.put(key, value);
	}

	public String getString(String key) {
		return (String) this.get(key);
	}

	/*==========  ==========*/
	public Object extractObject(String className) {
		Object tmpObj = null;
		/* Validate Input Prototype Schema */

		/* Get Class Name */

		/* Reflection assemble Object */
		try {
			/* */
			Class<?> protoClass = Class.forName(className);
			/* Create Destination Object */
			tmpObj = protoClass.newInstance();
			/*===== STEP 2. Fill properties with public fields =====*/
			Field[] fields = protoClass.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				Type fieldType = field.getType();
				Object value = null;
				/* 根据字段类型决定结果集中使用哪种get方法从数据中取到数据 */
				if (fieldType.equals(String.class)) {
					value = this.getString(fieldName);
					field.set(tmpObj, value);
				} else if (fieldType.equals(Integer.class)) {
					value = this.getInteger(fieldName);
				} else if (fieldType.equals(Long.class)) {
					value = this.getLong(fieldName);
				} else if (fieldType.equals(Boolean.class)) {
					value = this.getBoolean(fieldName);
				} else {
					// Recursive Object if Necessary
				}

			}
			/*===== STEP 3. Fill properties with setters =====*/
			Method[] methods = protoClass.getMethods();
			//protoClass.
			for (Method method : methods) {
				String methodName = method.getName();

				if (methodName.startsWith("set")) {
					//Type[] paramType = method.getGenericParameterTypes();
					String propertyName = methodName.substring(4);
					/* Get Type Info */

					/* Get Value From Map */

					/* Set Value via Setter */
					method.invoke(methodName, this.get(propertyName));
				}
			}
			/* */

			/* Fill all Public Properties */

			/* Return Object */
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return tmpObj;
	}
}
