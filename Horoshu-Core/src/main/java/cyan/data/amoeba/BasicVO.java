package cyan.data.amoeba;

import com.alibaba.fastjson.JSON;

/**
 * Created by DreamInSun on 2016/2/11.
 */
public class BasicVO {
    /*========== toString ==========*/
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
