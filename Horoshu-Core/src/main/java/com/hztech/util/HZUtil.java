package com.hztech.util;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.RowSet;

import org.jdom.Document;
import org.jdom.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Types;

import com.hztech.platform.agent.agentm2m.AgentMC;
import com.hztech.platform.agent.agentw2m.AgentW;
import com.hztech.platform.agent.agentw2m.Invoker;
import com.hztech.platform.web.WebBase;

/**
 * ���������࣬������������ķ���������Ĵ���
 * 
 * @author ��ռ��20070908����
 * 
 */
public class HZUtil {
	public final static String QUERY_SHOW_FIRST_COLUMN_MARK = "SHOW1";
	private final static String WARN_REPLACE_SUFFIX = "(NO VALUE!)";
	private final static String NULL_STRING = "";
	private final static char specChar = '{';
	private final static char specChar2 = '}';

	public HZUtil() {
		super();
		// TODO �Զ����ɹ��캯�����
	}

	/**
	 * web������м�㷽��,������request�����ĵ��� ������ ��ռ�� 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>��������·��������
	 * @param methodName
	 *            <code>String</code>������
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>ComOutput</code>�����������
	 * @throws ��
	 */
	public static ComOutput webExecBean(String className, String methodName, ComInput comInput) {
		return webExecBean(className, methodName, comInput, null);
	}

	/**
	 * web������м�㷽�� ������ ��ռ�� 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>��������·��������
	 * @param methodName
	 *            <code>String</code>������
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param request
	 *            <code>HttpServletRequest</code>
	 * @return <code>ComOutput</code>�����������
	 * @throws ��
	 */
	public static ComOutput webExecBean(String className, String methodName, ComInput comInput, HttpServletRequest request) {
		comInput.setPm("AgentName", "ComAgent.webExecBean");
		AgentW amcTest = new AgentW(className, request);
		ComOutput co2 = amcTest.cmd(methodName, comInput);
		return co2;
	}

	/**
	 * �м������м�㷽��,�������м����ö˵Ĳ����ĵ��� ������ ��ռ�� 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>��������·��������
	 * @param methodName
	 *            <code>String</code>������
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>ComOutput</code>�����������
	 * @throws ��
	 */
	public static ComOutput beanExecBean(String className, String methodName, ComInput comInput) {
		return beanExecBean(className, methodName, comInput, null);
	}

	/**
	 * �м������м�㷽�� ������ ��ռ�� 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>��������·��������
	 * @param methodName
	 *            <code>String</code>������
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param param
	 *            <code>Object</code>�м����ö˵Ĳ������ݱ���
	 * @return <code>ComOutput</code>�����������
	 * @throws ��
	 */
	public static ComOutput beanExecBean(String className, String methodName, ComInput comInput, Object param) {
		comInput.setPm("AgentName", "ComAgent.beanExecBean");
		AgentMC amcTest = new AgentMC(className, param);
		ComOutput co2 = amcTest.cmd(methodName, comInput, 0);
		return co2;
	}

	/**
	 * web�����web�㷽�� ������ ��ռ�� 2008-07-05
	 * 
	 * @param request
	 *            <code>HttpServletRequest</code>web���������
	 * @param response
	 *            <code>HttpServletResponse</code>web��Ӧ�����
	 * @param className
	 *            <code>String</code>��������·�����������������̳���WebBase��
	 * @param methodName
	 *            <code>String</code>������
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>ComOutput</code>�����������
	 * @throws ��
	 */
	public static ComOutput webExecWeb(HttpServletRequest request, HttpServletResponse response, String className, String methodName, ComInput comInput) {
		ComOutput co = null;
		try {
			comInput.setPm("AgentName", "ComAgent.webExecWeb");
			Object obj = Class.forName(className).newInstance();

			if (obj instanceof WebBase) {
				WebBase wb = (WebBase) Class.forName(className).newInstance();
				wb.setRequest(request);
				wb.setResponse(response);
				wb.setComInput(comInput);
				co = (ComOutput) Invoker.dynaCall(wb, methodName);
			} else {
				co = RSUtil.setOutput(-1, "�ࣺ" + className + "����WebBase������࣬�޷����д�����ã�", null);
			}
		} catch (Exception e) {
			co = RSUtil.setOutput(-1, exception2String(e), null);
		}

		return co;
	}

