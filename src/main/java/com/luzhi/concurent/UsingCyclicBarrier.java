package com.luzhi.concurent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author zhilu
 */
public class UsingCyclicBarrier {

    public static void main(String[] args) {
        CyclicBarrierUsage.run();
    }

    @Slf4j
    static class CyclicBarrierUsage {

        /**
         * 设置屏障数为3(根据线程个数设置屏障数)
         */
        private static final CyclicBarrier CYCLIC_BARRIER = new CyclicBarrier(3);

        private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(3, 3, 10L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        public static void run() {
            EXECUTOR.submit(() -> {
                try {
                    log.info("A当前线程: {}, 执行任务一", Thread.currentThread().getName());
                    Thread.sleep(1000L);
                    CYCLIC_BARRIER.await();

                    log.info("A当前线程: {}, 执行任务二", Thread.currentThread().getName());
                    Thread.sleep(1000L);
                    CYCLIC_BARRIER.await();

                    log.info("A当前线程: {}, 执行任务三", Thread.currentThread().getName());
                } catch (InterruptedException | BrokenBarrierException exception) {
                    exception.printStackTrace();
                }
            });

            EXECUTOR.submit(() -> {
                try {
                    log.info("B当前线程: {}, 执行任务一", Thread.currentThread().getName());
                    Thread.sleep(2000L);
                    CYCLIC_BARRIER.await();

                    log.info("B当前线程: {}, 执行任务二", Thread.currentThread().getName());
                    Thread.sleep(2000L);
                    CYCLIC_BARRIER.await();

                    log.info("B当前线程: {}, 执行任务三", Thread.currentThread().getName());
                } catch (InterruptedException | BrokenBarrierException exception) {
                    exception.printStackTrace();
                }
            });

            EXECUTOR.submit(() -> {
                try {
                    log.info("C当前线程: {}, 执行任务一", Thread.currentThread().getName());
                    Thread.sleep(1000L);
                    CYCLIC_BARRIER.await();

                    log.info("C当前线程: {}, 执行任务二", Thread.currentThread().getName());
                    Thread.sleep(1000L);
                    CYCLIC_BARRIER.await();

                    log.info("C当前线程: {}, 执行任务三", Thread.currentThread().getName());
                } catch (InterruptedException | BrokenBarrierException exception) {
                    exception.printStackTrace();
                }
            });

            EXECUTOR.shutdown();
        }
    }
}
