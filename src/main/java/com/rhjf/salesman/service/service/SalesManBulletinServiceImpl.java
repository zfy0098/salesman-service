package com.rhjf.salesman.service.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.rhjf.account.modle.domain.salesman.LoginUser;
import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.SalesManBulletin;
import com.rhjf.salesman.core.service.SalesManBulletinService;
import com.rhjf.salesman.service.mapper.SalesManBulletinMapper;

import net.sf.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by hadoop on 2017/8/4.
 */
@Transactional
public class SalesManBulletinServiceImpl implements SalesManBulletinService {


    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private SalesManBulletinMapper salesManBulletinMapper;


    /**
     * 显示公告列表
     *
     * @param user
     * @param paramter
     * @return
     */
    public ParamterData salesmanBulletinList(LoginUser user, ParamterData paramter) {


        Map<String, String> map = new HashMap<>();

        map.put("agentID", user.getAgentID());
        map.put("salesmanID", user.getSalesManID());

        List<SalesManBulletin> list = salesManBulletinMapper.salesManBulletinList(map);

        Integer count = salesManBulletinMapper.unreadCount(user.getSalesManID());

        paramter.setCount(String.valueOf(count));
        paramter.setList(JSONObject.fromObject(list).toString());


        return paramter;
    }


    /**
     * 显示公告详细
     *
     * @param user
     * @param paramter
     * @return
     */
    public ParamterData salesmanBulletinDetail(LoginUser user, ParamterData paramter) {


        SalesManBulletin salesManBulletin = salesManBulletinMapper.getSalesmanBulletinDetail(paramter.getID());

        paramter.setList(JSONObject.fromObject(salesManBulletin).toString());

        return paramter;
    }
}
