package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.UserConfig;
import com.rhjf.account.modle.domain.salesman.YMFQRCode;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.QRCodeService;
import com.rhjf.salesman.core.util.DateUtil;
import com.rhjf.salesman.service.mapper.LoginMapper;
import com.rhjf.salesman.service.mapper.UserConfigMapper;
import com.rhjf.salesman.service.mapper.YMFQRCodeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/8/21.
 */
public class QRCodeServiceImpl implements QRCodeService{


    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private YMFQRCodeMapper ymfqrCodeMapper;

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private UserConfigMapper userConfigMapper;


    public ParamterData BindedYMF(LoginUser user , ParamterData paramter){


        String merchantLoginID = paramter.getMerchantLoginID();
        String tradeCode = paramter.getTradeCode();
        String QRCodeURL = paramter.getQRCodeURL();

        String code = QRCodeURL.substring(QRCodeURL.indexOf("=") +1 );

        YMFQRCode QRCode = ymfqrCodeMapper.getQRCodeBindInfo(code);

        if(QRCode != null){

            if("1".equals(QRCode.getBinded())){
                paramter.setRespCode(RespCode.BindedErrir[0]);
                paramter.setRespDesc("该固定码已经被绑定");
                return paramter;
            }

            Map<String,String> map = new HashMap<>();
            map.put("loginid" , paramter.getMerchantLoginID());
            LoginUser user2 = loginMapper.login(map);


            map.clear();
            // UserID=#{userID} and TradeCode=#{tradeCode}
            map.put("userID" , user2.getID());
            map.put("tradeCode" ,tradeCode);


            List<YMFQRCode> list = ymfqrCodeMapper.userYMFlist(map);

            if(list!=null&&list.size() > 0){

                System.out.println(list.size());
                System.out.println(list.toString());

                log.info("该用户已经绑定过到账类型为：" +tradeCode + "的固定码，" + paramter.getMerchantLoginID());
                paramter.setRespCode(RespCode.BindedErrir[0]);
                paramter.setRespDesc("该用户已经绑定过到账类型为：" +tradeCode + "的固定码");
                return paramter;
            }

            String agentID = user2.getAgentID();
            String QRCodeAgentID = QRCode.getAgentID();

            if(agentID.equals(QRCodeAgentID)){

                UserConfig userConfig = new UserConfig();
                userConfig.setUserID(user2.getID());
                userConfig.setPayChannel(1);

                userConfig = userConfigMapper.getUserConfig(userConfig);

                QRCode.setUserID(user2.getID());
                QRCode.setBindedDate(DateUtil.getNowTime(DateUtil.yyyyMMdd));
                QRCode.setTradeCode(tradeCode);
                QRCode.setCode(code);
                QRCode.setBinded("1");

                if("T1".equals(tradeCode)){
                    QRCode.setSettlementRate(userConfig.getT1SettlementRate());
                    QRCode.setRate(userConfig.getT1SaleRate());
                }else{
                    QRCode.setSettlementRate(userConfig.getT0SettlementRate());
                    QRCode.setRate(userConfig.getT0SaleRate());
                }


                int ret = ymfqrCodeMapper.updateBindedInfo(QRCode);

                if(ret > 0){
                    log.info("码数据" + code + "更新绑定信息成功");
                    paramter.setRespCode(RespCode.SUCCESS[0]);
                    paramter.setRespDesc(RespCode.SUCCESS[1]);
                }else{
                    log.info("码数据" + code + "更新绑定信息失败");
                    paramter.setRespCode(RespCode.ServerDBError[0]);
                    paramter.setRespDesc(RespCode.ServerDBError[1]);
                }
            } else {
                log.info("申请固定码代理商与用户代理商不是同一个人无法进行绑定" + paramter.getQRCodeURL());
                paramter.setRespCode(RespCode.BindedErrir[0]);
                paramter.setRespDesc(RespCode.BindedErrir[1]);
            }
        }else{
            paramter.setRespCode(RespCode.DATANOTEXISTError[0]);
            paramter.setRespCode(RespCode.DATANOTEXISTError[1]);
        }
        return paramter;
    }
}
