package ctop.v3.msb.support.file;

public class Log {
	//线程运行信息显示的日志类
	public Log() {
	}

	public static void sleep(int nsecond) {
		try {
			Thread.sleep(nsecond);
		} catch (Exception e) {
			System.out.println("线程沉睡");
		}
	}

	public static void log(String message) { //显示日志信息
		System.err.println(message);
	}

	public static void log(int message) { //显示日志信息
		System.err.println(message);
	}
}
