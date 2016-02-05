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
        // 构造输出XML文件的字节输出流
        File outFile = new File(outFileName);
        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(outFile));
        // 构造一个XML编码器
        XMLEncoder xmlEncoder = new XMLEncoder(bos);
        // 使用XML编码器写对象
        xmlEncoder.writeObject(obj);
        // 关闭编码器
        xmlEncoder.close();

        return outFile.getAbsolutePath();
    }

    public static Object xml2Object(String inFileName)
            throws FileNotFoundException {
        // 构造输入的XML文件的字节输入流
        BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(inFileName));
        // 构造一个XML解码器
        XMLDecoder xmlDecoder = new XMLDecoder(bis);
        // 使用XML解码器读对象
        Object obj = xmlDecoder.readObject();
        // 关闭解码器
        xmlDecoder.close();

        return obj;
    }

}
