#  Netty总结

##  介绍

1. Netty是一个异步的、基于事件驱动的网络应用框架，用以快速开发高性能、高可靠性的网络IO程序

2. Netty主要针对在TCP协议下，面向Clients端的高并发应用，或者Peer to Peer场景下的大量数据持续传输的应用

3. Netty本质是一个NIO框架，适用于服务器通讯相关的多种应用场景

4. Netty模型

   ![Netty模型](/Users/a/Desktop/Java-victor/images/Netty模型.png)

##  应用场景

1. 互联网行业：在分布式系统中，各个节点之间需要远程服务调用，高性能的RPC框架必不可少，Netty作为异步高性能的通信框架，往往作为基础通信组件被这些RPC框架使用。
2. 典型的应用有：阿里分布式服务框架Dubbo的RPC框架使用Dubbo协议进行节点间通信，Dubbo协议默认使用Netty作为基础通信组件，用于实现各进程节点之间的内部通信。
3. 大数据领域：经典的Hadoop的高性能通信和序列化组件（AVRO实现数据文件共享）的RPC框架，默认采用Netty进行跨界点通信。

##  I/O模型基本说明

1. I/O模型简单的理解：就是用什么样的通道进行数据的发送和接收，很大程度上决定了程序通信的性能。
2. Java共支持3种网络编程模型I/O模式：BIO、NIO、AIO。
3. Java BIO：同步并阻塞（传统阻塞型），服务器实现模式为一个连接一个线程，即客户端有连续请求时服务端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的开销，可以通过线程池机制改善（实现多个客户连接服务器）。适用于连接数目较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中。
4. Java NIO：同步并阻塞，服务器实现模式为一个线程处理多个请求（连接），即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求就进行处理。适用于连接数亩多且连接比较短（轻操作）的架构，比如聊天服务器、弹幕系统、服务器间通讯等等。
5. Java AIO：异步并阻塞，AIO引入异步通道的概念，采用了Proactor模式，简化了程序的编写，有效的请求才启动线程，它的特点是先由操作系统完成后才通知服务端程序启动线程去处理，一般适用于连接数较多且持续时间较长的应用。适用于连接数目比较多且连接比较长（重操作）的架构，比如相册服务器，充分调用OS参与并发操作。

###  BIO

####  BIO编程简单流程

1. 服务器端启动一个ServerSocket
2. 客户端启动Socket对服务器进行通信，默认情况下服务器需要对每个客户建立一个线程与之通讯
3. 客户端发出请求后，先咨询服务器是否有线程响应，如果没有则会等待，或者被拒绝
4. 如果有响应，客户端线程会等待请求结束后，再继续执行（会有可能阻塞）

####  BIO问题分析

- 每个请求都需要创建独立的线程，与对应的客户端进行数据Read、业务处理、数据Write
- 当并发数较大时，需要创建大量线程来处理连接，系统资源占用较大
- 连接建立后，如果当前线程暂时没有数据可读，则线程就阻塞在Read操作上，造成线程资源浪费

####  BIO实战

```java
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
//得出结论，确实是一个Client连接就会新建一个线程。
```

###  NIO

####  基本介绍

- Java NIO全程Java Non-blocking IO，是指JDK提供的新API。从JDK1.4开始，Java提供了一系列改进的输入/输入的新特性，被统称为NIO（New IO），是同步非阻塞的。
- NIO相关类都被放在java.nio包及子包下，并且对原java.io包中的很多类进行改写。
- NIO有三大核心部分：Channel（通道）、Buffer（缓冲区）、Selector（选择器）
- NIO是面向缓冲区，或者面向块编程的。数据读取到一个它稍后处理的缓冲区，需要时可在缓冲区中前后移动，这就是增加了处理过程中的灵活性，使用它可以提供非阻塞式的高伸缩性网络。
- Java NIO的非阻塞模式，使一个线程从某通道发送请求或者读取数据，但是它仅能得到目前可用的数据，如果目前没有数据可用时，就什么都不会读取，而不是**保持线程阻塞**，所以直至数据变得可读之前，该线程可以做其它的事情。非阻塞写也是如此，一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程同时可以去做别的事情。
- NIO是可以做到用一个线程来处理多个操作的，假设有10000个请求过来，根据实际情况，可以分配50或者100个线程来处理。不像之前的BIO那样，非得分配10000个。
- HTTP2.0使用了多路复用的技术，做到同一个连接并发处理多个请求，而且并发请求的数量比HTTP1.1大了好几个数量级。

