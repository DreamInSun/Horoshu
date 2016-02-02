package ctop.v3.msb.support.file;

//���䱣����Ϣ����
class TranBean {

	/*========== Properties ==========*/
	/** ���ص�ַ */
	private String webAddr;
	/** ���ص�ָ����Ŀ¼ */
	private String fileDir;
	/** ���غ��ļ��������� */
	private String fileName;
	/** �ļ��ּ����߳�����, Ĭ��Ϊ 3�� */
	private int count;

	/*========= Constructor ==========*/
	/**
	 * Ĭ�ϵĹ��췽��
	 */
	public TranBean() {
		this("", "", "", 3);
	}

	/**
	 * �������Ĺ��췽��
	 * 
	 * @param webAddr
	 * @param fileDir
	 * @param fileName
	 * @param count
	 */
	public TranBean(String webAddr, String fileDir, String fileName, int count) {
		this.webAddr = webAddr;
		this.fileDir = fileDir;
		this.fileName = fileName;
		this.count = count;
	}

	/*========== Getter & Setter ==========*/
	public String getWebAddr() {
		return webAddr;
	}

	public void setWebAddr(String webAddr) {
		this.webAddr = webAddr;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
