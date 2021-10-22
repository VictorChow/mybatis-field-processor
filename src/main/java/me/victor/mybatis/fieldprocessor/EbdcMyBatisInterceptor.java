package me.victor.mybatis.fieldprocessor;

import cn.hutool.core.util.ReflectUtil;
import me.victor.mybatis.fieldprocessor.annotation.DoProcess;
import lombok.val;
import me.victor.mybatis.fieldprocessor.util.EbdcMyBatisUtil;
import me.victor.mybatis.fieldprocessor.util.ProcessTarget;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.Properties;

/**
 * MyBatis拦截器，处理{@link DoProcess}标记的字段
 *
 * @author Victor
 * @date 17/08/25 17:04
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = Statement.class)
})
public class EbdcMyBatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        val target = invocation.getTarget();
        if (target instanceof Executor) {
            val sqlType = ((MappedStatement) invocation.getArgs()[0]).getSqlCommandType();
            val param = invocation.getArgs()[1];
            val recover = EbdcMyBatisUtil.process(param, sqlType, ProcessTarget.PARAMETER);
            val result = invocation.proceed();
            recover.ifPresent(Runnable::run);
            return result;
        } else if (target instanceof ResultSetHandler) {
            val result = invocation.proceed();
            val sqlType = ((MappedStatement) ReflectUtil.getFieldValue(target, "mappedStatement")).getSqlCommandType();
            EbdcMyBatisUtil.process(result, sqlType, ProcessTarget.RESULT);
            return result;
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor || target instanceof ResultSetHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        //空方法
    }
}
