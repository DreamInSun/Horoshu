package ctop.v3.msb.support.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//扩展线程类,实现部分文件的抓取
class FileFetch extends Thread {

	/*========== Properties ==========*/
	/** 网址 */
	String webAddr;
	/** 开始位置 */
	long startPosition;
	/** 结束位置 */
	long endPosition;
	/** 线程编号 */
	int threadID;
	/** 下载结束 */
	boolean downLoadOver = false;
	/** 是否停止请求 */
	boolean isStopGet = false;
	/** 存储文件的类 */
	FileAccess fileAccessI = null;

	/*========= Constructor ==========*/
	public FileFetch(String surl, String sname, long startPosition, long endPosition, int threadID) throws IOException {
		this.webAddr = surl;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.threadID = threadID;
		this.fileAccessI = new FileAccess(sname, startPosition);
	}

	//实现线程的方法
	public void run() {
		while (startPosition < endPosition && !isStopGet) {
			try {
				URL url = new URL(webAddr); //根据网络资源创建URL对象
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //创建 打开的连接对象
				//设置描述发出的HTTP请求的终端的信息
				httpConnection.setRequestProperty("User-Agent", "NetFox");
				String sproperty = "byte=" + startPosition + "-";
				httpConnection.setRequestProperty("RANGE", sproperty);
				Log.log(sproperty);
				//获取 网络资源的输入流
				InputStream input = httpConnection.getInputStream();
				byte[] b = new byte[1024];
				int nRead;
				//循环将文件下载制定目录
				while ((nRead = input.read(b, 0, 1024)) > 0 && startPosition < endPosition && !isStopGet) {
					startPosition += fileAccessI.write(b, 0, nRead); //调用方法将内容写入文件
				}
				Log.log("线程\t" + (threadID + 1) + "\t结束....");
				downLoadOver = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//打印回应的头的信息
	public void logResponseHead(HttpURLConnection con) {
		for (int i = 1;; i++) {
			String header = con.getHeaderFieldKey(i); //循环答应回应的头信息
			if (header != null) {
				Log.log(header + ":" + con.getHeaderField(header));
			} else {
				break;
			}
		}
	}

	public void splitterStop() {
		isStopGet = true;
	}
}
