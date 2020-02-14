package com.xust.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author: Victor
 * @create: 2020-02-06 16:45
 **/

public class NIOFileChannel03 {
    public static void main(String[] args) throws IOException {
        File file = new File("/users/a/desktop/摄像头");

        FileInputStream fileInputStream = new FileInputStream(file);

        FileChannel channel1 = fileInputStream.getChannel();

        FileOutputStream fileOutputStream = new FileOutputStream("/users/a/desktop/she");

        FileChannel channel2 = fileOutputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());

        while (true) {
            byteBuffer.clear();
            int read = channel1.read(byteBuffer);
            if (read == -1) {
                break;
            }
            byteBuffer.flip();
            channel2.write(byteBuffer);
        }
        fileInputStream.close();
        fileOutputStream.close();

    }
}
