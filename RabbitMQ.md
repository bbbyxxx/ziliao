#  RabbitMQ总结

##  消息的来龙去脉

MQ的本质是什么？

消息的通讯。

##  消息路由机制

AMQP协议：

- 消息先从生产者Producer出发到达交换器Exchange
- 交换器Exchange根据路由规则将消息转发对应的队列Queue之上
- 消息在队列Queue上进行存储
- 消费者Consumer订阅队列Queue进行消费

对于消息可靠性的分析从上面四个阶段探讨。

##  Phase 1

###  事务（同步）

消息从生产者到交换器Exchange可以发生各种情况，生产者客户端发送出去之后可以发生网络丢包、网络故障等造成消息丢失。为此AMQP协议在建立之初就考虑到这种情况而提供了事务机制。

- channel.txSelect：将当前通道设置成事务模式
- channel.txCommit：用于提交事务
- channel.txRollback：用于事务回滚

事务确实能够解决消息发送方和MQ之间确认的问题，只有消息成功被MQ接收，事务才能提交成功，否则我们便可在捕获异常之后进行事务回滚，与此同时进行消息重发。

###  confirm机制（异步）

一旦信道开启confirm模式，所有在该信道上面发布的消息都会被指派一个唯一的ID（从1开始），一旦被投递到匹配的队列后，MQ就会给生产者发送一个Basic.Ack，这就使得生产者知道已经到达队列了。



两者是互斥的，不能共存，若从事务-->confirm，报错：cannot switch from tx to confirm ； 若从confirm-->事务，报错：cannot switch from confirm to tx.

##  Phase 2

###  参数

mandatory和mmediate 是chann.basicPublish方法中的两个参数，都有当消息传递过程中不可达目的地时将消息返回给生产者的功能。mmediate3.0之后被去掉了，官方解释：会影响镜像队列的性能，会增加代码复杂性。

当mandatory为true，如果找不到队列，那么会调用Basic.Return将消息返回给生产者。如果为false，直接丢弃。

###  备份交换器

若匹配不到队列，就会发送给备份交换器，可以将未被路由的消息存储在MQ中，再再需要的时候去处理这些消息。可以通过在声明交换器（channel.exchangeDeclare方法)的时候添加alternate-exchange参数来实现。

##  Phase 3

持久化可以提高队列的可靠性，以防在重启、关机、宕机等下的数据丢失。持久化需要在声明队列时将durable参数设置为true实现的，如果不设置持久化，那么在重启之后数据就会丢失。既要对数据持久化，也要对队列持久化，发送消息的时候将消息的deliveryMode设置为2，就是将消息设置为持久化。

##  Phase 4

消息确认机制。消费者在订阅队列时，可以指定autoAck参数，当autoACk等于false时，MQ会等待消费者显示地回复确认信号后才从内存中移去消息。当autoAck等于true时，不管是否真正消费了消息，都会删除。

采用消息确认机制后，只要设置autoAck为false，消费者就有足够的时间处理消息，不用担心处理消息过程中消费者进程挂掉后消息丢失的问题，因为MQ会一直等待消费者显示调用Basic.Ack命令。

##  

