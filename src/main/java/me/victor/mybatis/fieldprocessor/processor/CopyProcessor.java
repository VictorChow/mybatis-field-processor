package me.victor.mybatis.fieldprocessor.processor;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import me.victor.mybatis.fieldprocessor.annotation.Copy;
import me.victor.mybatis.fieldprocessor.util.ProcessorParam;

/**
 * 复制其他属性的值
 *
 * @author w13817
 * @date 2021/08/30 14:04
 */
public class CopyProcessor implements FieldValueProcessor<Object, Copy> {

    @Override
    public Object process(Object value, ProcessorParam<Copy> param) {
        String filedName = param.getAnnotation().value();
        return ObjectUtil.clone(ReflectUtil.getFieldValue(param.getObject(), filedName));
    }
}
