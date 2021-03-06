package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.SalesManProfitService;
import com.rhjf.salesman.core.util.DateUtil;
import com.rhjf.salesman.core.util.UtilsConstant;
import com.rhjf.salesman.service.mapper.SalesManProfitMapper;
import com.rhjf.salesman.service.mapper.SalesmanMonthProfitMapper;
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

    @Autowired
    private SalesmanMonthProfitMapper salesmanMonthProfitMapper;



    /**
     * 查询收益明细显示某天
     *
     * @param user
     * @param paramter
-     * @return
     */
    @Override
    public ParamterData profitDetailByDay(LoginUser user, ParamterData paramter) {

        String toDay = paramter.getTradeDate();

        Map<String, String> map = new HashMap<>();
        map.put("toDay", toDay);
        map.put("salesManID", user.getSalesManID());

        List<Map<String, String>> list = salesManProfitMapper.profitDetailByDay(map);

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
    @Override
    public ParamterData profitDetailByToDayCurve(LoginUser user, ParamterData paramter){


        log.info("用户：" + user.getLoginID() + "查询收益折线数据");


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
            map.put("tradeDate" , sbf.toString());

            Map<String, String> salesManProfit = salesManProfitMapper.profitTotalByDay(map);

            log.info("查询日期：" + sbf.toString() + " 的收益情况，" + salesManProfit.toString());

            if(salesManProfit == null || salesManProfit.isEmpty()){
                salesManProfit = new HashMap<>();
                salesManProfit.put("amount" , "0");
                salesManProfit.put("distributeProfit" , "0") ;
                salesManProfit.put("profit" , "0");
                salesManProfit.put("LocalDate" , tradeDate);
            }


            log.info("当天的收益数据：" + JSONObject.fromObject(salesManProfit).toString());

            if(tradeDate.equals(DateUtil.getNowTime(DateUtil.yyyyMMdd))){
                map.clear();
                map.put("amount" ,salesManProfit.get("amount"));
                map.put("LocalDate" , DateUtil.getNowTime(DateUtil.yyyyMMdd));
                list.add(map);
            }

            log.info("折线数据:" + JSONArray.fromObject(list).toString());

            paramter.setDistributeProfit(UtilsConstant.ObjToStr(salesManProfit.get("distributeProfit")));

            String profit = "0.0";
            if(salesManProfit.get("profit") != null){
                profit =  String.valueOf(salesManProfit.get("profit"));
            }

            paramter.setProfit(profit);

            String profitTotal = "0.0";
            if(salesManProfit.get("amount")!= null){
                profitTotal = String.valueOf(salesManProfit.get("amount"));
            }

            paramter.setProfitTotal(profitTotal);

            paramter.setList(JSONArray.fromObject(list).toString());
            paramter.setRespCode(RespCode.SUCCESS[0]);
            paramter.setRespDesc(RespCode.SUCCESS[1]);
        }catch (Exception e){

            log.info("查询折线数据出现异常：" + e.getMessage());
            log.error("查询折线数据出现异常" , e);
            paramter.setRespCode(RespCode.NETWORKError[0]);
            paramter.setRespDesc(RespCode.NETWORKError[1]);
        }
        return paramter;
    }


    /**
     * 月报收益
     *
     * @param user
     * @return
     */
    @Override
    public ParamterData monthlyReport(LoginUser user, ParamterData paramterData) {

        log.info("用户：" + user.getLoginID() + "查询收益月报数据");

        List<Map<String, String>> monthlyReportList = salesmanMonthProfitMapper.monthlyReport(user.getID());

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
            map2.put("count", "0");
            map2.put("isopen" , map2.get("Open").toString());  // 0未读 1已读
            map2.remove("Open");
            list.add(map2);
            map.put(year, list);
        }

        JSONArray array = new JSONArray();
        for (String year : map.keySet()) {
            List<Map<String,String>> data = map.get(year);
            JSONObject json = new JSONObject();
            json.put("year", year);
            json.put("content", JSONArray.fromObject(data));
            array.add(json);
        }

        paramterData.setList(array.toString());
        paramterData.setRespCode(RespCode.SUCCESS[0]);
        paramterData.setRespDesc(RespCode.SUCCESS[1]);
        return paramterData;
    }


    /** 月报详细数据 **/
    @Override
    public ParamterData monthlyReportDetailed(LoginUser user , ParamterData paramter){

        String tradeDate = paramter.getTradeDate();

        StringBuffer stringBuffer = new StringBuffer(tradeDate);

        if(tradeDate.length() < 6){
            stringBuffer.insert(4, "0");
        }

        Map<String,String> map = new HashMap<>();
        map.put("salesManID" , user.getID());
        map.put("date" , stringBuffer.toString());

        log.info("用户ID：" + user.getID());
        log.info("查询的时间:" + stringBuffer.toString());

        List<Map<String,String>> list = salesManProfitMapper.monthlyReportDetailed(map);

        salesmanMonthProfitMapper.updateMonthlyOpenStatus(map);

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
    @Override
    public ParamterData monthlyReportCurve(LoginUser user,ParamterData paramterData){


        JSONArray array  = new JSONArray();
        paramterData.setList(array.toString());

        String tradeDate = paramterData.getTradeDate();
        StringBuffer stringBuffer = new StringBuffer(tradeDate);
        if(tradeDate.length() < 6){
            stringBuffer.insert(4, "0");
        }

        Map<String,String> map = new HashMap<>(16);
        map.put("SalesManID" , user.getID());
        map.put("ProfitMonth" , stringBuffer.toString());

        Map<String,String> totalProfitMap =  salesmanMonthProfitMapper.monthlyTotalProfit(map);

        paramterData.setProfitTotal(totalProfitMap.get("TotalProfit"));
        paramterData.setDistributeProfit(totalProfitMap.get("DistributeProfit"));
        paramterData.setProfit(totalProfitMap.get("Profit"));

        paramterData.setRespCode(RespCode.SUCCESS[0]);
        paramterData.setRespDesc(RespCode.SUCCESS[1]);
        return  paramterData;
    }
}
