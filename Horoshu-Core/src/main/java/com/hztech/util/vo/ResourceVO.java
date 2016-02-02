package com.hztech.util.vo;

import java.util.ArrayList;

import org.jdom.Element;

public class ResourceVO {
	private String filename = null;//资源对应的不带路径文件名
	private String fullFilename = null;//资源对应的带路径文件名
	private String type = null;//其一般为resource
	private String appid = null;//开发商应用系统ID
	private String pcjid = null;//应用系统的PCJ包ID
	public String getPcjid() {
		return pcjid;
	}
	public void setPcjid(String pcjid) {
		this.pcjid = pcjid;
	}

	private String resourceName = null;//完整的资源名称，以sysservice,sysbl,sysminibl,sysbr,sysbo开头
	private String cnResourceName = null;//完整的中文资源名称，各级间以“->”分隔
	public String getCnResourceName() {
		return cnResourceName;
	}
	public void setCnResourceName(String cnResourceName) {
		this.cnResourceName = cnResourceName;
	}

	ArrayList<String> arrVersion = new ArrayList<String>();//各资源的版本
	ArrayList<Element> arrElement = new ArrayList<Element>();//各资源对应的Element
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setFullFilename(String fullFilename) {
		this.fullFilename = fullFilename;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	public void setElement(String version,Element element){
		int index = arrVersion.indexOf(version);
		if(index<0){
			arrVersion.add(version);
			arrElement.add(element);
		}else{
			arrElement.set(index, element);
		}
	}
	
	public String getFilename() {
		return filename;
	}
	public String getFullFilename() {
		return fullFilename;
	}
	public String getType() {
		return type;
	}
	public String getAppid() {
		return appid;
	}
	public String getResourceName() {
		return resourceName;
	}
	
	/**
	 * get version information,multi-version'split is "###"
	 * @return
	 */
	public String getVersions(){
		String version = "";
		for(String tmpVer:arrVersion)
			version += "###" + tmpVer;
		if(version.length()>=3) version = version.substring(3);
		
		return version;
	}
	
	public Element getElement(String version){
		int index = arrVersion.indexOf(version);
		if(index<0)
			return null;
		
		return arrElement.get(index);
	}
	
	public String getHelp(){
		return "暂无资源【"+this.resourceName+"】的帮助信息！";
	}
}