	/**
	 * ���ComInput�Ƿ񾭹�������ж�̬����� ������ ��ռ�� 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return ��
	 * @throws ��δ�����������
	 *             �����׳�Exception�쳣����ʾ"δ����ComAgent����ô���"
	 */
	public static void checkAgentComInput(ComInput comInput) throws Exception {
		Object obj = comInput.getPm("AgentName");
		if (obj == null) {
			throw new Exception("δ����ComAgent����ô���");
		}

		if ("".equals(obj)) {
			throw new Exception("δ����ComAgent����ô���");
		}

		return;
	}

	/**
	 * ��ComInput�л�ȡRequest Parameter ������ ��ռ�� 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param name
	 *            <code>String</code>��������
	 * @return <code>String</code>�ַ���ֵ
	 * @throws ��
	 */
	public static String getRequestParameterByComInput(ComInput comInput, String name) {
		if (name == null)
			return null;

		Object obj = comInput.getPm("HZHRP_" + name);
		if (obj == null)
			return null;

		return (String) obj;
	}

	/**
	 * ��ComInput�л�ȡRequest Attribute ������ ��ռ�� 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param name
	 *            <code>String</code>��������
	 * @return <code>String</code>�ַ���ֵ
	 * @throws ��
	 */
	public static String getRequestAttributeByComInput(ComInput comInput, String name) {
		if (name == null)
			return null;

		Object obj = comInput.getPm("HZHRA_" + name);
		if (obj == null)
			return null;

		return (String) obj;
	}

	/**
	 * ��ComInput�л�ȡSession Attribute ������ ��ռ�� 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param name
	 *            <code>String</code>��������
	 * @return <code>String</code>�ַ���ֵ
	 * @throws ��
	 */
	public static String getSessionAttributeByComInput(ComInput comInput, String name) {
		if (name == null)
			return null;

		Object obj = comInput.getPm("HZHSA_" + name);
		if (obj == null)
			return null;

		return (String) obj;
	}