####  NIO与BIO的区别

- BIO是以流的方式处理数据，而NIO以块的方式处理数据，块I/O的效率比流I/O高很多
- BIO是阻塞的，NIO则是非阻塞的
- BIO基于字节流和字符流进行操作，而NIO基于Channel和Buffer进行操作，数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中。Selector用于监听多个通道的时间，因此使用单线程就是可以监听多个客户端通道
- BIO要么是输入流，要么是输出流，不能双向，但是NIO的Buffer可以读也可以写，需要flip方法切换

### AIO

- JDK 7引入Asynchronous I/O，即AIO。在进行I/O编程中，常用到两种模式：Reactor和Proactor。Java的NIO就是Reactor，当有事件触发时，服务器端得到通知，进行相应的处理。
- AIO即NIO2.0，叫做异步不阻塞的IO。AIO引入异步通道的概念，采用了Proactor模式，简化了程序编写，有效的请求才启动线程，它的特点是先由操作系统完成后才通知服务端程序启动线程去处理，一般适用于连接数较多且连接时间较长的应用。

###  BIO、NIO、AIO对比

|  IO模型  | BIO（同步阻塞） | NIO（同步非阻塞） | AIO（异步非阻塞） |
| :------: | :-------------: | :---------------: | :---------------: |
| 编程难度 |      简单       |       复杂        |       复杂        |
|  可靠性  |       差        |        好         |        好         |
|  吞吐量  |       低        |        高         |        高         |

举例说明：

1. 同步阻塞：到理发店理发，就一直等理发师，直到轮到自己理发。
2. 同步非阻塞：到理发店理发，发现前面有人，给理发师说下，先干其它事情，一会过来看是否轮到自己。
3. 异步非阻塞：给理发师打电话，让理发师上门服务，自己干其它事情，理发师自己来家给你理发。

##  Netty三大组件

![Netty三大组件](/Users/a/Desktop/Java-victor/images/Netty/Netty三大组件.png)

- 每个Channel都会对应一个Buffer
- Selector对应一个线程，一个线程对应多个Channel
- 程序切换到哪个Selector是由事件决定的，Event就是一个重要的概念
- Selector会根据不同的时间，在各个通道上切换
- Buffer就是一个内存块，底层是一个数组
- 数据的读取写入是通过Buffer
- Channel是双向的，可以返回底层操作系统的情况

###  Buffer

缓冲区（Buffer)：本质上是一个可以读写数据的内存卡，可以理解成是一个容器对象，该对象提供了一组方法，可以更轻松地使用内存块，缓冲区对象内置了一些机制，能够跟踪和记录缓冲区的状态变化情况。Channel提供从文件、网络读取数据的渠道，但是读取或写入的数据都必须经由Buffer。 

###  Channel

1. NIO的通道类似于流，但有些区别如下
   - 通道可以同时进行读写，而流只能读或者只能写
   - 通道可以实现异步读写数据
   - 通道可以从缓冲读数据，也可以写数据到缓冲
   
2. BIO中的Stream是单向的，例如FileInputStream对象只能进行读取数据的操作，而NIO的通道Channel是双向的，可以读也可以写

3. Channel是一个接口，常见的Channel类有：FielChannel（文件）、DatagramChannel（UDP）、ServerSocketChannel和SocketChannel （后面两个用于TCP的数据读写）

   ```java
   public class NIOFileChannelDemo {
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
   
   ```

   

####  关于Buffer和Channel的注意事项和细节

1. ByteBuffer支持类型化的put和get，put放入的是什么数据类型，get就应该使用相应的数据类型来取出，否则可能有BufferUnderflowException。

