#  Netty总结

##  介绍

1. Netty是一个异步的、基于事件驱动的网络应用框架，用以快速开发高性能、高可靠性的网络IO程序

2. Netty主要针对在TCP协议下，面向Clients端的高并发应用，或者Peer to Peer场景下的大量数据持续传输的应用

3. Netty本质是一个NIO框架，适用于服务器通讯相关的多种应用场景

4. Netty模型

   ![Netty模型](/Users/a/Desktop/Java-victor/images/Netty/Netty模型.png)

##  优点

1. 设计优雅：适用于各种传输类型的统一API阻塞和非阻塞Socket；基于灵活且可扩展的事件模型，可以清晰地分离关注点；高度可定制的线程模型。
2. 使用方便：详细记录的Javadoc，用户指南和示例；没有其它依赖项。
3. 高性能、吞吐量更高：延迟更低；减少资源消耗；最小化不必要的内存复制。
4. 安全：完整的SSL/TLS和StartTLS支持。
5. 社区活跃、不断更新：社区活跃，版本迭代周期短，发现的bug可以被及时修复，同时更多的功能将会被加入。

##  应用场景

1. 互联网行业：在分布式系统中，各个节点之间需要远程服务调用，高性能的RPC框架必不可少，Netty作为异步高性能的通信框架，往往作为基础通信组件被这些RPC框架使用。
2. 典型的应用有：阿里分布式服务框架Dubbo的RPC框架使用Dubbo协议进行节点间通信，Dubbo协议默认使用Netty作为基础通信组件，用于实现各进程节点之间的内部通信。
3. 大数据领域：经典的Hadoop的高性能通信和序列化组件（AVRO实现数据文件共享）的RPC框架，默认采用Netty进行跨界点通信。

##  I/O模型基本说明

1. I/O模型简单的理解：就是用什么样的通道进行数据的发送和接收，很大程度上决定了程序通信的性能。
2. Java共支持3种网络编程模型I/O模式：BIO、NIO、AIO。
3. Java BIO：同步并阻塞（传统阻塞型），服务器实现模式为一个连接一个线程，即客户端有连续请求时服务端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的开销，可以通过线程池机制改善（实现多个客户连接服务器）。适用于连接数目较小且固定的架构，这种方式对服务器资源要求比较高，并发局限于应用中。
4. Java NIO：同步不阻塞，服务器实现模式为一个线程处理多个请求（连接），即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求就进行处理。适用于连接数目多且连接比较短（轻操作）的架构，比如聊天服务器、弹幕系统、服务器间通讯等等。
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

##  Reactor模式

1. 基于I/O复用模型：多个连接共用一个阻塞对象，应用程序只需要在一个阻塞对象等待，无需等待所有连接。当某个连接有新的数据可以处理时，操作系统通知应用程序，线程从阻塞状态返回，开始进行业务处理。
2. 基于线程池复用线程资源：不必再为每个连接创建线程，将连接完成后的业务处理任务分配给线程进行处理，一个线程可以处理多个连接的业务。

###  核心组成

- Reactor：Reactor在一个单独的线程中运行，复制监听和分发事件，分发给适当的处理程序来对IO事件做出反应。
- Handlers：处理程序执行I/O事件要完成的实际事件，类似于客户想要与之交谈的公司中的实际官员。Reactor通过调度适当的处理程序来相应I/O事件，处理程序执行非阻塞操作。

###  单Reactor单线程

服务器端用一个线程通过多路复用搞定所有的IO操作（连接、读、写），编码简单，清晰明了，但是如果连接过多，将无法支撑。

1. select是前面I/O复用模型介绍的标准网络编程API，可以实现应用程序通过一个阻塞对象监听多路连接请求。
2. Reactor对象通过select监控客户端请求事件，收到事件后通过dispatch进行分发。
3. 如果是建立连接请求事件，则由Acceptor通过Accept处理连接请求，然后创建一个handler对象处理连接完成后的业务处理。
4. 如果不是，则Reactor会分发调用连接对应的handler来响应。
5. handler会完成Read-->业务处理-->Send的完整业务流程。

###  单Reactor多线程

1. Reactor对象通过select监控客户端请求事件，收到事件后，通过dispatch进行分发。
2. 如果建立连接请求，则由Accpetor通过accpet处理连接请求，然后创建一个Handler对象处理完成连接后的各种事件。
3. 如果不是连接请求，则由reactor分发调用相应的handler处理。
4. handler只负责响应事件，不做具体的业务处理，通过read读取数据后，会分发给后面的worker 线程池的某个线程处理业务。（使阻塞发生在线程中）
5. worker 线程池会分配独立的线程完成真正的业务，同时返回结果。
6. handler 收到相应后，通过send将结果返回给client。

