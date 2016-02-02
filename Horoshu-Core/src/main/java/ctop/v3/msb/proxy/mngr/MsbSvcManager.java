package ctop.v3.msb.proxy.mngr;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;

import com.hztech.platform.v3.agent.s2b2o.URS;
import com.hztech.platform.v3.common.base.ResourceBase;
import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;
import com.hztech.util.RSUtil;

import ctop.v3.msb.MSB.IMsbMsgHandler;
import ctop.v3.msb.MSB.IMsbRpcHandler;
import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.proxy.data.ServiceEntry;

/**
 * Service Manager responsibility to maintain local service registering.
 * 
 * @author DreamInSun
 * 
 * @param <TProxyID>
 */
public class MsbSvcManager implements IMsbuSvcManager {

	/* ========== Constant ========== */

	/* ========== Properties ========== */
	/* Service Map : Key SvcUrn */
	private Map<String, ServiceEntry> m_ServiceMap = new HashMap<String, ServiceEntry>();

	static ResourceBase g_dummyResourceBase = new ResourceBase();

	/* ========== Constructor ========== */
	public MsbSvcManager() {
	}

	/* ===================================================================== */
	/* ==================== Interface : IMsbSvcManager ===================== */
	/* ===================================================================== */

	/* ========== IMsbSvcManager : bindMsgService ========== */
	@Override
	public boolean bindService(ServiceEntry svcDesc) {
		/* ===== ===== */
		// TODO Validate SercvieDescriptor
		/* ===== Add to Map ===== */
		m_ServiceMap.put(svcDesc.msbUrn.svcName, svcDesc);
		return true;
	}

	/* ========== IMsbSvcManager : unbindMsgService ========== */
	@Override
	public void unbindService(MsbUrn msbUrn) {
		m_ServiceMap.remove(msbUrn.svcName);
	}

	/* ========== IMsbSvcManager : getServiceEntry ========== */
	@Override
	public ServiceEntry getServiceEntry(MsbUrn msbUrn) {
		return m_ServiceMap.get(msbUrn.svcName);
	}

	@Override
	public ServiceEntry[] getSvcEntryList() {
		ServiceEntry[] tmpSvcEntryList = new ServiceEntry[m_ServiceMap.size()];
		m_ServiceMap.values().toArray(tmpSvcEntryList);
		return tmpSvcEntryList;
	}

	/* ========== IMsbSvcManager : getServiceList ========== */
	@Override
	public String[] getServiceList() {
		String[] tmpRet = new String[m_ServiceMap.size()];
		synchronized (m_ServiceMap) {
			m_ServiceMap.keySet().toArray(tmpRet);
		}
		return tmpRet;
	}

	@Override
	public MsbUrn[] getServiceMsburnList() {
		MsbUrn[] tmpRet = new MsbUrn[m_ServiceMap.size()];
		int i = 0;
		for (ServiceEntry svcEntry : m_ServiceMap.values()) {
			tmpRet[i++] = svcEntry.msbUrn;
		}
		return tmpRet;
	}

	/* ========== IMsbSvcManager : invokePlatformService ========== */
	@Override
	public ComOutput invokePlatformService(String svcUrn, ComInput ci) {
		ComOutput co = null;
		try {
			co = (ComOutput) URS.invoke(g_dummyResourceBase, svcUrn, ci);
		} catch (Exception exp) {
			co = (ComOutput) RSUtil.setOutput(-1, exp.getMessage());
		}
		return co;
	}

	/* ========== IMsbSvcManager : invokeRpcService ========== */
	@Override
	public ComOutput invokeRpcService(String svcUrn, ComInput ci) {
		IMsbRpcHandler svcHandler = m_ServiceMap.get(svcUrn).getRpcHandler();
		if (svcHandler != null) {
			return svcHandler.onInvokeSvr(ci);
		}
		return null;
	}

	/* ========== IMsbSvcManager : invokeMsgService ========== */
	@Override
	public void invokeMsgService(String svcUrn, Message msg) {
		IMsbMsgHandler svcHandler = m_ServiceMap.get(svcUrn).getMsgHandler();
		if (svcHandler != null) {
			svcHandler.onReceiveMessage(msg);
		}
	}

}
