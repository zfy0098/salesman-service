package com.rhjf.salesman.service.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/9/1.
 */
public interface SalesmanMonthProfitMapper {



    /**
     * 查询月报数据
     **/
    public List<Map<String, String>> monthlyReport(String salesManID);


    /**
     *    查询某个月的总收益
     */
    public Map<String,String> monthlyTotalProfit(Map<String,String> map);


    /**
     *   更新月报数据 open 状态值
     */
    public int updateMonthlyOpenStatus(Map<String,String> map);
}
