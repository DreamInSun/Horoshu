package ctop.v3.msb.common.urn;

import java.io.Serializable;

/**
 * MsbUrn Version <br />
 * 1.1.R <br/>
 * Version stipulations defined as follow:<br/>
 * Major Version : Modifications not support 'Backward Compatibility'. <br />
 * Minor Version : Modifications support 'Backward Compatibility'. <br />
 * Patch Version : We dispose patch version on distribute version. <br />
 * Build Version : We dispose build version on distribute version. <br />
 * <br />
 * Version Type : <br />
 * Alpha='A'(Developing Prototype)<br />
 * Beta='B'( Testing Prototype)<br />
 * Candidate='C'( Release Candidate)<br />
 * Release='R'( Release Officially)<br />
 * 
 * @author DreamInSun
 * 
 */
public class MsbUrnVer implements Comparable<MsbUrnVer>, Serializable {
	private static final long serialVersionUID = 1L;

	/*========== Constant ==========*/
	/** Version Type : Developing Prototype */
	public static char ALPHA = 'A';
	/** Version Type : Testing Prototype */
	public static char BETA = 'B';
	/** Version Type : Release Candidate */
	public static char CANDIDATE = 'C';
	/** Version Type : Release Officially */
	public static char RELEASE = 'R';
	/** Version Type : Final Version never change any more */
	public static char FINAL = 'F';

	/*========== Properties ==========*/
	private static StringBuilder g_sb = new StringBuilder(32);

	/*========== Properties ==========*/
	private String m_str;
	private int majorVer;
	private int minorVer;
	private char typeVer;

	/*========== Constant ==========*/
	public MsbUrnVer() {
		this(0, 0, MsbUrnVer.CANDIDATE);
	}

	public MsbUrnVer(int major) {
		this(major, 0);
	}

	public MsbUrnVer(int major, int minor) {
		this(major, minor, MsbUrnVer.RELEASE);
	}

	public MsbUrnVer(int major, int minor, char type) {
		majorVer = major;
		minorVer = minor;
		typeVer = Character.toUpperCase(type);
		updateStr();
	}

	private void updateStr() {
		g_sb.delete(0, g_sb.length());
		m_str = g_sb.append(majorVer).append('.').append(minorVer).append('.').append(typeVer).toString();
	}

	/*========== Getter & Setter ==========*/
	public int getMajorVer() {
		return majorVer;
	}

	public void setMajorVer(int majorVer) {
		this.majorVer = majorVer;
		updateStr();
	}

	public int getMinorVer() {
		return minorVer;
	}

	public void setMinorVer(int minorVer) {
		this.minorVer = minorVer;
		updateStr();
	}

	public char getTypeVer() {
		return typeVer;
	}

	public void setTypeVer(char typeVer) {
		this.typeVer = typeVer;
		updateStr();
	}

	/*========== Parser ===========*/
	public static MsbUrnVer parse(String strVerion) {
		MsbUrnVer tmpRet = null;
		try {
			String tmp[] = strVerion.split("[.]");
			if (tmp.length == 3) {
				tmpRet = new MsbUrnVer(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), tmp[2].charAt(0));
			} else {
				tmpRet = new MsbUrnVer(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]));
			}
		} catch (Exception exp) {
			// Nothing todo just return a default Version Object
		}
		return tmpRet;
	}

	/*==========  ==========*/
	@Override
	public String toString() {
		return m_str;
	}

	/*========== INterface : Comparable ==========*/
	@Override
	public int compareTo(MsbUrnVer o) {
		int major = this.majorVer - o.majorVer;
		if (major != 0) {
			return major * 1024;
		} else {
			int minor = this.minorVer - o.minorVer;
			return minor;
		}
	}
}
