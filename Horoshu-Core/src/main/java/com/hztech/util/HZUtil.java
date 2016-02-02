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
 * 公共代理类，本类包含公共的方法及各层的代理
 * 
 * @author 何占华20070908创建
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
		// TODO 自动生成构造函数存根
	}

	/**
	 * web层调用中间层方法,不传入request参数的调用 创建人 何占华 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>带完整包路径的类名
	 * @param methodName
	 *            <code>String</code>方法名
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>ComOutput</code>公共输出参数
	 * @throws 无
	 */
	public static ComOutput webExecBean(String className, String methodName, ComInput comInput) {
		return webExecBean(className, methodName, comInput, null);
	}

	/**
	 * web层调用中间层方法 创建人 何占华 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>带完整包路径的类名
	 * @param methodName
	 *            <code>String</code>方法名
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param request
	 *            <code>HttpServletRequest</code>
	 * @return <code>ComOutput</code>公共输出参数
	 * @throws 无
	 */
	public static ComOutput webExecBean(String className, String methodName, ComInput comInput, HttpServletRequest request) {
		comInput.setPm("AgentName", "ComAgent.webExecBean");
		AgentW amcTest = new AgentW(className, request);
		ComOutput co2 = amcTest.cmd(methodName, comInput);
		return co2;
	}

	/**
	 * 中间层调用中间层方法,不传入中间层调用端的参数的调用 创建人 何占华 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>带完整包路径的类名
	 * @param methodName
	 *            <code>String</code>方法名
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>ComOutput</code>公共输出参数
	 * @throws 无
	 */
	public static ComOutput beanExecBean(String className, String methodName, ComInput comInput) {
		return beanExecBean(className, methodName, comInput, null);
	}

	/**
	 * 中间层调用中间层方法 创建人 何占华 2008-07-05
	 * 
	 * @param className
	 *            <code>String</code>带完整包路径的类名
	 * @param methodName
	 *            <code>String</code>方法名
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param param
	 *            <code>Object</code>中间层调用端的参数，暂保留
	 * @return <code>ComOutput</code>公共输出参数
	 * @throws 无
	 */
	public static ComOutput beanExecBean(String className, String methodName, ComInput comInput, Object param) {
		comInput.setPm("AgentName", "ComAgent.beanExecBean");
		AgentMC amcTest = new AgentMC(className, param);
		ComOutput co2 = amcTest.cmd(methodName, comInput, 0);
		return co2;
	}

	/**
	 * web层调用web层方法 创建人 何占华 2008-07-05
	 * 
	 * @param request
	 *            <code>HttpServletRequest</code>web层请求对象
	 * @param response
	 *            <code>HttpServletResponse</code>web层应答对象
	 * @param className
	 *            <code>String</code>带完整包路径的类名，该类必须继承自WebBase类
	 * @param methodName
	 *            <code>String</code>方法名
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>ComOutput</code>公共输出参数
	 * @throws 无
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
				co = RSUtil.setOutput(-1, "类：" + className + "不是WebBase类的子类，无法进行代理调用！", null);
			}
		} catch (Exception e) {
			co = RSUtil.setOutput(-1, exception2String(e), null);
		}

		return co;
	}

	/**
	 * 检查ComInput是否经过本类进行动态代理的 创建人 何占华 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return 无
	 * @throws 若未经过本类代理
	 *             ，则抛出Exception异常，提示"未经过ComAgent类调用代理！"
	 */
	public static void checkAgentComInput(ComInput comInput) throws Exception {
		Object obj = comInput.getPm("AgentName");
		if (obj == null) {
			throw new Exception("未经过ComAgent类调用代理！");
		}

		if ("".equals(obj)) {
			throw new Exception("未经过ComAgent类调用代理！");
		}

		return;
	}

	/**
	 * 从ComInput中获取Request Parameter 创建人 何占华 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param name
	 *            <code>String</code>参数名称
	 * @return <code>String</code>字符串值
	 * @throws 无
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
	 * 从ComInput中获取Request Attribute 创建人 何占华 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param name
	 *            <code>String</code>属性名称
	 * @return <code>String</code>字符串值
	 * @throws 无
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
	 * 从ComInput中获取Session Attribute 创建人 何占华 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param name
	 *            <code>String</code>属性名称
	 * @return <code>String</code>字符串值
	 * @throws 无
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
	 * 打印出ComInput中所有的值(以NOTCOPY_开头的参数除外) 创建人 何占华 2008-07-05
	 * 
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param name
	 *            <code>String</code>属性名称
	 * @return 无
	 * @throws 无
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
	 * 将rowset中能转变成String的变成String类型，否则为空字符串 创建人 何占华 2008-07-05
	 * 
	 * @param rs
	 *            <code>RowSet</code>结果集
	 * @param fieldNo
	 *            <code>int</code>字段序号，从1开始
	 * @return <code>String</code>字符串值
	 * @throws 无
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
	 * 采用“NO VALUE!”警告方式的convertSQL替换 创建人 何占华 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>要替换的字符串
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param separator
	 *            <code>String</code>变量分隔符
	 * @param replaceType
	 *            <code>int</code>替换类型
	 * @return <code>String</code>已经替换完成的字符串值
	 * @throws 无
	 */
	public static String convertSQL(String str, HashMap<String, Object> comInput, String separator, int replaceType) {
		return convertSQL(str, comInput, separator, replaceType, 1);
	}

	/**
	 * 翻译SQL语句,将形如"分隔符 + 参数名"的参数用相应的值代替 创建人 何占华 2008-07-05 增加对###符号不进行替换的支持 修改人 何占华 2008-08-28
	 * 提供{xYYYY}和xYYYY等同表达的功能，其中x表示separator符号，且YYYY不受变量命名规则的限制 修改人 何占华 2009-04-24
	 * 
	 * @param str
	 *            <code>String</code>待翻译的SQL语句缓存字符串
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @param separator
	 *            <code>String</code>调用者自定义的参数名前分隔符号,如 ':', '@','#', '$' ,'%','&'等非空格类字符
	 * @param replaceType
	 *            <code>int</code>替换方式，0为xxx,1为'xxx',2为'%xxx%',3为'xxx','yyy','zzz'
	 * @param warnType
	 *            <code>int</code>警告类型 0-保持原样，1-采用":NO VALUE!"警告，2-采用“空值”替换
	 * @return <code>String</code>已经替换完成的字符串值
	 * @throws 无
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
		//考虑###的情况，该情况不进行替换
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
			//对ComInput中有设置的参数值和未设置的参数值进行区别对待，对后者进行提示
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
	 * 根据字符串，返回该字符串可以构成变量的字符长度
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
	 * 根据字符串，返回该字符串可以构成变量的字符长度，寻找"}"，若无则返回0
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
	 * 结合字符串和ComInput，生成新的字符串,当为@XXX时，当XXX为字符串数组则转变为'xxx','xxx'的格式，为以“,”号分隔的字符串时也转变成'xxx','xxx'的格式 本方法支持对$/#/@开头的变量的替换
	 * 创建人 何占华 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>待替换字符串
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>String</code>替换完后的字符串值
	 * @throws 无
	 */
	public static String strXci(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0);
		sql = HZUtil.convertSQL(sql, comInput, "#", 1);
		//添加对@的支持
		return HZUtil.convertSQL(sql, comInput, "@", 3, 0);
	}

	/**
	 * 结合字符串和ComInput，生成新的字符串 本方法支持对$/#/:开头的变量的替换 创建人 何占华 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>待替换字符串
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>String</code>替换完后的字符串值
	 * @throws 无
	 */
	public static String strXciWithColon(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0);
		sql = HZUtil.convertSQL(sql, comInput, ":", 1);
		return HZUtil.convertSQL(sql, comInput, "#", 1);
	}

	/**
	 * 结合字符串和ComInput，生成新的字符串 本方法支持对$/#/:/?开头的变量的替换，“?”将变成'%xxx%'的形式 创建人 何占华 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>待替换字符串
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>String</code>替换完后的字符串值
	 * @throws 无
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
	 * 结合字符串和ComInput，生成新的字符串 本方法支持对$/#/:开头的变量的替换，若在ComInput找不到变量参数则不进行替换！ 创建人 何占华 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>待替换字符串
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>String</code>替换完后的字符串值
	 * @throws 无
	 */
	public static String strXciWithHtml(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0, 0);
		sql = HZUtil.convertSQL(sql, comInput, ":", 1, 0);
		return HZUtil.convertSQL(sql, comInput, "#", 1, 0);
	}

	/**
	 * 结合字符串和ComInput，生成新的字符串,若属性值为空则显示“空值” 本方法支持对$/#开头的变量的替换，若在ComInput找不到变量参数则用“空值”替换！ 创建人 何占华 2008-07-05
	 * 
	 * @param str
	 *            <code>String</code>待替换字符串
	 * @param comInput
	 *            <code>ComInput</code>公共输入参数
	 * @return <code>String</code>替换完后的字符串值
	 * @throws 无
	 */
	public static String descXci(String str, HashMap<String, Object> comInput) {
		if (str == null || "".equals(str))
			return "";

		String sql = HZUtil.convertSQL(str, comInput, "[$]", 0, 2);
		return HZUtil.convertSQL(sql, comInput, "#", 1, 2);
	}

	/**
	 * 仅支持对#变量的替换
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
	 * 仅支持对$变量的替换
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
	 * a数组及b数组进行比较，得出a数组中有而b数组中无的元素、a数组中有且b数组中也有的元素、a数组中没有而b数组中有的元素 分别按顺序放入到返回的数字中 创建人 何占华 2008-08-13
	 * 
	 * @param last
	 *            <code>String[]</code>原先的数组-a数组
	 * @param cur
	 *            <code>String[]</code>现在的数组-b数组
	 * @return <code>String[][]</code>比较后的数组，第0个元素为a有b无，第1个元素为a有b有，第2个元素为a无b有；各元素均至少为0长度对象
	 * @throws 无
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
				if (last[i].equalsIgnoreCase(cur[j])) {//a和b都有
					sb2.append("," + cur[j]);
					tmp[j] = true;
					break;
				}
			}
			if (j == cur.length) {//a有b没有
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
	 * 进行字符串的批量替换 创建人 何占华 2008-07-05
	 * 
	 * @param src
	 *            <code>String</code>待替换字符串
	 * @param fnd
	 *            <code>String</code>待替换的子串
	 * @param rep
	 *            <code>String</code>替换的子串
	 * @return <code>String</code>替换完后的字符串值
	 * @throws 无
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
	 * 功能：将错误实例e的内容转变成字符串 创建人：何占华 创建日期：2008-07-25
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
	 * 功能：根据文件名判断是何种文件 创建人：何占华 创建日期：2008-08-15
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
		//将XML参数设置到ComInput中
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

				//加密 bylaisz 2011年12月02日
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
	 * 对字符串进行编码
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
	 * 对字符串进行解码
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
	 * 慧舟工具用从ROWSET获取XML字符串
	 * 
	 * @param rowSet
	 * @param hasXMLHead
	 * @return
	 * @throws Exception
	 */
	public static String getToolXMLData(RowSet rowSet, boolean hasXMLHead) throws Exception {
		int columnCount = rowSet.getMetaData().getColumnCount();//总列数
		String cell = "";
		StringBuffer xmlData = new StringBuffer(2048);
		xmlData.setLength(0);
		rowSet.beforeFirst();

		//开始构建xmldata
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
		// TODO 自动生成方法存根
		String s1 = escape("你好123也好!");
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
			throw new ParserException("XML文件或路径:"+path+"不存在！");
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
