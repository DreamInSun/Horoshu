package ctop.v3.msb.proxy.mngr;

import javax.jms.Message;

import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.proxy.data.ServiceEntry;

/**
 * MSBU Service Manager keeps the service registered in local MSBU. <br />
 * All the service will be index by short SvcUrn. <br />
 * 
 * @author DreamInSun
 * 
 */
public interface IMsbuSvcManager {

	/*==========  ==========*/
	boolean bindService(ServiceEntry svcDesc);

	void unbindService(MsbUrn msbUrn);

	ServiceEntry getServiceEntry(MsbUrn msbUrn);

	ServiceEntry[] getSvcEntryList();

	String[] getServiceList();

	MsbUrn[] getServiceMsburnList();

	/*==========  ==========*/
	ComOutput invokePlatformService(String svcUrn, ComInput ci);

	ComOutput invokeRpcService(String svcUrn, ComInput ci);

	void invokeMsgService(String svcUrn, Message msg);
}
