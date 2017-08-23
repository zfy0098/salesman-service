package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.SalesManProfitService;
import com.rhjf.salesman.core.util.DateUtil;
import com.rhjf.salesman.service.mapper.SalesManProfitMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by hadoop on 2017/8/4.
 */
@Transactional
@Service("salesManProfitService")
public class SalesManProfitServiceImpl implements SalesManProfitService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SalesManProfitMapper salesManProfitMapper;

    /**
     * 业务员查询收益总和
     *
     * @param user
     * @param paramter
     * @return
     */
    public ParamterData profitTotal(LoginUser user, ParamterData paramter) {

        Map<String, String> salesManProfit = salesManProfitMapper.profitTotal(user.getID());

        if (salesManProfit == null || salesManProfit.isEmpty()) {
            salesManProfit = new HashMap<>();
            salesManProfit.put("distributeProfit", "0");
            salesManProfit.put("profit", "0");
        }

        paramter.setList(JSONObject.fromObject(salesManProfit).toString());

        return paramter;
    }


    /**
     * 查询收益明细显示某天
     *
     * @param user
     * @param paramter
-     * @return
     */
    public ParamterData profitDetailByToDay(LoginUser user, ParamterData paramter) {

        String toDay = paramter.getTradeDate();

        Map<String, String> map = new HashMap<>();
        map.put("toDay", toDay);
        map.put("salesManID", user.getSalesManID());

        List<Map<String, String>> list = salesManProfitMapper.profitDetailByToDay(map);

        paramter.setList(JSONArray.fromObject(list).toString());
        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;
    }


    /**
     * 收益明细 折线数据
     *
     * @return
     */
    public ParamterData profitDetailByToDayCurve(LoginUser user, ParamterData paramter) {

        String tradeDate = paramter.getTradeDate();

        StringBuffer sbf = new StringBuffer(tradeDate);

        sbf.insert(4,"-");
        sbf.insert(7,"-");
        log.info("插入 - 的日期:" + sbf.toString());


        try{
            String startTime = DateUtil.getDateAgo(sbf.toString() , 3);

            log.info("查询的开始日期：" + startTime);
            log.info("查询的结束日期：" + sbf.toString());

            Map<String, String> map = new HashMap<>();
            map.put("salesManID", user.getSalesManID());
            map.put("startTime", startTime);
            map.put("endTime", sbf.toString());
            List<Map<String, String>> list = salesManProfitMapper.profitDetailByDayCurve(map);


            map.clear();
            map.put("salesManID" ,user.getID());
            map.put("date" , sbf.toString());

            Map<String, String> salesManProfit = salesManProfitMapper.profitTotalByDay(map);

            if(tradeDate.equals(DateUtil.getNowTime(DateUtil.yyyyMMdd))){
                // TotalProfit as amount , CONCAT(EarningsDate,'') as LocalDate
                map.clear();
                map.put("amount" ,String.valueOf(Integer.parseInt(salesManProfit.get("distributeProfit")) + Integer.parseInt(salesManProfit.get("profit"))));
                map.put("LocalDate" , DateUtil.getNowTime(DateUtil.yyyy_MM_dd));
                list.add(map);
            }

            paramter.setDistributeProfit(salesManProfit.get("distributeProfit"));
            paramter.setProfit(salesManProfit.get("profit"));
            paramter.setProfitTotal(String.valueOf(Integer.parseInt(salesManProfit.get("distributeProfit")) + Integer.parseInt(salesManProfit.get("profit"))));

            paramter.setList(JSONArray.fromObject(list).toString());
            paramter.setRespCode(RespCode.SUCCESS[0]);
            paramter.setRespDesc(RespCode.SUCCESS[1]);
        }catch (Exception e){

            e.printStackTrace();
        }
        return paramter;
    }


    /**
     * 显示分润可提现余额
     *
     * @param user
     * @param paramterData
     * @return
     */
    public ParamterData profitIncomeBalance(LoginUser user, ParamterData paramterData) {

        paramterData.setBalance(user.getFeeBalance());
        paramterData.setRespDesc(RespCode.SUCCESS[0]);
        paramterData.setRespDesc(RespCode.SUCCESS[1]);

        return paramterData;
    }


    /**
     * 月报收益
     *
     * @param user
     * @return
     */
    public ParamterData monthlyReport(LoginUser user, ParamterData paramterData) {

        log.info("用户：" + user.getLoginID() + "查询收益月报数据");

        List<Map<String, String>> monthlyReportList = salesManProfitMapper.monthlyReport(user.getID());

        Map<String, List<Map<String, String>>> map = new HashMap<>();

        for (int i = 0; i < monthlyReportList.size(); i++) {
            Map<String, String> map2 = monthlyReportList.get(i);

            String year = map2.get("year").toString();

            List<Map<String, String>> list = map.get(year);

            if (list == null) {
                list = new ArrayList<>();
            }
            map2.remove("year");
            String month = map2.get("month").toString();

            if (month.charAt(0) == '0') {
                month = month.substring(1);
            }
            map2.put("month", month);
            map2.put("amount", String.valueOf(map2.get("amount")));
            map2.put("count", String.valueOf(map2.get("count")));
            map2.put("isopen" , "0");  // 0未读 1已读
            list.add(map2);
            map.put(year, list);
        }

        JSONArray array = new JSONArray();

//        for (String year : map.keySet()) {
//            List<Map<String, String>> data = map.get(year);
//            JSONObject json = new JSONObject();
//            json.put("year", year);
//            json.put("content", JSONArray.fromObject(data));
//            array.add(json);
//        }

        paramterData.setList(array.toString());
        paramterData.setRespCode(RespCode.SUCCESS[0]);
        paramterData.setRespDesc(RespCode.SUCCESS[1]);
        return paramterData;
    }


    /** 月报详细数据 **/
    public ParamterData monthlyReportDetailed(LoginUser user , ParamterData paramter){

        String tradeDate = paramter.getTradeDate();

        StringBuffer stringBuffer = new StringBuffer(tradeDate);

        if(tradeDate.length() < 6){
            stringBuffer.insert(4, "0");
        }

        Map<String,String> map = new HashMap<>();
        map.put("salesManID" , user.getID());
        map.put("toDay" , stringBuffer.toString());

        List<Map<String,String>> list = salesManProfitMapper.monthlyReportDetailed(map);


        paramter.setList(JSONArray.fromObject(list).toString());
        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;
    }


    /**
     *    月报折线数据
     * @param user
     * @param paramterData
     * @return
     */
    public ParamterData monthlyReportCurve(LoginUser user,ParamterData paramterData){


        JSONArray array  = new JSONArray();
        JSONObject json = new JSONObject();

        json.put("amount"  , "1");
        json.put("LocalDate" , "6");

        array.add(json);

        json.put("amount"  , "2");
        json.put("LocalDate" , "7");

        array.add(json);


        json.put("amount"  , "12");
        json.put("LocalDate" , "5");

        array.add(json);


        paramterData.setList(array.toString());
        paramterData.setProfitTotal("15");
        paramterData.setDistributeProfit("7");
        paramterData.setProfit("8");

        paramterData.setRespCode(RespCode.SUCCESS[0]);
        paramterData.setRespDesc(RespCode.SUCCESS[1]);

        return  paramterData;
    }
}
