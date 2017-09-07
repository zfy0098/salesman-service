package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.UserBankCard;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public interface UserBankCardMapper {


    /**
     * 添加商户结算信息
     **/
    public int addMerchantBankCardInfo(UserBankCard bankCardInfo);


    /**
     * 根据银行卡号获取银行名称
     **/
    public Map<String, String> getBankName(String accountNo);


    /**
     * 为商户添加信用卡
     **/
    public int addCreditCardNo(UserBankCard bankCard);


    /**
     * 获取商户结算卡信息
     **/
    public UserBankCard getUserBankCardInfo(String userID);
}
