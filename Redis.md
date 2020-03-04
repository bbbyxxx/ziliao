#  Redis

##  为什么要用缓存？

主要从“高性能”和“高并发”来看待这两个问题

###  高性能

假如用户第一次访问数据库中的某些数据，这个过程会比较慢，因为是从硬盘上读取的。将该用户访问的数据存放在缓存中，下一次再访问数据的时候就可以直接从缓存中拿去了，操作缓存就是直接操作内存，所以速度相当快。

###  高并发

直接操作缓存能够承受的请求是远远大于直接访问数据库的，所以我们可以考虑把数据库中的部分数据复制到缓存中，这样用户的一部分请求会直接到缓存这里而不用经过数据库。

##  Redis是什么

Redis是C语言开发的一个开源的高性能键值对的内存数据库，可以用作数据库、缓存、消息中间件等。Redis作为一个内存数据库，具有以下优点：

- 性能优秀，数据在内存中，读写速度非常快，支持并发10W QPS。
- 单进程单线程，是线程安全的，采用IO多路复用机制。
- 丰富的数据类型，有string、hash、list、set、sorted set等。
- 支持数据持久化，可以将内存中数据保存在磁盘中，重启时加载。
- 主从复制、哨兵、高可用。
- 可以作为消息中间件使用，支持发布订阅。

###  Redis数据类型

Redis内部使用一个redisObject对象来表示所有的key和value。
![Redis数据结构](/Users/a/Desktop/Java-victor/images/Redis/Redis数据结构.png)

type表示一个value对象具体是何种数据类型，encoding是不同数据类型在Redis内部的存储方式。比如type = string表示value存储的是一个普通字符串，那么encoding可能是raw或者int。

1. string是Redis最基本的类型，可以理解成与Memcached一模一样的类型，一个key对应一个value，value不仅可以是string，也可以是数字。string类型是安全的，也就是说string可以包含任何数据，string类型的值最大能存储512M。
2. hash是一个键值集合，Redis的hash是一个string的key和value的映射表，hash特别适合存储对象。
3. list是简单的字符串列表，按照插入顺序排序，可以添加一个元素到列表的头部或或者尾部，底层实现是一个双向链表，可以用来当队列用。
4. set是string类型的无序集合，集合是通过hashtable实现的，set中的元素没有顺序且没有重复。
5. zset和set一样是string类型的，但是可以通过score来对成员排序，并且插入是有序的，即自动排序。它的实现方式是内部使用HashMap和跳跃表(skiplist)来保证数据的存储和有序，HashMap里放的是成员到score的映射，跳跃表里放的是所有的成员，排序依据是HashMap里存的score，使用跳跃表的结构可以获得比较高的查找效率，并且在实现上比较简单。

##  Redis和Memcached的区别

1. Redis支持更丰富的数据类型（支持更复杂的应用场景）：Redis不仅仅支持简单的k/v类型的数据，同时还提供list、set、zset、hash等类型的存储，Memcached仅支持String。
2. redis支持数据的持久化，可以将内存中的数据保存在磁盘中，重启的时候可以再次加载进行使用，而Memcached把数据全部放在内存中。
3. Memcached没有原生的集群模式，需要依靠客户端来实现往集群中分片写入数据；Redis目前是原生支持cluster模式的。
4. Memcached是多线程，非阻塞IO复用的网络模型；Redis是使用单线程的多路IO复用模型。



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

Redis通过创建快照来获得存储在内存里面的数据在某个时间点上的副本，存储在dump文件里。Redis创建快照后，可以对快照进行备份，可以将快照复制到其他服务器从而创建具有相同数据的服务器副本，还可以将快照留在原地以便重启服务器的时候使用。

默认Redis是会以快照“RDB”的形式将数据持久化到磁盘的一个二进制文件dump.rdb。工作原理是当Redis需要持久化时，Redis会fork一个子进程，子进程将数据写到磁盘上一个临时RDB文件中。

