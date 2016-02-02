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
	 * 调用OPJB的方法
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
			throw new Exception("类【" + className + "】必须是BLBase或BRBase的派生类！");
		if (Invoker.isMethod(objInstance, methodName))
			throw new Exception("类【" + className + "】的方法【" + methodName + "】不存在！");
		ArgumentHolder ah = new ArgumentHolder();
		ah.setArgument(comInput);
		return (ComOutput) Invoker.dynaCall(objInstance, methodName, ah);
	}
	
	/**
	 * 调用PJB的方法
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
		}else{//资源类的执行
			try{
				ResourceBase rb = new ResourceBase();//暂以空资源类赋值
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
			sb.append("    <td>服务资源名称</td>");
			sb.append("    <td colspan='4'><span lang='en-us'>register</span></td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>描述</td>");
			sb.append("    <td colspan='4'>在第三方系统首次使用开放式平台时的注册服务，注册成功返回<span lang='en-us'>session_key</span>，否则返回空值</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>输入参数ID</td>");
			sb.append("    <td>中文名称</td>");
			sb.append("    <td>是否外部赋值</td>");
			sb.append("    <td>类型</td>");
			sb.append("    <td>约束</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>app_id</span></td>");
			sb.append("    <td>应用程序<span lang='en-us'>ID</span></td>");
			sb.append("    <td>是</td>");
			sb.append("    <td>字符串型</td>");
			sb.append("    <td>无</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>license_key</span></td>");
			sb.append("    <td>应用程序授权码</td>");
			sb.append("    <td>是</td>");
			sb.append("    <td>字符串型</td>");
			sb.append("    <td>无</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>返回值说明</td>");
			sb.append("    <td colspan='4'>retvalue值存放<span lang='en-us'>session_key</span></td>");
			sb.append("  </tr>");
			sb.append("</table>");
			strRet = sb.toString();
		}else if("unregister".equalsIgnoreCase(resourceName)){
			sb.append("<table border='1' style='width: 100%'>");
			sb.append("  <tr>");
			sb.append("    <td>服务资源名称</td>");
			sb.append("    <td colspan='4'><span lang='en-us'>unregister</span></td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>描述</td>");
			sb.append("    <td colspan='4'>在第三方系统退出开放式平台时的注销服务，注销成功后retValue等于0，否则为-1</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>输入参数ID</td>");
			sb.append("    <td>中文名称</td>");
			sb.append("    <td>是否外部赋值</td>");
			sb.append("    <td>类型</td>");
			sb.append("    <td>约束</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>app_id</span></td>");
			sb.append("    <td>应用程序<span lang='en-us'>ID</span></td>");
			sb.append("    <td>是</td>");
			sb.append("    <td>字符串型</td>");
			sb.append("    <td>无</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td><span lang='en-us'>license_key</span></td>");
			sb.append("    <td>应用程序授权码</td>");
			sb.append("    <td>是</td>");
			sb.append("    <td>字符串型</td>");
			sb.append("    <td>无</td>");
			sb.append("  </tr>");
			sb.append("  <tr>");
			sb.append("    <td>返回值说明</td>");
			sb.append("    <td colspan='4'>retvalue值存放0或者-1</td>");
			sb.append("  </tr>");
			sb.append("</table>");
			strRet = sb.toString();
		}else{//资源类的执行
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
