#  Redis

##  为什么要用缓存？

主要从“高性能”和“高并发”来看待这两个问题

###  高性能

假如用户第一次访问数据库中的某些数据，这个过程会比较慢，因为是从硬盘上读取的。将该用户访问的数据存放在缓存中，下一次再访问数据的时候就可以直接从缓存中拿去了，操作缓存就是直接操作内存，所以速度相当快。

###  高并发

直接操作缓存能够承受的请求是远远大于直接访问数据库的，所以我们可以考虑把数据库中的部分数据复制到缓存中，这样用户的一部分请求会直接到缓存这里而不用经过数据库。

##  Redis和Memcached的区别

1. Redis支持更丰富的数据类型（支持更复杂的应用场景）：Redis不仅仅支持简单的k/v类型的数据，同时还提供list、set、zset、hash等类型的存储，Memcached仅支持String。
2. redis支持数据的持久化，可以将内存中的数据保存在磁盘中，重启的时候可以再次加载进行使用，而Memcached把数据全部放在内存中。
3. Memcached没有原生的集群模式，需要依靠客户端来实现往集群中分片写入数据；Redis目前是原生支持cluster模式的。
4. Memcached是多线程，非阻塞IO复用的网络模型；Redis是使用单线程的多路IO复用模型。

##  Redis常见数据结构以及使用场景分析

###  String

常用命令：set、get、decr、incr、mget等。

String是简单的key-value类型，value不仅仅是String，还可以是数据。常规key-value缓存应用；常规技术：微博数、粉丝数等

###  Hash

常用命令：hset、hget、hgetall等。

Hash是一个String类型的field和value的映射表，Hash特别适合用于存储对象，后续操作的时候，可以直接仅仅修改这个对象中的某个字段的值。

>key=JavaUserVictor
>
>value={
>
>"id" : 1 ,
>
>"name" : "Victor",
>
>"age" : 20 
>
>}

###  List

常用命令：lpush、rpush、lpop、rpop、lrange等。

List为一个双向链表，是Redis最重要的数据结构之一，比如微博的关注列表、粉丝列表、消息列表等功能都可以用List来实现。

###  Set

常用命令：sadd、spop、smembers、sunion等。