#  Java基础

##  Java基础部分

###  为什么重写equals()还要重写hashCode()？

HashMap中，如果要比较key是否相等，要同时使用这两个方法，因为自定义的类的hashCode()方法继承于Object类，其hashCode()为默认的内存地址，这样即便有两个相同的对象，比较也是不相等的。HashMap中先检查key的hashCode()是否相等，如果相等再equals()，若相等则认为这两个key时相等的，这样比是因为我们可以重写hashCode()值使相同的key计算的hashCode()值相同，而Object中的equals()比的就是hashcode值，所以会存在判断出错，所以需要重写再进行一次判断。(即重写hashCode()必须重写equals())重写后如果两个对象的hashCode()值不同肯定这两个对象不相同，就不必再equals()。

```java
public boolean equals(Object obj) {
  return (this == obj); //如果两个对象的引用完全相同则返回true，如果是基本类型，就比较数值是否相等
}
```

### 一个十进制的数在内存中是怎么存的？

在计算机中是以补码的形式存储的，而二进制的小数无法精确的表达10进制小数，则在计算的过程中会产生误差 。

```java
float f = 4.0 - 3.6; //0.40000001则会出现误差
```

###  Java中的八大基本类型

byte（8位）、short（16位）、int（32）、long（64）、float（单精度，32位）、double（64位）、char（16位）、boolean（1位）。在Java中万物皆对象，对象抽象类，所以会有它们的包装类，分别对应（Byte）、（Short）、（Integer）、（Long）、（Float）、（Double）、（Character）、（Boolean）。故有自动装箱和拆箱，装箱即从基本数据类型转换为对应的包装类型，反之为拆箱。

```java
Integer integer = Integer.valueOf(1);
int i = integer.intValue();
/**
int和Integer的区别？
除了上述，还有一个就是Integer默认值为null，比如当一个考生缺考，此时就应该为null，而不是为0。
*/

/**
这里需要注意缓冲池的概念，当值在-128～127之间，会从缓冲池中返回，不在这个范围，初始化时则会在堆内存中新建对象并返回引用（地址不同，即hashCode()不同，可以重写hashCode()），至于为什么设计缓冲池这里不做概述。
相应的还有
ByteCache、ShortCache、LongCache、CharacterCache
*/
```

###  Java中到底是值传递还是引用传递？

在Java中没有引用传递，因为Java舍弃了指针，所有传参方式都是值传递，如果是基本型变量，则传递的是一个副本，改变副本不改变原值，而当传入一个对象型变量时，就是一个“引用传递”，此时传递的是对象地址的一个副本，对这个地址进行操作就会同时改变原对象，就类似于两个引用指向一个对象（浅拷贝）。

###  数组(Array)和列表(ArrayList)有什么区别？

- Array可以包含基本类型和对象类型，List只能包含对象类型
- Array大小是固定的，ArrayList大小是动态变化的（ArrayList初始化容量为10，每次扩容1.5倍 10->15->22...)
- ArrayList中提供了更多的方法和特性，比如add()、addfirst()等等
- ArrayList的底层实现就是一个动态的Array

###  String、StringBuffer、StringBuilder的区别？

Java平台提供。。。存储和操作字符串，即包含多个字符的字符数据，String是不可变得，其它两个是可变，并且StringBuffer为线程安全，当如果知道字符串数据要改变的时候并且线程安全就可使用StringBuffer。当改变String时，反编译可知道new了StringBuilder对象再进行append()。扩容机制：默认为16，当需要扩容时，先检查2*当前容量+2，如果不满足，则当前容量+准备存储的长度。

```java
int newCapacity = (value.length << 1)+2;
StringBuilder(int capacity)；
/*通过上面这个构造方法，如果初始容量为0，那么<<1会一直为0，那么扩容就失去了它的本质了*/
```

###  String为什么是不可变得？