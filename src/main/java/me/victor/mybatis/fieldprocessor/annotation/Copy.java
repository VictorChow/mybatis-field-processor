package me.victor.mybatis.fieldprocessor.annotation;

import me.victor.mybatis.fieldprocessor.processor.CopyProcessor;
import me.victor.mybatis.fieldprocessor.util.ProcessTarget;

import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.core.Ordered;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 复制其他属性的值
 * @author Victor
 * @date 2021/9/28 14:05
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DoProcess(sqlType = SqlCommandType.SELECT, target = ProcessTarget.RESULT, processor = CopyProcessor.class, order = Ordered.HIGHEST_PRECEDENCE)
public @interface Copy {

    /**
     * 其他属性名
     */
    String value();
}
