package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.Salesman;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.SalesManService;
import com.rhjf.salesman.service.mapper.AuthenticationMapper;
import com.rhjf.salesman.service.mapper.BankCodeMapper;
import com.rhjf.salesman.service.mapper.SalesmanMapper;
import com.rhjf.salesman.service.mapper.UserBankCardMapper;
import com.rhjf.salesman.service.util.AuthUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
@Transactional
@Service("salesmanService")
public class SalesManServiceImpl implements SalesManService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SalesmanMapper salesmanMapper;

    @Autowired
    private BankCodeMapper bankCodeMapper;

    @Autowired
    private UserBankCardMapper userBankCardMapper;

    @Autowired
    private AuthenticationMapper authenticationMapper;

    @Autowired
    private AuthUtil authUtil;


    /**
     * 业务员修改结算信息
     *
     * @param user
     * @param paramter
     * @return
     */
    @Override
    public ParamterData updateBankInfo(LoginUser user, ParamterData paramter) {

        log.info("业务员:" + user.getLoginID() + "修改结算卡信息卡号：" + paramter.getBankCardNo());


        Salesman salesman = salesmanMapper.salesmanInfo(user.getSalesManID());

        boolean flag = authUtil.authen( authenticationMapper , salesman.getSalesName() , salesman.getSalesCardID() ,  paramter.getBankCardNo() , paramter.getPhoneNumber());
        if(flag){
            paramter.setRespCode(RespCode.BankCardInfoErroe[0]);
            paramter.setRespDesc(RespCode.BankCardInfoErroe[1]);
            return paramter;
        }

        Map<String,String> map =  userBankCardMapper.getBankName(paramter.getBankCardNo());
        if(map == null || map.isEmpty()){
            paramter.setRespCode(RespCode.BankCardInfoErroe[0]);
            paramter.setRespDesc(RespCode.BankCardInfoErroe[1]);
        }else{

            salesman.setID(salesman.getID());
            salesman.setAccountNo(paramter.getBankCardNo());
            salesman.setPhoneNumber(paramter.getPhoneNumber());
            Map<String,String> bankBinMap = bankCodeMapper.bankBinMap(salesman.getAccountNo());
            salesman.setBankName(bankBinMap.get("bankName"));
            salesman.setCardName(bankBinMap.get("cardName"));
            salesman.setBankSybol(bankBinMap.get("bankCode"));

            int x = salesmanMapper.updateSalesmanInfo(salesman);

            if (x > 0) {

                log.info("业务员:" + user.getLoginID() + "修改结算信息成功");

                paramter.setBankName(bankBinMap.get("bankName"));

                String cardName = "储蓄卡";
                if("CREDIT_CARD".equals(bankBinMap.get("cardName"))){
                    cardName = "信用卡";
                }
                paramter.setCardName(cardName);
                paramter.setBankSymbol(bankBinMap.get("bankCode"));

                paramter.setRespCode(RespCode.SUCCESS[0]);
                paramter.setRespDesc(RespCode.SUCCESS[1]);
            } else {

                log.info("业务员:" + user.getLoginID() + "修改结算信息保存数据库失败");

                paramter.setRespCode(RespCode.ServerDBError[0]);
                paramter.setRespDesc(RespCode.ServerDBError[1]);
            }
        }
        return paramter;
    }

}