优点：充分利用多核CPU。

缺点：多线程数据共享和访问比较复杂，reactor处理所有的事件的监听和响应，在单线程运行，在高并发场景容易出现性能瓶颈。

###  主从Reactor多线程

这种模型主要有Nginx主从Reactor多进程模型，Memcached主从多线程，Netty主从多线程模型等。

1. Reactor主线程MainReactor对象通过select监听连接事件，收到事件后通过Acceptor处理连接事件。
2. 当Acceptor处理连接事件后，MainReactor将连接分配给SubReactor。
3. SubReactor将连接加入到连接队列进行监听，并创建handler进行各种事件处理。
4. 当有新事件发生时，SubReactor就会调用对应的handler处理。
5. handler通过read读取数据，分发给后面的worker线程处理。
6. worker线程池分配独立的workder线程进行业务处理，并返回结果。
7. handler收到响应的结果后，再通过send将结果返回给client。
8. Reactor主线程可以对应多个Reactor子线程，可以关联多个SubReactor。

优点：父线程与子线程的数据交互简单、职责明确，父线程只需要接收连接，子线程完成后续业务处理。

缺点：编程复杂度较高。

###  Reactor模式优点

- 响应快：不必为单个同步时间所阻塞，虽然Reactor本身是同步的。
- 可以最大程度避免复杂的多线程及同步问题，并且避免了多线程/进程的切换开销。
- 扩展性好：可以方便的通过增加Reactor实例个数来充分利用CPU资源。
- 复用性好：Reactor模型本身与具体事件处理逻辑无关，具有很高的复用性。

##  Netty工作原理

1. Netty抽象出两组线程池NIOEventLoopGroup，BossGroup专门接收客户端的连接，WorkerGroup专门负责网络的读写。
2. NIOEventLoopGroup相当于一个事件循环组，这里面有多个事件循环，每一个事件循环是NIOEventLoop。
3. NIOEventLoop表示一个不断循环的执行处理任务的线程，每个NIOEventLoop都有一个selector，用于监听绑定在其上的socket的网络通讯。
4. NIOEventLoopGroup可以有多个线程，即可以含有多个NIOEventLoop。
5. 每个Boss NIOEventLoop循环执行的步骤有三步
   - 轮询accpet事件
   - 处理accept事件，与client建立连接，生成NIOSocketChannel，并将其注册到某个worker NIOEventLoop上的selector
   - 处理任务队列的任务，即runAllTasks
6. 每个Worker NIOEventLoop循环执行的步骤
   - 轮询read、write事件
   - 处理IO事件，在对应NIOSocketChannel处理
   - 处理任务队列的任务，即runAllTasks
7.   每个Worker NIOEventLoop处理业务时，会使用pipeline（管道），pipeline中包含了channel，即通过pipeline可以获取到对应通道，管道中维护了很多的处理器。

##  Future-Listentr机制

1. 当Future对象刚刚创建时，处于非完成状态，调用者可以通过返回的ChannelFuture来获取操作执行的状态，注册监听函数来执行完成后的操作。
2. 常见有如下操作
   - isDone：判断当前操作是否完成
   - isSuccess：判断当前操作是否成功
   - getCause：获取当前操作失败的原因
   - isCancelled：判断当前操作是否被取消
   - addListener：注册监听起，当操作已完成，将会通知指定的监听器；如果Future对象已完成，则通知指定的监听器

##  Netty核心模块

###  Bootstrap、ServerBootstrap

Bootstrap意思是引导，一个Netty应用通常由一个Bootstrap开始，主要作用是配置整个Netty程序，串联各个组件，Netty中Bootstrap类是客户端程序的启动引导类，Server是服务端启动引导类。

###  Channel

- Netty网络通信的组件，能够用于执行网络I/O操作
- 通过Channel可获得当前网络连接的通道的状态
- 通过Channel可获得网络连接的配置参数
- Channel提供异步的网络I/O操作，调用立即返回一个ChannelFuture实例，通过注册监听器到ChannelFuture上，可以I/O操作成功、失败或取消时回调通知调用方
- 支持关联I/O操作与对应的处理程序
- 不同协议、不同的阻塞类型的连接都有不同的Channel类型与之对应。比如
  - NioSocketChannel：异步的客户端Tcp Socket连接
  - NioServerSocketChannel：异步的服务器端Tcp Socket连接
  - NioDatagramChannel：异步的UDP 
  - NioSctpChannel：异步的客户端Sctp连接

###  Selector

1. Netty基于Selector对象实现I/O多路复用，通过Selector一个线程可以监听多个连接的Channel事件
2. 当向一个Selector中注册Channel后，Selector内部的机制就可以自动不断地查询这些注册的Channel是否有已就绪的I/O事件，这样程序就可以很简单的使用一个线程高效地管理多个Channel

