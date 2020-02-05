#  ZooKeeper学习笔记

##  环境配置

1. 首先在linux上安装好并配置环境变量

2. 去zookeeper官网下载zookeeper3.1.14，http://zookeeper.apache.org/，并将tar包通过FinalShell上传至linux

3. 解压，tar -zxvf zookeeper-3.4.14.tar.gz

4. 进入配置文件，将zoo_sample.cfg改名为zoo.cfg，mv  zoo_sample.cfg  zoo.cfg，建议做备份 cp  zoo_sample.cfg  zoo.cfg

5. 进入bin目录下启动zookeeper服务  ./zkServer.sh start

6. 创建maven项目，倒入依赖

   ```java
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>
   
       <groupId>com.xust</groupId>
       <artifactId>dubbo_project</artifactId>
       <packaging>pom</packaging>
       <version>1.0-SNAPSHOT</version>
   
       <properties>
           <motan.version>0.3.0</motan.version>
           <dubbo.version>2.5.3</dubbo.version>
           <dubbox.version>2.8.4</dubbox.version>
           <spring.version>4.3.6.RELEASE</spring.version>
           <java.version>1.8</java.version>
           <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
       </properties>
       <dependencies>
           <!-- https://mvnrepository.com/artifact/junit/junit -->
           <dependency>
               <groupId>junit</groupId>
               <artifactId>junit</artifactId>
               <version>4.12</version>
               <scope>test</scope>
           </dependency>
           <dependency>
               <groupId>com.alibaba</groupId>
               <artifactId>dubbo</artifactId>
               <version>2.5.3</version>
               <exclusions>
                   <exclusion>
                       <groupId>org.springframework</groupId>
                       <artifactId>spring</artifactId>
                   </exclusion>
               </exclusions>
           </dependency>
           <dependency>
               <groupId>com.github.sgroschupf</groupId>
               <artifactId>zkclient</artifactId>
               <version>0.1</version>
           </dependency>
           <!-- spring相关 -->
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-core</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-beans</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-context</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-jdbc</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-web</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-webmvc</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-aop</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-tx</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-orm</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-context-support</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-test</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.springframework</groupId>
               <artifactId>spring-jms</artifactId>
               <version>${spring.version}</version>
           </dependency>
           <dependency>
               <groupId>org.aspectj</groupId>
               <artifactId>aspectjrt</artifactId>
               <version>1.6.11</version>
           </dependency>
           <dependency>
               <groupId>org.aspectj</groupId>
               <artifactId>aspectjweaver</artifactId>
               <version>1.6.11</version>
           </dependency>
       </dependencies>
       <modules>
           <module>dubbo_api</module>
           <module>dubbo_consumer</module>
           <module>dubbo_provider</module>
       </modules>
   </project>
   ```

##  ZooKeeper是什么

- ZooKeeper是一个开放源码的分布式协调服务，它是集群的管理者，监视着集群中各个节点状态然后根据节点提交的反馈进行下一步合理操作。最终，将简单易用的接口和性能高效、功能稳定的系统提供给用户。
- 分布式应用程序可以基于ZooKeeper实现诸如数据发布/订阅、负载均衡、命名服务、分布式协调/通知、集群管理、Master选举、分布式锁和分布式队列等功能。
- ZooKeeper提供了如下分布式一致特性：顺序一致性、原子性、单一视图、可靠性、实时性。

##  ZooKeeper文件系统

- ZooKeeper提供了一个多层级的节点命名空间（节点称为znode）。与文件系统不同的是，这些节点都可以设置关联的数据，而文件系统中只有文件节点可以存放数据而目录节点不行。

- ZooKeeper为了保证高吞吐和低延迟，在内存中维护了这个树状的目录结构。这种特性使得ZooKeeper不能用于存放大量的数据，每个节点的存放数据上限为1M。

## ZAB协议

- ZAB协议是为分布式协调服务ZooKeeper专门设计的一种支持崩溃恢复的原子广播协议。
- ZAB协议包括两种基本的模式：崩溃恢复和消息广播。
- 当整个ZooKeeper集群刚刚启动或者Leader服务器宕机、重启或者网络故障导致不存在过半的服务器与Leader保持正常通信时，所有进程（服务器）进入崩溃恢复模式，首先选举产生新的Leader服务器，然后集群中Follower服务器开始与新的Leader服务器进行数据同步，当集群中超过半数机器与该Leader服务器完成数据同步之后，退出恢复模式进入消息广播模式，Leader服务器开始接收客户端的事务请求生成事务提案来进行事务请求处理。

##  服务器角色

###  Leader

- 事务请求的唯一调度和处理者，保证集群事务处理的顺序性

- 集群内部各服务的调度者

###  Follower

- 处理客户端的非事务请求，转发事务请求给Leader服务器
- 参与事务请求Proposal的投票
- 参与Leader选举投票

###  Observer

- 处理客户端的非事务请求，转发事务请求给Leader服务器
- 不参与任何形式的投票

##  ZooKeeper有哪几种部署模式

单机模式、伪集群模式、集群模式

##  集群至少要几台机器，集群规则是怎样的

集群规则为2N+1台，N>0.