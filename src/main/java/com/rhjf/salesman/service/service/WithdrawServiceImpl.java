package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.Salesman;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.WithdrawService;
import com.rhjf.salesman.service.mapper.LoginMapper;
import com.rhjf.salesman.service.mapper.SalesManProfitMapper;
import com.rhjf.salesman.service.mapper.SalesmanMapper;
import com.rhjf.salesman.service.mapper.WithdrawMapper;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/8/29.
 */
public class WithdrawServiceImpl implements WithdrawService {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SalesmanMapper salesmanMapper;

    @Autowired
    private WithdrawMapper withdrawMapper;

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private SalesManProfitMapper salesManProfitMapper;

    /**  用户提现 **/
    public ParamterData txProfit(LoginUser user , ParamterData paramter){

        String amount = paramter.getAmount();
        Salesman salesman = salesmanMapper.salesmanInfo(user.getSalesManID());
        log.info("业务员：" + user.getLoginID() + "发起提现请求,提现金额:" + paramter.getAmount());


        Map<String,Object> map = new HashMap<>();
        map.put("in_loginID",user.getID());
        map.put("in_amount", amount);
        map.put("in_termSerno" , paramter.getSendTime() );
        map.put("in_txType","0");
        map.put("in_accountNo" , salesman.getAccountNo());
        map.put("ret" , 0);

        withdrawMapper.txProfit(map);

        Integer ret = Integer.parseInt(map.get("ret").toString());

        if(ret==2){
            log.info("业务员："+ user.getLoginID()+ "提款金额不足，提款金额：" + paramter.getAmount());
            paramter.setRespCode(RespCode.TXAMOUNTNOTENOUGH[0]);
            paramter.setRespDesc(RespCode.TXAMOUNTNOTENOUGH[1]);
        } else if( ret == 1){


            Map<String,String> usermap = new HashMap<>();
            usermap.put("loginid" ,user.getLoginID());

            user = loginMapper.login(usermap);
            paramter.setBalance(user.getFeeBalance());

            log.info("业务员："+ user.getLoginID()+ "提款成功");
            paramter.setRespCode(RespCode.SUCCESS[0]);
            paramter.setRespDesc(RespCode.SUCCESS[1]);
        }else{
            log.info("业务员："+ user.getLoginID()+ "提款失败 , 返回状态值：" + ret );

            paramter.setRespCode(RespCode.ServerDBError[0]);
            paramter.setRespDesc(RespCode.ServerDBError[1]);
        }
        return paramter;
    }



    /**  提现记录 **/
    public ParamterData TxRecordList(LoginUser user , ParamterData paramter){

        log.info("业务员" + user.getLoginID() + "获取提现记录, 查询时间:" + paramter.getTradeDate());


        /**   交易年月   **/
        StringBuffer stringBuffer = new StringBuffer(paramter.getTradeDate());

        StringBuffer sbf = new StringBuffer(paramter.getTradeDate());

        if(paramter.getTradeDate().length() < 6){
            stringBuffer.insert(4, "-0");
            sbf.insert(4, "0");
        }else{
            stringBuffer.insert(4, "-");
        }


        log.info("查询时间：分润总和时间" + stringBuffer.toString());
        log.info("查询时间：提现记录时间:" + sbf.toString());


        Map<String,String> params = new HashMap<>();
        params.put("salesManID" , user.getID());
        params.put("tradeDate" , sbf.toString());


        Map<String,String> pro = salesManProfitMapper.profitMonth(params);
        paramter.setProfitTotal(String.valueOf(pro.get("totalProfit")));
        log.info("业务员：" + user.getLoginID() + "时间：" + stringBuffer.toString() + "分润总和:" + String.valueOf(pro.get("totalProfit")));

        params.clear();
        params.put("userID",user.getID());
        params.put("tradeDate",stringBuffer.toString());
        List<Map<String,String>> list = withdrawMapper.TxRecordList(params);
        log.info("业务员：" + user.getLoginID() + "时间：" + stringBuffer.toString() + "提现记录:" + JSONArray.fromObject(list).toString());

        paramter.setList(JSONArray.fromObject(list).toString());
        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;
    }
}