###  ChannelHandler

1. ChannelHandler是一个接口，处理I/O事件或拦截I/O操作，并将其转发到其ChannelPipeline（业务处理链）中的下一个处理程序。
2. ChannelHandler本身并没有提供很多方法，因为这个接口有许多的方法实现，方便使用可以继承它的子类。 

##  Protobuf

###  介绍

是谷歌开发出来的一个语言无关、平台无关的数据序列化工具，在rpc或tcp通信等很多场景都可以使用。通俗来讲，如果客户端和服务端使用的是不同的语言，那么在服务端定义一个数据结构，通过protobuf转换为字节流，传送到客户端解码就可以得到对应的数据结构。一条消息数据用protobuf后的大小是json的10分之1，xml的20分之1，是二进制序列化的10分之1。

###  优点

- 简单、快。
- 将数据序列化为二进制之后，占用的空间相当小，基本仅保留了数据部分，而xml和json会附带消息结构在数据中。
- 使用起来很方便，只需要反序列化，而不需要xml和json那样层层解析。
- 编解码速度快，数据体积小，性能更高。

##  RPC

###  基本介绍

Remote Procedure Call，远程过程调用，是一个计算机通信协议，该协议允许运行于一台计算机的程序调用另一台计算机的子程序，而无需额外地为这个交互作用编程。

###  调用流程

1. client以本地调用方式调用服务
2. client stub接收到调用后负责将方法、参数等封装成能够进行网络传输的消息体
3. client stub将消息进行编码并发送到服务端
4. server stub收到消息后进行解码
5. server stub根据解码结果调用本地的服务
6. 本地服务执行并将结果返回给server stub
7. server stub将返回导入结果进行编码并发送至消费方
8. client stub接收到消息并进行解码
9. client得到结果

RPC的目标就是将2--8这些步骤都封装起来，用户无需关心这些细节，可以像调用本地方法一样即可完成远程服务调用。

##  Netty执行流程

1. 创建ServerBootStrap实例
2. 设置并绑定Reactor线程池：EventLoopGroup，new两个NioEventLoopGroup，EventLoop就是处理所有注册到本线程的Selector上面的channel
3. 设置并绑定服务端的Channel，NioServerSocketChannel
4. 创建处理网络事件的ChannelPipeline，并绑定对应的handler
5. 绑定并启动监听端口
6. 当轮询到准备就绪的channel后，由Reactor线程：NioEventLoop执行pipline中的方法，最终调度并执行channelHandler

##  Netty高性能体现在哪些方面

###  传输

IO模型在很大程度上决定了框架的性能，相比于BIO，Netty采用异步通信模式，因为NIO一个线程可以并发处理N个客户端连接和读写操作，这从根本上解决了传统同步阻塞IO一连接一线程模型，架构的性能、弹性伸缩能力和可靠性都得到了极大的提升。

###  协议

Netty默认提供了对Protobuf的支持，也可以通过扩展Netty的编解码接口，用户可以实现其它的高性能序列化框架。

###  线程

采用Reactor线程模型。

##  Netty的零拷贝体现在哪里，与操作系统上的有什么区别？

零拷贝就是在操作数据时，不需要将数据buffer从一个内存区域拷贝到另一个内存区域。少了一次内存的拷贝，CPU的效率就得以提升。在OS层面上就是避免在用户态与内核态之间来回拷贝数据。Netty的Zero-copy完全是在用户态的，更多的偏向于优化数据操作。

普通方式需要四次数据拷贝和四次上下文切换：

> 1. 数据从磁盘读取到内核的read buffer
> 2. 数据从内核缓冲区拷贝到用户缓冲区
> 3. 数据从用户缓冲区拷贝到内核的socket buffer
> 4. 数据从内核的socket buffer拷贝到网卡接口的缓冲区
>
> 第二、三步是没有必要的，通过Java的FileChannel.transferTo方法，可以避免上面两次多余的拷贝。

- Netty的接收和发送ByteBuf采用Direct Buffers，使用堆外直接内存进行Socket读写，不需要进行字节缓冲区的二次拷贝。如果使用传统的堆内存进行socket读写，JVM会将堆内存Buffer拷贝一份到直接内存中，然后才写入Socket中。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。
- Netty提供了组合buffer对象，可以聚合多个ByteBuffer对象，用户可以像操作一个Buffer那样方便的对组合Buffer进行操作，避免了传统通过内存拷贝的方式将几个小buffer合并成一个大的buffer。
- Netty文件传输采用了transferTo方法，可以直接将文件传输区的数据发送到目标Channel，避免了传统通过循环write方式导致的内存拷贝问题。