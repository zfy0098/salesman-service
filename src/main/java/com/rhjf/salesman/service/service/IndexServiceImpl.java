package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.Salesman;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.IndexService;
import com.rhjf.salesman.core.util.DateJsonValueProcessor;
import com.rhjf.salesman.core.util.DateUtil;
import com.rhjf.salesman.service.mapper.AppversionMapper;
import com.rhjf.salesman.service.mapper.SalesManProfitMapper;
import com.rhjf.salesman.service.mapper.SalesmanMapper;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by hadoop on 2017/8/7.
 *
 * @author hadoop
 *
 */

@Transactional
@Service("indexService")
public class IndexServiceImpl implements IndexService{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SalesmanMapper salesmanMapper;

    @Autowired
    private SalesManProfitMapper salesManProfitMapper;

    @Autowired
    private AppversionMapper appversionMapper;


    @Value("${aboutURL}")
    private String aboutURL;

    @Value("${myQRCodeURL}")
    private String myQRCodeURL;

    /**
     *   用户的登录以后 加载基础数据
     * @param user
     * @param paramter
     * @return
     */
    @Override
    public ParamterData index(LoginUser user , ParamterData paramter){


        /**  获取业务员基本信息 **/
        Salesman salesman = salesmanMapper.salesmanInfo(user.getSalesManID());


        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.registerJsonValueProcessor(Date.class, new DateJsonValueProcessor(DateUtil.yyyy_MM_dd));
        JSONObject jo = JSONObject.fromObject(salesman,jsonConfig);


        paramter.setList(jo.toString());

        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.yyyy_MM_dd);

        String registerDate = sdf.format(salesman.getRegisterDate());


        /**  获取业务员收益总和 **/
        Map<String,String> profitmap =  salesManProfitMapper.profitTotal(user.getSalesManID());

        paramter.setProfit(String.valueOf(profitmap.get("profit")));
        paramter.setDistributeProfit(String.valueOf(profitmap.get("distributeProfit")));
        paramter.setProfitTotal(String.valueOf(profitmap.get("totalProfit")));

        paramter.setBalance(user.getFeeBalance());

        paramter.setMyQRCodeURL(myQRCodeURL + user.getLoginID());



        Map<String,Object> versionMap = appversionMapper.getAppserver(paramter.getDeviceType());

        if(versionMap != null){
            paramter.setOpen(versionMap.get("SalesManOpen").toString());
        }

        paramter.setTradeDate(registerDate.replace("-" , ""));

        paramter.setCompanyAptitudeURL("00");
        paramter.setAboutURL(aboutURL);

        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;
    }
}
