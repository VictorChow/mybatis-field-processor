package me.victor.mybatis.fieldprocessor.util;

import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.SimpleCache;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.victor.mybatis.fieldprocessor.annotation.DoProcess;
import me.victor.mybatis.fieldprocessor.annotation.EnableProcess;
import me.victor.mybatis.fieldprocessor.processor.FieldValueProcessor;

/**
 *
 * @author Victor
 * @date 2021/9/3 14:26
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"rawtypes", "unchecked", "AlibabaConstantFieldShouldBeUpperCase", "JavadocReference", "AlibabaUndefineMagicConstant"})
public class EbdcMyBatisUtil {
    /**
     * Class、字段和注解的映射缓存
     */
    private static final SimpleCache<String, Map<Field, List<Pair<Annotation, DoProcess>>>> fieldAnnotationCache = new SimpleCache<>();
    /**
     * Class和注解处理器实例的缓存
     */
    private static final SimpleCache<Class<? extends FieldValueProcessor>, FieldValueProcessor> processorCache = new SimpleCache<>();

    /**
     * 预处理字段，调用处理逻辑，并提供处理PARAMETER后的回滚操作
     * @author Victor
     * @date 2021/9/9 15:49
     * @param obj 待处理对象
     * @return 回滚操作
     */
    public static Optional<Runnable> process(Object obj) {
        return process(obj, SqlCommandType.SELECT, ProcessTarget.RESULT);
    }

    /**
     * 预处理字段，调用处理逻辑，并提供处理PARAMETER后的回滚操作
     * @author Victor
     * @date 2021/9/9 15:49
     * @param obj 待处理对象
     * @param sqlType SQL命令类型
     * @param target 处理目标
     * @return 回滚操作
     */
    public static Optional<Runnable> process(Object obj, SqlCommandType sqlType, ProcessTarget target) {
        val processItems = getObjectCollection(obj).stream()
                .filter(it -> !EbdcMyBatisUtil.ignoreProcess(it))
                .collect(Collectors.toList());
        if (processItems.isEmpty()) {
            return Optional.empty();
        }
        if (target == ProcessTarget.RESULT) {
            processItems.forEach(it -> processField(it, sqlType, target));
            return Optional.empty();
        }
        val restorePairs = processItems.stream()
                .map(it -> processField(it, sqlType, target))
                .collect(Collectors.toList());
        return Optional.of(() -> restorePairs.forEach(it -> EbdcMyBatisUtil.restoreParamFieldValues(it.getKey(), it.getValue())));
    }

    /**
     * 对象是否忽略处理
     * @author Victor
     * @date 2021/9/9 14:58
     * @param obj 对象
     * @return boolean
     */
    private static boolean ignoreProcess(Object obj) {
        return obj == null || obj.getClass().getAnnotation(EnableProcess.class) == null;
    }

    /**
     * 获取类中字段和被{@link DoProcess}元注解标记的注解的映射关系，并按优先级排序
     *
     * 样例对象：
     * <pre>
     *     @ Data
     *     public class Test {
     *         @ Encrypt
     *         @ Decrypt
     *         @ PhoneMasking
     *         private String phone;
     *     }
     * </pre>
     * 在查询时返回和{@link SqlCommandType#SELECT}相关的字段和注解的映射，其他命令下同理，结果样例：
     * <pre>
     *     key: Field: phone
     *     value: [{Decrypt, DoProcess}, {PhoneMasking, DoProcess}]
     * </pre>
     * @author Victor
     * @date 2021/9/9 15:00
     * @param clazz 对象的class
     * @param target 处理目标
     * @param sqlType SQL命令类型
     * @return 映射关系
     */
    private static Map<Field, List<Pair<Annotation, DoProcess>>> findAnnotatedField(Class<?> clazz, ProcessTarget target, SqlCommandType sqlType) {
        val cacheKey = StrUtil.format("{}#{}#{}", clazz, target, sqlType);
        return fieldAnnotationCache.get(cacheKey, () -> {
            val map = Arrays.stream(ReflectUtil.getFields(clazz))
                    .filter(it -> AnnotatedElementUtils.hasAnnotation(it, DoProcess.class))
                    .collect(Collectors.toMap(UnaryOperator.identity(),
                            it -> AnnotatedElementUtils.findAllMergedAnnotations(it, DoProcess.class).stream()
                                    .filter(item -> item.target() == target)
                                    .filter(item -> ArrayUtil.contains(item.sqlType(), sqlType))
                                    .sorted(Comparator.comparingInt(DoProcess::order))
                                    .map(item -> Pair.of(findRootAnnotation(it, item), item))
                                    .collect(Collectors.toList())));
            map.entrySet().removeIf(it -> CollUtil.isEmpty(it.getValue()));
            return map;
        });
    }

    /**
     * 根据{@link AnnotatedElementUtils#findAllMergedAnnotations(AnnotatedElement, Class)}的{@link DoProcess}对象获取直接标记在字段上的根注解，获取步骤如下：
     * 1.findAllMergedAnnotations获取到的为动态代理生成的对象，获取{@link Proxy#h}属性，其类型为{@link org.springframework.core.annotation.SynthesizedMergedAnnotationInvocationHandler}
     * 2.获取{@link org.springframework.core.annotation.SynthesizedMergedAnnotationInvocationHandler#annotation}属性，其实现类型为{@link org.springframework.core.annotation.TypeMappedAnnotation}
     * 3.{@link org.springframework.core.annotation.TypeMappedAnnotation}包含{@link org.springframework.core.annotation.TypeMappedAnnotation#mapping}属性和（{@link org.springframework.core.annotation.TypeMappedAnnotation#rootAttributes}属性
     * 4.获取{@link org.springframework.core.annotation.AnnotationTypeMapping#distance}属性，含义为当前注解距离根注解的距离（如果当前注解为根注解则为0）
     * 5.当distance≤1时，返回{@link org.springframework.core.annotation.TypeMappedAnnotation#rootAttributes}属性
     * 6.当distance＞1时，获取{@link org.springframework.core.annotation.AnnotationTypeMapping#source}对象中的{@link org.springframework.core.annotation.AnnotationTypeMapping#annotation}属性
     * 7.此时获取到的注解为动态代理生成，因为findAllMergedAnnotations的Class为元注解{@link DoProcess}，所以{@link AliasFor}无法覆盖动态代理对象里的属性
     * 8.暂时解决方案为获取动态代理对象的实际Class，用findAllMergedAnnotations再次获取该Class，{@link AliasFor}才能覆盖属性
     * @author Victor
     * @date 2021/9/9 15:04
     * @param proxy 动态代理生成的{@link DoProcess}对象
     * @return 直接标记在字段上的注解
     */
    private static Annotation findRootAnnotation(Field field, Object proxy) {
        if (proxy == null || !Proxy.isProxyClass(proxy.getClass())) {
            return null;
        }
        try {
            val handler = Proxy.getInvocationHandler(proxy);
            val typeMappedAnnotation = ReflectUtil.getFieldValue(handler, "annotation");
            val annotationTypeMapping = ReflectUtil.getFieldValue(typeMappedAnnotation, "mapping");
            val distance = (int) ReflectUtil.getFieldValue(annotationTypeMapping, "distance");
            if (distance <= 1) {
                return (Annotation) ReflectUtil.getFieldValue(typeMappedAnnotation, "rootAttributes");
            }
            val annotation = (Annotation) ReflectUtil.getFieldValue(ReflectUtil.getFieldValue(annotationTypeMapping, "source"), "annotation");
            val type = (Class<? extends Annotation>) ReflectUtil.getFieldValue(Proxy.getInvocationHandler(annotation), "type");
            //判断注解的类型，如果是DoPress，直接返回，如果不是，重新获取此类型的注解使@AliasFor生效
            if (type == DoProcess.class) {
                return annotation;
            }
            return AnnotatedElementUtils.findMergedAnnotation(field, type);
        } catch (Exception e) {
            log.error("获取原注解失败", e);
            return null;
        }
    }

    /**
     * 根据处理器处理字段的值
     * @author Victor
     * @date 2021/9/9 15:07
     * @param field 字段field
     * @param annotations 标记在field上的注解
     * @param originalValue 原始字段的值
     * @return 处理后的值
     */
    private static Object processOriginalValue(Object obj, Field field, List<Pair<Annotation, DoProcess>> annotations, Object originalValue) {
        return annotations.stream()
                .reduce(originalValue,
                        (tmp, pair) -> processorCache.get(pair.getValue().processor(),
                                () -> pair.getValue().processor().newInstance()).process(tmp, ProcessorParam.of(obj, field, pair.getKey())),
                        (o1, o2) -> o1);
    }

    /**
     * 还原被修改的入参的属性值
     * @author Victor
     * @date 21/08/19 13:33
     * @param param 入参值
     * @param map Field和原值的映射
     */
    private static void restoreParamFieldValues(Object param, Map<Field, Object> map) {
        if (EbdcMyBatisUtil.ignoreProcess(param) || CollUtil.isEmpty(map)) {
            return;
        }
        map.forEach((k, v) -> ReflectUtil.setFieldValue(param, k, v));
    }

    /**
     * 对参数做包装处理，统一为Collection
     * @author Victor
     * @date 2021/10/19 14:48
     * @param obj 待处理对象
     * @return 对象包装的Collection
     */
    private static Collection<?> getObjectCollection(Object obj) {
        Collection<?> collection;
        if (obj instanceof DefaultSqlSession.StrictMap) {
            val map = (DefaultSqlSession.StrictMap<?>) obj;
            if (map.containsKey("collection")) {
                collection = (Collection<?>) map.get("collection");
            } else if (map.containsKey("array")) {
                val array = map.get("array");
                collection = IntStream.range(0, Array.getLength(array))
                        .mapToObj(it -> Array.get(array, it))
                        .collect(Collectors.toList());
            } else {
                collection = Collections.emptyList();
            }
        } else if (obj instanceof Collection) {
            collection = (Collection<?>) obj;
        } else {
            collection = Collections.singletonList(obj);
        }
        return collection;
    }

    /**
     * 处理字段逻辑
     * @author Victor
     * @date 2021/9/9 15:51
     * @param obj 待处理对象
     * @param sqlType SQL命令类型
     * @param target 处理目标
     * @return 对象中字段Field和初始值的映射
     */
    private static Pair<Object, Map<Field, Object>> processField(Object obj, SqlCommandType sqlType, ProcessTarget target) {
        val fieldProcessMap = EbdcMyBatisUtil.findAnnotatedField(obj.getClass(), target, sqlType);
        val fieldOriginalValueMap = new HashMap<Field, Object>((int) (fieldProcessMap.size() / 0.75 + 1));
        fieldProcessMap.forEach((field, annotations) -> {
            Object originalValue = ReflectUtil.getFieldValue(obj, field);
            fieldOriginalValueMap.put(field, originalValue);
            Object newValue = EbdcMyBatisUtil.processOriginalValue(obj, field, annotations, originalValue);
            ReflectUtil.setFieldValue(obj, field, newValue);
        });
        return Pair.of(obj, fieldOriginalValueMap);
    }
}
