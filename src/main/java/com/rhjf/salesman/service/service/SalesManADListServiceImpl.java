package com.rhjf.salesman.service.service;

import com.rhjf.account.modle.domain.salesman.ParamterData;
import com.rhjf.account.modle.domain.salesman.SalesManADList;
import com.rhjf.salesman.core.constants.RespCode;
import com.rhjf.salesman.core.service.SalesManADListService;
import com.rhjf.salesman.core.util.UtilsConstant;
import com.rhjf.salesman.service.mapper.SalesManADMapper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by hadoop on 2017/8/4.
 */
@Transactional
public class SalesManADListServiceImpl implements SalesManADListService {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private SalesManADMapper salesManADMapper;


    /**
     *   首页轮播图
     * @param paramter
     * @return
     */
    public ParamterData adlist(ParamterData paramter){


        List<SalesManADList> adlist = salesManADMapper.adlist();

        Object[] content = new Object[adlist.size()];
        Object[] imgurl = new Object[adlist.size()];
        Object[] adurl = new Object[adlist.size()];
        Object[] title = new Object[adlist.size()];


        JSONObject json = new JSONObject();
        for(int i = 0; i< adlist.size() ; i++){
            content[i] = UtilsConstant.ObjToStr(adlist.get(i).getContent());
            imgurl[i] =  UtilsConstant.ObjToStr(adlist.get(i).getImgURL());
            adurl[i] =  UtilsConstant.ObjToStr(adlist.get(i).getADURL());
            title[i] = UtilsConstant.ObjToStr(adlist.get(i).getTitle());
        }

        json.put("content" , content);
        json.put("imgurl" , imgurl);
        json.put("adurl" , adurl);
        json.put("title" , title);


        paramter.setList(json.toString());
        paramter.setRespCode(RespCode.SUCCESS[0]);
        paramter.setRespDesc(RespCode.SUCCESS[1]);

        return paramter;

    }


}
