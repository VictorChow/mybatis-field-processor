package me.victor.mybatis.fieldprocessor.processor;

import cn.hutool.core.date.DateUtil;
import me.victor.mybatis.fieldprocessor.annotation.AutoFill;
import me.victor.mybatis.fieldprocessor.util.ProcessorParam;

import com.google.common.collect.ImmutableMap;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 自动填充注解处理器
 * @author Victor
 * @date 2021/9/2 10:38
 */
public class AutoFillProcessor implements FieldValueProcessor<Object, AutoFill> {

    /**
     * 类型和填充值的映射
     */
    private final Map<Class<?>, Supplier<?>> map = ImmutableMap.of(
            Date.class, Date::new,
            Long.class, System::currentTimeMillis,
            Long.TYPE, System::currentTimeMillis,
            String.class, DateUtil::now,
            LocalDateTime.class, LocalDateTime::now
    );

    @Override
    public Object process(Object value, ProcessorParam<AutoFill> param) {
        if (param.getAnnotation().alwaysUpdate()) {
            return map.getOrDefault(param.getField().getType(), () -> null).get();
        }
        return Optional.ofNullable(value).orElseGet(() -> map.getOrDefault(param.getField().getType(), () -> null).get());
    }
}
