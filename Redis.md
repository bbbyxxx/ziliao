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

```c
key=JavaUserVictor
value={
"id" : 1 ,
"name" : "Victor",
"age" : 20 
}
```

###  List

常用命令：lpush、rpush、lpop、rpop、lrange等。

List为一个双向链表，是Redis最重要的数据结构之一，比如微博的关注列表、粉丝列表、消息列表等功能都可以用List来实现。

###  Set

常用命令：sadd、spop、smembers、sunion等。

Set提供了判断某个成员是否在一个set集合内的重要接口，这个也是List所不能提供的。可以基于Set轻易实现交集、并集、差集的操作。

Sorted Set

常用命令：zadd、zrange、zrem、zcard等。

和Set相比，Sorted Set增加了一个权重参数score，使得集合中的元素能够按score进行有序排列。

##  Redis的过期时间

Redis中可以对数据设置过期时间，应用场景：token过期、短信验证码等。



###  定期删除

Redis默认每隔100ms**随机**抽取一些设置了过期时间的key，检查其是否过期，如果过期就删除。如果遍历所有的设置过期时间的key的话，会给CPU带来很大的负载。

###  惰性删除

定期删除可能会导致很多过期key到了时间并没有被删除掉。惰性删除即当获取数据时检查key是否过期，如果过期就删除。

如果定期删除漏掉了很多key，然后也没有及时去查，就会导致大量key堆积在内存里，导致Redis内存块耗尽了，所以就有了Redis内存淘汰机制。

##  Redis内存淘汰机制

1. volatile-lru：从已设置过期时间的数据集中挑选最近最少使用的数据淘汰。
2. volatile-ttl：从已设置过期时间的数据集中挑选将要过期的数据淘汰。
3. volatile-random：从已设置过期时间的数据集中任意选择数据淘汰。
4. allkeys-lru：当内存不足以容纳新写入数据时，在键空间中，移除最近最少使用的key（最常用）
5. allkey-random：从数据集中任意选择数据淘汰。

##  Redis持久化机制

###  快照持久化（RDB）

Redis通过创建快照来获得存储在内存里面的数据在某个时间点上的副本。Redis创建快照后，可以对快照进行备份，可以将快照复制到其他服务器从而创建具有相同数据的服务器副本，还可以将快照留在原地以便重启服务器的时候使用。

###  AOF持久化

每执行一条会更改Redis中的数据的命令，Redis就会将该命令写入硬盘中的AOF文件。AOF文件的保存位置和RDB的位置相同，都是通过dir参数设置的。

>appendfsync  always       每次有修改都会写入AOF文件
>
>appendfsync. everysec    每秒钟同步一次
>
>appendfsync  no              让操作系统决定何时进行同步

##  Redis事务

Redis通过MULTI、EXEC、WATCH等命令来实现事务功能。事务提供了一种将多个命令请求打包，然后一次性、按顺序地执行多个命令的机制，并且在事务执行期间，服务器不会中断事务而改去执行其他客户端的命令请求，它会将事务中的所有命令都执行完毕，然后去处理其他客户端的命令请求。

在Redis中，事务总是具有原子性、一致性和隔离性，并且当Redis运行在某种特定的持久化模式下，事务也具有持久性。

##  跳表实现

###  跳跃表节点定义和跳跃表描述符定义

```c
typedef struct zskiplistNode {
	sds ele;
  double score;										//分值
  struct zkiplistNode *bkackward; //后退指针
	struct zskiplistLevel {
    struct zskiplistNode *forward;	//前进指针
		unsigned int span;  						//节点在该层和前向节点的距离
  } level[];
}zskiplistNode;

typedef struct zskiplist {
  struct zskiplistNode *header, * tail;		//头节点
  unsigned long length;										//节点数量
  int level;															//目前表内节点的最大层数
}zskiplist;
```

###  node的创建

```c
zskiplistNode *zslCreateNode (int level, double score, sds ele) {
  zskiplistNode *zn = zmalloc(sizeof(*zn) + level*sizeof(struct zskiplistLevel));
  zn->score = score;
  zn->ele = ele;
  return zn;
}
```



###  新建跳跃表

```c
zskiplist * zslCreate(void) {
  int j;
  zskiplist *zsl;
  
  zsl = zmalloc(sizeof(*zsl));
  zsl->level = 1;
  zsl->length = 0;
  
  
  //创建一个层数为32，分值为0，成员对象为null的表头节点
  zsl->header = zslCreateNode(ZSKIPLIST_MAXLEVEL, 0, NULL);
  for (j = 0;j < ZSKIPLIST_MAXLEVEL; j++) {
    zsl->header->level[j].forward = NULL;
    zsl->header->level[j].span = 0;
  }
  
  zsl->header->backward = NULL;
  zsl->tail = NULL;
  return zsl;
}
```

