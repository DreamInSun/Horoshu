package ctop.v3.msb.common.urn;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * MSB Domain Contains
 * 
 * @author DreamInSun
 * 
 */
public class MsbDomain implements Externalizable {
	/*========== Constant ===========*/
	private static final String ROOT_DOMAIN = "00000000";
	private static final int DOMAIN_MAX_LENTH = 8;
	private static final int DOMAIN_STEP = 2;
	private static final int DOMAIN_PART_LEN = DOMAIN_MAX_LENTH / DOMAIN_STEP;

	/*========== Properties ==========*/
	public String m_Domain;
	private String[] parts = new String[DOMAIN_PART_LEN];
	private int level = 0;

	/*==========  ==========*/
	@Deprecated
	public MsbDomain() {
	}

	public MsbDomain(String domain) {
		this.parseDomain(domain);
	}

	private void parseDomain(String domain) {
		m_Domain = domain.substring(0, DOMAIN_MAX_LENTH);
		/* Judge Level */
		parts = splitDomain(domain);
		level = 0;
		for (String str : parts) {
			if (!str.equals("00")) {
				level++;
			}
		}
	}

	public int matcheLevel(String domain) {
		int i = 0;
		String[] tmpString = splitDomain(domain);
		for (; i < DOMAIN_PART_LEN; i++) {
			if (!tmpString[i].equals(parts[i])) {
				break;
			}
		}
		return i;
	}

	public int matcheLevel(MsbDomain domain) {
		int i = 0;
		String[] tmpString = domain.getParts();
		for (; i < DOMAIN_PART_LEN; i++) {
			if (!tmpString[i].equals(parts[i])) {
				break;
			}
		}
		return i;
	}

	private final String[] splitDomain(String domain) {
		String[] tmpString = new String[DOMAIN_PART_LEN];
		for (int i = 0; i < DOMAIN_PART_LEN; i++) {
			tmpString[i] = m_Domain.substring((i * DOMAIN_STEP), ((i + 1) * DOMAIN_STEP));

		}
		return tmpString;
	}

	/*========== Getter & Setter ==========*/
	public int getLevel() {
		return level;
	}

	public String[] getParts() {
		return parts;
	}

	public void setParts(String[] parts) {
		this.parts = parts;
	}

	public String getParentDomain() {
		StringBuilder tmpSb = new StringBuilder(ROOT_DOMAIN);
		if (level > 1) {
			int parentLen = (level - 1) * DOMAIN_STEP;
			tmpSb.replace(0, parentLen, m_Domain.substring(0, parentLen));
		}
		return tmpSb.toString();
	}

	public boolean isChildDomainOf(MsbDomain domain) {
		boolean tmpDomain = false;
		String[] domainParts = domain.getParts();
		/* Check Level */
		int level = domain.getLevel();
		if (this.level == level + 1) {
			/* Check Domain Parts */
			if (domain != null) {
				int matchParts = 0;
				for (; matchParts < DOMAIN_PART_LEN; matchParts++) {
					if (!this.parts[matchParts].equals(domainParts[matchParts])) {
						break;
					}
				}
				tmpDomain = (matchParts == level);
			}
		}
		return tmpDomain;
	}

	/*========== Externalizable ==========*/
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(this.m_Domain);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		String domain = in.readUTF();
		try {
			this.parseDomain(domain);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*==========  ===========*/
	@Override
	public String toString() {
		return m_Domain;
	}

	/*========== Object : HashCode ==========*/
	/**
	 * Assurance MSBURN Hash Code the Same as String MsbUrn, but in the same format will get the same value.
	 */
	@Override
	public int hashCode() {
		return m_Domain.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (!(obj instanceof MsbDomain)) {
			return false;
		}
		return ((MsbDomain) obj).m_Domain.equals(this.m_Domain);
	}
}
