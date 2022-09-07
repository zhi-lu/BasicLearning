package com.luzhi.container;

import cn.hutool.core.util.RandomUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zhilu
 * @version jdk1.8
 * <p>
 * 我们先聊一聊HashMap1.8中的特性{
 * 1: 对于HashMap1.8来说,当链表的长度达到8并且数组table长度大于等于64的时候会升级成为红黑树.当我们的桶上的TreeBin个数小于6时会进行收缩将红黑树转化为链表.
 * 2: 对于默认的HashMap构造方法来说(不指定初始大小).则默认容积为16,并且负载因子为0.75,和jdk1.7版本相同.
 * 3: 对于HashMap1.7来说他在多线程的情况下,具有死循环,数据丢失,数据覆盖这些问题.但我们的HashMap1.8解决了死循环问题和数据丢失问题.但任然没有办法解决
 * 数据覆盖的问题(任然具有多线程的并发问题).
 * }
 * 问:谈一谈你对HashMap1.7和1.8的结构和插入数据的方法包括底层结构的实现原理.{
 * 回答: HashMap1.7和1.8的结构区别. HashMap1.7使用数据结构是: 数组 + 链表.
 * HashMap1.8的数据结构是 数据 + 链表 + 红黑树.
 * <p>
 * HashMap1.7 采用的是头插入法, 是先扩容再插入数据,扩容时需要重新rehash(重新计算hash值)
 * HashMap1.8 采用的是尾插入法, 先进行插入然后再进行扩容, 扩容时不需要重新计算hash值.
 * <p>
 * HashMap1.7为什么要使用数组 + 链表的存储结构.因为我们数组中存储的是key-value(HashEntry or Node)这样的实例.我们在put操作的时候会计算我们key的hash值
 * 通过{@link HashMap#hash(Object)}方法计算我们的key的hash值.由于数组长度(桶数)是有限的,在有限的长度下我们使用hash,由于hash具有随机性.
 * 当不同的key值的hash值相同时则会出现hash碰撞(冲突).就会出现链表(我们hashmap解决hash冲突的方式是通过链地址法).
 * <p>
 * HashMap1.8为什么要在在1.7版本上加上红黑树.因为在原有的基础上.当一个桶数的链表很长时,他查询数据的效率就会比较慢.我们get
 * 操作先计算key的hash值然后在找到对应的数组(桶数)位置.然后再进行equals值比较的方法找到具体的key-value(实体数据HashEntry或者Node)数据.然后返回具体的key
 * 然后通过已经找出的实体(HashEntry或者Node)从而找到key所映射的value值.由于我们链表查询的效率比较低,时间复杂度为O(n),所以我们引用树型结构去在一定的情况下由链表变为红黑树结构.
 * 红黑树查询的效率很高,时间复杂度为O(lgn).而且红黑树虽然属于二叉查找树.但是普通的二叉查找树的时间复杂度会受到其树深度的影响,而红黑树可以保证在最坏情况下的时间复杂度仍为O(lgn).
 * 当数据量多到一定程度时,使用红黑树比"二叉查找树"的效率要高.同"平衡二叉查找树(AVL)"相比较,红黑树没有像"平衡二叉树"对平衡性要求的那么苛刻,
 * 虽然两者的时间复杂度相同,但是红黑树在实际测算中的速度要更胜一筹！
 * 备注:平衡二叉树的时间复杂度是O(logn),红黑树的时间复杂度为O(lgn),两者都表示的都是时间复杂度为对数关系（lg函数为底是10的对数,用于表示时间复杂度时可以忽略）
 * <p>
 * }
 * <p>
 * 问:谈一谈什么是hash碰撞(冲突)该如何控制hash冲突频率{
 * 首先我们先了解hash算法.
 * 什么是hash算法:      是把输入的值通过散列值算法计算出的一个散列值.
 * 什么是hash表:        hash表又叫做"散列表",它是通过key直接访问到内存存储位置的数据结构,在具体实现上,我们通过hash函数把key映射到表中某个位置,来获取这个位置的数据,从而加快数据的查找.
 * 什么是hash碰撞:      在计算hash地址的过程中会出现对于不同的关键字出现相同的哈希地址的情况,即key1 ≠ key2,但是f(key1) = f(key2),这种情况就是Hash 碰撞
 * 如何控制hash冲突频率: 通过好的hash算法和扩容机制.
 * }
 * <p>
 * 问:如何解决hash碰撞{
 * one   ->   开放地址法: 也称为线性探测法,就是从发生冲突的位置开始,按照一定次序(顺序)从hash表找到一个空闲位置,把发生冲突的元素存到这个位置.ThreadLocal就是用这种方法解决hash碰撞.
 * two   ->   链地址法:   就是把冲突的key,以单向链表来进行存储,比如HashMap
 * three ->   再哈希法:   就是存在冲突的时候,再hash,一只运算知道不再产生冲突
 * }
 * <p>
 * 问:谈一谈你对HashMap1.7和1.8的扩容机制{
 * 回答:他们的扩容时机相同.
 * 1:数组为空的时候也就是存放数据的数组table==null或者桶数为0时数组table.length==0时
 * 2:元素个数超过我们自定义定义的或者默认定义的数组长度*负载因子的时候.
 * 3:当链表长度大于8并且数组长度小于64的时候
 * <p>
 * 他们如何扩容.
 * 先创建一个新的Entry空数组,长度是原数组的2倍,遍历原Entry数组.如果oldTab[i]只有一条数据,没有形成链表,那么直接放入新的table中进行存放数据.
 * 如果有多个数据的链表直接存放链表头数据即可(链表通过指针确保元素之间的线性关系).
 * newTab[e.hash & (newCap - 1)]当前节点存放的hash&新数组容量-1,对于jdk1.8中无需在rehash(重新计算key的hash值).
 * <p>
 * 为什么扩容时2的幂等次方.
 * HashMap的容量是2的n次幂时,存放时二进制0000000000000001000这种型式,,"这样与添加元素的hash值进行位运算时,能够充分的散列",使得添加的元素均匀分布在HashMap的每个位置上,
 * 减少hash碰撞(冲突).
 * }
 * <p>
 * 问:谈一谈HashMap的put操作干什么的如何实现的？{
 * 回答:首先put操作是为了将我们的指定的值与映射中指定的键相关联,如果该映射中先前包含了改键的映射,则替换之前键所映射的旧值.
 * 实现原理需要结合源码来看.大体思路就是将映射put判断table是不是为空,table长度是否为零.如果是的话进行扩容.不是通过{@link HashMap#hash(Object)}
 * 计算key的hash值.判断是否已经存在这个映射.如果存在直接覆盖旧值,如果不存在直接插入.再插入的过程中需要判断是否超过最大容积.如果是进行扩容.
 * 再具体插入还需要当前插入容器是红黑树还是链表,如果是红黑树则直接插入,如果是链表判断链表长度是否超过8,数组桶数超过64然后通过这些条件是否让链表转换为红黑树.
 * 链表长度是超过8,数组桶数超过64则转换为红黑树,大体操作将我们Node节点进行treeifyBin操作然后生成具体的TreeBin.通过一定的算法将链表生成红黑树.
 * 否则只是进行扩容.
 * <p>
 * }
 * 问:说一下HashMap的key能存储null吗?key最好用什么类型的值.{
 * 答案: one -> 再HashMap中key可以是为null,包括value也可以为null(因为既然是线程不安全的大多数情况下在单线程下使用.不存在key的二义性)
 * 而ConcurrentHashMap中的key和value都不可以是null(由于是线程安全的大多数在多线程情况下使用.会存在key的二义性).
 * <p>
 * two -> 对于HashMap的key来说,他的值类型最好是像{@link Integer}or{@link String}不可变的类型.他们值的hash是重写的hashcode()
 * 方法进行算出来的.你Integer值是什么那么你的hashcode值是固定不变的,同理对于String对象来说你的值是什么你的hashcode也不会发生改变.
 * String类最为常用,例如Spring的三级缓存.如果用可变类型的value作为key.则很有可能发生我们的key的hashcode会发生变化.
 * 导致我们put的数据get不出来.
 * <p>
 * }
 * <p>
 * 问:为什么程序中不能出现死循环,那他的坏处是什么该如何排查死循环.{
 * 答案: 如果程序中出现了死循环.则会导致我们服务器的性能会大大的下降,甚至是服务器直接宕机.
 * 解决 unix(mac liunx) 通过lsof 或者 ps命令去排查找出我们死循环的进程(死循环进程cpu占比高所以非常容易找到).
 * 然后通过kill -9 命令杀死死循环的进程.
 * nt(windows) 通过powershell中的get-process 找到我们的死循环的进程(死循环进程cpu占比高所以非常容易找到)
 * 然后通过Stop-Process命杀死死循环的进程.
 * 但以上是解决的方法,很多时候需要我们在coding中要想好再去写code.要尽可能去避免死循环发生.
 * }
 * <p>
 * 问:什么是桶数和负载因子？为什么负载因子默认是0.75,这么做有什么好处.{
 * 回答: 容量是哈希表中的桶数,初始容量只是哈希表创建时的容量.负载因子是哈希表在其容量自动增加之前允许达到的程度的度量.
 * 当哈希表中的条目数超过负载因子和当前容量的乘积时,对哈希表进行重新哈希(即重建内部数据结构),使哈希表具有大约两倍的桶数.
 * 作为一般规则,我们将HashMap的loadfactory设置为0,75是为了更好折中容器的空间和时间成本关系.较高的值会减少空间开销(产生的hash冲突的概率变大),但会增加查找成本
 * (反映在HashMap类的大多数操作中,包括get和put).较低的值会虽然会减少时间的开销(hash碰撞(冲突)减少),但会造成的空间消耗较大.具体看自己的事务要求.
 * 比如说如果迭代性能很重要,则不要将初始容量设置得太高(或负载因子太低).这一点非常重要.
 * }
 * <p>
 * 问:谈一谈HashMap的并发问题{
 * one   -> HashMap是线程安全的吗?   答案: HashMap不是安全的.
 * two   -> HashMap为什么线程不安全? 答案: HashMap1.7版本存在在多线程情况下会出现死循环,数据丢失,数据覆盖这些并发问题.
 * 这些问题是由于1.7版本在多线程扩容所引起的链表顺序倒置,从而出现了上述那些并发问题.
 * 而HashMap1.8版本通过尾插法解决了死循环数据丢失.但任然存在数据覆盖这种并发问题.
 * 在put操作的过程中没有使用同步锁,从而导致出现数据覆盖这种情况.
 * three ->如何解决HashMap的线程不安全问题呢? 答案: i: 使用Hashtable来取代我们的HashMap容器,但Hashtable解决并发问题是通过
 * 直接在操作方法上加上重量级锁synchronized.简单粗暴,但效率低下.参考synchronized底层实现机制.
 * ii: 使用{@link java.util.Collections#synchronizedMap(Map)}.原理生成一个普通的map和mutex(互斥锁)
 * (如果在构造方法中传入了mutex则使用传入的mutex否则则默认生成一个mutex并且该互斥锁对象的类型就是我们{@link java.util.Collections})
 * 的内部静态类对象SynchronizedMap对象,在相关操作方法上加上重量级锁synchronized,锁这个被定义的互斥锁对象.来确保在并发操作情况下。能够达到
 * 线程安全.所以并发效率并不高.
 * iii: 使用并发包(juc)下的{@link java.util.concurrent.ConcurrentHashMap},在实际开发中由并发的需求
 * 则使用ConcurrentHashMap来创建我们的hash容器(Map对象)(Spring三级缓存的一级缓存使用ConcurrentHashMap).
 * 相比较上面两种在确保安全的情况下能提高一定的并发效率.但不能保证一些方法能够是强一致性的.
 *
 * @see Map
 * @see java.util.HashMap
 * @see java.util.Hashtable
 * @see java.util.Collections
 * @see java.util.concurrent.ConcurrentHashMap
 */
public class HashMapJdkEight {

    protected static final int THREAD_LOOP_NUMBER = 0xffffff >>> 2;


    public static void main(String[] args) {
        execute();
    }

    public static void execute() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 1 << 10, 10L, TimeUnit.SECONDS, new SynchronousQueue<>(), Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
        RandomStringAndValue randomStringAndValue = new RandomStringAndValue();

        try {
            for (int i = 0; i < THREAD_LOOP_NUMBER; i++) {
                executor.execute(randomStringAndValue);
            }
        } finally {
            executor.shutdown();
            System.out.format("执行成功,多线程操作现在容器的大小为: %d", randomStringAndValue.getSize());
        }
    }

    static class RandomStringAndValue implements Runnable {

        private static final int STRING_RANDOM_LEN = 6;

        private final Map<String, Integer> map = new HashMap<>(16);

        @Override
        public void run() {
            map.put(RandomUtil.randomString(STRING_RANDOM_LEN), RandomUtil.randomInt());
        }

        public int getSize() {
            return map.size();
        }
    }
}

