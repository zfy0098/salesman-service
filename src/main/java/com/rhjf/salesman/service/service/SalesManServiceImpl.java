package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.Salesman;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.SalesManService;
import com.rhjf.salesman.core.util.UtilsConstant;
import com.rhjf.salesman.service.mapper.AuthenticationMapper;
import com.rhjf.salesman.service.mapper.BankCodeMapper;
import com.rhjf.salesman.service.mapper.SalesmanMapper;
import com.rhjf.salesman.service.mapper.UserBankCardMapper;
import com.rhjf.salesman.service.util.auth.AuthService;
import com.rhjf.salesman.service.util.auth.Author;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

        boolean flag = authencation(salesman.getSalesName() , salesman.getSalesCardID() ,  paramter.getBankCardNo());
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


    public  boolean authencation( String name , String IDCardNo ,String bankCardNo){
        Map<String, String> bankAuthencationMan = authenticationMapper.bankAuthenticationInfo(bankCardNo);
        if (bankAuthencationMan == null || bankAuthencationMan.isEmpty()) {

            log.info("未查到卡号：" + bankCardNo + "的鉴权信息");

            Map<String, String> authMap = new HashMap<String, String>();
            AuthService authService = new AuthService();
            authMap.put("accName", name);
            authMap.put("cardNo", bankCardNo);
            authMap.put("certificateNo", IDCardNo);
            Map<String, String> reqMap = authService.authKuai(authMap);

            log.info("新商户：鉴权，" + authMap.toString() + "鉴权结果:" + reqMap.toString());
            if (!reqMap.get("respCode").equals(Author.SUCESS_CODE)) {
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
                bankInfo.put("RespDesc", reqMap.get("respMsg"));
                log.info("鉴权通过。将" + bankCardNo +"保存数据库");

                authenticationMapper.addAuthencationInfo(bankInfo);
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
