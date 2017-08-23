package com.rhjf.salesman.service.mapper;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
/**
 * Created by hadoop on 2017/8/9.
 */

@Component
public interface CardApplyRecordMapper {


    public List<Map<String,String>> findCardApplyRecord(Map<String,String> map);


    public int insertCardApplyRecord(Map<String,String> map);

}
