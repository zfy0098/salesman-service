package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.PayChannel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hadoop on 2017/8/9.
 */
@Component
public interface PayChannelMapper {



    public List<PayChannel> payChannelList();
}
