package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.PayMerchant;
import com.rhjf.salesman.core.constants.Constants;
import com.rhjf.salesman.core.service.TestService;
import com.rhjf.salesman.service.mapper.PayMerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hadoop on 2017/8/15.
 */
public class TestServiceImpl implements TestService {


    @Autowired
    private PayMerchantMapper mapper;


    public ParamterData test(ParamterData paramter){
        List<PayMerchant> list = new ArrayList<>();

        PayMerchant payMerchant = new PayMerchant();
        payMerchant.setMerchantID("111");
        payMerchant.setMerchantName("测试");
        payMerchant.setSignKey("11");
        payMerchant.setDESKey("11");
        payMerchant.setQueryKey("11");
        payMerchant.setUserID("55");
        payMerchant.setPayType(Constants.PayChannelWXScancode);

        list.add(payMerchant);

        payMerchant = new PayMerchant();
        payMerchant.setMerchantID("111");
        payMerchant.setMerchantName("测试");
        payMerchant.setSignKey("22");
        payMerchant.setDESKey("22");
        payMerchant.setQueryKey("11");
        payMerchant.setUserID("55");
        payMerchant.setPayType(Constants.payChannelAliScancode);

        list.add(payMerchant);

        mapper.saveMerchantInfo(list);
        return paramter;
    }
}
