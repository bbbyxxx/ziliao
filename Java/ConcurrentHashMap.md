#  ConcurrentHashMap

##  源码分析

重要的属性：

```java
static final int MOVED     = -1; // 表示正在迁移
static final int TREEBIN   = -2; // 表示已经转换成树
static final int RESERVED  = -3; // hash for transient reservations
static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash
```

sizeCtl:

```java
sizeCtl = 0;	//未指定初始容量
sizeCtl > 0;	//由指定的初始容量计算而来，再找最近的2的幂次方，比如传入6，则初始化容量为16
sizeCtl = -1;	//table正在初始化

//初始化完成后:
sizeCtl = table.length * 0.75


```



```java
static final int HASH_BITS = 0x7fffffff;  //01111111_11111111_11111111_11111111
// jdk 1.8的hash策略，都是为了减少hash冲突	
static final int spread(int h) {	    //无符号右移加入高位影响，与HASH_BITS做与操作保留对hash有用的比特位，有让hash>0的意思
    return (h ^ (h >>> 16)) & HASH_BITS;
}

```

put源码分析：

```java
/**
* 当添加一对键值对的时候，首先会去判断保存这些键值对的数组是不是已经初始化了，
* 如果没有的话就初始化数组
*  然后通过计算hash值来确定放在数组的哪个位置
* 如果这个位置为空则直接添加，如果不为空的话，则取出这个节点来
* 如果取出来的节点的hash值是MOVED(-1)的话，则表示当前正在对这个数组进行扩容，复制到新的数组，则当前线程也去帮助复制
* 最后一种情况就是，如果这个节点，不为空，也不在扩容，则通过synchronized来加锁，进行添加操作
*    然后判断当前取出的节点位置存放的是链表还是树
*    如果是链表的话，则遍历整个链表，直到取出来的节点的key来个要放的key进行比较，如果key相等，并且key的hash值也相等的话，则说明是同一个key，则覆盖掉value，否则的话则添加到链表的末尾
*    如果是树的话，则调用putTreeVal方法把这个元素添加到树中去
*  最后在添加完成之后，会判断在该节点处共有多少个节点（注意是添加前的个数），如果达到8个以上了的话，
*  则调用treeifyBin方法来尝试将处的链表转为树，或者扩容数组

*/

// onlyIfAbsent: false的时候表示这个value一定会设置，true的时候表示当value为空的时候才会设置
final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) throw new NullPointerException();
        int hash = spread(key.hashCode());
    	// 节点数，主要用于每次加入节点后查看是否需要从链表转为红黑树
        int binCount = 0;
        for (Node<K,V>[] tab = table;;) { //CAS写法，如果不成功则一直重试
            Node<K,V> f; int n, i, fh;
            // 除非构造时指定初始化集合，否则默认构造不初始化table
            if (tab == null || (n = tab.length) == 0)
                tab = initTable();  // 初始化table
            //CAS操作得到对应table中元素
            else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                if (casTabAt(tab, i, null,
                             new Node<K,V>(hash, key, value, null)))
                    break;         // 创建Node对象作为链表首结点
            }
            else if ((fh = f.hash) == MOVED) // 表示当前节点正在扩容
                // 让当前线程调用该方法也参与到扩容过程中来。扩容完毕后tab指向新table
                // 通过允许多线程复制的功能，以此来减少数组的复制所带来的性能损失
                tab = helpTransfer(tab, f);
            else {
                V oldVal = null;
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        // 表明是链表节点类型，hash值是大于0的
                        if (fh >= 0) {
                            binCount = 1;
                            for (Node<K,V> e = f;; ++binCount) {
                                K ek;
                                if (e.hash == hash &&
                                    ((ek = e.key) == key ||
                                     (ek != null && key.equals(ek)))) {
                                    oldVal = e.val;
                                    // 表示是旧值则覆盖
                                    if (!onlyIfAbsent)
                                        e.val = value;
                                    break;
                                }
                                Node<K,V> pred = e;
                                if ((e = e.next) == null) {
                                    // 把新节点加入链表尾部
                                    pred.next = new Node<K,V>(hash, key,
                                                              value, null);
                                    break;
                                }
                            }
                        }
                        else if (f instanceof TreeBin) {
                            // 红黑树的插入
                            Node<K,V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                           value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent)
                                    p.val = value;
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD) //如果桶中结点数超过8个就会转为红黑树
                        treeifyBin(tab, i);
                    if (oldVal != null)
                        return oldVal;
                    break;
                }
            }
        }
        addCount(1L, binCount); //更新size，检查是否扩容，在1.7时是先检查是否需要扩容再进行插入
        return null;
    }
```

