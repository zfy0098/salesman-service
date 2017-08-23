package com.rhjf.salesman.service.mapper;

import com.rhjf.account.modle.domain.salesman.YMFQRCode;

import java.util.List;
import java.util.Map;
/**
 * Created by hadoop on 2017/8/21.
 */
public interface YMFQRCodeMapper {


    public YMFQRCode getQRCodeBindInfo(String code);


    public List<YMFQRCode> userYMFlist(Map<String,String> map);


    public int updateBindedInfo(YMFQRCode QRCode);
}
