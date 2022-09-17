package com.luzhi.concurent;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author zhilu
 * @version jdk1.8
 * <p>
 * 并发编程的三大核心: 分工, 同步, 互斥. 在日常开发中,经常会碰到需要在主线程中开启多个子线程去并行的执行任务,
 * 并且主线程需要等待所有子线程执行完毕再进行汇总的场景,这就涉及到分工与同步.
 */
public class UsingCountDownLatch {

    public static void main(String[] args) {
        new CountDownLatchUsage().start();
    }

    @Slf4j
    static class CountDownLatchUsage extends Thread {
        public CountDownLatchUsage() {
            super("CountDownLatchSubjectClassThread");
        }

        @Override
        public void run() {
            CountDownLatch downLatch = new CountDownLatch(3);
            ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 10L, TimeUnit.SECONDS, new LinkedBlockingDeque<>(),
                    Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

            executor.submit(() -> {
                try {
                    log.info("begin task ...");
                    downLatch.await();
                    log.info("end task ...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            executor.submit(() -> {
                try {
                    log.info("begin..., Currently latch value is {}", downLatch.getCount());
                    sleep(1);
                    log.info("end..., Currently latch value is {}", downLatch.getCount());
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                } finally {
                    downLatch.countDown();
                }
            });

            executor.submit(() -> {
                try {
                    log.info("begin..., Currently latch value is {}", downLatch.getCount());
                    sleep(2);
                    log.info("end..., Currently latch value is {}", downLatch.getCount());
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                } finally {
                    downLatch.countDown();
                }
            });

            executor.submit(() -> {
                try {
                    log.info("begin..., Currently latch value is {}", downLatch.getCount());
                    sleep(3);
                    log.info("end..., Currently latch value is {}", downLatch.getCount());
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                } finally {
                    downLatch.countDown();
                }
            });

            executor.shutdown();
        }
    }
}
