package com.rhjf.salesman.service.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.core.Ordered;

import java.lang.reflect.Method;


/**
* @author zzg
*
* @version 1.0
*
* 创建时间：2017年6月27日 下午6:44:41
*
* @ClassName 类名称
*
* @Description 类描述
*/
public class DataSourceAdvice implements MethodBeforeAdvice, AfterReturningAdvice, ThrowsAdvice, Ordered {


    private Logger logger = LoggerFactory.getLogger(DataSourceAdvice.class);


    @Override
    public void afterReturning(Object arg0, Method method, Object[] arg2, Object arg3) throws Throwable {


    }

    @Override
    public void before(Method method, Object[] arg1, Object target) throws Throwable {

//        if (method.getName().toLowerCase().startsWith(DataSourceContextHolder.ANALYZE)) {
//            DataSourceContextHolder.setDsType(DataSourceContextHolder.ANALYZE);
//        }
//        else {
            DataSourceContextHolder.setDsType(DataSourceContextHolder.EMOVE);
//        }
    }

    public void afterThrowing(Method method, Object[] args, Object target, Exception ex) throws Throwable {
        ex.printStackTrace();
        logger.error("-----error", ex.getMessage());
    }

    public int getOrder() {

        return 1;
    }
}
