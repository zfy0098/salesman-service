package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.BankConfig;
import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.CreaditCardService;
import com.rhjf.salesman.core.util.UtilsConstant;
import com.rhjf.salesman.service.mapper.BankConfigMapper;
import com.rhjf.salesman.service.mapper.CardApplyRecordMapper;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Created by hadoop on 2017/8/9.
 */
@Transactional
public class CreaditCardServiceImpl implements CreaditCardService{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BankConfigMapper bankConfigMapper;

    @Autowired
    private CardApplyRecordMapper cardApplyRecordMapper;

    /**
     *   获取信用卡开通银行列表
     * @param paramter
     * @return
     */
    public ParamterData GetBank(ParamterData paramter){

        log.info("获取申请信用卡银行列表.");

        List<Map<String,String>> list = bankConfigMapper.getBankList();

        paramter.setList(JSONArray.fromObject(list).toString());

        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return  paramter;
    }


    /**
     *   申请信用卡
     * @param user
     * @param paramter
     * @return
     */
    public ParamterData applyForCard(LoginUser user , ParamterData paramter){

        log.info("用户申请信用卡");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -3);    //得到前3个月
            String time = sdf2.format(calendar.getTime());

            Map<String,String> map = new HashMap<>();
            map.put("BankID" , paramter.getBankID());
            map.put("ApplicantIDCardNo" , paramter.getIDCard());
            map.put("CreateTime" , time);
            List<Map<String,String>> list  = cardApplyRecordMapper.findCardApplyRecord(map);

            if(list!=null&&list.size()>0){
                log.info("身份证号：" + paramter.getIDCard() + "在90天内有过申请记录,此次申请不通过");
                paramter.setRespCode("A3");
                paramter.setRespDesc("此身份证90天内已经申请过");
                return paramter;
            }

            map.clear();
            map.put("ID", UtilsConstant.getUUID());
            map.put("ApplicantIDCardNo",paramter.getIDCard());
            map.put("ApplicantPhone" , paramter.getPhoneNumber());
            map.put("ApplicantName" , paramter.getName());
            map.put("UserID" , user.getID());
            map.put("OrganID", paramter.getAgencyNumber());
            map.put("CreateTime" , sdf.format(new Date()));
            map.put("BankID" , paramter.getBankID());

            log.info("申请信用卡用户信息:" + paramter.getIDCard() + " , " + paramter.getPhoneNumber()
                    + " , " + paramter.getName() + " , 对应银行ID：" + paramter.getBankID());

            int rsNet = cardApplyRecordMapper.insertCardApplyRecord(map);
            if(rsNet  < 0){

                log.info("用户：" + paramter.getIDCard() + "申请信用卡数据保存失败");
                paramter.setRespCode(RespCode.ServerDBError[1]);
                paramter.setRespDesc(RespCode.ServerDBError[2]);
            }else{

                log.info("用户：" + paramter.getIDCard() + "申请信用卡成功,  success");
                paramter.setRespCode(RespCode.SUCCESS[0]);
                paramter.setRespDesc(RespCode.SUCCESS[1]);

                BankConfig tbc  = bankConfigMapper.getBanConfigInfo(paramter.getBankID());
                paramter.setBankURL(tbc.getBankUrl());
            }
        } catch (Exception e) {
            paramter.setRespCode("A1");
            paramter.setRespDesc("系统异常");
            log.info("申请信用系统异常,"+ e.getMessage());
            log.error("申请信用卡系统异常 ExceptionMessage:"+e.getMessage() , e);
        }

        return paramter;
    }
}
