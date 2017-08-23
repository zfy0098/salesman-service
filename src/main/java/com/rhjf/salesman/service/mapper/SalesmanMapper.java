package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.Salesman;
import org.springframework.stereotype.Component;

/**
 * Created by hadoop on 2017/8/4.
 */
@Component
public interface SalesmanMapper {

    public int updateSalesmanInfo(Salesman salesman);

    public Salesman salesmanInfo(String userID);
}
