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

###  事务

消息从生产者到交换器Exchange可以发生各种情况，生产者客户端发送出去之后可以发生网络丢包、网络故障等造成消息丢失。为此AMQP协议在建立之初就考虑到这种情况而提供了事务机制。

- channel.txSelect：将当前通道设置成事务模式
- channel.txCommit：用于提交事务
- channel.txRollback：用于事务回滚

事务确实能够解决消息发送方和MQ之间确认的问题，只有消息成功被MQ接收，事务才能提交成功，否则我们便可在捕获异常之后进行事务回滚，与此同时进行消息重发。

###  confirm机制



##  Phase 2

##  Phase 3

##  Phase 4

##  

