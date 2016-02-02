package ctop.v3.msb.support.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 扩展多线程类,负责文件的抓取,控制内部线程
 * 
 * @author DreamInSun
 * 
 */
class ControlFileFetch extends Thread {

	/*========== Properties ==========*/
	/** 扩展信息bean */
	TranBean tranBean = null;
	/** 开始位 */
	long[] startPosition;
	/** 结束位置 */
	long[] endPosition;
	/** 子线程对象 */
	FileFetch[] childThread;
	/** 文件长度 */
	long fileLength;
	/** 是否第一次去文件 */
	boolean isFitstGet = true;
	/** 停止标志 */
	boolean isStopGet = false;
	/** 文件下载的临时信息 */
	File fileName;
	/** 输出到文件的输出流 */
	DataOutputStream output;

	/*========== Constructor ==========*/
	public ControlFileFetch(TranBean tranBean) {
		this.tranBean = tranBean;
		fileName = new File(tranBean.getFileDir() + File.separator + tranBean.getFileName() + ".info"); //创建文件
		System.out.println(tranBean.getFileDir() + File.separator + tranBean.getFileName() + ".info");
		if (fileName.exists()) {
			isFitstGet = false;
			readInfo();
		} else {
			startPosition = new long[tranBean.getCount()];
			endPosition = new long[tranBean.getCount()];
		}
	}

	/*========== Thread : run ===========*/
	public void run() {
		try {
			if (isFitstGet) { //第一次读取文件
				fileLength = getFieldSize(); //调用方法获取文件长度
				if (fileLength == -1) {
					System.err.println("文件长度为止");
				} else if (fileLength == -2) {
					System.err.println("不能访问文件");
				} else {
					System.out.println("文件的长度:" + fileLength);
					//循环划分 每个线程要下载的文件的开始位置
					for (int i = 0; i < startPosition.length; i++) {
						startPosition[i] = (long) (i * (fileLength / startPosition.length));
					}
					//循环划分每个线程要下载的文件的结束位置
					for (int i = 0; i < endPosition.length - 1; i++) {
						endPosition[i] = startPosition[i + 1];
					}
					//设置最后一个 线程的下载 结束位置 文件的的长度
					endPosition[endPosition.length - 1] = fileLength;
				}
				//创建 子线程数量的数组
				childThread = new FileFetch[startPosition.length];
				for (int i = 0; i < startPosition.length; i++) {
					childThread[i] = new FileFetch(tranBean.getWebAddr(), tranBean.getFileDir() + File.separator + tranBean.getFileName(), startPosition[i], endPosition[i], i);
					Log.log("线程" + (i + 1) + ",的开始位置=" + startPosition[i] + ",结束位置=" + endPosition[i]);
					childThread[i].start();
				}
				boolean breakWhile = false;
				while (!isStopGet) {
					savePosition();
					Log.sleep(500);//
					breakWhile = true;
					for (int i = 0; i < startPosition.length; i++) { //循环实现下载文件
						if (!childThread[i].downLoadOver) {
							breakWhile = false;
							break;
						}
						if (breakWhile)
							break;
					}
					System.err.println("文件下载结束!");
				}
			}
		} catch (Exception e) {
			System.out.println("下载文件出错:" + e.getMessage());
		}
	}

	/*========== savePosition ==========*/
	/**
	 * 保存下载信息(文件指针信息)
	 */
	private void savePosition() {
		try {
			output = new DataOutputStream(new FileOutputStream(fileName));
			output.writeInt(startPosition.length);
			for (int i = 0; i < startPosition.length; i++) {
				output.writeLong(childThread[i].startPosition);
				output.writeLong(childThread[i].endPosition);
			}
			output.close();
		} catch (Exception e) {
			System.out.println("保存下载信息出错:" + e.getMessage());
		}
	}

	/*========== getFieldSize ==========*/
	/**
	 * 获得文件的长度
	 * 
	 * @return
	 */
	public long getFieldSize() {
		int fileLength = -1;
		try {
			URL url = new URL(tranBean.getWebAddr()); //根据网址传入网址创建URL对象
			//打开连接对象
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			//设置描述发出HTTP请求的终端信息
			httpConnection.setRequestProperty("User-Agent", "NetFox");
			int responseCode = httpConnection.getResponseCode();
			//表示不能访问文件
			if (responseCode >= 400) {
				errorCode(responseCode);
				return -2;
			}
			String head;
			for (int i = 1;; i++) {
				head = httpConnection.getHeaderFieldKey(i); //获取文件头部信息
				if (head != null) {
					if (head.equals("Content-Length")) { //根据头部信息获取文件长度
						fileLength = Integer.parseInt(httpConnection.getHeaderField(head));
						break;
					}
				} else {
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("获取文件长度出错:" + e.getMessage());
		}
		Log.log("文件长度" + fileLength);
		return fileLength;
	}

	/*========== errorCode ==========*/
	private void errorCode(int errorCode) {
		System.out.println("错误代码:" + errorCode);
	}

	/*========== readInfo ==========*/
	/**
	 * 读取文件指针位置
	 */
	private void readInfo() {
		try {
			//创建数据输出流
			DataInputStream input = new DataInputStream(new FileInputStream(fileName));
			int count = input.readInt(); //读取分成的线程下载个数
			startPosition = new long[count]; //设置开始线程
			endPosition = new long[count]; //设置结束线程
			for (int i = 0; i < startPosition.length; i++) {
				startPosition[i] = input.readLong(); //读取每个线程的开始位置
				endPosition[i] = input.readLong(); //读取每个线程的结束位置
			}
			input.close();
		} catch (Exception e) {
			System.out.println("读取文件指针位置出错:" + e.getMessage());
		}
	}
}
