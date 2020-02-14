package com.xust.nio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author: Victor
 * @create: 2020-02-06 16:20
 **/

public class NIOFileChannel01 {
    public static void main(String[] args) throws IOException {
        //创建一个输出流
        FileOutputStream fileOutputStream = new FileOutputStream("/users/a/desktop/a.txt");
        //获取通道
        FileChannel channel = fileOutputStream.getChannel();
        //创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        //放值
        byteBuffer.put("hello".getBytes());
        //刷新
        byteBuffer.flip();

        channel.write(byteBuffer);

        fileOutputStream.close();
    }
}
