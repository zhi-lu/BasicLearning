package com.luzhi.annotation;

import java.lang.annotation.*;

/**
 * @author zhilu
 * @version jdk1.8
 * <p>
 * 实现自定义的@AutoWired注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface AutoWired {
}
