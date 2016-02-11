package cyan.core.config;

/**
 * Created by DreamInSun on 2016/2/2.
 */
public interface IConfig {

    IConfig set(String key, Object obj);

    Integer getInt(String key);

    Integer getInt(String key, Integer defaultVal);

    Number getDouble(String key);

    Number getDouble(String key, Double defaultVal);

    String getString(String key);

    String getString(String key, String defaultVal);

    <T> T getObject(String key);

    <T> T getObject(String key, T defaultVal);
}
