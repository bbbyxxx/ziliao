#  Dubbo学习笔记

##  环境配置

1. 首先在linux上安装好并配置环境变量

2. 去zookeeper官网下载zookeeper3.1.14，http://zookeeper.apache.org/，并将tar包通过FinalShell上传至linux

3. 解压，tar -zxvf zookeeper-3.4.14.tar.gz

4. 进入配置文件，将zoo_sample.cfg改名为zoo.cfg，mv  zoo_sample.cfg  zoo.cfg，建议做备份 cp  zoo_sample.cfg  zoo.cfg

5. 进入bin目录下启动zookeeper服务  ./zkServer.sh start

6. 创建maven项目，倒入依赖

   ```java
   
   ```

   