package ctop.v3.msb.managment;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import ctop.v3.msb.common.urn.MsbUrn;
import ctop.v3.msb.msbu.mngr.access.AccessCtrlBrief;
import ctop.v3.msb.proxy.data.ServiceEntry;
import ctop.v3.msb.usrm.UsrmSvcMaster;

/**
 * Wrapper UsrmSvcMaster with Management APIs.
 * 
 * @author DreamInSun
 * 
 */
public class UsrmManager {

	/*========== Constant ==========*/

	/*========== Properties ==========*/
	private UsrmSvcMaster m_UsrmMaster;

	/*========== Constructor ==========*/
	public UsrmManager(UsrmSvcMaster svcMaster) {
		m_UsrmMaster = svcMaster;
	}

	/*==========  ==========*/
	public ServiceEntry[] getMsbUrnList() {
		ServiceEntry[] tmpRet = null;
		if (m_UsrmMaster != null) {
			tmpRet = new ServiceEntry[m_UsrmMaster.getServiceMap().size()];
			m_UsrmMaster.getServiceMap().values().toArray(tmpRet);
		}
		return tmpRet;
	}

	/*=============================================================*/
	/*==================== Services Management ====================*/
	/*=============================================================*/

	/*==========  ==========*/
	/**
	 * 
	 * @param jsonParam
	 *            null
	 * @return
	 */
	public String getSvcSummary(String jsonParam) {
		return JSON.toJSONString(m_UsrmMaster.getServiceMap().values(), SerializerFeature.DisableCircularReferenceDetect);
	}

	/**
	 * 
	 * @param jsonParam
	 *            strMsbUrn
	 * @return
	 */
	public String getSvcDetail(String jsonParam) {
		ServiceEntry svcEntry;
		try {
			svcEntry = m_UsrmMaster.getSvcEntry(MsbUrn.parse(jsonParam));
			return JSON.toJSONString(svcEntry, SerializerFeature.DisableCircularReferenceDetect);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*==============================================================*/
	/*==================== USRM Node Management ====================*/
	/*==============================================================*/

	/*========== getUsrmInfo ==========*/
	/**
	 * 
	 * @param jsonParam
	 *            nodeName
	 * @return
	 */
	public String getUsrmInfo(String jsonParam) {
		return JSON.toJSONString(m_UsrmMaster.getMsbuInfo(jsonParam), SerializerFeature.DisableCircularReferenceDetect);
	}

	/*==========  ==========*/
	public String getUsrmNodeList(String jsonParam) {
		return JSON.toJSONString(m_UsrmMaster.getUsrmNodeInfoList(), SerializerFeature.DisableCircularReferenceDetect);
	}

	/*==============================================================*/
	/*==================== USRM Access Control ====================*/
	/*==============================================================*/

	/*========== Access Control : getAccessControllist ==========*/
	public String getAccessControllist(String jsonParam) {
		return JSON.toJSONString(m_UsrmMaster.getAccessCtrlBriefs(), SerializerFeature.DisableCircularReferenceDetect);
	}

	/*==========  ==========*/

	public String getMsbuInfoList(String jsonParam) {
		return JSON.toJSONString(m_UsrmMaster.getMsbuInfoList(), SerializerFeature.DisableCircularReferenceDetect);
	}

	public String submitAccessCtrlBriefs(String jsonParam) {
		List<AccessCtrlBrief> acBriefList = JSON.parseArray(jsonParam, AccessCtrlBrief.class);
		boolean ret = m_UsrmMaster.updateAccessCtrlBirefs(acBriefList);
		return Boolean.toString(ret);
	}
}
