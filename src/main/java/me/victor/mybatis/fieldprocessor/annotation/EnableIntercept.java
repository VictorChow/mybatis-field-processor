package me.victor.mybatis.fieldprocessor.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用字段处理的注解，在DAO层方法上标记
 *
 * @author Victor
 * @date 2021/10/29 08:50
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableIntercept {
}
