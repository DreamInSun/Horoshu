package ctop.v3.msb.usrm.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.proxy.data.ServiceEntry;

public class MsbSvcBulletin implements Externalizable {
	private static final long serialVersionUID = 1L;

	/*==========  ==========*/

	/*========== Constant ===========*/

	/*========== Properties ===========*/
	public UsrmNodeInfo m_UsrmNodeInfo;
	private Map<MsbUrn, ServiceEntry> m_ModifiedServices = new HashMap<MsbUrn, ServiceEntry>();

	/*========== Constructor ==========*/
	/** Only for deserialization */
	@Deprecated
	public MsbSvcBulletin() {
	}

	public MsbSvcBulletin(UsrmNodeInfo nodeInfo) {
		m_UsrmNodeInfo = nodeInfo;
	}

	/*=========  ==========*/
	public void addService(ServiceEntry svcEntry) {
		/*===== STEP 1. Eliminate Duplication =====*/
		if (m_ModifiedServices.containsKey(svcEntry.msbUrn)) {
			m_ModifiedServices.remove(svcEntry.msbUrn);
		}
		/*===== STEP 2. Put Modification Information =====*/
		svcEntry.setModifyType(ServiceEntry.EModifyType.REGISTERING);
		m_ModifiedServices.put(svcEntry.msbUrn, svcEntry);
	}

	public void deleteService(ServiceEntry svcEntry) {
		/*===== STEP 1. Eliminate Duplication =====*/
		if (m_ModifiedServices.containsKey(svcEntry.msbUrn)) {
			m_ModifiedServices.remove(svcEntry.msbUrn);
		}
		/*===== STEP 2. Put Modification Information =====*/
		svcEntry.setModifyType(ServiceEntry.EModifyType.UNREGISTERING);
		m_ModifiedServices.put(svcEntry.msbUrn, svcEntry);
	}

	public void clearDirty(MsbUrn msbUrn) {
		m_ModifiedServices.remove(msbUrn);
	}

	public int size() {
		return m_ModifiedServices.size();
	}

	public Map<MsbUrn, ServiceEntry> getModifiedServices() {
		return m_ModifiedServices;
	}

	/*========== Externalizable ==========*/
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(m_UsrmNodeInfo);
		out.writeInt(m_ModifiedServices.size());
		for (Entry<MsbUrn, ServiceEntry> mapEntry : m_ModifiedServices.entrySet()) {
			out.writeObject(mapEntry.getKey());
			out.writeObject(mapEntry.getValue());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.m_UsrmNodeInfo = (UsrmNodeInfo) in.readObject();
		int loop = in.readInt();
		for (int i = 0; i < loop; i++) {
			MsbUrn msbUrn = (MsbUrn) in.readObject();
			ServiceEntry svcEntry = (ServiceEntry) in.readObject();
			this.m_ModifiedServices.put(msbUrn, svcEntry);
		}
	}
}
