package ctop.v3.msb.support.file;

public class Log {
	//�߳�������Ϣ��ʾ����־��
	public Log() {
	}

	public static void sleep(int nsecond) {
		try {
			Thread.sleep(nsecond);
		} catch (Exception e) {
			System.out.println("�̳߳�˯");
		}
	}

	public static void log(String message) { //��ʾ��־��Ϣ
		System.err.println(message);
	}

	public static void log(int message) { //��ʾ��־��Ϣ
		System.err.println(message);
	}
}