数组初始化

table初始化是没有加锁的，当要初始化时会通过CAS操作将sizeCtl设为-1，而sizeCtl由volatile修饰，保证修改对后面线程可见。  之后如果再有线程执行到此方法时检测到SIZETL为负数，那么说明已经有线程在扩容了，这个线程就会调用Thread.yield让出CPU时间片。

```java
/**
* 如果sizeCtl小于0，说明别的线程正在进行初始化，让出执行权
* 如果sizeCtl大于0，则初始化一个大小为sizeCtl的数组
* 否则初始化默认大小16的数组
* 设置sizeCtl为数组长度的3/4
*/
private final Node<K,V>[] initTable() {
        Node<K,V>[] tab; int sc;
        while ((tab = table) == null || tab.length == 0) {
            if ((sc = sizeCtl) < 0)
                Thread.yield(); //说明已经有线程在扩容了，这个线程就会调用Thread.yield让出一次CPU执行时间
            
            //当正在初始化时将sizeCtl设为-1
            else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if ((tab = table) == null || tab.length == 0) {
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        @SuppressWarnings("unchecked")
                        Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                        table = tab = nt;
                        sc = n - (n >>> 2);
                    }
                } finally {
                    sizeCtl = sc;
                }
                break;
            }
        }
        return tab;
    }
```

###   扩容实现

 ####   什么时候会扩容？

1. 使用put添加元素时会调用addCount()，内部检查sizeCtl是否需要扩容
2. tryPrisize()被调用，当链表转红黑树时table容量小于64， 或调用putAll一次性加入大量元素

```java
/**
* 扩容表为指可以容纳指定个数的大小（总是2的N次方）
* 假设原来的数组长度为16，则在调用tryPresize的时候，size参数的值为16<<1(32)，此时sizeCtl的值为12
* 计算出来c的值为64,则要扩容到sizeCtl≥为止
*  第一次扩容之后 数组长：32 sizeCtl：24
*  第二次扩容之后 数组长：64 sizeCtl：48
*  第二次扩容之后 数组长：128 sizeCtl：94 --> 这个时候才会退出扩容
*/



private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
        int n = tab.length, stride;
        if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
            stride = MIN_TRANSFER_STRIDE; // subdivide range
        if (nextTab == null) {            // initiating
            try {
                @SuppressWarnings("unchecked")
                Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];  //扩容到2倍
                nextTab = nt;
            } catch (Throwable ex) {      // try to cope with OOME
                sizeCtl = Integer.MAX_VALUE;   //扩容保护
                return;
            }
            nextTable = nextTab;
            transferIndex = n;  //扩容总进度， >=transferIndex的桶都已分配出去
        }
        int nextn = nextTab.length;
  		//扩容时的特殊节点，标明此节点正在进行迁移，扩容期间的元素查找要调用其find()方法在nextTab中查找
        ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
    	//当前线程是否需要继续寻找下一个可处理的节点
        boolean advance = true;
    	//所有桶是否都迁移完成
        boolean finishing = false; // to ensure sweep before committing nextTab
        for (int i = 0, bound = 0;;) {
            Node<K,V> f; int fh;
            while (advance) {
                int nextIndex, nextBound;
                if (--i >= bound || finishing)
                    advance = false;
                //迁移总进度<=0B表示所有桶都已迁移完成
                else if ((nextIndex = transferIndex) <= 0) {
                    i = -1;
                    advance = false;
                }
                else if (U.compareAndSwapInt
                         (this, TRANSFERINDEX, nextIndex,
                          nextBound = (nextIndex > stride ?
                                       nextIndex - stride : 0))) {
                    //确定当前线程每次分配的待迁移桶的范围为[bound, nextIndex]
                    bound = nextBound;
                    i = nextIndex - 1;
                    advance = false;
                }
            }
            if (i < 0 || i >= n || i + n >= nextn) {
                int sc;
                if (finishing) {// 当所有线程都干完活
                    nextTable = null;
                    table = nextTab; //替换新table
                    sizeCtl = (n << 1) - (n >>> 1);  //使sizeCtl为新容量的0.75倍
                    return;
                }
                if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                    //相等时说明已经没有线程在参与扩容了
                    if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
                        return;
                    finishing = advance = true;
                    i = n; // recheck before commit
                }
            }
            else if ((f = tabAt(tab, i)) == null)
                //如果i处是ForwardingNode表示第i个桶已经有线程在负责迁移了
                advance = casTabAt(tab, i, null, fwd);
            else if ((fh = f.hash) == MOVED)
                advance = true; // already processed
            else {
                //桶内迁移需要加锁
                synchronized (f) {
                    if (tabAt(tab, i) == f) {
                        Node<K,V> ln, hn;
                        if (fh >= 0) {
                            //表示是链表节点
                            int runBit = fh & n;
                            Node<K,V> lastRun = f;
                            for (Node<K,V> p = f.next; p != null; p = p.next) {
                                int b = p.hash & n;
                                if (b != runBit) {
                                    runBit = b;
                                    lastRun = p;
                                }
                            }
                            if (runBit == 0) {
                                ln = lastRun;
                                hn = null;
                            }
                            else {
                                hn = lastRun;
                                ln = null;
                            }
                            for (Node<K,V> p = f; p != lastRun; p = p.next) {
                                int ph = p.hash; K pk = p.key; V pv = p.val;
                                if ((ph & n) == 0)
                                    ln = new Node<K,V>(ph, pk, pv, ln);
                                else
                                    hn = new Node<K,V>(ph, pk, pv, hn);
                            }
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                        else if (f instanceof TreeBin) {
                            // 红黑树处理
                            TreeBin<K,V> t = (TreeBin<K,V>)f;
                            TreeNode<K,V> lo = null, loTail = null;
                            TreeNode<K,V> hi = null, hiTail = null;
                            int lc = 0, hc = 0;
                            for (Node<K,V> e = t.first; e != null; e = e.next) {
                                int h = e.hash;
                                TreeNode<K,V> p = new TreeNode<K,V>
                                    (h, e.key, e.val, null, null);
                                if ((h & n) == 0) {
                                    if ((p.prev = loTail) == null)
                                        lo = p;
                                    else
                                        loTail.next = p;
                                    loTail = p;
                                    ++lc;
                                }
                                else {
                                    if ((p.prev = hiTail) == null)
                                        hi = p;
                                    else
                                        hiTail.next = p;
                                    hiTail = p;
                                    ++hc;
                                }
                            }
                            //复制完树节点之后，判断该节点处构成的树是否需要转回链表
                            ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                                (hc != 0) ? new TreeBin<K,V>(lo) : t;
                            hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                                (lc != 0) ? new TreeBin<K,V>(hi) : t;
                            setTabAt(nextTab, i, ln);
                            setTabAt(nextTab, i + n, hn);
                            setTabAt(tab, i, fwd);
                            advance = true;
                        }
                    }
                }
            }
        }
    }
```

