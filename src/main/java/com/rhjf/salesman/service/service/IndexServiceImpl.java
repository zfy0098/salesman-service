package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.Salesman;
import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.IndexService;
import com.rhjf.salesman.core.util.DateUtil;
import com.rhjf.salesman.service.mapper.SalesManProfitMapper;
import com.rhjf.salesman.service.mapper.SalesmanMapper;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by hadoop on 2017/8/7.
 */

@Transactional
@Service("indexService")
public class IndexServiceImpl implements IndexService{


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SalesmanMapper salesmanMapper;

    @Autowired
    private SalesManProfitMapper salesManProfitMapper;


    /**
     *   用户的登录以后 加载基础数据
     * @param user
     * @param paramter
     * @return
     */
    public ParamterData index(LoginUser user , ParamterData paramter){


        /**  获取业务员基本信息 **/
        Salesman salesman = salesmanMapper.salesmanInfo(user.getSalesManID());



        paramter.setList(JSONObject.fromObject(salesman).toString());


        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyy_MM_dd);

        String registerDate = sdf.format(salesman.getRegisterDate());


        /**  获取业务员收益总和 **/
        Map<String,String> profitmap =  salesManProfitMapper.profitTotal(user.getSalesManID());

        paramter.setProfit(String.valueOf(profitmap.get("profit")));
        paramter.setDistributeProfit(String.valueOf(profitmap.get("distributeProfit")));
        paramter.setProfitTotal((Integer.parseInt(String.valueOf(profitmap.get("profit"))) + Integer.parseInt(String.valueOf(profitmap.get("distributeProfit")))) + "");

        paramter.setBalance(user.getFeeBalance());

        paramter.setMyQRCodeURL(Constants.myQRCodeURL + user.getLoginID());

        paramter.setTradeDate(registerDate.replace("-" , ""));


        paramter.setCompanyAptitudeURL("00");
        paramter.setAboutURL(Constants.aboutURL);

        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;
    }
}
