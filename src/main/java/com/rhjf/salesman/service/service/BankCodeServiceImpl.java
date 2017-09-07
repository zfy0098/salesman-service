package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.BankCodeService;
import com.rhjf.salesman.service.mapper.BankCodeMapper;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by hadoop on 2017/8/10.
 */
@Transactional
public class BankCodeServiceImpl implements BankCodeService {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BankCodeMapper bankCodeMapper;


    /**
     *  获取支行名称列表
     * @param paramter
     * @return
     */
    public ParamterData bankBranchList(ParamterData paramter){

        String bankProv = paramter.getBankProv();
        String bankCity = paramter.getBankCity();
        String accountNo = paramter.getBankCardNo();

        log.info("获取支行名称列表： 所在省份:" + bankProv + " , 城市:" + bankCity + ", 卡号：" + accountNo);


        Map<String,String> bankBinMap = bankCodeMapper.bankBinMap(accountNo);

        if(bankBinMap == null || bankBinMap.isEmpty()){

            log.info("获取支行列表失败: 所在省份:" + bankProv + " , 城市:" + bankCity + ", 卡号：" + accountNo);

            paramter.setRespCode(RespCode.BankCardInfoErroe[0]);
            paramter.setRespDesc("匹配银行名称失败，请更换银行卡。");

        }else{

            Map<String,String> map = new HashMap<>();
            map.put("bankName" , bankBinMap.get("bankName"));
            map.put("bankProv" , bankProv);
            map.put("bankCity" , bankCity);

            log.info("获取支行名称：" +  bankBinMap.get("bankName") + "， 城市:" + bankCity + " , 省份:" + bankProv);

            List<String> list =  bankCodeMapper.bankBranchList(map);

            paramter.setList(JSONArray.fromObject(list).toString());

            paramter.setRespCode(RespCode.SUCCESS[0]);
            paramter.setRespDesc(RespCode.SUCCESS[1]);
        }
        return paramter;
    }
}
