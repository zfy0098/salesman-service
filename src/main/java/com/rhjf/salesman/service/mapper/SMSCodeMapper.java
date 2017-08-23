package com.rhjf.salesman.service.mapper;


import org.springframework.stereotype.Component;

import  java.util.Map;

/**
 * Created by hadoop on 2017/8/11.
 */
@Component
public interface SMSCodeMapper {

    public int insertSmsCode(Map<String,String> map );


    public Map<String, String> getSmsCode(String loginID);



    public int delSmsCode(String loginID);
}
