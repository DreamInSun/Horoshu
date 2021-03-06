package cyan.svc.horoshu.svcmngr;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Service Bulletin is a List of SvrInfoDescriptor. <br />
 * This POJO aim to quick share services in the MSB.
 *
 * @author DreamInSun
 */
public final class SvcEntry implements Externalizable {
    private static final long serialVersionUID = 1L;

    /*========== Constant ==========*/
    public enum EModifyType {
        NONE, REGISTERING, UNREGISTERING, SERVICE_VALID, SERVICE_INVALID,
    }

    public enum EType {
        PLATFORM, RPC, MSG, UNKNOWN
    }

    /*========== Services Meta Information ==========*/
    public MsbUrn msbUrn;

	/*========== Service Detail Information ==========*/
    /**
     * Store the XML File
     */
    private SvcDesc m_SvcDesc;

    /* Life Information */
    public long tsModified;
    public long tsUpdate;

    /* Authentication Information */
    public boolean domainVisibale = false;

	/*========== Service Handler ==========*/
    /** */
    private EType m_SvcType;
    /** */
    private IMsbRpcHandler m_RpcHandler = null;
    /** */
    private IMsbMsgHandler m_MsgHandler = null;

    /*==========  =========*/
    private EModifyType m_ModifyType = EModifyType.NONE;

	/*==========  =========*/

    /**
     * Create RPC Service Descriptor.
     *
     * @param msbUrn
     * @param handler
     * @param desc
     * @return
     */
    public static ctop.v3.msb.proxy.data.SvcEntry createRpcSvcEntry(MsbUrn msbUrn, IMsbRpcHandler handler, SvcDescriptor desc) {
        ctop.v3.msb.proxy.data.SvcEntry tmpSvcEntry = new ctop.v3.msb.proxy.data.SvcEntry();
        tmpSvcEntry.msbUrn = msbUrn;
        tmpSvcEntry.m_SvcType = EType.RPC;
        tmpSvcEntry.m_RpcHandler = handler;
        tmpSvcEntry.m_SvcDesc = desc;
        return tmpSvcEntry;
    }

	/*==========  =========*/

    /**
     * Create Message Service Descriptor.
     *
     * @param msbUrn
     * @param handler
     * @param desc
     * @return
     */
    public static ctop.v3.msb.proxy.data.SvcEntry createMsgSvcEntry(MsbUrn msbUrn, IMsbMsgHandler handler, SvcDescriptor desc) {
        ctop.v3.msb.proxy.data.SvcEntry tmpSvcEntry = new ctop.v3.msb.proxy.data.SvcEntry();
        tmpSvcEntry.msbUrn = msbUrn;
        tmpSvcEntry.m_SvcType = EType.MSG;
        tmpSvcEntry.m_MsgHandler = handler;
        tmpSvcEntry.m_SvcDesc = desc;
        return tmpSvcEntry;
    }

    /*==========  =========*/
    public static SvcEntry createPlatformSvcEntry(MsbUrn msbUrn, SvcDescriptor desc) {
        SvcEntry tmpSvcEntry = new ctop.v3.msb.proxy.data.SvcEntry();
        tmpSvcEntry.msbUrn = msbUrn;
        tmpSvcEntry.m_SvcType = EType.PLATFORM;
        tmpSvcEntry.m_SvcDesc = (SvcDesc) desc;
        return tmpSvcEntry;
    }

	/*========== Constructor ==========*/

    /**
     * Only for deserialization
     */
    @Deprecated
    public SvcEntry() {
    }

    /*========== Getter & Setter ==========*/
    public EType getType() {
        return m_SvcType;
    }

    public SvcDesc getDetail() {
        return m_SvcDesc;
    }

    public EModifyType getModifyType() {
        return m_ModifyType;
    }

    public void setModifyType(EModifyType m_ModeType) {
        this.m_ModifyType = m_ModeType;
    }

    /*========== Interface : Externalizable ===========*/
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(msbUrn);
        out.writeUTF(m_SvcType.name());
        out.writeObject(m_SvcDesc);
        out.writeUTF(m_ModifyType.name());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        //msbUrn =  in.readObject();
        m_SvcType = EType.valueOf(in.readUTF());
        m_SvcDesc = (SvcDesc) in.readObject();
        m_ModifyType = EModifyType.valueOf(in.readUTF());

    }
}
