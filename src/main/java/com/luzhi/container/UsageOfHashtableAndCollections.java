package com.luzhi.container;

import cn.hutool.core.util.RandomUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author zhilu
 */
public class UsageOfHashtableAndCollections {

    public static void main(String[] args) {
        execute();
    }

    public static void execute() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 1 << 10, 20L, TimeUnit.SECONDS, new SynchronousQueue<>(),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
        ExecutorService service = Executors.newCachedThreadPool();
        HashMap<String, Integer> hashMap = new HashMap<>(16);
        Map<String, Integer> map = Collections.synchronizedMap(hashMap);
        RandomStringAndValue randomStringAndValue = new RandomStringAndValue();
        RandomStringAndValue value = new RandomStringAndValue(map);
        try {
            for (int i = 0; i < HashMapJdkEight.THREAD_LOOP_NUMBER; i++) {
                executor.execute(randomStringAndValue);
            }
        } finally {
            executor.shutdown();
            System.out.format("执行成功,多线程操作现在容器的大小为: %d\n", randomStringAndValue.getSize());
        }

        try {
            for (int i = 0; i < HashMapJdkEight.THREAD_LOOP_NUMBER ; i++) {
                service.execute(value);
            }
        }finally {
            service.shutdown();
            System.out.format("执行成功,多线程操作现在容器的大小为: %d\n", randomStringAndValue.getSize());
        }
    }

    static class RandomStringAndValue implements Runnable {

        private static final int STRING_RANDOM_LEN = 6;

        public RandomStringAndValue(){
            this.map = new Hashtable<>(16);
        }

        public RandomStringAndValue(Map<String, Integer> hash){
            this.map = hash;
        }
        private final Map<String, Integer> map;

        @Override
        public void run() {
            map.put(RandomUtil.randomString(STRING_RANDOM_LEN), RandomUtil.randomInt());
        }

        public int getSize() {
            return map.size();
        }
    }
}
