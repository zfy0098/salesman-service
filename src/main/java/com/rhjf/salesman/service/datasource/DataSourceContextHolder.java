package com.rhjf.salesman.service.datasource;


/**
* @author zzg
*
* @version 1.0
*
* 创建时间：2017年6月27日 下午6:44:45
*
* @ClassName 类名称
*
* @Description 类描述
*/
public class DataSourceContextHolder {

    public static final String EMOVE = "emove";
    public static final String ANALYZE = "analyze";
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    public static void setDsType(String dsType) {
        contextHolder.set(dsType);
    }

    public static String getDsType() {
        return contextHolder.get();
    }

    public static void clearDsType() {
        contextHolder.remove();
    }

}
