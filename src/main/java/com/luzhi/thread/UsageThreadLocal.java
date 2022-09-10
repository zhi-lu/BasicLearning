package com.luzhi.thread;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhilu
 * @version jdk1.8
 * <p>
 * 对{@link ThreadLocal}的基本使用和对应的使用场景.以及底层实现.如何解决内存泄露问题.
 * 使用场景:{
 * 此类提供线程的局部变量,这个变量只能在这个线程被读写.在其他线程内是无法被访问的.因为每个访问一个(通过它的get或set方法)的线程都有它自己的、
 * 独立初始化的变量副本. <br>ThreadLocal</br>实例通常是希望将状态与线程相关联的类中的私有静态字段(例如,用户 ID 或事务 ID).
 * <p>
 * 变量分为局部变量和全局变量,局部变量的使用一般不涉及到多线程读写的并发问题---(如脏读,脏写). 全局变量如果设计到多线程对象的并发
 * 问题则使用同步操作来保证我们线程安全读写操作.
 * <p>
 * 对于<br>ThreadLocal</br>来说它的使用场景为: 就是当我们只想在本身的线程内使用的变量,可以用ThreadLocal来实现,并且这些变量是和线程的生命周期密切相关的,
 * 线程结束,变量也就销毁了.
 * <p>
 * 所以<br>ThreadLocal</br>不是为了解决线程之间的共享变量,如果是多线程访问共享变量,则使用同步机制来确保变量读写的安全.
 * }<br>
 * <p>
 * 底层实现:{
 * 首先{@link ThreadLocal}是一个泛型类,能接受任何类型的对象.一个线程可以有多个{@link ThreadLocal}对象.
 * 通过{@link ThreadLocal#set(Object)} or {@link ThreadLocal#get()}方法,设置ThreadLocal内部的值或者获取值. 实际是通过{@link ThreadLocal}的内部静态类<tt>ThreadLocalMap</tt>
 * 的"get() -> 实际为getEntry(), set()" 的方法设置值和获取值. 所以，我们的值保存没有保存到{@link ThreadLocal}中.实际上
 * 我们变量的值是保存到内部静态类<tt>ThreadLocalMap</tt>中. 相对于我们的{@link ThreadLocal}只是一个中间工具,用来传递值.
 * }<br>
 * <p>
 * 如何解决内存泄露问题{
 * 如何出现内存泄露问题的呢? 因为对于我们的{@link ThreadLocal}来说,其内部静态类<tt>ThreadLocalMap</tt>的key -> {@link ThreadLocal}对象
 * 为弱引用. 而我们的value是一个强引用.当出现垃圾回收的时候会回收key.此时就会出现key为null的value这种情况.
 * <p>
 * ThreadLocalMap 实现中已经考虑了这种情况, 在调用 {@code #get(), #set(), #remove()} 方法的时候. 会清理掉key为null的记录.
 * 如果说会出现内存泄漏, 那只有在出现了key为null情况后. 没有手动调用{@code #remove()}方法. 并且之后也不再调用{@code #get(), #set(), #remove()} 方法的情况下.
 * }
 * <p>
 * 总结{
 * one -> 在使用{@link ThreadLocal}的时候,最好声明为静态类型.
 * two -> 在用完我们的{@code ThreadLocal}的时候,最好手动释放调用{@link ThreadLocal#remove()}方法.
 * }
 */
public class UsageThreadLocal {

    private static final ThreadLocal<String> THREAD_LOCAL = ThreadLocal.withInitial(() -> "init value");

    public static void main(String[] args) {
        run();
        runNoStaticThreadLocal();
        usuallyUsingThreadLocal();
    }

    protected static void run() {
        String name = Thread.currentThread().getName();
        System.out.println("当前线程: " + name + ", 输出线程中的变量值为:" + THREAD_LOCAL.get());
        // 设置值.
        THREAD_LOCAL.set("new value");
        // 获取值.
        System.out.println("当前线程: " + name + "输出更改后线程变量的值为:" + THREAD_LOCAL.get());
        THREAD_LOCAL.remove();
        System.out.println("当前执行的线程: " + name + ", 移除后线程变量的值为:" + THREAD_LOCAL.get());
    }

    protected static void runNoStaticThreadLocal() {
        String name = Thread.currentThread().getName();
        // 创建, 创建成功并初始化值.
        ThreadLocal<String> local = ThreadLocal.withInitial(() -> name + " init value");
        local.remove();
        System.out.println("输出值为: " + local.get());
    }


    protected static void usuallyUsingThreadLocal() {
        new ThreadId("Thread--One").start();
    }

    private static class ThreadId extends Thread {

        /**
         * 设置线程的初始ID为0
         */
        private static final AtomicInteger THREAD_ID = new AtomicInteger(1);

        /**
         * 通常使用{@link ThreadLocal}来生成唯一标识.
         */
        private static final ThreadLocal<Integer> THREAD_LOCAL = ThreadLocal.withInitial(THREAD_ID::get);

        public ThreadId(String name) {
            super(name);
        }

        @Override
        public void run() {
            System.out.format("当前线程名: %s, 线程ID是: %d", Thread.currentThread().getName(), THREAD_LOCAL.get());
        }
    }
}
