package com.luzhi.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhilu
 */
public class StopTheWorld {
    public static class ExecuteThread extends Thread {

        public static final int LOOP_NUMBER = 10000;

        List<byte[]> list = new ArrayList<>();

        @Override
        public void run() {
            try {
                for (; ; ) {
                    // 产生垃圾
                    for (int i = 0; i < LOOP_NUMBER; i++) {
                        byte[] buffer = new byte[1024];
                        list.add(buffer);
                    }

                    for (int i = 0; i < LOOP_NUMBER; i++) {
                        if (list.size() > LOOP_NUMBER) {
                            list.clear();
                            // 触发gc回收,虚拟机进入(The Stop World)
                            System.gc();
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }


    public static class InterceptorTime extends Thread {
        private static final long START_TIME = System.currentTimeMillis();

        @Override
        public void run() {
            try {
                for (; ; ) {
                    long time = System.currentTimeMillis() - START_TIME;
                    System.out.printf("%d.%d\n+", time / 1000, time % 1000);
                    Thread.sleep(1000);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ExecuteThread executeThread = new ExecuteThread();
        InterceptorTime interceptorTime = new InterceptorTime();
        executeThread.start();
        interceptorTime.start();
    }
}
