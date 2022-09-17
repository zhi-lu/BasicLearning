package com.luzhi.concurent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhilu
 * <p>
 * 同步基础知识补充.
 *
 * 补充:
 *  到底什么是内存屏障, 内存屏障有什么用{
 *  CPU指令load Barrier和 store Barrier构成内存屏障, 内存屏障的作用是禁止指令进行重排序,
 *  以及多线程在使用共享变量的时候,当一个线程发现缓存中的数据被另一个线程所修改.那么我们的内存屏障
 *  会把修改后的值强制加载到主内存中,并且将之前线程的缓存行数据进行失效.
 * }
 */
public class ConcurrentBasicKnowledge {

    public static void main(String[] args) throws InterruptedException {
        AsIfSerial serial = new AsIfSerial();
        serial.runNoSerial();
        serial.runSerial();

        ///////////////////////////////////////////////////////////////////////////

        HappensBefore happensBefore = new HappensBefore();
        happensBefore.start();
    }

    /**
     * @author zhilu
     * @version jdk1.8
     * <p>
     * 什么是as-if-serial?
     * 答:无论你编译器还是解释器如何执行指令,如何进行指令重排序,在单线程下执行指令获取的结果不能收到影响.
     * (没有数据依赖的两条指令是可以进行重排序的)
     */
    static class AsIfSerial {

        public void runNoSerial() {
            float pi = 3.14f;
            float i = 2.0f;
            float area = pi * i * i;
            System.out.println("输出面积是:" + area);
        }

        public void runSerial() {
            float i = 2.0f;
            float pi = 3.14f;
            float area = i * i * pi;
            System.out.println("输出面积是:" + area);
        }
    }

    /**
     * @author zhilu
     * @version jdk1.8
     * <p></p>
     * 什么是happens-before?
     * 总结:happens-before规则程序在多线程操作上体现,A线程操作happens-before B线程操作,则A线程操作的
     * 结果对于B线程是可见的,而且A线程操作执行顺序优先B线程操作执行.
     */
    static class HappensBefore extends Thread {

        private volatile int num = 0;

        private static final int LOOP_NUMBER = 0xfff >> 2;

        private final AtomicInteger atomicInteger = new AtomicInteger(0);

        private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(12, 12, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());


        @Override
        public void run() {
            for (int i = 0; i < LOOP_NUMBER; i++) {
                EXECUTOR.execute(() -> {
                    atomicInteger.getAndIncrement();
                    add();
                });
            }
            EXECUTOR.shutdown();
            System.out.format("输出num的值为 %d, atomicInteger值为: %d\n", num, atomicInteger.get());
        }


        public synchronized void add() {
            num++;
        }
    }
}
