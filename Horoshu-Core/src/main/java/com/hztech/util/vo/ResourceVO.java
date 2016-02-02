package com.hztech.util.vo;

import java.util.ArrayList;

import org.jdom.Element;

public class ResourceVO {
	private String filename = null;//��Դ��Ӧ�Ĳ���·���ļ���
	private String fullFilename = null;//��Դ��Ӧ�Ĵ�·���ļ���
	private String type = null;//��һ��Ϊresource
	private String appid = null;//������Ӧ��ϵͳID
	private String pcjid = null;//Ӧ��ϵͳ��PCJ��ID
	public String getPcjid() {
		return pcjid;
	}
	public void setPcjid(String pcjid) {
		this.pcjid = pcjid;
	}

	private String resourceName = null;//��������Դ���ƣ���sysservice,sysbl,sysminibl,sysbr,sysbo��ͷ
	private String cnResourceName = null;//������������Դ���ƣ��������ԡ�->���ָ�
	public String getCnResourceName() {
		return cnResourceName;
	}
	public void setCnResourceName(String cnResourceName) {
		this.cnResourceName = cnResourceName;
	}

	ArrayList<String> arrVersion = new ArrayList<String>();//����Դ�İ汾
	ArrayList<Element> arrElement = new ArrayList<Element>();//����Դ��Ӧ��Element
	
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
		return "������Դ��"+this.resourceName+"���İ�����Ϣ��";
	}
}
