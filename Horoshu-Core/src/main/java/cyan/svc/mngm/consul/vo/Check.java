package cyan.svc.mngm.consul.vo;

import com.alibaba.fastjson.JSON;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by DreamInSun on 2016/2/10.
 */
public class Check implements Externalizable {

    /*========== Properties ==========*/
    public String Node;
    public String CheckID;
    public String Name;
    public String Notes;
    public String Status;
    public String ServiceID;


    /*========== Interface : Externalizable ==========*/
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(Node);
        objectOutput.writeUTF(CheckID);
        objectOutput.writeUTF(Name);
        objectOutput.writeUTF(Notes);
        objectOutput.writeUTF(Status);
        objectOutput.writeUTF(ServiceID);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        Node = objectInput.readUTF();
        CheckID = objectInput.readUTF();
        Name = objectInput.readUTF();
        Notes = objectInput.readUTF();
        Status = objectInput.readUTF();
        ServiceID = objectInput.readUTF();
    }

    /*========== toString ==========*/
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
