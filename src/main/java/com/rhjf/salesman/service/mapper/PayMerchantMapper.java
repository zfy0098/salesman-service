package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.PayMerchant;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hadoop on 2017/8/11.
 */

@Component
public interface PayMerchantMapper {


    /**
     *   保存平台商户信息
     * @param list
     * @return
     */
    public int saveMerchantInfo(List<PayMerchant> list);



}
