package top.catnies.firredismessenger.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ByteUtils {
    private ByteUtils() { }

    public static void writeString(DataOutputStream dos, String value) throws IOException {
        if (value == null) {
            dos.writeInt(-1); // 标记 null
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        dos.writeInt(bytes.length);
        dos.write(bytes);
    }

    public static String readString(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        if (length == -1) {
            return null;
        }
        byte[] buffer = new byte[length];
        dis.readFully(buffer);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static void writeStringArray(DataOutputStream dos, String[] array) throws IOException {
        if (array == null) {
            dos.writeInt(-1); // -1 表示 null
            return;
        }
        dos.writeInt(array.length); // 长度
        for (String s : array) {
            writeString(dos, s); // 每个元素可能为 null
        }
    }

    public static String[] readStringArray(DataInputStream dis) throws IOException {
        int length = dis.readInt();
        if (length == -1) {
            return null;
        }
        String[] array = new String[length];
        for (int i = 0; i < length; i++) {
            array[i] = readString(dis);
        }
        return array;
    }

}
