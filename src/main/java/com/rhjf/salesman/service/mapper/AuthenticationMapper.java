package com.rhjf.salesman.service.mapper;

import java.util.Map;

/**
 * Created by hadoop on 2017/8/17.
 */
public interface AuthenticationMapper {


    /**
     *   查询改银行卡号是否被鉴权过
     * @param bankNo
     * @return
     */
    public Map<String,String> bankAuthenticationInfo(String bankNo);


    /**
     *   添加鉴权信息
     * @param map
     * @return
     */
    public int addAuthencationInfo(Map<String,String> map);

}
