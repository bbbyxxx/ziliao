package com.xust.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author: Victor
 * @create: 2020-02-06 17:13
 **/

public class NIOFileChannel04 {
    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("/users/a/desktop/she");
        FileOutputStream fileOutputStream = new FileOutputStream("/users/a/desktop/he");

        FileChannel source = fileInputStream.getChannel();
        FileChannel dest = fileOutputStream.getChannel();

        dest.transferFrom(source,0, source.size());

        source.close();
        dest.close();
        fileInputStream.close();
        fileOutputStream.close();
    }
}
