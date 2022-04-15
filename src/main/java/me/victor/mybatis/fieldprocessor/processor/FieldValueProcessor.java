package me.victor.mybatis.fieldprocessor.processor;



import java.util.function.BiFunction;

import me.victor.mybatis.fieldprocessor.util.MyBatisUtil;
import me.victor.mybatis.fieldprocessor.util.ProcessorParam;

/**
 * 处理器接口，实例会被缓存在{@link MyBatisUtil#processorCache}
 * 注意：避免在实现类中出现内存泄漏的代码
 *
 * @author Victor
 * @date 17/08/25 17:04∂
 */
public interface FieldValueProcessor<T, R> extends BiFunction<T, ProcessorParam, R> {
}
