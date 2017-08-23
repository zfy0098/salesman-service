package com.rhjf.salesman.service.mapper;

import java.util.List;
import java.util.Map;

import com.rhjf.account.modle.domain.salesman.SalesManBulletin;
import org.springframework.stereotype.Component;

/**
 * Created by hadoop on 2017/8/4.
 */
@Component
public interface SalesManBulletinMapper {



    public List<SalesManBulletin> salesManBulletinList(Map<String,String> map);



    public Integer unreadCount(String salesManID);


    public SalesManBulletin getSalesmanBulletinDetail(String ID);


}
