package com.luzhi.test;

import com.luzhi.annotation.AutoWired;
import com.luzhi.controller.UserController;
import com.luzhi.service.TimeService;
import org.junit.Test;

import java.util.stream.Stream;

public class MyTwoTest {

    @Test
    public void test() {
        UserController userController = new UserController();
        Class<? extends UserController> clazz = userController.getClass();
        // 获取所有属性值
        Stream.of(clazz.getDeclaredFields()).forEach(field -> {
            String name = field.getName();
            System.out.println(name);
            AutoWired autoWired = field.getAnnotation(AutoWired.class);
            if (autoWired != null) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                try {
                    Object instance = type.newInstance();
                    field.set(userController, instance);
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        System.out.println(userController.getUserService());
    }

    @Test
    public void testTwo(){
        TimeService timeService = TimeService.getInstance();
        System.out.println(timeService.getTime());
    }
}