	/**
	 * ��ӡ��ComInput�����е�ֵ(��NOTCOPY_��ͷ�Ĳ�������) ������ ��ռ�� 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param name
	 *            <code>String</code>��������
	 * @return ��
	 * @throws ��
	 */
	public static void printComInput(ComInput comInput) {
		Map map = comInput.getMap();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			if (!entry.getKey().toString().startsWith("NOTCOPY_")) {
				Debug.showDebug(entry.getKey() + ":" + entry.getValue());
			}
		}
	}

	/**
	 * ��rowset����ת���String�ı��String���ͣ�����Ϊ���ַ��� ������ ��ռ�� 2008-07-05
	 * 
	 * @param rs
	 *            <code>RowSet</code>�����
	 * @param fieldNo
	 *            <code>int</code>�ֶ���ţ���1��ʼ
	 * @return <code>String</code>�ַ���ֵ
	 * @throws ��
	 */
	public static String getString(RowSet rs, int fieldNo) {
		String str = "";
		int fieldType = 0;

		try {
			fieldType = rs.getMetaData().getColumnType(fieldNo);
			switch (fieldType) {
			case Types.BIGINT:
			case Types.BIT:
			case Types.BOOLEAN:
			case Types.CHAR:
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.INTEGER:
			case Types.LONGVARCHAR:
			case Types.NUMERIC:
			case Types.REAL:
			case Types.SMALLINT:
			case Types.TINYINT:
			case Types.VARCHAR:
				str = rs.getString(fieldNo);
				break;
			case Types.DATE:
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				str = format.format(rs.getDate(fieldNo));
				break;
			case Types.TIME:
				SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
				str = format2.format(rs.getTime(fieldNo));
				break;
			case Types.TIMESTAMP:
				str = rs.getTimestamp(fieldNo).toString();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			Debug.showDebug(e, "KKKfieldNo:fieldType-" + fieldNo + ":" + fieldType + "KKK");
		}
		return str;
	}

	/**
	 * ���á�NO VALUE!�����淽ʽ��convertSQL�滻 ������ ��ռ�� 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>Ҫ�滻���ַ���
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param separator
	 *            <code>String</code>�����ָ���
	 * @param replaceType
	 *            <code>int</code>�滻����
	 * @return <code>String</code>�Ѿ��滻��ɵ��ַ���ֵ
	 * @throws ��
	 */
	public static String convertSQL(String str, HashMap<String, Object> comInput, String separator, int replaceType) {
		return convertSQL(str, comInput, separator, replaceType, 1);
	}

	/**
	 * ����SQL���,������"�ָ��� + ������"�Ĳ�������Ӧ��ֵ���� ������ ��ռ�� 2008-07-05 ���Ӷ�###���Ų������滻��֧�� �޸��� ��ռ�� 2008-08-28
	 * �ṩ{xYYYY}��xYYYY��ͬ���Ĺ��ܣ�����x��ʾseparator���ţ���YYYY���ܱ���������������� �޸��� ��ռ�� 2009-04-24
	 * 
	 * @param str
	 *            <code>String</code>�������SQL��仺���ַ���
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @param separator
	 *            <code>String</code>�������Զ���Ĳ�����ǰ�ָ�����,�� ':', '@','#', '$' ,'%','&'�ȷǿո����ַ�
	 * @param replaceType
	 *            <code>int</code>�滻��ʽ��0Ϊxxx,1Ϊ'xxx',2Ϊ'%xxx%',3Ϊ'xxx','yyy','zzz'
	 * @param warnType
	 *            <code>int</code>�������� 0-����ԭ����1-����":NO VALUE!"���棬2-���á���ֵ���滻
	 * @return <code>String</code>�Ѿ��滻��ɵ��ַ���ֵ
	 * @throws ��
	 */
	public static String convertSQL(String str, HashMap<String, Object> comInput, String separator, int replaceType, int warnType) {
		String colName = "", colFullName = "";

		if (str == null || str.equals("")) {
			return str;
		}

		if (comInput == null) {
			return str;
		}

		String sql = str;
		boolean b3Sharp = false;
		//����###�������������������滻
		if ("#".equals(separator) && sql.indexOf("###") >= 0) {
			sql = HZUtil.replaceAll(sql, "###", "(*$*1*1**)");
			b3Sharp = true;
		}

		/*
		temp = sql.split(separator);
		int i = 0;
		for(i=1;i<temp.length;i++){
			
			temp[i] = temp[i].trim().substring(0, getVarLength(temp[i]));
		}*/
		ArrayList<String> alColName = new ArrayList<String>();
		ArrayList<String> alColFullName = new ArrayList<String>();
		String sep = separator;
		if (sep.charAt(0) == '[')
			sep = sep.substring(1, sep.length() - 1);
		int y = 0, slen = sep.length(), vlen = 0;
		while (y < sql.length()) {
			if (sep.equals(sql.substring(y, y + slen))) {
				if (y == 0 || sql.charAt(y - 1) != specChar) {
					vlen = getVarLength(sql.substring(y + 1));
					if (vlen > 0) {
						alColName.add(sql.substring(y + 1, y + 1 + vlen));
						alColFullName.add(sep + sql.substring(y + 1, y + 1 + vlen));
						y += 1 + vlen;
						continue;
					}
				} else {
					vlen = getVarLength2(sql.substring(y + 1));
					if (vlen > 0) {
						alColName.add(sql.substring(y + 1, y + 1 + vlen - 1));
						alColFullName.add(specChar + sep + sql.substring(y + 1, y + 1 + vlen - 1) + specChar2);
						y += 1 + vlen;
						continue;
					}
				}
			}
			y++;
		}

		/*
		for(int x=0;x<alColName.size();x++){
			System.out.println(alColName.get(x)+":"+alColFullName.get(x));
		}
		if(1==1) return sql;
		*/

		String value = "";

		for (int i = 0; i < alColName.size(); i++) {
			colName = alColName.get(i);
			colFullName = alColFullName.get(i);
			//��ComInput�������õĲ���ֵ��δ���õĲ���ֵ��������Դ����Ժ��߽�����ʾ
			if (warnType == 2)
				value = NULL_STRING;
			else
				value = colName + WARN_REPLACE_SUFFIX;

			if (colName.trim().equals("")) {
				continue;
			}
			if (comInput.get(colName) != null) {
				value = String.valueOf(comInput.get(colName));
			}

			if (warnType == 0 && value.endsWith(WARN_REPLACE_SUFFIX)) {
				;
			} else {
				if (replaceType == 0) {
					sql = hzReplaceFirst(sql, colFullName, value);
				} else if (replaceType == 1) {
					sql = hzReplaceFirst(sql, colFullName, "'" + value + "'");
				} else if (replaceType == 2) {
					sql = hzReplaceFirst(sql, colFullName, "'%" + value + "%'");
				} else if (replaceType == 3) {
					Object obj = comInput.get(colName);
					String[] arr3;
					if (obj instanceof String[]) {
						arr3 = (String[]) obj;
					} else if (obj instanceof int[]) {
						int[] objs = (int[]) obj;
						if (objs.length > 0) {
							arr3 = new String[objs.length];
							for (int j = 0; j < objs.length; j++) {
								arr3[j] = String.valueOf(objs[j]);
							}
						} else {
							arr3 = new String[] {};
						}
					} else {
						arr3 = value.split(",");
					}
					String arr3Tmp = "";
					for (int k = 0; k < arr3.length; k++) {
						arr3Tmp += ",'" + arr3[k] + "'";
					}
					if (!arr3Tmp.equals("")) {
						arr3Tmp = arr3Tmp.substring(1);
					} else {
						arr3Tmp = "''";
					}
					sql = hzReplaceFirst(sql, colFullName, arr3Tmp);
				} else {
					sql = hzReplaceFirst(sql, colFullName, "'" + value + "'");
				}
			}
		}

		if (b3Sharp) {
			sql = HZUtil.replaceAll(sql, "(*$*1*1**)", "###");
		}

		return sql;
	}

	/*
	 * �����ַ��������ظ��ַ������Թ��ɱ������ַ�����
	 */
	private static int getVarLength(String var) {
		int i = 0;
		char c;

		for (i = 0; i < var.length(); i++) {
			c = var.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
				continue;
			} else {
				break;
			}
		}
		return i;
	}

	/*
	 * �����ַ��������ظ��ַ������Թ��ɱ������ַ����ȣ�Ѱ��"}"�������򷵻�0
	 */
	private static int getVarLength2(String var) {
		if (var == null) {
			return 0;
		}

		int iRet = var.indexOf(specChar2);
		if (iRet < 1)
			return 0;

		return iRet + 1;
	}

	/**
	 * ����ַ�����ComInput�������µ��ַ���,��Ϊ@XXXʱ����XXXΪ�ַ���������ת��Ϊ'xxx','xxx'�ĸ�ʽ��Ϊ�ԡ�,���ŷָ����ַ���ʱҲת���'xxx','xxx'�ĸ�ʽ ������֧�ֶ�$/#/@��ͷ�ı������滻
	 * ������ ��ռ�� 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>���滻�ַ���
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>String</code>�滻�����ַ���ֵ
	 * @throws ��
	 */
	public static String strXci(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0);
		sql = HZUtil.convertSQL(sql, comInput, "#", 1);
		//��Ӷ�@��֧��
		return HZUtil.convertSQL(sql, comInput, "@", 3, 0);
	}

	/**
	 * ����ַ�����ComInput�������µ��ַ��� ������֧�ֶ�$/#/:��ͷ�ı������滻 ������ ��ռ�� 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>���滻�ַ���
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>String</code>�滻�����ַ���ֵ
	 * @throws ��
	 */
	public static String strXciWithColon(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0);
		sql = HZUtil.convertSQL(sql, comInput, ":", 1);
		return HZUtil.convertSQL(sql, comInput, "#", 1);
	}

	/**
	 * ����ַ�����ComInput�������µ��ַ��� ������֧�ֶ�$/#/:/?��ͷ�ı������滻����?�������'%xxx%'����ʽ ������ ��ռ�� 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>���滻�ַ���
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>String</code>�滻�����ַ���ֵ
	 * @throws ��
	 */
	public static String strXciWithColonAndQuestion(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0);
		sql = HZUtil.convertSQL(sql, comInput, ":", 1);
		sql = HZUtil.convertSQL(sql, comInput, "[?]", 2);
		return HZUtil.convertSQL(sql, comInput, "#", 1);
	}

	/**
	 * ����ַ�����ComInput�������µ��ַ��� ������֧�ֶ�$/#/:��ͷ�ı������滻������ComInput�Ҳ������������򲻽����滻�� ������ ��ռ�� 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>���滻�ַ���
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>String</code>�滻�����ַ���ֵ
	 * @throws ��
	 */
	public static String strXciWithHtml(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0, 0);
		sql = HZUtil.convertSQL(sql, comInput, ":", 1, 0);
		return HZUtil.convertSQL(sql, comInput, "#", 1, 0);
	}

	/**
	 * ����ַ�����ComInput�������µ��ַ���,������ֵΪ������ʾ����ֵ�� ������֧�ֶ�$/#��ͷ�ı������滻������ComInput�Ҳ��������������á���ֵ���滻�� ������ ��ռ�� 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>���滻�ַ���
	 * @param comInput
	 *            <code>ComInput</code>�����������
	 * @return <code>String</code>�滻�����ַ���ֵ
	 * @throws ��
	 */
	public static String descXci(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0, 2);
		return HZUtil.convertSQL(sql, comInput, "#", 1, 2);
	}

	/**
	 * ��֧�ֶ�#�������滻
	 * 
	 * @param str
	 * @param comInput
	 * @return
	 */
	public static String sharpXci(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		return HZUtil.convertSQL(str, comInput, "#", 1, 2);
	}

	/**
	 * ��֧�ֶ�$�������滻
	 * 
	 * @param str
	 * @param comInput
	 * @return
	 */
	public static String dollarXci(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		return HZUtil.convertSQL(str, comInput, "[$]", 0, 2);
	}

	/**
	 * a���鼰b������бȽϣ��ó�a�������ж�b�������޵�Ԫ�ء�a����������b������Ҳ�е�Ԫ�ء�a������û�ж�b�������е�Ԫ�� �ֱ�˳����뵽���ص������� ������ ��ռ�� 2008-08-13
	 * 
	 * @param last
	 *            <code>String[]</code>ԭ�ȵ�����-a����
	 * @param cur
	 *            <code>String[]</code>���ڵ�����-b����
	 * @return <code>String[][]</code>�ȽϺ�����飬��0��Ԫ��Ϊa��b�ޣ���1��Ԫ��Ϊa��b�У���2��Ԫ��Ϊa��b�У���Ԫ�ؾ�����Ϊ0���ȶ���
	 * @throws ��
	 */
	public static String[][] compare2StrArray(String[] last, String[] cur) {
		String[] r = {};

		if (last == null || last.length == 0) {
			return new String[][] { r, r, cur == null ? r : cur };
		}

		if (cur == null || cur.length == 0) {
			return new String[][] { last, r, r };
		}

		StringBuffer sb1 = new StringBuffer(1024);
		StringBuffer sb2 = new StringBuffer(1024);
		StringBuffer sb3 = new StringBuffer(1024);
		boolean[] tmp = new boolean[cur.length];

		int i = 0, j = 0;

		for (i = 0; i < last.length; i++) {
			for (j = 0; j < cur.length; j++) {
				if (last[i].equalsIgnoreCase(cur[j])) {//a��b����
					sb2.append("," + cur[j]);
					tmp[j] = true;
					break;
				}
			}
			if (j == cur.length) {//a��bû��
				sb1.append("," + last[i]);
			}
		}

		for (i = 0; i < cur.length; i++) {
			if (!tmp[i]) {
				sb3.append("," + cur[i]);
			}
		}

		String s1 = sb1.length() == 0 ? "" : sb1.toString().substring(1);
		String s2 = sb2.length() == 0 ? "" : sb2.toString().substring(1);
		String s3 = sb3.length() == 0 ? "" : sb3.toString().substring(1);

		return new String[][] { s1.split(","), s2.split(","), s3.split(",") };
	}

	/**
	 * �����ַ����������滻 ������ ��ռ�� 2008-07-05
	 * 
	 * @param src
	 *            <code>String</code>���滻�ַ���
	 * @param fnd
	 *            <code>String</code>���滻���Ӵ�
	 * @param rep
	 *            <code>String</code>�滻���Ӵ�
	 * @return <code>String</code>�滻�����ַ���ֵ
	 * @throws ��
	 */
	public static String replaceAll(String src, String fnd, String rep) {
		if (src == null)
			return src;
		if (fnd == null)
			return src;
		if (rep == null) {
			if (src.indexOf(fnd) >= 0) {
				return null;
			} else {
				return src;
			}
		}

		String[] arrStr = split(src, fnd);

		StringBuffer sb = new StringBuffer(1024);
		for (int i = 0; i < arrStr.length; i++) {
			sb.append(rep);
			sb.append(arrStr[i]);
		}

		return sb.substring(rep.length());
	}

	public static String[] split(String src, String separate) {
		if (src == null)
			return null;
		if (separate == null)
			return new String[] { src };
		if (src.length() == 0)
			return new String[] { "" };

		ArrayList<String> al = new ArrayList<String>();
		int pos = 0;
		int prepos = 0;
		int fndlen = separate.length();

		pos = src.indexOf(separate, pos);
		while (pos >= 0) {
			al.add(src.substring(prepos, pos));
			prepos = pos + fndlen;
			pos = src.indexOf(separate, pos + fndlen);
		}
		al.add(src.substring(prepos));

		if (al.size() == 0)
			return null;
		String[] arrStr = new String[1];
		return al.toArray(arrStr);
	}

	/**
	 * ���ܣ�������ʵ��e������ת����ַ��� �����ˣ���ռ�� �������ڣ�2008-07-25
	 */
	public static String exception2String(Exception e) {
		String exStr = null;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		exStr = sw.toString();
		pw.close();
		try {
			sw.close();
		} catch (IOException ioe) {
			;
		}
		sw = null;
		pw = null;
		if (exStr == null)
			exStr = "";

		return exStr;
	}

	/**
	 * ���ܣ������ļ����ж��Ǻ����ļ� �����ˣ���ռ�� �������ڣ�2008-08-15
	 */
	public static String getMimeType(String fName) {
		fName = fName.toLowerCase();
		if (fName.endsWith(".jpg") || fName.endsWith(".jpeg") || fName.endsWith(".jpe"))
			return "image/jpeg";
		else if (fName.endsWith(".gif"))
			return "image/gif";
		else if (fName.endsWith(".bmp"))
			return "image/bmp";
		else if (fName.endsWith(".pdf"))
			return "application/pdf";
		else if (fName.endsWith(".htm") || fName.endsWith(".html") || fName.endsWith(".shtml"))
			return "text/html";
		else if (fName.endsWith(".avi"))
			return "video/x-msvideo";
		else if (fName.endsWith(".mov") || fName.endsWith(".qt"))
			return "video/quicktime";
		else if (fName.endsWith(".mpg") || fName.endsWith(".mpeg") || fName.endsWith(".mpe"))
			return "video/mpeg";
		else if (fName.endsWith(".zip"))
			return "application/zip";
		else if (fName.endsWith(".tiff") || fName.endsWith(".tif"))
			return "image/tiff";
		else if (fName.endsWith(".rtf"))
			return "application/rtf";
		else if (fName.endsWith(".mid") || fName.endsWith(".midi"))
			return "audio/x-midi";
		else if (fName.endsWith(".xl") || fName.endsWith(".xls") || fName.endsWith(".xlv") || fName.endsWith(".xla") || fName.endsWith(".xlb") || fName.endsWith(".xlt") || fName.endsWith(".xlm")
				|| fName.endsWith(".xlk"))
			return "application/excel";
		else if (fName.endsWith(".doc") || fName.endsWith(".dot"))
			return "application/msword";
		else if (fName.endsWith(".png"))
			return "image/png";
		else if (fName.endsWith(".xml"))
			return "text/xml";
		else if (fName.endsWith(".svg"))
			return "image/svg+xml";
		else if (fName.endsWith(".mp3"))
			return "audio/mp3";
		else if (fName.endsWith(".ogg"))
			return "audio/ogg";
		else
			return "text/plain";
	}

	private static String hzReplaceFirst(String str, String src, String dest) {
		if (str == null || src == null || dest == null)
			return str;
		int pos = str.indexOf(src);
		String back = "";
		if (pos + src.length() < str.length()) {
			back = str.substring(pos + src.length());
		}

		return str.substring(0, pos) + dest + back;
	}

	public static ComInput dom2ComInput(Document doc, ComInput comInput) {
		//��XML�������õ�ComInput��
		Element elmAction = doc.getRootElement().getChild("head").getChild("action");
		comInput.setPm("SYS_AJAX_ACTION", elmAction.getValue());

		Element elmBody = doc.getRootElement().getChild("body");
		List list = elmBody.getChildren();
		for (int i = 0; i < list.size(); i++) {
			Element elm = (Element) list.get(i);
			if ("dom".equalsIgnoreCase(elm.getAttributeValue("type"))) {
				Document docObj = new Document((Element) elm.clone());
				comInput.setPm(elm.getName(), docObj);
			} else {

				//���� bylaisz 2011��12��02��
				String strValue = HZSecretSimple.dataEnctypeSimple(elm.getName(), elm.getValue());
				comInput.setPm(elm.getName(), ComInput.getReplaceValue(strValue));

			}
		}

		return comInput;
	}

	public static String comOutput2Str(ComOutput co) {
		return RSUtil.xmlComOutput(co, null, null);
	}

	/**
	 * ���ַ������б���
	 * 
	 * @param src
	 * @return
	 */
	public static String escape(String src) {
		int i;
		char j;
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length() * 6);

		for (i = 0; i < src.length(); i++) {
			j = src.charAt(i);
			if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j))
				tmp.append(j);
			else if (j < 256) {
				tmp.append("%");
				if (j < 16)
					tmp.append("0");
				tmp.append(Integer.toString(j, 16));
			} else {
				tmp.append("%u");
				tmp.append(Integer.toString(j, 16));
			}
		}
		return tmp.toString();
	}

	/**
	 * ���ַ������н���
	 * 
	 * @param src
	 * @return
	 */
	public static String unescape(String src) {
		StringBuffer tmp = new StringBuffer();
		tmp.ensureCapacity(src.length());
		int lastPos = 0, pos = 0;
		char ch;
		while (lastPos < src.length()) {
			pos = src.indexOf("%", lastPos);
			if (pos == lastPos) {
				if (src.charAt(pos + 1) == 'u') {
					ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
					tmp.append(ch);
					lastPos = pos + 6;
				} else {
					ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
					tmp.append(ch);
					lastPos = pos + 3;
				}
			} else {
				if (pos == -1) {
					tmp.append(src.substring(lastPos));
					lastPos = src.length();
				} else {
					tmp.append(src.substring(lastPos, pos));
					lastPos = pos;
				}
			}
		}
		return tmp.toString();
	}

	/**
	 * ���۹����ô�ROWSET��ȡXML�ַ���
	 * 
	 * @param rowSet
	 * @param hasXMLHead
	 * @return
	 * @throws Exception
	 */
	public static String getToolXMLData(RowSet rowSet, boolean hasXMLHead) throws Exception {
		int columnCount = rowSet.getMetaData().getColumnCount();//������
		String cell = "";
		StringBuffer xmlData = new StringBuffer(2048);
		xmlData.setLength(0);
		rowSet.beforeFirst();

		//��ʼ����xmldata
		if (hasXMLHead)
			xmlData.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
		xmlData.append("<rows>");
		SimpleDateFormat datefmt = new SimpleDateFormat("yyMMddHHmmss");
		int i = 0;
		while (rowSet.next()) {
			i++;
			xmlData.append("<row id=\"" + datefmt.format(new Date()) + i + "\">");
			for (int j = 1; j <= columnCount; j++) {
				cell = rowSet.getString(j) == null ? "" : rowSet.getString(j);
				if (RSUtil.isLawfulXML(cell)) {
					xmlData.append("<cell>" + cell + "</cell>");
				} else {
					xmlData.append("<cell><![CDATA[" + cell + "]]></cell>");
				}
			}
			xmlData.append("</row>");
		}
		xmlData.append("</rows>");
		return xmlData.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO �Զ����ɷ������
		String s1 = escape("���123Ҳ��!");
		String s2 = unescape(s1);
		System.out.println(s1);
		System.out.println(s2);
		System.out.println(replaceAll("com.\\nhzt\\nech.*", "\\n", "\n"));
		//if(1==1) return;
		/*
		try{
			//int k = 1/0;
			//System.out.println(k);
			//String[][] s = HZUtil.compare2StrArray(new String[]{"1","2"},new String[]{"2","6"});
			//String[][] s = HZUtil.compare2StrArray(new String[]{},new String[]{});
			//String[][] s = HZUtil.compare2StrArray(null,null);
			System.out.println(new Date().getTime());
			String[] s1 = new String[1000];
			String[] s2 = new String[1000];
			for(int i=0;i<s1.length;i++){
				s1[i] = String.valueOf(new Random().nextInt(2000));
			}
			
			for(int i=0;i<s1.length;i++){
				s2[i] = String.valueOf(new Random().nextInt(2000));
			}
			
			String[][] s = HZUtil.compare2StrArray(s1,s2);
			for(int i=0;i<s.length;i++){
				for(int j=0;j<s[i].length;j++){
					//System.out.print(s[i][j]+",");
				}
				//System.out.println();
			}
		}catch(Exception e){
			System.out.println("hzh:" + exception2String(e));
		}
		System.out.println(new Date().getTime());
		
		if(1==1) return;
		*/
		/*
		System.out.println(XMLIO.class.getResource("").getFile());
		String path = ClassLoader.getSystemResource("").getFile()+"h"+File.separator;
		File directory = new File(path);
		File[] files = null;
		if(directory.isDirectory()){
			files = directory.listFiles();
		}else if(directory.isFile()){
			files = new File[1];
			files[0] = directory;
		}else{
			throw new ParserException("XML�ļ���·��:"+path+"�����ڣ�");
		}
		
		for (int i=0;i<files.length;i++){
			System.out.println(files[i].getName());
		}
		*/

		ComInput ci = new ComInput();
		ci.setPm("H1", "kkkk");
		ci.setPm("H2", "uuuu");
		String[] h8 = { "sss", "ttt" };
		ci.setPm("H8", h8);
		ci.setPm("1", "helloworld");
		//System.out.println(strXciWithColonAndQuestion("$H2 $rr ? #rr ?H2, ?H8 :H1 :h2 ?",ci));
		System.out.println(strXci("{$H2}He $rr ? [1].2H e{#rr}s ?H2, k in(@H8) :H1 :h2 ?$s", ci));

		/*
		String[] tmp = split("com.hztech.*","*");
		for(String s:tmp){
			System.out.println(s);
		}
		*/

	}

}
