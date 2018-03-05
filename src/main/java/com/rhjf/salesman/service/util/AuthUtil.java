package com.rhjf.salesman.service.util;

import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.util.DESUtil;
import com.rhjf.salesman.core.util.HttpClient;
import com.rhjf.salesman.core.util.MD5;
import com.rhjf.salesman.core.util.UtilsConstant;
import com.rhjf.salesman.service.mapper.AuthenticationMapper;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by hadoop on 2017/9/12.
 */
@Service
public class AuthUtil {

    private static Logger log = LoggerFactory.getLogger(AuthUtil.class);


    @Value("${AUTHENCATION_URL}")
    private   String authenticationURL;


    private static String a;


    @Value("${REPORT_CHANNELNO}")
    private    String channelNo;
    @Value("${REPORT_CHANNELNAME}")
    private    String channelName;
    @Value("${REPORT_DES3_KEY}")
    private    String  des3Key;
    @Value("${REPORT_SIGN_KEY}")
    private    String signKey;



    public  boolean authen(AuthenticationMapper authenticationMapper , String name , String IDCardNo , String bankCardNo , String payerPhone){


        Map<String, String> bankAuthencationMan = authenticationMapper.bankAuthenticationInfo(bankCardNo);
        if (bankAuthencationMan == null || bankAuthencationMan.isEmpty()) {

            log.info("未查到卡号：" + bankCardNo + "的鉴权信息");

            try {
                Map<String,Object> map = new TreeMap<String,Object>();
                map.put("channelNo", channelNo);
                map.put("channelName", channelName);
                map.put("orderNo", UtilsConstant.RandCode());
                map.put("cardNo", DESUtil.encode(des3Key,bankCardNo));

                if(!UtilsConstant.strIsEmpty(payerPhone)){
                    map.put("mobile" , payerPhone);
                }


                map.put("name", DESUtil.encode(des3Key,name));
                map.put("idNo", DESUtil.encode(des3Key,IDCardNo));

                String sign = MD5.sign(JSONObject.fromObject(map) + signKey, "utf-8").toUpperCase();
                map.put("sign", sign);


                log.info("鉴权请求地址:" + authenticationURL);
                log.info("鉴权请求报文：" + map.toString());

                String content = HttpClient.post(authenticationURL  , map , null);

                log.info("鉴权响应报文：" + content);

                JSONObject json = JSONObject.fromObject(content);


                log.info("新商户：鉴权，" + map.toString() + "鉴权结果:" + json.toString());
                if (!json.getString("resCode").equals(Constants.payRetCode)) {
                    log.info("业务员新增用户： 银行信息鉴权没有通过");
                    return true;
                } else {

                    //  鉴权通过 将银行卡鉴权信息保存数据库
                    Map<String, String> bankInfo = new HashMap<>();
                    bankInfo.put("ID", UtilsConstant.getUUID());
                    bankInfo.put("IdNumber", IDCardNo);
                    bankInfo.put("RealName", name);
                    bankInfo.put("BankCardNo", bankCardNo);
                    bankInfo.put("RespCode", "00");
                    bankInfo.put("RespDesc", json.getString("resMsg"));
                    log.info("鉴权通过。将" + bankCardNo +"保存数据库");

                    authenticationMapper.addAuthencationInfo(bankInfo);

                    return false;
                }
            }catch (Exception e){
                log.error("鉴权接口发生异常：" + e.getMessage()  , e);
                return true;
            }

        } else {
            if (!name.equals(bankAuthencationMan.get("RealName")) || !IDCardNo.equals(bankAuthencationMan.get("IdNumber"))) {
                log.info("业务员新增用户：银行信息鉴权没有通过");
                return true;
            }else{
                log.info("卡号：" + bankCardNo + "查询到历史鉴权数据,并且信息一致");
            }
        }
        return false;
    }

}
