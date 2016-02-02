package ctop.v3.msb.support.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

//�洢�ļ�����
class FileAccess implements Serializable {
	private static final long serialVersionUID = 1L;
	RandomAccessFile saveFile; //Ҫ������ļ�
	long position;

	public FileAccess() throws IOException {
		this("", 0);
	}

	public FileAccess(String sname, long position) throws IOException {
		this.saveFile = new RandomAccessFile(sname, "rw"); //���������ȡ����, �� ��/д�ķ�ʽ
		this.position = position;
		saveFile.seek(position); //����ָ��λ��
	}

	//���ַ����� д���ļ�
	public synchronized int write(byte[] b, int start, int length) {
		int n = -1;
		try {
			saveFile.write(b, start, length);
			n = length;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n;
	}
}
