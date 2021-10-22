package me.victor.mybatis.fieldprocessor.annotation;


import me.victor.mybatis.fieldprocessor.processor.FieldValueProcessor;
import me.victor.mybatis.fieldprocessor.util.ProcessTarget;

import org.apache.ibatis.mapping.SqlCommandType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 处理字段的注解
 *
 * @author Victor
 * @date 17/08/25 17:04
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DoProcess {

    /**
     * SQL命令类型
     */
    SqlCommandType[] sqlType();

    /**
     * 处理目标
     */
    ProcessTarget target();

    /**
     * 字段处理器
     */
    @SuppressWarnings("rawtypes")
    Class<? extends FieldValueProcessor> processor();

    /**
     * 优先级，数字越小优先级越高
     */
    int order() default 0;
}