2. 可以讲一个普通Buffer转成只读Buffer。

3. NIO还提供了MappedByteBuffer，可以让文件直接在内存中进行修改，而如何到文件由NIO来完成。

   ```java
   public class MappedByteBufferTest {
       public static void main(String[] args) throws IOException {
           RandomAccessFile randomAccessFile = new RandomAccessFile("/users/a/desktop/he","rw");
           FileChannel fileChannel = randomAccessFile.getChannel();
           MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
           mappedByteBuffer.put(0,(byte) 'h');
   
       }
   }
   ```

   

###  Selector

- Selector能够检测到多个注册的通道上是否有事件发生（注意：多个Channel以事件的方式可以注册到同一个selector），如果有事件发生，便获取事件然后针对每个事件进行相应的处理。这样就可以只用一个单线程去管理多个通道，也就是管理多个连接和请求。

- 只有在连接/通道真正有读写事件发生时，才会进行读写，就大大减少了系统开销，并且不必为每个连接都创建一个线程，不用去维护多个线程，避免了多线程之间的上下文切换导致的开销。

##  NIO非阻塞网络编程原理分析

1. 当客户端连接时，会通过ServerSocketChannel得到SocketChannel。
2. 将SocketChannel注册到Selector（register(Selector sel, int ops) ，一个selector可以注册多个SocketChannel。
3. 注册后会返回一个SelectionKey，会和该Selector关联。
4. Selector进行监听select方法，返回有事件发生的通道的个数。
5. 进一步得到各个SelectionKey，反向获取SocketChannel。
6. 根据得到的channel完成业务处理。

Server端:

```java
package com.xust.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author: Victor
 * @create: 2020-02-06 20:53
 **/

public class NIOServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        Selector selector = Selector.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        //设置为非阻塞
        serverSocketChannel.configureBlocking(false);
        //把ServerSocketChannel注册到selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //循环等待客户端连接
        while (true) {
            if (selector.select(1000) == 0) {
                //No event
                System.out.println("服务器等待了1秒，无连接");
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                //根据key对应的通道做相应的处理
                if (key.isAcceptable()) {
                    //给该客户端生成一个SocketChannel
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    System.out.println("客户端连接成功，生成了一个channel :"+socketChannel.hashCode());
                    socketChannel.register(selector,SelectionKey.OP_READ, ByteBuffer.allocate(1024));

                }
                if (key.isReadable()) {
                    SocketChannel channel = (SocketChannel) key.channel();
                    //获取到该channel的buffer
                    ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                    channel.read(byteBuffer);
                    Thread.sleep(2000);
                    System.out.println("from client message :" + new String(byteBuffer.array()));
                }
                keyIterator.remove();
            }
        }
    }
}

```

Client端:

```java
package com.xust.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author: Victor
 * @create: 2020-02-06 21:29
 **/

public class NIOClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 6666);
        if (!socketChannel.connect(inetSocketAddress)) {
            while (!socketChannel.finishConnect()) {
                System.out.println("因为连接需要时间，客户端不会阻塞，可以去做其他工作");

            }
        }
        String str = "hello, world";
        ByteBuffer byteBuffer = ByteBuffer.wrap(str.getBytes());
        socketChannel.write(byteBuffer);
    }

}

```

##  NIO与零拷贝

###  零拷贝

1. 我们说的零拷贝是从操作系统的角度来说的。因为内核缓冲区之间没有数据是重复的。
2. 零拷贝不仅仅带来更少的数据复制，还能带来其他的性能优势，例如更少的上下文切换，更少的CPU缓存伪共享以及无CPU校验和计算。

###  mmap和sendFile的区别

1. mmap适合小数据量读写，sendFile适合大文件传输。
2. mmap需要4次上下文切换，3次数据拷贝；sendFile需要3次上下文切换，最少2次数据拷贝。
3. sendFile可以利用DMA方式，减少CPU拷贝，mmap不能，必须从内核拷贝到Socket缓冲区。

- 

