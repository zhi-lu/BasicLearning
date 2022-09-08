package com.luzhi.lock;

import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhilu
 */
public class UsageLock {

    private static final int THREAD_LOOP_NUMBER = 0xfff >>> 2;
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(10, 1 << 10, 5L, TimeUnit.SECONDS, new SynchronousQueue<>(),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args) {
        executeTryLock();
    }

    public static void execute() {
        UsageReentrantLock lock = new UsageReentrantLock();
        try {
            for (int i = 0; i < THREAD_LOOP_NUMBER; i++) {
                EXECUTOR.execute(lock);
            }
        } finally {
            EXECUTOR.shutdown();
            System.out.format("输出操作%d次后,number的值为:%d\n", THREAD_LOOP_NUMBER, lock.getNumber());
        }
    }


    public static void executeNotLock(){
        NotUsingLock notUsingLock = new NotUsingLock();
        try {
            for (int i = 0; i < THREAD_LOOP_NUMBER; i++) {
                EXECUTOR.execute(notUsingLock);
            }
        }finally {
            EXECUTOR.shutdown();
            System.out.format("输出操作%d次后,不加锁的number的值是:%d\n", THREAD_LOOP_NUMBER, notUsingLock.getNumber());
        }
    }

    private static void executeTryLock(){
        UsingTryLock tryLock = new UsingTryLock();
        try {
            for (int i = 0; i < THREAD_LOOP_NUMBER; i++) {
                EXECUTOR.execute(tryLock);
            }
        }finally {
            EXECUTOR.shutdown();
            System.out.format("输出操作%d次后,tryLock的number的值是:%d\n", THREAD_LOOP_NUMBER, tryLock.getNumber());
        }
    }

    private static class UsageReentrantLock implements Runnable {

        private static final Lock LOCK = new ReentrantLock(false);

        private int number = 0;

        @Override
        public void run() {
            LOCK.lock();
            try {
                number += 1;
            } finally {
                LOCK.unlock();
            }
        }

        public int getNumber() {
            return number;
        }
    }

    private static class NotUsingLock implements Runnable{

        private int number = 0;

        @Override
        public void run() {
            number += 1;
        }

        public int getNumber() {
            return number;
        }
    }

    private static class UsingTryLock implements Runnable{

        private static final Lock LOCK = new ReentrantLock();

        private static final long BLOCK_TIME = 1L;

        private int number = 0;

        @Override
        public void run() {
            try {
                if (LOCK.tryLock(BLOCK_TIME, TimeUnit.SECONDS)){
                    try {
                        number += 1;
                    }finally {
                        LOCK.unlock();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public int getNumber() {
            return number;
        }
    }
}
