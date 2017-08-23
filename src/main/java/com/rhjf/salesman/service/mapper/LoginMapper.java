package com.rhjf.salesman.service.mapper;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.rhjf.account.modle.domain.salesman.LoginUser;

@Component
public interface LoginMapper {


    public LoginUser login(Map<String, String> map);

    /**
     * 修改密码
     **/
    public int updatepwd(LoginUser user);

    /**
     * 添加商户
     **/
    public int addMerchant(LoginUser user);


    /**
     * 商户列表
     **/
    public List<Map<String, String>> merchantlist(Map<String, String> map);


    /**
     * 某一个商户为业务员制造的收益
     **/
    public Map<String, String> queryMerchantTotalAmount(Map<String, String> map);


    /**
     * 查询某一个商户拓客数量
     **/
    public Integer merchantTokerCount(Map<String, String> map);


    /**
     * 更新用户登录信息
     **/
    public int updateUserLoginInfo(LoginUser user);


    /**
     * 用户提现
     **/
    public Integer txProfit(Map<String, Object> map);


    /**
     * 提现列表
     **/
    public List<Map<String, String>> TxRecordList(Map<String, String> map);


    /**
     * 更新照片
     **/
    public int updatePhotoInfo(Map<String, String> map);


    public int updateUserBankStatus(LoginUser user);


    /**
     * 修改商户等级
     **/
    public int updateMerchantLevel(Map<String, String> map);


    /**
     * 查询商户拓
     **/
    public int tokerCount(String userID);
}
