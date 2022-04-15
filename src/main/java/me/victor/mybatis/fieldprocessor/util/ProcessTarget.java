package me.victor.mybatis.fieldprocessor.util;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;

/**
 * 处理时机
 * @author Victor
 * @date 2021/9/2 14:42
 */
public enum ProcessTarget {
    /**
     * 拦截{@link Executor}时，处理被注解标记的对象的字段
     */
    PARAMETER,

    /**
     * 拦截{@link ResultSetHandler}时，处理被注解标记的对象的字段
     */
    RESULT
}
