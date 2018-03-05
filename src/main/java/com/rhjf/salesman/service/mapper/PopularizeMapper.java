package com.rhjf.salesman.service.mapper;

import java.util.List;
import java.util.Map;

/**
 * Created by hadoop on 2018/1/2.
 *
 * @author hadoop
 */
public interface PopularizeMapper {


    public List<Map<String,Object>> popularizeList(String deviceType);
}
