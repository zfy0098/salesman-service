package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.UserConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hadoop on 2017/8/9.
 */
@Component
public interface UserConfigMapper {


    public int addMerchantConfig(List<UserConfig> userConfig);


    public UserConfig getUserConfig(UserConfig userConfig);


    /**
     * 修改商户费率
     **/
    public int updateUserConfig(UserConfig userConfig);
}