####    在扩容的时候可以对数组进行读写操作吗？

可以，当在进行数组扩容的时候，如果当前节点还没有被处理，那就可以进行写操作，如果已经被处理了，那么当前线程也会加入扩容中协同扩容。



####    并发扩容总结

1. 单线程新建nextTable，新容量一般为原table容量的两倍
2. 每个线程想增/删元素时，如果访问的桶是ForwardingNode节点，则表明当前正处于扩容状态，协助一起扩容完成后再完成相应的数据更改操作
3. 扩容时将原table的所有桶排序分配，每个线程每次最小分配16个桶，防止资源竞争导致的效率下降。单个桶内元素的迁移是加锁的，但桶范围处理分配可以多线程，在没有迁移完成所有桶之前每个线程需要重复获取迁移桶范围，直至所有桶迁移完成
4. 一个旧桶内的数据迁移完成但不是所有桶都迁移完成时，查询数据委托给ForwardingNode节点查询nextTable完成
5. 迁移过程sizeCtl用于记录参与扩容线程的数量，全部迁移完成后sizeCtl更新为新table容量的0.75倍



```java
/*
* 相比put方法，get就很单纯了，支持并发操作，、
* 当key为null的时候回抛出NullPointerException的异常
* get操作通过首先计算key的hash值来确定该元素放在数组的哪个位置
* 然后遍历该位置的所有节点
* 如果不存在的话返回null
*/
public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        int h = spread(key.hashCode());
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) {
            if ((eh = e.hash) == h) { //检查头节点
                if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                    return e.val;
            }
            else if (eh < 0)  //如果是树则调用find
                return (p = e.find(h, key)) != null ? p.val : null;
            while ((e = e.next) != null) {
                if (e.hash == h &&
                    ((ek = e.key) == key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }
```

