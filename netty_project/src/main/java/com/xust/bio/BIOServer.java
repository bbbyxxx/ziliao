package com.xust.bio;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Victor
 * @create: 2020-02-06 11:50
 **/

public class BIOServer {

    public static void handler(Socket socket) {
        System.out.println("线程  id=" + Thread.currentThread().getId()+ "名字=" + Thread.currentThread().getName());
        byte[] bytes = new byte[1024];
        try {
            InputStream inputStream = socket.getInputStream();
            while (true) {
                System.out.println("等待数据。。。。");
                int read = inputStream.read(bytes);
                if (read != -1) {
                    System.out.println("Thread" + Thread.currentThread().getId());
                    System.out.println(new String(bytes, 0, read));
                }else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("关闭和client的连接");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //线程池机制
        ExecutorService CachedThreadPool = Executors.newCachedThreadPool();

        ServerSocket serverSocket = new ServerSocket(6666);
        System.out.println("服务器启动了。。。");
        while (true) {
            System.out.println("等待连接。。。");
            final Socket socket = serverSocket.accept();
            System.out.println("连接一个客户端");
            CachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    handler(socket);
                }
            });
        }
    }
}
