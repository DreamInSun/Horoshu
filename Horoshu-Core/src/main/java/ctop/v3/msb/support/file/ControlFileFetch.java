package ctop.v3.msb.support.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ��չ���߳���,�����ļ���ץȡ,�����ڲ��߳�
 * 
 * @author DreamInSun
 * 
 */
class ControlFileFetch extends Thread {

	/*========== Properties ==========*/
	/** ��չ��Ϣbean */
	TranBean tranBean = null;
	/** ��ʼλ */
	long[] startPosition;
	/** ����λ�� */
	long[] endPosition;
	/** ���̶߳��� */
	FileFetch[] childThread;
	/** �ļ����� */
	long fileLength;
	/** �Ƿ��һ��ȥ�ļ� */
	boolean isFitstGet = true;
	/** ֹͣ��־ */
	boolean isStopGet = false;
	/** �ļ����ص���ʱ��Ϣ */
	File fileName;
	/** ������ļ�������� */
	DataOutputStream output;

	/*========== Constructor ==========*/
	public ControlFileFetch(TranBean tranBean) {
		this.tranBean = tranBean;
		fileName = new File(tranBean.getFileDir() + File.separator + tranBean.getFileName() + ".info"); //�����ļ�
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
			if (isFitstGet) { //��һ�ζ�ȡ�ļ�
				fileLength = getFieldSize(); //���÷�����ȡ�ļ�����
				if (fileLength == -1) {
					System.err.println("�ļ�����Ϊֹ");
				} else if (fileLength == -2) {
					System.err.println("���ܷ����ļ�");
				} else {
					System.out.println("�ļ��ĳ���:" + fileLength);
					//ѭ������ ÿ���߳�Ҫ���ص��ļ��Ŀ�ʼλ��
					for (int i = 0; i < startPosition.length; i++) {
						startPosition[i] = (long) (i * (fileLength / startPosition.length));
					}
					//ѭ������ÿ���߳�Ҫ���ص��ļ��Ľ���λ��
					for (int i = 0; i < endPosition.length - 1; i++) {
						endPosition[i] = startPosition[i + 1];
					}
					//�������һ�� �̵߳����� ����λ�� �ļ��ĵĳ���
					endPosition[endPosition.length - 1] = fileLength;
				}
				//���� ���߳�����������
				childThread = new FileFetch[startPosition.length];
				for (int i = 0; i < startPosition.length; i++) {
					childThread[i] = new FileFetch(tranBean.getWebAddr(), tranBean.getFileDir() + File.separator + tranBean.getFileName(), startPosition[i], endPosition[i], i);
					Log.log("�߳�" + (i + 1) + ",�Ŀ�ʼλ��=" + startPosition[i] + ",����λ��=" + endPosition[i]);
					childThread[i].start();
				}
				boolean breakWhile = false;
				while (!isStopGet) {
					savePosition();
					Log.sleep(500);//
					breakWhile = true;
					for (int i = 0; i < startPosition.length; i++) { //ѭ��ʵ�������ļ�
						if (!childThread[i].downLoadOver) {
							breakWhile = false;
							break;
						}
						if (breakWhile)
							break;
					}
					System.err.println("�ļ����ؽ���!");
				}
			}
		} catch (Exception e) {
			System.out.println("�����ļ�����:" + e.getMessage());
		}
	}

	/*========== savePosition ==========*/
	/**
	 * ����������Ϣ(�ļ�ָ����Ϣ)
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
			System.out.println("����������Ϣ����:" + e.getMessage());
		}
	}

	/*========== getFieldSize ==========*/
	/**
	 * ����ļ��ĳ���
	 * 
	 * @return
	 */
	public long getFieldSize() {
		int fileLength = -1;
		try {
			URL url = new URL(tranBean.getWebAddr()); //������ַ������ַ����URL����
			//�����Ӷ���
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			//������������HTTP������ն���Ϣ
			httpConnection.setRequestProperty("User-Agent", "NetFox");
			int responseCode = httpConnection.getResponseCode();
			//��ʾ���ܷ����ļ�
			if (responseCode >= 400) {
				errorCode(responseCode);
				return -2;
			}
			String head;
			for (int i = 1;; i++) {
				head = httpConnection.getHeaderFieldKey(i); //��ȡ�ļ�ͷ����Ϣ
				if (head != null) {
					if (head.equals("Content-Length")) { //����ͷ����Ϣ��ȡ�ļ�����
						fileLength = Integer.parseInt(httpConnection.getHeaderField(head));
						break;
					}
				} else {
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("��ȡ�ļ����ȳ���:" + e.getMessage());
		}
		Log.log("�ļ�����" + fileLength);
		return fileLength;
	}

	/*========== errorCode ==========*/
	private void errorCode(int errorCode) {
		System.out.println("�������:" + errorCode);
	}

	/*========== readInfo ==========*/
	/**
	 * ��ȡ�ļ�ָ��λ��
	 */
	private void readInfo() {
		try {
			//�������������
			DataInputStream input = new DataInputStream(new FileInputStream(fileName));
			int count = input.readInt(); //��ȡ�ֳɵ��߳����ظ���
			startPosition = new long[count]; //���ÿ�ʼ�߳�
			endPosition = new long[count]; //���ý����߳�
			for (int i = 0; i < startPosition.length; i++) {
				startPosition[i] = input.readLong(); //��ȡÿ���̵߳Ŀ�ʼλ��
				endPosition[i] = input.readLong(); //��ȡÿ���̵߳Ľ���λ��
			}
			input.close();
		} catch (Exception e) {
			System.out.println("��ȡ�ļ�ָ��λ�ó���:" + e.getMessage());
		}
	}
}
