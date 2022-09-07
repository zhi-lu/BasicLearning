package com.luzhi.concurent;

import cn.hutool.core.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhilu
 * <p>
 * 我们先聊一聊ConcurrentHasHMap1.7和ConcurrentHashMap1.8的一些基本特性{
 * 1: 对于所有的在CHM的数据都是被volatile所修饰的,即在内存中是随时可见的.
 * 2: 数据结构的不同 {对于CHM1.7来说它的数据结构是Segment分段锁和 数组 + 链表. 即Segment(锁 + 内部也维护的Entry数组) + HashEntry + {@link java.util.concurrent.locks.ReentrantLock}
 * 对于CHM1.8来说它的数据结构是 Node数组 + 链表 + 红黑树, 并发操作是由synchronized和CAS来实现的.}
 * 3: CHM1.7和CHM1.8的锁的粒度不同 {
 * 对于CHM1.7的锁粒度来说是去锁Segment.
 * 对于CHM1.8的锁粒度来说是去锁HashEntry链表和红黑树的首节点..
 * }
 * <p>
 * 谈一谈你对Segment的理解{
 * 我们CHM的优势就是使用了锁分段机制,每一个Segment是独立的,所以在Segment中进行的Segment读写操作是高度自治的.不会影响其他Segment
 * 段的读写操作的.类型一个二级Hash.
 * <p>
 * Segment继承我们的{@link java.util.concurrent.locks.ReentrantLock},用于存放HashEntry[]数组的.所以我们的CHM的底层并没有对
 * HahsMap进行扩展, 而是同样再底层中基于数组+链表实现的.
 * }
 * <p>
 * 谈一谈整CHM的设计思路{
 * 首先我们的CHM是采用分段锁机制,则不像其他的线程安全的Hash容器.例如像{@link java.util.Hashtable} or {@link java.util.Collections#synchronizedMap(Map)}
 * 在并发操作的时候,比如说put操作是对整个Hash容器进行加锁(synchronized).get的时候不能write, write的时候不能get.导致并发时仅有一个操作执行.从而导致并发效率低下.
 * 而我们CHM进行put操作的时候是先计算我们key-value数据的hash值,通过hashcode找到具体的Segment.如果当前没有其他的put操作对这个Segment.那它将数据put这个Segment中
 * 并且对这个Segment进行加锁.只有当其他线程对这个给Segment进行put操作.也就是我又要进行put操作,将另一条数据放入我们的CHM并且通过散列值算法数据的hashcode值
 * 正好是要放在该已经加锁的Segment下这个put操作才会阻塞.等上一个put完成时释放锁.才会将阻塞队列里的put操作任务唤醒.然后等待的put操作进行tryAcquire尝试获取到锁
 * 如果拿到锁执行put操作,没有拿到锁继续阻塞着.所以只要不是放在一个分段中,就没有锁竞争,实现真正的并行插入.相比于对整个Map加锁的设计,分段锁大大的提高了高并发环境下的处理能力.
 * 但同时,由于不是对整个Map加锁,导致一些需要扫描整个Map的方法(如size(), containsValue())需要使用特殊的实现,另外一些方法(如clear())甚至放弃了对一致性的要求(ConcurrentHashMap是弱一致性的).
 * }
 * <p>
 * 谈一谈CHM的构造方法参数{
 * one    ->     initialCapacity,代表的是HashEntry[]数组的大小,也就是ConcurrentHashMap的大小.初始化默认为16.
 * two    ->     loadFactor,负载因子,在判断扩容的时候用到,默认是0.75
 * three  ->     concurrencyLevel,并发级别,代表Segment[]数组的大小,也就是分段锁的个数,默认是16
 * 举一个例子,假如new ConcurrentHashMap(32, 0.75, 16)就是新建了一个ConcurrentHashMap,他的容量是32,分段锁的个数是16,也就是每个Segment里面HashEntry[]数组的长度是2.
 * }
 * <p>
 * 说一说get操作和操作的时候为什么不需要加锁{
 * 1: 先根据key计算出hashcode:
 * 2: 确定Segment数组的位置：hashcode&segments.length-1;
 * 3: 确定HashEntry数组的位置：hashcode&HashEntry.length-1
 * <p>
 * get()方法使用了Unsafe对象的getObjectVolatile()方法读取Segment和HashEntry,保证获取的值是最新的.
 * 但由于遍历过程中其他线程可能对链表结构做了调整,因此get和containsKey返回的可能是过时的数据,
 * 这一点是ConcurrentHashMap在弱一致性上的体现.如果要求强一致性,那么必须使用Collections.synchronizedMap()或者Hashtable方法.
 * 再整个get过程中使用了大量的volatile关键字,其实就是保证了可见性(加锁也可以,但是降低了性能),get只是读取操作,
 * 所以我们只需要保证读取的是最新的数据即可.
 * }
 * <p>
 * 问题一  JDK1.7中ConcurrentHashMap的底层原理
 * ConcurrentHashMap底层是由两层嵌套数组来实现的
 * ConcurrentHashMap对象中有一个属性segments, 类型为Segment[];
 * Segement对象中有一个属性table,类型为HashEntry[].
 * 当调用ConcurrentHashMap的put方法时,先根据key计算出对应的Segment[]的数组下表j.确定好当前key,value应该插入到哪个Segment对象中,如果Segments[j]数组为空,则利用自旋锁方式在j位置生成一个Segment对象.
 * <p>
 * 之后调用Segment对象的put方法.Segment对象的put方法会先加锁,然后根据key计算出对应的hashEntry[]的数组下表i,然后将key和value封装为HashEntry对象放入该位置,此过程和JDK7中的HashMap的put方法一样,然后解锁.
 * <p>
 * 问题二  JDK1.7中ConcurrentHashMap是如何保证线程安全的？
 * 主要利用Unsafe操作+ReetrantLock+分段思想.
 * <p>
 * 主要使用了Unsafe操作中的：
 * <p>
 * compareAndSwapObject：通过CAS操作修改对象的属性.
 * putOrderedObject:并发安全的给数组中的某个位置赋值:
 * getObjectVolatile：并发安全的获取数组中某个位置的元素.
 * 分段思想是为了提高ConcurrentHashMap的并发量,分段越高则代表支持的最大并发量越高.
 * <p>
 * 当调用ConcurrentHashMap的put方法,最终会调用到Segement的put方法,而Segment类继承了ReentrantLock,所以Segment自带可重入锁,当调用Segment的put方法时,
 * 会先利用可重入锁加锁,加锁成功后再将待插入的key,value插入到小型的HashMap中,插入完成后解锁.
 *
 * <p>===========================================================================================================</>
 * 问题一  JDK1.8中的ConcurrentHashMap是如何保证线程安全的？
 * 主要利用Unsafe中的CAS操作和synchronized关键字的.
 * UnSafe中的CAS操作的使用,主要负责并安全的修改对象的属性或数组某个位置的值.
 * synchronized关键字主要负责在需要操作某个位置时进行加锁（该位置不为空）,比如向某个位置的链表进行插入节点,向某个位置的红黑树插入节点.
 * 当向ConcurrentHashMap中put一个key,value时：
 * <p>
 * 首先根据key计算对应的数组下标i,如果该位置没有元素,则通过自旋的方法向该位置赋值.
 * 如果该位置有元素,则synchronized会加锁
 * 如果加锁成功之后,在判断该元素的类型
 * 如果是链表节点则进行添加链表节点到链表中
 * 如果是红黑树节点则添加节点到红黑树
 * 添加成功之后,判断是否需要红黑树话
 * addCount,concurrentHashMap的元素个数加1,并且这个操作也是需要并发安全的.并且元素个数加1成功后,会继续判断是否需要扩容,如果需要,则进行扩容.
 * 同时一个线程在put的时候发现当前ConcurrentHashMap正在进行扩容会去帮助进行扩容.
 * <p>
 * 问题二  JDK7和JDK8中的ConcurrentHashMap中的不同点？
 * JDK1.7使用了分段锁即可重入锁ReentrantLock, 而JDK8中没有分段锁了,而是使用synchronized来进行控制:即JDK1.8实现降低了锁的粒度,JDK1.7版本锁粒度是基于segment,包含多个hashentry的,而JDK1.8的锁的粒度是HashEntry(首节点)
 * JDK中的扩容性能更高,支持多线程同时扩容,实际上JDK1,7也支持多线程扩容,因为JDK7中的扩容是针对每个Segment,使用也可能多线程扩容,但是性能没有JDK1.8高,因为JDK8中对于任意一个线程都可以去帮助扩容:
 * JDK8中的元素个数统计的实现也不一样了,JDK8中增加了CounterCell来帮助计数,而JDK7中没有,JDK7中是put的时候每个Segment内部计数,统计的时候是遍历每个Segment对象加锁统计.
 * <p>
 * 问题三  扩容期间在未迁移到的hash桶中插入数据会怎么样？
 * 只要插入的位置扩容线程还未迁移到,就可以插入,当迁移到插入位置时,就会阻塞等待插入操作完成后再继续迁移.
 * <p>
 * 问题四  正在迁移的hash桶遇到get操作会怎么样？
 * 在扩容过程期间形成的链表是使用的类似于复制引用的方式,也就是说新生成的是复制出来的,而非原来的链表迁移过去的,所以原来 hash 桶上的链表并没有受到影响,
 * 因此如果当前节点有数据,还没迁移完成,此时不影响读,能够正常进行.如果当前链表已经迁移完成,那么头节点会被设置成fwd节点,此时get线程会帮助扩容.
 * <p>
 * 问题五  正在迁移的hash桶遇到put/remove操作会发生什么？
 * 如果当前链表已经迁移完成,那么头节点会被设置成fwd(路由节点)节点,此时写线程会帮助扩容,如果扩容没有完成,当前链表的头节点会被锁住,所以写线程会被阻塞,直到扩容完成.
 * <p>
 * 问题六  如果lastRun节点正好在一条全部都为高位或者全部都为低位的链表上,会不会形成死循环？
 * 在数组长度为64之前会导致一直扩容,但是到了64或者以上后就会转换为红黑树,因此不会一直死循环.
 * <p>
 * 问题八  并发情况下,各线程中的数据可能不是最新的,那为什么get方法不需要加锁？
 * get操作全程不需要加锁是因为Node的成员val是用volatile修饰的,在多线程环境下线程A修改结点的val或者新增节点的时候是对线程B可见的.
 * <p>
 * 问题九  ConcurrentHashMap 和 Hashtable 的区别?
 * ConcurrentHashMap 和 Hashtable 的区别主要体现在实现线程安全的方式上不同.
 * 底层数据结构：
 * JDK1.7的 ConcurrentHashMap 底层采用 分段的数组+链表 实现,JDK1.8 采用的数据结构跟HashMap1.8的结构一样,数组+链表/红黑二叉树.Hashtable是采用 数组+链表 的形式.
 * 实现线程安全的方式（重要）： 1 在JDK1.7的时候,ConcurrentHashMap（分段锁） 对整个桶数组进行了分割分段(Segment),每一把锁只锁容器其中一部分数据,多线程访问容器里不同数据段的数据,就不会存在锁竞争,提高并发访问率.
 * 到了 JDK1.8 的时候已经摒弃了Segment的概念,而是直接用 Node数组+链表+红黑树的数据结构来实现,并发控制使用 synchronized和CAS来操作.synchronized只锁主链表或者红黑树的首节点.
 * 只要首节点hash不产生冲突,那么就不存在并发问题.
 * 2:Hashtable(同一把锁): 使用synchronized来保证线程安全,效率非常低下.当一个线程访问同步方法时,其他线程也访问同步方法,可能会进入阻塞或轮询状态,如使用 put 添加元素,另一个线程不能使用 put 添加元素,也不能使用 get,竞争会越来越激烈效率越低.
 * <p>
 * 问题十  ConcurrentHashMap 和 HashMap 的相同点和不同点
 * 相同之处：
 * 都是数组 +链表+红黑树的数据结构（JDK8之后）,所以基本操作的思想一致
 * 都实现了Map接口,继承了AbstractMap 操作类,所以方法大都相似,可以相互切换
 * <p>
 * 不同之处：
 * ConcurrentHashMap 是线程安全的,多线程环境下,无需加锁直接使用
 * ConcurrentHashMap 多了转移节点(fwd),主要用户保证扩容时的线程安全.
 * <p>
 * 扩容过程中,读访问能否访问的到数据？怎么实现的？
 * 可以的.当数组在扩容的时候,会对当前操作节点进行判断,如果当前节点还没有被设置成fwd(路由节点或者转移节点)节点,那就可以进行读写操作.
 * 如果该节点已经被处理了,那么当前线程也会加入到扩容的操作中去.
 * <p>
 * 为什么超过冲突超过8才将链表转为红黑树而不直接用红黑树?
 * 默认使用链表,链表占用的内存更小.
 * 正常情况下,想要达到冲突为8的几率非常小,如果真的发生了转为红黑树可以保证极端情况下的效率
 * <p>
 * 我们的ConcurrentHashMap和HashMap的扩容有什么不同？
 * HashMap的扩容是创建一个新数组,将值直接放入新数组中,JDK7采用头链接法,会出现死循环,JDK8采用尾链接法,不会造成死循环
 * ConcurrentHashMap 扩容是从数组队尾开始拷贝,拷贝节点时会锁住节点,拷贝完成后将节点设置为转移节点(fwd).所以槽点拷贝完成后将新数组赋值给容器
 * <p>
 * 我们的ConcurrentHashMap是如何发现当前槽点正在扩容的？
 * ConcurrentHashMap 新增了一个节点类型,叫做转移节点(fwd),当我们发现当前槽点是转移节点时(转移节点的hash值是-1）,即表示Map正在进行扩容.
 * <p>
 * 谈一谈CAS算法在ConcurrentHashMap中的应用
 * CAS是一种乐观锁,在执行操作时会判断内存中的值是否和准备修改前获取的值相同,如果相同,把新值赋值给对象,否则赋值失败,整个过程都是原子性操作,无线程安全问题
 * ConcurrentHashMap的put操作是结合自旋用到了CAS,如果hash计算出的位置的槽点值为空,就采用CAS+自旋进行赋值,如果赋值是检查值为空,就赋值,如果不为空说明有其他线程先赋值了,放弃本次操作,进入下一轮循环
 * <p>
 * JDK7的put过程
 * 首先对key进行第一次hash,通过hash值确定segment的值:
 * 如果此时segment未初始化,则利用自旋CAS操作来创建对应的segment:
 * 获取当前segment的hashentry数组后进行对key进行第2次hash,通过值确定在hashentry数组的索引位置.
 * 通过继承ReetrantLock的tryLock方法尝试去获取锁,如果获取成功就直接插入相应的位置,如果已经有线程获取该segment的锁,
 * 那么当前线程会以自旋的方式去继续调用tryLock方法去获取锁,超过指定次数就挂起,等待唤醒.然后对当前索引的hashentry链进行遍历,如果有重复的key,则替换:如果没有重复的,则插入到链头.
 * 释放锁.
 * <p>
 * JDK8的put过程:
 * 如果没有初始化就先调用initTable()方法对其初始化:
 * 对key进行hash计算,求得值没有哈希冲突的话,则利用自旋CAS操作来进行插入数据:
 * 如果存在hash冲突,那么就加synchronized锁来保证线程安全
 * 如果存在扩容,那么就去协助扩容
 * 加完数据之后,再判断是否还需要扩容
 * <p>
 * JDK7的get过程:
 * 与JDK7的put过程类似,也是需要两次hash,不过不需要加锁,因为将存储元素都标记成了volatile,对内存都是可见性的.
 * <p>
 * JDK8的get过程:
 * 计算hash值,定位table索引位置,也是不需要加锁,通过volatile关键字进行保证了.
 * @see java.util.Map
 * @see sun.misc.Unsafe
 * @see java.util.HashMap
 * @see java.util.Hashtable
 * @see java.util.Collections
 * @see java.util.concurrent.ConcurrentHashMap
 */
public class ConcurrentHashMapJdkEight {

    public static final int LOOP_NUMBER = 0xfff >>> 2;

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1 << 3, 1 << 10, 20L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        Command command = new Command();
        RandomStringAndValue randomStringAndValue = new RandomStringAndValue();
        try {
            for (int i = 0; i < LOOP_NUMBER; i++) {
                executor.execute(command);
            }
        } finally {
            System.out.format("执行成功,多线程执行的操作为:%d次. 容器包含的大小为:%d个.\n", command.getAtomicIntegerValue(), command.mapSize());
        }

        try {
            for (int i = 0; i < LOOP_NUMBER; i++) {
                executor.execute(randomStringAndValue);
            }
        }finally {
            executor.shutdown();
            System.out.format("执行成功,多线程操作现在容器的大小为: %d", randomStringAndValue.getSize());
        }
    }

    static class Command implements Runnable {

        private final Map<String, Integer> STRING_INTEGER_MAP = new HashMap<>(16);

        private final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

        @Override
        public void run() {
            synchronized (this) {
                int num = ATOMIC_INTEGER.get();
                String key = "key -- " + num;
                STRING_INTEGER_MAP.put(key, num);
                ATOMIC_INTEGER.getAndIncrement();
            }
        }

        public int getAtomicIntegerValue() {
            return ATOMIC_INTEGER.get();
        }

        public int mapSize() {
            return STRING_INTEGER_MAP.size();
        }
    }


    static class RandomStringAndValue implements Runnable {

        private static final int STRING_RANDOM_LEN = 6;

        private final Map<String, Integer> map = new ConcurrentHashMap<>(16, 0.75f);

        @Override
        public void run() {
            map.put(RandomUtil.randomString(STRING_RANDOM_LEN), RandomUtil.randomInt());
        }

        public int getSize() {
            return map.size();
        }
    }
}