###  插入新节点

1.查找每一层的插入点，所谓插入点指新节点插入后作为新节点前继的节点，redis用数组来句路





##  缓存穿透

缓存穿透是指缓存和数据库中都没有的数据，而用户不断发起请求，如发起id为-1的数据或id为特别大不存在的数据。这时的用户很可能是攻击者，攻击者会导致数据库压力过大。

###  解决方案

1. 接口层增加校验，如权限校验、id<=0的直接拦截。
2. 从缓存取不到的数据，在数据库中也没有取到，这时也可以将key-value对写为key-null，缓存有效时间可以设置短点，这样可以防止攻击用户反复用同一个id暴力攻击。

##  缓存击穿

缓存击穿是指缓存中没有但数据库中有的数据，这时由于并发用户特别多，同时读缓存没读到数据，又同时去数据库去取数据，引起数据库压力瞬间增大，造成过大压力。

###  解决方案

1. 设置热点数据永远不过期。

2. 加互斥锁，分布式锁。

   ```java
   public static String getData(String key) throws InterruptedException {
     String result = getDataFromRedis(key);
     if (result == null) {
       //获取锁
       if(reenlock.trylock()) {
         result = getDataFromMysql(key);
         if(result != null) {
           setDataToCache(key, result);
         }
         reenlock.unlock();
       }
       else {
         //等100ms再重新获取
      		Thread.sleep(100);
         result = getData(key);
       }
     }
   }
   ```

##  缓存雪崩

缓存雪崩是指缓存中数据大批量到过期时间，而查询数据量巨大，引起数据库压力过大甚至宕机，和缓存击穿不同的是，缓存雪崩是所有数据都过期了。

###  解决方案

1. 缓存数据的过期时间设置随机，防止同一时间大量数据过期现象发生。
2. 如果缓存数据库是分布式部署，将热点数据均匀分布在不同的缓存数据库中。
3. 设置热点数据永不过期。

##  一致性Hash

在使用Redis的时候，为了保证Redis的高可用，或者Redis的读写性能，最简单的方式我们会做主从复制，组成Master-Slave的形式，或者搭建Redis集群，进行数据的读写分离。同样类似于数据库，当数据大于500w的时候就要分库分表，当数据量很大的时候同样可以对Redis进行类似的操作，就是分库分表。

当我们有2000w数据量时，就可以通过Hash值、取模、按照类别、按照某一个字段等等常见的规则进行分区，分库。但是如果按照这种方式有一种缺陷，就是当服务器的数量在变动的时候，所有的缓存位置都要发生改变。由于服务器在生产环境中随时会故障需要移除或者需要扩展时都会出现这种问题，那么就有了一致性Hash算法。

一致性Hash算法的原理是将节点hash值映射到hash环上，它具有如下特性：

- 单调性：指如果已经有一些请求通过哈希分派到了相应的服务器进行处理，又有新的服务器加入到系统的时候，应保证原有的请求可以被映射到原有或者新的服务器中，而不是映射到原来的其他的服务器上。
- 分散性：分布式环境中，客户端不知道服务器的存在，可能只知道其中一部分服务器，在客户端看来它看到的部分服务器就会形成一个完整的hash环，如果多个客户端都把部分服务器作为一个完整hash环，那么可能会导致，同一个用户的请求被路由到不同的服务器进行处理，这种情况显然是应该避免的，因为它不能保证同一个用户的请求落到同一个服务器，好的哈希算法应该尽量降低分散性。
- 平衡性：负载均衡，是指hash后的请求能够分散到不同的服务器上，一致性hash可以做到每个服务器都能处理请求，但是不能保证每个服务器处理的请求数量大致相同。

采用一致性hash算法的分布式集群中将新的机器加入，其原理是通过使用与对象存储一样的hash算法将机器也映射到环中（对机器的hash值是采用机器的ip或者机器唯一的别名作为输入值），然后以顺时针的方向计算，将所有对象存储到离自己最近的机器中。

但是这种情况也有他的缺点：如果节点hash值在hash环上分布不均匀，会导致缓存数据在每个节点不均匀分配；节点增加或减少，需要重新分布的缓存数据也不能均匀分配。此时就要采用虚拟节点，就要将原来的某个节点进行拆分再映射，此时计算就应该是机器的IP或者唯一的别名+序号作为输入值，再均衡分布。