package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.BankConfig;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/8/9.
 */

@Component
public interface BankConfigMapper {


    public List<Map<String, String>> getBankList();


    public BankConfig getBanConfigInfo(String ID);


}
