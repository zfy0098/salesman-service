package com.rhjf.salesman.service.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
* @author zzg
*
* @version 1.0
*
* 创建时间：2017年6月27日 下午6:44:50
*
* @ClassName 类名称
*
* @Description 类描述
*/
public class DynamicallyDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {

        return DataSourceContextHolder.getDsType();
    }

}
