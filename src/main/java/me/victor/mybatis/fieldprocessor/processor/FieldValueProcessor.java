package me.victor.mybatis.fieldprocessor.processor;



import java.lang.annotation.Annotation;

import me.victor.mybatis.fieldprocessor.util.EbdcMyBatisUtil;
import me.victor.mybatis.fieldprocessor.util.ProcessorParam;

/**
 * 处理器接口，实例会被缓存在{@link EbdcMyBatisUtil#processorCache}
 * 注意：避免在实现类中出现内存泄漏的代码
 *
 * @author Victor
 * @date 17/08/25 17:04
 */
public interface FieldValueProcessor<T, A extends Annotation> {

    /**
     * 处理方法
     * @author Victor
     * @date 21/08/17 17:06
     * @param value 原值
     * @param param 处理器需要的参数
     * @return 处理后的值
     */
    T process(T value, ProcessorParam<A> param);
}
