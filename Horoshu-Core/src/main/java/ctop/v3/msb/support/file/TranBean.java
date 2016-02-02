package ctop.v3.msb.support.file;

//传输保存信息的类
class TranBean {

	/*========== Properties ==========*/
	/** 下载地址 */
	private String webAddr;
	/** 下载到指定的目录 */
	private String fileDir;
	/** 下载后文件的新名字 */
	private String fileName;
	/** 文件分几个线程下载, 默认为 3个 */
	private int count;

	/*========= Constructor ==========*/
	/**
	 * 默认的构造方法
	 */
	public TranBean() {
		this("", "", "", 3);
	}

	/**
	 * 带参数的构造方法
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
