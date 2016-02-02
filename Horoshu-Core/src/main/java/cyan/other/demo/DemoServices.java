package cyan.other.demo;

import com.hztech.util.ComInput;
import com.hztech.util.ComOutput;
import com.hztech.util.RSUtil;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class DemoServices {

	public static ComOutput reverseString(ComInput ci) {
		String strIn = ci.getStrPm("Load");
		strIn = new StringBuffer(strIn).reverse().toString();
		return RSUtil.setRetValue(strIn);
	}

	public static ComOutput toUpperCase(ComInput ci) {
		String strIn = ci.getStrPm("Load");
		strIn = strIn.toUpperCase();
		return RSUtil.setRetValue(strIn);
	}

	public static ComOutput toLowerCase(ComInput ci) {
		String strIn = ci.getStrPm("Load");
		strIn = strIn.toLowerCase();
		return RSUtil.setRetValue(strIn);
	}

	public static ComOutput toBase64(ComInput ci) {
		String strIn = ci.getStrPm("Load");
		String strOut = Base64.encode(strIn.getBytes()).toString();
		return RSUtil.setRetValue(strOut);
	}
}
