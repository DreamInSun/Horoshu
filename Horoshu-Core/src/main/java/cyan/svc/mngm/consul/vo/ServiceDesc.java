package cyan.svc.mngm.consul.vo;

import com.alibaba.fastjson.JSON;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Consul的Service描述
 * {
 * "Node": "foobar",
 * "Address": "10.1.10.12",
 * "ServiceID": "redis",
 * "ServiceName": "redis",
 * "ServiceTags": null,
 * "ServiceAddress": "",
 * "ServicePort": 8000
 * }
 * <p/>
 * Created by DreamInSun on 2016/2/10.
 */
public class ServiceDesc implements Externalizable {
    /*========== Variables ==========*/
    /**
     * Consul Node ID
     */
    public String Node;
    /**
     * Address of Consul Node
     */
    public String Address;
    /**
     * ${HostName} :${ContainerName}:${Port}
     */
    public String ServiceID;
    /**
     * $SERVICE_NAME or ${ImageName} - ${InternalPort}
     */
    public String ServiceName;
    public String[] ServiceTags;
    /**
     * $SERVICE_IP or Service Host IP
     */
    public String ServiceAddress;
    /**
     * Real Service Port
     */
    public Integer ServicePort;

    /*========== Interface : Externalizable ==========*/
    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeUTF(Node);
        objectOutput.writeUTF(Address);
        objectOutput.writeUTF(ServiceID);
        objectOutput.writeUTF(ServiceName);
        objectOutput.writeInt(ServiceTags.length);
        for (String tag : ServiceTags) {
            objectOutput.writeUTF(tag);
        }
        objectOutput.writeUTF(ServiceAddress);
        objectOutput.writeInt(ServicePort);
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        Node = objectInput.readUTF();
        Address = objectInput.readUTF();
        ServiceID = objectInput.readUTF();
        ServiceName = objectInput.readUTF();
        int tagLen = objectInput.readInt();
        ServiceTags = new String[tagLen];
        for (int i = 0; i < tagLen; i++) {
            ServiceTags[i] = objectInput.readUTF();
        }
        ServiceAddress = objectInput.readUTF();
        ServicePort = objectInput.readInt();
    }

    /*========== toString ==========*/
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
