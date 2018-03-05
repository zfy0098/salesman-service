package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.PopularizeService;
import com.rhjf.salesman.service.mapper.PopularizeMapper;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2018/1/2.
 *
 * @author hadoop
 */
@Service("popularizeService")
public class PopularizeServiceImpl implements PopularizeService{


    @Autowired
    private PopularizeMapper popularizeMapper;


    @Override
    public ParamterData popularize(ParamterData paramter) {

        String deviceType = paramter.getDeviceType();

        List<Map<String,Object>> list = popularizeMapper.popularizeList(deviceType);

        paramter.setList(JSONArray.fromObject(list).toString());
        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;
    }
}
