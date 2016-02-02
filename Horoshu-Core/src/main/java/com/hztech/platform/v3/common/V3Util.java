package com.hztech.platform.v3.common;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;

import com.hztech.platform.agent.agentw2m.ArgumentHolder;
import com.hztech.platform.agent.agentw2m.Invoker;
import com.hztech.platform.cache.XMLIDCache;
import com.hztech.platform.persistence.h.xml.XMLAgent;
import com.hztech.platform.v3.agent.s2b2o.URS;
import com.hztech.platform.v3.business.common.base.*;
import com.hztech.platform.v3.common.base.ResourceBase;
import com.hztech.util.*;
import com.hztech.util.vo.ResourceVO;

public class V3Util {
	/**
	 * ����OPJB�ķ���
	 * @param className
	 * @param methodName
	 * @param comInput
	 * @return
	 * @throws Exception
	 */
	public static ComOutput execOPJB(String className,String methodName,ComInput comInput) throws Exception{
		Object objInstance = Class.forName(className).newInstance();
		if (objInstance instanceof BLBase || objInstance instanceof BRBase)
			;
		else
			throw new Exception("�ࡾ" + className + "��������BLBase��BRBase�������࣡");
		if (Invoker.isMethod(objInstance, methodName))
			throw new Exception("�ࡾ" + className + "���ķ�����" + methodName + "�������ڣ�");
		ArgumentHolder ah = new ArgumentHolder();
		ah.setArgument(comInput);
		return (ComOutput) Invoker.dynaCall(objInstance, methodName, ah);
	}
	
	/**
	 * ����PJB�ķ���
	 * @param className
	 * @param methodName
	 * @param comInput
	 * @return
	 * @throws Exception
	 */
	public static ComOutput execPJB(String className,String methodName,ComInput comInput) throws Exception{
		return HZUtil.beanExecBean(className, methodName, comInput);
	}
	
	public static String restfullExecute(HttpServletRequest request, HttpServletResponse response){
		String resourceName = getResourceName(request);
		ComInput comInput = ComInput.getComInput2(request);
		ComOutput co = null;
		if("register".equalsIgnoreCase(resourceName)){
			co = ResourceAuth.register(comInput.getStrPm("app_id"), comInput.getStrPm("license_key"));
		}else if("unregister".equalsIgnoreCase(resourceName)){
			comInput.copyPm("session_key", ComInputConst.SESSION_KEY_PROPERTY);
			co = ResourceAuth.unregister(comInput.getStrPm(ComInputConst.SESSION_KEY_PROPERTY), comInput.getStrPm("license_key"));
		}else{//��Դ���ִ��
			try{
				ResourceBase rb = new ResourceBase();//���Կ���Դ�ำֵ
				co = URS.invoke(rb, resourceName, comInput);
			}catch(Exception e){
				co = RSUtil.setOutput(-1,e.getMessage());
			}
		}
		
		return RSUtil.xmlComOutput(co, request, "all");
	}
	
	/**
	 * get all Service resources at one supported platform
	 * @return ArrayList Object;array struct:the first is Resource Name,the second is Chinese Resource Name,the third is version(multi-version'split is "###")
	 */
	public static ArrayList<String[]> getAllServiceResource(){
		ArrayList<String[]> alRet = new ArrayList<String[]>();
		HZCacheBase<String,ResourceVO> hcbResource = XMLIDCache.getResourceCache();
		for(String key:hcbResource.getAlKeyType()){
			ResourceVO rvo = hcbResource.get(key);
			alRet.add(new String[]{key,rvo.getCnResourceName(),rvo.getVersions()});
		}
		return alRet;
	}
	
	/**
	 * get resource's detail information according to resource's name and resource's version
	 * @param resourceName
	 * @param version
	 * @return Element Object
	 */
	public static Element getDetailInfoByResourceName(String resourceName,String version){
		HZCacheBase<String,ResourceVO> hcbResource = XMLIDCache.getResourceCache();
		ResourceVO rvo = hcbResource.get(resourceName);
		return rvo.getElement(version);
	}
	
	/**
	 * get resource's help,reserved!
	 * @param request
	 * @param response
	 * @return
	 */
	public static String getResourceHelp(HttpServletRequest request, HttpServletResponse response){
		String resourceName = getResourceName(request);
		String strRet = null;
		StringBuffer sb = new StringBuffer(1024);
		if("register".equalsIgnoreCase(resourceName)){
			sb.append("<table border='1' style='width: 100%'>");
			sb.append("  <tr>");
			sb.append("    <td>������Դ����</td>");
			sb.append("    <td colspan='4'><span lang='en-us'>register</span></td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>����</td>");
			sb.append("    <td colspan='4'>�ڵ�����ϵͳ�״�ʹ�ÿ���ʽƽ̨ʱ��ע�����ע��ɹ�����<span lang='en-us'>session_key</span>�����򷵻ؿ�ֵ</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>�������ID</td>");
			sb.append("    <td>��������</td>");
			sb.append("    <td>�Ƿ��ⲿ��ֵ</td>");
			sb.append("    <td>����</td>");
			sb.append("    <td>Լ��</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>app_id</span></td>");
			sb.append("    <td>Ӧ�ó���<span lang='en-us'>ID</span></td>");
			sb.append("    <td>��</td>");
			sb.append("    <td>�ַ�����</td>");
			sb.append("    <td>��</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>license_key</span></td>");
			sb.append("    <td>Ӧ�ó�����Ȩ��</td>");
			sb.append("    <td>��</td>");
			sb.append("    <td>�ַ�����</td>");
			sb.append("    <td>��</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>����ֵ˵��</td>");
			sb.append("    <td colspan='4'>retvalueֵ���<span lang='en-us'>session_key</span></td>");
			sb.append("  </tr>");
			sb.append("</table>");
			strRet = sb.toString();
		}else if("unregister".equalsIgnoreCase(resourceName)){
			sb.append("<table border='1' style='width: 100%'>");
			sb.append("  <tr>");
			sb.append("    <td>������Դ����</td>");
			sb.append("    <td colspan='4'><span lang='en-us'>unregister</span></td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>����</td>");
			sb.append("    <td colspan='4'>�ڵ�����ϵͳ�˳�����ʽƽ̨ʱ��ע������ע���ɹ���retValue����0������Ϊ-1</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>�������ID</td>");
			sb.append("    <td>��������</td>");
			sb.append("    <td>�Ƿ��ⲿ��ֵ</td>");
			sb.append("    <td>����</td>");
			sb.append("    <td>Լ��</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>app_id</span></td>");
			sb.append("    <td>Ӧ�ó���<span lang='en-us'>ID</span></td>");
			sb.append("    <td>��</td>");
			sb.append("    <td>�ַ�����</td>");
			sb.append("    <td>��</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>license_key</span></td>");
			sb.append("    <td>Ӧ�ó�����Ȩ��</td>");
			sb.append("    <td>��</td>");
			sb.append("    <td>�ַ�����</td>");
			sb.append("    <td>��</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>����ֵ˵��</td>");
			sb.append("    <td colspan='4'>retvalueֵ���0����-1</td>");
			sb.append("  </tr>");
			sb.append("</table>");
			strRet = sb.toString();
		}else{//��Դ���ִ��
			ResourceVO rvo = XMLAgent.getResourceVO(resourceName);
			strRet = rvo.getHelp();
		}
		
		return strRet;
	}
	
	private static String getResourceName(HttpServletRequest request){
		String path = request.getPathInfo();
		if(path.endsWith("/")) path = path.substring(0, path.length()-1);
		String resourceName = path.replaceAll("[/]", ".");
		return resourceName;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
