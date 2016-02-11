package cyan.svc.mngm.consul.vo;

import com.alibaba.fastjson.JSON;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by DreamInSun on 2016/2/10.
 */
public class Service implements Externalizable {

    /*========== Variables ==========*/
    public String ID;
    public String Service;
    public String[] Tags;
    public String Address;
    public Integer Port;


    /*========== Interface : Externalizable ==========*/
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(ID);
        objectOutput.writeUTF(Service);
        objectOutput.writeInt(Tags.length);
        for (String tag : Tags) {
            objectOutput.writeUTF(tag);
        }
        objectOutput.writeUTF(Address);
        objectOutput.writeInt(Port);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        ID = objectInput.readUTF();
        Service = objectInput.readUTF();
        int tagLen = objectInput.readInt();
        Tags = new String[tagLen];
        for (int i = 0; i < tagLen; i++) {
            Tags[i] = objectInput.readUTF();
        }
        Address = objectInput.readUTF();
        Port = objectInput.readInt();
    }


    /*========== toString ==========*/
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
