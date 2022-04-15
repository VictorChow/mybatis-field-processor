package me.victor.mybatis.fieldprocessor.util;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.SimpleCache;
import cn.hutool.core.lang.func.Func0;
import lombok.experimental.UtilityClass;
import me.victor.mybatis.fieldprocessor.annotation.DoProcess;
import me.victor.mybatis.fieldprocessor.annotation.EnableIntercept;
import me.victor.mybatis.fieldprocessor.processor.FieldValueProcessor;

import org.apache.ibatis.mapping.MappedStatement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * MyBatis拦截器缓存类
 * @author Victor
 * @date 2021/11/1 11:17
 */
@UtilityClass
@SuppressWarnings({"rawtypes", "AlibabaConstantFieldShouldBeUpperCase"})
class MyBatisCache {
    /**
     * 通过判断{@link MappedStatement#id}，判断DAO层方法是否没有添加拦截注解{@link EnableIntercept}
     */
    private static final SimpleCache<String, Boolean> ignoreCache = new SimpleCache<>();
    /**
     * Class、字段和注解的映射缓存
     */
    private static final SimpleCache<String, Map<Field, List<Pair<Annotation, DoProcess>>>> fieldAnnotationCache = new SimpleCache<>();
    /**
     * Class和注解处理器实例的缓存
     */
    private static final SimpleCache<Class<? extends FieldValueProcessor>, FieldValueProcessor> processorCache = new SimpleCache<>();

    public static boolean obtainIgnore(String key, Func0<Boolean> supplier) {
        return ignoreCache.get(key, supplier);
    }

    public static Map<Field, List<Pair<Annotation, DoProcess>>> obtainFieldAnnotation(String key, Func0<Map<Field, List<Pair<Annotation, DoProcess>>>> supplier) {
        return fieldAnnotationCache.get(key, supplier);
    }

    public static FieldValueProcessor obtainProcessor(Class<? extends FieldValueProcessor> key, Func0<FieldValueProcessor> supplier) {
        return processorCache.get(key, supplier);
    }
}