优点：这种文件非常适合用于备份：比如可以每个一小时备份一次，即使遇上问题，也可以随时将数据集还原到不同的版本。

缺点：如果需要尽量避免在服务器故障时丢失数据，那么RDB不合适，除非每秒都要进行一个备份。

###  AOF持久化

AOF可以做到全程持久化，只需要在配置中开启appendonly yes。每执行一条会更改Redis中的数据的命令，Redis就会将该命令写入硬盘中的AOF文件。当Redis重启时，将会读取AOF文件进行重放，恢复到Redis关闭前的最后时刻。

优点：会让Redis变得非常耐久，可以设置不同的策略，默认是每秒一次，计算故障停机，也最多会丢失一秒钟的数据。

缺点：AOF的文件体积会大于RDB的体积，根据所使用的策略，AOF的速度可能会慢于RDB。

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

缓存击穿是指和缓存雪崩有点像，但有一点不同，缓存雪崩是因为大面积的缓存失效，打崩了DB，而缓存击穿是指一个key非常热点，在不停地扛着大量的请求，大并发集中对这一个点进行访问，当这个key在失效的瞬间，持续的大并发直接落到了数据库上，就在这个key上的点击穿了缓存。

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

```java
public class ConsistentHash<T> {
    private static String[] servers = {"192.168.0.1:8001","192.168.0.2:8001","192.168.0.3:8001","192.168.0.4:8001"};
  	//真实节点列表，因为服务器上下线很正常，所以用链表
    private static List<String> realNodes = new LinkedList<String>();
  	//key为虚拟节点的hash值，value为服务器名称
    private static SortedMap<Integer, String> virtualNodes = new TreeMap<Integer, String>();

  	//一个真实节点有8个虚拟节点
    private static final int V_NODE_NUM = 8;
    static {
        for (int i = 0; i < servers.length; i++) {
            realNodes.add(servers[i]);
        }

        for (String string : realNodes) {
            for (int i = 0; i < V_NODE_NUM; i++) {
                String vNodeName = string + "&&VN" + i;
                int hash = getHash(vNodeName);
                System.out.println("虚拟节点[" + vNodeName + "]被添加，hash值为:" + hash);
                virtualNodes.put(hash, vNodeName);
            }
        }
    }

    /**
     * FNV1_32_HASH算法
     * @param str
     * @return
     */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash =  (int) 2166136261L;
        for(int i = 0; i< str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

  	//应该路由到那台服务器
    private static String getServer(String node) {
        int hash = getHash(node);
      	//得到大于该hash值的所有map
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        Integer i = subMap.firstKey();
      	//获取虚拟节点
        String vNodeName = subMap.get(i);
        System.out.println(vNodeName);
      	//截取真实节点
        return vNodeName.split("&&VN")[0];
    }


    public static void main(String[] args) {
        String[] nodes = { "127.0.0.1:1111", "221.226.0.1:2222", "102.211.0.122:3333" , "238.226.0.1:2222", "221.211.0.122:3333"};
        for (int i = 0; i < nodes.length; i++)
            System.out.println("[" + nodes[i] + "]的hash值为" + getHash(nodes[i]) + ", 被路由到结点[" + getServer(nodes[i]) + "]");
    }
}

```

##  主从复制

主从复制模式结合哨兵能解决单点故障问题，提高Redis可用性。从节点提供读，主节点提供写操作，对于读多写少的状况，可给主节点配置多个从节点，从而提高响应效率。

###  复制过程

1. 从节点执行slaveof[masterIP] [masterPort] ，保存主节点信息。
2. 从节点中的定时任务发送主节点信息，建立和主节点的socket连接。
3. 从节点发送ping信号，主节点返回pong，两边能互相通信。
4. 连接建立后，主节点将所有数据发送给从节点（数据同步）。
5. 主节点把当前的数据同步给从节点后，便完成了复制的建立过程。接下来，主节点就会持续地把写命令发送给从节点，保证主从数据一致性。

