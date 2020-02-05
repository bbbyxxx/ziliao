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