package ctop.v3.msb.support.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

//存储文件的类
class FileAccess implements Serializable {
	private static final long serialVersionUID = 1L;
	RandomAccessFile saveFile; //要保存的文件
	long position;

	public FileAccess() throws IOException {
		this("", 0);
	}

	public FileAccess(String sname, long position) throws IOException {
		this.saveFile = new RandomAccessFile(sname, "rw"); //创建随机读取对象, 以 读/写的方式
		this.position = position;
		saveFile.seek(position); //设置指针位置
	}

	//将字符数据 写入文件
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
