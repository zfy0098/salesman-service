package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.BankCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2017/8/10.
 */
@Component
public interface BankCodeMapper {


    public List<String> bankBranchList(Map<String,String> map);


    public Map<String,String> bankBinMap(String accountNo);


    public BankCode getBankCode(Map<String,String> map);

}
