package com.rhjf.salesman.service.mapper;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * Created by hadoop on 2017/8/4.
 */

@Component
public interface SalesManProfitMapper {

    /**
     * 业务员查询收益总和
     *
     * @param salesManID
     * @return
     */
    public Map<String, String> profitTotal(String salesManID);



    /**
     *   查询当天收益总和
     * @param map
     * @return
     */
    public Map<String, String> profitTotalByDay(Map map);


    /**
     *   查询收益明细显示某天
     * @param map
     * @return
     */
    public List<Map<String, String>> profitDetailByDay(Map map);


    public List<Map<String, String>> profitDetailByDayCurve(Map<String, String> map);

    /**
     * 查询业务员月收益总数
     *
     * @param map
     * @return
     */
    public Map<String, String> profitMonth(Map<String, String> map);




    /**
     * 月报详细数据
     ***/
    public List<Map<String, String>> monthlyReportDetailed(Map<String, String> map);

}
