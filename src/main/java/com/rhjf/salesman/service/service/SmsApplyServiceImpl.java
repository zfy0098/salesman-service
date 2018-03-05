package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.SmsApplyService;
import com.rhjf.salesman.core.util.HttpClient;
import com.rhjf.salesman.core.util.MD5;
import com.rhjf.salesman.core.util.UtilsConstant;
import com.rhjf.salesman.service.mapper.LoginMapper;
import com.rhjf.salesman.service.mapper.SMSCodeMapper;
import com.rhjf.salesman.service.util.SmsUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by hadoop on 2017/8/11.
 */
@Transactional
public class SmsApplyServiceImpl implements SmsApplyService {


    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private SMSCodeMapper smsCodeMapper;

    @Autowired
    private LoginMapper loginMapper;



    @Value("${SMSCODEAPPID}")
    private String smsCodeAppID;

    @Value("${ZhuCe}")
    private String zhuCe;

    /**
     *    业务员录入商户 获取短信验证码
     * @param repData
     * @return
     */
    @Override
    public ParamterData send(ParamterData repData){

        log.info("商户手机号：" + repData.getMerchantLoginID() + "获取验证码");


        Map<String, String> userMap = new HashMap<>();
        userMap.put("loginid",repData.getMerchantLoginID());
        LoginUser user2 = loginMapper.login(userMap);

        if(user2 != null){

            log.info("手机号：" + repData.getMerchantLoginID() + "已经被注册， 此次添加失败");

            repData.setRespCode(RespCode.RegisterError[0]);
            repData.setRespDesc(RespCode.RegisterError[1]);
            return repData;
        }


        //生成验证码
        String smsCode = UtilsConstant.GetSmsCode();
        Map<String,String> map = new HashMap<>();
        map.put("id" , UtilsConstant.getUUID());
        map.put("phone" , repData.getMerchantLoginID());
        map.put("smsCode" , smsCode);

        int nRet = smsCodeMapper.insertSmsCode(map);
        if(nRet < 0){
            log.info("记录手机号校验码失败，手机号=【"+repData.getMerchantLoginID()+"】");
            repData.setRespCode("96");
            repData.setRespDesc("手机号校验失败");
            return repData;
        }


        try {
            String url = "http://api.app2e.com/smsBigSend.api.php";

            Map<String, Object> smsMap = new HashMap<String, Object>();

            String msg = "【哆米付】感谢您选择哆米付，" + smsCode + "是您的注册验证码，更多内容请关注微信公众号“哆米付”";

            smsMap.put("username", "duomipay");
            smsMap.put("pwd", MD5.sign("rPJFaxoA", "utf-8"));
            smsMap.put("p", repData.getMerchantLoginID());
            smsMap.put("charSetStr", "utf");
            smsMap.put("msg", msg);


            String result = HttpClient.post(url, smsMap, "1");

            log.info("获取验证码响应报文：" + result);

            JSONObject json = JSONObject.fromObject(result);


            Integer status = json.getInt("status");

            if (status == 100) {
                repData.setRespCode("00");
                repData.setRespDesc("短信已发送");
            } else {
                repData.setRespCode("96");
                repData.setRespDesc(json.getString("message"));
            }
        }catch (Exception e){
            log.error("获取短信验证码失败, " , e.getMessage());
            repData.setRespCode(RespCode.NETWORKError[0]);
            repData.setRespCode(RespCode.NETWORKError[1]);
        }

        /** 发送短信 **/
//        JSONObject json = SmsUtil.sendSMS(repData.getMerchantLoginID() , smsCode, zhuCe , "5" , smsCodeAppID);
//        if(json.getInt("code")==0){
//            repData.setRespCode(RespCode.SUCCESS[0]);
//            repData.setRespDesc("短信已发送");
//        }else{
//            repData.setRespCode("96");
//            repData.setRespDesc(json.getString("message"));
//        }
        return repData;
    }


    /**
     *   验证短信验证码
     * @param paramterData
     * @return
     */
    @Override
    public ParamterData verificationSMSCode(ParamterData paramterData){


        String loginID = paramterData.getMerchantLoginID();
        String smsCode = paramterData.getSmsCode();
        Map<String,String> codeMap = smsCodeMapper.getSmsCode(loginID);


        if(codeMap == null){

            log.info("商户：" + paramterData.getMerchantLoginID() + "验证码数据为空");

            paramterData.setRespCode(RespCode.SMSCodeError[0]);
            paramterData.setRespDesc(RespCode.SMSCodeError[1]);
            return paramterData;
        }



        log.info("验证新增用户的手机验证码是否正确:"+ paramterData.getMerchantLoginID() + ",输入验证码:" + smsCode +
                " , 系统保存验证码：" + codeMap.get("smsCode"));


        if(codeMap != null && smsCode.equals(codeMap.get("smsCode"))){
            smsCodeMapper.delSmsCode(loginID);
            paramterData.setRespCode(RespCode.SUCCESS[0]);
            paramterData.setRespDesc(RespCode.SUCCESS[1]);
        } else{
            paramterData.setRespCode(RespCode.SMSCodeError[0]);
            paramterData.setRespDesc(RespCode.SMSCodeError[1]);
        }
        return paramterData;
    }





}
