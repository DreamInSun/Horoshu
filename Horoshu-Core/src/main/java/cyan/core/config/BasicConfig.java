package cyan.core.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DreamInSun on 2016/2/2.
 */
public class BasicConfig implements IConfig {
    /*========= Properties =========*/
    private Map<String, Object> m_configMap = new HashMap<>();

    /*========= Constructor =========*/
    public BasicConfig() {

    }

    /*========= Factory =========*/
    public static BasicConfig getEmptyConfig() {
        return new BasicConfig();
    }

    /*========= Interface : IConfig =========*/
    @Override
    public IConfig set(String key, Object obj) {
        m_configMap.put(key, obj);
        return this;
    }

    @Override
    public Integer getInt(String key) {
        return this.getInt(key, 0);
    }

    @Override
    public Integer getInt(String key, Integer defaultVal) {
        Object val = m_configMap.get(key);
        if (val != null) {
            return Integer.parseInt(val.toString());
        }
        return defaultVal;
    }

    @Override
    public Double getDouble(String key) {
        return getDouble(key, 0.0);
    }

    @Override
    public Double getDouble(String key, Double defaultVal) {
        Object val = m_configMap.get(key);
        if (val != null) {
            return Double.parseDouble(val.toString());
        }
        return defaultVal;
    }

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public String getString(String key, String defaultVal) {
        Object val = m_configMap.get(key);
        if (val != null) {
            return val.toString();
        }
        return defaultVal;
    }

    @Override
    public <T> T getObject(String key) {
        return getObject(key, null);
    }

    @Override
    public <T> T getObject(String key, T defaultVal) {
        Object val = m_configMap.get(key);
        if (val != null) {
            return (T) val;
        }
        return defaultVal;
    }


}
