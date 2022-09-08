package com.luzhi.lock;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhilu
 */
public class UsageReentrantWriteReadLock {

    /**
     * 生成一个可重入的读写锁
     */
    private static final ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    /**
     * 从{@link #READ_WRITE_LOCK} 生成读锁
     */
    private static final Lock READ_LOCK = READ_WRITE_LOCK.readLock();

    /**
     * 从{@link #READ_WRITE_LOCK} 生成写锁
     */
    private static final Lock WRITE_LOCK = READ_WRITE_LOCK.writeLock();

    /**
     * 生成int容器
     */
    private static final int[] INTS = new int[10];

    public static void main(String[] args) {
        new WriteWorkThread().start();
        new ReadWorkThread().start();
    }


    static class WriteWorkThread extends Thread {
        public WriteWorkThread() {
            super("write");
        }

        @Override
        public void run() {
            new ReadAndWriteWork().write(9);
        }
    }

    static class ReadWorkThread extends Thread {
        public ReadWorkThread() {
            super("read");
        }

        @Override
        public void run() {
            for (int num : new ReadAndWriteWork().read()) {
                System.out.print(num + " ");
            }
        }
    }

    static class ReadAndWriteWork {

        public void write(int index) {
            WRITE_LOCK.lock();
            try {
                System.out.format("write开始当前执行的线程是:%s, 时间是%s\n", Thread.currentThread().getName(), System.currentTimeMillis());
                if (index > -1 && index < INTS.length) {
                    INTS[index] = index + 1;
                } else {
                    throw new IndexOutOfBoundsException();
                }
            } finally {
                WRITE_LOCK.unlock();
                System.out.format("write开始当前执行的线程是:%s, 时间是%s\n", Thread.currentThread().getName(), System.currentTimeMillis());
            }
        }

        public int[] read() {
            READ_LOCK.lock();
            try {
                System.out.format("read开始当前执行的线程是:%s, 时间是%s\n", Thread.currentThread().getName(), System.currentTimeMillis());
                return Arrays.copyOf(INTS, INTS.length);
            } finally {
                READ_LOCK.unlock();
                System.out.format("read开始当前执行的线程是:%s, 时间是%s\n", Thread.currentThread().getName(), System.currentTimeMillis());
            }
        }
    }
}
