package com.xust.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author: Victor
 * @create: 2020-02-06 16:33
 **/

public class NIOFileChannel02 {
    public static void main(String[] args) throws IOException {
        File file = new File("/users/a/desktop/摄像头");

        FileInputStream fileInputStream = new FileInputStream(file);

        FileChannel channel = fileInputStream.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate((int)file.length());

        channel.read(byteBuffer);

        byteBuffer.flip();

        System.out.print(new String(byteBuffer.array()));

        fileInputStream.close();
    }
}
