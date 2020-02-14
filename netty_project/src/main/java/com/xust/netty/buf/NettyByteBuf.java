package com.xust.netty.buf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

/**
 * @author: Victor
 * @create: 2020-02-12 21:00
 **/

public class NettyByteBuf {
    public static void main(String[] args) {
        ByteBuf byteBuf = Unpooled.copiedBuffer("hello,world!", Charset.forName("utf-8"));
        if (byteBuf.hasArray()) {

            byte[] content = byteBuf.array();
            System.out.println(new String(content, Charset.forName("utf-8")));
            System.out.println("bytebuf  =" + byteBuf );
            System.out.println(byteBuf.arrayOffset());  //0
            System.out.println(byteBuf.readerIndex());  //0
            System.out.println(byteBuf.writerIndex());  //12
            System.out.println(byteBuf.capacity());     //36
            int len = byteBuf.readableBytes();
            System.out.println(len);
            for (int i = 0; i < len; i++) {
                System.out.println(byteBuf.getByte(i));
            }
        }
    }
}
