package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.SalesManADList;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hadoop on 2017/8/4.
 */
@Component
public interface SalesManADMapper {



    public List<SalesManADList> adlist();
}
