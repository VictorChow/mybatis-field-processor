package me.victor.mybatis.fieldprocessor.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 处理器需要的参数
 * @author Victor
 * @date 2021/10/18 15:27
 */
@Data
@AllArgsConstructor(staticName = "of")
public class ProcessorParam {

    /**
     * 原对象
     */
    private Object object;

    /**
     * 被标记的属性
     */
    private Field field;

    /**
     * 原注解
     */
    private Annotation annotation;
}
