package me.victor.mybatis.fieldprocessor.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用字段处理的注解，在类上标记
 *
 * @author Victor
 * @date 17/08/25 17:04
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableProcess {
}
