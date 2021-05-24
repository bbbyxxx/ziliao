#  MySQL数据类型优化

摘选自《高性能MySQL 》第四章第一节 选择优化的数据类型，主要对IP地址如何在数据库存储进行了调研，起到抛砖引玉的作用。

##  基础概念

设计表时选择正确的数据类型对于获得高性能至关重要，请遵循下面几个原则：

1. **更小的通常更好**
   尽量使用可以正确存储数据的最小数据类型，因为它们占用更少的磁盘，内存和CPU缓存，并且处理时需要的CPU周期也更少。

   ```mysql
   TINYINT 1字节 (-128，bai127) (0，255) 小整数值
   SMALLINT 2 字节 (-32 768，32 767) (0，65 535) 大整数值
   MEDIUMINT 3 字节 (-8 388 608，8 388 607) (0，16 777 215) 大整数值
   INT或INTEGER 4 字节 (-2 147 483 648，2 147 483 647) (0，4 294 967 295) 大整数值
   BIGINT 8 字节 (-9 233 372 036 854 775 808，9 223 372 036 854 775 807) (0，18 446 744 073 709 551 615) 极大整数值
   ```

2. **简单就好**
   简单数据类型的操作通常需要更少的CPU周期，例如整型比字符操作代价更低，不要用字符串来存储日期和时间，用整型来存储IP地址。

3. **尽量避免NULL**
   通常情况下最好指定列为NOT NULL，除非真的需要存储NULL值，如果查询中包含NULL的值，对MySQL来说更难优化，因为可为NULL的列使得索引、索引统计和值比较都更复杂。 **可为NULL的列会使用更多的存储空间**
   ![null_1](images/MySQL/null_1.png)

   

   ![null_2](images/MySQL/null_2.png)

##  实战Demo

整型字段的比较比字符串效率高很多，这也符合一项优化原则：字段类型定义使用最合适（最小），最简单的数据类型，之前用varchar(15)来存，现在int(10)就ok.


![saveip](images/MySQL/saveip.png)

```mysql
a.b.c.d 的ip number是：
a * 256的3次方 + b * 256的2次方 + c * 256的1次方 + d * 256的0次方
```

插入一百万数据消耗内存和用时：

![企业微信20200714114744](images/MySQL/企业微信20200714114744.png)


![企业微信20200714114824](images/MySQL/企业微信20200714114824.png)

```python
    def save_int_ip(self):
        start_time = time.time()
        i = 0
        for i in range(1000000):
            sql = """
            insert into ip_int(ip) values (inet_aton('255.255.255.255'));
            """
            cursor.execute(sql)
            i = i + 1

        end_time = time.time()
        print("save int ip to mysql cost: " + str(end_time - start_time))

    def save_char_ip(self):
        start_time = time.time()
        i = 0
        for i in range(1000000):
            sql = """
               insert into ip_char(ip) values ('255.255.255.255');
               """
            cursor.execute(sql)
            i = i + 1

        end_time = time.time()
        print("save char ip to mysql cost: " + str(end_time - start_time))
```


![select_db](images/MySQL/select_db.png)

##  总结

1. 建表时预计一下字段大小，能用TINYINT绝不用INT
2. 请使用MySQL中内置的时期和时间数据类来存储时间，不要用字符串
3. 使用整型存储IP，不要用字符串
4. 用其它字符来表示NULL，设置字段为NOT NULL