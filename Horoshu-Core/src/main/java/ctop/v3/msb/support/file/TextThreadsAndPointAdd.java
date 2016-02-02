package ctop.v3.msb.support.file;

/**
 * ���̶߳ϵ����� ���� (HTTP)
 * 
 * @author Bin Windows NT 6.1
 */
public class TextThreadsAndPointAdd {

	/*========== Constructor ==========*/
	public TextThreadsAndPointAdd(String webAddr, String fileDir, String fileName, int count) {
		try {
			TranBean bean = new TranBean(webAddr, fileDir, fileName, count);
			ControlFileFetch fileFetch = new ControlFileFetch(bean);
			fileFetch.start();
		} catch (Exception e) {
			System.out.println("���߳������ļ�����:" + e.getMessage());
			System.exit(1);
		}
	}

	/*========== Main ==========*/
	public static void main(String[] args) {
		String webAddr = "http://nchc.dl.sourceforge.net/project/jtidy/JTidy/r938/jtidy-r938.zip";
		String fileDir = "F:/temp";
		String fileName = "ss.zip";
		int count = 5;
		new TextThreadsAndPointAdd(webAddr, fileDir, fileName, count);
	}

}
