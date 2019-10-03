package cn.yjl.game.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IoUtil {

    public static Object readObjectInputStream(InputStream is) {
        Object ret = null;
        try (ObjectInputStream ois = new ObjectInputStream(is)) {
            ret = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void writeObjectInputStream(OutputStream os, Object obj) {
        try (ObjectOutputStream oos = new ObjectOutputStream(os)) {
            oos.writeObject(obj);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> readInputStream(InputStream is) {
        return readInputStream(is, Charset.defaultCharset().name());
    }

    public static List<String> readInputStream(InputStream is, String charset) {
        try {
            String content = new String(readBytesInputStream(is), charset);
            return Stream.of(content.split("(\r?\n|\r\n?)")).collect(Collectors.toList());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static byte[] readBytesInputStream(InputStream is) {
        byte[] byteArray = new byte[0];
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            List<Byte> bytes = new ArrayList<Byte>();
            int character = -1;
            while ((character = bis.read()) != -1) {
                bytes.add((byte) character);
            }
            byteArray = new byte[bytes.size()];
            int index = 0;
            while (index < bytes.size()) {
                byteArray[index] = bytes.get(index++);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {

            }
        }
        return byteArray;
    }

    public static List<String> readFile(String filePath) {

        List<String> ret = new ArrayList<String>();
        try (FileReader fr = new FileReader(filePath); BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                ret.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
