package cyan.core.convert;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;

/**
 * Created by DreamInSun on 2016/2/5.
 */
public class XmlConvertor {
    public static String object2XML(Object obj, String outFileName)
            throws FileNotFoundException {
        // �������XML�ļ����ֽ������
        File outFile = new File(outFileName);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(outFile));
        // ����һ��XML������
        XMLEncoder xmlEncoder = new XMLEncoder(bos);
        // ʹ��XML������д����
        xmlEncoder.writeObject(obj);
        // �رձ�����
        xmlEncoder.close();

        return outFile.getAbsolutePath();
    }

    public static Object xml2Object(String inFileName)
            throws FileNotFoundException {
        // ���������XML�ļ����ֽ�������
        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(inFileName));
        // ����һ��XML������
        XMLDecoder xmlDecoder = new XMLDecoder(bis);
        // ʹ��XML������������
        Object obj = xmlDecoder.readObject();
        // �رս�����
        xmlDecoder.close();

        return obj;
    }

}
