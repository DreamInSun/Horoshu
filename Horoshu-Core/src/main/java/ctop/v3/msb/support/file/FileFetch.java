package ctop.v3.msb.support.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//��չ�߳���,ʵ�ֲ����ļ���ץȡ
class FileFetch extends Thread {

	/*========== Properties ==========*/
	/** ��ַ */
	String webAddr;
	/** ��ʼλ�� */
	long startPosition;
	/** ����λ�� */
	long endPosition;
	/** �̱߳�� */
	int threadID;
	/** ���ؽ��� */
	boolean downLoadOver = false;
	/** �Ƿ�ֹͣ���� */
	boolean isStopGet = false;
	/** �洢�ļ����� */
	FileAccess fileAccessI = null;

	/*========= Constructor ==========*/
	public FileFetch(String surl, String sname, long startPosition, long endPosition, int threadID) throws IOException {
		this.webAddr = surl;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.threadID = threadID;
		this.fileAccessI = new FileAccess(sname, startPosition);
	}

	//ʵ���̵߳ķ���
	public void run() {
		while (startPosition < endPosition && !isStopGet) {
			try {
				URL url = new URL(webAddr); //����������Դ����URL����
				HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //���� �򿪵����Ӷ���
				//��������������HTTP������ն˵���Ϣ
				httpConnection.setRequestProperty("User-Agent", "NetFox");
				String sproperty = "byte=" + startPosition + "-";
				httpConnection.setRequestProperty("RANGE", sproperty);
				Log.log(sproperty);
				//��ȡ ������Դ��������
				InputStream input = httpConnection.getInputStream();
				byte[] b = new byte[1024];
				int nRead;
				//ѭ�����ļ������ƶ�Ŀ¼
				while ((nRead = input.read(b, 0, 1024)) > 0 && startPosition < endPosition && !isStopGet) {
					startPosition += fileAccessI.write(b, 0, nRead); //���÷���������д���ļ�
				}
				Log.log("�߳�\t" + (threadID + 1) + "\t����....");
				downLoadOver = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	//��ӡ��Ӧ��ͷ����Ϣ
	public void logResponseHead(HttpURLConnection con) {
		for (int i = 1;; i++) {
			String header = con.getHeaderFieldKey(i); //ѭ����Ӧ��Ӧ��ͷ��Ϣ
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
