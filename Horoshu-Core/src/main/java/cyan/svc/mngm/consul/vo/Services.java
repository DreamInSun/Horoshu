package cyan.svc.mngm.consul.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by DreamInSun on 2016/2/11.
 */
public class Services extends HashMap<String, Set<String>> {

    /*========== Constructor ==========*/
    public Services() {

    }

    public Services(JSONObject jsonObj) {
        for (String key : jsonObj.keySet()) {
            JSONArray valArr = jsonObj.getJSONArray(key);
            Set<String> tags = new HashSet<>();
            for (Object val : valArr) {
                tags.add(String.valueOf(val));
            }
            this.put(key, tags);
        }
    }

    /*========== toString ==========*/
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
