package me.victor.mybatis.fieldprocessor.annotation;

import me.victor.mybatis.fieldprocessor.processor.AutoFillProcessor;
import me.victor.mybatis.fieldprocessor.util.ProcessTarget;

import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自动填充注解
 *
 * @author Victor
 * @date 2021/9/2 10:26
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@DoProcess(sqlType = SqlCommandType.UNKNOWN, target = ProcessTarget.PARAMETER, processor = AutoFillProcessor.class)
public @interface AutoFill {

    /**
     * 覆盖DoProcess的SqlCommandType[]
     */
    @AliasFor(annotation = DoProcess.class)
    SqlCommandType[] sqlType();

    /**
     * 是否每次调用都更新
     */
    boolean alwaysUpdate() default false;
}
