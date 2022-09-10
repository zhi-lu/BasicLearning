package com.luzhi.lock;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhilu
 */
public class ReplaceWaitNotify {

    private static final Lock LOCK = new ReentrantLock();
    private static final Condition CONDITION = LOCK.newCondition();
    private static final Queue<String> QUEUE = new LinkedList<>();

    public static void main(String[] args) throws InterruptedException {
        new ThreadAwait("await").start();
        new ThreadSingal("singal").start();
    }


    static class ThreadSingal extends Thread {
        public ThreadSingal(String threadName) {
            super(threadName);
        }

        @Override
        public void run() {
            new ReplaceImplement().addStringAndSingal("luzhi");
        }
    }

    static class ThreadAwait extends Thread {
        private ThreadAwait(String threadName) {
            super(threadName);
        }

        @Override
        public void run() {
            try {
                String string = new ReplaceImplement().getStringAndAwait();
                System.out.println("我的名字是:" + string);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    static class ReplaceImplement {
        public void addStringAndSingal(String string) {
            LOCK.lock();
            try {
                // 将等待的线程进行唤醒.如果当前线程唤醒和其他线程并且持有同一个Condition所关联的锁。则将选择一个进行唤醒.
                System.out.format("singal开始当前执行的线程是:%s, 时间是%s\n", Thread.currentThread().getName(), System.currentTimeMillis());
                Thread.sleep(3000L);
                QUEUE.add(string);
                CONDITION.signal();
                System.out.format("singal结束当前执行的线程是:%s, 时间是%s\n", Thread.currentThread().getName(), System.currentTimeMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                LOCK.unlock();
            }
        }

        public String getStringAndAwait() throws InterruptedException {
            LOCK.lock();
            try {
                // 使当前线程一直等待,直到它发出信号或中断.或者指定的等待时间过去.
                System.out.format("await开始当前执行的线程是:%s,   时间是%d\n", Thread.currentThread().getName(), System.currentTimeMillis());
                while (QUEUE.size() <= 1) {
                    if (!CONDITION.await(2L, TimeUnit.SECONDS)) {
                        break;
                    }
                }
                System.out.format("await结束当前执行的线程是:%s,   时间是%d\n", Thread.currentThread().getName(), System.currentTimeMillis());
                return QUEUE.poll() == null ? "null" : QUEUE.poll();
            } finally {
                LOCK.unlock();
            }
        }
    }
}
