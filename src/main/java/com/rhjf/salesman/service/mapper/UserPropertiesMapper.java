package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.UserProperties;

/**
 * Created by hadoop on 2017/8/18.
 */
public interface UserPropertiesMapper {


    public UserProperties getUserProperties(String userID);


    public int addUserProperties(UserProperties userProperties);


    public int updateUserProperties(UserProperties userProperties);
}
