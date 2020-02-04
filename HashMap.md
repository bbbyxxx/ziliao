#  HashMap

##  源码详细分析

###  构造函数

```java
//无参构造方法
//构造一个空的HashMap，初始容量为16，负载因子为0.75
public HashMap() {
  this.loadFactor = DEFAULT_LOAD_FACTOR;
}
public HashMap(int initialCapacity) {
  this(initialCapacity, DEFAULT_LOAD_FACTOR);
}
public HashMap(int initialCapacity, float loadFactor) {
  if(initialCapacity < 0) 
    throw Exception;
  if(initialCapacity > MAXIMUM_CAPACITY) 
    initialCapacity = MAXIMUM_CAPACITY;
  if(loadFactor <= 0 || FLoat.isNaN(loadFactor)) 
    throw Exception;
  this.loadFactor = loadFactor;
  this.threshold = tableSizeFor(initialCapacity);
}
```

###  tableSizeFor

```java
static final int tableSizeFor(int cap) {
  int n = cap - 1;
  n |= n >>> 1;
  n |= n >>> 2;
  n |= n >>> 4;
  n |= n >>> 8;
  n |= n >>> 16;
  return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n+1;
}
```

###  成员变量

```java
transient Node<K,V>[] table;
transient int size;
transient int modCount;
int threshold;
final float loadFactor;
/*
 HashMap 采用链表法避免哈希冲突，当链表长度大于TREEIFY_THRESHOLD(默认为8)时，将链表转换为红黑树，当小于UNTREEIFY_THRESHOLD(默认为6)时，又会转回链表以达到性能平衡，
*/
```

![HashMap数据结构](/Users/a/Desktop/Java-victor/images/HashMap数据结构.png)

###  key的hash

```java
static final int hash(Object key) {
  int h;
  return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  //能够将hashCode高位和低位的值进行混合做异或运算，而且混合后，低位的信息中加入了高位的信息，能够使每一位数字都能够得到使用，那么生成的hash值的随机性会增大。
}
```

### resize

```java
final Node<K,V>[] resize() {
  //保存当前table
  Node<K,V> oldTab = table;
  int oldCap = (oldTab == null) ? 0 : oldTab.length;
  //保存当前阈值
  int oldThr = threshold;
  //初始化新的table容量和阈值
  int newCap, newThr = 0;
  if(oldCap > 0) {
    //若table容量已超过最大容量，更新阈值为Integer.MAX_VALUE，这样以后就不会自动扩容了
    if(oldCap >= MAXIMUM_CAPACITY) {
      threshold = Integer.MAX_VALUE:
      return oldTab;
    }
    //double oldCap
    else if((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)  {
      new Thr = oldThr << 1; //double threshold
    }
    //是指用户自己定义的
    else if(oldThr > 0) {
      newCap = oldThr;
    }
    //当table为空被调用
    else{
      newCap = DEFAULT_INITIAL_CAPACITY;
      newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
  }
  Node<K,V>[] newTab = (Node<K,V>[]) new Node[newCap];
  table = newTab;
  if(oldTab != nul) {
    //把oldTab中的节点reHash到newTab中去
    for(int j = 0; j < oldTab; ++j) {
      Node<K,V> e;
      if((e = oldTab[j]) != null) {
        oldTab[j] = null;
        //若节点是单个节点，直接在newTab中进行重定位
        if(e.next == null) newTab[e.hash & (newCap - 1)] =e;
        //若节点是TreeNode节点，要进行红黑树的rehash操作
        else if(e instanceof TreeNode) 
          ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
        //若是链表，进行链表的rehash操作
        else{
          Node<K,V> loHead = null, loTail = null;
          Node<K,V> hiHead = null, hiTail = null;
					Node<K,V> next;
          ...
        }
      }
    }
  }
  return newTab;
}
```

我们使用的是2次幂的扩展，所以，与元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。

####  什么时候扩容？

通过HashMap源码可以看到是在put操作时，即向容器中添加元素时，判断当前容器中元素的个数是否达到阈值（当前数组长度 * 加载因子）的时候，就要自动扩容（重新计算容量，计算出所需容量的大小之后重新定义一个新的容器，将原来容器中的元素放入其中）了。

###  putval

```java
 final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        //如果table为空或者长度为0，则resize
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
   			//确定插入table的位置，算法是(n-1) & hash,在n为2的幂时，相当于取余
   			//找到key值对应的槽并且是第一个，直接加入
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
   			//在table的i位置发生碰撞，1.key值一样，替换value值
   			//2.key值不一样，链表或红黑树
        else {
            Node<K,V> e; K k;
            //第一个node的hash值即为要加入元素的hash
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
              	//不是TreeNode，即为链表，遍历链表
                for (int binCount = 0; ; ++binCount) {
                    //链表的尾端也没有找到key值相同的节点，则生成一个新的Node
                  	//然后判断链表的节点个数是不是达到转换成红黑树的上届，如果达到则转换成红黑树
                    if ((e = p.next) == null) {
                      	//创建节点并插入到尾部
                        p.next = newNode(hash, key, value, null);
                      	//超过了链表的设置长度8就转换成红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
                  	//如果e不为空就替换旧的oldValue值
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold)
            resize();
        afterNodeInsertion(evict);
        return null;
    }
```

###  hash冲突发生的几种情况

- 两节点key值相同（hash值一定相同），导致冲突
- 两节点key值不同，由于hash函数的局限性导致hash值相同，冲突
- 

##  为什么是线程不安全的

##  1.7和1.8的HashMap实现区别总结



