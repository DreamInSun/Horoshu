package cyan.svc.mngm.consul.vo;

import com.alibaba.fastjson.JSON;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by DreamInSun on 2016/2/11.
 */
public class Node implements Externalizable {

    /*========== Properties ==========*/
    public String Node;
    public String Address;

    /*========== Constructor ==========*/


    /*========== Interface : Externalizable ==========*/
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(Node);
        objectOutput.writeUTF(Address);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        Node = objectInput.readUTF();
        Address = objectInput.readUTF();
    }

    /*========== toString ==========*/
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
