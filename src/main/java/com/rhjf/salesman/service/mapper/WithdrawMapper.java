package com.rhjf.salesman.service.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/8/29.
 */
public interface WithdrawMapper {


    /**
     * 用户提现
     **/
    public Integer txProfit(Map<String, Object> map);


    /**
     * 提现列表
     **/
    public List<Map<String, String>> TxRecordList(Map<String, String> map);
}
