/**
 * 
 */
package com.hztech.platform.v3.common;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.sql.RowSet;

import com.hztech.util.HZUtil;

/**
 * @author Administrator
 *
 */
public class ComData implements Serializable,Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5093688087032205783L;
	
	private int inOut = 0;//in=0,out=1
	private int errorCode = 0;//zero:normal,not-zero:error
	private String errorDescription = "";//error description
	
	private LinkedHashMap<String,Object> head = new LinkedHashMap<String,Object>();//one-dimension data
	private ArrayList<RowSet> body = null;//tow-dimension data
	private ArrayList<String[]> cnFields = null;//chinese field name according to RowSet
	
	public ComData(){
		;
	}
	
	public void setInOut(int inOut){
		this.inOut = inOut;
	}
	
	public int getInOut(){
		return this.inOut;
	}

	/**
	 * get value by name
	 * @param name
	 * @return
	 */
	public Object getPm(String name) {
		return head.get(name);
	}
	
	/**
	 * set value to name
	 * @param name
	 * @param value
	 */
	public ComData setPm(String name, Object value) {
		head.put(name, value);
		return this;
	}
	
	/**
	 * get string's value by name
	 * @param name
	 * @return
	 */
	public String getStrPm(String name){
		Object obj = this.getPm(name);
		if(obj==null) return null;
		
		return String.valueOf(obj);
	}
	
	/**
	 * get converted attribute value
	 * @param attributeName
	 * @return
	 */
	public String getStrXPm(String attributeName){
		Object obj = this.getPm(attributeName);
		if(obj==null) return null;
		
		return HZUtil.descXci(String.valueOf(obj),head);
	}
	
	/**
	 * set converted attribute value
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	public ComData setStrXPm(String attributeName,String attributeValue){
		this.setPm(attributeName,HZUtil.descXci(attributeValue,head));
		
		return this;
	}
	
	/**
	 * compare tow attributes
	 * @param attributeName1
	 * @param attributeName2
	 * @return
	 */
	public boolean equal(String attributeName1,String attributeName2){
		
		return equalValue(attributeName1,this.getStrPm(attributeName2));
	}
	
	/**
	 * compare attribute and value
	 * @param attributeName
	 * @param value
	 * @return
	 */
	public boolean equalValue(String attributeName,String value){
		String value1 = this.getStrPm(attributeName);
		if(value1==value) return true;
		
		if(value1!=null && value1.equals(value)) return true;
		
		return false;
	}
	
	/**
	 * copy one attribute to another attribute
	 * @param srcName
	 * @param destName
	 * @return
	 */
	public ComData copyPm(String srcName,String destName){
		this.setPm(destName,this.getPm(srcName));
		
		return this;
	}
	
	/**
	 * remove one attribute
	 * @param name
	 * @return
	 */
	public ComData removePm(String name){
		head.remove(name);
		return this;
	}
	
	/**
	 * wether if parameter existing
	 * @param paraName
	 * @return
	 */
	public boolean existPara(String paraName){
		return head.containsKey(paraName);
	}
	
	/**
	 * making GUID vlaue for idname
	 * @param idName
	 * @return
	 */
	public ComData setGUID(String idName){
		this.setPm(idName,RandomGUID.getGUID());
		return this;
	}
	
	/**
	 * making date-time value
	 * @param datetime
	 * @return
	 */
	public ComData setDateTime(String datetime){
		this.setPm(datetime,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
		return this;
	}
	
	/**
	 * making date value
	 * @param date
	 * @return
	 */
	public ComData setDate(String date){
		this.setPm(date,new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
		return this;
	}
	
	/**
	 * making time value
	 * @param time
	 * @return
	 */
	public ComData setTime(String time){
		this.setPm(time,new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		return this;
	}
	
	/**
	 * making some attributes not including updated'attribute
	 * @param pkID
	 * @return
	 */
	public ComData initCreateWithNoUpdate(String pkID){
		this.setGUID(pkID);
		this.copyPm("SYSUSERCODE","CREUSRCODE");
		this.setDate("CREDATE");
		this.setTime("CRETIME");
		this.setPm("ISEFFECT","0");
		
		return this;
	}
	
	/**
	 * making some attributes for create
	 * @param pkID
	 * @return
	 */
	public ComData initCreate(String pkID){
		this.setGUID(pkID);
		this.copyPm("SYSUSERCODE","CREUSRCODE");
		this.setDate("CREDATE");
		this.setTime("CRETIME");
		this.copyPm("SYSUSERCODE","EDIUSRCODE");
		this.setDate("EDIDATE");
		this.setTime("EDITIME");
		this.setPm("ISEFFECT","0");
		
		return this;
	}
	
	/**
	 * making some attributes for update
	 * @return
	 */
	public ComData initUpdate(){
		this.copyPm("SYSUSERCODE","EDIUSRCODE");
		this.setDate("EDIDATE");
		this.setTime("EDITIME");
		
		return this;
	}
	
	//------------------------up to ComInput   --------------------------------------------------------------------------
	//------------------------down to ComOutput--------------------------------------------------------------------------
	
	/**
	 * get error code,zero is normal
	 */
	public int getRetCode() {
		return this.errorCode;
	}
	
	/**
	 * set error code
	 * @param retCode
	 */
	public void setRetCode(int retCode) {
		this.errorCode = retCode;
	}
	
	/**
	 * get error description
	 * @return
	 */
	public String getRetDesc() {
		return this.errorDescription;
	}
	
	/**
	 * set error description
	 * @param retDesc
	 */
	public void setRetDesc(String retDesc) {
		this.errorDescription = retDesc;
	}
	
	/**
	 * get returned value
	 * @return
	 */
	public String getRetValue() {
		return getStrPm("_ret_value_");
	}
	
	/**
	 * set returned value
	 * @param retValue
	 */
	public void setRetValue(String retValue) {
		setPm("_ret_value_",retValue);
	}
	
	/**
	 * get tip's type value
	 * @return
	 */
	public int getTipType() {
		String strTipType = getStrPm("_tip_type_");
		if(strTipType==null) return 0;
		
		return Integer.parseInt(strTipType);
	}
	
	/**
	 * set tip's type value
	 * @param tipType
	 */
	public void setTipType(int tipType) {
		setPm("_tip_type_",String.valueOf(tipType));
	}
	
	/**
	 * get content's value
	 * @return
	 */
	public String getContent() {
		return getStrPm("_content_");
	}
	
	/**
	 * set content's value
	 * @param content
	 */
	public void setContent(String content) {
		setPm("_content_",content);
	}
	
	/**
	 * add content's value
	 * @param content
	 */
	public void setContent2(String content) {
		if(content==null) return;
		if(content.length()==0){
			setContent("");
			return;
		}
		String ctn = getContent();
		if(ctn==null){
			ctn = content;
		}else if(ctn.equals(content)){
			;
		}else{
			ctn += "$$$"+content;
		}
		
		setContent(ctn);
	}
	
	/**
	 * get log's level value
	 * @return
	 */
	public String getLoglevel() {
		return getStrPm("_log_level_");
	}
	
	/**
	 * set log's level value
	 * @param loglevel
	 */
	public void setLoglevel(String loglevel) {
		setPm("_log_level_",loglevel);
	}
	
	/**
	 * add log's level value
	 * @param loglevel
	 */
	public void setLoglevel2(String loglevel) {
		if(loglevel==null) return;
		if(loglevel.length()==0){
			setLoglevel("");
			return;
		}
		String ll = getLoglevel();
		if(ll==null){
			ll = loglevel;
		}else if(ll.equals(loglevel)){
			;
		}else{
			ll += "$$$"+loglevel;
		}
		
		setLoglevel(ll);
	}
	
	/**
	 * set content and loglevel
	 * @param content
	 * @param loglevel
	 */
	public void setLog(String content,String loglevel){
		setContent(content);
		setLoglevel(loglevel);
	}
	
	/**
	 * add loglevel and content
	 * @param loglevel
	 * @param content
	 */
	public void setLog2(String loglevel,String content){
		setLoglevel2(loglevel);
		setContent2(content);
	}

	/**
	 * get body
	 * @return
	 */
	public ArrayList<RowSet> getBody() {
		return body;
	}

	@Deprecated
	public ArrayList<String[]> getCnFields() {
		return cnFields;
	}
	
	/**
	 * add one rowset object
	 * @param rowSet
	 */
	public void addRowSet(RowSet rowSet){
		if(rowSet == null) return;
		if(body == null) body = new ArrayList<RowSet>();
		body.add(rowSet);
	}
	
	/**
	 * convert to ComInput class
	 * @return
	 */
	public ComInput toComInput(){
		ComInput ci = new ComInput();
		for(Map.Entry<String,Object> entry:head.entrySet()){
			ci.setPm(entry.getKey(), entry.getValue());
		}
		//mark
		ci.setPm("_in_out_", inOut);
		return ci;
	}
	
	/**
	 * convert ComInput to ComData
	 * @param comInput
	 * @return
	 */
	public static ComData importComInput(ComInput comInput){
		ComData cd = new ComData();
		for(Map.Entry<String,Object> entry:comInput.entrySet()){
			cd.setPm(entry.getKey(), entry.getValue());
		}
		
		return cd;
	}
	
	/**
	 * convert to ComOutput class
	 * @return
	 */
	public ComOutput toComOutput(){
		ComOutput co = RSUtil.setOutput(errorCode, errorDescription);
		co.setRetValue(getRetValue());
		co.setContent(getContent());
		co.setLoglevel(getLoglevel());
		co.setTipType(getTipType());
		if(body !=null && body.size()>0){
			for(int i=0;i<body.size();i++)
				co = RSUtil.addOutput(co, body.get(i));
		}
		
		return co;
	}
	
	/**
	 * convert ComOutput to ComData
	 * @param comOutput
	 * @return
	 */
	public static ComData importComOutput(ComOutput comOutput){
		ComData cd = new ComData();
		cd.errorCode = comOutput.getRetCode();
		cd.errorDescription = comOutput.getRetDesc();
		cd.setRetValue(comOutput.getRetValue());
		cd.setContent(comOutput.getContent());
		cd.setLoglevel(comOutput.getLoglevel());
		cd.setTipType(comOutput.getTipType());
		int rsNum = RSUtil.getRSNum(comOutput);
		if(rsNum>0) cd.body = new ArrayList<RowSet>();
		for(int i=0;i<rsNum;i++){
			cd.body.add(RSUtil.getRS(comOutput, i));
		}
		return cd;
	}
}
