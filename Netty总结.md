#  Netty总结

##  介绍

1. Netty是一个异步的、基于事件驱动的网络应用框架，用以快速开发高性能、高可靠性的网络IO程序

2. Netty主要针对在TCP协议下，面向Clients端的高并发应用，或者Peer to Peer场景下的大量数据持续传输的应用

3. Netty本质是一个NIO框架，适用于服务器通讯相关的多种应用场景

4. Netty模型

   ![Netty模型](/Users/a/Desktop/Java-victor/images/Netty模型.png)

##  应用场景

1. 互联网行业：在分布式系统中，各个